package main.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.population.PopulationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TravelTimeEventHandler
		implements ActivityEndEventHandler, ActivityStartEventHandler {

	private final Map<Id<Person>, Double> travelTimebyAgent = new HashMap<>();
	private final Map<Id<Person>, ActivityEndEvent> openTrips = new HashMap<>();
	private final ArrayList<Id<Person>> frohnauAgents = new ArrayList<>();

	Map<Id<Person>, Double> getTravelTimeByAgent() {
		return travelTimebyAgent;
	}

	double computeOverallTravelTime() {
		return
				travelTimebyAgent.values().stream().mapToDouble(d -> d).sum();
	}

	public void setFrohnauAgents(String path) {

			Scanner s ;
//			ArrayList<String> list = new ArrayList<String>();
			try {
				s = new Scanner(new File(path));
				while (s.hasNext()){
					frohnauAgents.add(Id.createPersonId(s.next()));
				}
				s.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}



		}

	@Override

	public void handleEvent(ActivityStartEvent event) {
		if (openTrips.containsKey(event.getPersonId()) && isNotInteraction(event.getActType())) {
			ActivityEndEvent endEvent = openTrips.remove(event.getPersonId());
			double travelTime = event.getTime() - endEvent.getTime();
			travelTimebyAgent.merge(event.getPersonId(), travelTime, (a, b) -> a + b);
		}
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		if ((isNotInteraction(event.getActType())) && (frohnauAgents.contains(event.getPersonId()))) {
			openTrips.put(event.getPersonId(), event);
		}
	}

	private boolean isNotInteraction(String activityType) {
		return !activityType.contains("interaction");
	}

}
