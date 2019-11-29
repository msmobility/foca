package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;
import de.tum.bgu.msm.freight.io.AbstractReader;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.*;

@Deprecated
public class DistributionCenterReaderXML extends MatsimXmlParser {

    private final static Logger logger = Logger.getLogger(DistributionCenterReaderXML.class);

    private final static String DISTRIBUTION_CENTER = "distributionCenter";
    private final static String CATCHMENT_AREA = "catchmentArea";
    private final static String MICRO_ZONE = "microZone";
    private final static String MICRO_DEPOT = "microDepot";
    private final static String MICRO_DEPOT_CATCHMENT_AREA = "microDepotCatchmentArea";

    private int counterDistributionCenters;
    private final DataSet dataSet;

    private DistributionCenter currentDistributionCenter;
    private MicroDepot currentMicroDepot;
    private List<InternalMicroZone> currentCatchmentArea;



    public DistributionCenterReaderXML(DataSet dataSet){
        this.dataSet = dataSet;
    }

    public void read(String fileName) {
        readFile(fileName);
        fillCatchmentAreas();

    }

    @Override
    public void startTag(String name, Attributes atts, Stack<String> context) {
        if(name.equals(DISTRIBUTION_CENTER)){
            startDistributionCenter(atts);
        } else if (name.equals(MICRO_ZONE)){
            startMicroZone(atts);
        } else if (name.equals(MICRO_DEPOT)){
            startMicroDepot(atts);
        } else if (name.equals(CATCHMENT_AREA)){
            currentCatchmentArea = new ArrayList<>();
        } else if (name.equals(MICRO_DEPOT_CATCHMENT_AREA)){
            currentCatchmentArea = new ArrayList<>();
        }

    }

    private void startMicroDepot(Attributes atts) {
        int id = Integer.parseInt(atts.getValue("id"));
        String name = atts.getValue("name");
        double x = Double.parseDouble(atts.getValue("x"));
        double y = Double.parseDouble(atts.getValue("y"));
        int microZoneId = Integer.parseInt(atts.getValue("microZone"));
        Coordinate coord_gk4 = new Coordinate(x,y);
        this.currentMicroDepot = new MicroDepot(id, name, coord_gk4, currentDistributionCenter.getCommodityGroup(),
                currentDistributionCenter, currentDistributionCenter.getZoneId(), microZoneId);
    }

    private void startMicroZone(Attributes atts) {
        InternalZone internalZone = (InternalZone) dataSet.getZones().get(currentDistributionCenter.getZoneId());
        int microZoneid = Integer.parseInt(atts.getValue("id"));
        if (internalZone.getMicroZones().containsKey(microZoneid)){
            InternalMicroZone internalMicroZone = internalZone.getMicroZones().get(microZoneid);
            currentCatchmentArea.add(internalMicroZone);
        }
    }

    private void startDistributionCenter(Attributes atts) {
        int id = Integer.parseInt(atts.getValue("id"));
        String name = atts.getValue("name");
        double x = Double.parseDouble(atts.getValue("x"));
        double y = Double.parseDouble(atts.getValue("y"));
        CommodityGroup commodityGroup = CommodityGroup.valueOf(atts.getValue("commodityGroup").toUpperCase());
        Coordinate coord_gk4 = new Coordinate(x,y);
        int zoneId = Integer.parseInt(atts.getValue("zone"));
        this.currentDistributionCenter = new DistributionCenter(id, name, coord_gk4, commodityGroup, zoneId);
        addDistributionCenter(currentDistributionCenter, zoneId, commodityGroup);

    }


    @Override
    public void endTag(String name, String content, Stack<String> context) {
        if(name.equals(DISTRIBUTION_CENTER)){
            currentDistributionCenter = null;
        } else if (name.equals(CATCHMENT_AREA)){
            currentDistributionCenter.getZonesServedByThis().addAll(currentCatchmentArea);
            currentCatchmentArea = null;
        } else if (name.equals(MICRO_DEPOT)){
            currentDistributionCenter.getMicroDeportsServedByThis().add(currentMicroDepot);
            currentMicroDepot = null;
        } else if (name.equals(MICRO_DEPOT_CATCHMENT_AREA)){
            currentMicroDepot.getZonesServedByThis().addAll(currentCatchmentArea);
            currentCatchmentArea = null;
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
