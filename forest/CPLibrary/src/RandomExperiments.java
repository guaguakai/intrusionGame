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


public class RandomExperiments {

	
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

	private static AbstractBaseGraph<INode, ExtDWE> DenseRandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r, int min, int max){
		Configuration.initialize(108374);
		Random random = new Random();
		return GraphGenerator.getDenseRandomGraph(numNodes, nrOfSources, nrOfTargets, r, min, max);
		
		//System.out.println("Number of Edges: " + team.getGraph().edgeSet().size());
	}
	public static AbstractBaseGraph<INode, ExtDWE> getMadagascarGraph() {
		// TODO Auto-generated method stub
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);

		//AbstractBaseGraph<INode, ExtDWE> graph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		
		List<INode> nodeList = new ArrayList<INode>();
		for (int x = 0; x < 383; x++) {
			INode v = new Node();
			v.setType(NODE_TYPE.INTERMEDIATE);
			nodeList.add(v);
			graph.addVertex(v);
		}

		int[] targetIndexes = {77,78,79,80,81,82,83,84,85,86};
				//1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};//,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90};
		
		int[] sourceIndexes = {63,64,65,66,67,68,69,70,71,72,73,74,75,76};
				
		INode virtual = new Node();
		virtual.setType(NODE_TYPE.SOURCE);
		graph.addVertex(virtual);
		
		for (int i : sourceIndexes) {
			ExtDWE e = graph.addEdge(virtual, nodeList.get(i - 1));
			e.setType(EDGE_TYPE.VIRTUAL);
		}
		
		
//		Low Priority (1) = 83,86
//		Medium Priority (2) = 78,81,85,
//		High Priority (3) = 77,79,80,82,84
		
		for (int i : targetIndexes) {
			nodeList.get(i - 1).setType(NODE_TYPE.TARGET);
			nodeList.get(i - 1).setPayoff(30);
		}
		
		nodeList.get(83).setPayoff(20);
		nodeList.get(86).setPayoff(20);
		
		nodeList.get(78).setPayoff(10);
		nodeList.get(81).setPayoff(10);
		nodeList.get(85).setPayoff(10);
		

		int[][] edgeSet = {{1,2},{2,77},{1,71},{71,3},{3,4},{4,5},{5,77},{71,6},{6,72},{72,3},{72,10},{3,10},{10,4},{10,11},{4,7},{11,7},{7,8},{8,9},{9,78},{11,12},{12,8},{6,14},{14,70},{70,61},{61,84},{14,15},{15,16},{16,17},{14,18},{70,18},{18,69},{17,69},{69,19},{19,84},{69,20},{20,84},{17,68},{69,68},{68,21},{21,84},{68,22},{22,23},{23,24},{24,85},{23,13},{13,12},{68,67},{67,25},{25,84},{84,62},{84,27},{25,26},{26,79},{67,28},{28,29},{29,30},{30,79},{30,32},{32,33},{33,85},{32,31},{31,22},{62,66},{66,34},{34,35},{35,80},{35,36},{36,79},{30,26},{35,65},{65,38},{38,80},{80,74},{74,64},{65,37},{37,73},{73,39},{39,79},{43,79},{43,41},{41,40},{40,85},{85,42},{42,60},{60,86},{64,46},{46,63},{63,47},{47,37},{47,73},{47,48},{48,82},{82,45},{45,40},{40,44},{63,49},{49,82},{63,52},{52,53},{53,54},{54,83},{54,59},{59,83},{59,55},{55,81},{44,81},{53,56},{56,76},{56,57},{76,50},{57,76},{57,75},{51,64},{64,50}};
		
		for (int[] edge : edgeSet) {
		
			graph.addEdge(nodeList.get(edge[0] - 1), nodeList.get(edge[1] - 1));
			graph.addEdge(nodeList.get(edge[1] - 1), nodeList.get(edge[0] - 1));
		}

		System.out.println("No. of edges: " + graph.edgeSet().size());
		System.out.println("No. of nodes: " + graph.vertexSet().size());

		return graph;
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
		
		if(args.length==0){
			System.out.println("filename, budget, prob");
		}
		
		String results = "";
		int iter  = Integer.parseInt(args[1]);
		
		boolean prob = false;
		
		
		if(args[2].equals("p")){
			prob=true;
		}
		
		AbstractBaseGraph graph = DenseRandomGraph( Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Double.parseDouble(args[6]), 5, 7);
		

		//GraphGenerator.removeNodesWithDegree4(graph, GraphGenerator.getTargetSet(graph), GraphGenerator.getSourceSet(graph));
		
		int [] resourcecost = {5, 6, 7, 8};
		int [] resourcecoverage;
		
		if(prob){
			int [] coverage = {2, 2, 2, 2};
			resourcecoverage = coverage;
		}else{
			int[] coverage = {1, 2, 3, 4};
			resourcecoverage = coverage;
		}
		
		double [] resourceprob = {0.5,0.6, 0.7, 0.8};
		
		teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, prob);
		
		for(int i=7;i<args.length;i++){
			int budget  = Integer.parseInt(args[i]);
			
			results = CompactSearch(graph, budget, 1);
			
			
			try {
			    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data/"+args[0]+"compact.csv", true)));
			    //out.print(budget + ", " + graph.vertexSet().size() + ", "+graph.edgeSet().size());
			    out.println(results);
			    out.close();
			} catch (IOException e) {
			    
			}
			results = CompactSearch(graph, budget, 2);
			
			
			try {
			    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data/"+args[0]+"compact.csv", true)));
			    //out.print(budget + ", " + graph.vertexSet().size() + ", "+graph.edgeSet().size());
			    out.println(results);
			    out.close();
			} catch (IOException e) {
			    
			}
			results = smartBruteForce(graph, budget);
			
			try {
			    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data/"+args[0]+"smartBF.csv", true)));
			    //out.print(budget + ", " + graph.vertexSet().size() + ", "+graph.edgeSet().size());
			    out.println(results);
			    out.close();
			} catch (IOException e) {
			    
			}

			// results = BruteForce(graph, budget);
			
			// try {
			//     PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data/"+args[0]+"BF.csv", true)));
			//     //out.print(budget + ", " + graph.vertexSet().size() + ", "+graph.edgeSet().size());
			//     out.println(results);
			//     out.close();
			// } catch (IOException e) {
			    
			// }
		}
		
	}
}
