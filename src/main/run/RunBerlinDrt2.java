package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import com.google.common.collect.ImmutableSet;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.*;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

/** **************** THIS RUN CLASS IS NOT USED ************************************
 *  **************** USE RunBerlinZoomer INSTEAD ***********************************
 * A drt mode is added to the Berlin Scenario. In its current implementation, the drt fleet has a service area limited
 * to the neighborhood of Frohnau. This scenario can be extended to have multiple fleets, with seperate service areas.
 * TODO: Get access_walk and egress_walk out of the code. Deprecated
 * TODO: NetworkChangeEvents do not appear in events
 * TODO: "could not identify main mode"
 * TODO: "java.lang.ArrayIndexOutOfBoundsException: -2386092 -- nothing to do with AStar
 *
 * Changed: 1) Reroute weight changed in config 2) Astar on 3) time variant off 4) marg utility of drt to 1000
 * 5) max vel to 25
 *
 * Change Change: 1) Time dependent back on 2) max vel back to 1000
 */

public class RunBerlinDrt2 {

    enum DrtMode { none, teleportBeeline, teleportBasedOnNetworkRoute, full , TeleportedModeFreespeedFactor }
    private static DrtMode drtMode = DrtMode.full  ;
    private static boolean drt2 = false ;

    public static void main(String[] args) {
        String username = "jakob";
        String version = "2019-07-16/A-fullAttempt";
        String rootPath = null;

        switch (username) {
            case "jakob":
                rootPath = "C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/";
                break;
            case "david":
                rootPath = "D:/Eigene Dateien/Dokumente/Uni/tubCloud/Master/02_SoSe2019/MatSim/DRT/PolicyCase/";
                break;
            default:
                System.out.println("Incorrect Base Path");
        }


        // -- C O N F I G --

//        String configFileName = rootPath + "Input_global/berlin-v5.4-1pct.config.xml";
        String configFileName = rootPath + "Input_global/berlin-config-ReRoute.xml";
        Config config = ConfigUtils.loadConfig( configFileName);

        config.network().setInputFile("berlin-v5-network.xml.gz");
//        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz"); // full 1% population
        config.plans().setInputFile("berlin-downsample.xml"); // 1% of 1% population
//        config.plans().setInputFile("berlin-plans-Frohnau-scrubbed.xml"); // 1% population in Frohnau - scrubbed
        config.plans().setInputPersonAttributeFile("berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("berlin-v5.4-transit-vehicles.xml.gz");

        String FrohnauLinkPath = rootPath + "Input_global/linksWithinFrohnau.txt";
        String outputDirectory = rootPath + version + "/output/";

        // === GBL: ===

        new File(outputDirectory).mkdirs();

        config.controler().setLastIteration(30);
        config.global().setNumberOfThreads( 1 );
        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
//        config.controler().setRoutingAlgorithmType( FastAStarLandmarks );
        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn );
        config.controler().setWritePlansInterval(5);
        config.controler().setWriteEventsInterval(5);
        config.transit().setUseTransit(true) ;

//         Network Change Events
        config.network().setTimeVariantNetwork(true);
        config.network().setChangeEventsInputFile(rootPath + "Input_global/networkChangeEvents.xml");


        
        // === ROUTER: ===
        {
            config.plansCalcRoute().setInsertingAccessEgressWalk(true);
            config.plansCalcRoute().setRoutingRandomness(3.); //jr
            config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
            config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
            config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
            config.plansCalcRoute().removeModeRoutingParams("undefined");

            if (drtMode == DrtMode.teleportBeeline) {// (configure teleportation router)
                PlansCalcRouteConfigGroup.ModeRoutingParams drtParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
                drtParams.setMode(TransportMode.drt);
                drtParams.setTeleportedModeSpeed(1000. / 3.6); // CHANGED jr
                config.plansCalcRoute().addModeRoutingParams(drtParams);
                if (drt2) {
                    PlansCalcRouteConfigGroup.ModeRoutingParams drt2Params = new PlansCalcRouteConfigGroup.ModeRoutingParams();
                    drt2Params.setMode("drt2");
                    drt2Params.setTeleportedModeSpeed(1000. / 3.6);  // CHANGED jr
                    config.plansCalcRoute().addModeRoutingParams(drt2Params);
                }
                // teleportation router for walk or bike is automatically defined.
            }else if (drtMode == DrtMode.TeleportedModeFreespeedFactor) {// (configure teleportation router)
                PlansCalcRouteConfigGroup.ModeRoutingParams drtParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
                drtParams.setMode(TransportMode.drt);
                drtParams.setTeleportedModeFreespeedFactor(.1);
                config.plansCalcRoute().addModeRoutingParams(drtParams);
            // teleportation router for walk or bike is automatically defined.
        }
        else if (drtMode == DrtMode.teleportBasedOnNetworkRoute) {// (route as network route)
                Collection<String> networkModes =  new ArrayList<>(config.plansCalcRoute().getNetworkModes() );

                networkModes.add(TransportMode.drt);
                if (drt2) {
                    networkModes.add("drt2");
                }
                config.plansCalcRoute().setNetworkModes(networkModes);
            }

            // set up walk2 so we don't need walk in raptor:
            PlansCalcRouteConfigGroup.ModeRoutingParams walkParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
            walkParams.setMode("walk2");
            walkParams.setTeleportedModeSpeed(5. / 3.6);
            config.plansCalcRoute().addModeRoutingParams(walkParams);


//            // THIS SHOULDNT BE NECCESSARY - DEPRECATED
//            PlansCalcRouteConfigGroup.ModeRoutingParams accessParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
//            accessParams.setMode("access_walk");
//            accessParams.setTeleportedModeSpeed(5. / 3.6);
//            config.plansCalcRoute().addModeRoutingParams(accessParams);
        }
        // === RAPTOR: ===
        {
            SwissRailRaptorConfigGroup configRaptor = createRaptorConfigGroup();
            config.addModule(configRaptor);
        }

        // === SCORING: ===
        {
            double margUtlTravPt = config.planCalcScore().getModes().get(TransportMode.pt).getMarginalUtilityOfTraveling();
            if (drtMode != DrtMode.none) {
                // (scoring parameters for drt modes)
                PlanCalcScoreConfigGroup.ModeParams drtScoreParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.drt);
//                drtScoreParams.setMarginalUtilityOfTraveling(margUtlTravPt);
                drtScoreParams.setMarginalUtilityOfTraveling(1000.);
                config.planCalcScore().addModeParams(drtScoreParams);

                if (drt2) {
                    PlanCalcScoreConfigGroup.ModeParams drt2ScoreParams = new PlanCalcScoreConfigGroup.ModeParams("drt2");
                    drt2ScoreParams.setMarginalUtilityOfTraveling(margUtlTravPt);
                    config.planCalcScore().addModeParams(drt2ScoreParams);
                }
            }
            PlanCalcScoreConfigGroup.ModeParams walkScoreParams = new PlanCalcScoreConfigGroup.ModeParams("walk2");
            walkScoreParams.setMarginalUtilityOfTraveling(margUtlTravPt);
            config.planCalcScore().addModeParams(walkScoreParams);

            PlanCalcScoreConfigGroup.ModeParams NNWScoreParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.non_network_walk);
            NNWScoreParams.setMarginalUtilityOfTraveling(margUtlTravPt);
            config.planCalcScore().addModeParams(NNWScoreParams);

//// make everything really unattractive
//            config.planCalcScore().getModes().get(TransportMode.car).setMarginalUtilityOfTraveling(-10.);
//            config.planCalcScore().getModes().get(TransportMode.ride).setMarginalUtilityOfTraveling(-10.);
//            config.planCalcScore().getModes().get("bicycle").setMarginalUtilityOfTraveling(-10.);
//            config.planCalcScore().getModes().get(TransportMode.walk).setMarginalUtilityOfTraveling(-10.);
//            config.planCalcScore().getModes().get(TransportMode.transit_walk).setMarginalUtilityOfTraveling(-10.);
////            config.planCalcScore().getModes().get("access_walk").setMarginalUtilityOfTraveling(-10.);
////            config.planCalcScore().getModes().get("egress_walk").setMarginalUtilityOfTraveling(-10.);
//            config.planCalcScore().getModes().get("walk2").setMarginalUtilityOfTraveling(-10.);
//            config.planCalcScore().getModes().get(TransportMode.drt).setMarginalUtilityOfTraveling(0.); //except drt, of course
//
//
//
//            // drt interaction
//            PlanCalcScoreConfigGroup.ActivityParams drtParams = new PlanCalcScoreConfigGroup.ActivityParams("drt interaction");
//            drtParams.setTypicalDuration(1.);
//            config.planCalcScore().addActivityParams(drtParams);
//        configureScoring(config);

