package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import com.google.common.collect.ImmutableSet;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
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
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.pt.utils.TransitScheduleValidator;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

import java.util.*;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;
/** Attempt to adapt PtALongALine2 to Berlin Scenario.
 * TODO: modify allowed modes on network in order to allocate a service area to mode "drt"
 */

public class drtCodeEx2 {

    private static final Id<Link> TR_LINK_m1_0_ID = Id.createLinkId( "trLinkm1-0" );
    private static final Id<Link> TR_LINK_0_1_ID = Id.createLinkId( "trLink0-1" );
    private static final Id<Link> TR_LONG_LINK_LEFT_ID = Id.createLinkId( "trLinkLongLeft" );
    private static final Id<Link> TR_LINK_MIDDLE_ID = Id.createLinkId( "trLinkMiddle" );
    private static final Id<Link> TR_LONG_LINK_RIGHT_ID = Id.createLinkId( "trLinkLongRight" );
    private static final Id<Link> TR_LINK_LASTM1_LAST_ID = Id.createLinkId( "trLinkLastm1-Last" );
    private static final Id<Link> TR_LINK_LAST_LASTp1_ID = Id.createLinkId( "trLinkLast-Lastp1" ) ;

    private static final Id<TransitStopFacility> tr_stop_fac_0_ID = Id.create( "StopFac0", TransitStopFacility.class );
    private static final Id<TransitStopFacility> tr_stop_fac_10000_ID = Id.create( "StopFac10000", TransitStopFacility.class );
    private static final Id<TransitStopFacility> tr_stop_fac_5000_ID = Id.create( "StopFac5000", TransitStopFacility.class );

    private static final Id<VehicleType> busTypeID = Id.create( "bus", VehicleType.class );

    enum DrtMode { none, teleportBeeline, teleportBasedOnNetworkRoute, full }
    private static DrtMode drtMode = DrtMode.full ;
    private static boolean drt2 = true ;

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


//        Config config = createConfig("C:/Users/jakob/tubCloud/Shared/DRT/PtAlongALine/drt-A/"  );
        Config config = ConfigUtils.loadConfig( configFileName);
        config.global().setNumberOfThreads( 1 );

        config.controler().setOutputDirectory( rootPath + "PtExample/A/" ) ;
        config.controler().setLastIteration( 0 );

        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.walk ).setTeleportedModeSpeed( 3. );
        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.bike ).setTeleportedModeSpeed( 10. );

        config.qsim().setEndTime( 24.*3600. );

        config.transit().setUseTransit(true) ;

        // This configures otfvis:
        OTFVisConfigGroup visConfig = ConfigUtils.addOrGetModule( config, OTFVisConfigGroup.class );
        visConfig.setDrawTransitFacilities( false );
        visConfig.setColoringScheme( OTFVisConfigGroup.ColoringScheme.bvg ) ;
        visConfig.setDrawTime(true);
        visConfig.setDrawNonMovingItems(true);
        visConfig.setAgentSize(125);
        visConfig.setLinkWidth(30);
        visConfig.setShowTeleportedAgents( true );
        visConfig.setDrawTransitFacilities( true );
//		{
//			BufferedImage image = null ;
//			Rectangle2D zoomstore = new Rectangle2D.Double( 0., 0., +100.*1000., +10.*1000. ) ;
//			ZoomEntry zoomEntry = new ZoomEntry( image, zoomstore, "*Initial*" ) ;
//			visConfig.addZoom( zoomEntry );
//		}

        config.qsim().setSnapshotStyle( QSimConfigGroup.SnapshotStyle.kinematicWaves );
        config.qsim().setTrafficDynamics( QSimConfigGroup.TrafficDynamics.kinematicWaves );

        configureScoring(config);

        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.network().setInputFile("berlin-v5-network.xml.gz");
