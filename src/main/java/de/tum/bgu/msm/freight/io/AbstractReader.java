package de.tum.bgu.msm.freight.io;


import de.tum.bgu.msm.freight.data.DataSet;

abstract class AbstractReader {


    protected final DataSet dataSet;

    AbstractReader(DataSet dataSet) {
            this.dataSet = dataSet;
        }

    public abstract void read();


}
