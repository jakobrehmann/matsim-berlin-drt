package main.analysis;



import main.utils.MyUtils;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.router.*;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class AnalysePlans {
	

	public static void main ( String[] args ) throws IOException {
        String username = "jakob";
        String rootPath = null;
        String percent = "1";

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

        String popBase = rootPath + "Input_global/plans/berlin-plans-10pct-frohnau-scrubbed.xml.gz";
        String popPolicy = rootPath + "2019-07-23/05_FullRun/output/berlin-v5.4-10pct.output_plans.xml.gz";
        String subpopPath = rootPath+ "/analysis/impactedAgents.txt ";
        String outputFile = "" ;
        String popInput = "" ;

//        String VehWithinRingBase = rootPath + ".\\output\\VehWithinRingBase.txt";

        ArrayList<String> impactedAgents = MyUtils.readLinksFile(subpopPath) ;
        boolean subpopFilter = true ;

        if (subpopFilter) outputFile = rootPath + "analysis/plansAnalysisSubpop.txt";
        else outputFile = rootPath + "analysis/plansAnalysisFull.txt";


        BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
        String headers = "nPersons, nCarLegs, nCarPerPerson, nCarsUsingPersons, nPtLegs, totalCarDistance, totalPtDistance, nZoomerLegs, totalZoomerDistance, totalDistance";
        bw.write(headers);
        bw.newLine();

        for (int i = 1; i <= 2; i++) {
            if (i ==1) popInput = popBase ;
            else if (i==2) popInput = popPolicy ;
            else break;


            Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
            new PopulationReader(sc).readFile(popInput);

            final Population pop = sc.getPopulation();

            long nAgents = 0 ;
            long nCarLegs = 0 ;
            long nPtLegs = 0 ;
            long nZoomerLegs = 0;
            long nCarUsingPersons = 0 ;
            double totalCarDistance = 0. ;
            double totalPtDistance = 0. ;
            double totalZoomerDistance = 0;
            double totalDistance = 0 ;

            for ( Person person : pop.getPersons().values() ) {
                if (subpopFilter) {
                    if (!impactedAgents.contains(person.getId().toString())) continue;
                }

                nAgents ++ ;
                boolean carUser = false ;
                Plan plan = person.getSelectedPlan() ;
//                for (TripStructureUtils.Subtour pe : TripStructureUtils.getSubtours(plan, new StageActivityTypesImpl())){
//                    List<PlanElement> planEls = (List<PlanElement> ) pe.;
//                    TripStructureUtils.getLegs();
//                    MainModeIdentifier mmi = new MainModeIdentifierImpl((List<PlanElement>) ());
////                    String mainMode = mmi.identifyMainMode(pe.getTrips());
////                }
//                for ( List<Leg> planEls :TripStructureUtils.getLegs(plan) ) {
//
//                }
//
//
//
//                if
                for ( Leg leg : TripStructureUtils.getLegs( plan ) ) {
                    totalDistance += leg.getRoute().getDistance() ;
                    if ( TransportMode.car.equals( leg.getMode() ) ) {
                        nCarLegs++ ;
                        carUser = true ;
                        totalCarDistance += leg.getRoute().getDistance() ;
                    }
                    else if ( TransportMode.pt.equals(leg.getMode())) {
                        nPtLegs++ ;
                        totalPtDistance += leg.getRoute().getDistance() ;
                    } else if ("zoomer".equals(leg.getMode())) {
                        nZoomerLegs++ ;
                        totalZoomerDistance +=  leg.getRoute().getDistance() ;
                    }
                }
                if ( carUser ) nCarUsingPersons++ ;
            }
            bw.newLine();
            bw.write(nAgents + "," + nCarLegs + "," + 1.*nCarLegs/pop.getPersons().size()+ "," +
                    nCarUsingPersons + "," + nPtLegs + "," + totalCarDistance/1000 + "," + totalPtDistance/1000 + "," +
                    nZoomerLegs + "," + totalZoomerDistance/1000 + "," + totalDistance/1000);

            // could the people walk further to pt stops in the past?


            System.out.println( "Number of persons = " + nAgents) ;
            System.out.println( "Number of car legs = " + nCarLegs ) ;
            System.out.println( "Number of car legs per person = " + 1.*nCarLegs/pop.getPersons().size() ) ;
            System.out.println( "Number of car using persons = " + nCarUsingPersons ) ;
            System.out.println( "Number of pt legs = " + nPtLegs ) ;
            System.out.println( "Total Driving Distance = " + totalCarDistance/1000 ) ;
            System.out.println( "Total Pt Distance = " + totalPtDistance/1000) ;
            System.out.println( "Number of zoomer legs = " + nZoomerLegs);
            System.out.println( "Total Zoomer Distance = " + totalZoomerDistance/1000 ) ;

        }

        bw.flush();
        bw.close();

    }

    static void writePlansFile(String headers, String analysisBase, String analysisPolicy, String outputFile){
        BufferedWriter bw = IOUtils.getBufferedWriter(outputFile);
        try {
            bw.write(headers);
            bw.newLine();
            bw.write((analysisBase));
            bw.newLine();
            bw.write(analysisPolicy);
            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