//        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz"); // full 1% population
//        config.plans().setInputFile("berlin-downsample.xml"); // 1% of 1% population
        config.plans().setInputFile("berlin-plans-Frohnau.xml"); // 1% population in Frohnau
        config.plans().setInputPersonAttributeFile("berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("berlin-v5.4-transit-vehicles.xml.gz");

        // === GBL: ===

        config.controler().setLastIteration(50);
        config.global().setNumberOfThreads( 1 );
        config.controler().setOutputDirectory(rootPath + version + "/output/D/");
//        config.controler().setRoutingAlgorithmType( FastAStarLandmarks );
        config.transit().setUseTransit(true) ;
        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn );
        config.controler().setWritePlansInterval(5);

        // === ROUTER: ===

        config.plansCalcRoute().setInsertingAccessEgressWalk( true );

        config.qsim().setVehiclesSource( QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData );
        // (as of today, will also influence router. kai, jun'19)

        if(  drtMode == DrtMode.teleportBeeline ){// (configure teleportation router)
            PlansCalcRouteConfigGroup.ModeRoutingParams drtParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
            drtParams.setMode( TransportMode.drt );
            drtParams.setTeleportedModeSpeed( 100. / 3.6 );
            config.plansCalcRoute().addModeRoutingParams( drtParams );
            if( drt2 ){
                PlansCalcRouteConfigGroup.ModeRoutingParams drt2Params = new PlansCalcRouteConfigGroup.ModeRoutingParams();
                drt2Params.setMode( "drt2" );
                drt2Params.setTeleportedModeSpeed( 100. / 3.6 );
                config.plansCalcRoute().addModeRoutingParams( drt2Params );
            }
            // teleportation router for walk or bike is automatically defined.
        } else if( drtMode == DrtMode.teleportBasedOnNetworkRoute ){// (route as network route)
            Set<String> networkModes = new HashSet<>( );
            networkModes.add( TransportMode.drt );
            if( drt2 ){
                networkModes.add( "drt2" );
            }
            config.plansCalcRoute().setNetworkModes( networkModes );
        }

        // set up walk2 so we don't need walk in raptor:
        PlansCalcRouteConfigGroup.ModeRoutingParams walkParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
        walkParams.setMode( "walk2" );
        walkParams.setTeleportedModeSpeed( 5. / 3.6 );
        config.plansCalcRoute().addModeRoutingParams( walkParams );


        config.plansCalcRoute().setRoutingRandomness( 3. ); //jr
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
        config.plansCalcRoute().removeModeRoutingParams("undefined");

        // === RAPTOR: ===
        {
            SwissRailRaptorConfigGroup configRaptor = ConfigUtils.addOrGetModule( config, SwissRailRaptorConfigGroup.class ) ;

            if ( drtMode!= DrtMode.none){
                configRaptor.setUseIntermodalAccessEgress(true);
                {
                    // Xxx
                    SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetXxx = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
                    //					paramSetXxx.setMode( TransportMode.walk ); // this does not work because sbb raptor treats it in a special way
                    paramSetXxx.setMode( "walk2" );
                    paramSetXxx.setRadius( 1000000 );
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
                    paramSetDrt2.setRadius( 1000000 );
                    //				paramSetDrt2.setPersonFilterAttribute( null );
                    //				paramSetDrt2.setStopFilterAttribute( null );
                    configRaptor.addIntermodalAccessEgress( paramSetDrt2 );
                }
            }

        }

        // === SCORING: ===

        double margUtlTravPt = config.planCalcScore().getModes().get( TransportMode.pt ).getMarginalUtilityOfTraveling();
        if ( drtMode!= DrtMode.none ) {
            // (scoring parameters for drt modes)
            PlanCalcScoreConfigGroup.ModeParams drtScoreParams=new PlanCalcScoreConfigGroup.ModeParams(TransportMode.drt);
            drtScoreParams.setMarginalUtilityOfTraveling(margUtlTravPt);
            config.planCalcScore().addModeParams(drtScoreParams);

            if ( drt2 ) {
                PlanCalcScoreConfigGroup.ModeParams drt2ScoreParams= new PlanCalcScoreConfigGroup.ModeParams("drt2");
                drt2ScoreParams.setMarginalUtilityOfTraveling(margUtlTravPt);
                config.planCalcScore().addModeParams(drt2ScoreParams ) ;
            }
        }
        PlanCalcScoreConfigGroup.ModeParams walkScoreParams = new PlanCalcScoreConfigGroup.ModeParams("walk2");
        walkScoreParams.setMarginalUtilityOfTraveling( margUtlTravPt );
        config.planCalcScore().addModeParams( walkScoreParams) ;

        config = RunBerlinZoomer.SetupActivityParams(config); //jr

        // === QSIM: ===

        config.qsim().setSimStarttimeInterpretation( QSimConfigGroup.StarttimeInterpretation.onlyUseStarttime );
        // yy why?  kai, jun'19

        // === DRT: ===

        if ( drtMode== DrtMode.full ){
            // (configure full drt if applicable)

            String drtVehiclesFile = "drt_vehicles.xml";
            String drt2VehiclesFile = "drt2_vehicles.xml";

            DvrpConfigGroup dvrpConfig = ConfigUtils.addOrGetModule( config, DvrpConfigGroup.class );
            dvrpConfig.setNetworkModes( ImmutableSet.copyOf( Arrays.asList( TransportMode.drt, "drt2" ) ) ) ;

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
                drtConfig.setUseModeFilteredSubnetwork( true );
                mm.addParameterSet( drtConfig );
            }
            if ( drt2 ) {
                DrtConfigGroup drtConfig = new DrtConfigGroup();
                drtConfig.setMaxTravelTimeAlpha( 1.3 );
                drtConfig.setVehiclesFile( drt2VehiclesFile );
                drtConfig.setMaxTravelTimeBeta( 5. * 60. );
                drtConfig.setStopDuration( 60. );
                drtConfig.setMaxWaitTime( Double.MAX_VALUE );
                drtConfig.setRequestRejection( false );
                drtConfig.setMode( "drt2" );
                drtConfig.setUseModeFilteredSubnetwork( true );
                mm.addParameterSet( drtConfig );
            }

            for( DrtConfigGroup drtConfigGroup : mm.getModalElements() ){
                DrtConfigs.adjustDrtConfig( drtConfigGroup, config.planCalcScore() );
            }

            // TODO: avoid really writing out these files. However so far it is unclear how
            // to configure DRT and load the vehicles otherwise
            createDrtVehiclesFile(drtVehiclesFile, "DRT-", 10, Id.createLinkId("0-1" ) );
            if ( drt2 ){
                createDrtVehiclesFile( drt2VehiclesFile, "DRT2-", 10, Id.createLinkId( "1000-999" ) );
            }

        }

        // === VSP: ===

        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn );

        // ### SCENARIO: ###

