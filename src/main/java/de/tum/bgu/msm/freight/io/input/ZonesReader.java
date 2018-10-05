package de.tum.bgu.msm.freight.io.input;



import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import de.tum.bgu.msm.freight.data.InternalZone;

import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;
import java.util.List;

public class ZonesReader extends CSVReader {

    private final static Logger logger = Logger.getLogger(ZonesReader.class);
    private int idIndex;
    private int nameIndex;
    //private int zoneTypeIndex;

    private int thisRecordCounter = 1;

    protected ZonesReader(FreightFlowsDataSet dataSet) {
        super(dataSet);

    }

    protected void processHeader(String[] header) {
        idIndex = MitoUtil.findPositionInArray("Verkehrszelle", header);
        nameIndex = MitoUtil.findPositionInArray("Verkehrszellenname", header);
        //zoneTypeIndex = MitoUtil.findPositionInArray("type", header);

    }

    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[idIndex]);
        String name = record[nameIndex];
        //todo only internals at the moment
        if (thisRecordCounter < 413){
            InternalZone zone = new InternalZone(id, name);
            thisRecordCounter++;
            dataSet.getZones().put(id, zone);

        }


    }

    public void read() {
        super.read("./input/zones.csv", ";");
        logger.info("Read " + dataSet.getZones().size() + " zones.");
        mapFeaturesToZones(dataSet);

    }



    public static void mapFeaturesToZones(FreightFlowsDataSet dataSet) {
        int counter = 1;
        Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures("input/shp/de_lkr_4326.shp");

        for (SimpleFeature feature: features) {
            int zoneId = Integer.parseInt(feature.getAttribute("RS").toString());
            InternalZone zone = (InternalZone) dataSet.getZones().get(zoneId);
            if (zone != null){
                zone.setShapeFeature(feature);
                counter++;
            }else{
                logger.warn("zoneId " + zoneId + " doesn't exist in the zone system");
            }
        }
        logger.info("Read " + counter + " zones.");
    }
}
