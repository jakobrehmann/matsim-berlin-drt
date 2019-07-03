package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;

public class RunBerlinBike2 {
    private final String[] args;

    // NOTE: This config has been changed: the output config from the berlin scen was used. additionally, acttype car interaction was added
    private Config config= ConfigUtils.loadConfig("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/2019-06-26/input/config_NEW_berlin.xml",
            new SwissRailRaptorConfigGroup());

    public static void main( final String[] args ) {
        new RunBerlinBike2( args ).run() ;
    }

    public RunBerlinBike2( String [] args ) {
        this.args = args ;
    }

    public void run( ){
        ArrayList<String> modes = new ArrayList<>() ;
        modes.add("car");
        modes.add("freight");
        config.plansCalcRoute().setNetworkModes(modes);
        config.controler().setOutputDirectory("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/2019-06-26/output");
        config.controler().setLastIteration(1);
        config.network().setInputFile("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/2019-06-26/input/berlin-v5-network.xml.gz");
        Scenario scenario = ScenarioUtils.loadScenario(config );

        Controler controler = new Controler(scenario);

        new ConfigWriter(config).write("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/2019-06-26/config_trial.xml");
        controler.run();
    }
}
