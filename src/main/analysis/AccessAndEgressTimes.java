package main.analysis;


import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;


import main.utils.MyUtils;

public class AccessAndEgressTimes {

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
        
        String baseCasePopFilename = rootPath + "2019-07-23/05_FullRun/output/berlin-v5.4-" + percent + "pct.output_plans.xml.gz";
        String policyCasePopFilename = rootPath + "Input_global/plans/berlin-plans-" + percent + "pct-original.xml.gz";
        
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
        
       
        
        Double baseCaseAverageAETime = calculateAverageAETime(basePop, frohnauAgents);
        Double policyCaseAverageAETime = calculateAverageAETime(policyPop, frohnauAgents);
        
        Double differenceAETime = policyCaseAverageAETime - baseCaseAverageAETime;
        
        System.out.println("The average access/ egress time in base case is: " + baseCaseAverageAETime);
        System.out.println("The average access/ egress time in policy case is: " + policyCaseAverageAETime);
        System.out.println("The difference between base and policy case is: " + differenceAETime);        
        
    
	}    
        
	
	
	
	private static Double calculateAverageAETime(Population pop, List<String> filteredAgents) {
		// TODO Auto-generated method stub
    			
    	List<Plan> selectedPlans = pop.getPersons().values().stream()
    				.filter(person -> filteredAgents.contains(person.getId().toString()))
    				.map(person -> person.getSelectedPlan())
    				.collect(Collectors.toList());
    	
    			
    	HashMap<Leg, Double> legRouteDuration = new HashMap<>();

    	
    	selectedPlans.stream()
    		.forEach(plan ->{
    			
    		plan.getPlanElements().stream()
    			.filter(element -> element instanceof Leg)
    			.forEach(element -> {
    				
    				Leg legElement = ((Leg)element);
    				
    				String legElementMode = legElement.getMode().toString();
    				
    				switch(legElementMode) {
    					
    					case "zoomer":
    						
    						legRouteDuration.put(legElement, legElement.getRoute().getTravelTime());
    						break;
    				
    					case "access_walk":
    						
    						if(((Activity)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)+1)))).getType().toString().equals("pt interaction")) {
    							
    							legRouteDuration.put(legElement, legElement.getRoute().getTravelTime());
    						}
    						break;
    				
    					case "egress_walk":

    						if(((Activity)(plan.getPlanElements().get((plan.getPlanElements().indexOf(element)-1)))).getType().toString().equals("pt interaction")) {
    							
    							legRouteDuration.put(legElement, legElement.getRoute().getTravelTime());
    						}
    						break;
    						
    					default:
    						
    						break;
    				}
    				
    				
    			});
    		
    		});
    	
    	System.out.println("Plans regarded: " + selectedPlans.size());
    	System.out.println("Legs regarded: " + legRouteDuration.size());
    	return ((legRouteDuration.values().stream().mapToDouble(i -> i).sum())/legRouteDuration.size());
        	
      	
        	
	}

				
	    
}
