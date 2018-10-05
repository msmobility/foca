package de.tum.bgu.msm.freight.io;


import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;

abstract class AbstractReader {


    protected final FreightFlowsDataSet dataSet;

    AbstractReader(FreightFlowsDataSet dataSet) {
            this.dataSet = dataSet;
        }

    public abstract void read();


}
