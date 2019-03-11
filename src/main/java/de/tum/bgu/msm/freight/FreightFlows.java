package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.modules.runMATSim.MATSimAssignment;
import de.tum.bgu.msm.freight.properties.Properties;

import java.io.IOException;

public class FreightFlows {

    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();

        /*
        Place to configure the properties according to users' prefrereces, otherwise the default values are chosen
         */

        properties.setSelectedDestinations(new int[]{9162, 9362});
        properties.setRunId("assignmentFull3");
        properties.setIterations(1);

        //properties.setStoreExpectedTimes(true);

        properties.logUsedProperties();

        InputManager io = new InputManager(properties);
        io.readInput();

        MATSimAssignment matsimAssignment = new MATSimAssignment(properties);
        matsimAssignment.load(io.getDataSet());
        matsimAssignment.run();


    }
}
