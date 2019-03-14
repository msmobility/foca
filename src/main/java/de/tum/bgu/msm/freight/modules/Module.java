package de.tum.bgu.msm.freight.modules;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.properties.Properties;

public interface Module {
    void setup(DataSet dataSet, Properties properties);

    void run();
}
