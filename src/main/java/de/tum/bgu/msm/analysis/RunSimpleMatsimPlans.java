package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.modules.assignment.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.assignment.MATSimConfigUtils;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunSimpleMatsimPlans {

    public static void main(String[] args) {


        Config config = ConfigUtils.createConfig();
        Properties properties = new Properties();
        properties.setNetworkFile("networks/matsim/studyNetworkDense.xml.gz");
        properties.setRunId("subAreaAssignment");
        properties.setIterations(10);


        config = MATSimConfigUtils.configure(config, properties);
        config.plans().setInputFile("output/ld_all_2011/ld_all_2011.mito_network_plans.xml.gz");
        Scenario scenario = ScenarioUtils.loadScenario(config);

        Controler controler = new Controler(scenario);
        controler.run();



    }

}
