package main.config;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.vis.otfvis.OTFVisConfigGroup;

public class RunPlayground {

	
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

		Node prevNode;
		{
			Node node = nf.createNode( Id.createNodeId( 0 ), new Coord( 0., 0. ) );
			scenario.getNetwork().addNode( node );
			prevNode = node;
		}
		for( int ii = 1 ; ii <= 10 ; ii++ ){
			Node node = nf.createNode( Id.createNodeId( ii ), new Coord( ii * 100, 0. ) );
			scenario.getNetwork().addNode( node );
//			Link ll = nf.createLink(Id.createLinkId(ii), prevNode, node);
//			scenario.getNetwork().addLink(ll);
//			Link ll2 = nf.createLink(Id.createLinkId(ii+"b"), node, prevNode);
//			scenario.getNetwork().addLink(ll2);
			addLinkAndFacility( scenario,nf, ff,  prevNode, node );		
			prevNode = node;
		}
		
		
		NetworkWriter wr = new NetworkWriter(scenario.getNetwork());
		wr.write(".\\scenarios\\playground\\input\\network.xml");
		
//		
//		OTFVisConfigGroup otfvis = new OTFVisConfigGroup();
//		otfvis.setDrawNonMovingItems(true);
//		config.addModule(otfvis);
//	

		Controler controler = new Controler(scenario);
//		controler.addOverridingModule(new OTFVisLiveModule());
		
	}
	
	private static void addLinkAndFacility( Scenario scenario, NetworkFactory nf, ActivityFacilitiesFactory ff, Node prevNode, Node node ){
		final String str = prevNode.getId() + "-" + node.getId();
		Link link = nf.createLink( Id.createLinkId( str ), prevNode, node ) ;
		Set<String> set = new HashSet<>() ;
		set.add("car" ) ;
		link.setAllowedModes( set ) ;
		link.setLength( CoordUtils.calcEuclideanDistance( prevNode.getCoord(), node.getCoord() ) );
		link.setCapacity( 3600. );
		link.setFreespeed( 50./3.6 );
		scenario.getNetwork().addLink( link );
		// ---
		ActivityFacility af = ff.createActivityFacility( Id.create( str, ActivityFacility.class ), link.getCoord(), link.getId() ) ;
		ActivityOption option = ff.createActivityOption( "shop" ) ;
		af.addActivityOption( option );
		scenario.getActivityFacilities().addActivityFacility( af );
	}

}
