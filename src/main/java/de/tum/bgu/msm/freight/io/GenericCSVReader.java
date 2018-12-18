package de.tum.bgu.msm.freight.io;

import com.sun.corba.se.spi.ior.ObjectKey;
import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import de.tum.bgu.msm.util.MitoUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class GenericCSVReader extends CSVReader {

    private final String delimiter;
    private List<String> fields;
    private String fileName;
    private int counter = 0;

    private Map<String, Integer> indexes;
    private Map<Integer, Map<String, String>> values;

    public GenericCSVReader(FreightFlowsDataSet dataSet, List<String> fields, String fileName, String delimiter) {
        super(dataSet);
        this.fields = fields;
        this.fileName = fileName;
        this.delimiter = delimiter;

        indexes = new HashMap<>();
        values = new HashMap<>();

    }

    @Override
    protected void processHeader(String[] header) {
        for (String field : fields){
            indexes.put(field, MitoUtil.findPositionInArray(field, header));
        }
    }

    @Override
    protected void processRecord(String[] record) {

        Map<String, String> thisObjectAttribute = new HashMap<>();
        for (String field : fields){
            thisObjectAttribute.put(field, record[indexes.get(field)]);
        }
        values.put(counter, thisObjectAttribute);
        counter ++;
    }

    @Override
    public void read() {
        super.read(fileName, delimiter);
    }

    public Map<Integer, Map<String, String>> readAndReturnResults(){
        read();
        return values;

    }

}
