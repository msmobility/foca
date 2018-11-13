package de.tum.bgu.msm.freight.modules.assignment.counts;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;

import java.util.ArrayList;
import java.util.List;

public class LinksFileReader extends CSVReader {

    private String fileName;
    List<String> links = new ArrayList<>();
    private int idIndex;

    public LinksFileReader(FreightFlowsDataSet dataSet, String fileName) {
        super(dataSet);
        this.fileName = fileName;
    }

    @Override
    protected void processHeader(String[] header) {

        idIndex = MitoUtil.findPositionInArray("ID", header);

    }

    @Override
    protected void processRecord(String[] record) {
        String linkId = record[idIndex];
        links.add(linkId);
    }

    @Override
    public void read() {
        super.read(fileName, ",");
    }

    public List<String> getListOfIds(){
        return links;
    }
}
