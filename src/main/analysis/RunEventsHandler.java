/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package main.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class RunEventsHandler {
//	static Path inputNetwork = Paths.get("C:\\Users\\jakob\\Dropbox\\Documents\\Education-TUB\\2019_SS\\MATSim\\HA1\\input\\be_5_network_with-pt-ride-freight.xml") ;
//	static Path inputEventsBase = Paths.get("C:\\Users\\jakob\\Dropbox\\Documents\\Education-TUB\\2019_SS\\MATSim\\HA1\\web_output\\berlin-v5.3-1pct.output_events.xml.gz");
//	static Path inputEventsTempo30 = Paths.get("C:\\Users\\jakob\\Dropbox\\Documents\\Education-TUB\\2019_SS\\MATSim\\HA1\\tempo30Case\\output\\ITERS\\it.200\\berlin-v5.3-1pct.200.events.xml.gz");
//	static Path outputCarDistBase = Paths.get(".\\output\\carDistancesBase.txt");
//	static Path outputCarDistTempo30 = Paths.get(".\\output\\carDistancesTempo30.txt");
//	static Path LinksWithinRing = Paths.get(".\\output\\LinksWithinRing.txt");
//	private static Path VehWithinRingBase = Paths.get(".\\output\\VehWithinRingBase.txt");
//	private static Path VehWithinRingTempo30 = Paths.get(".\\output\\VehWithinRingTempo30.txt");
	
	public static void main(String[] args) {

		String username = "jakob";
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

		String eventsPolicy = rootPath + "2019-07-23/05_FullRun/output/berlin-v5.4-10pct.output_events.xml.gz" ;
		String eventsBase = rootPath + "Input_global/events/berlin-v5.3-10pct.output_events.xml.gz" ; // this needs to be reduced to just agents in Frohnau
		String inputNetwork = rootPath + "Input_global/berlin-v5-network.xml.gz" ;
		String agentsWithinFrohnauFilename = rootPath + "Input_Global/agents-10pct-InsideFrohnau.txt" ;
//		ArrayList<String> linksWithinRing = MyUtils.readLinksFile(LinksWithinRing.toString()) ;
//		ArrayList<String> vehWithinRing = MyUtils.readLinksFile(VehWithinRingBase.toString()) ;

		//Initialization
		EventsManager managerBase = EventsUtils.createEventsManager();
		new MatsimEventsReader(managerBase).readFile(eventsBase);
		EventsManager managerPolicy = EventsUtils.createEventsManager();
		new MatsimEventsReader(managerPolicy).readFile(eventsPolicy);


		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(inputNetwork);

		// Travel Time
		{
			TravelTimeEventHandler timeHandlerBase = new TravelTimeEventHandler() ;
			timeHandlerBase.setFrohnauAgents(agentsWithinFrohnauFilename);
			managerBase.addHandler(timeHandlerBase);

			double totalTravelTimeBase = timeHandlerBase.computeOverallTravelTime() ;
			System.out.println("time (base) = " + totalTravelTimeBase/3600 + " hours") ;

			TravelTimeEventHandler timeHandlerPolicy = new TravelTimeEventHandler() ;
			timeHandlerPolicy.setFrohnauAgents(agentsWithinFrohnauFilename);
			managerPolicy.addHandler(timeHandlerPolicy);

			double totalTravelTimePolicy = timeHandlerPolicy.computeOverallTravelTime() ;
			System.out.println("time (policy) = " + totalTravelTimePolicy/3600 + " hours") ;

//			// For Subpopulation
//			Map<Id<Person>, Double> travelTimeByAgent = timeHandler.getTravelTimeByAgent() ;
//			double subPopTravelTimeTotal = 0. ;
//
//			for (Id<Person> per : travelTimeByAgent.keySet()) {
//				if (vehWithinRing.contains(per.toString())) {
//					subPopTravelTimeTotal += travelTimeByAgent.get(per);
//				}
//			}
//			System.out.println("Subpop time = " + subPopTravelTimeTotal/3600 + " hours") ;

		}
		// Car Distance Evaluator 
//		{
//			CarTravelDistanceEvaluator carTravelDistanceEvaluator = new CarTravelDistanceEvaluator(scenario.getNetwork(), linksWithinRing);
//			manager.addHandler(carTravelDistanceEvaluator);
//			new MatsimEventsReader(manager).readFile(inputEvents);
//			MyUtils.writeDistancesToFile(carTravelDistanceEvaluator.getDistanceDistribution(), outputCarDistTempo30.toString());
//			System.out.print(carTravelDistanceEvaluator.getTravelledDistanceTotal());
//		}
		
	
//		 If cars cross into inner city
//		{
//			CityCenterEventEnterHandler cityCenterEventEnterHandler = new CityCenterEventEnterHandler();
//
//			for (String link : linksWithinRing) { //add city center links
//				System.out.println();
//				cityCenterEventEnterHandler.addLinkId(Id.createLinkId(link));
//			}
//			manager.addHandler(cityCenterEventEnterHandler);
//
//			System.out.println(cityCenterEventEnterHandler.getVehiclesInCityCenter().size());
//			System.out.println("Events file read!");
//			MyUtils.writeArrayListVehicle(cityCenterEventEnterHandler.getVehiclesInCityCenter(), VehWithinRingTempo30.toString() ) ;
//		}
			


	}

}
