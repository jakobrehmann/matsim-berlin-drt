package main.population;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
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

/** DownsamplePopulationFrohnau analyses the plans of our population, and finds the agents who have at least one activity
 * (in selected plan) that occurs within the bounds of Frohnau, as defined by a .shp file. The output is a new population,
 * composed entirely of people who have activities within Frohnau.
 */

public class DownsamplePopulationFrohnau {

    // Set Paths
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
            String inputPopFilename = rootPath + "Input_global/plans/berlin-plans-10pct-original.xml.gz";
            String outputPopFilename = rootPath  + "Input_global/plans/berlin-plans-10pct-frohnau.xml.gz";
            String AgentsWithinFrohnauFilename = rootPath + "Input_global/agentsWithinFrohnau.txt";
            String AgentsOutsideFrohnauFilename = rootPath + "Input_global/agentsOutsideFrohnau.txt";
            String filterShape = rootPath + "Frohnau/Frohnau-1.shp" ;

            // Initialize Variables
            ArrayList<String> agentsWithinFrohnau = new ArrayList<>() ;
            ArrayList<String> agentsOutsideFrohnau = new ArrayList<>() ;
            int agentsTotal = 0;

            // Read Population
            Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            new PopulationReader(sc).readFile(inputPopFilename);
            final Population pop = sc.getPopulation();

            // New Population
            Scenario sc2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            Population pop2 = sc2.getPopulation() ;

            // Read Shapefile
            final Collection<Geometry> geometries = new ArrayList<>();
            for (SimpleFeature feature : ShapeFileReader.getAllFeatures(filterShape) ){
                geometries.add((Geometry) feature.getDefaultGeometry());
            }

            // Find Agents with activities within Borders of Frohnau
            for ( Person person : pop.getPersons().values() ) {
                agentsTotal++;
                Plan plan = person.getSelectedPlan() ;
                agentsOutsideFrohnau.add(person.getId().toString());
                for ( Activity activity : TripStructureUtils.getActivities(plan, null)) {
                    Coord coord = activity.getCoord();
                    if (geometries.stream().anyMatch(geom -> geom.contains(MGC.coord2Point(coord)))) {
                        agentsOutsideFrohnau.remove(person.getId().toString());
                        agentsWithinFrohnau.add(person.getId().toString());
                        pop2.addPerson(person);
                        break;
                    }

                }}


            // Print and Save Results
            System.out.println("AGENTS --- inside:" + agentsWithinFrohnau.size() + " --- outside: " + agentsOutsideFrohnau.size() + " --- total: " + agentsTotal);

            writeIdsToFile(agentsWithinFrohnau, AgentsWithinFrohnauFilename);
            writeIdsToFile(agentsOutsideFrohnau, AgentsOutsideFrohnauFilename);

            new PopulationWriter(sc2.getPopulation()).write(outputPopFilename);

        }

        static void writeIdsToFile(ArrayList<String> linkIds, String outputFile){
            BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
            try {
                for (int i = 0;i< linkIds.size();i++){
                    bw.write(linkIds.get(i));
                    bw.newLine();
                }
                bw.flush();
                bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
