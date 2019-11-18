package de.tum.bgu.msm.freight.io.input;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.freight.data.*;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.freight.longDistance.*;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class OrigDestFlowsReader extends CSVReader {

    private static Logger logger = Logger.getLogger(OrigDestFlowsReader.class);
    private final double flowScaleFactor;


    private final Map<Integer, HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>>> flowsByYear = new HashMap<>();

    private final int thisYear;
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
    private int currentYear;


    protected OrigDestFlowsReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
        this.flowScaleFactor = properties.getFlowsScaleFactor();
        thisYear = properties.getYear();
    }

    protected void processHeader(String[] header) {
        originIndex = MitoUtil.findPositionInArray("Quellzelle", header);
        destinationIndex = MitoUtil.findPositionInArray("Zielzelle", header);
        originHLIndex = MitoUtil.findPositionInArray("QuellzelleHL", header);
        destinationHLIndex = MitoUtil.findPositionInArray("ZielzelleHL", header);
        originTerminalIndex = MitoUtil.findPositionInArray("Quellterminal", header);
        destinationTerminalIndex = MitoUtil.findPositionInArray("Zielterminal", header);
        modeHLIndex = MitoUtil.findPositionInArray("ModeHL", header);
        modeVLIndex = MitoUtil.findPositionInArray("ModeVL", header);
        modeNLIndex = MitoUtil.findPositionInArray("ModeNL", header);
        commodityHLIndex = MitoUtil.findPositionInArray("GuetergruppeHL", header);
        commodityVLIndex = MitoUtil.findPositionInArray("GuetergruppeVL", header);
        commodityNLIndex = MitoUtil.findPositionInArray("GuetergruppeNL", header);
        tonsHLIndex = MitoUtil.findPositionInArray("TonnenHL", header);
        tonsVLIndex = MitoUtil.findPositionInArray("TonnenVL", header);
        tonsNLIndex = MitoUtil.findPositionInArray("TonnenNL", header);
        typeVLIndex = MitoUtil.findPositionInArray("VerkArtVL", header);
        typeHLIndex = MitoUtil.findPositionInArray("VerkArtHL", header);
        typeNLIndex = MitoUtil.findPositionInArray("VerkArtNL", header);

    }

    protected void processRecord(String[] record) {
        //if(properties.getRand().nextDouble() < properties.getFlowsScaleFactor()) {

        int origin = Integer.parseInt(record[originIndex]);
        int destination = Integer.parseInt(record[destinationIndex]);

        FlowOriginToDestination flowOriginToDestination = new FlowOriginToDestination(origin, destination);


        if (flowsByYear.get(currentYear).contains(origin, destination)) {
            int index = flowsByYear.get(currentYear).get(origin, destination).keySet().size();
            flowsByYear.get(currentYear).get(origin, destination).put(index, flowOriginToDestination);
        } else {
            Map<Integer, FlowOriginToDestination> flowsThisOrigDestPair = new LinkedHashMap<>();
            flowsThisOrigDestPair.put(0, flowOriginToDestination);
            flowsByYear.get(currentYear).put(origin, destination, flowsThisOrigDestPair);
        }

        int originHL = Integer.parseInt(record[originHLIndex]);
        int destinationHL = Integer.parseInt(record[destinationHLIndex]);

        int originTerminal = Integer.parseInt(record[originTerminalIndex]);
        int destinationTerminal = Integer.parseInt(record[destinationTerminalIndex]);

        LDMode LDModeHL = LDMode.valueOf(Integer.parseInt(record[modeHLIndex]));
        Commodity commodityHL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityHLIndex]));
        double tonsHL = Double.parseDouble(record[tonsHLIndex]) * flowScaleFactor;
        FlowType flowTypeHL = FlowType.getFromCode(Integer.parseInt(record[typeHLIndex]));
        FlowSegment mainCourse = new FlowSegment(originHL, destinationHL, LDModeHL, commodityHL, tonsHL, SegmentType.MAIN, flowTypeHL, origin, destination);
        flowOriginToDestination.addFlow(mainCourse);

        int precarriageMode;
        if ((precarriageMode = Integer.parseInt(record[modeVLIndex])) != 0) {
            //there is a VorLauf
            LDMode LDModeVL = LDMode.valueOf(precarriageMode);
            Commodity commodityVL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityVLIndex]));
            double tonsVL = Double.parseDouble(record[tonsVLIndex]) * flowScaleFactor;
            FlowType flowTypeVL = FlowType.getFromCode(Integer.parseInt(record[typeVLIndex]));
            FlowSegment preCarriage = new FlowSegment(origin, originHL, LDModeVL, commodityVL, tonsVL, SegmentType.PRE, flowTypeVL, origin, destination);
            preCarriage.setDestinationTerminal(originTerminal);
            mainCourse.setOriginTerminal(originTerminal);
            flowOriginToDestination.addFlow(preCarriage);
        }

        int onCarriageMode;
        if ((onCarriageMode = Integer.parseInt(record[modeNLIndex])) != 0) {
            //there is a NachLauf
            LDMode LDModeNL = LDMode.valueOf(onCarriageMode);
            Commodity commodityNL = Commodity.getMapOfValues().get(Integer.parseInt(record[commodityNLIndex]));
            double tonsNL = Double.parseDouble(record[tonsNLIndex]) * flowScaleFactor;
            FlowType flowTypeNL = FlowType.getFromCode(Integer.parseInt(record[typeNLIndex]));
            FlowSegment onCarriage = new FlowSegment(destinationHL, destination, LDModeNL, commodityNL, tonsNL, SegmentType.POST, flowTypeNL, origin, destination);
            onCarriage.setOriginTerminal(destinationTerminal);
            mainCourse.setDestinationTerminal(destinationTerminal);
            flowOriginToDestination.addFlow(onCarriage);
        }

