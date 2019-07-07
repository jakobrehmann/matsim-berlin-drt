package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;

import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.*;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;


/** drt is used as an access/egress mode for a pt modes. Not yet functional. These are the next steps:
 * 1) get drt running, so that agents begin to use drt to access/egress from pt
 * 2) limit this functionality to Frohnau.
 */

public class RunBerlinDrt {

    public static void main(String[] args) {
        String username = "jakob";
        String version = "2019-07-05";
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

        // Input Files -- local
        config.network().setInputFile("berlin-v5-network.xml.gz");
        config.plans().setInputFile("berlin-plans-Frohnau.xml"); // 1% population in Frohnau
//        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz"); // full 1% population
        config.plans().setInputPersonAttributeFile("berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("berlin-v5.4-transit-vehicles.xml.gz");


//      config.plans().setRemovingUnneccessaryPlanAttributes(true);

        config.controler().setLastIteration(15);
        config.global().setNumberOfThreads( 1 );
        config.controler().setOutputDirectory("C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-05\\output");
        config.controler().setRoutingAlgorithmType( FastAStarLandmarks );
        config.transit().setUseTransit(true) ;
        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn );

        // QSim
        config.qsim().setSnapshotStyle( QSimConfigGroup.SnapshotStyle.kinematicWaves );
        config.qsim().setTrafficDynamics( QSimConfigGroup.TrafficDynamics.kinematicWaves );
        config.qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );
        config.qsim().setSimStarttimeInterpretation( QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime );
        config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles( true );
        config.qsim().setEndTime( 24.*3600. );
        config.qsim().setUsingTravelTimeCheckInTeleportation( true );


        // Scoring
        config = SetupActivityParams(config);
        configureScoring(config);

        // Routing
        config.plansCalcRoute().setInsertingAccessEgressWalk( true );
        config.plansCalcRoute().setRoutingRandomness( 3. );
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
        config.plansCalcRoute().removeModeRoutingParams("undefined");

        // Replanning
        config.subtourModeChoice().setProbaForRandomSingleTripMode( 0.5 );
        config.strategy().setFractionOfIterationsToDisableInnovation(1.); // temp
        config.strategy().clearStrategySettings();
        StrategyConfigGroup.StrategySettings ReRoute = new StrategyConfigGroup.StrategySettings();
        ReRoute.setWeight(1.);
        ReRoute.setStrategyName("ReRoute");
        ReRoute.setSubpopulation("person");
        config.strategy().addStrategySettings(ReRoute);

        StrategyConfigGroup.StrategySettings ReRouteFreight = new StrategyConfigGroup.StrategySettings();
        ReRouteFreight.setWeight(1.);
        ReRouteFreight.setStrategyName("ReRoute");
        ReRouteFreight.setSubpopulation("freight");
        config.strategy().addStrategySettings(ReRouteFreight);

        // drt
        PlanCalcScoreConfigGroup.ModeParams modeParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.drt);
        config.planCalcScore().addModeParams(modeParams);

        String drtVehiclesFile = rootPath + version + "/input/drt_vehicles.xml";
        Id<Link> startLink = Id.createLinkId("92611") ; // near S-Frohnau
        createDrtVehiclesFile(drtVehiclesFile, "DRT-", 10, startLink );

        DvrpConfigGroup dvrpConfig = ConfigUtils.addOrGetModule( config, DvrpConfigGroup.class );
        MultiModeDrtConfigGroup mm = ConfigUtils.addOrGetModule( config, MultiModeDrtConfigGroup.class );
        {
            DrtConfigGroup drtConfig = new DrtConfigGroup();
            drtConfig.setMaxTravelTimeAlpha( 1.3 );
            drtConfig.setVehiclesFile( drtVehiclesFile );
            drtConfig.setMaxTravelTimeBeta( 5. * 60. );
            drtConfig.setStopDuration( 60. );
            drtConfig.setMaxWaitTime( Double.MAX_VALUE );
            drtConfig.setRequestRejection( false );
            drtConfig.setMode( TransportMode.drt );
            mm.addParameterSet( drtConfig );
        }

        for( DrtConfigGroup drtConfigGroup : mm.getModalElements() ) {
            DrtConfigs.adjustDrtConfig(drtConfigGroup, config.planCalcScore());
        }

        // Raptor
        SwissRailRaptorConfigGroup raptor = setupRaptorConfigGroup();
        config.addModule(raptor);


        // -- S C E N A R I O --
        Scenario scenario = ScenarioUtils.loadScenario( config );
        scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory( DrtRoute.class, new DrtRouteFactory() ); // new

        // The following is for the _router_, not the qsim!  kai, jun'19
        VehiclesFactory vf = scenario.getVehicles().getFactory();
        {
            VehicleType vehType = vf.createVehicleType( Id.create( TransportMode.drt, VehicleType.class ) );
            vehType.setMaximumVelocity( 50./3.6 );
            scenario.getVehicles().addVehicleType( vehType );
        }
