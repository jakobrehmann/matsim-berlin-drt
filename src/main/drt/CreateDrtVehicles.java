package main.drt;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.DvrpVehicleSpecification;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;

import java.util.ArrayList;
import java.util.List;


public class CreateDrtVehicles {
    static void createDrtVehiclesFile( String taxisFile, String vehPrefix, int numberofVehicles, Id<Link> startLinkId ) {
        List<DvrpVehicleSpecification> vehicles = new ArrayList<>();
        for (int i = 0; i< numberofVehicles;i++){
            //for multi-modal networks: Only links where drts can ride should be used.
            DvrpVehicleSpecification v = ImmutableDvrpVehicleSpecification.newBuilder()
                    .id(Id.create(vehPrefix + i, DvrpVehicle.class))
                    .startLinkId(startLinkId)
                    .capacity(4)
                    .serviceBeginTime(0)
                    .serviceEndTime(36*3600)
                    .build();
            vehicles.add(v);
        }
        new FleetWriter(vehicles.stream()).write(taxisFile);
    }
}