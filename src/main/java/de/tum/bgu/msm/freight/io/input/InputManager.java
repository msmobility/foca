package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import de.tum.bgu.msm.freight.properties.Properties;

public class InputManager {

    private FreightFlowsDataSet dataSet;
    private Properties properties;

    public InputManager(Properties properties){
        this.properties = properties;
    }

    public void readInput(){
        this.dataSet = new FreightFlowsDataSet();
        readZones();
        readCommodityAttributes();
        readOrigDestFlows(2010);
    }

    private void readZones(){
        ZonesReader zonesReader = new ZonesReader(dataSet, properties);
        zonesReader.read();

    }

    private void readCommodityAttributes(){
        CommodityAttributesReader commodityAttributesReader = new CommodityAttributesReader(dataSet, properties);
        commodityAttributesReader.read();
    }

    private void readOrigDestFlows(int year){
        OrigDestFlowsReader origDestFlowsReader = new OrigDestFlowsReader(dataSet, year, properties);
        origDestFlowsReader.read();
    }


    public FreightFlowsDataSet getDataSet() {
        return dataSet;
    }
}
