package main.network;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.io.IOUtils;
import org.opengis.feature.simple.SimpleFeature;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/** ExtractLInksWithinFrohnau checks the Network file, and finds all links that are within the bounds of Frohnau (as
 * defined by a .shp file) and all links that are outside of Frohnau. The respective lists of links are saved as .txt files.
 */
public class ExtractLinksWithinFrohnau {

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

        String networkInput = rootPath + "Input_global/berlin-v5-network.xml.gz" ;
        String filterShape = rootPath +"Frohnau/Frohnau-1.shp" ;
        String outputDirectory = rootPath + "Frohnau/";

        //Initialize Variables
        ArrayList<String> linksWithinFrohnau = new ArrayList<>() ;
        ArrayList<String> linksOutsideFrohnau = new ArrayList<>() ;
        int linksTotal = 0;

        // Read Network
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkInput);

        // Read Shapefile
        final Collection<Geometry> geometries = new ArrayList<>();
        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(filterShape)) {
            geometries.add((Geometry) feature.getDefaultGeometry());
        }

        // Find Links Within and Outside of Frohnau Shapefile
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

        // Print and Save Results
        System.out.println("LINKS --- inside:" + linksWithinFrohnau.size() + " --- outside: " + linksOutsideFrohnau.size() + " --- total: " + linksTotal);

        writeIdsToFile(linksWithinFrohnau, outputDirectory + "linksWithinFrohnau.txt");
        writeIdsToFile(linksOutsideFrohnau, outputDirectory + "linksOutsideFrohnau.txt");

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
