package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import de.tum.bgu.msm.freight.modules.assignment.counts.CountEventHandler;
import de.tum.bgu.msm.freight.modules.assignment.counts.LinksFileReader;
import de.tum.bgu.msm.freight.modules.assignment.counts.MultiDayCounts;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;
import java.util.Map;

public class MATSimAssignment {

    private Config config;
    private MutableScenario scenario;

    private Properties properties;
    private FreightFlowsDataSet dataSet;

    public MATSimAssignment(Properties properties){
        this.properties = properties;
    }

    public void load(FreightFlowsDataSet dataSet) throws IOException {
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
        }
        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.5);
            config.strategy().addStrategySettings(strategySettings);
        }

        config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
        config.strategy().setMaxAgentPlanMemorySize(4);

        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("start");
        homeActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("end");
        workActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(workActivity);

        config.controler().setRunId(properties.getRunId());
        config.controler().setOutputDirectory("./output/" + properties.getRunId() + "/matsim");
        config.network().setInputFile(properties.getNetworkFile());

        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        config.qsim().setUsingThreadpool(false);

        int iterations = properties.getIterations();
        config.controler().setLastIteration(iterations);
        config.controler().setWritePlansInterval(iterations);
        config.controler().setWriteEventsInterval(iterations);

        config.qsim().setStuckTime(10);
        //skips scaling of the network!
        config.qsim().setFlowCapFactor(1);
        config.qsim().setStorageCapFactor(1);

        config.linkStats().setWriteLinkStatsInterval(1);
    }

    private void createPopulation() throws IOException {
        FlowsToVehicleAssignment flowsToVehicleAssignment = new FlowsToVehicleAssignment(dataSet, properties);
        Population population = flowsToVehicleAssignment.disaggregateToVehicles(config);
        flowsToVehicleAssignment.printOutResults();

        scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        scenario.setPopulation(population);
    }

    private void runMatsim() {
        final Controler controler = new Controler(scenario);

        CountEventHandler countEventHandler = new CountEventHandler();
        if (properties.isReadEventsForCounts()) {
            LinksFileReader linksFileReader = new LinksFileReader(null, properties.getCountStationLinkListFile());
            linksFileReader.read();

            for (String linkId : linksFileReader.getListOfIds()) {
                countEventHandler.addLinkById(linkId);
            }


            countEventHandler.addLinkById("1");
            controler.getEvents().addHandler(countEventHandler);
        }
        controler.run();
        if (properties.isReadEventsForCounts()) {
            Map<Id, Integer> mapOfCounts = countEventHandler.getMapOfCOunts();
            try {
                MultiDayCounts.printOutCounts("./output/" + properties.getRunId() + "/" + properties.getCountsFileNameWithoutPath(), mapOfCounts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
