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
        readOrigDestFlows();


    }

    private void readWeightDistribution() {
        ParcelWightDistributionReader parcelWightDistributionReader = new ParcelWightDistributionReader(dataSet, properties);
        parcelWightDistributionReader.read();
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
