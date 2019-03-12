package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DistributionCenterReader extends CSVReader {

    private static Logger logger = Logger.getLogger(DistributionCenter.class);

    private int posId;
    private int posName;
    private int posX;
    private int posY;
    private int posCommodity;
    private int posZone;

    private int counter = 0;

    private Properties properties;

    protected DistributionCenterReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }

    @Override
    protected void processHeader(String[] header) {
        posId = MitoUtil.findPositionInArray("id", header);
        posName = MitoUtil.findPositionInArray("name", header);
        posX = MitoUtil.findPositionInArray("x", header);
        posY = MitoUtil.findPositionInArray("y", header);
        posCommodity = MitoUtil.findPositionInArray("commodityGroup", header);
        posZone = MitoUtil.findPositionInArray("zone", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[posId]);
        String name = record[posName];
        double x = Double.parseDouble(record[posX]);
        double y = Double.parseDouble(record[posY]);
        CommodityGroup commodityGroup = CommodityGroup.valueOf(record[posCommodity].toUpperCase());
        int zoneId  = Integer.parseInt(record[posZone]);

        DistributionCenter dc = new DistributionCenter(id, name, new Coord(x,y), commodityGroup);

        addDistributionCenter(dc, zoneId, commodityGroup);


    }

    private void addDistributionCenter(DistributionCenter dc, int zoneId, CommodityGroup commodityGroup) {

        Map<Integer, Map<CommodityGroup, ArrayList<DistributionCenter>>> distributionCenters = dataSet.getDistributionCenters();
        Map<CommodityGroup, ArrayList<DistributionCenter>> distributionCentersAtThisZone;
        ArrayList<DistributionCenter> distributionCentersAtThisZoneAndCommodityGroup;
        if (distributionCenters.containsKey(zoneId)){
            //there is some dc for this zone
            distributionCentersAtThisZone = distributionCenters.get(zoneId);
            if (distributionCentersAtThisZone.containsKey(commodityGroup)){
//                there is some dc for this zone and commodity
               distributionCentersAtThisZoneAndCommodityGroup = distributionCentersAtThisZone.get(commodityGroup);
            } else {
                //thre is not a dc for this zone and commodity, but other dc for other commodities
                distributionCentersAtThisZoneAndCommodityGroup = new ArrayList<>();
                distributionCentersAtThisZone.put(commodityGroup, distributionCentersAtThisZoneAndCommodityGroup);
            }
        } else {
            //there was nothing for this zone;
            distributionCentersAtThisZone = new HashMap<>();
            distributionCentersAtThisZoneAndCommodityGroup = new ArrayList<>();

        }
        distributionCentersAtThisZoneAndCommodityGroup.add(dc);
        distributionCentersAtThisZone.put(commodityGroup, distributionCentersAtThisZoneAndCommodityGroup);
        distributionCenters.put(zoneId, distributionCentersAtThisZone);

        counter++;

    }

    @Override
    public void read() {
        super.read(properties.getDistributionCentersFile(), ",");
        logger.info("Read " + counter + " distribution centers.");

    }
}
