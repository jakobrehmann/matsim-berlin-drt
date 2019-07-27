package main.analysis;

import java.util.*;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.VehicleDepartsAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.api.experimental.events.handler.VehicleDepartsAtFacilityEventHandler;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.Vehicle;

// Goal: nPersons use line in Base Case.
// nPersons who enter or leave bus within Frohnau
public class PTExtendedVehicleEventHandler implements PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler, VehicleArrivesAtFacilityEventHandler, VehicleDepartsAtFacilityEventHandler {
	Set<String> vehSave ;
	Set<String> facilitiesToCancel ;
	ArrayList<Id<Person>> impactedAgents ;
	int nImpactedAgents ;

	PTExtendedVehicleEventHandler() {
		this.vehSave = new HashSet<>();
		this.facilitiesToCancel = new HashSet<>(Arrays.asList("070101001048", "070101003092", "070101001049", "070101002080", "070101001050", "070101001050.1", "070101000435", "070101000436", "070101000449", "070101000450", "070101000451", "070101001718", "070101001046", "070101001047", "070101001048.1", "070101001515", "070101001516", "070101001516.1", "070101000442", "070101000443", "070101000441", "070101000444", "070101000440", "070101000445", "070101000439", "070101000446", "070101003671"));
		this.impactedAgents = new ArrayList<>();
		this.nImpactedAgents = 0;
	}

	public ArrayList<Id<Person>> getImpactedAgents() {
		return impactedAgents;
	}

	public Set<String> getVehSave() {
		return vehSave;
	}

	public int getnImpactedAgents() {
		return nImpactedAgents;
	}

	@Override
	public void handleEvent(VehicleArrivesAtFacilityEvent vehicleArrivesAtFacilityEvent) {
		String vehId = vehicleArrivesAtFacilityEvent.getVehicleId().toString();
		if (facilitiesToCancel.contains(vehicleArrivesAtFacilityEvent.getFacilityId().toString())) {
			vehSave.add(vehId);
		}
	}

	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		if (vehSave.contains(event.getVehicleId().toString())) {
			impactedAgents.add(event.getPersonId());
			nImpactedAgents++;
		}

	}


	@Override
	public void handleEvent(PersonLeavesVehicleEvent personLeavesVehicleEvent) {
		if (vehSave.contains(personLeavesVehicleEvent.getVehicleId().toString())) {
			impactedAgents.add(personLeavesVehicleEvent.getPersonId());
			nImpactedAgents++;
		}
	}


	@Override
	public void handleEvent(VehicleDepartsAtFacilityEvent vehicleDepartsAtFacilityEvent) {
		String vehId = vehicleDepartsAtFacilityEvent.getVehicleId().toString();
		vehSave.remove(vehId);
	}


	// ------------------ Unnecessary -------------------------

	@Deprecated
	public TransitLine getTransitLine(Scenario scenario, PersonEntersVehicleEvent event) {
		// TODO Auto-generated method stub


		HashMap<Id<Vehicle>, TransitLine> vehicleOnTransitLine = new HashMap<>();

		TransitSchedule transitSchedule = scenario.getTransitSchedule();

		Set<String> linesToConsider = new HashSet<>(Arrays.asList("17306_700", "17354_700", "17476_700"));


		// Identify relevant vehicles
		for (String line : linesToConsider) {

			TransitLine lineToConsider = transitSchedule.getTransitLines().get(Id.create(line, TransitLine.class));

			for (TransitRoute route : lineToConsider.getRoutes().values()) {

				for (Departure departure : route.getDepartures().values()) {

					vehicleOnTransitLine.put(departure.getVehicleId(), lineToConsider);
				}
			}
		}

		return vehicleOnTransitLine.get(event.getVehicleId());

	}

	@Deprecated
	public boolean isPTVehicle(PersonEntersVehicleEvent event) {
		// TODO Auto-generated method stub

		return event.getVehicleId().toString().startsWith("tr");

	}
}
