package main.run;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.matsim.analysis.ScoreStats;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.scenario.ScenarioUtils;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;

/**
 * @author ikaddoura
 */

public final class RunBerlinBike4 {

    public static final String CONFIG_PATH = "C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-03\\input\\berlin-v5.4-1pct.config.xml";
    private static final Logger log = Logger.getLogger(RunBerlinBike4.class );

    private final CommandLine cmd;

    private final String configFileName;
    private Config config;
    private Scenario scenario;
    private Controler controler;

    private boolean hasPreparedConfig = false ;
    private boolean hasPreparedScenario = false ;
    private boolean hasPreparedControler = false ;

    public static void main(String[] args) {

        for (String arg : args) {
            log.info( arg );
        }

        if ( args.length==0 ) {
            String configFileName = "C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-03\\input\\berlin-v5.4-1pct.config.xml";
            new RunBerlinBike4( new String[]{ "--" + CONFIG_PATH, configFileName } ).run() ;

        } else {
            new RunBerlinBike4( args ).run() ;
        }
    }

    @Deprecated // use version with String [] args
    public RunBerlinBike4( String configFileName) {
        this.configFileName = configFileName;
        this.cmd = null ;
    }

    public RunBerlinBike4( String [] args ) {

        try{

            if (Arrays.toString(args).contains("--" + CONFIG_PATH)) {
                cmd = new CommandLine.Builder( args )
                        .allowPositionalArguments(false)
                        .requireOptions(CONFIG_PATH)
                        .allowAnyOption(true)
                        .build() ;
                this.configFileName = cmd.getOptionStrict( CONFIG_PATH ) ;

            } else {
                // required by the GUI
                cmd = new CommandLine.Builder( args )
                        .allowPositionalArguments(true)
                        .allowAnyOption(true)
                        .build() ;
                this.configFileName = cmd.getPositionalArgumentStrict(0);
            }

        } catch( CommandLine.ConfigurationException e ){
            throw new RuntimeException( e ) ;
        }
    }

    public final Controler prepareControler( AbstractModule... overridingModules ) {
        // note that for something like signals, and presumably drt, one needs the controler object

        if ( !hasPreparedScenario ) {
            prepareScenario() ;
        }

        controler = new Controler( scenario );

        if (controler.getConfig().transit().isUsingTransitInMobsim()) {
            // use the sbb pt raptor router
            controler.addOverridingModule( new AbstractModule() {
                @Override
                public void install() {
                    install( new SwissRailRaptorModule() );
                }
            } );
        } else {
            log.warn("Public transit will be teleported and not simulated in the mobsim! "
                    + "This will have a significant effect on pt-related parameters (travel times, modal split, and so on). "
                    + "Should only be used for testing or car-focused studies with fixed modal split.  ");
        }

        // use the (congested) car travel time for the teleported ride mode
        controler.addOverridingModule( new AbstractModule() {
            @Override
            public void install() {
                addTravelTimeBinding( TransportMode.ride ).to( networkTravelTime() );
                addTravelDisutilityFactoryBinding( TransportMode.ride ).to( carTravelDisutilityFactoryKey() );
            }
        } );

        for ( AbstractModule overridingModule : overridingModules ) {
            controler.addOverridingModule( overridingModule );
        }

        hasPreparedControler = true ;
        return controler;
    }

    public final void addOverridingModule( AbstractModule controlerModule ) {
        if ( controler==null ) {
            prepareControler(  ) ;
        }
        controler.addOverridingModule( controlerModule ) ;
    }

    public final void addOverridingQSimModule( AbstractQSimModule qSimModule ) {
        if ( controler==null ) {
            prepareControler(  ) ;
        }
        controler.addOverridingQSimModule( qSimModule );
    }

    public final Scenario prepareScenario() {
        if ( !hasPreparedConfig ) {
            prepareConfig( ) ;
        }

        // note that the path for this is different when run from GUI (path of original config) vs.
        // when run from command line/IDE (java root).  :-(    See comment in method.  kai, jul'18

        scenario = ScenarioUtils.loadScenario( config );

        hasPreparedScenario = true ;
        return scenario;
    }

    public Config prepareConfig(ConfigGroup... customModules) {
        OutputDirectoryLogging.catchLogEntries();

        config = ConfigUtils.loadConfig( configFileName, customModules ) ; // I need this to set the context

        config.controler().setLastIteration(0); // jr
        config.controler().setOutputDirectory("../output");
        config.network().setInputFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-network.xml.gz");
        config.plans().setInputFile("berlin-v5.4-1pct.plans.xml.gz");
        config.plans().setInputPersonAttributeFile("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-person-attributes.xml.gz");
        config.vehicles().setVehiclesFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-mode-vehicle-types.xml");
        config.transit().setTransitScheduleFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5-transit-schedule.xml.gz");
        config.transit().setVehiclesFile("http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct/input/berlin-v5.4-transit-vehicles.xml.gz");


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

        if (cmd != null) {
            try {
                cmd.applyConfiguration( config );
            } catch (ConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        hasPreparedConfig = true ;
        return config ;
    }

    public void run() {
        if ( !hasPreparedControler ) {
            prepareControler() ;
        }
        controler.run();
        log.info("Done.");
    }

    final ScoreStats getScoreStats() {
        return controler.getScoreStats() ;
    }

    final Population getPopulation() {
        return controler.getScenario().getPopulation();
    }

}
