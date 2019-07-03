//package main.run;
//
//import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
//import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.api.core.v01.TransportMode;
//import org.matsim.core.config.Config;
//import org.matsim.core.config.ConfigUtils;
//import org.matsim.core.config.ConfigWriter;
//import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
//import org.matsim.core.config.groups.QSimConfigGroup;
//import org.matsim.core.controler.Controler;
//import org.matsim.core.scenario.ScenarioUtils;
//import org.matsim.vis.otfvis.OTFVisConfigGroup;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Collection;
//
//
//public class RunBerlinBike {
//
//    public static void main(String[] args) {
//
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
////        Config config = createConfig(outputDir.toString(), rootPath);
//        Config config= ConfigUtils.loadConfig(rootPath.toString() + "/PolicyCase/2019-06-26/input/config_NEW_berlin.xml",
//                new SwissRailRaptorConfigGroup());
//        config.network().setInputFile(rootPath.toString() + "/PolicyCase/2019-06-26/input/berlin-v5-network.xml.gz");
//        config.controler().setLastIteration(0);
//
//        config.controler().setOutputDirectory(outputDir.toString());
//
//        ArrayList<String> modes = new ArrayList<>() ;
//        modes.add("car");
//        modes.add("freight");
//        config.plansCalcRoute().setNetworkModes(modes);
////        SwissRailRaptorConfigGroup configRaptor = createRaptorConfigGroup(1000000, 1000000);// (radius walk, radius bike)
////        config.addModule(configRaptor);
//
//        Scenario scenario = ScenarioUtils.createScenario(config) ;
//
//        Controler controler = new Controler(scenario);
//
////        controler.addOverridingModule(new SwissRailRaptorModule());
//
//        new ConfigWriter(config).write(rootPath.toString()+"/PolicyCase/2019-06-26/config_trial.xml");
//
//        // This will start otfvis.  Comment out if not needed.
////		controler.addOverridingModule( new OTFVisLiveModule() );
//
//        controler.run();
//    }
//
//    static SwissRailRaptorConfigGroup createRaptorConfigGroup(int radiusWalk, int radiusBike) {
//        SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
//        configRaptor.setUseIntermodalAccessEgress(true);
//
//        // Walk
//        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalk = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
//        paramSetWalk.setMode(TransportMode.walk);
//        paramSetWalk.setRadius(radiusWalk);
//        paramSetWalk.setPersonFilterAttribute(null);
//        paramSetWalk.setStopFilterAttribute(null);
//        configRaptor.addIntermodalAccessEgress(paramSetWalk );
//
//        // Bike
//        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetBike = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
//        paramSetBike.setMode(TransportMode.bike);
//        paramSetBike.setRadius(radiusBike);
//        paramSetBike.setPersonFilterAttribute(null);
//        paramSetBike.setStopFilterAttribute("bikeAccessible");
//        paramSetBike.setStopFilterValue("true");
//        configRaptor.addIntermodalAccessEgress(paramSetBike );
//
//        return configRaptor;
//    }
//
//    static Config createConfig(String outputDir, Path rootPath) {
////        Config config = ConfigUtils.createConfig();
//        Config config = ConfigUtils.loadConfig(rootPath.toString() + "/PolicyCase/2019-06-26/input/config_old.xml") ;
//
////        config.global().setNumberOfThreads(1);
//
//        config.controler().setOutputDirectory(outputDir);
//        config.controler().setLastIteration(0);
//
////        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.walk).setTeleportedModeSpeed(3.);
////        config.plansCalcRoute().getModeRoutingParams().get(TransportMode.bike).setTeleportedModeSpeed(10.);
//
////
////        ArrayList<String> modes = new ArrayList<>() ;
////        modes.add("car");
////        modes.add("freight");
////        config.plansCalcRoute().setNetworkModes(modes);
////
////        config.qsim().setEndTime(24. * 3600.);
////        config.qsim().setNumberOfThreads(1);
////
//////        config.transit().setUseTransit(true);
////
////        // This configures otfvis:
////        OTFVisConfigGroup visConfig = ConfigUtils.addOrGetModule(config, OTFVisConfigGroup.class);
////        visConfig.setDrawTransitFacilities(false);
////        visConfig.setColoringScheme(OTFVisConfigGroup.ColoringScheme.bvg);
////        visConfig.setDrawTime(true);
////        visConfig.setDrawNonMovingItems(true);
////        visConfig.setAgentSize(125);
////        visConfig.setLinkWidth(30);
////        visConfig.setShowTeleportedAgents(true);
//////		{
//////			BufferedImage image = null ;
//////			Rectangle2D zoomstore = new Rectangle2D.Double( 0., 0., +100.*1000., +10.*1000. ) ;
//////			ZoomEntry zoomEntry = new ZoomEntry( image, zoomstore, "*Initial*" ) ;
//////			visConfig.addZoom( zoomEntry );
//////		}
////
////        config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.kinematicWaves);
////        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);
////
//////        configureScoring(config);
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
//
//    }
//}