package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.properties.Properties;

public class InputManager {

    private DataSet dataSet;
    private Properties properties;

    public InputManager(Properties properties){
        this.properties = properties;
    }

    public void readInput(){
        this.dataSet = new DataSet();
        readZones();
        readCommodityAttributes();
        readDistributionCenters();

        readTerminals();
        readMakeUseTable();
        readWeightDistribution();

        if (properties.longDistance().isDisaggregateLongDistanceFlows()){
            readOrigDestFlows();
        } else {
            readLongDistanceTrucks();
        }
        //todo this will add a silo data container for a certain study area (so far only covers the silo study area)
        //new SPReader(dataSet).readSyntheticPopulation("C:/models/silo/muc/siloMuc.properties");

    }

    private void readLongDistanceTrucks() {
        new LongDistanceTruckReader(dataSet, properties).read();
    }

    private void readDistributionCenterCatchmentAreas() {
        new DistributionCenterCatchmentAreaReader(dataSet, properties).read();
    }

    private void readWeightDistribution() {
        ParcelWeightDistributionReader parcelWeightDistributionReader = new ParcelWeightDistributionReader(dataSet, properties);
        parcelWeightDistributionReader.read();
    }

    private void readTerminals() {
        TerminalReader terminalReader = new TerminalReader(dataSet, properties);
        terminalReader.read();
    }

    private void readMakeUseTable() {
        MakeUseTablesReader makeUseTablesReader = new MakeUseTablesReader(dataSet, properties, true);
        makeUseTablesReader.read();
        makeUseTablesReader = new MakeUseTablesReader(dataSet, properties, false);
        makeUseTablesReader.read();
    }

    private void readDistributionCenters() {

//        DistributionCenterReaderXML distributionCenterReaderXML = new DistributionCenterReaderXML(dataSet);
//        distributionCenterReaderXML.setValidating(false);
//        distributionCenterReaderXML.read(properties.getDistributionCentersFile());
        DistributionCenterReader distributionCenterReader = new DistributionCenterReader(dataSet, properties);
        distributionCenterReader.read();
    }


    private void readZones(){
        ZonesReader zonesReader = new ZonesReader(dataSet, properties);
        zonesReader.read();

    }

    private void readCommodityAttributes(){
        CommodityAttributesReader commodityAttributesReader = new CommodityAttributesReader(dataSet, properties);
        commodityAttributesReader.read();
    }

    private void readOrigDestFlows(){
        OrigDestFlowsReader origDestFlowsReader = new OrigDestFlowsReader(dataSet, properties);
        origDestFlowsReader.read();
    }


    public DataSet getDataSet() {
        return dataSet;
    }
}
