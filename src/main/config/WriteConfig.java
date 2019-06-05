package main.config;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup.IntermodalAccessEgressParameterSet;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.run.Controler;


public class WriteConfig {

	public static void main(String[] args) {
		
		
		
		SwissRailRaptorConfigGroup config = new SwissRailRaptorConfigGroup();

		
		config.setUseIntermodalAccessEgress(true);
		// DRT
		IntermodalAccessEgressParameterSet paramSetDRT = new IntermodalAccessEgressParameterSet();
		paramSetDRT.setMode(TransportMode.drt);
		paramSetDRT.setRadius(10000); // 10 km
		paramSetDRT.setPersonFilterAttribute(null);
		paramSetDRT.setStopFilterAttribute("DRTStation");
		paramSetDRT.setStopFilterValue("true");
		config.addIntermodalAccessEgress(paramSetDRT );
		
		// Walk
		IntermodalAccessEgressParameterSet paramSetWalk = new IntermodalAccessEgressParameterSet();
		paramSetWalk.setMode(TransportMode.walk);
		paramSetWalk.setRadius(2000); // 2 km
		paramSetWalk.setPersonFilterAttribute(null);
		paramSetWalk.setStopFilterAttribute(null);
		config.addIntermodalAccessEgress(paramSetWalk );
		
		
		
		Config fullConfig = ConfigUtils.createConfig(config);
		
//		Controler controler = DrtControlerCreator.createControlerWithSingleModeDrt(fullConfig, otfvis);
		
		new ConfigWriter(fullConfig).write(".\\scenarios\\berlin\\input\\config.xml");

	}

}
