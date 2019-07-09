package main.population;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.StreamingPopulationReader;
import org.matsim.core.population.io.StreamingPopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class DownsamplePopulation {

    public static void main(String[] args) {

        String username = "jakob";
        String version = "2019-07-08";
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
        String inputPopFilename = rootPath + version + "/input/berlin-v5.4-1pct.plans.xml.gz";
        String outputPopFilename = rootPath + version + "/input/berlin-downsample.xml";


        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        StreamingPopulationWriter writer = new StreamingPopulationWriter(0.01);

        // the reader will read in an existing population file
        StreamingPopulationReader reader = new StreamingPopulationReader(scenario);
        reader.addAlgorithm(writer);

        try {
            writer.startStreaming(outputPopFilename);
            reader.readFile(inputPopFilename);
        } finally {
            writer.closeStreaming();
        }
    }
}
