package main.network;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.IOUtils;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class FrohnauShapeFilter {
    static Path networkInput = Paths.get("C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-05\\input\\berlin-v5-network.xml.gz") ;
    static Path populationInput = Paths.get("C:\\Users\\jakob\\tubCloud\\Shared\\DRT\\PolicyCase\\2019-07-05\\input\\berlin-v5.4-1pct.plans.xml.gz") ;
    private static Path filterShape = Paths.get("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/Frohnau/Frohnau-1.shp") ;
    private static Path outputDirectory = Paths.get("C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/Frohnau");
    static ArrayList<String> linksWithinFrohnau = new ArrayList<>() ;
    static ArrayList<String> linksOutsideFrohnau = new ArrayList<>() ;
    static ArrayList<String> agentsWithinFrohnau = new ArrayList<>() ;
    static ArrayList<String> agentsOutsideFrohnau = new ArrayList<>() ;
    static int agentsTotal = 0;
    static  int linksTotal = 0;


    public static void main(String[] args) {

        // Read Network
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkInput.toString());

        // Read Population
        Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(sc).readFile(populationInput.toString());
        final Population pop = sc.getPopulation();


        // Read Shapefile
        final Collection<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(filterShape.toString())) {
            geometries.add((Geometry) feature.getDefaultGeometry());
        }


        // Get Links in Frohnau
        for(Link i : network.getLinks().values()) {
            linksTotal++ ;
            Coord coord = i.getCoord() ;
            Set<String> modes = i.getAllowedModes() ;
            if ((geometries.stream().anyMatch(geom -> geom.contains(MGC.coord2Point(coord)))) && (modes.contains("car"))){
                linksWithinFrohnau.add(i.getId().toString());
            }
            else if ((modes.contains("car"))){
                linksOutsideFrohnau.add(i.getId().toString());
            }
        }

        // Get Agents in Frohnau

        for ( Person person : pop.getPersons().values() ) {
            agentsTotal++;
            Plan plan = person.getSelectedPlan() ;
            agentsOutsideFrohnau.add(person.getId().toString());
            for ( Activity activity : TripStructureUtils.getActivities(plan, null)) {
                Coord coord = activity.getCoord();
                if (geometries.stream().anyMatch(geom -> geom.contains(MGC.coord2Point(coord)))) {
                    agentsOutsideFrohnau.remove(person.getId().toString());
                    agentsWithinFrohnau.add(person.getId().toString());
                    break;
                }

            }}

        System.out.println("LINKS --- inside:" + linksWithinFrohnau.size() + " --- outside: " + linksOutsideFrohnau.size() + " --- total: " + linksTotal);
        System.out.println("AGENTS --- inside:" + agentsWithinFrohnau.size() + " --- outside: " + agentsOutsideFrohnau.size() + " --- total: " + agentsTotal);

        writeIdsToFile(linksWithinFrohnau, outputDirectory.toString() + "/linksWithinFrohnau.txt");
        writeIdsToFile(linksOutsideFrohnau, outputDirectory.toString() + "/linksOutsideFrohnau.txt");
        writeIdsToFile(agentsWithinFrohnau, outputDirectory.toString() + "/agentsWithinFrohnau.txt");
        writeIdsToFile(agentsOutsideFrohnau, outputDirectory.toString() + "/agentsOutsideFrohnau.txt");

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
