package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.data.Purpose;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TripCSVToMATSimPlan {

	// This class will read trip lists in CSV from MITO and creates a MATSim XML
	// plan file
	private static String delimiter = ",";

	private static String filename;
	private static String outputFileName;
	private static PopulationFactory factory;

	private static final double scaleFactor = 0.20;

	public static void main(String[] args) {
		// TODO add logging
		filename = args[0];
		outputFileName = args[1];
		Config config = ConfigUtils.createConfig();


		Scenario scenario = ScenarioUtils.loadScenario(config);
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(scenario.getNetwork());
		Set<String> modesCar = new HashSet<>();
		modesCar.add("car");

		Population population = PopulationUtils.createPopulation(config);
		factory = population.getFactory();
		Random random = new Random(0);

		try {
			FileReader in = null;
			BufferedReader br = null;
			try {
				in = new FileReader(filename);
				br = new BufferedReader(in);

				String line;
				int i = 0;
				br.readLine(); // skip CSV header
				while ((line = br.readLine()) != null) {
					if (random.nextDouble() < scaleFactor){
						Person p = createPersonFromTrip(i++, line);
						if (p != null) {
							population.addPerson(p);
						}
					}
				}
			} finally {
				if (br != null) {
					br.close();
				}

				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		PopulationWriter popwriter = new PopulationWriter(population);
		popwriter.write(outputFileName);

		System.out.println("done.");
	}

	private static Person createPersonFromTrip(int i, String line) {
		Trip t = new Trip(line);

		String mode = decodeMode(t.mode);
		Id<Person> matsimId = Id.createPersonId(t.id);

		Person p = factory.createPerson(Id.createPersonId(matsimId));
		Plan plan = factory.createPlan();

		Purpose purpose = Purpose.valueOf(t.purpose);
		boolean roundTrip = !(purpose.equals(Purpose.NHBW) || purpose.equals(Purpose.NHBO));

		String firstActivityType = getOriginActivity(purpose);
		Coord firstCoord = new Coord(t.originX, t.originY);

		Activity firstAct = factory.createActivityFromCoord(firstActivityType, firstCoord);
		firstAct.setEndTime(t.departure_time);
		plan.addActivity(firstAct);

		Leg firstLeg = factory.createLeg(mode);
		firstLeg.setDepartureTime(t.departure_time);
		plan.addLeg(firstLeg);

		String secondActivityType = getDestinationActivity(purpose);
		Coord secondCoord = new Coord(t.destinationX, t.destinationY);

		Activity secondAct = factory.createActivityFromCoord(secondActivityType, secondCoord);
		plan.addActivity(secondAct);


		if (roundTrip) {
			secondAct.setEndTime(t.departure_time_return);

			Leg secondLeg = factory.createLeg(mode);
			secondLeg.setDepartureTime(t.departure_time_return);
			plan.addLeg(secondLeg);

			Activity thirdAct = factory.createActivityFromCoord(firstActivityType, firstCoord);
			plan.addActivity(thirdAct);
		}

		p.addPlan(plan);
		p.setSelectedPlan(plan);
		return p;
	}

	private static String getDestinationActivity(Purpose purpose) {
		return "dest_passenger";
	}

	private static String getOriginActivity(Purpose purpose) {
		return "orig_passenger";
	}

	private static String decodeMode(String encodedMode) {
		switch (encodedMode) {
		case "autoDriver":
			return "car";
		case "autoPassenger":
			return "car_passenger";
		case "train":
		case "bus":
		case "tramOrMetro":
			return "pt";
		case "bicycle":
			return "bike";
		default:
			return encodedMode;
		}
	}

	public final static class Trip {
		public final double originX;
		public final double originY;
		public final double destinationX;
		public final double destinationY;
		public final String purpose;
		public final String id;
		public final double distance;
		public final String mode;
		public final double departure_time;
		public final double departure_time_return;

		public Trip(String line) {
			String[] data = line.split(delimiter);
			this.originX = Double.parseDouble(data[2]);
			this.originY = Double.parseDouble(data[3]);
			this.destinationX = Double.parseDouble(data[5]);
			this.destinationY = Double.parseDouble(data[6]);
			this.purpose = data[7];
			this.id = data[0];
			this.distance = Double.parseDouble(data[9]);
			this.mode = data[14];
			// departure time comes in minutes, needed as seconds
			this.departure_time = Double.parseDouble(data[15]) * 60; 

			if (!data[16].equals("NA")) {
				this.departure_time_return = Double.parseDouble(data[16]) * 60;
			}
			else {
				this.departure_time_return = -1;
			}
		}
	}
}
