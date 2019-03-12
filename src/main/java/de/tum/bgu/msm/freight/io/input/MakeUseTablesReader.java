package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MakeUseTablesReader extends CSVReader {

    private static Logger logger = Logger.getLogger(MakeUseTablesReader.class);

    private int indexIndustry;
    private Map<Commodity, Integer> indexesByCommodity = new HashMap<>();
    private Properties properties;
    private boolean make;

    protected MakeUseTablesReader(DataSet dataSet, Properties properties, boolean make) {
        super(dataSet);
        this.properties = properties;
        this.make = make;
    }

    @Override
    protected void processHeader(String[] header) {
        indexIndustry = MitoUtil.findPositionInArray("industry", header);
        for (Commodity commodity : Commodity.values()){
            int code = commodity.getCode();
            int indexThisCommodity = MitoUtil.findPositionInArray(String.valueOf(code), header);
            indexesByCommodity.put(commodity, indexThisCommodity);
        }
    }

    @Override
    protected void processRecord(String[] record) {
        String industry = record[indexIndustry];
        for (Commodity commodity : Commodity.values()){
            int indexThisCommodity = indexesByCommodity.get(commodity);
            double coefficient = Double.parseDouble(record[indexThisCommodity]);
            if (make) {
                dataSet.getMakeTable().put(industry, commodity, coefficient);
            } else {
                dataSet.getUseTable().put(industry, commodity, coefficient);
            }
        }
    }

    @Override
    public void read() {
        if (make){
            super.read(properties.getMakeTableFilename(), ",");
            logger.info("Make table read");
        } else {
            super.read(properties.getUseTableFilename(), ",");
            logger.info("Use table read");
        }

    }
}
