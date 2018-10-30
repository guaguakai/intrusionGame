import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import search.cutoffSearch;
import teamBuildingSolvers.QuickSolve;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;

public class TestQuickSolve {
		
	private static AbstractBaseGraph<INode, ExtDWE> GridGraph(int width, int height, int nrSources,int nrTargets, int payoff) {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		
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
		
			}else{
				Node n = new Node(NODE_TYPE.INTERMEDIATE, 0);
				n.setLatLon(i, j+1);
				
				nodeList.add(n);
				graph.addVertex(n);
				ExtDWE e = graph.addEdge(nodeList.get(nodeList.size()-2), n);
			}
		}
		if(j>0){
			for(int i=1;i<width-1;i++){
				if(i%2 == 0){
					ExtDWE e = graph.addEdge(rows.get(j-1).get(i), nodeList.get(i));
				}else{
					ExtDWE e = graph.addEdge(nodeList.get(i), rows.get(j-1).get(i));
				}
			}
		}
		rows.add(nodeList);
		
		}
		
		Node n = new Node(NODE_TYPE.SOURCE, 0);
		graph.addVertex(n);
		for(int i=0;i<nrSources;i++){
			ExtDWE e = graph.addEdge(n, rows.get(i).get(0));
			e.setType(EDGE_TYPE.VIRTUAL);
		}
		
		for(int i=0;i<nrTargets;i++){
			rows.get(i).get(width-1).setType(NODE_TYPE.TARGET);
			rows.get(i).get(width-1).setPayoff(payoff);
				
			}
		
		return graph;
	
	}
	private static AbstractBaseGraph<INode, ExtDWE> genGraph() {
	 	AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		
	 	Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n9 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		
		Node n6 = new Node(NODE_TYPE.TARGET, 4);
		Node n7 = new Node(NODE_TYPE.TARGET, 5);
		Node n8 = new Node(NODE_TYPE.TARGET, 4);
		//Node n6 = new Node(NODE_TYPE.SOURCE, 0);
		
		graph.addVertex(n0);
		graph.addVertex(n1);
		graph.addVertex(n2);
		graph.addVertex(n3);
		graph.addVertex(n4);
		graph.addVertex(n5);
		graph.addVertex(n6);
		graph.addVertex(n7);
		graph.addVertex(n8);
		graph.addVertex(n9);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		e1.setType(EDGE_TYPE.VIRTUAL);
		ExtDWE e2 = graph.addEdge(n0, n2);
		e2.setType(EDGE_TYPE.VIRTUAL);
		ExtDWE e3 = graph.addEdge(n0, n3);
		e3.setType(EDGE_TYPE.VIRTUAL);
		ExtDWE e4 = graph.addEdge(n0, n4);
		e4.setType(EDGE_TYPE.VIRTUAL);
		
		ExtDWE e5 = graph.addEdge(n1, n6);
		ExtDWE e6 = graph.addEdge(n6, n1);
		
		ExtDWE e7 = graph.addEdge(n1, n8);
		ExtDWE e8 = graph.addEdge(n8, n1);
		
		ExtDWE e9 = graph.addEdge(n2, n3);
		ExtDWE e10 = graph.addEdge(n3, n2);
		
		ExtDWE e11 = graph.addEdge(n3, n7);
		ExtDWE e12 = graph.addEdge(n7, n3);
		
		ExtDWE e13 = graph.addEdge(n4, n5);
		ExtDWE e14 = graph.addEdge(n5, n4);
		
		ExtDWE e15 = graph.addEdge(n5, n7);
		ExtDWE e16 = graph.addEdge(n7, n5);
		
		ExtDWE e17 = graph.addEdge(n5, n9);
		ExtDWE e18 = graph.addEdge(n9, n5);
		
		ExtDWE e20 = graph.addEdge(n9, n6);
		ExtDWE e21 = graph.addEdge(n6, n9);
		
		ExtDWE e22 = graph.addEdge(n3, n7);
		ExtDWE e23 = graph.addEdge(n7, n3);

		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);	
		edgeList.add(e4);	
		edgeList.add(e5);	
		edgeList.add(e6);	
		edgeList.add(e7);	
		edgeList.add(e8);	
		edgeList.add(e9);	
		edgeList.add(e10);	
		edgeList.add(e11);
		edgeList.add(e12);
		edgeList.add(e13);
		edgeList.add(e14);
		edgeList.add(e15);
		edgeList.add(e16);
		edgeList.add(e17);
		edgeList.add(e18);
		edgeList.add(e20);
		edgeList.add(e21);
		edgeList.add(e22);
		edgeList.add(e23);
		
		
		
		
		return graph;
		
		
		
	}
	private static AbstractBaseGraph<INode, ExtDWE> genTestGraph() {
	 	AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		
	 	Node n0 = new Node(NODE_TYPE.TARGET, 5);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.SOURCE, 0);
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
		ExtDWE e2 = graph.addEdge(n1, n0);
		
		ExtDWE e3 = graph.addEdge(n1, n2);
		ExtDWE e4 = graph.addEdge(n2, n1);
		
		//ExtDWE e5 = graph.addEdge(n2, n5);
		ExtDWE e6 = graph.addEdge(n5, n2);
		
		ExtDWE e7 = graph.addEdge(n0, n3);
		ExtDWE e8 = graph.addEdge(n3, n0);
		
		ExtDWE e9 = graph.addEdge(n3, n4);
		ExtDWE e10 = graph.addEdge(n4, n3);
		
		//ExtDWE e11 = graph.addEdge(n4, n5);
		ExtDWE e12 = graph.addEdge(n5, n4);

		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);	
		edgeList.add(e4);	
		//edgeList.add(e5);	
		edgeList.add(e6);	
		e6.setType(EDGE_TYPE.VIRTUAL);
		edgeList.add(e7);	
		edgeList.add(e8);	
		edgeList.add(e9);	
		edgeList.add(e10);	
		//edgeList.add(e11);
		edgeList.add(e12);	
		e12.setType(EDGE_TYPE.VIRTUAL);
		
		
		return graph;
		
		
		
	}
	
	private static AbstractBaseGraph<INode, ExtDWE> RandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r){
		Configuration.initialize(108374);
		Random random = new Random();
		AbstractBaseGraph<INode, ExtDWE> graph =  GraphGenerator.getRandomGeometricGraph(numNodes, nrOfSources, nrOfTargets, r,
			10, random);
		List<ExtDWE> edgestoRemove = new ArrayList<ExtDWE>();
		for(Iterator<ExtDWE> edges = graph.edgeSet().iterator();edges.hasNext();){
        	ExtDWE e = edges.next();
        	INode n1 = e.getsource();
        	INode n2 = e.gettarget();

        	if(n1.getType()== NODE_TYPE.TARGET && n2.getType()== NODE_TYPE.TARGET){
        		edgestoRemove.add(e);
        	}
        	
        }
		graph.removeAllEdges(edgestoRemove);
		List<INode> nodestoRemove = new ArrayList<INode>();
		for(Iterator<INode> nodes = graph.vertexSet().iterator();nodes.hasNext();){
        	INode n = nodes.next();
        	if(n.getType()==NODE_TYPE.TARGET && graph.inDegreeOf(n) == 0){
        		nodestoRemove.add(n);
        	}
        }
		
		graph.removeAllVertices(nodestoRemove);
		
		return graph;
	}
	
	public static void testBetterResp(TeamBuildingProblem uspObj){

		boolean ENABLE_FILE_WRITE = true;
		
		
		String CONFIG = "a zS"; 
		/**
		 * CONFIG: s: skeleton f: skeleton - better defender response t:
		 * skeleton - better attacker response d: better defender response a:
		 * better attacker response o: converge only with better responses w:
		 * converge with better responses first, then use best responses
		 */
		
		double r = 0.1;	
		double betterResponseEpsilon = 0.001;
		double finalConvergenceEpsilon = 0.002;
		int targetVal = 100; // Random integer between 1 and this number

		System.out.flush();
		finalConvergenceEpsilon = 0.002;

		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(uspObj,5);
		ruggedObj.setNumRoundsBeforeDiscard(100);
		ruggedObj.setEnableDiscardUselessActions(false);

		// ruggedObj.setkShortestPaths(2);
		
		ruggedObj.setEnableSkeleton(CONFIG.contains("s"), CONFIG.contains("f"),
				CONFIG.contains("t"));
		ruggedObj.setEnableBetterDefenderOracle(CONFIG.contains("d"));
		ruggedObj.setEnableBetterAttackerOracle(CONFIG.contains("a"));
		ruggedObj.setOnlyWarmStartConverge(CONFIG.contains("o"));
		ruggedObj.setEnableWarmStartConverge(CONFIG.contains("w"));

		ruggedObj.setFinalConvergenceEpsilon(finalConvergenceEpsilon);
		ruggedObj.setBetterResponseEpsilon(betterResponseEpsilon);
								
		int warmStartCount = 5;

		
		List<model.teamBuildingModels.DefenderAllocation> daList = null;
		if ( CONFIG.contains("qM") ) {
			daList = ruggedObj.getWarmStartDefenderMinCutEdges(warmStartCount);
		} else if ( CONFIG.contains("qE") ) {
			daList = ruggedObj.getWarmStartDefenderRandomEdges(warmStartCount);
		} else if ( CONFIG.contains("qR") ) {
			//daList = ruggedObj.getWarmStartDefenderRanger();	
		}
		
		List<model.teamBuildingModels.AdversaryPath> apList = null;
		if ( CONFIG.contains("zR")) {
			apList = ruggedObj.getWarmStartAttackerRanger();	
		} else if ( CONFIG.contains("zS")) {
			apList = ruggedObj.getWarmStartAttackerShortestPath();
		}				 
		
		ruggedObj.warmStart(daList, apList);
		
		
		//System.out.println("Warm Start Strategy Count: " + ruggedObj.getDefenderAllocations().size() + ", " + ruggedObj.getAdversaryPaths().size() );
		System.out.flush();
		System.err.flush();
				
		
		try {
			ruggedObj.run();
		} catch (MalformedGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
		}

		System.out.flush();
		System.err.flush();
		
		
//		System.out.println("Final Size of matrix: "
//				+ ruggedObj.getDefenderAllocations().size() + " x "
//				+ ruggedObj.getAdversaryPaths().size());
//		System.out.println("Final Load Time for problems: "
//				+ ruggedObj.getLoadTime() / 1000.0);
//		ruggedObj.printRunTimeBreakup();
//		System.out.println("Final Average Path Length: "
//				+ ruggedObj.getAveragePathLength());
//		
		
		System.out.println("\n \n GameValue: "
				+ (ruggedObj.getGameValue()));
		System.out.println("Final Run Time: "
				+ (ruggedObj.getRunTime() * 1.0 / 1000));
//		System.out.println("Final Number of Iterations: "
//				+ ruggedObj.getNumberOfIterations());
//		System.out.println("Final Total Time: "
//				+ (ruggedObj.getTotalTime() / 1000.00));
//		
//		System.out.println("Strategy: ");
//		List<model.teamBuildingModels.DefenderAllocation> dalist = ruggedObj.getDefenderAllocations();
//		List<Double> strategy = ruggedObj.getDefenderStrategy();
//		assert(dalist.size() == strategy.size());
//		for ( int i = 0; i < strategy.size(); i++ ) {
//			if ( strategy.get(i) > lpWrapper.Configuration.EPSILON) {
//				System.out.println(dalist.get(i) + ": " + strategy.get(i));
//			}
//		}
		
		
		int j=0;
		for(int i=0;i<ruggedObj.getDefenderStrategy().size();i++){
			if(!ruggedObj.getDefenderStrategy().get(i).equals(0.0)){
				j++;
				System.out.println("Defender Strategy "+j+" : "
						+ ruggedObj.getDefenderStrategy().get(i));
				model.teamBuildingModels.DefenderAllocation da = ruggedObj.getDefenderAllocations().get(i);
				for(String r1 :da.getResourceMap().keySet()){
					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
				}
				System.out.println();
			}
			
		}

		//System.out.println("Adversary Strategy: "+ ruggedObj.getAdversaryStrategy());
		
		
		for(int i=0;i<ruggedObj.getAdversaryPaths().size();i++){
			if(ruggedObj.getAdversaryStrategy().get(i)>0.00001){
				System.out.println(ruggedObj.getAdversaryPaths().get(i)+": "+ruggedObj.getAdversaryStrategy().get(i));
			}
		}
		//System.out.println("Adversary Paths: " + ruggedObj.getAdversaryPaths());
		
		
		
		//ruggedObj.writeMeasure();
		System.out.flush();
		System.err.flush();
		//System.out.print(ruggedObj.getcutoffUsed());
		ruggedObj.end();

		

}
	public static void testSearch(AbstractBaseGraph graph, double budget, int cutoff) throws MalformedGraphException, LPSolverException{
		
		cutoffSearch.run(graph, budget, cutoff, false);
		//cutoffSearch.run(graph, budget, cutoff, true);
	}
	
	public static void testSolver(AbstractBaseGraph graph, List resources, List coverage) throws LPSolverException{
		List<Double> P = new ArrayList<Double>();
		coverage.add(0.5);
		
		TeamBuildingProblem team = new TeamBuildingProblem(resources, coverage, P);
		team.setGraph(graph);
		
		//testBetterResp(team);
		System.out.println();
		
		
	
		QuickSolve test = new QuickSolve(team);
		System.out.println(test.runP()+"\n");
		System.out.println(test.runtime+"\n");
		
		for(int i=0;i<test.attackerPaths.size();i++){
			System.out.println(test.attackerPaths.get(i)+": "+test.strategy.getAttackerStrategy().get(i));
		}
		
		
		for(int i=0;i<test.strategy.getDefenderStrategy().size();i++){
			double p = test.strategy.getDefenderStrategy().get(i);
			if(p>0){
				System.out.println(p+": "+test.strategy.defenderAllocation.get(i).defenderCoverage);
			}
		}
		
		

	}
	public static void main(String[] args) throws LPSolverException{
		
		int i = 4;
		//AbstractBaseGraph graph = GridGraph(i,i,i,i,1);
		//AbstractBaseGraph graph = RandomGraph( 10, 4, 4, 0.2);
		AbstractBaseGraph graph = genGraph();
		
		//GraphVisual g = new GraphVisual(graph);
		//g.run();
		
		
		List<Double> resources = new ArrayList<Double>();
		resources.add(1.0);
		//resources.add(10.0);

		List<Double> coverage = new ArrayList<Double>();
		coverage.add(3.0);
		//coverage.add(5.0);

		//testSearch(graph,30,5);
		testSolver(graph, resources, coverage);
		
		
	}
}
