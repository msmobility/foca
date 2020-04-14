package de.tum.bgu.msm.freight.modules.syntheticMicroDepotGeneration;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.geo.*;
import de.tum.bgu.msm.freight.io.output.DistributionCenterCsvWriter;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;

import java.io.FileNotFoundException;


/**
 * creates micro depot at synthetic locations for each distribution center of selectedDistributionCenters
 */
public class SyntheticMicroDepots implements Module {

    private static Logger logger = Logger.getLogger(SyntheticMicroDepots.class);
    private DataSet dataSet;
    private Properties properties;

    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
    }

    @Override
    public void run() {
        generateMicroDepots();
        assignMicroZonesToMicroDepots();
        try {
            printOutData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void generateMicroDepots() {

        for (int zoneId : properties.getAnalysisZones()) {
            InternalZone internalZone = (InternalZone) dataSet.getZones().get(zoneId);
            SimpleFeature shapeFeature = internalZone.getShapeFeature();
            Geometry zoneGeometry = (Geometry) shapeFeature.getDefaultGeometry();
            BoundingBox bbox = shapeFeature.getBounds();

            double xMin = bbox.getMinX();
            double xMax = bbox.getMaxX();
            double yMin = bbox.getMinY();
            double yMax = bbox.getMaxY();

            double gridSpacing = properties.shortDistance().getDistanceBetweenMicrodepotsInGrid();

            double x = xMin + gridSpacing / 2;
            double y;
            int counter = 0;


            //logger.info(counter + "," + "x" + "," + "y" + "," + "distributionCenter" + "," + "microDepotMicroZone");

            while (x < xMax - gridSpacing / 2) {
                y = yMin + gridSpacing / 2;
                while (y < yMax - gridSpacing / 2) {
                    Coordinate coordinate = new Coordinate(x, y);
                    Geometry point = new GeometryFactory().createPoint(coordinate);
                    if (zoneGeometry.contains(point)) {
                        InternalMicroZone microDepotMicroZone = null;
                        DistributionCenter distributionCenter = null;
                        for (InternalMicroZone internalMicroZone : internalZone.getMicroZones().values()) {
                            Geometry microZoneGeometry = (Geometry) internalMicroZone.getShapeFeature().getDefaultGeometry();
                            if (microZoneGeometry.contains(point)) {
                                microDepotMicroZone = internalMicroZone;
                                for (DistributionCenter distributionCenterCandidate : dataSet.getDistributionCentersForZoneAndCommodityGroup(zoneId, CommodityGroup.PACKET).values()) {
                                    if (distributionCenterCandidate.getZonesServedByThis().contains(microDepotMicroZone)){
                                        //will find the last in the case there is overlap
                                        distributionCenter = distributionCenterCandidate;
                                    }
                                }

                            }
                        }
                        if (distributionCenter!= null && microDepotMicroZone!= null){
                            counter++;
                            MicroDepot microDepot = new MicroDepot(counter, String.valueOf(counter), coordinate, CommodityGroup.PACKET, distributionCenter, zoneId, microDepotMicroZone.getId());
                            distributionCenter.getMicroDeportsServedByThis().add(microDepot);

                            ParcelShop parcelShop = new ParcelShop(counter, String.valueOf(counter), coordinate, CommodityGroup.PACKET, distributionCenter, zoneId, microDepotMicroZone.getId());
                            distributionCenter.getParcelShopsServedByThis().add(parcelShop);

                            //logger.info(counter + "," + coordinate.x + "," + coordinate.y + "," + distributionCenter.getId() + "," + microDepotMicroZone.getId());
                        }
                    }
                    y+= gridSpacing;
                }

                x += gridSpacing;
            }
            logger.info("Generated " + counter + " synthetic microdepots.");
            logger.warn("Generated " + counter + " parcel shops at the same locations!");


        }


    }


    private void assignMicroZonesToMicroDepots() {

        double maxDistanceToMicroDepot = properties.shortDistance().getMaxDistanceToMicroDepot();
        logger.info("Assign: " + "microzone" +  "," + "microDepot" + "," + "distributionCenter");
        for (int zoneId : properties.getAnalysisZones()){
            for (DistributionCenter distributionCenter : dataSet.getDistributionCentersForZoneAndCommodityGroup(zoneId, CommodityGroup.PACKET).values()) {
                int counter = 0;
                for (InternalMicroZone internalMicroZone : distributionCenter.getZonesServedByThis()){
                    MicroDepot md = null;
                    double maxDistance = maxDistanceToMicroDepot;
                    for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()){
                        Geometry microZoneGeometry = (Geometry) internalMicroZone.getShapeFeature().getDefaultGeometry();
                        double currentDistance = microZoneGeometry.getCentroid().getCoordinate().distance(microDepot.getCoord_gk4());
                        if ( currentDistance < maxDistance){
                            maxDistance = currentDistance;
                            md = microDepot;
                        }
                    }
                    if (md != null && maxDistance < maxDistanceToMicroDepot){
                        md.getZonesServedByThis().add(internalMicroZone);
                        logger.info("Assign: " + internalMicroZone.getId() + "," + md.getId() + "," + distributionCenter.getId());
                        counter++;
                    }
                }
                logger.info("Assigned " + counter + " micro zones to micro depots in " + distributionCenter.getId());
            }

        }

        logger.info("Finished micro depot assignment" );


    }

    private void printOutData() throws FileNotFoundException {
        new DistributionCenterCsvWriter().writeToCsv(dataSet, "./output/" + properties.getRunId() + "/distributionCenters.csv");

    }

}
