package de.tum.bgu.msm.freight.io;

import de.tum.bgu.msm.freight.data.DataSet;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public abstract class CSVReader extends AbstractReader {

    private static final Logger logger = Logger.getLogger(de.tum.bgu.msm.io.input.CSVReader.class);

    private BufferedReader reader;

    private int numberOfRecords = 0;

    protected CSVReader(DataSet dataSet) {
        super(dataSet);
    }

    protected abstract void processHeader(String[] header);

    protected abstract void processRecord(String[] record);

    public void read(String fileName, String delimiter) {
        initializeReader(fileName, delimiter);
        try {
            String record;
            while ((record = reader.readLine()) != null) {
                numberOfRecords++;
                processRecord(record.split(delimiter));
            }
        } catch (IOException e) {
            logger.error("Error parsing record number " + numberOfRecords + ": " + e.getMessage(), e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info(this.getClass().getSimpleName() + ": Read " + numberOfRecords + " records.");
    }

    private void initializeReader(String fileName, String delimiter) {
        try {
            reader = new BufferedReader(new FileReader(fileName));
            processHeader(reader.readLine().split(delimiter));
        } catch (IOException e) {
            logger.error("Error initializing csv reader: " + e.getMessage(), e);
        }
    }
}
