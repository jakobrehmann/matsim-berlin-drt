package main.network;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/** ExtractLInksWithinFrohnau checks the Network file, and finds all links that are within the bounds of Frohnau (as
 * defined by a .shp file) and all links that are outside of Frohnau. The respective lists of links are saved as .txt files.
 */
public class ExtractFacilitiesWithinFrohnau {

    public static void main(String[] args) {

        // Set Paths
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

//        String networkInput = rootPath + "Input_global/berlin-v5-network.xml.gz" ;
        String transitSchedInput = rootPath + "Input_global/berlin-v5-transit-schedule_Adjusted.xml.gz" ;
        String filterShape = rootPath +"Frohnau/Frohnau-1.shp" ;
        String outputDirectory = rootPath + "Frohnau/";

        //Initialize Variables
        ArrayList<String> facilitiesWithinFrohnau = new ArrayList<>() ;

        int facilitiesTotal = 0;

        // Read Network

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        // read in existing files
        new TransitScheduleReader(scenario).readFile(transitSchedInput);

        // Read Shapefile
        final Collection<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(filterShape)) {
            geometries.add((Geometry) feature.getDefaultGeometry());
        }

        // Find Facilities Within Frohnau Shapefile
        for(TransitStopFacility i : scenario.getTransitSchedule().getFacilities().values()) {
            facilitiesTotal++ ;
            Coord coord = i.getCoord() ;
            if (geometries.stream().anyMatch(geom -> geom.contains(MGC.coord2Point(coord)))){
                facilitiesWithinFrohnau.add(i.getId().toString());
            }
        }

        // Print and Save Results
        System.out.println("LINKS --- inside:" + facilitiesWithinFrohnau.size() + " --- total: " + facilitiesTotal);

        writeIdsToFile(facilitiesWithinFrohnau, outputDirectory + "facilitiesWithinFrohnau.txt");


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
