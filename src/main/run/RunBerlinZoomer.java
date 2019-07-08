package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;
import static org.matsim.core.config.groups.PlanCalcScoreConfigGroup.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.*;
import org.matsim.core.config.groups.*;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
import org.matsim.core.scenario.ScenarioUtils;



/** Mode zoomer is a teleported mode that can only be used as an access/egress mode within a pt route. Zoomer is a
 * placeholder mode, which can be configured in order to emulate bike, drt, or other modes.
 * TODO: now that I limiked population to Frohnau, no one is using Zoomer. Not sure if it is because of the population
 *      or because of something else I changed...
 * TODO: Is there a way to clear the intitial plans? That way, in the 0th iteration during the intial routing, zoomer would
 *      obviously chosen instead of walk for access and egress
 */


public class RunBerlinZoomer {

    public static void main(String[] args) {

        String username = "jakob";
        String version = "2019-07-08";
        String rootPath = null;

        switch (username) {
            case "jakob":
                rootPath = "C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/";
                break;
            case "david":
                rootPath = "C:/Users/david/ENTER_PATH_HERE";
                break;
            default:
                System.out.println("Incorrect Base Path");
        }

        String configFileName = rootPath + version + "/input/berlin-v5.4-1pct.config.xml";

        // -- C O N F I G --
        Config config = ConfigUtils.loadConfig( configFileName); //, customModules ) ; // I need this to set the context

        // Input Files -- from server
/*      config.network().setInputFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-network.xml.gz");
        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz");
        config.plans().setInputPersonAttributeFile("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5.4-transit-vehicles.xml.gz");
*/

        // Input Files -- local
        config.network().setInputFile("berlin-v5-network.xml.gz");
//        config.plans().setInputFile("berlin-plans-Frohnau.xml"); // 1% population in Frohnau
//        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz"); // full 1% population
        config.plans().setInputFile("berlin-downsample.xml"); // 1% population in Frohnau
        config.plans().setInputPersonAttributeFile("berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("berlin-v5.4-transit-vehicles.xml.gz");


        config.controler().setLastIteration(50);
        config.global().setNumberOfThreads( 1 );
        config.controler().setOutputDirectory(rootPath + version + "/output/C/");
        config.controler().setRoutingAlgorithmType( FastAStarLandmarks );
        config.transit().setUseTransit(true) ;
        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn );
        config.controler().setWritePlansInterval(5);

        // QSim
        config.qsim().setSnapshotStyle( QSimConfigGroup.SnapshotStyle.kinematicWaves );
        config.qsim().setTrafficDynamics( TrafficDynamics.kinematicWaves );
        config.qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );
        config.qsim().setSimStarttimeInterpretation( QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime );
        config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles( true );
        config.qsim().setEndTime( 24.*3600. );
        config.qsim().setUsingTravelTimeCheckInTeleportation( true );


        // Scoring
        config = SetupActivityParams(config);

        // Routing
        config.plansCalcRoute().setInsertingAccessEgressWalk( true );
        config.plansCalcRoute().setRoutingRandomness( 3. );
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
        config.plansCalcRoute().removeModeRoutingParams("undefined");

        // Replanning
        config.subtourModeChoice().setProbaForRandomSingleTripMode( 0.5 );
//        config.strategy().setFractionOfIterationsToDisableInnovation(1.); // temp
//        config.strategy().clearStrategySettings();
//        StrategyConfigGroup.StrategySettings ReRoute = new StrategyConfigGroup.StrategySettings();
//        ReRoute.setWeight(1.);
////        ReRoute.setStrategyName("ReRoute");
////        ReRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ReRoute);
//        ReRoute.setStrategyName(DefaultPlanStrategiesModule.DefaultStrategy.ChangeTripMode);
//        ReRoute.setSubpopulation("person");
//        config.strategy().addStrategySettings(ReRoute);
//
//        StrategyConfigGroup.StrategySettings ReRouteFreight = new StrategyConfigGroup.StrategySettings();
//        ReRouteFreight.setWeight(1.);
//        ReRouteFreight.setStrategyName("ReRoute");
//        ReRouteFreight.setSubpopulation("freight");
//        config.strategy().addStrategySettings(ReRouteFreight);