            config = RunBerlinZoomer.SetupActivityParams(config); //jr
        }
        // === QSIM: ===
        {
            config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.kinematicWaves);
            config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);
            config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);
            // (as of today, will also influence router. kai, jun'19)
            config.qsim().setSimStarttimeInterpretation(QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime);
            config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);
            config.qsim().setEndTime(24. * 3600.);
            config.qsim().setUsingTravelTimeCheckInTeleportation(true);
            config.qsim().setNumberOfThreads(1);


        }
        // === DRT: ===
        {
            if (drtMode == DrtMode.full) {
                // (configure full drt if applicable)

                String drtVehiclesFile = rootPath + version + "/drt_vehicles.xml";
                String drt2VehiclesFile = rootPath + version + "/drt2_vehicles.xml";

                DvrpConfigGroup dvrpConfig = ConfigUtils.addOrGetModule(config, DvrpConfigGroup.class);
                dvrpConfig.setNetworkModes(ImmutableSet.copyOf(Arrays.asList(TransportMode.drt, "drt2")));


                MultiModeDrtConfigGroup mm = ConfigUtils.addOrGetModule(config, MultiModeDrtConfigGroup.class);
                {
                    DrtConfigGroup drtConfig = new DrtConfigGroup();
                    drtConfig.setMaxTravelTimeAlpha(1.3);
                    drtConfig.setVehiclesFile(drtVehiclesFile);
                    drtConfig.setMaxTravelTimeBeta(5. * 60.);
                    drtConfig.setStopDuration(60.);
                    drtConfig.setMaxWaitTime(Double.MAX_VALUE);
                    drtConfig.setRequestRejection(false);
                    drtConfig.setMode(TransportMode.drt);
                    drtConfig.setUseModeFilteredSubnetwork(true); //jr
                    mm.addParameterSet(drtConfig);
                }
                if (drt2) {
                    DrtConfigGroup drtConfig = new DrtConfigGroup();
                    drtConfig.setMaxTravelTimeAlpha(1.3);
                    drtConfig.setVehiclesFile(drt2VehiclesFile);
                    drtConfig.setMaxTravelTimeBeta(5. * 60.);
                    drtConfig.setStopDuration(60.);
                    drtConfig.setMaxWaitTime(Double.MAX_VALUE);
                    drtConfig.setRequestRejection(false);
                    drtConfig.setMode("drt2");
                    drtConfig.setUseModeFilteredSubnetwork(true); //jr
                    mm.addParameterSet(drtConfig);
                }

                for (DrtConfigGroup drtConfigGroup : mm.getModalElements()) {
                    DrtConfigs.adjustDrtConfig(drtConfigGroup, config.planCalcScore());
                }

                Id<Link> startLink = Id.createLinkId("105323"); // near S-Frohnau
                createDrtVehiclesFile(drtVehiclesFile, "DRT-", 1000, startLink);
                if (drt2) {
                    createDrtVehiclesFile(drt2VehiclesFile, "DRT2-", 10, startLink);
                }

            }

        }
        // === OTFVIS: ===
        {
            OTFVisConfigGroup visConfig = ConfigUtils.addOrGetModule(config, OTFVisConfigGroup.class);
            visConfig.setDrawTransitFacilities(false);
            visConfig.setColoringScheme(OTFVisConfigGroup.ColoringScheme.bvg);
            visConfig.setDrawTime(true);
            visConfig.setDrawNonMovingItems(true);
            visConfig.setAgentSize(125);
            visConfig.setLinkWidth(30);
            visConfig.setShowTeleportedAgents(true);
            visConfig.setDrawTransitFacilities(true);

        }
        // ### SCENARIO: ###
        Scenario scenario = ScenarioUtils.loadScenario(config); //jr
        {
//        Scenario scenario = createScenario(config , 30 );


            if (drtMode == DrtMode.full) {
                scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DrtRoute.class, new DrtRouteFactory());
            }

            addModeToLinks(scenario.getNetwork(), FrohnauLinkPath, TransportMode.drt);
            if (drt2) {
                addModeToLinks(scenario.getNetwork(), FrohnauLinkPath, "drt2");
            }
            // TODO: reference somehow network creation, to ensure that these link ids exist


            // The following is also for the router! kai, jun'19
            VehiclesFactory vf = scenario.getVehicles().getFactory();
            if (drt2) {
                VehicleType vehType = vf.createVehicleType(Id.create("drt2", VehicleType.class));
                vehType.setMaximumVelocity(1000. / 3.6); //jr
                scenario.getVehicles().addVehicleType(vehType);
            }
            {
                VehicleType vehType = vf.createVehicleType(Id.create(TransportMode.drt, VehicleType.class));
                vehType.setMaximumVelocity(1000. / 3.6); //jr
                scenario.getVehicles().addVehicleType(vehType);
            }
