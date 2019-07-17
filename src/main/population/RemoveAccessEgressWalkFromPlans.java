package main.population;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.IOUtils;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/** RemoveAccessEgressWalkFromPlans takes a population file as input, and replaces all leg modes of "access_walk" and
 * "egress_walk" with "non_network_walk". "non_network_walk" is a new addition to MATSim, and still causes some bugs.
 */

public class RemoveAccessEgressWalkFromPlans {
        public static void main(String[] args) {

            String username = "jakob";
            String rootPath = null;

            switch (username) {
                case "jakob":
                    rootPath = "C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/";
                    break;
                case "david":
                    rootPath = "C:/Users/david/ENTER_PATH_HERE";
                    break;
                default:
                    System.out.println("Incorrect Base Path");
            }
            String inputPopFilename = rootPath + "Input_global/Berlin-plans-Frohnau.xml";
            String outputPopFilename = rootPath + "Input_global/Berlin-plans-Frohnau-scrubbed.xml";

            // Read Population
            Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            new PopulationReader(sc).readFile(inputPopFilename);
            final Population pop = sc.getPopulation();

            // New Population
            Scenario sc2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            Population pop2 = sc2.getPopulation() ;

            // Find Agents with activities within Borders of Frohnau
            for ( Person person : pop.getPersons().values() ) {
                for (Plan plan : person.getPlans()){
                    for (Leg leg : TripStructureUtils.getLegs(plan)) {
                        if ((leg.getMode().equals("access_walk")) || (leg.getMode().equals("egress_walk"))) {
                            leg.setMode(TransportMode.non_network_walk);
                        }
                    }
                }

            }

            new PopulationWriter(sc.getPopulation()).write(outputPopFilename);

        }
}
