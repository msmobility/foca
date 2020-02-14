package de.tum.bgu.msm.analysis;

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
        properties.setRunId("test_passengers_100");
        properties.setIterations(50);

        properties.setNetworkFile("./networks/matsim/muc_all_bicycle_v2.xml.gz");


        config = MATSimConfigUtils.configure(config, properties);
        config.plans().setInputFile("input/carPlans/plans_dc_20.xml.gz");

//        config.qsim().setFlowCapFactor(0.05);
//        config.qsim().setStorageCapFactor(0.05);

        Scenario scenario = ScenarioUtils.loadScenario(config);

        Controler controler = new Controler(scenario);
        controler.run();



    }

}