//        {
//            // (does not work without; I don't really know why. kai)
//            VehicleType vehType = vf.createVehicleType( Id.create( TransportMode.car, VehicleType.class ) );
//            vehType.setMaximumVelocity( 25./3.6 );
//            scenario.getVehicles().addVehicleType( vehType );
//        }

            VehicleType vehType = vf.createVehicleType(Id.create(TransportMode.ride, VehicleType.class));
            vehType.setMaximumVelocity(25. / 3.6);
            scenario.getVehicles().addVehicleType(vehType);

//		scenario.getPopulation().getPersons().values().removeIf( person -> !person.getId().toString().equals( "3" ) );
        }
        // ### CONTROLER: ###
        {
            Controler controler = new Controler(scenario);

            controler.addOverridingModule(new SwissRailRaptorModule());

            if (drtMode == DrtMode.full) {
                controler.addOverridingModule(new DvrpModule());
                controler.addOverridingModule(new MultiModeDrtModule());
                if (drt2) {
                    controler.configureQSimComponents(DvrpQSimComponents.activateModes(TransportMode.drt, "drt2"));
                } else {
                    controler.configureQSimComponents(DvrpQSimComponents.activateModes(TransportMode.drt));
                }
            }


            // This will start otfvis.  Comment out if not needed.
//        controler.addOverridingModule( new OTFVisLiveModule() );

            // use the (congested) car travel time for the teleported ride mode
            controler.addOverridingModule(new AbstractModule() {
                @Override
                public void install() {
                    addTravelTimeBinding(TransportMode.ride).to(networkTravelTime());
                    addTravelDisutilityFactoryBinding(TransportMode.ride).to(carTravelDisutilityFactoryKey());
                }
            });


//            new ConfigWriter(config).write(rootPath + version + "/configWithDrtTest.xml");
//        new NetworkWriter(scenario.getNetwork()).write(rootPath + version + "/networkWithDrt.xml");
            controler.run();
        }
    }


