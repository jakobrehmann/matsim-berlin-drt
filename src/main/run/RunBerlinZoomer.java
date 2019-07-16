package main.run;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;
import static org.matsim.core.config.groups.PlanCalcScoreConfigGroup.*;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.*;
import org.matsim.core.config.groups.*;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


/** "Zoomer" is a teleported mode that can only be used as an access/egress mode within a pt route. Zoomer is a
 * placeholder mode, which can be configured in order to emulate bike, drt, etc.
 */


public class RunBerlinZoomer {

    public static void main(String[] args) {
        String username = "jakob";
        String version = "2019-07-16/A-BackToZoomer";
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

//        String configFileName = rootPath + "Input_global/berlin-v5.4-1pct.config.xml";
        String configFileName = rootPath + "Input_global/berlin-config-ReRoute.xml";

        // -- C O N F I G --
        Config config = ConfigUtils.loadConfig( configFileName);

        // Input Files -- local
        config.network().setInputFile("berlin-v5-network.xml.gz");
//        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz"); // full 1% population
//        config.plans().setInputFile("berlin-downsample.xml"); // 1% of 1% population
        config.plans().setInputFile("berlin-plans-Frohnau.xml"); // 1% population in Frohnau
        config.plans().setInputPersonAttributeFile("berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("berlin-v5.4-transit-vehicles.xml.gz");

        String outputDirectory = rootPath + version + "/output/";
        new File(outputDirectory).mkdirs();

        config.controler().setLastIteration(50);
        config.global().setNumberOfThreads( 1 );
        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setRoutingAlgorithmType( FastAStarLandmarks );
        config.transit().setUseTransit(true) ;
        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn );
        config.controler().setWritePlansInterval(5);
        config.controler().setWriteEventsInterval(5);

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


        // Zoomer Setup
        ModeParams zoomParams = new ModeParams("zoomer");
        zoomParams.setMarginalUtilityOfTraveling(100);
        config.planCalcScore().addModeParams(zoomParams);

        PlansCalcRouteConfigGroup.ModeRoutingParams zoomRoutingParams = new PlansCalcRouteConfigGroup.ModeRoutingParams();
        zoomRoutingParams.setMode("zoomer");
        zoomRoutingParams.setBeelineDistanceFactor(1.3);
        zoomRoutingParams.setTeleportedModeSpeed(10000.);
        config.plansCalcRoute().addModeRoutingParams(zoomRoutingParams);

        // Raptor
        SwissRailRaptorConfigGroup raptor = setupRaptorConfigGroup();
        config.addModule(raptor);


//         Network Change Events
        config.network().setTimeVariantNetwork(true);
        config.network().setChangeEventsInputFile("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/Input_global/networkChangeEvents.xml");


        // -- S C E N A R I O --
        Scenario scenario = ScenarioUtils.loadScenario( config );

//        //Adds Zoomer Attribute to Transit Schedule
//        ArrayList<String> frohnauStops = readFile("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/Input_global/FrohnauStopFacilities.txt") ;
//        for (String stopId : frohnauStops) {
//            scenario.getTransitSchedule().getFacilities().get(stopId).getAttributes().putAttribute( "zoomerAccessible", "true" );
//        }


        VehiclesFactory vf = scenario.getVehicles().getFactory();
        VehicleType vehType = vf.createVehicleType(Id.create(TransportMode.ride, VehicleType.class));
        vehType.setMaximumVelocity(25. / 3.6);
        scenario.getVehicles().addVehicleType(vehType);

        // -- C O N T R O L E R --
        Controler controler = new Controler( scenario );
        controler.addOverridingModule(new SwissRailRaptorModule());

        // use the (congested) car travel time for the teleported ride mode
        controler.addOverridingModule( new AbstractModule() {
            @Override
            public void install() {
                addTravelTimeBinding( TransportMode.ride ).to( networkTravelTime() );
                addTravelDisutilityFactoryBinding( TransportMode.ride ).to( carTravelDisutilityFactoryKey() );
            }
        } );

        controler.run(); //


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

        // Access Walk
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkA = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalkA.setMode(TransportMode.access_walk);
        paramSetWalkA.setRadius(1);
        paramSetWalkA.setPersonFilterAttribute(null);
        paramSetWalkA.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalkA );

        // Egress Walk
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkE = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalkE.setMode(TransportMode.egress_walk);
        paramSetWalkE.setRadius(1);
        paramSetWalkE.setPersonFilterAttribute(null);
        paramSetWalkE.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalkE );

        // Transit Walk
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetWalkNN = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetWalkNN.setMode(TransportMode.transit_walk);
        paramSetWalkNN.setRadius(1);
        paramSetWalkNN.setPersonFilterAttribute(null);
        paramSetWalkNN.setStopFilterAttribute(null);
        configRaptor.addIntermodalAccessEgress(paramSetWalkNN );

        // Zoomer
        SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet paramSetZoomer = new SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet();
        paramSetZoomer.setMode("zoomer");
        paramSetZoomer.setRadius(10000);
        paramSetZoomer.setPersonFilterAttribute(null);
//        paramSetBike.setStopFilterAttribute("bikeAccessible");
//        paramSetBike.setStopFilterValue("true");
        configRaptor.addIntermodalAccessEgress(paramSetZoomer );

        return configRaptor;
    }


    static Config SetupActivityParams(Config config) {
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

    public static ArrayList<String> readFile (String fileName){
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
