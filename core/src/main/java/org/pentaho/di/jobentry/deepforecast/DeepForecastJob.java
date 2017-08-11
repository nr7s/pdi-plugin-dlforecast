package org.pentaho.di.jobentry.deepforecast;

import org.apache.commons.io.FileUtils;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVNLinesSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DeepForecastJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeepForecastJob.class);
    private static void initFiles(String temp) {
        tempDir = new File(temp + File.separator + "temp");
        trainFeaturesFile = new File(tempDir, "train_features.csv");
        trainLabelsFile = new File(tempDir, "train_labels.csv");
        testFeaturesFile = new File(tempDir, "test_features.csv");
        testLabelsFile = new File(tempDir, "test_labels");
        resultsDir = new File(getOutput());
    }

    private static File tempDir;
    private static File trainFeaturesFile;
    private static File trainLabelsFile;
    private static File testFeaturesFile;
    private static File testLabelsFile;
    private static File resultsDir;

    private static String filename;
    private static String forecastSteps;
    private static String configPath;
    private static String toLoadFile;
    private static String output;
    private static String modelName;
    private static String target;
    private static String temp;

    private static int numOfVariables = 0;

    public DeepForecastJob(String filename, String forecastSteps, String toLoadFile, String output, String modelName, String configPath, String target, String temp) {
        this.filename = filename;
        this.forecastSteps = forecastSteps;
        this.configPath = configPath;
        this.toLoadFile =  toLoadFile;
        this.output = output;
        this.modelName = modelName;
        this.target = target;
        this.temp = temp;
    }

    private static Updater getUpdater(String updater) {
        switch (updater) {
            case "NESTEROVS":
                return Updater.NESTEROVS;
            case "RMSPROP":
                return Updater.RMSPROP;
            case "ADAGRAD":
                return Updater.ADAGRAD;
        }
        return Updater.NESTEROVS;
    }

    private static Activation getActivation(String activation) {
        switch (activation) {
            case "TANH":
                return Activation.TANH;
            case "SOFTSIGN":
                return Activation.SOFTSIGN;
        }
        return Activation.TANH;
    }

    public static void process() throws Exception {
        initFiles(getTemp());

        List<String> rawStrings = new ArrayList<>();
        Path rawPath = Paths.get(getFilename());
        rawStrings = Files.readAllLines(rawPath, Charset.defaultCharset());
        String header = rawStrings.get(0);
        List<String> cols = Arrays.asList(header.split(","));
        rawStrings.remove(0);
        setNumOfVariables(rawStrings);
        Properties prop = new Properties();
        if (getConfigPath() != null) {
            prop.load(FileUtils.openInputStream(new File(getConfigPath())));
        }
        if(!resultsDir.exists()) {
            resultsDir.mkdirs();
        }

        long id = System.currentTimeMillis() / 1000;
        Path check = Paths.get(resultsDir + File.separator + "check" + id + ".csv");
        Path output = Paths.get(resultsDir + File.separator + "forecast" + id + ".csv");

        Updater updater = getUpdater(prop.getProperty("updater", "NESTEROVS"));
        Activation activation = getActivation(prop.getProperty("activation", "TANH"));
        int nEpochs = Integer.parseInt(prop.getProperty("nEpochs", "50"));
        int miniBatchSize = Integer.parseInt(prop.getProperty("miniBatchSize", "32"));
        int hiddenNodes = Integer.parseInt(prop.getProperty("hiddenNodes", "10"));
        int timeSeriesSize = Integer.parseInt(prop.getProperty("timeSeriesSize", "30"));
        int tBPTTLength = Integer.parseInt(prop.getProperty("tBPTTLength", "30"));
        int totalSize = rawStrings.size() - timeSeriesSize;
        int numberOfBatches = totalSize / miniBatchSize;
        int nrTrainBatches = numberOfBatches - 1;
        int nrTestBatches = 1;
        int trainSize = nrTrainBatches * miniBatchSize;
        int testSize = nrTestBatches * miniBatchSize;
        int lostSize = totalSize - (trainSize + testSize);
        List<Integer> targetIdxs = new ArrayList<>();
        List<String> targets = null;

        if (getTarget() != null) {
            targets = Arrays.asList(getTarget().split(","));
            for (String t: targets) {
                if (cols.contains(t)) {
                    targetIdxs.add(cols.indexOf(t));
                }
            }
            if (targetIdxs.size() == 0) {
                throw new Exception("Found no columns matching the specified names");
            }
        }

        if (Integer.parseInt(getForecastSteps()) >= timeSeriesSize) {
            throw new Exception("Forecast Steps (from dialog) need to be smaller than timeSeriesSize(in config file)");
        }

        if (miniBatchSize > totalSize / 2) {
            throw new Exception("miniBatchSize might be too big for your dataset");
        }

        prepareTrainAndTest(trainSize, testSize, lostSize, timeSeriesSize, rawStrings, targetIdxs);

        SequenceRecordReader trainFeatures = new CSVNLinesSequenceRecordReader(timeSeriesSize);
        trainFeatures.initialize(new FileSplit(trainFeaturesFile));
        SequenceRecordReader trainLabels = new CSVNLinesSequenceRecordReader(1);
        trainLabels.initialize(new FileSplit(trainLabelsFile));

        DataSetIterator trainDataIter = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, miniBatchSize, -1, true, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

        NormalizerMinMaxScaler normalizer = new NormalizerMinMaxScaler(0, 1);
        normalizer.fitLabel(true);
        normalizer.fit(trainDataIter);
        trainDataIter.reset();

        SequenceRecordReader testFeatures = new CSVNLinesSequenceRecordReader(timeSeriesSize);
        testFeatures.initialize(new FileSplit(testFeaturesFile));
        SequenceRecordReader testLabels = new CSVNLinesSequenceRecordReader(1);
        testLabels.initialize(new FileSplit(testLabelsFile));

        DataSetIterator testDataIter = new SequenceRecordReaderDataSetIterator(testFeatures, testLabels, miniBatchSize, -1, true, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

        trainDataIter.setPreProcessor(normalizer);
        testDataIter.setPreProcessor(normalizer);

        MultiLayerNetwork net;
        if (getToLoadFile() != null) {
            File load = new File(getToLoadFile());
            net = ModelSerializer.restoreMultiLayerNetwork(load);
        } else {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .seed(8347480)
                    .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                    .iterations(1)
                    .weightInit(WeightInit.XAVIER)
                    .learningRate(Double.parseDouble(prop.getProperty("learningRate", "0.01")))
                    .updater(updater)
                    .momentum(Double.parseDouble(prop.getProperty("momentum", "0.9")))
                    .rmsDecay(Double.parseDouble(prop.getProperty("rmsDecay", "0.95")))
                    .regularization(Boolean.parseBoolean(prop.getProperty("regularization", "false")))
                    .dropOut(Double.parseDouble(prop.getProperty("dropOut", "0.0")))
                    .l1(Double.parseDouble(prop.getProperty("l1", "0.0")))
                    .l2(Double.parseDouble(prop.getProperty("l2", "0.0")))
                    .list()
                    .layer(0, new GravesLSTM.Builder().activation(activation).nIn(numOfVariables).nOut(hiddenNodes)
                            .build())
                    .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .activation(Activation.IDENTITY).nIn(hiddenNodes).nOut(getTarget() == null ?
                                    numOfVariables : targetIdxs.size()).build())
                    .backpropType(Boolean.parseBoolean(prop.getProperty("truncatedBPTT", "false")) ?
                            BackpropType.TruncatedBPTT : BackpropType.Standard)
                    .tBPTTForwardLength(tBPTTLength).tBPTTBackwardLength(tBPTTLength)
                    .build();

            net = new MultiLayerNetwork(conf);
            net.init();

            for (int i = 0; i < nEpochs; i++) {
                net.fit(trainDataIter);
                trainDataIter.reset();
                LOGGER.info("Epoch " + i + " complete.");
            }
        }

        DataSet t;
        INDArray checkFit = null;

        while (trainDataIter.hasNext()) {
            t = trainDataIter.next();
            checkFit = net.rnnTimeStep(t.getFeatureMatrix());
        }
        normalizer.revertLabels(checkFit);
        INDArray checkArr = checkFit.get(NDArrayIndex.interval(checkFit.size(0) - 1, checkFit.size(0)), NDArrayIndex.all(), NDArrayIndex.all()).getRow(0);

        t = testDataIter.next();
        INDArray predicted = net.rnnTimeStep(t.getFeatureMatrix());
        normalizer.revertLabels(predicted);
        INDArray result = predicted.get(NDArrayIndex.interval(0, 1), NDArrayIndex.all(), NDArrayIndex.all()).getRow(0);

        genHeader(output, header, targetIdxs, targets);
        for (int i = 1; i <= Integer.parseInt(getForecastSteps()); i++) {
            int j;
            String outputString = "";
            for (j = 0; j < result.size(0) - 1; j++) {
                outputString = outputString.concat(result.getDouble(j, i) + ",");
            }
            Files.write(output, outputString.concat(result.getDouble(j, i) + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }

        genHeader(check, header, targetIdxs, targets);
        logCheck(checkArr, result, check);

        if (getModelName() != null) {
            File model = new File(resultsDir + File.separator + getModelName() + ".zip");
            ModelSerializer.writeModel(net, model, Boolean.parseBoolean(prop.getProperty("saveUpdater", "false")));
        }

        FileUtils.deleteDirectory(tempDir);
    }

    private static void logCheck(INDArray checkArr, INDArray result, Path check) throws IOException {
        for (int i = 0; i <= Integer.parseInt(getForecastSteps()); i++) {
            int j;
            String outputString = "";
            for (j = 0; j < checkArr.size(0) - 1; j++) {
                outputString = outputString.concat(checkArr.getDouble(j, i) + ",");
            }
            Files.write(check, outputString.concat(checkArr.getDouble(j, i) + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
        int j;
        String outputString = "";
        for (j = 0; j < result.size(0) - 1; j++) {
            outputString = outputString.concat(result.getDouble(j, 0) + ",");
        }
        Files.write(check, outputString.concat(result.getDouble(j, 0) + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
    }

    private static void genHeader(Path path, String header, List<Integer> targetIdxs, List<String> targets) throws IOException {
        if ( getTarget() == null ) {
            Files.write(path, header.concat(System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        } else {
            int outI = 0;
            while (outI < targetIdxs.size() - 1){
                Files.write(path, targets.get(outI).concat(",").getBytes(),
                        StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                outI++;
            }
            Files.write(path, targets.get(outI).concat(System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
    }

    private static void prepareTrainAndTest(int trainSize, int testSize, int lostSize, int timeSeriesSize, List<String> rawStrings, List<Integer> targetIdxs) throws IOException {
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        } else {
            throw new IOException("Failed trying to create a temp folder, probably the temporary directory you specified already has a folder named 'temp'");
        }

        for (int i = lostSize; i < trainSize + lostSize; i++) {
            Path featuresPath = Paths.get(trainFeaturesFile.getAbsolutePath());
            Path labelsPath = Paths.get(trainLabelsFile.getAbsolutePath());
            for (int j = 0; j < timeSeriesSize; j++) {
                Files.write(featuresPath, rawStrings.get(i + j).concat(System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            }
            prepareLabels(targetIdxs, labelsPath, rawStrings, timeSeriesSize, i);
        }

        for (int i = trainSize + lostSize; i < testSize + trainSize + lostSize; i++) {
            Path featuresPath = Paths.get(testFeaturesFile.getAbsolutePath());
            Path labelsPath = Paths.get(testLabelsFile.getAbsolutePath());
            for (int j = 0; j < timeSeriesSize; j++) {
                Files.write(featuresPath, rawStrings.get(i + j).concat(System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            }
            prepareLabels(targetIdxs, labelsPath, rawStrings, timeSeriesSize, i);
        }
    }

    private static void prepareLabels(List<Integer> targetIdxs, Path labelsPath,List<String> rawStrings, int timeSeriesSize, int i) throws IOException{
        if (targetIdxs.size() > 0) {
            if (targetIdxs.size() == 1) {
                Files.write(labelsPath, rawStrings.get(i + timeSeriesSize).split(",")[targetIdxs.get(0)].concat(System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            } else {
                for (int idx : targetIdxs) {
                    Files.write(labelsPath, rawStrings.get(i + timeSeriesSize).split(",")[idx].concat(",").getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                }
                Files.write(labelsPath, System.lineSeparator().getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            }
        } else {
            Files.write(labelsPath, rawStrings.get(i + timeSeriesSize).concat(System.lineSeparator()).getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        }
    }

    private static void setNumOfVariables(List<String> rawStrings) {
        numOfVariables = rawStrings.get(0).split(",").length;
    }

    public static String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public static String getForecastSteps() {
        return forecastSteps;
    }

    public void setForecastSteps(String forecastSteps) {
        this.forecastSteps = forecastSteps;
    }

    public static String getToLoadFile() {
        return toLoadFile;
    }

    public static void setToLoadFile(String toLoadFile) {
        DeepForecastJob.toLoadFile = toLoadFile;
    }

    public static String getOutput() {
        return output;
    }

    public static void setOutput(String output) {
        DeepForecastJob.output = output;
    }

    public static String getModelName() {
        return modelName;
    }

    public static void setModelName(String modelName) {
        DeepForecastJob.modelName = modelName;
    }

    public static String getConfigPath() {
        return configPath;
    }

    public static void setConfigPath(String configPath) {
        DeepForecastJob.configPath = configPath;
    }

    public static String getTemp() {
        return temp;
    }

    public static void setTemp(String temp) {
        DeepForecastJob.temp = temp;
    }

    public static String getTarget() {
        return target;
    }

    public static void setTarget(String target) {
        DeepForecastJob.target = target;
    }
}