//        Scenario scenario = createScenario(config , 30 );

        Scenario scenario = ScenarioUtils.loadScenario( config ); //jr

        if ( drtMode== DrtMode.full ) {
            scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory( DrtRoute.class, new DrtRouteFactory() );
        }

        // add drt modes to the car links' allowed modes in their respective service area
        addModeToAllLinksBtwnGivenNodes(scenario.getNetwork(), 0, 400, TransportMode.drt );
        if ( drt2 ){
            addModeToAllLinksBtwnGivenNodes( scenario.getNetwork(), 800, 1000, "drt2" );
        }
        // TODO: reference somehow network creation, to ensure that these link ids exist


        // The following is also for the router! kai, jun'19
        VehiclesFactory vf = scenario.getVehicles().getFactory();
        if ( drt2 ) {
            VehicleType vehType = vf.createVehicleType( Id.create( "drt2", VehicleType.class ) );
            vehType.setMaximumVelocity( 25./3.6 );
            scenario.getVehicles().addVehicleType( vehType );
        }{
            VehicleType vehType = vf.createVehicleType( Id.create( TransportMode.drt, VehicleType.class ) );
            vehType.setMaximumVelocity( 25./3.6 );
            scenario.getVehicles().addVehicleType( vehType );
        }
        {
            // (does not work without; I don't really know why. kai)
            VehicleType vehType = vf.createVehicleType( Id.create( TransportMode.car, VehicleType.class ) );
            vehType.setMaximumVelocity( 25./3.6 );
            scenario.getVehicles().addVehicleType( vehType );
        }

