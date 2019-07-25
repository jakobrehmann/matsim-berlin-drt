package main.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.core.mobsim.qsim.pt.TransitVehicle;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.Vehicle;

// Goal: nPersons use line in Base Case.
// nPersons who enter or leave bus within Frohnau
public class PTExtendedVehicleEventHandler implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler {


	Set<Id<Vehicle>> vehSave = new HashSet<>();

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

        return event.getVehicleId().toString().startsWith("tr");

    }


	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void handleEvent(PersonLeavesVehicleEvent personLeavesVehicleEvent) {

	}


	// adds vehicle to buffer if 1) stop occurs in frohnau and 2) vehicle is one of the pt vehicles from the bus lines in question.
	@Override
	public void handleEvent(VehicleArrivesAtFacilityEvent vehicleArrivesAtFacilityEvent) {
		Id<Vehicle> vehId = vehicleArrivesAtFacilityEvent.getVehicleId();
		// if...

		vehSave.add(vehId);

	}

	@Override
	public void handleEvent(VehicleDepartsAtFacilityEvent vehicleDepartsAtFacilityEvent) {
		Id<Vehicle> vehId = vehicleDepartsAtFacilityEvent.getVehicleId();
        vehSave.remove(vehId);
	}
}
