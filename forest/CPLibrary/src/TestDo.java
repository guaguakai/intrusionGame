import ilog.concert.IloException;
import ilog.concert.IloIntVar;

import java.util.ArrayList;
import java.util.List;

import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.Node;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;


public class TestDo {

	private static AbstractBaseGraph<INode, ExtDWE> genTestGraph() {
	 	AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		
	 	Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.TARGET, 5);
		//Node n6 = new Node(NODE_TYPE.SOURCE, 0);
		
		graph.addVertex(n0);
		graph.addVertex(n1);
		graph.addVertex(n2);
		graph.addVertex(n3);
		graph.addVertex(n4);
		graph.addVertex(n5);
		//graph.addVertex(n6);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		
		ExtDWE e2 = graph.addEdge(n0, n2);
		
		ExtDWE e3 = graph.addEdge(n1, n3);
		ExtDWE e4 = graph.addEdge(n2, n4);
		
		ExtDWE e5 = graph.addEdge(n3, n5);
		ExtDWE e6 = graph.addEdge(n4, n5);
		
	

		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);	
		edgeList.add(e4);	
		edgeList.add(e5);	
		edgeList.add(e6);	
	
		
		return graph;
		
		
		
	}
	private static AbstractBaseGraph<INode, ExtDWE> gensmallTestGraph() {
	 	AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		
	 	Node n3 = new Node(NODE_TYPE.TARGET, 5);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		//Node n6 = new Node(NODE_TYPE.SOURCE, 0);
		
		graph.addVertex(n0);
		graph.addVertex(n1);
		graph.addVertex(n2);
		graph.addVertex(n3);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		ExtDWE e2 = graph.addEdge(n1, n2);
		ExtDWE e3 = graph.addEdge(n2, n3);
		
		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		
		
		
		return graph;
		
		
		
	}
	public static void main(String[] args) throws Exception{
		AbstractBaseGraph graph = genTestGraph();
		
		List<Double> resources = new ArrayList<Double>();
		resources.add(1.0);
		//resources.add(1.0);

		List<Double> coverage = new ArrayList<Double>();
		coverage.add(2.0);
		//coverage.add(5.0);
		
		List<Double> Probability = new ArrayList<Double>();
		Probability.add(0.5);
		//Probability.add(5.0);
		
		TeamBuildingProblem team = new TeamBuildingProblem(resources, coverage, Probability);
		team.setGraph(graph);
		
		DefenderOracle DefOr = new DefenderOracle(team);
		System.out.println(DefOr.setUp());
		
		try {
			System.out.println(DefOr.cp.getObjValue());
		} catch (IloException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for(IloIntVar v : DefOr.variables){
			System.out.println(v +": "+DefOr.cp.getValue(v));
		}

	}
}
