package main.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.Vehicle;

public class PTExtendedVehicleEventHandler implements PersonEntersVehicleEventHandler{

	private final Event enterVehicleEvent;
	
	PTExtendedVehicleEventHandler(Event enterVehicleEvent){
		
		this.enterVehicleEvent = enterVehicleEvent;
	}
	

	public TransitLine getTransitLine(Scenario scenario, PersonEntersVehicleEvent event) {
		// TODO Auto-generated method stub
		
		
		HashMap<Id<Vehicle>,TransitLine> vehicleOnTransitLine = new HashMap<>();
		
		TransitSchedule transitSchedule = scenario.getTransitSchedule();
		
		Set<String> linesToConsider = new HashSet<>(Arrays.asList("17306_700", "17354_700", "17476_700"));
		
		
		// Identify relevant vehicles
		for(String line: linesToConsider) {
			
			TransitLine lineToConsider = transitSchedule.getTransitLines().get(Id.create(line, TransitLine.class));
			
			for(TransitRoute route: lineToConsider.getRoutes().values()) {
				
				for(Departure departure: route.getDepartures().values()) {
					
					vehicleOnTransitLine.put(departure.getVehicleId(), lineToConsider);
				}
			}
		}
		
		return vehicleOnTransitLine.get(event.getVehicleId());
		
	}
	
	public boolean isPTVehicle(PersonEntersVehicleEvent event) {
		// TODO Auto-generated method stub
		
		if(event.getVehicleId().toString().startsWith("tr")) {
			
			return true;
		}
		
		return false;
	}


	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		// TODO Auto-generated method stub
		
	}

}
