//package main.network;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.Set;
//
//import org.matsim.api.core.v01.Id;
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.core.config.Config;
//import org.matsim.core.config.ConfigUtils;
//import org.matsim.core.network.io.MatsimNetworkReader;
//import org.matsim.core.scenario.ScenarioUtils;
//import org.matsim.pt.transitSchedule.api.TransitLine;
//import org.matsim.pt.transitSchedule.api.TransitRoute;
//import org.matsim.pt.transitSchedule.api.TransitRouteStop;
//import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
//import org.matsim.pt.transitSchedule.api.TransitStopFacility;
//import org.matsim.vehicles.VehicleReaderV1;
//
//public class PTAdjustments {
//
//
//// Do links and nodes have to be deleted or just the schedule?
//
//// Links to delete..
//// Bus 220:
//// pt_15964, pt_15982, pt_15965, pt_15981, pt_15966, pt_15980, pt_15967, pt_15979, pt_15968, pt_15978, pt_15969, pt_15976, pt_15977
//
//// Bus 125:
//// pt_12961, pt_13074, pt_12960, pt_13075, pt_12959, pt_13076, pt_12990, pt_13077, pt_12989, pt_13078, pt_12988, pt_13079, pt_13080, pt_12986, pt_12987, pt_12988
//
//
//// nodes to delete..
//// Bus 220:
//// pt_070101000439, pt_070101000446, pt_070101000441, pt_070101000444, pt_070101000442, pt_070101000443, pt_070101001515, pt_070101001516, pt_070101003671
//
//// Bus 125:
//// pt_070101001047, pt_070101001048, pt_070101003092, pt_070101001046, pt_070101001049, pt_070101001718, pt_070101002080, pt_070101000451, pt_070101001050, pt_070101000435, pt_070101000450, pt_070101000436, pt_070101000449
//
//
////StopFacilities
//// Bus 125:
//// 070101001048, 070101003092, 070101001049, 070101002080, 070101001050, 070101001050.1, 070101000435, 070101000436, 070101000449, 070101000450, 070101000451, 070101001718, 070101001046, 070101001047, 070101001048.1
//
//// Transit Line to adjust
//// <transitLine id="17354_700"> => Bus 220
//// <transitLine id="17476_700"> => Bus N20
//
//// <transitLine id="17306_700"> => Bus 125
//
//	public static void main (String[] args) {
//
//		Path transitSchedule = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5-transit-schedule.xml.gz");
//		Path transitVehicles = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5-transit-vehicles.xml.gz");
//		Path networkPath = Paths.get("D:\\Eigene Dateien\\Dokumente\\Uni\\tubCloud\\Master\\02_SoSe2019\\MatSim\\Hausaufgabe2_David\\berlin-v5-network.xml.gz");
//
//		Config config = ConfigUtils.createConfig();
//		Scenario scenario = ScenarioUtils.createScenario(config);
//
//		// read in existing files
//		new TransitScheduleReader(scenario).readFile(transitSchedule.toString());
//		new VehicleReaderV1(scenario.getTransitVehicles()).readFile(transitVehicles.toString());
//		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkPath.toString());
//
//
//		// Declare and initialize array with all facilityIds Line 125 to be cancelled
//		String facilityIds125[] = {"070101001048", "070101003092", "070101001049", "070101002080", "070101001050", "070101001050.1", "070101000435", "070101000436", "070101000449", "070101000450", "070101000451", "070101001718", "070101001046", "070101001047", "070101001048.1"};
//
//		// Iterate through all facilities to remove
//		for (String facilityId : facilityIds125) {
//
//			// Request TransitStop facility
//			TransitStopFacility facility = scenario.getTransitSchedule().getFacilities().get(Id.create(facilityId, TransitStopFacility.class));
//
//			// Request TransitLine
//			TransitLine Bus125 = scenario.getTransitSchedule().getTransitLines().get(Id.create("17306_700",TransitLine.class));
//
//			Bus125.removeRoute(route)
//
//			// Remove stop of this facility in all existing routes
//			for (TransitRoute route : Bus125.getRoutes().values()) {
//				TransitRouteStop stop = route.getStop(facility);
//
//				String routeId = route.getId().toString();
//
//				// Not all routes contain all stop facilities, therefore exception handler is needed
//
//				if (stop != null){
//
//					route.getStops().remove(stop);
//
//
//			}
//		}
//
//
//
//	}
//
//}
