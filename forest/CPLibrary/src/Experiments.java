import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lpWrapper.LPSolverException;
import model.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import search.BruteForce;
import search.Search;
import search.teamNode;


public class Experiments {
	private static AbstractBaseGraph<INode, ExtDWE> GridGraph(int width, int height, int nrSources,int nrTargets, int payoff) {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		//AbstractBaseGraph<INode, ExtDWE> graph = new WeightedMultigraph<INode, ExtDWE>(
		//		ExtDWE.class);
		
		List<ArrayList<INode>> rows = new ArrayList<ArrayList<INode>>(height);
		
		for(int j=0;j<height;j++){
			
		
		ArrayList<INode> nodeList = new ArrayList<INode>();
		//rows.add(nodeList);
		
		for(int i=0;i<width-1;i++){
			if(nodeList.isEmpty()){
				Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
				n1.setLatLon(i, j);
				Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
				n1.setLatLon(i, j+1);
				
				graph.addVertex(n1);
				graph.addVertex(n2);
				
				nodeList.add(n1);
				nodeList.add(n2);
				
				ExtDWE e = graph.addEdge(n1, n2);
				//ExtDWE e1 = graph.addEdge(n2, n1);
		
			}else{
				Node n = new Node(NODE_TYPE.INTERMEDIATE, 0);
				n.setLatLon(i, j+1);
				
				nodeList.add(n);
				graph.addVertex(n);
				ExtDWE e = graph.addEdge(nodeList.get(nodeList.size()-2), n);
				//ExtDWE e2 = graph.addEdge(n,nodeList.get(nodeList.size()-2));
			}
		}
		if(j>0){
			for(int i=1;i<width-1;i++){
				//if(i%2 == 0){
					ExtDWE e = graph.addEdge(rows.get(j-1).get(i), nodeList.get(i));
				//}else{
					ExtDWE e1 = graph.addEdge(nodeList.get(i), rows.get(j-1).get(i));
				//}
			}
		}
		rows.add(nodeList);
		
		}
		
		double stepSRC = height/nrSources;
		
		Node n = new Node(NODE_TYPE.SOURCE, 0);
		graph.addVertex(n);
		double loc=0;

		for(int i=0;i<nrSources;i++){
			ExtDWE e = graph.addEdge(n, rows.get((int)loc).get(0));
			e.setType(EDGE_TYPE.VIRTUAL);
			loc= loc+stepSRC;
		}
		
		double stepTGT = height/nrTargets;
		loc=0;
		for(int i=0;i<nrTargets;i++){
			rows.get((int)loc).get(width-1).setType(NODE_TYPE.TARGET);
			rows.get((int)loc).get(width-1).setPayoff(payoff);
			loc= loc+stepTGT;
			}
		
		return graph;
	
	}

