package main.network;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
//import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
//import org.matsim.api.core.v01.population.Route;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;

public class AdjustPT {

	public static void main(String[] args) {
		
		
		Path transitSchedule = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\DRT\\PolicyCase\\Input_global\\berlin-v5-transit-schedule.xml.gz");
		Path newTransitSchedule = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\DRT\\PolicyCase\\Input_global\\berlin-v5-transit-schedule_Adjusted.xml.gz");
		
		Path networkPath = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\DRT\\PolicyCase\\Input_global\\berlin-v5-network.xml.gz");
		
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		// read in existing files
		new TransitScheduleReader(scenario).readFile(transitSchedule.toString());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath.toString());
		
		// initialize String variables with line and facility values
		
		Set<String> facilitiesToCancel = new HashSet<>(Arrays.asList("070101001048", "070101003092", "070101001049", "070101002080", "070101001050", "070101001050.1", "070101000435", "070101000436", "070101000449", "070101000450", "070101000451", "070101001718", "070101001046", "070101001047", "070101001048.1", "070101001515", "070101001516", "070101001516.1","070101000442", "070101000443", "070101000441", "070101000444", "070101000440", "070101000445", "070101000439", "070101000446"));
		Set<String> linksToCancel = new HashSet<>(Arrays.asList("pt_15964", "pt_15982", "pt_15965", "pt_15981", "pt_15966", "pt_15980", "pt_15967", "pt_15979", "pt_15968", "pt_15978", "pt_15969", "pt_15976", "pt_15977", "pt_12961", "pt_13074", "pt_12960", "pt_13075", "pt_12959", "pt_13076", "pt_12990", "pt_13077", "pt_12989", "pt_13078", "pt_12988", "pt_13079", "pt_13080", "pt_12986", "pt_12987", "pt_12988"));
		Set<String> linesToConsider = new HashSet<>(Arrays.asList("17306_700", "17354_700", "17476_700"));
		
		//execute adjustLine-method which adjusts each transit line
		for (String lineID: linesToConsider) {
				
			AdjustPT.adjustLine(scenario, lineID, facilitiesToCancel, linksToCancel);
			
		}
		
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(newTransitSchedule.toString());
		
		
	}
	
	
	public static void adjustLine(Scenario scenario, String lineID, Set<String> facilitiesToCancel, Set<String> linksToCancel) {
		
		// initialize transitLine
		TransitLine ptLine = scenario.getTransitSchedule().getTransitLines().get(Id.create(lineID, TransitLine.class));
		
		System.out.println(ptLine);
		
		List<TransitRoute> toRemove = ptLine.getRoutes().values().stream()
		.filter(route -> route.getStops().stream().anyMatch(stop -> {
			
			String stopId = stop.getStopFacility().getId().toString();
			return facilitiesToCancel.contains(stopId);
			
		}))
		.collect(Collectors.toList());
		
		List<TransitRoute> toAdd = toRemove.stream()
				.map(route -> copyRoute(scenario, route, facilitiesToCancel, linksToCancel))
				.collect(Collectors.toList());
		
		for(TransitRoute route: toRemove) {
			
			ptLine.removeRoute(route);
		}
		
		for(TransitRoute route: toAdd) {
			
			ptLine.addRoute(route);
		}
		
		
		
//		for(Iterator<TransitRoute> it = ptLine.getRoutes().values().iterator() ; it.hasNext();) {
//			TransitRoute route = it.next() ;
//				
//				for (TransitRouteStop stop: route.getStops()) {
//					
//					String stopId = stop.getStopFacility().getId().toString();
//					
//					if(facilitiesToCancel.contains(stopId)) {
//						
//						TransitRoute newRoute = AdjustPT.copyRoute(scenario, route, facilitiesToCancel, linksToCancel);
//						// Delete old route
//						it.remove();
//						// Add this route to ptLine
//						ptLine.addRoute(newRoute);
//						
////						ptLine.removeRoute(route);
//						
//						
//						
//						break;
//					}
//				}
//				
//				
//				
//
//			}
		
		
					
	}
			
	public static TransitRoute copyRoute(Scenario scenario, TransitRoute route, Set<String> facilitiesToCancel, Set<String> linksToCancel) {
		
		// createNewRoute

		List<Id<Link>> oldLinks = new ArrayList<>();
		
		oldLinks.add(route.getRoute().getStartLinkId());
		for(Id<Link> id: route.getRoute().getLinkIds()) {
			oldLinks.add(id);
		}
		oldLinks.add(route.getRoute().getEndLinkId());
		
		List<Id<Link>> newLinks = new ArrayList<Id<Link>>();
		
		List<TransitRouteStop> oldStops = route.getStops();
		List<TransitRouteStop> newStops = new ArrayList<TransitRouteStop>();
		
		for (TransitRouteStop stop: oldStops) {
			
			if (!facilitiesToCancel.contains(stop.getStopFacility().getId().toString())) {
			
				newStops.add(stop);
				
			}
		}
		
		//new method "Cancel no. of links as no. of Stops were cancelled
		//runs with error unfortunately

		
		int linkOffSet;
		
		//determine where to start and stop adding links based on oldStops
		if (oldStops.get(0) == newStops.get(0)) {
			
			// Case: Route is shortened in the end
			linkOffSet = 0;
			
		} else {
			
			// Case: Route is shortened at the beginning
			linkOffSet = (oldStops.size() - newStops.size());
		}
		
		
		for (int link = 0; link <= newStops.size() - 1; link++) {
			
			newLinks.add(oldLinks.get(link + linkOffSet));
			
		}
		
		
		// old method "Cancel if link is in list..."
		
//		for (Id<Link> link: oldLinks) {
//			
//			if (!linksToCancel.contains(link.toString())) {
//				
//				newLinks.add(link);
//			}
//		}
		
		
		TransitRoute newRoute = scenario.getTransitSchedule().getFactory().createTransitRoute(
				route.getId(),
				RouteUtils.createNetworkRoute(newLinks, scenario.getNetwork()),
				newStops,
				TransportMode.pt);
		
		return newRoute;
		
	}
	
			
	
}