//    private static void configureScoring(Config config) {
//        PlanCalcScoreConfigGroup.ModeParams accessWalk = new PlanCalcScoreConfigGroup.ModeParams( TransportMode.non_network_walk );
//        accessWalk.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(accessWalk);
//
//        PlanCalcScoreConfigGroup.ModeParams transitWalk = new PlanCalcScoreConfigGroup.ModeParams("transit_walk");
//        transitWalk.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(transitWalk);
//
//        PlanCalcScoreConfigGroup.ModeParams bike = new PlanCalcScoreConfigGroup.ModeParams("bike");
//        bike.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(bike);
//
//        PlanCalcScoreConfigGroup.ModeParams drt = new PlanCalcScoreConfigGroup.ModeParams("drt");
//        drt.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(drt);
//    }

    static SwissRailRaptorConfigGroup createRaptorConfigGroup() {
        SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
        configRaptor.setUseIntermodalAccessEgress(true);

        if ( drtMode!= DrtMode.none){
            configRaptor.setUseIntermodalAccessEgress(true);
            {
                // Xxx
                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetXxx = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                //					paramSetXxx.setMode( TransportMode.walk ); // this does not work because sbb raptor treats it in a special way
                paramSetXxx.setMode( "walk2" );
                paramSetXxx.setRadius( 10000 );
                configRaptor.addIntermodalAccessEgress( paramSetXxx );
                // (in principle, walk as alternative to drt will not work, since drt is always faster.  Need to give the ASC to the router!  However, with
                // the reduced drt network we should be able to see differentiation.)
            }
            {
                // drt
                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetDrt = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                paramSetDrt.setMode( TransportMode.drt );
                paramSetDrt.setRadius( 1000000 );
                configRaptor.addIntermodalAccessEgress( paramSetDrt );
            }
            if ( drt2 ){
                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetDrt2 = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                paramSetDrt2.setMode( "drt2" );
                paramSetDrt2.setRadius( 10000 );
                //				paramSetDrt2.setPersonFilterAttribute( null );
                //				paramSetDrt2.setStopFilterAttribute( null );
                configRaptor.addIntermodalAccessEgress( paramSetDrt2 );
            }

            { // from zoomer
                // Walk
                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalk = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                paramSetWalk.setMode(TransportMode.walk);
                paramSetWalk.setRadius(10000);
                paramSetWalk.setPersonFilterAttribute(null);
                paramSetWalk.setStopFilterAttribute(null);
                configRaptor.addIntermodalAccessEgress(paramSetWalk );

                // Walk
//                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkNN = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
//                paramSetWalkNN.setMode(TransportMode.non_network_walk);
//                paramSetWalkNN.setRadius(1);
//                paramSetWalkNN.setPersonFilterAttribute(null);
//                paramSetWalkNN.setStopFilterAttribute(null);
//                configRaptor.addIntermodalAccessEgress(paramSetWalkNN );

                // Access Walk
                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkA = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                paramSetWalkA.setMode("access_walk");
                paramSetWalkA.setRadius(10000);
                paramSetWalkA.setPersonFilterAttribute(null);
                paramSetWalkA.setStopFilterAttribute(null);
                configRaptor.addIntermodalAccessEgress(paramSetWalkA );

                // Egress Walk
                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkE = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                paramSetWalkE.setMode("egress_walk");
                paramSetWalkE.setRadius(10000);
                paramSetWalkE.setPersonFilterAttribute(null);
                paramSetWalkE.setStopFilterAttribute(null);
                configRaptor.addIntermodalAccessEgress(paramSetWalkE );

                // Transit Walk
                SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkNN = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                paramSetWalkNN.setMode(TransportMode.transit_walk);
                paramSetWalkNN.setRadius(10000);
                paramSetWalkNN.setPersonFilterAttribute(null);
                paramSetWalkNN.setStopFilterAttribute(null);
                configRaptor.addIntermodalAccessEgress(paramSetWalkNN );
            }
        }

        return configRaptor;
    }


    static void createDrtVehiclesFile( String taxisFile, String vehPrefix, int numberofVehicles, Id<Link> startLinkId ) {
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

    static void addModeToLinks(Network network, String FrohnauLinksPath,  String drtMode ) {

        ArrayList<String> links = readLinksFile(FrohnauLinksPath) ;
        for (String linkId : links) {
            Set<String> newAllowedModes = new HashSet<>( network.getLinks().get( Id.createLinkId( linkId ) ).getAllowedModes() );
            newAllowedModes.add(drtMode);
            network.getLinks().get(Id.createLinkId(Id.createLinkId( linkId ) )).setAllowedModes( newAllowedModes );
        }
    }

    public static ArrayList<String> readLinksFile(String fileName){
        Scanner s ;
        ArrayList<String> list = new ArrayList<String>();
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
