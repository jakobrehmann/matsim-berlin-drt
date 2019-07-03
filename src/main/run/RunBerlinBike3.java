package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;

public class RunBerlinBike3 {
    public static void main(String[] args){

        // **Config**
        // Config produced from output. ModeParams for Bike added. planscalcroute params added. Activity Params for "car interaction" added
        Config config= ConfigUtils.loadConfig("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/2019-06-26/input/config_NEW_berlin.xml", new SwissRailRaptorConfigGroup());
//        ArrayList<String> modes = new ArrayList<>() ;
//        modes.add("car");
//        modes.add("freight");
//        config.plansCalcRoute().setNetworkModes(modes);

//        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.walk ).setTeleportedModeSpeed( 3. );
//        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.bike ).setTeleportedModeSpeed( 10000. );

//
//        PlanCalcScoreConfigGroup.ModeParams bike = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.bike);
//        bike.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(bike);


        config.controler().setOutputDirectory("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/2019-06-26/output");
        config.controler().setLastIteration(0);
        config.network().setInputFile("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/2019-06-26/input/berlin-v5-network.xml.gz");

        config.plansCalcRoute().setInsertingAccessEgressWalk(true);
        //config.plansCalcRoute().removeModeRoutingParams( TransportMode.ride );

        config.removeModule("swissRailRaptor");
        SwissRailRaptorConfigGroup configRaptor = createRaptorConfigGroup(10, 1000000);// (radius walk, radius bike)
        config.addModule(configRaptor);


        // **Scenario**
        Scenario scenario = ScenarioUtils.loadScenario(config );

        // **Controler**
        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new SwissRailRaptorModule()) ;

        // use the (congested) car travel time for the teleported ride mode
        controler.addOverridingModule( new AbstractModule() {
            @Override
            public void install() {
                addTravelTimeBinding( TransportMode.ride ).to( networkTravelTime() );
                addTravelDisutilityFactoryBinding( TransportMode.ride ).to( carTravelDisutilityFactoryKey() );
            }
        } );

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
//        paramSetBike.setMode(TransportMode.bike);
        paramSetBike.setMode(TransportMode.bike);
        paramSetBike.setRadius(radiusBike);
        paramSetBike.setPersonFilterAttribute(null);
//        paramSetBike.setStopFilterAttribute("bikeAccessible");
//        paramSetBike.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetBike );

        return configRaptor;
    }

}
