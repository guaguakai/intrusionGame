package model.cyber;

import java.util.ArrayList;
import java.util.List;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import teamBuildingSolvers.DefenderOracle.BRDefenderP;
import cyberSolvers.Pomdp;

public class network {
	
	public int time;
	public List<host> computers;
	AbstractBaseGraph graph;
	
	public void buildNetwork(){
		host h1 = new host();
		h1.createNodes(time);
		
		host h2 = new host();
		h2.createNodes(time);
		
		h1.neighbors.add(h2);
		h2.neighbors.add(h1);
		
		h1.addNodes(graph);
		h2.addNodes(graph);
		
		h1.buildNeighborhood(graph);
		h2.buildNeighborhood(graph);
		
	}
	public void run(){
//		List<Double> resources = new ArrayList<Double>();
//		resources.add(5.0);
//	
//		List<Double> coverage = new ArrayList<Double>();
//		coverage.add(2.0);
//		
//		List<Double> Prob = new ArrayList<Double>();
//		Prob.add(0.7);
//
//		
//		TeamBuildingProblem team = new TeamBuildingProblem(resources, coverage, Prob);
//
//		team.setGraph(graph);
//		
//		BRDefenderP def = new BRDefenderP(team);
//		def.loadProblem();
//		def.writeProb("testpoop");
//		def.solve();
//		
		
		
		Pomdp p = new Pomdp(graph);
		
		p.timeHorizon=time;
		
		p.loadProblem();
		p.writeProb("testpoop");
		p.solve();
		System.out.println(p.getExpectedPayoff());
		System.out.println(p.getExpectedPayoff()/(double)time);
		p.getPolicy();
		
	}

	public static void main(String[] args){
		network n = new network();
		n.time=2;
		AbstractBaseGraph<INode, ExtDWE> g = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		Node n1 = new Node();
		Node n2 = new Node();
		
		g.addVertex(n1);
		g.addVertex(n2);
		ExtDWE e = g.addEdge(n1, n2);
		g.setEdgeWeight(e, 0.3);
		
		n1.payoff=5;
		n2.payoff=5;
		
		n.graph=g;
		
		n.run();
		
	}
}
