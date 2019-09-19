package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.DataBuilder;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.properties.Properties;
import de.tum.bgu.msm.schools.DataContainerWithSchools;
import de.tum.bgu.msm.utils.SiloUtil;

public class SPReader {

    private final DataSet dataSet;

    /**
     * Adds a SILO data container to the dataset
     * @param dataSet
     */
    public SPReader(DataSet dataSet) {
        this.dataSet = dataSet;
    }


    public void readSyntheticPopulation(String propertiesFile){
        Properties properties = SiloUtil.siloInitialization(propertiesFile);
        DataContainerWithSchools dataContainer = DataBuilder.getModelDataForMuc(properties, null);
        DataBuilder.read(properties, dataContainer);
        dataSet.setSiloDataContainer(dataContainer);
    }

}
