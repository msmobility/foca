package de.tum.bgu.msm.analysis;

import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubPopulationGenerator {

    public static final Logger logger = Logger.getLogger(SubPopulationGenerator.class);

    public static void main(String[] args) {


        String scenario = args[0];

        Population population = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        Population subPopulation = PopulationUtils.createPopulation(ConfigUtils.createConfig());
        PopulationUtils.readPopulation(population, "./output/" + scenario + "/" + scenario + ".output_plans.xml.gz");

        Network network = NetworkUtils.readNetwork("./output/" + scenario + "/" + scenario + ".output_network.xml.gz");

        ArrayList<SimpleFeature> features = new ArrayList<>();
        features.addAll(ShapeFileReader.getAllFeatures(args[1]));
        SimpleFeature feature = features.get(0);


        List<String> listOfBoundaryLinks = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(args[2]));
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                listOfBoundaryLinks.add(line.split(",")[0]);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int countInternal = 0;
        int countThru = 0;
        int countInbound = 0;
        int countOutbound = 0;

        for (Person person : population.getPersons().values()) {
            boolean choosePerson = false;
            boolean originInside = false;
            boolean destInside = false;

            Plan plan = person.getSelectedPlan();
            Activity activityAtOrigin = (Activity) plan.getPlanElements().get(0);
            Coord origCoord = activityAtOrigin.getCoord();
            Route route = ((Leg) plan.getPlanElements().get(1)).getRoute();
            String[] routeString = route.getRouteDescription().split(" ");


            Map<Integer, Tuple<Double, Link>> borderCrossings = new HashMap<>();
            int sequence = 0;
            double time = activityAtOrigin.getEndTime().seconds();
            for (String linkId : routeString) {
                Link link = network.getLinks().get(Id.createLinkId(linkId));
                time += link.getLength() / link.getFreespeed();
                if (listOfBoundaryLinks.contains(linkId)) {
                    choosePerson = true;
                    borderCrossings.put(sequence, new Tuple<>(time, link));
                    sequence++;
                }
            }

            Activity activityAtDestination = (Activity) plan.getPlanElements().get(2);
            Coord destCoord = activityAtDestination.getCoord();

            GeometryFactory factory = new GeometryFactory();

            Point originPoint = factory.createPoint(new Coordinate(origCoord.getX(), origCoord.getY()));

            if (((Geometry) feature.getDefaultGeometry()).contains(originPoint)) {
                originInside = true;
                choosePerson = true;
            }
            Point destPoint = factory.createPoint(new Coordinate(destCoord.getX(), destCoord.getY()));

            if (((Geometry) feature.getDefaultGeometry()).contains(destPoint)) {
                destInside = true;
                choosePerson = true;
            }

            if (choosePerson && borderCrossings.size() > 0) {
                Person newPerson = subPopulation.getFactory().createPerson(person.getId());
                Plan newPlan = PopulationUtils.createPlan();
                newPerson.addPlan(newPlan);
                Activity orig;
                Activity dest;

                Link firstLinkAtBorder = borderCrossings.get(0).getSecond();
                double firstCrossingTime = borderCrossings.get(0).getFirst();

                if (originInside) {
                    orig = PopulationUtils.createActivityFromCoord(activityAtOrigin.getType(), activityAtOrigin.getCoord());
                    orig.setEndTime(activityAtOrigin.getEndTime().seconds());
                    if (destInside) {
                        //internal
                        countInternal++;
                        dest = PopulationUtils.createActivityFromCoord(activityAtDestination.getType(), activityAtDestination.getCoord());
                        newPlan.addActivity(orig);
                        newPlan.addLeg(PopulationUtils.createLeg(TransportMode.car));
                        newPlan.addActivity(dest);
                        subPopulation.addPerson(newPerson);
                    } else {
                        //outbound
                        countOutbound++;
                        dest = PopulationUtils.createActivityFromCoord(activityAtDestination.getType(), firstLinkAtBorder.getFromNode().getCoord());
                        newPlan.addActivity(orig);
                        newPlan.addLeg(PopulationUtils.createLeg(TransportMode.car));
                        newPlan.addActivity(dest);
                        subPopulation.addPerson(newPerson);
                    }
                } else {
                    orig = PopulationUtils.createActivityFromCoord(activityAtOrigin.getType(), firstLinkAtBorder.getFromNode().getCoord());
                    orig.setEndTime(firstCrossingTime);
                    if (destInside) {
                        //inbound
                        countInbound++;
                        dest = PopulationUtils.createActivityFromCoord(activityAtDestination.getType(), activityAtDestination.getCoord());
                        newPlan.addActivity(orig);
                        newPlan.addLeg(PopulationUtils.createLeg(TransportMode.car));
                        newPlan.addActivity(dest);
                        subPopulation.addPerson(newPerson);
                    } else {
                        //thru
                        countThru++;
                        try {
                            Link secondLinkAtBorder = borderCrossings.get(1).getSecond();
                            dest = PopulationUtils.createActivityFromCoord(activityAtDestination.getType(), secondLinkAtBorder.getFromNode().getCoord());
                            newPlan.addActivity(orig);
                            newPlan.addLeg(PopulationUtils.createLeg(TransportMode.car));
                            newPlan.addActivity(dest);
                            subPopulation.addPerson(newPerson);
                        } catch (NullPointerException e ){
                            logger.warn("A second crossing point is not found!");
                        }
                    }

                }


            }


        }

        logger.info("Total before: " + population.getPersons().size());
        logger.info("Internal: " + countInternal);
        logger.info("Inbound: " + countInbound);
        logger.info("Outbound: " + countOutbound);
        logger.info("Thru: " + countThru);

        PopulationWriter pw = new PopulationWriter(subPopulation);
        pw.write("output/" + scenario + "/" + scenario + ".mito_network_plans.xml.gz");

    }


}
