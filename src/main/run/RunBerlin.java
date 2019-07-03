//package main.run;
//
////import com.sun.java.util.jar.pack.Instruction;
//import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
//import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
//import org.matsim.api.core.v01.Id;
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.api.core.v01.TransportMode;
//import org.matsim.api.core.v01.network.Link;
//import org.matsim.contrib.drt.run.DrtConfigGroup;
//import org.matsim.contrib.drt.run.DrtConfigs;
//import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
//
//import org.matsim.contrib.drt.routing.DrtRoute;
//import org.matsim.contrib.drt.routing.DrtRouteFactory;
//import org.matsim.contrib.drt.run.*;
//import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
//import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
//import org.matsim.contrib.dvrp.fleet.FleetWriter;
//import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
//import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
//import org.matsim.contrib.dvrp.run.DvrpModule;
//import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
//import org.matsim.core.config.Config;
//import org.matsim.core.config.ConfigUtils;
//import org.matsim.core.config.ConfigWriter;
//import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
//import org.matsim.core.config.groups.QSimConfigGroup;
//import org.matsim.core.config.groups.VspExperimentalConfigGroup;
//import org.matsim.core.controler.Controler;
//import org.matsim.core.scenario.ScenarioUtils;
//import org.matsim.pt.utils.TransitScheduleValidator;
//import org.matsim.vehicles.VehicleType;
//import org.matsim.vehicles.VehiclesFactory;
//import org.matsim.vis.otfvis.OTFVisConfigGroup;
//import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
//import org.matsim.contrib.drt.run.MultiModeDrtModule;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class RunBerlin {
//
//
//
//    public static void main(String[] args) {
//        String username = "jakob";
//        Path rootPath = null;
//
//        switch (username) {
//            case "jakob":
//                rootPath = Paths.get("C:/Users/jakob/tubCloud/Shared/DRT");
//                break;
//            case "david":
//                rootPath = Paths.get("C:/Users/david/ENTER_PATH_HERE");
//                break;
//            default:
//                System.out.println("Incorrect Base Path");
//        }
//
//        // Config
//        Path outputDir = Paths.get(rootPath.toString() + "/PolicyCase/2019-06-26/output");
//        Config config = createConfig(outputDir.toString(), rootPath);
//
//        config.network().setInputFile(rootPath.toString() + "/PolicyCase/2019-06-26/input/berlin-v5.3-1pct.output_network.xml.gz");
//        // should be be using output plans?
//        config.plans().setInputFile(rootPath.toString() + "/PolicyCase/2019-06-26/input/berlin-v5.3-1pct.output_plans.xml.gz");
//        config.plans().setInputPersonAttributeFile(rootPath.toString() + "/PolicyCase/2019-06-26/input/berlin-v5-person-attributes.xml.gz") ;
//        config.plans().setRemovingUnneccessaryPlanAttributes(true);
//
//
//        // Placeholders, need to be filled!
//        config.transit().setVehiclesFile(rootPath.toString() + "/PolicyCase/2019-06-26/input/berlin-v5-transit-vehicles.xml.gz");
//        config.transit().setTransitScheduleFile(rootPath.toString() +"/PolicyCase/2019-06-26/input/berlin-v5-transit-schedule.xml");
//        config.vehicles().setVehiclesFile(rootPath.toString() + "/PolicyCase/2019-06-26/input/berlin-v5-mode-vehicle-types.xml");
//
//        config.qsim().setSimStarttimeInterpretation( QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime );
//        config.plansCalcRoute().setInsertingAccessEgressWalk( true );
//
//
//        PlanCalcScoreConfigGroup.ModeParams modeParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.drt);
//        config.planCalcScore().addModeParams(modeParams);
//
////        config.qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );
//        config.controler().setLastIteration( 0 );
//
//        // Raptor
//        SwissRailRaptorConfigGroup configRaptor = ConfigUtils.addOrGetModule( config, SwissRailRaptorConfigGroup.class ) ;
//        configRaptor.setUseIntermodalAccessEgress(true);
//
////        // drt
//        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetDrt = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
//        paramSetDrt.setMode( TransportMode.drt );
//        paramSetDrt.setRadius( 1000000 );
//        configRaptor.addIntermodalAccessEgress( paramSetDrt );
//
//        String drtVehiclesFile = rootPath.toString() + "/PolicyCase/2019-06-26/input/drt_vehicles.xml";
//        Id<Link> startLink = Id.createLinkId("92611") ; // near S-Frohnau
//        createDrtVehiclesFile(drtVehiclesFile, "DRT-", 10, startLink );
//
//        DvrpConfigGroup dvrpConfig = ConfigUtils.addOrGetModule( config, DvrpConfigGroup.class );
//        MultiModeDrtConfigGroup mm = ConfigUtils.addOrGetModule( config, MultiModeDrtConfigGroup.class );
//        {
//            DrtConfigGroup drtConfig = new DrtConfigGroup();
//            drtConfig.setMaxTravelTimeAlpha( 1.3 );
//            drtConfig.setVehiclesFile( drtVehiclesFile );
//            drtConfig.setMaxTravelTimeBeta( 5. * 60. );
//            drtConfig.setStopDuration( 60. );
//            drtConfig.setMaxWaitTime( Double.MAX_VALUE );
//            drtConfig.setRequestRejection( false );
//            drtConfig.setMode( TransportMode.drt );
//            mm.addParameterSet( drtConfig );
//        }
//
//        for( DrtConfigGroup drtConfigGroup : mm.getModalElements() ) {
//            DrtConfigs.adjustDrtConfig(drtConfigGroup, config.planCalcScore());
//        }
//
//        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn );
//
//
//        // Scenario
//        Scenario scenario = ScenarioUtils.createScenario(config) ;
//
//        // Placeholder: modify network to allow mode "drt" on links in Frohnau
//        // e.g. PtAlongALineTest.addModeToAllLinksBtwnGivenNodes(scenario.getNetwork(), 0, 1000, TransportMode.drt );
//
////        TransitScheduleValidator.printResult( TransitScheduleValidator.validateAll( scenario.getTransitSchedule(), scenario.getNetwork() ) );
//
//        scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory( DrtRoute.class, new DrtRouteFactory() );
//
//
//
//        // The following is for the _router_, not the qsim!  kai, jun'19
//        VehiclesFactory vf = scenario.getVehicles().getFactory();
//        {
//            VehicleType vehType = vf.createVehicleType( Id.create( TransportMode.drt, VehicleType.class ) );
//            vehType.setMaximumVelocity( 50./3.6 );
//            scenario.getVehicles().addVehicleType( vehType );
//        }
////        {
////            VehicleType vehType = vf.createVehicleType( Id.create( TransportMode.car, VehicleType.class ) );
////            vehType.setMaximumVelocity( 50./3.6 );
////            scenario.getVehicles().addVehicleType( vehType );
////        }
//
//
//        Controler controler = new Controler(scenario);
//
//        controler.addOverridingModule(new SwissRailRaptorModule() ) ;
////
//        controler.addOverridingModule( new DvrpModule() );
//        controler.addOverridingModule( new MultiModeDrtModule() );
//        controler.configureQSimComponents( DvrpQSimComponents.activateModes( TransportMode.drt ) );
//
//        // This will start otfvis.  Comment out if not needed.
////		controler.addOverridingModule( new OTFVisLiveModule() );
//
//        controler.run();
//
//
//    }
//
//    static Config createConfig(String outputDir, Path rootPath) {
////        Config config = ConfigUtils.createConfig();
//        Config config = ConfigUtils.loadConfig(rootPath.toString() + "/PolicyCase/2019-06-26/input/config_old.xml") ;
//
//        config.global().setNumberOfThreads(1);
//
//        config.controler().setOutputDirectory(outputDir);
//        config.controler().setLastIteration(0);
//
//        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.walk).setTeleportedModeSpeed(3.);
////        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.bike).setTeleportedModeSpeed(10.);
//
//        config.qsim().setEndTime(24. * 3600.);
//        config.qsim().setNumberOfThreads(1);
//
//        config.transit().setUseTransit(true);
//
//        // This configures otfvis:
//        OTFVisConfigGroup visConfig = ConfigUtils.addOrGetModule(config, OTFVisConfigGroup.class);
//        visConfig.setDrawTransitFacilities(false);
//        visConfig.setColoringScheme(OTFVisConfigGroup.ColoringScheme.bvg);
//        visConfig.setDrawTime(true);
//        visConfig.setDrawNonMovingItems(true);
//        visConfig.setAgentSize(125);
//        visConfig.setLinkWidth(30);
//        visConfig.setShowTeleportedAgents(true);
////		{
////			BufferedImage image = null ;
////			Rectangle2D zoomstore = new Rectangle2D.Double( 0., 0., +100.*1000., +10.*1000. ) ;
////			ZoomEntry zoomEntry = new ZoomEntry( image, zoomstore, "*Initial*" ) ;
////			visConfig.addZoom( zoomEntry );
////		}
//
//        config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.kinematicWaves);
//        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);
//
//        configureScoring(config);
//        return config;
//    }
//    private static void configureScoring(Config config) {
////        PlanCalcScoreConfigGroup.ModeParams nonNetworkWalk = new PlanCalcScoreConfigGroup.ModeParams( TransportMode.non_network_walk );
////        nonNetworkWalk.setMarginalUtilityOfTraveling(0);
////        config.planCalcScore().addModeParams(nonNetworkWalk);
//
////		ModeParams egressWalk = new ModeParams( TransportMode.egress_walk );
////		egressWalk.setMarginalUtilityOfTraveling(0);
////		config.planCalcScore().addModeParams(egressWalk);
//
//        PlanCalcScoreConfigGroup.ModeParams transitWalk = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.transit_walk);
//        transitWalk.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(transitWalk);
//
//        PlanCalcScoreConfigGroup.ModeParams bike = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.bike);
//        bike.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(bike);
//
//        PlanCalcScoreConfigGroup.ModeParams drt = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.drt);
//        drt.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(drt);
//    }
//
//    static void createDrtVehiclesFile( String taxisFile, String vehPrefix, int numberofVehicles, Id<Link> startLinkId ) {
//        List<DvrpVehicleSpecification> vehicles = new ArrayList<>();
//        for (int i = 0; i< numberofVehicles;i++){
//            //for multi-modal networks: Only links where drts can ride should be used.
//            DvrpVehicleSpecification v = ImmutableDvrpVehicleSpecification.newBuilder()
//                    .id(Id.create(vehPrefix + i, DvrpVehicle.class))
//                    .startLinkId(startLinkId)
//                    .capacity(4)
//                    .serviceBeginTime(0)
//                    .serviceEndTime(36*3600)
//                    .build();
//            vehicles.add(v);
//        }
//        new FleetWriter(vehicles.stream()).write(taxisFile);
//    }
//
//}