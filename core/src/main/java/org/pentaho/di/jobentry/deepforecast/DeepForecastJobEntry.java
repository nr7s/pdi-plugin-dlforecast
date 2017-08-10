/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.jobentry.deepforecast;

import java.util.List;

import jdk.nashorn.internal.runtime.ConsString;
import jxl.demo.XML;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.w3c.dom.Node;

@JobEntry(
        id = "DeepForecast",
        name = "DeepForecast.Name",
        description = "DeepForecast.TooltipDesc",
        image = "icon.svg",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.BigData",
        i18nPackageName = "org.pentaho.di.jobentries.deepforecast"
)
public class DeepForecastJobEntry extends JobEntryBase implements Cloneable, JobEntryInterface {
    private static Class<?> PKG = DeepForecastJobEntry.class; // for i18n purposes $NON-NLS-1$

    private String filename;
    private String output;
    private String temp;
    private String forecastSteps;
    public String configPath;
    public String toLoadFile;
    public String modelName;
    public String target;
    private int runConfiguration;

    public static final int RUN_CONFIGURATION_LOCAL = 0;
    public static final int RUN_CONFIGURATION_SPARK = 1;

    public static final String[] runConfigurationCode = {"LOCAL", "SPARK", };

    public static final String[] runConfigurationOptions = {
            BaseMessages.getString( PKG, "DeepForecast.Local.Label" ),
            BaseMessages.getString( PKG, "DeepForecast.Spark.Label" ), };

    private static String DEFAULT_FORECAST_STEPS = "3";

    public DeepForecastJobEntry(String name ) {
        super( name, "" );
        filename = null;
        output = null;
        temp = null;
        forecastSteps = DEFAULT_FORECAST_STEPS;
        configPath = null;
        toLoadFile = null;
        modelName = null;
        target = null;
        runConfiguration = RUN_CONFIGURATION_LOCAL;
    }

    public DeepForecastJobEntry() {
        this( "" );
    }


    public Object clone() {
        DeepForecastJobEntry je = (DeepForecastJobEntry) super.clone();
        return je;
    }

