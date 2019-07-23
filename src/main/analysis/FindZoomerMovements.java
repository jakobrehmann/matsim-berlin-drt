package main.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.scenario.ScenarioUtils;

public class FindZoomerMovements {

	public static void main(String[] args) {
		
        String username = "david";
        String rootPath = null;
        String percent = "1";

        switch (username) {
            case "jakob":
                rootPath = "C:/Users/jakob/tubCloud/Shared/DRT/PolicyCase/";
                break;
            case "david":
                rootPath = "D:/Eigene Dateien/Dokumente/Uni/tubCloud/Master/02_SoSe2019/MatSim/DRT/PolicyCase/";
                break;
            default:
                System.out.println("Incorrect Base Path");
        }
        
        String inputPopFilename = rootPath + "2019-07-17/C-ZoomerNormalStrategyWeights/output/berlin-v5.4-" + percent + "pct.output_plans.xml.gz";
        
        // Read Population
        Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(sc).readFile(inputPopFilename);
        final Population pop = sc.getPopulation();
        
        for (Person person: pop.getPersons().values() ) {
        	
			Plan plan = person.getSelectedPlan();
            
            List<Leg> legsWithZoomerMode =  TripStructureUtils.getLegs(plan).stream()
            		.filter(leg -> leg.getMode().toString().contains("zoomer"))
            		.collect(Collectors.toList());
           
            //List<Coord> coordStartZoomer = new ArrayList<>();
            //List<Coord> coordEndZoomer = new ArrayList<>();

            List<PlanElement> elements = plan.getPlanElements();
            
            for (Leg leg: legsWithZoomerMode) {
            	
                for(elementIndex = 0;elements.size(); elementIndex++) {
                	
                	if(elements.get(elementIndex).equals(leg)) {
                		
                		
                	}
                			
                }
            	
            }


            
            
            
            for (Leg leg: legsWithZoomerMode) {
            	
            	//System.out.println(leg.getRoute().getStartLinkId());
            	
            }
            
        }
	}
}