//        {
//            VehicleType vehType = vf.createVehicleType( Id.create( TransportMode.car, VehicleType.class ) );
//            vehType.setMaximumVelocity( 50./3.6 );
//            scenario.getVehicles().addVehicleType( vehType );
//        }

        // Placeholder: modify network to allow mode "drt" on links in Frohnau
//         e.g. PtAlongALineTest.addModeToAllLinksBtwnGivenNodes(scenario.getNetwork(), 0, 1000, TransportMode.drt );
//        TransitScheduleValidator.printResult( TransitScheduleValidator.validateAll( scenario.getTransitSchedule(), scenario.getNetwork() ) );


        // -- C O N T R O L E R --
        Controler controler = new Controler( scenario );
        controler.addOverridingModule(new SwissRailRaptorModule());
        controler.addOverridingModule( new DvrpModule() ); // new
        controler.addOverridingModule( new MultiModeDrtModule() );
        controler.configureQSimComponents( DvrpQSimComponents.activateModes( TransportMode.drt ) );

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

    private static SwissRailRaptorConfigGroup setupRaptorConfigGroup() {
        SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
        configRaptor.setUseIntermodalAccessEgress(true);

        // Walk
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalk = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalk.setMode(TransportMode.walk);
        paramSetWalk.setRadius(1);
        paramSetWalk.setPersonFilterAttribute(null);
        paramSetWalk.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalk );

        // drt
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetDrt = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetDrt.setMode( TransportMode.drt );
        paramSetDrt.setRadius( 1000000 );
        configRaptor.addIntermodalAccessEgress( paramSetDrt );
//        paramSetDrt.setStopFilterAttribute("drtAccessible");
//        paramSetDrt.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetDrt );

        return configRaptor;
    }

    private static Config SetupActivityParams(Config config) {
        // activities:
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams( "home_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams( "work_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            params.setOpeningTime(6. * 3600.);
            params.setClosingTime(20. * 3600.);
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams( "leisure_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            params.setOpeningTime(9. * 3600.);
            params.setClosingTime(27. * 3600.);
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams( "shopping_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            params.setOpeningTime(8. * 3600.);
            params.setClosingTime(20. * 3600.);
            config.planCalcScore().addActivityParams( params );
        }
        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            final PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams( "other_" + ii + ".0" ) ;
            params.setTypicalDuration( ii );
            config.planCalcScore().addActivityParams( params );
        }
        {
            final PlanCalcScoreConfigGroup.ActivityParams params = new PlanCalcScoreConfigGroup.ActivityParams( "freight" ) ;
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

    public static void createDrtVehiclesFile( String taxisFile, String vehPrefix, int numberofVehicles, Id<Link> startLinkId ) {
        List<DvrpVehicleSpecification> vehicles = new ArrayList<>();
        for (int i = 0; i< numberofVehicles;i++){
            //for multi-modal networks: Only links where drts can ride should be used.
            DvrpVehicleSpecification v = ImmutableDvrpVehicleSpecification.newBuilder()
                    .id(Id.create(vehPrefix + i, DvrpVehicle.class))
                    .startLinkId(startLinkId)
                    .capacity(4)
                    .serviceBeginTime(0)
                    .serviceEndTime(36*3600)
                    .build();
            vehicles.add(v);
        }
        new FleetWriter(vehicles.stream()).write(taxisFile);
    }

        private static void configureScoring(Config config) {
//        PlanCalcScoreConfigGroup.ModeParams nonNetworkWalk = new PlanCalcScoreConfigGroup.ModeParams( TransportMode.non_network_walk );
//        nonNetworkWalk.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(nonNetworkWalk);

//		ModeParams egressWalk = new ModeParams( TransportMode.egress_walk );
//		egressWalk.setMarginalUtilityOfTraveling(0);
//		config.planCalcScore().addModeParams(egressWalk);

        PlanCalcScoreConfigGroup.ModeParams transitWalk = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.transit_walk);
        transitWalk.setMarginalUtilityOfTraveling(0);
        config.planCalcScore().addModeParams(transitWalk);

        PlanCalcScoreConfigGroup.ModeParams bike = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.bike);
        bike.setMarginalUtilityOfTraveling(0);
        config.planCalcScore().addModeParams(bike);

        PlanCalcScoreConfigGroup.ModeParams drt = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.drt);
        drt.setMarginalUtilityOfTraveling(0);
        config.planCalcScore().addModeParams(drt);
    }
}
