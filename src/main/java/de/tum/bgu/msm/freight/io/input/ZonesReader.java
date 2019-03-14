package de.tum.bgu.msm.freight.io.input;



import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import de.tum.bgu.msm.freight.data.*;

import de.tum.bgu.msm.freight.data.geo.ExternalZone;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;

public class ZonesReader extends CSVReader {

    private final static Logger logger = Logger.getLogger(ZonesReader.class);
    private int idIndex;
    private int nameIndex;
    private int zoneTypeIndex;
    private int lonIndex;
    private int latIndex;

    private Properties properties;

    protected ZonesReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;

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
        } else if (type.equals("SEEPORT")){
            double lat = Double.parseDouble(record[latIndex]);
            double lon = Double.parseDouble(record[lonIndex]);
            ExternalZone zone = new ExternalZone(id, name, lat, lon);
            dataSet.getZones().put(id, zone);
        }


    }

    public void read() {
        super.read(properties.getZoneInputFile(), ",");
        logger.info("Read " + dataSet.getZones().size() + " zones.");
        mapFeaturesToZones(dataSet);
        mapFeaturesToMicroZones(dataSet, 9162, properties.getMunichMicroZonesShapeFile());
        mapFeaturesToMicroZones(dataSet, 9362, properties.getRegensburgMicroZonesShapeFile());

    }



    private void mapFeaturesToZones(DataSet dataSet) {
        int counter = 1;
        Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures(properties.getZoneShapeFile());

        for (SimpleFeature feature: features) {
            int zoneId = Integer.parseInt(feature.getAttribute(properties.getIdFieldInZonesShp()).toString());
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

    private void mapFeaturesToMicroZones(DataSet dataSet, int idZone, String zoneFileName) {

        Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures(zoneFileName);
        InternalZone macroZone = (InternalZone) dataSet.getZones().get(idZone);
        macroZone.setInStudyArea(true);
        MultiPolygon polygon = (MultiPolygon) macroZone.getShapeFeature().getDefaultGeometry();
        int n_microzones = 0;
        for (SimpleFeature feature: features) {
            if (polygon.contains((Geometry) feature.getDefaultGeometry())){
                int zoneId = Integer.parseInt(feature.getAttribute(properties.getIdFieldInMicroZonesShp()).toString());
                InternalMicroZone microZone = new InternalMicroZone(zoneId, feature);
                macroZone.addMicroZone(microZone);

                double employment = Double.parseDouble(feature.getAttribute("Employment").toString());
                microZone.setAttribute("employment", employment);

                try{
                double population = Double.parseDouble(feature.getAttribute("Population").toString());
                microZone.setAttribute("population", population);
                } catch (NullPointerException e){
                    //todo some micro zone data is missing
                    microZone.setAttribute("population", 1);
                    logger.warn("Population is not defined in micro-zone " + microZone.getId() + " of zone " + idZone);
                }



                for (String jobType : properties.getJobTypes()){
                    try{
                        double jobsThis_type = Double.parseDouble(feature.getAttribute(jobType).toString());
                        microZone.setAttribute(jobType, jobsThis_type);
                    } catch (NullPointerException e){
                        //todo some micro zone data is missing
                        microZone.setAttribute(jobType, 1);
                        logger.warn("The job type " + jobType + " is not defined in micro-zone " + microZone.getId() + " of zone " + idZone);
                    }

                }

                n_microzones ++;
            }
        }

        logger.info("Added " + n_microzones + " zones to zone " + idZone);
    }
}
