package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.Commodity;
import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import de.tum.bgu.msm.freight.data.Mode;
import de.tum.bgu.msm.freight.data.OrigDestFlow;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;

import org.apache.log4j.Logger;

import java.util.ArrayList;

public class OrigDestFlowsReader extends CSVReader {

    private static Logger logger = Logger.getLogger(OrigDestFlowsReader.class);

    private int year;
    private int originIndex;
    private int destinationIndex;
    private int modeIndex;
    private int commodityIndex;
    private int tonsIndex;

    protected OrigDestFlowsReader(FreightFlowsDataSet dataSet, int year) {
        super(dataSet);
        this.year = year;
    }

    protected void processHeader(String[] header) {
        originIndex = MitoUtil.findPositionInArray("Quellzelle", header);
        destinationIndex = MitoUtil.findPositionInArray("Zielzelle", header);
        modeIndex = MitoUtil.findPositionInArray("ModeHL", header);
        commodityIndex = MitoUtil.findPositionInArray("GuetergruppeHL", header);
        tonsIndex = MitoUtil.findPositionInArray("TonnenHL", header);

    }

    protected void processRecord(String[] record) {
        int origin = Integer.parseInt(record[originIndex]);
        int destination = Integer.parseInt(record[destinationIndex]);
        Mode mode = Mode.valueOf(Integer.parseInt(record[modeIndex]));
        Commodity commodity = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityIndex]));
        double tons = Double.parseDouble(record[tonsIndex]);
        OrigDestFlow origDestFlow = new OrigDestFlow(year, origin, destination, mode, commodity, tons);
        if (dataSet.getFlowMatrix().contains(origin, destination)){
            dataSet.getFlowMatrix().get(origin, destination).add(origDestFlow);
        } else {
            ArrayList<OrigDestFlow> flowsThisOrigDestPair = new ArrayList<OrigDestFlow>();
            flowsThisOrigDestPair.add(origDestFlow);
            dataSet.getFlowMatrix().put(origin, destination, flowsThisOrigDestPair);
        }



    }

    public void read() {
        super.read(Properties.matrixFileName, ";");
        logger.info("Read " + dataSet.getFlowMatrix().size() + " origin/destination pairs with freight flows.");
    }
}
