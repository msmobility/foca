package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;


public class DistributionCenterCatchmentAreaReader extends CSVReader {


    private static Logger logger = Logger.getLogger(DistributionCenterCatchmentAreaReader.class);
    private Properties properties;

    private int microZoneIdex;
    private int zoneIndex;
    private int commodityGroupIndex;
    private int dcIndex;


    protected DistributionCenterCatchmentAreaReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }

    @Override
    protected void processHeader(String[] header) {
        microZoneIdex = MitoUtil.findPositionInArray("microZone", header);
        zoneIndex = MitoUtil.findPositionInArray("zone", header);
        commodityGroupIndex = MitoUtil.findPositionInArray("commodityGroup", header);
        dcIndex = MitoUtil.findPositionInArray("distributionCenter", header);

    }

    @Override
    protected void processRecord(String[] record) {

        int microZoneId = Integer.parseInt(record[microZoneIdex]);
        int zoneId = Integer.parseInt(record[zoneIndex]);
        int dcId = Integer.parseInt(record[dcIndex]);

        CommodityGroup commodityGroup = CommodityGroup.valueOf(record[commodityGroupIndex].toUpperCase());
        DistributionCenter distributionCenter = dataSet.getDistributionCenters().get(zoneId).get(commodityGroup).get(dcId);

        InternalZone internalZone = (InternalZone) dataSet.getZones().get(zoneId);
        InternalMicroZone internalMicroZone = internalZone.getMicroZones().get(microZoneId);
        if (internalMicroZone != null){
            distributionCenter.getZonesServedByThis().add(internalMicroZone);
        }
    }

    @Override
    public void read() {
        super.read(properties.getDistributionCentersCatchmentAreaFile(), ",");

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