//		scenario.getPopulation().getPersons().values().removeIf( person -> !person.getId().toString().equals( "3" ) );

        // ### CONTROLER: ###

        Controler controler = new Controler(scenario);

        controler.addOverridingModule(new SwissRailRaptorModule() ) ;

        if ( drtMode== DrtMode.full ){
            controler.addOverridingModule( new DvrpModule() );
            controler.addOverridingModule( new MultiModeDrtModule() );
            if ( drt2 ){
                controler.configureQSimComponents( DvrpQSimComponents.activateModes( TransportMode.drt, "drt2" ) );
            } else{
                controler.configureQSimComponents( DvrpQSimComponents.activateModes( TransportMode.drt ) );
            }
        }

        // This will start otfvis.  Comment out if not needed.
//        controler.addOverridingModule( new OTFVisLiveModule() );

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

    static Config createConfig( String outputDir ){
        Config config = ConfigUtils.createConfig() ;

        config.global().setNumberOfThreads( 1 );

        config.controler().setOutputDirectory( outputDir ) ;
        config.controler().setLastIteration( 0 );

        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.walk ).setTeleportedModeSpeed( 3. );
        config.plansCalcRoute().getModeRoutingParams().get( TransportMode.bike ).setTeleportedModeSpeed( 10. );

        config.qsim().setEndTime( 24.*3600. );

        config.transit().setUseTransit(true) ;

        // This configures otfvis:
        OTFVisConfigGroup visConfig = ConfigUtils.addOrGetModule( config, OTFVisConfigGroup.class );
        visConfig.setDrawTransitFacilities( false );
        visConfig.setColoringScheme( OTFVisConfigGroup.ColoringScheme.bvg ) ;
        visConfig.setDrawTime(true);
        visConfig.setDrawNonMovingItems(true);
        visConfig.setAgentSize(125);
        visConfig.setLinkWidth(30);
        visConfig.setShowTeleportedAgents( true );
        visConfig.setDrawTransitFacilities( true );
