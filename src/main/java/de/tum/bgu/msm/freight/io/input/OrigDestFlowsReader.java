package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.*;
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
    private int originHLIndex;
    private int destinationHLIndex;
    private int modeHLIndex;
    private int modeVLIndex;
    private int modeNLIndex;
    private int commodityHLIndex;
    private int tonsHLIndex;
    private int commodityVLIndex;
    private int commodityNLIndex;
    private int tonsNLIndex;
    private int tonsVLIndex;
    private int typeVLIndex;
    private int typeHLIndex;
    private int typeNLIndex;

    private Properties properties;


    protected OrigDestFlowsReader(FreightFlowsDataSet dataSet, int year, Properties properties) {
        super(dataSet);
        this.year = year;
        this.properties = properties;
    }

    protected void processHeader(String[] header) {
        originIndex = MitoUtil.findPositionInArray("Quellzelle", header);
        destinationIndex = MitoUtil.findPositionInArray("Zielzelle", header);
        originHLIndex = MitoUtil.findPositionInArray("QuellzelleHL", header);
        destinationHLIndex = MitoUtil.findPositionInArray("ZielzelleHL", header);
        modeHLIndex = MitoUtil.findPositionInArray("ModeHL", header);
        modeVLIndex = MitoUtil.findPositionInArray("ModeVL", header);
        modeNLIndex = MitoUtil.findPositionInArray("ModeNL", header);
        commodityHLIndex = MitoUtil.findPositionInArray("GuetergruppeHL", header);
        commodityVLIndex = MitoUtil.findPositionInArray("GuetergruppeVL", header);
        commodityNLIndex = MitoUtil.findPositionInArray("GuetergruppeNL", header);
        tonsHLIndex = MitoUtil.findPositionInArray("TonnenHL", header);
        tonsVLIndex = MitoUtil.findPositionInArray("TonnenVL", header);
        tonsNLIndex = MitoUtil.findPositionInArray("TonnenNL", header);
        typeVLIndex = MitoUtil.findPositionInArray("VerkArtVL", header);;
        typeHLIndex = MitoUtil.findPositionInArray("VerkArtHL", header);;
        typeNLIndex = MitoUtil.findPositionInArray("VerkArtNL", header);;

    }

    protected void processRecord(String[] record) {
        int origin = Integer.parseInt(record[originIndex]);
        int destination = Integer.parseInt(record[destinationIndex]);

        OrigDestFlow origDestFlow = new OrigDestFlow(year, origin, destination);

        if (dataSet.getFlowMatrix().contains(origin, destination)){
            dataSet.getFlowMatrix().get(origin, destination).add(origDestFlow);
        } else {
            ArrayList<OrigDestFlow> flowsThisOrigDestPair = new ArrayList<OrigDestFlow>();
            flowsThisOrigDestPair.add(origDestFlow);
            dataSet.getFlowMatrix().put(origin, destination, flowsThisOrigDestPair);
        }

        int originHL = Integer.parseInt(record[originHLIndex]);
        int destinationHL = Integer.parseInt(record[destinationHLIndex]);

        if (origin != originHL){
            //there is a VL
            Mode modeVL = Mode.valueOf(Integer.parseInt(record[modeVLIndex]));
            Commodity commodityVL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityVLIndex]));
            double tonsVL = Double.parseDouble(record[tonsVLIndex]);
            FlowType flowTypeVL = FlowType.getFromCode(Integer.parseInt(record[typeVLIndex]));
            Trip tripVL = new Trip(origin, originHL, modeVL, commodityVL, tonsVL, Segment.PRE, flowTypeVL);
            origDestFlow.addTrip(tripVL);
        }

        Mode modeHL = Mode.valueOf(Integer.parseInt(record[modeHLIndex]));
        Commodity commodityHL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityHLIndex]));
        double tonsHL = Double.parseDouble(record[tonsHLIndex]);
        FlowType flowTypeHL = FlowType.getFromCode(Integer.parseInt(record[typeHLIndex]));
        Trip tripHL = new Trip(originHL, destinationHL, modeHL, commodityHL, tonsHL, Segment.MAIN, flowTypeHL);
        origDestFlow.addTrip(tripHL);

        if (destination != destinationHL){
            //there is a NL
            Mode modeNL = Mode.valueOf(Integer.parseInt(record[modeNLIndex]));
            Commodity commodityNL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityNLIndex]));
            double tonsNL = Double.parseDouble(record[tonsNLIndex]);
            FlowType flowTypeNL = FlowType.getFromCode(Integer.parseInt(record[typeNLIndex]));
            Trip tripNL = new Trip(destinationHL, destination, modeNL, commodityNL, tonsNL, Segment.POST, flowTypeNL);
            origDestFlow.addTrip(tripNL);
        }
    }

    public void read() {
        super.read(properties.getMatrixFileName(), ";");
        logger.info("Read " + dataSet.getFlowMatrix().size() + " origin/destination pairs with freight flows.");
    }
}
