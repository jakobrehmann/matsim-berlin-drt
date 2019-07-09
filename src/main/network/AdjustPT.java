package main.network;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.VehicleReaderV1;

import jogamp.graph.font.typecast.ot.table.ID;

public class AdjustPT {

	public static void main(String[] args) {
		
		Path transitSchedule = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5-transit-schedule.xml.gz");
		Path transitVehicles = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5-transit-vehicles.xml.gz");
		Path networkPath = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5-network.xml.gz");

		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		// read in existing files
		new TransitScheduleReader(scenario).readFile(transitSchedule.toString());
		new VehicleReaderV1(scenario.getTransitVehicles()).readFile(transitVehicles.toString());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath.toString());
		
		// initialize String variables with line and facility values
		
		String facilityIDsToCancel[] = {"070101001048", "070101003092"};
		String linesToConsider[] = {"Bus120","Bus125","BusN20"};		
		
		//execute adjustLine-method which adjusts each transit line
		for (String lineID: linesToConsider) {
				
			AdjustPT.adjustLine(scenario, lineID, facilityIDsToCancel);
			
		}
		
		
		
	}
	
	
	public static void adjustLine(Scenario scenario, String lineID, String facilityIDsToCancel[]) {
		
		// initialize transitLine and transitStopFacility
		TransitLine ptLine = scenario.getTransitSchedule().getTransitLines().get(Id.create(lineID, TransitLine.class));			
		TransitStopFacility[] tsFacilities = new TransitStopFacility[facilityIDsToCancel.length];
		
		for (int x = 0; x<=facilityIDsToCancel.length-1;x++) {
			
			String facilityID = facilityIDsToCancel[x];
			tsFacilities[x] = scenario.getTransitSchedule().getFacilities().get(Id.create(facilityID, TransitStopFacility.class));
			
		}
		
		for(TransitRoute route: ptLine.getRoutes().values()) {
				
				AdjustPT.adjustRoute(scenario, ptLine, route, tsFacilities);	
		}
			
		
		
	}
	


	public static void adjustRoute(Scenario scenario, TransitLine ptLine, TransitRoute route, TransitStopFacility[] tsFacilities) {
		
		
		NetworkRoute newRoutePattern;
		List<TransitRouteStop> newStops;
		
		//Read-in old route
		
		
		List<Id<Link>> routeLinkIds = route.getRoute().getLinkIds();
		
		//delete links that either contain a transitstopFacility as starting or end point
		
		scenario.getNetwork().getLinks().get
		
		
		int i = routeLinkIds.size();
		for () {
		
			for (int x = 0; x<= i; x++) {
				
				routeLinkIds.remove(i);
				i = i-1;				

			}
		
		
		// createNewRoute
		
		
		TransitRoute newRoute = scenario.getTransitSchedule().getFactory().createTransitRoute(
				Id.create(route.getId().toString() + "_adjustedAutomatically", TransitRoute.class),
				RouteUtils.createNetworkRoute(routeLinkIds, scenario.getNetwork()),
				newStops,
				TransportMode.pt);
		
		// Add this route to ptLine
		ptLine.addRoute(newRoute);
		
		// Delete old route
		ptLine.removeRoute(route);
		
		
	}
	
			
	
}




