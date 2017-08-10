# pdi-plugin-dlforecast
Time series forecasting plugin for Pentaho Data Integration. Implemented using a simple, configurable DL4J LSTM network.

## Instalation
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

- TODO
