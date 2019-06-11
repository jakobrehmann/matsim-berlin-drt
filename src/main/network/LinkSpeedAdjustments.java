package main.network;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.matsim.analysis.VolumesAnalyzer;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

public class LinkSpeedAdjustments {
	
	public static void main(String[] args) {
		
		// set network path 
		Path networkpath = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5-network.xml.gz");
		Path baseCaseEventsPath = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5.3-1pct.output_events.xml.gz");
		
		// create network object and parse network
		Network network = NetworkUtils.createNetwork();
		new MatsimNetworkReader(network).readFile(networkpath.toString());
		
		VolumesAnalyzer volumesAnalyzer = new VolumesAnalyzer(3600, 36000, network);		
		
		EventsManager baseCaseManager = EventsUtils.createEventsManager();
		new MatsimEventsReader(baseCaseManager).readFile(baseCaseEventsPath.toString());
			
		baseCaseManager.addHandler(volumesAnalyzer);
		
		HashMap<Link, linkSet> linkMap = new HashMap();
		
		int key = 0;
		
		for (Link link : network.getLinks().values()) {
					
			int linkVolume[] = volumesAnalyzer.getVolumesForLink(link.getId());
			
			
		}
		
		
	}

}
