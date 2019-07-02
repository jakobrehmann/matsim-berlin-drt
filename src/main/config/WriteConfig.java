//package main.config;
//
//import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
//import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.api.core.v01.TransportMode;
//import org.matsim.contrib.av.intermodal.router.VariableAccessTransitRouterModule;
//import org.matsim.contrib.drt.run.DrtConfigGroup;
//import org.matsim.contrib.drt.run.DrtControlerCreator;
//import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
//import org.matsim.core.config.Config;
//import org.matsim.core.config.ConfigGroup;
//import org.matsim.core.config.ConfigUtils;
//import org.matsim.core.config.ConfigWriter;
//import org.matsim.core.config.groups.StrategyConfigGroup;
//import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
//import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
//import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
//import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule;
//import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
//import org.matsim.core.scenario.ScenarioUtils;
//import org.matsim.run.Controler;
//
//
//public class WriteConfig {
//	static Path vehiclesFile = Paths.get(".\\scenarios\\berlin\\input\\taxis_10.xml");
//	static Path basicConfig = Paths.get("C:\\Users\\jakob\\git\\matsim-code-examples\\scenarios\\equil\\config.xml");
//	public static void main(String[] args) {
//
//		Config config = ConfigUtils.loadConfig(basicConfig.toString()) ;
//
//		// Controler
//		config.controler().setLastIteration(0);
//		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
//		config.controler().setOutputDirectory(".\\output");
//
//		// Scoring
//		ModeParams paramsDrt = new ModeParams(TransportMode.drt);
//		config.planCalcScore().addModeParams(paramsDrt);
//
//		// Routing
//		for (ModeRoutingParams i : config.plansCalcRoute().getModeRoutingParams().values()) { // this is the only way I didn't lose other modes. There has to be an easier way...
//			config.plansCalcRoute().addModeRoutingParams(i);
//		}
//		ModeRoutingParams params = new ModeRoutingParams("drt");
//		params.setTeleportedModeSpeed(8.333333333333334); // What speed should this be?
//		config.plansCalcRoute().addModeRoutingParams(params );
//
////
//		{ // ***** drt Module *****
//			DrtConfigGroup configDrt = new DrtConfigGroup();
//			configDrt.setVehiclesFile(vehiclesFile.toString());
//			configDrt.setOperationalScheme("door2door"); // default, otherwise "stopbased"
//			configDrt.setMaxTravelTimeAlpha( 1.3 );
//			configDrt.setMaxTravelTimeBeta( 5. * 60. );
//			configDrt.setStopDuration( 60. );
//			configDrt.setMaxWaitTime( Double.MAX_VALUE );
//			config.addModule(configDrt);
//		}
//
//		{// ***** dvrp Module ***** // Do we need this?
//			DvrpConfigGroup configDvrp = new DvrpConfigGroup() ;
//			configDvrp.setMode(TransportMode.drt); // Is this right?
//			configDvrp.setNetworkMode(TransportMode.car);
//			configDvrp.setTravelTimeEstimationAlpha(0.05);
//			config.addModule(configDvrp);
//		}
//
//		{// ***** Raptor Module - Intermodal Access / Egress *****
//			SwissRailRaptorConfigGroup configRaptor = new SwissRailRaptorConfigGroup();
//			configRaptor.setUseIntermodalAccessEgress(true);
//
//			// DRT
//			IntermodalAccessEgressParameterSet paramSetDRT = new IntermodalAccessEgressParameterSet();
//			paramSetDRT.setMode(TransportMode.drt);
//			paramSetDRT.setRadius(10000); // 10 km
//			paramSetDRT.setPersonFilterAttribute(null);
//			paramSetDRT.setStopFilterAttribute("DRTStation");
//			paramSetDRT.setStopFilterValue("true");
//			configRaptor.addIntermodalAccessEgress(paramSetDRT );
//
//			// Walk
//			IntermodalAccessEgressParameterSet paramSetWalk = new IntermodalAccessEgressParameterSet();
//			paramSetWalk.setMode(TransportMode.walk);
//			paramSetWalk.setRadius(2000); // 2 km
//			paramSetWalk.setPersonFilterAttribute(null);
//			paramSetWalk.setStopFilterAttribute(null);
//			configRaptor.addIntermodalAccessEgress(paramSetWalk );
//
//			config.addModule(configRaptor);
//		}
//
//		{
//			StrategyConfigGroup.StrategySettings stratSets = new StrategyConfigGroup.StrategySettings(  );
//
//			String name = DefaultPlanStrategiesModule.DefaultStrategy.SubtourModeChoice.toString();
//			stratSets.setStrategyName(name ) ;
//			stratSets.setWeight( 0.1 );
//			config.strategy().addStrategySettings( stratSets );
//			config.subtourModeChoice().setModes( new String [] {TransportMode.car, TransportMode.drt});
//			config.subtourModeChoice().setChainBasedModes(new String [] {TransportMode.car, TransportMode.drt}) ; // do we need this?
//		}
//
////		Controler controler = DrtControlerCreator.createControlerWithSingleModeDrt(fullConfig, otfvis);
//
//		new ConfigWriter(config).write(".\\scenarios\\berlin\\input\\config.xml");
//		Scenario scenario = ScenarioUtils.loadScenario(config);
//		org.matsim.core.controler.Controler controler = DrtControlerCreator.createControler(config, false);
//		controler.addOverridingModule(new VariableAccessTransitRouterModule());
//		controler.run();
//
//	}
//
//}
//
//
//// https://github.com/matsim-org/matsim/blob/master/contribs/drt/src/main/resources/drt_example/drtconfig_door2door.xml
