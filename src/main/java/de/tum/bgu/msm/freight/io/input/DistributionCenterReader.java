package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import scala.Int;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DistributionCenterReader extends CSVReader {

    private static Logger logger = Logger.getLogger(DistributionCenter.class);

    private int posObject;
    private int posDcId;
    private int posDcName;
    private int posDcX;
    private int posDcY;
    private int posCommodity;
    private int posZoneId;
    private int posMdId;
    private int posMdName;
    private int posMdX;
    private int posMdY;
    private int posMicroZoneId;

    private DistributionCenter currentDistributionCenter;
    private MicroDepot currentMicroDepot;

    private int counter = 0;

    private Properties properties;

    protected DistributionCenterReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }

    @Override
    protected void processHeader(String[] header) {
        posObject = MitoUtil.findPositionInArray("object", header);
        posZoneId = MitoUtil.findPositionInArray("zone", header);
        posDcId = MitoUtil.findPositionInArray("dcId", header);
        posDcName = MitoUtil.findPositionInArray("dcName", header);
        posDcX = MitoUtil.findPositionInArray("dcX", header);
        posDcY = MitoUtil.findPositionInArray("dcY", header);
        posCommodity = MitoUtil.findPositionInArray("commodityGroup", header);
        posMdId = MitoUtil.findPositionInArray("microDepotId", header);
        posMdName = MitoUtil.findPositionInArray("microDepotName", header);
        posMdX = MitoUtil.findPositionInArray("microDepotX", header);
        posMdY = MitoUtil.findPositionInArray("microDepotY", header);
        posMicroZoneId= MitoUtil.findPositionInArray("microZoneId", header);

    }

    @Override
    protected void processRecord(String[] record) {
        String object = record[posObject];
        int zoneId  = Integer.parseInt(record[posZoneId]);
        int dcId = Integer.parseInt(record[posDcId]);
        String dcName = record[posDcName];
        double dcX = Double.parseDouble(record[posDcX]);
        double dcY = Double.parseDouble(record[posDcY]);
        CommodityGroup commodityGroup = CommodityGroup.valueOf(record[posCommodity].toUpperCase());
        int mdId = Integer.parseInt(record[posMdId]);
        String mdName = record[posMdName];
        double mdX = Double.parseDouble(record[posMdX]);
        double mdY = Double.parseDouble(record[posMdY]);
        int microZoneId = Integer.parseInt(record[posMicroZoneId]);

        if (object.equalsIgnoreCase(ObjectTypes.distributionCenter.toString())){
            currentDistributionCenter = new DistributionCenter(dcId, dcName, new Coordinate(dcX,dcY), commodityGroup, zoneId);
            addDistributionCenter(currentDistributionCenter, zoneId, commodityGroup);
        }



        if (object.equalsIgnoreCase(ObjectTypes.catchmentArea.toString())){
            InternalZone internalZone = (InternalZone) dataSet.getZones().get(currentDistributionCenter.getZoneId());
            InternalMicroZone internalMicroZone = internalZone.getMicroZones().get(microZoneId);
            currentDistributionCenter.getZonesServedByThis().add(internalMicroZone);
        }

        if (properties.shortDistance().isReadMicroDepotsFromFile()) {

            if (object.equalsIgnoreCase(ObjectTypes.microDepot.toString())) {
                currentMicroDepot = new MicroDepot(mdId, mdName, new Coordinate(mdX, mdY), currentDistributionCenter.getCommodityGroup(),
                        currentDistributionCenter, currentDistributionCenter.getZoneId(), microZoneId);
                currentDistributionCenter.getMicroDeportsServedByThis().add(currentMicroDepot);
            }

            if (object.equalsIgnoreCase(ObjectTypes.microDepotCatchmentArea.toString())) {
                InternalZone internalZone = (InternalZone) dataSet.getZones().get(currentDistributionCenter.getZoneId());
                InternalMicroZone internalMicroZone = internalZone.getMicroZones().get(microZoneId);
                currentMicroDepot.getZonesServedByThis().add(internalMicroZone);
            }
        } else {
            logger.warn("No micro depot information read from distribution center input file. Need to generate microdepots with other method if using cargo-bikes");
        }
    }

    private void addDistributionCenter(DistributionCenter dc, int zoneId, CommodityGroup commodityGroup) {

        Map<Integer, Map<CommodityGroup, Map<Integer, DistributionCenter>>> distributionCenters = dataSet.getDistributionCenters();
        Map<CommodityGroup, Map<Integer, DistributionCenter>> distributionCentersAtThisZone;
        Map<Integer, DistributionCenter> distributionCentersAtThisZoneAndCommodityGroup;
        if (distributionCenters.containsKey(zoneId)){
            //there is some dc for this zone
            distributionCentersAtThisZone = distributionCenters.get(zoneId);
            if (distributionCentersAtThisZone.containsKey(commodityGroup)){
//                there is some dc for this zone and commodity
               distributionCentersAtThisZoneAndCommodityGroup = distributionCentersAtThisZone.get(commodityGroup);
            } else {
                //thre is not a dc for this zone and commodity, but other dc for other commodities
                distributionCentersAtThisZoneAndCommodityGroup = new HashMap<>();
                distributionCentersAtThisZone.put(commodityGroup, distributionCentersAtThisZoneAndCommodityGroup);
            }
        } else {
            //there was nothing for this zone;
            distributionCentersAtThisZone = new HashMap<>();
            distributionCentersAtThisZoneAndCommodityGroup = new HashMap<>();

        }
        distributionCentersAtThisZoneAndCommodityGroup.put(dc.getId(), dc);
        distributionCentersAtThisZone.put(commodityGroup, distributionCentersAtThisZoneAndCommodityGroup);
        distributionCenters.put(zoneId, distributionCentersAtThisZone);

        counter++;

    }

    @Override
    public void read() {
        super.read(properties.getDistributionCentersFile(), ",");
        fillCatchmentAreas();
        logger.info("Read " + counter + " distribution centers.");

    }

    enum ObjectTypes{
        distributionCenter, catchmentArea, microDepot, microDepotCatchmentArea;
    }

    private void fillCatchmentAreas(){
        //fill distribution centers with catchment areas equal to all the areas, if no information is given
        for (int zone : dataSet.getDistributionCenters().keySet()){
            for(CommodityGroup commodityGroup : dataSet.getDistributionCenters().get(zone).keySet()){
                for (DistributionCenter distributionCenter : dataSet.getDistributionCenters().get(zone).get(commodityGroup).values()){
                    if (distributionCenter.getZonesServedByThis().isEmpty()){
                        InternalZone internalZone = (InternalZone) dataSet.getZones().get(zone);
                        distributionCenter.getZonesServedByThis().addAll(internalZone.getMicroZones().values());
                        logger.warn("Distribution center " + distributionCenter.getName() + " has not a defined catchment area");
                    }
                }
            }
        }
    }
}
