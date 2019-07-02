package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

import static main.run.RunBerlin.createConfig;

public class RunBerlinBike {

    public static void main(String[] args) {
        String username = "jakob";
        Path rootPath = null;

        switch (username) {
            case "jakob":
                rootPath = Paths.get("C:/Users/jakob/tubCloud/Shared/DRT");
                break;
            case "david":
                rootPath = Paths.get("C:/Users/david/ENTER_PATH_HERE");
                break;
            default:
                System.out.println("Incorrect Base Path");
        }

        // Config
        Path outputDir = Paths.get(rootPath.toString() + "/PolicyCase/2019-06-26/output");
        Config config = createConfig(outputDir.toString(), rootPath);

        SwissRailRaptorConfigGroup configRaptor = createRaptorConfigGroup(1000000, 1000000);// (radius walk, radius bike)
        config.addModule(configRaptor);

        Scenario scenario = ScenarioUtils.createScenario(config) ;

        Controler controler = new Controler(scenario);

        controler.addOverridingModule(new SwissRailRaptorModule());

        // This will start otfvis.  Comment out if not needed.
//		controler.addOverridingModule( new OTFVisLiveModule() );

        controler.run();
    }

    static SwissRailRaptorConfigGroup createRaptorConfigGroup(int radiusWalk, int radiusBike) {
        SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
        configRaptor.setUseIntermodalAccessEgress(true);

        // Walk
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalk = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalk.setMode(TransportMode.walk);
        paramSetWalk.setRadius(radiusWalk);
        paramSetWalk.setPersonFilterAttribute(null);
        paramSetWalk.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalk );

        // Bike
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetBike = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetBike.setMode(TransportMode.bike);
        paramSetBike.setRadius(radiusBike);
        paramSetBike.setPersonFilterAttribute(null);
        paramSetBike.setStopFilterAttribute("bikeAccessible");
        paramSetBike.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetBike );

        return configRaptor;
    }


}