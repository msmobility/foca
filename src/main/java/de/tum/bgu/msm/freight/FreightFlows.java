package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.modules.assignment.MATSimAssignment;

public class FreightFlows {

    public static void main (String[] args){

        InputManager io = new InputManager();
        io.readInput();

        MATSimAssignment matsimAssignment = new MATSimAssignment();
        matsimAssignment.load(io.getDataSet());
        matsimAssignment.run();


    }
}
