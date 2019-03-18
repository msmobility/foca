package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

public class ParcelWightDistributionReader  extends CSVReader {

    private final static Logger logger = Logger.getLogger(ParcelWightDistributionReader.class);
    private final Properties properties;

    protected ParcelWightDistributionReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }

    int positionWeight;
    int positionFrequency;


    @Override
    protected void processHeader(String[] header) {
        positionFrequency = MitoUtil.findPositionInArray("weight", header);
        positionFrequency = MitoUtil.findPositionInArray("frequency", header);
    }

    @Override
    protected void processRecord(String[] record) {
        double weight = Double.parseDouble(record[positionWeight]);
        double frequency = Double.parseDouble(record[positionFrequency]);
        dataSet.getParcelWeightDistribution().put(weight, frequency);

    }

    @Override
    public void read() {
        super.read(properties.getParcelWeightDistributionFile(), ",");
        logger.info("Read distribution of weights for parcels");
    }

}