	private static AbstractBaseGraph<INode, ExtDWE> RandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r){
		Configuration.initialize(108374);
		Random random = new Random();
		return GraphGenerator.getRandomGeometricGraph(numNodes, nrOfSources, nrOfTargets, r,
			50, random);
		
		//System.out.println("Number of Edges: " + team.getGraph().edgeSet().size());
	}

	public static String CompactSearch(AbstractBaseGraph graph, double budget, int t) throws MalformedGraphException, LPSolverException{
		 
		 teamNode root = new teamNode("root", null, 0, budget, false);
		 root.teamObj.setGraph(graph);
		 
		 Search tree = new Search(root.teamObj, budget);
		 tree.t=t;
		 double tick = System.currentTimeMillis();
		 teamNode result = tree.PQSearch(root);
		 double tock =  System.currentTimeMillis();
		 
		 
		 String results = 	"Budget: " + budget +", verticies: "
				 			+graph.vertexSet().size() + ", edges: "
				 			+result.teamObj.getGraph().edgeSet().size() + ", GV: "
				 			+result.getEvaluation() + ", runtime(S): " 
				 			+(tock-tick)/1000 + ", resources: " 
				 			+result.resourcesList + "," + tree.fullDefIter;;
		 
		 results = 	budget +","
		 			+graph.vertexSet().size() + ","
		 			+result.teamObj.getGraph().edgeSet().size() + ","
		 			+result.getEvaluation() + "," 
		 			+(tock-tick)/1000 + "," 
		 			+result.resourcesList;
		 	
		 System.out.println("\n"+results);
		 
		 return results;
		 
	 }

	 public static String smartBruteForce(AbstractBaseGraph graph, double budget) throws MalformedGraphException, LPSolverException{
	 
		 teamNode root = new teamNode("root", null, 0, budget, false);
		 root.teamObj.setGraph(graph);
		 
		 double tick = System.currentTimeMillis();
		 BruteForce tree = new BruteForce(root.teamObj, budget);	
		
		 try {
			   teamNode result = tree.smartSearch(root); 
			   double tock =  System.currentTimeMillis();
				 
				 
				 String results = 	"Budget: " + budget +", verticies: "
				 			+graph.vertexSet().size() + ", edges: "
				 			+result.teamObj.getGraph().edgeSet().size() + ", GV: "
				 			+result.getEvaluation() + ", runtime(S): " 
				 			+(tock-tick)/1000 + ", resources: " 
				 			+result.resourcesList;
			
				 System.out.println("\n"+results);
				 return results;
				 
			} catch (OutOfMemoryError E) {
				double tock =  System.currentTimeMillis();
				String results=""+ (tock-tick)/1000;
				System.out.println(results);
				return results;
			
			}
	
	}
	public static String BruteForce(AbstractBaseGraph graph, double budget) throws MalformedGraphException, LPSolverException{
		 
		 teamNode root = new teamNode("root", null, 0, budget, false);
		 root.teamObj.setGraph(graph);
		 
		 double tick = System.currentTimeMillis();
		 BruteForce tree = new BruteForce(root.teamObj, budget);	
		  try {
			   teamNode result = tree.Search(root); 
			   double tock =  System.currentTimeMillis();
				 
				 
				 String results = budget +","
				 			+graph.vertexSet().size() + ","
				 			+result.teamObj.getGraph().edgeSet().size() + ","
				 			+result.getEvaluation() + "," 
				 			+(tock-tick)/1000 + "," 
				 			+result.resourcesList;
			
				 System.out.println("\n"+results);
				 return results;
				 
			} catch (OutOfMemoryError E) {
				double tock =  System.currentTimeMillis();
				String results="OutOfMemoryError, "+ (tock-tick)/1000;
				System.out.println(results);
				return results;
			
			}
	 }
	
	public static void main(String[] args) throws MalformedGraphException, LPSolverException ,FileNotFoundException{
		Configuration.initialize(108374);

		String name = "Teamsfull";
		String results = "";
		int budget  = 15;
		
		int [] resourcecost = {5, 5, 5};
		int [] resourcecoverage = {1, 2, 3};
		double [] resourceprob = {0.9, 0.8, 0.7};
		
//		int [] resourcecost = {5, 6, 7, 8};
//		int [] resourcecoverage = {2, 2, 2, 2};
//		double [] resourceprob = {0.5,0.6, 0.7, 0.8};
		
		teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, true);
		int i=0;
		while(i<5){
			i++;
		AbstractBaseGraph graph;
		
		graph = RandomGraph(20,3,3,0.1);
		//graph = GridGraph(4,4,4,4);
		GraphGenerator.removeNodesWithDegree4(graph, GraphGenerator.getTargetSet(graph), GraphGenerator.getSourceSet(graph));

		//results = CompactSearch(graph, budget, 1);
		
//		
//		try {
//		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(name+"compact.csv", true)));
//		    //out.print(budget + ", " + graph.vertexSet().size() + ", "+graph.edgeSet().size());
//		    out.println(results);
//		    out.close();
//		} catch (IOException e) {
//		    
//		}
//		}
		results = smartBruteForce(graph, budget);
		
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(name+"BF.csv", true)));
		    //out.print(budget + ", " + graph.vertexSet().size() + ", "+graph.edgeSet().size());
		    out.println(results);
		    out.close();
		} catch (IOException e) {
		    
		}
		}
	}
}
