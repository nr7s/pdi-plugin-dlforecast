<a href="https://www.pentaho.com/">
    <img src="https://trello-attachments.s3.amazonaws.com/59354adb4d8effac88d0b57c/59a53d0198c33653a118f940/95e151ea95ba2f020beda2612ed1212a/pentaho-HGC-logo.png" align="right" height="50" />
</a>

# pdi-plugin-dlforecast
Time series forecasting plugin for [Pentaho Data Integration](http://community.pentaho.com/projects/data-integration/). Implemented using a simple, configurable [DL4J](https://deeplearning4j.org/) LSTM network.

## Table of content

- [Pre-requisites](#pre-requisites)
- [Installation](#installation)
- [Using the plugin](#using-the-plugin)
- [Extras](#extras)
- [To implement](#to-implement)

## Pre-requisites 
* Maven, version 3+
* Java JDK 1.8
* [Pentaho Data Integration Kettle](http://community.pentaho.com/projects/data-integration/)
* This [settings.xml](https://github.com/pentaho/maven-parent-poms/blob/master/maven-support-files/settings.xml) 
in your <user-home>/.m2 directory

## Installation
This refers to Ubuntu but if you are used to your OS you can easily adapt. Any doubts raise an issue or contact me.

1. After cloning/downloading go to the plugin main folder and build the plugin
```bash
git clone https://github.com/neuronist/pdi-plugin-dlforecast
cd pdi-plugin-dlforecast
mvn package
```
2. After successfully building the plugin go to the generated folder and extract the zip with the plugin and all the needed dependecies.
```bash
cd assemblies/plugin/target/
unzip kettle-deepforecast-plugin-8.0-SNAPSHOT.zip
```
3. After unzipping the generated plugin, just drag the plugins folder in your Spoon installation directory, in my case it would end up like this.

![installation1](https://user-images.githubusercontent.com/24592596/29178313-16034a38-7de9-11e7-8004-05215482d6de.png)

4. After that just run your Spoon and the plugin Job Step will be under the Big Data category as DeepForecast.

## Using the plugin

1. Before anything, make sure your data CSV is in the proper format. This means it has only numerical columns separated by commas with a single header line identifying the columns and is sorted by time starting with the oldest values to the most recent. The CSV should **not** contain a date column or a time column. You can check the *sample/data* folder in this repository for properly formatted CSV's. If you can also use the PDI to change your CSV by sorting it and removing undesired columns.

2. You can drag the plugin step into your job in whichever way you desire. This is a sample example.

![use1](https://user-images.githubusercontent.com/24592596/29707429-21ef0c66-897d-11e7-9b04-6fec5f8d48c9.png)

3. After setting yor job layout you can configure the Deep Forecast step by double clicking on it and changing the properties in the following dialog.

![use2](https://user-images.githubusercontent.com/24592596/29708070-5ed3e032-897f-11e7-8756-0478ee0e7340.png)

4. After filling in the desired settings, you are able to run your job. While training the model you can check the logging tab for a log of each epoch completion.

![use3](https://user-images.githubusercontent.com/24592596/29708501-b2d1b4a6-8980-11e7-81cc-d08aecdc7595.png)

5. When the model is trained, the predictions are made and the respective check and forecast CSV's will be saved at the output path that you specified. Identified like checkXXXXXXXXXX.csv and forecastXXXXXXXXXX.csv, where XXXXXXXXXX is a numerical identifier to distinguish results in case you want generate them in the same folder.

6. After that you can use your desired plotting tool to check the results. In the sample folder of this repository there's a simple python script to plot the results with. Plotting the results from the example before we would see a graph like this. Warning: this python script is just an example. If you wish to use it make sure your have python and the imported modules installed in your environment.

![use4](https://user-images.githubusercontent.com/24592596/29708921-619be636-8982-11e7-8f58-34580c0e42d7.png)

This is not a very good result but you're using a plugin that aims for some generality when dealing with time series. Meaning you can use this for milisecond-scale sensor data from a machine, daily temperature from geological places, stock prices and so on. The results and way the network works is dependent on each problem and data set. Thus you will need to work with the config.properties file and fit it to your data. 

From the default configurations file if you change the miniBatchSize to 16, testRatio to 0.1, learningRate to 0.007 and activation to SOFTSIGN you will get a better fit to the data and possibly a better forecast. If you run the plugin again. Here's the result with these modifications.

![use5](https://user-images.githubusercontent.com/24592596/29710620-fad00912-8988-11e7-9643-a280dc6d8cdc.png)

## Extras

* You don't need to specify a path to the configurations file the defaults will be used. The config.properties file in the sample folder of this repository is documented and you can use that as a guideline to tune your model.

* **Prediction Columns** is used to forecast on single or specific columns of a multivariate dataset.
Imagine you had a forex dataset with an header like *Close,High,Low*. If you wanted to train a model using the 3 columns but forecast only the *Low* value you would write in the **Prediction Columns** *Low*. If for instance you wanted to forecast both *Close* and *High* you would write *Close,High*. **Caution:** this is a very rudimentary feature, you need to be careful of whitespaces and case sensitive when setting the columns to predict. If you leave this empty it will predict over every variable, sometimes this doesn't work that well, when the different columns don't have a good correlation.

* If you want to save your model for later use you just need to set a **Model Name** in the dialog, you can leave it empty if you don't wish to save the trained model. A zip will be generated with that name in the specified output folder, next to the generated CSV's.

* You can also load a model by specifying the path in **Model File Path** to a compatible zip generated by this plugin or another deeplearning4j application. Be careful as the model needs to be compatible with the data. If you don't specify a path here a new model will be trained.

* For now the **Run Configuration** Spark is not working as intended so please only use the Local setting.

## To implement

This is still in a very early phase of development and some changes are planned:

* Handling of categorical columns
* Better preprocessing of time series data (taking seasonal and trend components)
* Better loading of models in terms of compatibility
* Providing a Training UI like DL4J has
* Enabling random search through an hyperparameter space (using Arbiter for example)
* An evaluation method based on metrics, the method of checking if the predictions are fitting the that was chosed with the ease of usage for the user in mind
* Full network customization, multiple layers and settings for each layer
* Implementing a recursive forecast method, right now we're doing a direct prediction and that limits the forecast to be smaller than the timeSeriesSize set in the properties file
* Add support for distributed training over Apache Spark for example






