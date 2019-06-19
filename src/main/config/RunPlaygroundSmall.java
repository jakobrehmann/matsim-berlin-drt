package main.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contrib.dvrp.data.VehicleImpl;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.contrib.locationchoice.utils.PlanUtils;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.qsim.pt.TransitVehicle;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.utils.gis.matsim2esri.network.CapacityBasedWidthCalculator;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleCapacityImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehicleWriterV1;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

public class RunPlaygroundSmall {

	
	public static void main(String[] args) {

		Config config = ConfigUtils.createConfig() ;
		config.controler().setLastIteration( 0 );

		Scenario scenario = ScenarioUtils.createScenario( config );
		// don't load anything

		NetworkFactory nf = scenario.getNetwork().getFactory();
		PopulationFactory pf = scenario.getPopulation().getFactory();
		ActivityFacilitiesFactory ff = scenario.getActivityFacilities().getFactory();

		// Construct a network and facilities along a line:
		// 0 --(0-1)-- 1 --(2-1)-- 2 -- ...
		// with a facility of same ID attached to each link.  Have home towards the left, and then select a shop facility, with frozen epsilons.

		Node nodeStart = nf.createNode(Id.createNodeId("Start"), new Coord(200.,200.)) ;
		scenario.getNetwork().addNode(nodeStart);
		
		Node prevNode;
		{
			Node node = nf.createNode( Id.createNodeId( 0 ), new Coord( 0., 0. ) );
			scenario.getNetwork().addNode( node );
			prevNode = node;
		}
		
		{
			Link link = nf.createLink(Id.createLinkId("0-Start"), prevNode, nodeStart) ;
			Set<String> set = new HashSet<>() ;
			set.add(TransportMode.car) ;
			set.add(TransportMode.drt);
			link.setAllowedModes( set ) ;
			link.setLength( CoordUtils.calcEuclideanDistance( prevNode.getCoord(), nodeStart.getCoord() ) );
			link.setCapacity( 3600. );
			link.setFreespeed( 50./3.6 );
			scenario.getNetwork().addLink( link );
		}
		
		for( int ii = 1 ; ii <= 4 ; ii++ ){
			Node node = nf.createNode( Id.createNodeId( ii ), new Coord( ii * 100, 0. ) );
			scenario.getNetwork().addNode( node );
			addLinkAndFacility( scenario,nf, ff,  prevNode, node, nodeStart );		
			prevNode = node;
		}
		
		
		// Add pt nodes & links
		Node pt_nodeStart = nf.createNode(Id.createNodeId("pt_Start"), new Coord(200.,200.)) ;
		scenario.getNetwork().addNode(pt_nodeStart);
		Node pt_nodeGoal = nf.createNode(Id.createNodeId("pt_Goal"), new Coord(200.,400.)) ;
		scenario.getNetwork().addNode(pt_nodeGoal);
		
		Link ptLinkTransit = nf.createLink(Id.createLinkId("pt_link"), pt_nodeStart, pt_nodeGoal) ;
		Set<String> set = new HashSet<>() ;
		set.add(TransportMode.pt) ;
		ptLinkTransit.setAllowedModes( set ) ;
		ptLinkTransit.setLength( CoordUtils.calcEuclideanDistance( pt_nodeStart.getCoord(), pt_nodeGoal.getCoord() ) );
		//ptLink.setCapacity( 3600. );
		//ptLink.setFreespeed( 50./3.6 );
		scenario.getNetwork().addLink( ptLinkTransit );
		
		Link ptLinkStart = nf.createLink(Id.createLinkId("ptLinkStart"), pt_nodeStart, pt_nodeStart) ;
		ptLinkStart.setAllowedModes(set); 
		ptLinkStart.setLength(1.);
		scenario.getNetwork().addLink( ptLinkStart );
		
		Link ptLinkGoal = nf.createLink(Id.createLinkId("ptLinkGoal"), pt_nodeGoal, pt_nodeGoal) ;
		ptLinkGoal.setAllowedModes(set); 
		ptLinkGoal.setLength(1.);
		scenario.getNetwork().addLink( ptLinkGoal );
		
		
		ActivityFacility af = ff.createActivityFacility( Id.create( "goal", ActivityFacility.class ), ptLinkGoal.getCoord(), ptLinkGoal.getId() ) ;
		ActivityOption option = ff.createActivityOption( "shop" ) ;
		af.addActivityOption( option );
		scenario.getActivityFacilities().addActivityFacility( af );

		// Create plans
		for( int jj = 0; jj < 4; jj++ ){
			Person person = pf.createPerson( Id.createPersonId( jj ) );
			
			scenario.getPopulation().addPerson( person );
			Plan plan = pf.createPlan();
			person.addPlan( plan );	
			
			Id<ActivityFacility> homeFacilityId = Id.create(jj + "-" + (jj+1), ActivityFacility.class) ;
			Activity act = pf.createActivityFromActivityFacilityId("home", homeFacilityId);
			plan.addActivity(act );
			
			Leg leg = pf.createLeg("pt");
			leg.setDepartureTime((7+jj)*3600);
			plan.addLeg(leg );
			
			Id<ActivityFacility> shopFacilityId = Id.create("goal", ActivityFacility.class) ;
			Activity act2 = pf.createActivityFromActivityFacilityId("shop", shopFacilityId) ;
			plan.addActivity(act2);
		
		}
		
		
		// Create Transit Schedule
		TransitSchedule sched = scenario.getTransitSchedule() ;
		TransitScheduleFactory sf = sched.getFactory() ;
		
		Id<TransitStopFacility> facilityId = Id.create("stopStart", TransitStopFacility.class);
		TransitStopFacility stopStart = sf.createTransitStopFacility(facilityId , new Coord(200.,200.), false);
		stopStart.setLinkId(Id.createLinkId("ptLinkStart"));
		sched.addStopFacility(stopStart);
		
		Id<TransitStopFacility> facilityId2 = Id.create("stopGoal", TransitStopFacility.class);
		TransitStopFacility stopGoal = sf.createTransitStopFacility(facilityId2 , new Coord(200.,400.), false);
		stopGoal.setLinkId(Id.createLinkId("ptLinkGoal"));
		sched.addStopFacility(stopGoal);
		
		Id<TransitLine> lineId = Id.create("line1", TransitLine.class);
		TransitLine line = sf.createTransitLine(lineId ); 
		Id<TransitRoute> routeId = Id.create("route1", TransitRoute.class);
		NetworkRoute route = pf.getRouteFactories().createRoute(NetworkRoute.class, ptLinkStart.getId(), ptLinkGoal.getId()) ;
		
		TransitRouteStop stop1 = sf.createTransitRouteStop(stopStart, 0.,0.);
		TransitRouteStop stop2 = sf.createTransitRouteStop(stopGoal, 10*60., 10*60.);
		List<TransitRouteStop> stops = new ArrayList<>() ;
		stops.add(stop1);
		stops.add(stop2);
		
		TransitRoute transitRoute = sf.createTransitRoute(routeId , route, stops , "pt");
		for (int kk = 7 ; kk <=19 ; kk++) {
			Departure departure = sf.createDeparture(Id.create(kk, Departure.class), kk*3600.);
			transitRoute.addDeparture(departure );
		}
		
		line.addRoute(transitRoute );
		sched.addTransitLine(line );
		
		VehicleType vt = VehicleUtils.getFactory().createVehicleType(null) ;
		VehicleCapacity cap = new VehicleCapacityImpl();
		cap.setSeats(100);
		cap.setStandingRoom(100);
		vt.setCapacity(cap);
		scenario.getVehicles().addVehicleType(vt);
		
		List<Vehicle> vehList = new ArrayList<>();
		for (int pp = 0 ; pp <=100 ; pp ++) {
			//Id<org.matsim.contrib.dvrp.data.Vehicle> vehId = Id.create("pt" + pp, org.matsim.contrib.dvrp.data.Vehicle.class);
//			Id<VehicleType> type = Id.create("default", VehicleType.class);
//			Vehicle veh = new Vehicle(vehId , ptLinkStart, 50., 0., 24*3600.) ;

			
			
			
			Vehicle v = VehicleUtils.getFactory().createVehicle(Id.createVehicleId(pp), vt );
			vehList.add(v);
			scenario.getVehicles().addVehicle(v);
		}
		
		
		
		
		// Write out Files
		NetworkWriter wr = new NetworkWriter(scenario.getNetwork());
		wr.write(".\\scenarios\\playground\\input\\network_small.xml");
		FacilitiesWriter fr = new FacilitiesWriter(scenario.getActivityFacilities()) ;
		fr.write(".\\scenarios\\playground\\input\\facilities_small.xml");
		PopulationWriter pw = new PopulationWriter(scenario.getPopulation());
		pw.write(".\\scenarios\\playground\\input\\plans_small.xml");
		TransitScheduleWriter sw = new TransitScheduleWriter(scenario.getTransitSchedule()) ;
		sw.writeFile(".\\scenarios\\playground\\input\\sched_small.xml");
		VehicleWriterV1 vw = new VehicleWriterV1(scenario.getVehicles());
		vw.writeFile(".\\scenarios\\playground\\input\\veh_small.xml");
		
		
		
//		
//		OTFVisConfigGroup otfvis = new OTFVisConfigGroup();
//		otfvis.setDrawNonMovingItems(true);
//		config.addModule(otfvis);
//	

		Controler controler = new Controler(scenario);
//		controler.addOverridingModule(new OTFVisLiveModule());
		
	}
	
