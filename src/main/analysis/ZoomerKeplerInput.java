package main.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;

import com.opencsv.CSVWriter;

public class ZoomerKeplerInput {

	public static void main(String[] args) {
		
		//rootPath settings
		
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
        
        //Set paths for input and csvOutput
        
        String inputPopFilename = rootPath + "2019-07-17/C-ZoomerNormalStrategyWeights/output/berlin-v5.4-" + percent + "pct.output_plans.xml.gz";
        String csvOutputPath = rootPath + "Report_global/drtLinkShapes.csv";
        
        //create new file object
        File file = new File(csvOutputPath);
        

       
        // Read Population
        Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(sc).readFile(inputPopFilename);
        final Population pop = sc.getPopulation();
        
 //       GeotoolsTransformation transformation = new GeotoolsTransformation("GK4", "WGS84");
        
        //start writing csvfile
        try {
        	
        	// create FileWriter and CSVWriter
			FileWriter outputfile = new FileWriter(file);
			CSVWriter writer = new CSVWriter(outputfile);
			
			// Write header line
			String[] header = {"start_lat", "start_lng", "end_lat", "end_lng", "time"};
			writer.writeNext(header);
			
			List<Plan> selectedPlans = pop.getPersons().values().stream()
					.map(person -> person.getSelectedPlan())
					.collect(Collectors.toList());
			
			HashMap<PlanElement, PlanElement> activityBeforeElement = new HashMap<>();
			HashMap<PlanElement, PlanElement> activityAfterElement = new HashMap<>();
			
			List<PlanElement> zoomerElements = selectedPlans.stream()
					.flatMap(plan -> zoomerInPlan(plan).stream())
					.collect(Collectors.toList());
			
			System.out.println(zoomerElements);
			

			});
//
//			for(Plan plan: selectedPlans) {
//				
//				List<PlanElement> zoomerElements = zoomerInPlan(plan);
//						
//						
//				zoomerElements.stream()
//				.forEach(element -> {
//					
//					
//					Coord coordStartZoomer = ((Activity)activityBeforeElement(element)).getCoord();
//					Coord coordEndZoomer = ((Activity)activityAfterElement(element)).getCoord();
//					
//					String x_coordStartZoomer = Double.toString(transformation.transform(coordStartZoomer).getX());
//					String y_coordStartZoomer = Double.toString(transformation.transform(coordStartZoomer).getY());
//					String x_coordEndZoomer = Double.toString(transformation.transform(coordEndZoomer).getX());
//					String y_coordEndZoomer = Double.toString(transformation.transform(coordEndZoomer).getY());
//					String drtTime = Double.toString(((Leg)element).getDepartureTime());
//					
//					String[] nextLine = {x_coordStartZoomer, y_coordStartZoomer, x_coordEndZoomer, y_coordEndZoomer, drtTime};
//					writer.writeNext(nextLine);
//					
//				});
//				
//			}

  
			
			

//	            
//	 
//	 
//	            for(Leg leg: legsWithZoomerMode) {
//	            	
//	            	for(int index=0; index < planElements.size();index++) {
//	            		
//	            		element = planElements.get(index);
//	            		
//	            		if(element instanceof Leg) {
//	            			
//	            			if((Leg)element == leg) {
//	            				
//	            				activityBeforeLeg.put(leg, (Activity)(planElements.get(index-1)));
//	            				activityAfterLeg.put(leg, (Activity)(planElements.get(index+1)));
//	            			}            			       			
//	            		}
//	            	}
//	            	
//	            	
//	            	
//	            	
//	            }
//	            
	            
     
	        
	        writer.close();
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       

	}
	
	public static List<PlanElement> zoomerInPlan(Plan plan) {
		
		return plan.getPlanElements().stream()
				.filter(element -> element instanceof Leg)
				.filter(element -> ((Leg)element).getMode().contains("zoomer"))					
				.collect(Collectors.toList());
	}
	

}
