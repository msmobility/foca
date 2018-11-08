package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;

public class InputManager {

    private FreightFlowsDataSet dataSet;

    public void readInput(){
        this.dataSet = new FreightFlowsDataSet();
        readZones(dataSet);
        readCommodityAttributes(dataSet);
        readOrigDestFlows(dataSet, 2010);



    }

    private void readZones(FreightFlowsDataSet dataSet){
        ZonesReader zonesReader = new ZonesReader(dataSet);
        zonesReader.read();

    }

    private void readCommodityAttributes(FreightFlowsDataSet dataSet){
        CommodityAttributesReader commodityAttributesReader = new CommodityAttributesReader(dataSet);
        commodityAttributesReader.read();
    }

    private void readOrigDestFlows(FreightFlowsDataSet dataSet, int year){
        OrigDestFlowsReader origDestFlowsReader = new OrigDestFlowsReader(dataSet, year);
        origDestFlowsReader.read();
    }


    public FreightFlowsDataSet getDataSet() {
        return dataSet;
    }
}
