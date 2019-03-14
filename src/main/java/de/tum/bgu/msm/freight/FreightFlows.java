package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.FlowsToVehicleAssignment;
import de.tum.bgu.msm.freight.modules.runMATSim.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.runMATSim.MatsimPopulationGenerator;
import de.tum.bgu.msm.freight.properties.Properties;

import java.io.IOException;

public class FreightFlows {

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();

        /*
        Place to configure the properties according to users' prefrereces, otherwise the default values are chosen
         */

        properties.setSelectedDestinations(new int[]{9162, 9362});
        properties.setRunId("assignmentFull4");
        properties.setIterations(1);

        //properties.setStoreExpectedTimes(true);

        properties.logUsedProperties();

        InputManager io = new InputManager(properties);
        io.readInput();

        DataSet dataSet = io.getDataSet();

        FlowsToVehicleAssignment flowsToVehicleAssignment = new FlowsToVehicleAssignment();
        MatsimPopulationGenerator matsimPopulationGenerator = new MatsimPopulationGenerator();
        MATSimAssignment matSimAssignment = new MATSimAssignment();

        flowsToVehicleAssignment.setup(dataSet, properties);
        matsimPopulationGenerator.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        flowsToVehicleAssignment.run();
        matsimPopulationGenerator.run();
        matSimAssignment.run();



    }
}