//		{
//			BufferedImage image = null ;
//			Rectangle2D zoomstore = new Rectangle2D.Double( 0., 0., +100.*1000., +10.*1000. ) ;
//			ZoomEntry zoomEntry = new ZoomEntry( image, zoomstore, "*Initial*" ) ;
//			visConfig.addZoom( zoomEntry );
//		}

        config.qsim().setSnapshotStyle( QSimConfigGroup.SnapshotStyle.kinematicWaves );
        config.qsim().setTrafficDynamics( QSimConfigGroup.TrafficDynamics.kinematicWaves );

        configureScoring(config);
        return config;
    }

    static Scenario createScenario( Config config, long numberOfPersons ){
        Scenario scenario = ScenarioUtils.createScenario( config );
        // don't load anything

        final int lastNodeIdx = 1000;
        final double deltaX = 100.;

        createAndAddCarNetwork( scenario, lastNodeIdx, deltaX );

        createAndAddPopulation( scenario , "pt", numberOfPersons );

        final double deltaY = 1000.;

        createAndAddTransitNetwork( scenario, lastNodeIdx, deltaX, deltaY );

        createAndAddTransitStopFacilities( scenario, lastNodeIdx, deltaX, deltaY );

        createAndAddTransitVehicleType( scenario );

        createAndAddTransitLine( scenario );

        TransitScheduleValidator.printResult( TransitScheduleValidator.validateAll( scenario.getTransitSchedule(), scenario.getNetwork() ) );
        return scenario;
    }

    private static void configureScoring(Config config) {
        PlanCalcScoreConfigGroup.ModeParams accessWalk = new PlanCalcScoreConfigGroup.ModeParams( TransportMode.non_network_walk );
        accessWalk.setMarginalUtilityOfTraveling(0);
        config.planCalcScore().addModeParams(accessWalk);

        PlanCalcScoreConfigGroup.ModeParams transitWalk = new PlanCalcScoreConfigGroup.ModeParams("transit_walk");
        transitWalk.setMarginalUtilityOfTraveling(0);
        config.planCalcScore().addModeParams(transitWalk);

        PlanCalcScoreConfigGroup.ModeParams bike = new PlanCalcScoreConfigGroup.ModeParams("bike");
        bike.setMarginalUtilityOfTraveling(0);
        config.planCalcScore().addModeParams(bike);

        PlanCalcScoreConfigGroup.ModeParams drt = new PlanCalcScoreConfigGroup.ModeParams("drt");
        drt.setMarginalUtilityOfTraveling(0);
        config.planCalcScore().addModeParams(drt);
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
        //		paramSetBike.setStopFilterAttribute("bikeAccessible");
        //		paramSetBike.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetBike );

        return configRaptor;
    }

    private static void createAndAddTransitLine( Scenario scenario ){
        PopulationFactory pf = scenario.getPopulation().getFactory();
        TransitSchedule schedule = scenario.getTransitSchedule();
        TransitScheduleFactory tsf = schedule.getFactory();
        VehiclesFactory tvf = scenario.getTransitVehicles().getFactory();

        List<Id<Link>> linkIds = new ArrayList<>() ;
        linkIds.add( TR_LINK_0_1_ID ) ;
        linkIds.add( TR_LONG_LINK_LEFT_ID ) ;
        linkIds.add( TR_LINK_MIDDLE_ID ) ;
        linkIds.add( TR_LONG_LINK_RIGHT_ID ) ;
        linkIds.add( TR_LINK_LASTM1_LAST_ID ) ;
        NetworkRoute route = createNetworkRoute( TR_LINK_m1_0_ID, linkIds, TR_LINK_LAST_LASTp1_ID, pf );

        List<TransitRouteStop> stops = new ArrayList<>() ;
        {
            stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( tr_stop_fac_0_ID ), 0., 0. ) );
            stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( tr_stop_fac_5000_ID ), 1., 1. ) );
            stops.add( tsf.createTransitRouteStop( schedule.getFacilities().get( tr_stop_fac_10000_ID ), 1., 1. ) );
        }
        {
            TransitRoute transitRoute = tsf.createTransitRoute( Id.create( "route1", TransitRoute.class ), route, stops, "bus" );
            for ( int ii=0 ; ii<100 ; ii++ ){
                String str = "tr_" + ii ;

                scenario.getTransitVehicles().addVehicle( tvf.createVehicle( Id.createVehicleId( str ), scenario.getTransitVehicles().getVehicleTypes().get( busTypeID) ) );

                Departure departure = tsf.createDeparture( Id.create( str, Departure.class ), 7. * 3600. + ii*300 ) ;
                departure.setVehicleId( Id.createVehicleId( str ) );
                transitRoute.addDeparture( departure );
            }
            TransitLine line = tsf.createTransitLine( Id.create( "line1", TransitLine.class ) );
            line.addRoute( transitRoute );

            schedule.addTransitLine( line );
        }
    }

    private static void createAndAddTransitVehicleType( Scenario scenario ){
        VehiclesFactory tvf = scenario.getTransitVehicles().getFactory();
        VehicleType busType = tvf.createVehicleType( busTypeID );
        {
            VehicleCapacity capacity = tvf.createVehicleCapacity();
            capacity.setSeats( 100 );
            busType.setCapacity( capacity );
        }
        {
            busType.setMaximumVelocity( 100. / 3.6 );
        }
        scenario.getTransitVehicles().addVehicleType( busType );
    }

    private static void createAndAddTransitStopFacilities( Scenario scenario, int lastNodeIdx, double deltaX, double deltaY ){
        TransitSchedule schedule = scenario.getTransitSchedule();
        TransitScheduleFactory tsf = schedule.getFactory();

        TransitStopFacility stopFacility0 = tsf.createTransitStopFacility( tr_stop_fac_0_ID, new Coord( deltaX, deltaY ), false );
        stopFacility0.setLinkId( TR_LINK_0_1_ID );
        schedule.addStopFacility( stopFacility0 );

        TransitStopFacility stopFacility5000 = tsf.createTransitStopFacility( tr_stop_fac_5000_ID, new Coord( 0.5 * (lastNodeIdx - 1) * deltaX, deltaY ), false );
        stopFacility5000.setLinkId( TR_LINK_MIDDLE_ID );
        stopFacility5000.getAttributes().putAttribute( "drtAccessible", "true" );
        stopFacility5000.getAttributes().putAttribute( "bikeAccessible", "true");

        schedule.addStopFacility( stopFacility5000 );

        TransitStopFacility stopFacility10000 = tsf.createTransitStopFacility( tr_stop_fac_10000_ID, new Coord( (lastNodeIdx - 1) * deltaX, deltaY ), false );
        stopFacility10000.setLinkId( TR_LINK_LASTM1_LAST_ID );
        schedule.addStopFacility( stopFacility10000 );
    }

    private static void createAndAddTransitNetwork( Scenario scenario, int lastNodeIdx, double deltaX, double deltaY ){
        NetworkFactory nf = scenario.getNetwork().getFactory();

        Node nodem1 = nf.createNode( Id.createNodeId("trNodeM1" ), new Coord(-100, deltaY ) );
        scenario.getNetwork().addNode(nodem1);
        // ---
        Node node0 = nf.createNode( Id.createNodeId("trNode0" ), new Coord(0, deltaY ) );
        scenario.getNetwork().addNode(node0);
        createAndAddTransitLink( scenario, nodem1, node0, TR_LINK_m1_0_ID );
        // ---
        Node node1 = nf.createNode( Id.createNodeId("trNode1"), new Coord(deltaX, deltaY ) ) ;
        scenario.getNetwork().addNode( node1 ) ;
        createAndAddTransitLink( scenario, node0, node1, TR_LINK_0_1_ID );
        // ---
        Node nodeMiddleLeft = nf.createNode( Id.createNodeId("trNodeMiddleLeft") , new Coord( 0.5*(lastNodeIdx-1)*deltaX , deltaY ) ) ;
        scenario.getNetwork().addNode( nodeMiddleLeft) ;
        {
            createAndAddTransitLink( scenario, node1, nodeMiddleLeft, TR_LONG_LINK_LEFT_ID );
        }
        // ---
        Node nodeMiddleRight = nf.createNode( Id.createNodeId("trNodeMiddleRight") , new Coord( 0.5*(lastNodeIdx+1)*deltaX , deltaY ) ) ;
        scenario.getNetwork().addNode( nodeMiddleRight ) ;
        createAndAddTransitLink( scenario, nodeMiddleLeft, nodeMiddleRight, TR_LINK_MIDDLE_ID );
        // ---
        Node nodeLastm1 = nf.createNode( Id.createNodeId("trNodeLastm1") , new Coord( (lastNodeIdx-1)*deltaX , deltaY ) ) ;
        scenario.getNetwork().addNode( nodeLastm1) ;
        createAndAddTransitLink( scenario, nodeMiddleRight, nodeLastm1, TR_LONG_LINK_RIGHT_ID );

        // ---
        Node nodeLast = nf.createNode(Id.createNodeId("trNodeLast"), new Coord(lastNodeIdx*deltaX, deltaY ) ) ;
        scenario.getNetwork().addNode(nodeLast);
        createAndAddTransitLink( scenario, nodeLastm1, nodeLast, TR_LINK_LASTM1_LAST_ID );
        // ---
        Node nodeLastp1 = nf.createNode(Id.createNodeId("trNodeLastp1"), new Coord(lastNodeIdx*deltaX+100., deltaY ) ) ;
        scenario.getNetwork().addNode(nodeLastp1);
        createAndAddTransitLink( scenario, nodeLast, nodeLastp1, TR_LINK_LAST_LASTp1_ID );
    }

    private static void createAndAddPopulation( Scenario scenario, String mode, long numberOfPersons ){
        PopulationFactory pf = scenario.getPopulation().getFactory();
        List<ActivityFacility> facilitiesAsList = new ArrayList<>( scenario.getActivityFacilities().getFacilities().values() ) ;
        final Id<ActivityFacility> activityFacilityId = facilitiesAsList.get( facilitiesAsList.size()-1 ).getId() ;
        for( int jj = 0 ; jj < numberOfPersons ; jj++ ){
            Person person = pf.createPerson( Id.createPersonId( jj ) );
            {
                scenario.getPopulation().addPerson( person );
                Plan plan = pf.createPlan();
                person.addPlan( plan );

                // --- 1st location at randomly selected facility:
                int idx = MatsimRandom.getRandom().nextInt( facilitiesAsList.size() );
                Id<ActivityFacility> homeFacilityId = facilitiesAsList.get( idx ).getId();
                Activity home = pf.createActivityFromActivityFacilityId( "dummy", homeFacilityId );
                if ( jj==0 ){
                    home.setEndTime( 7. * 3600. ); // one agent one sec earlier so that for all others the initial acts are visible in VIA
                } else {
                    home.setEndTime( 7. * 3600. + 1. );
                }
                plan.addActivity( home );
                {
                    Leg leg = pf.createLeg( mode );
                    leg.setDepartureTime( 7. * 3600. );
                    leg.setTravelTime( 1800. );
                    plan.addLeg( leg );
                }
                {
                    Activity shop = pf.createActivityFromActivityFacilityId( "dummy", activityFacilityId );
                    plan.addActivity( shop );
                }
            }
        }
    }

    private static void createAndAddCarNetwork( Scenario scenario, int lastNodeIdx, double deltaX ){
        // Construct a network and facilities along a line:
        // 0 --(0-1)-- 1 --(2-1)-- 2 -- ...
        // with a facility of same ID attached to each link.

        NetworkFactory nf = scenario.getNetwork().getFactory();
        ActivityFacilitiesFactory ff = scenario.getActivityFacilities().getFactory();

        Node prevNode;
        {
            Node node = nf.createNode( Id.createNodeId( 0 ), new Coord( 0., 0. ) );
            scenario.getNetwork().addNode( node );
            prevNode = node;
        }
        for( int ii = 1 ; ii <= lastNodeIdx ; ii++ ){
            Node node = nf.createNode( Id.createNodeId( ii ), new Coord( ii * deltaX, 0. ) );
            scenario.getNetwork().addNode( node );
            // ---
            addLinkAndFacility( scenario, nf, ff, prevNode, node );
            addLinkAndFacility( scenario, nf, ff, node, prevNode );
            // ---
            prevNode = node;
        }
    }

    private static void createAndAddTransitLink( Scenario scenario, Node node0, Node node1, Id<Link> TR_LINK_0_1_ID ){
        Link trLink = scenario.getNetwork().getFactory().createLink( TR_LINK_0_1_ID, node0, node1 );
        trLink.setFreespeed( 100. / 3.6 );
        trLink.setCapacity( 100000. );
        scenario.getNetwork().addLink( trLink );
    }

    private static NetworkRoute createNetworkRoute( Id<Link> startLinkId, List<Id<Link>> linkIds, Id<Link> endLinkId, PopulationFactory pf ){
        NetworkRoute route = pf.getRouteFactories().createRoute( NetworkRoute.class, startLinkId, endLinkId ) ;
        route.setLinkIds( startLinkId, linkIds, endLinkId ) ;
        return route;
    }

    private static void addLinkAndFacility( Scenario scenario, NetworkFactory nf, ActivityFacilitiesFactory ff, Node prevNode, Node node ){
        final String str = prevNode.getId() + "-" + node.getId();
        Link link = nf.createLink( Id.createLinkId( str ), prevNode, node ) ;
        Set<String> set = new HashSet<>() ;
        set.add("car" ) ;
        link.setAllowedModes( set ) ;
        link.setLength( CoordUtils.calcEuclideanDistance( prevNode.getCoord(), node.getCoord() ) );
        link.setCapacity( 3600. );
        link.setFreespeed( 50./3.6 );
        scenario.getNetwork().addLink( link );
        // ---
        ActivityFacility af = ff.createActivityFacility( Id.create( str, ActivityFacility.class ), link.getCoord(), link.getId() ) ;
        ActivityOption option = ff.createActivityOption( "shop" ) ;
        af.addActivityOption( option );
        scenario.getActivityFacilities().addActivityFacility( af );
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

    static void addModeToAllLinksBtwnGivenNodes(Network network, int fromNodeNumber, int toNodeNumber, String drtMode ) {
        for (int i = fromNodeNumber; i < toNodeNumber; i++) {
            Set<String> newAllowedModes = new HashSet<>( network.getLinks().get( Id.createLinkId( i + "-" + (i + 1) ) ).getAllowedModes() );
            newAllowedModes.add(drtMode);
            network.getLinks().get(Id.createLinkId( i + "-" + (i+1) )).setAllowedModes( newAllowedModes );

            newAllowedModes = new HashSet<>( network.getLinks().get( Id.createLinkId( (i + 1) + "-" + i ) ).getAllowedModes() );
            newAllowedModes.add(drtMode);
            network.getLinks().get(Id.createLinkId( (i+1) + "-" + i )).setAllowedModes( newAllowedModes );
        }
    }
}
