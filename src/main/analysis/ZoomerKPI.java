package main.analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import com.opencsv.CSVWriter;
import com.sun.xml.bind.v2.model.core.ID;

import main.utils.MyUtils;

public class ZoomerKPI {

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
        
        String csvOutputPath = rootPath + "Report_global/aeTimeValues.csv";
        
//      String subpopPath = rootPath + "/analysis/frohnauAgents.txt ";       
//      List<String> frohnauAgents = MyUtils.readLinksFile(subpopPath);
        
        
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
			String[] header = {"person_Id", "start_lat", "start_lng", "end_lat", "end_lng", "time", "distance"};
			writer.writeNext(header);
			
			List<Plan> selectedPlans = pop.getPersons().values().stream()
//					.filter(person -> frohnauAgents.contains(person.getId().toString()))
					.map(person -> person.getSelectedPlan())
					.collect(Collectors.toList());
			
			
			HashMap<PlanElement, Activity> activityBeforeElement = new HashMap<>();
			HashMap<PlanElement, Activity> activityAfterElement = new HashMap<>();
			HashMap<PlanElement, Person> elementOfPerson = new HashMap<>();
			List<PlanElement> zoomerElements = new ArrayList<>();
			
			
			selectedPlans.stream()
				.forEach(plan ->{
			
					plan.getPlanElements().stream()
						.filter(element -> element instanceof Leg)
						.filter(element -> ((Leg)element).getMode().contains("zoomer"))					
						.forEach(element -> {
							
							zoomerElements.add(element);
							
							activityBeforeElement.put(element, (Activity)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)-1))));
							activityAfterElement.put(element, (Activity)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)+1))));
							elementOfPerson.put(element, plan.getPerson());
							
							
						});
			});

						
			
			
			zoomerElements.stream()
				.forEach(element -> {
										
					Coord coordStartZoomer = activityBeforeElement.get(element).getCoord();
					Coord coordEndZoomer = activityAfterElement.get(element).getCoord();
					
					String peson_id = elementOfPerson.get(element).getId().toString();
					String x_coordStartZoomer = Double.toString(transformation.transform(coordStartZoomer).getX());
					String y_coordStartZoomer = Double.toString(transformation.transform(coordStartZoomer).getY());
					String x_coordEndZoomer = Double.toString(transformation.transform(coordEndZoomer).getX());
					String y_coordEndZoomer = Double.toString(transformation.transform(coordEndZoomer).getY());
					String taveltime = Double.toString(((Leg)element).getRoute().getTravelTime());
					String travelDistance = Double.toString(((Leg)element).getRoute().getDistance());

					String[] nextLine = {peson_id, x_coordStartZoomer, y_coordStartZoomer, x_coordEndZoomer, y_coordEndZoomer, taveltime, travelDistance};
					
					writer.writeNext(nextLine);
					
				});
				
 
     
	        
	        writer.close();
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
