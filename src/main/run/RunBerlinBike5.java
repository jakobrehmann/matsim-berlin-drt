package main.run;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

import java.util.Arrays;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.apache.log4j.Logger;
import org.matsim.analysis.ScoreStats;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.*;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;


public class RunBerlinBike5 {

    static String configFileName = "C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-03\\input\\berlin-v5.4-1pct.config.xml";
    Config config ;
    public static void main(String[] args) {

        // -- CONFIG --
        Config config = ConfigUtils.loadConfig( configFileName); //, customModules ) ; // I need this to set the context

        config.controler().setLastIteration(0); // jr
        config.controler().setOutputDirectory("C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-03\\output");
//        config.network().setInputFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-network.xml.gz");
//        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz");
//        config.plans().setInputPersonAttributeFile("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-person-attributes.xml.gz");
//        config.vehicles().setVehiclesFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-mode-vehicle-types.xml");
//        config.transit().setTransitScheduleFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-transit-schedule.xml.gz");
//        config.transit().setVehiclesFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5.4-transit-vehicles.xml.gz");
        config.network().setInputFile("berlin-v5-network.xml.gz");
        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz");
        config.plans().setInputPersonAttributeFile("berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("berlin-v5.4-transit-vehicles.xml.gz");

        // Introducing Zoomer!

        PlanCalcScoreConfigGroup.ModeParams zoomParams = new PlanCalcScoreConfigGroup.ModeParams("zoomer");
        zoomParams.setMarginalUtilityOfTraveling(0.);
        config.planCalcScore().addModeParams(zoomParams);

        PlansCalcRouteConfigGroup.ModeRoutingParams zoomRoutingParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("zoomer");
        zoomRoutingParams.setBeelineDistanceFactor(1.3);
        zoomRoutingParams.setTeleportedModeSpeed(10000.);
        config.plansCalcRoute().addModeRoutingParams(zoomRoutingParams);


//        SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
        SwissRailRaptorConfigGroup configRaptor = ConfigUtils.addOrGetModule( config, SwissRailRaptorConfigGroup.class ) ;
        configRaptor.setUseIntermodalAccessEgress(true);

        // Walk
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalk = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalk.setMode(TransportMode.walk);
        paramSetWalk.setRadius(1);
        paramSetWalk.setPersonFilterAttribute(null);
        paramSetWalk.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalk );

        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkA = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalkA.setMode(TransportMode.access_walk);
        paramSetWalkA.setRadius(1);
        paramSetWalkA.setPersonFilterAttribute(null);
        paramSetWalkA.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalkA );

        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkE = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalkE.setMode(TransportMode.egress_walk);
        paramSetWalkE.setRadius(1);
        paramSetWalkE.setPersonFilterAttribute(null);
        paramSetWalkE.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalkE );

        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkNN = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalkNN.setMode(TransportMode.transit_walk);
        paramSetWalkNN.setRadius(1);
        paramSetWalkNN.setPersonFilterAttribute(null);
        paramSetWalkNN.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalkNN );


        // Zoomer
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetBike = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetBike.setMode("zoomer");
        paramSetBike.setRadius(10000000);
        paramSetBike.setPersonFilterAttribute(null);
//        paramSetBike.setStopFilterAttribute("bikeAccessible");
//        paramSetBike.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetBike );

//        SwissRailRaptorConfigGroup raptor = configRaptor;
//        config.addModule(raptor);


        config.controler().setRoutingAlgorithmType( FastAStarLandmarks );

        config.subtourModeChoice().setProbaForRandomSingleTripMode( 0.5 );

        config.plansCalcRoute().setRoutingRandomness( 3. );
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
        config.plansCalcRoute().removeModeRoutingParams("undefined");

        config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles( true );

        // vsp defaults
        config.plansCalcRoute().setInsertingAccessEgressWalk( true );
        config.qsim().setUsingTravelTimeCheckInTeleportation( true );
        config.qsim().setTrafficDynamics( TrafficDynamics.kinematicWaves );


        config = SetupActivityParams(config);

        // --Scenario--
        Scenario scenario = ScenarioUtils.loadScenario( config );

        // --CONTROLER--
        Controler controler = new Controler( scenario );
        controler.addOverridingModule(new SwissRailRaptorModule()); // jr

        // use the (congested) car travel time for the teleported ride mode
        controler.addOverridingModule( new AbstractModule() {
            @Override
            public void install() {
                addTravelTimeBinding( TransportMode.ride ).to( networkTravelTime() );
                addTravelDisutilityFactoryBinding( TransportMode.ride ).to( carTravelDisutilityFactoryKey() );
            }
        } );

//        for ( AbstractModule overridingModule : overridingModules ) {
//            controler.addOverridingModule( overridingModule );
//        }


        new ConfigWriter(config).write("C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-03\\config_trial.xml");
        controler.run();

    }


    private static Config SetupActivityParams(Config config) {
        // activities:
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final ActivityParams params = new ActivityParams( "home_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final ActivityParams params = new ActivityParams( "work_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            params.setOpeningTime(6. * 3600.);
            params.setClosingTime(20. * 3600.);
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final ActivityParams params = new ActivityParams( "leisure_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            params.setOpeningTime(9. * 3600.);
            params.setClosingTime(27. * 3600.);
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final ActivityParams params = new ActivityParams( "shopping_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            params.setOpeningTime(8. * 3600.);
            params.setClosingTime(20. * 3600.);
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final ActivityParams params = new ActivityParams( "other_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            config.planCalcScore().addActivityParams( params );
        }
        {
            final ActivityParams params = new ActivityParams( "freight" ) ;
            params.setTypicalDuration( 12.*3600. );
            config.planCalcScore().addActivityParams( params );
        }

        return config ;
    }
}
