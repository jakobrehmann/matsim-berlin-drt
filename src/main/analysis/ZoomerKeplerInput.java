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
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import com.opencsv.CSVWriter;

public class ZoomerKeplerInput {

	public static void main(String[] args) {
		
		//rootPath settings
		
        String username = "david";
        String rootPath = null;
        String percent = "10";

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
        
        String inputPopFilename = rootPath + "2019-07-23/05_FullRun/output/berlin-v5.4-" + percent + "pct.output_plans.xml.gz";
//      String csvOutputPath = rootPath + "Report_global/walkingOD.csv";
        String csvOutputPath = rootPath + "Report_global/zoomerOD.csv";
        
        //create new file object
        File file = new File(csvOutputPath);
        

       
        // Read Population
        Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(sc).readFile(inputPopFilename);
        final Population pop = sc.getPopulation();
        
        CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(TransformationFactory.GK4, TransformationFactory.WGS84);
        
        //start writing csvfile
        try {
        	
        	// create FileWriter and CSVWriter
			FileWriter outputfile = new FileWriter(file);
			CSVWriter writer = new CSVWriter(outputfile);
			
			// Write header line
			String[] header = {"start_lng", "start_lat", "end_lng", "end_lat", "time"};
			writer.writeNext(header);
			
			List<Plan> selectedPlans = pop.getPersons().values().stream()
					.map(person -> person.getSelectedPlan())
					.collect(Collectors.toList());
			
			
			HashMap<PlanElement, Activity> activityBeforeElement = new HashMap<>();
			HashMap<PlanElement, Activity> activityAfterElement = new HashMap<>();
			List<PlanElement> zoomerElements = new ArrayList<>();
			
			
			selectedPlans.stream()
				.forEach(plan ->{
			
					plan.getPlanElements().stream()
						.filter(element -> element instanceof Leg)
	//					.filter(element -> ((Leg)element).getMode().contains("access_walk")||((Leg)element).getMode().contains("egress_walk"))
						.filter(element -> ((Leg)element).getMode().contains("zoomer"))					
						.forEach(element -> {
							
							zoomerElements.add(element);
							
							activityBeforeElement.put(element, (Activity)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)-1))));
							activityAfterElement.put(element, (Activity)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)+1))));
							
						});
			});

						
						
			zoomerElements.stream()
				.forEach(element -> {
					
					
					Coord coordStartZoomer = activityBeforeElement.get(element).getCoord();
					Coord coordEndZoomer = activityAfterElement.get(element).getCoord();
					
					String x_coordStartZoomer = Double.toString(transformation.transform(coordStartZoomer).getX());
					String y_coordStartZoomer = Double.toString(transformation.transform(coordStartZoomer).getY());
					String x_coordEndZoomer = Double.toString(transformation.transform(coordEndZoomer).getX());
					String y_coordEndZoomer = Double.toString(transformation.transform(coordEndZoomer).getY());
					String drtTime = Double.toString(((Leg)element).getDepartureTime());

					String[] nextLine = {x_coordStartZoomer, y_coordStartZoomer, x_coordEndZoomer, y_coordEndZoomer, drtTime};
					
					writer.writeNext(nextLine);
					
				});
				
 
     
	        
	        writer.close();
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       

	}
	
}