    private static abstract class FIELDS {
        static final String filename = "filename";
        static final String output = "output";
        static final String temp = "temp";
        static final String forecastSteps = "forecastSteps";
        static final String configPath = "configPath";
        static final String toLoadFile = "toLoadFile";
        static final String modelName = "modelName";
        static final String target = "target";
        static final String runConfiguration = "runConfiguration";
    }
    @Override
    public String getXML() {
        StringBuffer retval = new StringBuffer( 200 );

        retval.append( super.getXML() );
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.filename, Const.nullToEmpty( getFilename() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.output, Const.nullToEmpty( getOutput() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.temp, Const.nullToEmpty( getTemp() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.forecastSteps, Const.nullToEmpty( getForecastSteps() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.configPath, Const.nullToEmpty( getConfigPath() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.toLoadFile, Const.nullToEmpty( getToLoadFile() ) ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.modelName, getModelName() ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.target, getTarget() ));
        retval.append( "      " ).append( XMLHandler.addTagValue( FIELDS.runConfiguration, getRunConfigurationCode(getRunConfiguration()) ));
        return retval.toString();
    }

    @Override
    public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep, IMetaStore metaStore ) throws KettleXMLException {
        try {
            super.loadXML( entrynode, databases, slaveServers );
            setFilename( XMLHandler.getTagValue(entrynode, FIELDS.filename ) );
            setOutput( XMLHandler.getTagValue(entrynode, FIELDS.output) );
            setTemp( XMLHandler.getTagValue(entrynode, FIELDS.temp) );
            setForecastSteps( XMLHandler.getTagValue(entrynode, FIELDS.forecastSteps ) );
            setConfigPath( XMLHandler.getTagValue(entrynode, FIELDS.configPath ) );
            setToLoadFile( XMLHandler.getTagValue(entrynode, FIELDS.toLoadFile) );
            setModelName( XMLHandler.getTagValue(entrynode, FIELDS.modelName ) );
            setTarget( XMLHandler.getTagValue(entrynode, FIELDS.target ) );
            setRunConfiguration( getRunConfigurationInt(XMLHandler.getTagValue(entrynode, FIELDS.runConfiguration )));
        } catch ( Exception e ) {
            throw new KettleXMLException( "Unable to load arguments from XML node" , e );
        }
    }

    @Override
    public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
                         List<SlaveServer> slaveServers ) throws KettleException {
        try {
            setFilename(rep.getJobEntryAttributeString( id_jobentry, FIELDS.filename ));
            setOutput(rep.getJobEntryAttributeString( id_jobentry, FIELDS.output ));
            setTemp(rep.getJobEntryAttributeString( id_jobentry, FIELDS.temp ));
            setForecastSteps(rep.getJobEntryAttributeString( id_jobentry, FIELDS.forecastSteps ));
            setConfigPath(rep.getJobEntryAttributeString( id_jobentry, FIELDS.configPath));
            setToLoadFile(rep.getJobEntryAttributeString( id_jobentry, FIELDS.toLoadFile ));
            setModelName(rep.getJobEntryAttributeString( id_jobentry, FIELDS.modelName ));
            setTarget(rep.getJobEntryAttributeString( id_jobentry, FIELDS.target ));
            setRunConfiguration(getRunConfigurationInt(rep.getJobEntryAttributeString( id_jobentry, FIELDS.runConfiguration )));
        } catch ( Exception dbe ) {
            throw new KettleException(
                    "Unable to load job entry of type 'DeepForecast' from the repository for id_jobentry=" + id_jobentry, dbe );
        }
    }

    @Override
    public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
        try {
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.filename, getFilename() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.output, getOutput() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.temp, getTemp() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.forecastSteps, getForecastSteps() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.configPath, getConfigPath() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.toLoadFile, getToLoadFile() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.modelName, getModelName() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.target, getTarget() );
            rep.saveJobEntryAttribute( id_job, getObjectId(), FIELDS.runConfiguration, getRunConfigurationCode(getRunConfiguration()) );
        } catch ( Exception dbe ) {
            throw new KettleException( "Unable to save job entry of type 'DeepForecast' to the repository for id_job="
                    + id_job, dbe );
        }
    }


    /**
     * This method is called when it is the job entry's turn to run during the execution of a job.
     * It should return the passed in Result object, which has been updated to reflect the outcome
     * of the job entry. The execute() method should call setResult(), setNrErrors() and modify the
     * rows or files attached to the result object if required.
     *
     * @param result The result of the previous execution
     * @return The Result of the execution.
     */
    public Result execute( Result result, int nr ) {
        result.setNrErrors( 0 );
        result.setResult(true);

        String realFilename = environmentSubstitute(getFilename());
        String realOutput =  environmentSubstitute(getOutput());
        String realForecastSteps = environmentSubstitute(getForecastSteps());
        String realConfigPath = environmentSubstitute(getConfigPath());
        String realTemp = environmentSubstitute(getTemp());
        String realToLoadFile = environmentSubstitute(getToLoadFile());
        String realModelName = environmentSubstitute(getModelName());
        String realTarget = environmentSubstitute(getTarget());
        new DeepForecastJob(realFilename, realForecastSteps, realToLoadFile, realOutput, realModelName, realConfigPath, realTarget, realTemp);

        try {
            DeepForecastJob.process();
        } catch (Exception e) {
            result.setNrErrors( 1 );
            result.setResult(false);
            e.printStackTrace();
            logError(toString(), "Error processing DeepForecastJob: " + e.getMessage());
        }

        return result;
    }

    @Override
    public boolean evaluates() {
        return true;
    }

    @Override
    public boolean isUnconditional() {
        return false;
    }

    public static final int getRunConfigurationInt( String desc ) {
        for ( int i = 0; i < runConfigurationCode.length; i++ ) {
            if ( runConfigurationCode[i].equalsIgnoreCase( desc ) ) {
                return i;
            }
        }
        return 0;
    }

    public static final String getRunConfigurationCode( int i ) {
        if ( i < 0 || i >= runConfigurationCode.length ) {
            return null;
        }
        return runConfigurationCode[i];
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename( String filename ) {
        this.filename = filename;
    }

    public String getForecastSteps() {
        return forecastSteps;
    }

    public void setForecastSteps(String forecastSteps) {
        this.forecastSteps = forecastSteps;
    }

    public String getToLoadFile() {
        return toLoadFile;
    }

    public void setToLoadFile(String toLoadFile) {
        this.toLoadFile = toLoadFile;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public int getRunConfiguration() {
        return runConfiguration;
    }

    public void setRunConfiguration(int runConfiguration) {
        this.runConfiguration = runConfiguration;
    }
}
