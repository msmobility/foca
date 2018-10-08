package de.tum.bgu.msm.freight.io.input;



import de.tum.bgu.msm.freight.data.ExternalZone;
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
    private int zoneTypeIndex;
    private int lonIndex;
    private int latIndex;


    protected ZonesReader(FreightFlowsDataSet dataSet) {
        super(dataSet);

    }

    protected void processHeader(String[] header) {
        idIndex = MitoUtil.findPositionInArray("id", header);
        nameIndex = MitoUtil.findPositionInArray("name", header);
        zoneTypeIndex = MitoUtil.findPositionInArray("type", header);
        lonIndex = MitoUtil.findPositionInArray("lon", header);
        latIndex = MitoUtil.findPositionInArray("lat", header);

    }

    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[idIndex]);
        String name = record[nameIndex];
        String type = record[zoneTypeIndex];
        if (type.equals("LANDKREIS")){
            InternalZone zone = new InternalZone(id, name);
            dataSet.getZones().put(id, zone);
        } else if (type.equals("EXTERNAL")){
            double lat = Double.parseDouble(record[latIndex]);
            double lon = Double.parseDouble(record[lonIndex]);
            ExternalZone zone = new ExternalZone(id, name, lat, lon);
            dataSet.getZones().put(id, zone);
        } else {
            //todo do something with see ports
        }


    }

    public void read() {
        super.read("./input/zones_edit.csv", ",");
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
