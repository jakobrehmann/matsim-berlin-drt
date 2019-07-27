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

import main.utils.MyUtils;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunEventsHandler {
	
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

		//Initialize Managers
		EventsManager managerBase = EventsUtils.createEventsManager();
		EventsManager managerPolicy = EventsUtils.createEventsManager();
//
//
//		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
//		new MatsimNetworkReader(scenario.getNetwork()).readFile(inputNetwork);



		// Impacted Bus Agents
		PTExtendedVehicleEventHandler ptHandler = new PTExtendedVehicleEventHandler();
		managerBase.addHandler(ptHandler);

		// Travel Time

		TravelTimeEventHandler timeHandlerBase = new TravelTimeEventHandler() ;
		timeHandlerBase.setFrohnauAgents(agentsWithinFrohnauFilename);
		managerBase.addHandler(timeHandlerBase);

		TravelTimeEventHandler timeHandlerPolicy = new TravelTimeEventHandler() ;
		timeHandlerPolicy.setFrohnauAgents(agentsWithinFrohnauFilename);
		managerPolicy.addHandler(timeHandlerPolicy);



		// Run Event Handlers
		{
				new MatsimEventsReader(managerBase).readFile(eventsBase);
//				new MatsimEventsReader(managerPolicy).readFile(eventsPolicy);
		}



		// Subpopulation
		ArrayList<Id<Person>> impactedAgents = ptHandler.getImpactedAgents();
		List<String> impactedAgentsString = impactedAgents.stream().map(x -> x.toString()).collect(Collectors.toList());
		MyUtils.writeArrayListString(impactedAgentsString,rootPath+ "/analysis/impactedAgents.txt");
		int nImpactedAgents = ptHandler.getnImpactedAgents();

		System.out.println(" Number of Impacted Agents: " + nImpactedAgents);
		System.out.println(" Size of Impacted Agents Set: " + impactedAgents.size());
		System.out.println(" Veh Save Array Size: " + ptHandler.getVehSave().size());


		// Travel Time
		double totalTravelTimeBase = timeHandlerBase.computeOverallTravelTime() ;
		System.out.println("time (base) = " + totalTravelTimeBase/3600 + " hours") ;

		double totalTravelTimePolicy = timeHandlerPolicy.computeOverallTravelTime() ;
		System.out.println("time (policy) = " + totalTravelTimePolicy/3600 + " hours") ;

		Map<Id<Person>, Double> TravelTimesBase = timeHandlerBase.getTravelTimeByAgent();
		Map<Id<Person>, Double> TravelTimesPolicy = timeHandlerPolicy.getTravelTimeByAgent();

		double subpopTotalTTBase = TravelTimesBase.entrySet().stream()
				.filter(x -> impactedAgents.contains(x.getKey()))
				.mapToDouble(d -> d.getValue()).sum();
		System.out.println("time subpop (base) = " + subpopTotalTTBase/3600.);


		double subpopTotalTTPolicy = TravelTimesPolicy.entrySet().stream()
				.filter(x -> impactedAgents.contains(x.getKey()))
				.mapToDouble(d -> d.getValue()).sum();
		System.out.println("time subpop (policy) = " + subpopTotalTTPolicy/3600.);





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
