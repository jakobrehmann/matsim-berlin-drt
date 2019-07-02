//package main.config;
//
//import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
//import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet;
//import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
//import org.junit.Ignore;
//import org.junit.Rule;
//import org.junit.Test;
//import org.matsim.api.core.v01.Coord;
//import org.matsim.api.core.v01.Id;
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.api.core.v01.TransportMode;
//import org.matsim.api.core.v01.network.Link;
//import org.matsim.api.core.v01.network.Network;
//import org.matsim.api.core.v01.network.NetworkFactory;
//import org.matsim.api.core.v01.network.Node;
//import org.matsim.api.core.v01.population.*;
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
//import org.matsim.contrib.otfvis.OTFVisLiveModule;
//import org.matsim.core.config.Config;
//import org.matsim.core.config.ConfigUtils;
//import org.matsim.core.config.groups.*;
//import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
//import org.matsim.core.controler.Controler;
//import org.matsim.core.controler.OutputDirectoryHierarchy;
//import org.matsim.core.gbl.MatsimRandom;
//import org.matsim.core.population.routes.NetworkRoute;
//import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
//import org.matsim.core.scenario.ScenarioUtils;
//import org.matsim.core.utils.geometry.CoordUtils;
//import org.matsim.facilities.ActivityFacilitiesFactory;
//import org.matsim.facilities.ActivityFacility;
//import org.matsim.facilities.ActivityOption;
//import org.matsim.pt.transitSchedule.api.*;
//import org.matsim.pt.utils.TransitScheduleValidator;
//import org.matsim.testcases.MatsimTestUtils;
//import org.matsim.vehicles.VehicleCapacity;
//import org.matsim.vehicles.VehicleType;
//import org.matsim.vehicles.VehiclesFactory;
//import org.matsim.vis.otfvis.OTFVisConfigGroup;
//
//import java.awt.geom.Rectangle2D;
//import java.awt.image.BufferedImage;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.*;
//
//public class RunBerlinDrt {
//    private static final Id<Link> TR_LINK_0_1_ID = Id.createLinkId( "trLink0-1" );
//    private static final Id<Link> TR_LONG_LINK_LEFT_ID = Id.createLinkId( "trLinkLongLeft" );
//    private static final Id<Link> TR_LINK_MIDDLE_ID = Id.createLinkId( "trLinkMiddle" );
//    private static final Id<Link> TR_LONG_LINK_RIGHT_ID = Id.createLinkId( "trLinkLongRight" );
//    private static final Id<Link> TR_LINK_LASTM1_LAST_ID = Id.createLinkId( "trLinkLastm1-Last" );
//
//    private static final Id<TransitStopFacility> tr_stop_fac_0_ID = Id.create( "StopFac0", TransitStopFacility.class );
//    private static final Id<TransitStopFacility> tr_stop_fac_10000_ID = Id.create( "StopFac10000", TransitStopFacility.class );
//    private static final Id<TransitStopFacility> tr_stop_fac_5000_ID = Id.create( "StopFac5000", TransitStopFacility.class );
//
//    private static final Id<VehicleType> busTypeID = Id.create( "bus", VehicleType.class );
//
//        public static void main(String[] args) {
//
//        Config config = createConfig( utils.getOutputDirectory() );
//
//        SwissRailRaptorConfigGroup configRaptor = createRaptorConfigGroup(1000000, 1000000);// (radius walk, radius bike)
//        config.addModule(configRaptor);
//
//        Scenario scenario = createScenario( config, 1000 );
//
//        Controler controler = new Controler( scenario ) ;
//
//        controler.addOverridingModule(new SwissRailRaptorModule()) ;
//
//        // This will start otfvis.  Comment out if not needed.
////		controler.addOverridingModule( new OTFVisLiveModule() );
//
//        controler.run() ;
//    }
//
//
//
//    static Config createConfig( String outputDir ){
//        Config config = ConfigUtils.createConfig() ;
//
//        config.global().setNumberOfThreads( 1 );
//
//        config.controler().setOutputDirectory( outputDir ) ;
//        config.controler().setLastIteration( 0 );
//
//        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.walk ).setTeleportedModeSpeed( 3. );
//        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.bike ).setTeleportedModeSpeed( 10. );
//
//        config.qsim().setEndTime( 24.*3600. );
//
//        config.transit().setUseTransit(true) ;
//
//        // This configures otfvis:
//        OTFVisConfigGroup visConfig = ConfigUtils.addOrGetModule( config, OTFVisConfigGroup.class );
//        visConfig.setDrawTransitFacilities( false );
//        visConfig.setColoringScheme( OTFVisConfigGroup.ColoringScheme.bvg ) ;
//        visConfig.setDrawTime(true);
//        visConfig.setDrawNonMovingItems(true);
//        visConfig.setAgentSize(125);
//        visConfig.setLinkWidth(30);
//        visConfig.setShowTeleportedAgents( true );
////		{
////			BufferedImage image = null ;
////			Rectangle2D zoomstore = new Rectangle2D.Double( 0., 0., +100.*1000., +10.*1000. ) ;
////			ZoomEntry zoomEntry = new ZoomEntry( image, zoomstore, "*Initial*" ) ;
////			visConfig.addZoom( zoomEntry );
////		}
//
//        config.qsim().setSnapshotStyle( QSimConfigGroup.SnapshotStyle.kinematicWaves );
//        config.qsim().setTrafficDynamics( QSimConfigGroup.TrafficDynamics.kinematicWaves );
//
//        configureScoring(config);
//        return config;
//    }
//
//    static Scenario createScenario( Config config, long numberOfPersons ){
//        Scenario scenario = ScenarioUtils.createScenario( config );
//        // don't load anything
//
//        final int lastNodeIdx = 1000;
//        final double deltaX = 100.;
//
//        createAndAddCarNetwork( scenario, lastNodeIdx, deltaX );
//
//        createAndAddPopulation( scenario , "pt", numberOfPersons );
//
//        final double deltaY = 1000.;
//
//        createAndAddTransitNetwork( scenario, lastNodeIdx, deltaX, deltaY );
//
//        createAndAddTransitStopFacilities( scenario, lastNodeIdx, deltaX, deltaY );
//
//        createAndAddTransitVehicleType( scenario );
//
//        createAndAddTransitLine( scenario );
//
//        TransitScheduleValidator.printResult( TransitScheduleValidator.validateAll( scenario.getTransitSchedule(), scenario.getNetwork() ) );
//        return scenario;
//    }
//
//    private static void configureScoring(Config config) {
//        ModeParams nonNetworkWalk = new ModeParams( TransportMode.non_network_walk );
//        nonNetworkWalk.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(nonNetworkWalk);
//
////		ModeParams egressWalk = new ModeParams( TransportMode.egress_walk );
////		egressWalk.setMarginalUtilityOfTraveling(0);
////		config.planCalcScore().addModeParams(egressWalk);
//
//        ModeParams transitWalk = new ModeParams(TransportMode.transit_walk);
//        transitWalk.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(transitWalk);
//
//        ModeParams bike = new ModeParams(TransportMode.bike);
//        bike.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(bike);
//
//        ModeParams drt = new ModeParams(TransportMode.drt);
//        drt.setMarginalUtilityOfTraveling(0);
//        config.planCalcScore().addModeParams(drt);
//    }
//
//    static SwissRailRaptorConfigGroup createRaptorConfigGroup(int radiusWalk, int radiusBike) {
//        SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
//        configRaptor.setUseIntermodalAccessEgress(true);
//
//        // Walk
//        IntermodalAccessEgressParameterSet paramSetWalk = new IntermodalAccessEgressParameterSet();
//        paramSetWalk.setMode(TransportMode.walk);
//        paramSetWalk.setRadius(radiusWalk);
//        paramSetWalk.setPersonFilterAttribute(null);
//        paramSetWalk.setStopFilterAttribute(null);
//        configRaptor.addIntermodalAccessEgress(paramSetWalk );
//
//        // Bike
//        IntermodalAccessEgressParameterSet paramSetBike = new IntermodalAccessEgressParameterSet();
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
//    private static void createAndAddTransitLine( Scenario scenario ){
//        PopulationFactory pf = scenario.getPopulation().getFactory();
//        TransitSchedule schedule = scenario.getTransitSchedule();
//        TransitScheduleFactory tsf = schedule.getFactory();
//        VehiclesFactory tvf = scenario.getTransitVehicles().getFactory();
//
//        List<Id<Link>> linkIds = new ArrayList<>() ;
//        linkIds.add( TR_LONG_LINK_LEFT_ID ) ;
//        linkIds.add( TR_LINK_MIDDLE_ID ) ;
//        linkIds.add( TR_LONG_LINK_RIGHT_ID ) ;
//        NetworkRoute route = createNetworkRoute( TR_LINK_0_1_ID, linkIds, TR_LINK_LASTM1_LAST_ID, pf );
//
//        List<TransitRouteStop> stops = new ArrayList<>() ;
//        {
//            stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( tr_stop_fac_0_ID ), 0., 0. ) );
//            stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( tr_stop_fac_5000_ID ), 1., 1. ) );
//            stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( tr_stop_fac_10000_ID ), 1., 1. ) );
//        }
//        {
//            TransitRoute transitRoute = tsf.createTransitRoute( Id.create( "route1", TransitRoute.class ), route, stops, "bus" );
//            for ( int ii=0 ; ii<100 ; ii++ ){
//                String str = "tr_" + ii ;
//
//                scenario.getTransitVehicles().addVehicle( tvf.createVehicle( Id.createVehicleId( str ), scenario.getTransitVehicles().getVehicleTypes().get( busTypeID) ) );
//
//                Departure departure = tsf.createDeparture( Id.create( str, Departure.class ), 7. * 3600. + ii*300 ) ;
//                departure.setVehicleId( Id.createVehicleId( str ) );
//                transitRoute.addDeparture( departure );
//            }
//            TransitLine line = tsf.createTransitLine( Id.create( "line1", TransitLine.class ) );
//            line.addRoute( transitRoute );
//
//            schedule.addTransitLine( line );
//        }
//    }
//
//    private static void createAndAddTransitVehicleType( Scenario scenario ){
//        VehiclesFactory tvf = scenario.getTransitVehicles().getFactory();
//        VehicleType busType = tvf.createVehicleType( busTypeID );
//        {
//            VehicleCapacity capacity = tvf.createVehicleCapacity();
//            capacity.setSeats( 100 );
//            busType.setCapacity( capacity );
//        }
//        {
//            busType.setMaximumVelocity( 100. / 3.6 );
//        }
//        scenario.getTransitVehicles().addVehicleType( busType );
//    }
//
//    private static void createAndAddTransitStopFacilities( Scenario scenario, int lastNodeIdx, double deltaX, double deltaY ){
//        TransitSchedule schedule = scenario.getTransitSchedule();
//        TransitScheduleFactory tsf = schedule.getFactory();
//
//        TransitStopFacility stopFacility0 = tsf.createTransitStopFacility( tr_stop_fac_0_ID, new Coord( deltaX, deltaY ), false );
//        stopFacility0.setLinkId( TR_LINK_0_1_ID );
//        schedule.addStopFacility( stopFacility0 );
//
//        TransitStopFacility stopFacility5000 = tsf.createTransitStopFacility( tr_stop_fac_5000_ID, new Coord( 0.5 * (lastNodeIdx - 1) * deltaX, deltaY ), false );
//        stopFacility5000.setLinkId( TR_LINK_MIDDLE_ID );
//
//        stopFacility5000.getAttributes().putAttribute( "bikeAccessible", "true");
//        stopFacility5000.getAttributes().putAttribute( "drtAccessible", "true" );
//
//        schedule.addStopFacility( stopFacility5000 );
//
//        TransitStopFacility stopFacility10000 = tsf.createTransitStopFacility( tr_stop_fac_10000_ID, new Coord( (lastNodeIdx - 1) * deltaX, deltaY ), false );
//        stopFacility10000.setLinkId( TR_LINK_LASTM1_LAST_ID );
//        schedule.addStopFacility( stopFacility10000 );
//
//
////		stopFacility0.getAttributes().putAttribute( "drtAccessible", "true" );
////		stopFacility10000.getAttributes().putAttribute( "drtAccessible", "true" );
//    }
//
//    private static void createAndAddTransitNetwork( Scenario scenario, int lastNodeIdx, double deltaX, double deltaY ){
//        NetworkFactory nf = scenario.getNetwork().getFactory();
//
//        Node node0 = nf.createNode( Id.createNodeId("trNode0" ), new Coord(0, deltaY ) );
//        scenario.getNetwork().addNode(node0);
//        // ---
//        Node node1 = nf.createNode( Id.createNodeId("trNode1"), new Coord(deltaX, deltaY ) ) ;
//        scenario.getNetwork().addNode( node1 ) ;
//        createAndAddTransitLink( scenario, node0, node1, TR_LINK_0_1_ID );
//        // ---
//        Node nodeMiddleLeft = nf.createNode( Id.createNodeId("trNodeMiddleLeft") , new Coord( 0.5*(lastNodeIdx-1)*deltaX , deltaY ) ) ;
//        scenario.getNetwork().addNode( nodeMiddleLeft) ;
//        {
//            createAndAddTransitLink( scenario, node1, nodeMiddleLeft, TR_LONG_LINK_LEFT_ID );
//        }
//        // ---
//        Node nodeMiddleRight = nf.createNode( Id.createNodeId("trNodeMiddleRight") , new Coord( 0.5*(lastNodeIdx+1)*deltaX , deltaY ) ) ;
//        scenario.getNetwork().addNode( nodeMiddleRight ) ;
//        {
//            createAndAddTransitLink( scenario, nodeMiddleLeft, nodeMiddleRight, TR_LINK_MIDDLE_ID );
//        }
//        // ---
//        Node nodeLastm1 = nf.createNode( Id.createNodeId("trNodeLastm1") , new Coord( (lastNodeIdx-1)*deltaX , deltaY ) ) ;
//        scenario.getNetwork().addNode( nodeLastm1) ;
//        {
//            createAndAddTransitLink( scenario, nodeMiddleRight, nodeLastm1, TR_LONG_LINK_RIGHT_ID );
//        }
//
//        // ---
//        Node nodeLast = nf.createNode(Id.createNodeId("trNodeLast"), new Coord(lastNodeIdx*deltaX, deltaY ) ) ;
//        scenario.getNetwork().addNode(nodeLast);
//        {
//            createAndAddTransitLink( scenario, nodeLastm1, nodeLast, TR_LINK_LASTM1_LAST_ID );
//        }
//    }
//
//    private static void createAndAddPopulation( Scenario scenario, String mode, long numberOfPersons ){
//        PopulationFactory pf = scenario.getPopulation().getFactory();
//        List<ActivityFacility> facilitiesAsList = new ArrayList<>( scenario.getActivityFacilities().getFacilities().values() ) ;
//        final Id<ActivityFacility> activityFacilityId = facilitiesAsList.get( facilitiesAsList.size()-1 ).getId() ;
//        for( int jj = 0 ; jj < numberOfPersons ; jj++ ){
//            Person person = pf.createPerson( Id.createPersonId( jj ) );
//            {
//                scenario.getPopulation().addPerson( person );
//                Plan plan = pf.createPlan();
//                person.addPlan( plan );
//
//                // --- 1st location at randomly selected facility:
//                int idx = MatsimRandom.getRandom().nextInt( facilitiesAsList.size() );
//                Id<ActivityFacility> homeFacilityId = facilitiesAsList.get( idx ).getId();
//                Activity home = pf.createActivityFromActivityFacilityId( "dummy", homeFacilityId );
//                if ( jj==0 ){
//                    home.setEndTime( 7. * 3600. ); // one agent one sec earlier so that for all others the initial acts are visible in VIA
//                } else {
//                    home.setEndTime( 7. * 3600. + 1. );
//                }
//                plan.addActivity( home );
//                {
//                    Leg leg = pf.createLeg( mode );
//                    leg.setDepartureTime( 7. * 3600. );
//                    leg.setTravelTime( 1800. );
//                    plan.addLeg( leg );
//                }
//                {
//                    Activity shop = pf.createActivityFromActivityFacilityId( "dummy", activityFacilityId );
//                    plan.addActivity( shop );
//                }
//            }
//        }
//    }
//
//    private static void createAndAddCarNetwork( Scenario scenario, int lastNodeIdx, double deltaX ){
//        // Construct a network and facilities along a line:
//        // 0 --(0-1)-- 1 --(2-1)-- 2 -- ...
//        // with a facility of same ID attached to each link.
//
//        NetworkFactory nf = scenario.getNetwork().getFactory();
//        ActivityFacilitiesFactory ff = scenario.getActivityFacilities().getFactory();
//
//        Node prevNode;
//        {
//            Node node = nf.createNode( Id.createNodeId( 0 ), new Coord( 0., 0. ) );
//            scenario.getNetwork().addNode( node );
//            prevNode = node;
//        }
//        for( int ii = 1 ; ii <= lastNodeIdx ; ii++ ){
//            Node node = nf.createNode( Id.createNodeId( ii ), new Coord( ii * deltaX, 0. ) );
//            scenario.getNetwork().addNode( node );
//            // ---
//            addLinkAndFacility( scenario, nf, ff, prevNode, node );
//            addLinkAndFacility( scenario, nf, ff, node, prevNode );
//            // ---
//            prevNode = node;
//        }
//    }
//
//    private static void createAndAddTransitLink( Scenario scenario, Node node0, Node node1, Id<Link> TR_LINK_0_1_ID ){
//        Link trLink = scenario.getNetwork().getFactory().createLink( TR_LINK_0_1_ID, node0, node1 );
//        trLink.setFreespeed( 100. / 3.6 );
//        trLink.setCapacity( 100000. );
//        scenario.getNetwork().addLink( trLink );
//    }
//
//    private static NetworkRoute createNetworkRoute( Id<Link> startLinkId, List<Id<Link>> linkIds, Id<Link> endLinkId, PopulationFactory pf ){
//        NetworkRoute route = pf.getRouteFactories().createRoute( NetworkRoute.class, startLinkId, endLinkId ) ;
//        route.setLinkIds( startLinkId, linkIds, endLinkId ) ;
//        return route;
//    }
//
//    private static void addLinkAndFacility( Scenario scenario, NetworkFactory nf, ActivityFacilitiesFactory ff, Node prevNode, Node node ){
//        final String str = prevNode.getId() + "-" + node.getId();
//        Link link = nf.createLink( Id.createLinkId( str ), prevNode, node ) ;
//        Set<String> set = new HashSet<>() ;
//        set.add("car" ) ;
//        link.setAllowedModes( set ) ;
//        link.setLength( CoordUtils.calcEuclideanDistance( prevNode.getCoord(), node.getCoord() ) );
//        link.setCapacity( 3600. );
//        link.setFreespeed( 50./3.6 );
//        scenario.getNetwork().addLink( link );
//        // ---
//        ActivityFacility af = ff.createActivityFacility( Id.create( str, ActivityFacility.class ), link.getCoord(), link.getId() ) ;
//        ActivityOption option = ff.createActivityOption( "shop" ) ;
//        af.addActivityOption( option );
//        scenario.getActivityFacilities().addActivityFacility( af );
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
//    static void addModeToAllLinksBtwnGivenNodes( Network network, int fromNodeNumber, int toNodeNumber, String drtMode ) {
//        for (int i = fromNodeNumber; i < toNodeNumber; i++) {
//            Set<String> newAllowedModes = new HashSet<>( network.getLinks().get( Id.createLinkId( i + "-" + (i + 1) ) ).getAllowedModes() );
//            newAllowedModes.add(drtMode);
//            network.getLinks().get(Id.createLinkId( i + "-" + (i+1) )).setAllowedModes( newAllowedModes );
//
//            newAllowedModes = new HashSet<>( network.getLinks().get( Id.createLinkId( (i + 1) + "-" + i ) ).getAllowedModes() );
//            newAllowedModes.add(drtMode);
//            network.getLinks().get(Id.createLinkId( (i+1) + "-" + i )).setAllowedModes( newAllowedModes );
//        }
//    }
//
//}