        // Zoomer Setup
        ModeParams zoomParams = new ModeParams("zoomer");
        zoomParams.setMarginalUtilityOfTraveling(100);
        config.planCalcScore().addModeParams(zoomParams);

        PlansCalcRouteConfigGroup.ModeRoutingParams zoomRoutingParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
        zoomRoutingParams.setMode("zoomer");
        zoomRoutingParams.setBeelineDistanceFactor(1.3);
        zoomRoutingParams.setTeleportedModeSpeed(10000.);
        config.plansCalcRoute().addModeRoutingParams(zoomRoutingParams);

        // walk 2 setup
//        PlansCalcRouteConfigGroup.ModeRoutingParams pars = new PlansCalcRouteConfigGroup.ModeRoutingParams();
//        pars.setMode("walk2");
//        pars.setTeleportedModeSpeed(5./3.6);
        {
            PlansCalcRouteConfigGroup.ModeRoutingParams wlk = new PlansCalcRouteConfigGroup.ModeRoutingParams(  ) ;
            wlk.setMode( "walk2" ) ;
            wlk.setTeleportedModeSpeed( 5./3.6 ) ;
            config.plansCalcRoute().addModeRoutingParams( wlk );
        }


        double margUtlTravPt = config.planCalcScore().getModes().get( TransportMode.pt ).getMarginalUtilityOfTraveling();
        ModeParams modePars = new ModeParams("walk2");
        modePars.setMarginalUtilityOfTraveling(margUtlTravPt);
        config.planCalcScore().addModeParams( modePars );


        // Raptor
//        SwissRailRaptorConfigGroup raptor = setupRaptorConfigGroup();
//        config.addModule(raptor);
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
        paramSetBike.setRadius(10000);
        paramSetBike.setPersonFilterAttribute(null);
//        paramSetBike.setStopFilterAttribute("bikeAccessible");
//        paramSetBike.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetBike );


        // -- S C E N A R I O --
        Scenario scenario = ScenarioUtils.loadScenario( config );

        // -- C O N T R O L E R --
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


//        new ConfigWriter(config).write("C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-03\\config_trial.xml");
        controler.run(); //


    }

    private static SwissRailRaptorConfigGroup setupRaptorConfigGroup() {
        SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
        configRaptor.setUseIntermodalAccessEgress(true);

//        // Walk
//        IntermodalAccessEgressParameterSet paramSetWalk = new IntermodalAccessEgressParameterSet();
//        paramSetWalk.setMode(TransportMode.walk);
//        paramSetWalk.setRadius(1);
//        paramSetWalk.setPersonFilterAttribute(null);
//        paramSetWalk.setStopFilterAttribute(null);
//        configRaptor.addIntermodalAccessEgress(paramSetWalk );

        // Walk 2
        IntermodalAccessEgressParameterSet paramSetXxx = new IntermodalAccessEgressParameterSet();
        //					paramSetXxx.setMode( TransportMode.walk ); // this does not work because sbb raptor treats it in a special way
        paramSetXxx.setMode( "walk2" );
        paramSetXxx.setRadius( 1000000 );
        configRaptor.addIntermodalAccessEgress( paramSetXxx );
        // (in principle, walk as alternative to drt will not work, since drt is always faster.  Need to give the ASC to the router!  However, with
        // the reduced drt network we should be able to see differentiation.)

        // Zoomer
        IntermodalAccessEgressParameterSet paramSetZoomer = new IntermodalAccessEgressParameterSet();
        paramSetZoomer.setMode("zoomer");
        paramSetZoomer.setRadius(10000000);
        paramSetZoomer.setPersonFilterAttribute(null);
//        paramSetZoomer.setStopFilterAttribute("bikeAccessible");
//        paramSetZoomer.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetZoomer );

        return configRaptor;
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

    public static ArrayList<String> readIdFile(String fileName){
        Scanner s ;
        ArrayList<String> list = new ArrayList<>();
        try {
            s = new Scanner(new File(fileName));
            while (s.hasNext()){
                list.add(s.next());
            }
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        return list;
    }
}