	private static void addLinkAndFacility( Scenario scenario, NetworkFactory nf, ActivityFacilitiesFactory ff, Node prevNode, Node node, Node nodeStart ){
		final String str = prevNode.getId() + "-" + node.getId();
		Link link = nf.createLink( Id.createLinkId( str ), prevNode, node ) ;
		Set<String> set = new HashSet<>() ;
		set.add(TransportMode.car) ;
		set.add(TransportMode.drt);
		link.setAllowedModes( set ) ;
		link.setLength( CoordUtils.calcEuclideanDistance( prevNode.getCoord(), node.getCoord() ) );
		link.setCapacity( 3600. );
		link.setFreespeed( 50./3.6 );
		scenario.getNetwork().addLink( link );
		
		final String strStart = node.getId() + "-" + nodeStart.getId();
		Link linkStart = nf.createLink( Id.createLinkId( strStart ), node, nodeStart ) ;
		linkStart.setAllowedModes( set ) ;
		linkStart.setLength( CoordUtils.calcEuclideanDistance( node.getCoord(), nodeStart.getCoord() ) );
		linkStart.setCapacity( 3600. );
		linkStart.setFreespeed( 50./3.6 );
		scenario.getNetwork().addLink( linkStart );
		// ---
		ActivityFacility af = ff.createActivityFacility( Id.create( str, ActivityFacility.class ), link.getCoord(), link.getId() ) ;
		ActivityOption option = ff.createActivityOption( "home" ) ;
		af.addActivityOption( option );
		scenario.getActivityFacilities().addActivityFacility( af );
	}

}
