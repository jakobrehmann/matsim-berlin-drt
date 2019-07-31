package main.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import main.utils.MyUtils;

public class FrohnauStationTraffic {

	
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
        
        // Set paths for input 
        
        String policyCasePopFilename = rootPath + "2019-07-23/05_FullRun/output/berlin-v5.4-" + percent + "pct.output_plans.xml.gz";
        String baseCasePopFilename = rootPath + "Input_global/plans/berlin-plans-" + percent + "pct-original.xml.gz";
        
		// Read BaseCase Population
        Scenario baseSc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(baseSc).readFile(baseCasePopFilename);
        final Population basePop = baseSc.getPopulation();
        
		// Read PolicyCase Population
        Scenario policySc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(policySc).readFile(policyCasePopFilename);
        final Population policyPop = policySc.getPopulation();
        
        // Read-in Frohnau subpopulation
        String subpopPath = rootPath + "Input_global/agents-10pct-InsideFrohnau.txt ";       
        List<String> frohnauAgents = MyUtils.readLinksFile(subpopPath);
        
       
        
        int baseCaseStationTraffic = calculateStationTraffic(basePop, frohnauAgents);
        int policyCaseStationTraffic = calculateStationTraffic(policyPop, frohnauAgents);
        
        int differenceStationTraffic = policyCaseStationTraffic - baseCaseStationTraffic;
        
        System.out.println("Station traffic in base case is: " + baseCaseStationTraffic);
        System.out.println("Station traffic in policy case is: " + policyCaseStationTraffic);
        System.out.println("The difference in Station traffic between base and policy case is: " + differenceStationTraffic);        
        
	}    
        
	
	
	
	private static int calculateStationTraffic(Population pop, List<String> filteredAgents) {
		// TODO Auto-generated method stub
    			
    	List<Plan> selectedPlans = pop.getPersons().values().stream()
    				.filter(person -> filteredAgents.contains(person.getId().toString()))
    				.map(person -> person.getSelectedPlan())
    				.collect(Collectors.toList());
    	
    	List<PlanElement> access_walk = new ArrayList<>();
    	List<PlanElement> egress_walk = new ArrayList<>();
    	List<PlanElement> access_zoomer = new ArrayList<>();
    	List<PlanElement> egress_zoomer = new ArrayList<>();
    	List<PlanElement> transfer = new ArrayList<>();
    	
    	selectedPlans.stream()
		.forEach(plan ->{
			
			plan.getPlanElements().stream()
				.filter(element -> element instanceof Activity)
				.filter(element -> ((Activity)element).getType() == "pt interaction")
				.filter(element -> ((Activity)element).getCoord().getX() == 4587412.176538966)
				.filter(element -> ((Activity)element).getCoord().getY() == 5834115.416970312)
				.forEach(element -> {
					
					if(((Leg)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)-1)))).getMode().toString().equals("access_walk")) {
						access_walk.add(element);
					}
					
					if(((Leg)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)-1)))).getMode().toString().equals("zoomer")) {
						access_zoomer.add(element);
					}
					
					if(((Leg)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)+1)))).getMode().toString().equals("egress_walk")) {
						egress_walk.add(element);
					}
					
					if(((Leg)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)+1)))).getMode().toString().equals("zoomer")) {
						egress_zoomer.add(element);
					}
					
					if(((Leg)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)+1)))).getMode().toString().equals("transit_walk")) {
						transfer.add(element);
					}
					
					
				});
			
		});
    	
    	System.out.println("Walking Access:" + access_walk.size());
    	System.out.println("Walking Egress:" + egress_walk.size());
    	System.out.println("Zooming Access:" + access_zoomer.size());
    	System.out.println("Zooming Egress:" + egress_zoomer.size());
    	System.out.println("Transfer:" + transfer.size());
    	return access_walk.size() + egress_walk.size() + access_zoomer.size() + egress_zoomer.size() + transfer.size();
    	
        	
        	
	}

				

}