//        }
    }

    public void read() {

        for (int year : properties.flows().getCommodityFlowsYears()) {
            currentYear = year;
            String fileName = properties.getMatrixFolder() + properties.getMatrixFileNamePrefix() +
                    year + properties.getMatrixFileNameSuffix();
            flowsByYear.put(year, HashBasedTable.create());
            super.read(fileName, ";");
            AtomicInteger flowsCount = new AtomicInteger(0);
            flowsByYear.get(year).values().forEach(x -> flowsCount.addAndGet(x.size()));
            logger.info("Read " + flowsCount.get() + " origin/destination pairs with freight flows in year " + year + ".");
        }

        int previousYear = 0;
        int nextYear = 9999;

        for (int year : properties.flows().getCommodityFlowsYears()) {
            if (thisYear <= year) {
                nextYear = year;
                break;
            }
        }

        for (int year : properties.flows().getCommodityFlowsYears()) {
            if (thisYear >= year) {
                previousYear = year;
            }
        }

        if (previousYear == 0 || nextYear == 9999) {
            throw new RuntimeException("Cannot extrapolate flows before the first or after the last year.");
        } else if (previousYear == nextYear) {
            dataSet.getFlowMatrix().putAll(flowsByYear.get(thisYear));
        } else {
            interpolateFlows(thisYear, previousYear, nextYear);
        }
    }


    private void interpolateFlows(int thisYear, int previousYear, int nextYear) {
        logger.info("Will interpolate flows between " + previousYear + " and " + nextYear);
        HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>> previousYearData = flowsByYear.get(previousYear);
        HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>> nextYearData = flowsByYear.get(nextYear);
        HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>> thisYearData = HashBasedTable.create();

        for (int origin : previousYearData.rowKeySet()) {
            for (int destination : previousYearData.columnKeySet()) {
                Map<Integer, FlowOriginToDestination> previousYearFlows = previousYearData.get(origin, destination);
                if (previousYearFlows != null) {
                    Map<Integer, FlowOriginToDestination> thisYearFlows = new HashMap<>();
                    thisYearData.put(origin, destination, thisYearFlows);
                    Map<Integer, FlowOriginToDestination> nextYearFlows = nextYearData.get(origin, destination);
                    for (int key : previousYearFlows.keySet()) {
                        FlowOriginToDestination previousYearFlow = previousYearFlows.get(key);
                        FlowOriginToDestination nextYearFlow = nextYearFlows.get(key);
                        FlowOriginToDestination thisYearFlow = new FlowOriginToDestination(origin, destination);
                        thisYearFlows.put(key, thisYearFlow);
                        for (SegmentType segment : SegmentType.values()) {
                            FlowSegment previousYearFlowSegment = previousYearFlow.getFlowSegments().get(segment);
                            if (previousYearFlowSegment != null) {
                                double previousYearVolume = previousYearFlowSegment.getVolume_tn();
                                double nextYearVolume = nextYearFlow.getFlowSegments().get(segment).getVolume_tn();
                                double slope = (nextYearVolume - previousYearVolume) /
                                        (nextYear - previousYear);

                                double thisYearVolume = previousYearVolume + slope * (thisYear - previousYear);
                                FlowSegment thisYearFlowSegment = new FlowSegment(previousYearFlowSegment.getSegmentOrigin(),
                                        previousYearFlowSegment.getSegmentDestination(),
                                        previousYearFlowSegment.getLDMode(),
                                        previousYearFlowSegment.getCommodity(),
                                        thisYearVolume,
                                        previousYearFlowSegment.getSegmentType(),
                                        previousYearFlowSegment.getFlowType(),
                                        previousYearFlowSegment.getFlowOrigin(),
                                        previousYearFlowSegment.getFlowDestination());

                                thisYearFlowSegment.setDestinationTerminal(previousYearFlowSegment.getDestinationTerminal());
                                thisYearFlowSegment.setOriginTerminal(previousYearFlowSegment.getOriginTerminal());

                                thisYearFlow.addFlow(thisYearFlowSegment);
                            }
                        }
                    }
                }
            }
        }
        dataSet.getFlowMatrix().putAll(thisYearData);
        logger.info("Interpolated matrix for year 2020");
    }
}
