package de.tum.bgu.msm.freight.modules.common;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.utils.leastcostpathtree.LeastCostPathTree;
import org.matsim.vehicles.Vehicle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UncongestedTravelTime {

    private static final Logger LOGGER = Logger.getLogger(UncongestedTravelTime.class);
    private Network network;
    //private LeastCostPathTree leastCostPathTree;
    private Map<Id<Link>, Double> linkOffPeakHourTimes;

    private final int DEFAULT_PEAK_H_S = 8 * 3600;

    public UncongestedTravelTime(String networkFile) {

        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(networkFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();

        linkOffPeakHourTimes = new ConcurrentHashMap<>();
        for (Link link : network.getLinks().values()) {
            linkOffPeakHourTimes.put(link.getId(), link.getLength() / link.getFreespeed());
        }


        LOGGER.info("Assigned travel times");
    }

    public double getTravelTime(Coordinate fromCoord, Coordinate toCoord) {

        LeastCostPathTree leastCostPathTree =
                new LeastCostPathTree(new MyTravelTime(linkOffPeakHourTimes), new MyTravelDisutility(linkOffPeakHourTimes, linkOffPeakHourTimes));

        double euclideanDistance = getDistancePointToPoint(fromCoord, toCoord);
        if (euclideanDistance > 200e3) {
            return euclideanDistance / 80 * 3.6;
        } else {
            Node originNode = NetworkUtils.getNearestLink(network, new Coord(fromCoord.x, fromCoord.y)).getToNode();
            Node destinationNode = NetworkUtils.getNearestLink(network, new Coord(toCoord.x, toCoord.y)).getToNode();
            leastCostPathTree.calculate(network, originNode, DEFAULT_PEAK_H_S);
            return leastCostPathTree.getTree().get(destinationNode.getId()).getTime();
        }
    }

    private double getDistancePointToPoint(Coordinate fromCoord, Coordinate toCoord) {
        return Math.sqrt(Math.pow(fromCoord.getX() - toCoord.getX(), 2) + Math.pow(fromCoord.getY() - toCoord.getY(), 2));
    }

    public void calculateTravelTimeMatrix(CoordinateTransformation ct, DataSet dataSet, Properties properties) {
        LOGGER.info("Starting calculation of matrix for " + dataSet.getZones().size() + " zones.");
        AtomicInteger counter = new AtomicInteger(0);
        dataSet.getZones().values().forEach(origin -> {
            Coordinate fromCoord = origin.getCoordinates(properties.getRand());
            Node originNode = NetworkUtils.getNearestLink(network, new Coord(fromCoord.x, fromCoord.y)).getToNode();
            LeastCostPathTree leastCostPathTree =
                    new LeastCostPathTree(new MyTravelTime(linkOffPeakHourTimes), new MyTravelDisutility(linkOffPeakHourTimes, linkOffPeakHourTimes));
            leastCostPathTree.calculate(network, originNode, DEFAULT_PEAK_H_S);
            for (Zone destination : dataSet.getZones().values()) {
                Coordinate toCoord = destination.getCoordinates(properties.getRand());
                double euclideanDistance = getDistancePointToPoint(fromCoord, toCoord);
                if (euclideanDistance > 200e3) {
                    dataSet.getUncongestedTravelTimeMatrix().put(origin.getId(), destination.getId(), euclideanDistance / 80 * 3.6);
                } else {
                    Node destinationNode = NetworkUtils.getNearestLink(network, new Coord(toCoord.x, toCoord.y)).getToNode();
                    double time = leastCostPathTree.getTree().get(destinationNode.getId()).getTime() - DEFAULT_PEAK_H_S;
                    dataSet.getUncongestedTravelTimeMatrix().put(origin.getId(), destination.getId(), time);
                }
            }
            LOGGER.info("Zone " + counter.incrementAndGet() + " completed (" + origin.getId() + ").");
        });
    }

    class MyTravelTime implements TravelTime {

        Map<Id<Link>, Double> peakHourTimes;

        public MyTravelTime(Map<Id<Link>, Double> peakHourTimes) {
            this.peakHourTimes = peakHourTimes;
        }

        @Override
        public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
            return peakHourTimes.get(link.getId());
        }
    }

    class MyTravelDisutility implements TravelDisutility {

        Map<Id<Link>, Double> peakHourTimes;
        Map<Id<Link>, Double> offPeakHourTimes;

        public MyTravelDisutility(Map<Id<Link>, Double> peakHourTimes, Map<Id<Link>, Double> offPeakHourTimes) {
            this.peakHourTimes = peakHourTimes;
            this.offPeakHourTimes = offPeakHourTimes;
        }

        @Override
        public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
            return peakHourTimes.get(link.getId());
        }

        @Override
        public double getLinkMinimumTravelDisutility(Link link) {
            return offPeakHourTimes.get(link.getId());
        }
    }
}
