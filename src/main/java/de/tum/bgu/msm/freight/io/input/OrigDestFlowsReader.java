package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.*;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OrigDestFlowsReader extends CSVReader {

    private static Logger logger = Logger.getLogger(OrigDestFlowsReader.class);

    private int year;
    private int originIndex;
    private int destinationIndex;
    private int originHLIndex;
    private int destinationHLIndex;
    private int originTerminalIndex;
    private int destinationTerminalIndex;
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


    protected OrigDestFlowsReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }

    protected void processHeader(String[] header) {
        originIndex = MitoUtil.findPositionInArray("Quellzelle", header);
        destinationIndex = MitoUtil.findPositionInArray("Zielzelle", header);
        originHLIndex = MitoUtil.findPositionInArray("QuellzelleHL", header);
        destinationHLIndex = MitoUtil.findPositionInArray("ZielzelleHL", header);
        originTerminalIndex = MitoUtil.findPositionInArray("Quellterminal", header);;
        destinationTerminalIndex = MitoUtil.findPositionInArray("Zielterminal", header);;
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
        if(properties.getRand().nextDouble() < properties.getFlowsScaleFactor()) {

            int origin = Integer.parseInt(record[originIndex]);
            int destination = Integer.parseInt(record[destinationIndex]);

            FlowOriginToDestination flowOriginToDestination = new FlowOriginToDestination(origin, destination);


            if (dataSet.getFlowMatrix().contains(origin, destination)) {
                int index = dataSet.getFlowMatrix().get(origin, destination).keySet().size();
                dataSet.getFlowMatrix().get(origin, destination).put(index, flowOriginToDestination);
            } else {
                Map<Integer, FlowOriginToDestination> flowsThisOrigDestPair = new HashMap<>();
                flowsThisOrigDestPair.put(0, flowOriginToDestination);
                dataSet.getFlowMatrix().put(origin, destination, flowsThisOrigDestPair);
            }

            int originHL = Integer.parseInt(record[originHLIndex]);
            int destinationHL = Integer.parseInt(record[destinationHLIndex]);

            int originTerminal = Integer.parseInt(record[originTerminalIndex]);
            int destinationTerminal = Integer.parseInt(record[destinationTerminalIndex]);

            Mode modeHL = Mode.valueOf(Integer.parseInt(record[modeHLIndex]));
            Commodity commodityHL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityHLIndex]));
            double tonsHL = Double.parseDouble(record[tonsHLIndex]);
            FlowType flowTypeHL = FlowType.getFromCode(Integer.parseInt(record[typeHLIndex]));
            FlowSegment mainCourse = new FlowSegment(originHL, destinationHL, modeHL, commodityHL, tonsHL, SegmentType.MAIN, flowTypeHL, origin, destination);
            flowOriginToDestination.addFlow(mainCourse);

            int precarriageMode;
            if ((precarriageMode = Integer.parseInt(record[modeVLIndex])) != 0) {
                //there is a VorLauf
                Mode modeVL = Mode.valueOf(precarriageMode);
                Commodity commodityVL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityVLIndex]));
                double tonsVL = Double.parseDouble(record[tonsVLIndex]);
                FlowType flowTypeVL = FlowType.getFromCode(Integer.parseInt(record[typeVLIndex]));
                FlowSegment preCarriage = new FlowSegment(origin, originHL, modeVL, commodityVL, tonsVL, SegmentType.PRE, flowTypeVL, origin, destination);
                preCarriage.setDestinationTerminal(originTerminal);
                mainCourse.setOriginTerminal(originTerminal);
                flowOriginToDestination.addFlow(preCarriage);
            }

            int onCarriageMode;
            if ((onCarriageMode = Integer.parseInt(record[modeNLIndex])) != 0) {
                //there is a NachLauf
                Mode modeNL = Mode.valueOf(onCarriageMode);
                Commodity commodityNL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityNLIndex]));
                double tonsNL = Double.parseDouble(record[tonsNLIndex]);
                FlowType flowTypeNL = FlowType.getFromCode(Integer.parseInt(record[typeNLIndex]));
                FlowSegment onCarriage = new FlowSegment(destinationHL, destination, modeNL, commodityNL, tonsNL, SegmentType.POST, flowTypeNL, origin, destination);
                onCarriage.setOriginTerminal(destinationTerminal);
                mainCourse.setDestinationTerminal(destinationTerminal);
                flowOriginToDestination.addFlow(onCarriage);
            }

        }
    }

    public void read() {
        super.read(properties.getMatrixFileName(), ";");
        AtomicInteger flowsCount = new AtomicInteger(0);
        dataSet.getFlowMatrix().values().forEach(x -> flowsCount.addAndGet(x.size()));
        logger.info("Read " + flowsCount.get() + " origin/destination pairs with freight flows.");

    }
}
