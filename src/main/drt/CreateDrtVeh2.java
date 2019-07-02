//package org.matsim.maas.utils;
//
//import org.matsim.api.core.v01.Id;
//import org.matsim.api.core.v01.Scenario;
//import org.matsim.api.core.v01.TransportMode;
//import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
//import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
//import org.matsim.contrib.dvrp.fleet.FleetWriter;
//import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
//import org.matsim.core.config.ConfigUtils;
//import org.matsim.core.network.io.MatsimNetworkReader;
//import org.matsim.core.scenario.ScenarioUtils;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Random;
//import java.util.UUID;
//import java.util.stream.Stream;
//
///**
// * @author jbischoff
// * This is an example script to create a vehicle file for taxis, SAV or DRTs.
// * The vehicles are distributed randomly in the network.
// */
//public class CreateDrtVeh2 {
//
//    private static final int numberOfVehicles = 1500;
//    private static final int seatsPerVehicle = 6;
//    private static final double operationStartTime = 0;
//    private static final double operationEndTime = 24 * 60 * 60; //24h
//
//    private static final Random random = new Random(0);
//
//    private static final Path networkFile = Paths.get("path/to/your/network.xml.gz");
//    private static final Path outputFile = Paths.get("path/to/your/outputfile.xml.gz");
//
//    public static void main(String[] args) {
//
//        new CreateFleetVehicles().run();
//    }
//
//    private void run() {
//
//        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
//        new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile.toString());
//        Stream<DvrpVehicleSpecification> vehicleSpecificationStream = scenario.getNetwork().getLinks().entrySet().stream()
//                .filter(entry -> entry.getValue().getAllowedModes().contains(TransportMode.car)) // drt can only start on links with Transport mode 'car'
//                .sorted((e1, e2) -> (random.nextInt(2) - 1)) // shuffle links
//                .limit(numberOfVehicles) // select the first *numberOfVehicles* links
//                .map(entry -> ImmutableDvrpVehicleSpecification.newBuilder()
//                        .id(Id.create("drt_" + UUID.randomUUID().toString(), DvrpVehicle.class))
//                        .startLinkId(entry.getKey())
//                        .capacity(seatsPerVehicle)
//                        .serviceBeginTime(operationStartTime)
//                        .serviceEndTime(operationEndTime)
//                        .build());
//
//        new FleetWriter(vehicleSpecificationStream).write(outputFile.toString());
//    }
//
//}