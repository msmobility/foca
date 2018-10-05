package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

public class MATSimAssignment {

    private Config config;
    private MutableScenario scenario;
    private int iterations = 10;
    private double scaleFactor = 0.1;
    private FreightFlowsDataSet dataSet;

    public void load(FreightFlowsDataSet dataSet){
        this.dataSet = dataSet;
        configMatsim();
        createPopulation();
    }

    public void run() {
        runMatsim();
    }

    private void configMatsim() {
        config = ConfigUtils.createConfig();

        config.controler().setFirstIteration(0);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ChangeExpBeta");
            strategySettings.setWeight(0.5);
            config.strategy().addStrategySettings(strategySettings);
        }{
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.5);
            config.strategy().addStrategySettings(strategySettings);
        }

        config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
        config.strategy().setMaxAgentPlanMemorySize(4);

        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("production");
        homeActivity.setTypicalDuration(12*60*60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("consumption");
        workActivity.setTypicalDuration(8*60*60);
        config.planCalcScore().addActivityParams(workActivity);

        String runId = "flowAssignment";
        config.controler().setRunId(runId);
        config.controler().setOutputDirectory("./output/");
        config.network().setInputFile("./networks/matsim/germany.xml.gz");

        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        config.qsim().setUsingThreadpool(false);

        config.controler().setLastIteration(iterations);
        config.controler().setWritePlansInterval(iterations);
        config.controler().setWriteEventsInterval(iterations);

        config.qsim().setStuckTime(10);
        //skips scaling of the network!
        config.qsim().setFlowCapFactor(1);
        config.qsim().setStorageCapFactor(1);
    }

    private void createPopulation() {
        FlowsToVehicleAssignment flowsToVehicleAssignment = new FlowsToVehicleAssignment(dataSet);
        Population population = flowsToVehicleAssignment.disaggregateToVehicles(config, scaleFactor);
        scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        scenario.setPopulation(population);
    }

    private void runMatsim() {
        final Controler controler = new Controler(scenario);
        controler.run();
    }

}
