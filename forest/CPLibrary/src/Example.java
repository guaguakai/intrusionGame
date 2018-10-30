import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import lpWrapper.LPSolverException;
import model.Configuration;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.graph.AbstractBaseGraph;

import search.teamNode;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;


public class Example {
	
	
	public static RuggedBetterResponsesCutoff runSolver(AbstractBaseGraph graph, List<Double> resources,List<Double> coverage, List<Double> probability){
			
			
			TeamBuildingProblem teamObj = new TeamBuildingProblem(resources, coverage, probability);

			int numNodes = graph.vertexSet().size();

			boolean ENABLE_FILE_WRITE = true;

			String CONFIG = "d a zS"; 
	
			double r = 0.1;	
			double betterResponseEpsilon = 0.001;
			double finalConvergenceEpsilon = 0.002;	
			int targetVal = 100; // Random integer between 1 and this number
			System.out.flush();
			finalConvergenceEpsilon = 0.002;

			teamObj.setGraph(graph);
			RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(teamObj);
			
			ruggedObj.setNumRoundsBeforeDiscard(100);
			ruggedObj.setEnableDiscardUselessActions(false);

			ruggedObj.setEnableSkeleton(CONFIG.contains("s"), CONFIG.contains("f"),
					CONFIG.contains("t"));
			ruggedObj.setEnableBetterDefenderOracle(CONFIG.contains("d"));
			ruggedObj.setEnableBetterAttackerOracle(CONFIG.contains("a"));
			ruggedObj.setOnlyWarmStartConverge(CONFIG.contains("o"));
			ruggedObj.setEnableWarmStartConverge(CONFIG.contains("w"));

			ruggedObj.setFinalConvergenceEpsilon(finalConvergenceEpsilon);
			ruggedObj.setBetterResponseEpsilon(betterResponseEpsilon);
									
			int warmStartCount = 15;

			
			List<model.teamBuildingModels.DefenderAllocation> daList = null;
			if ( CONFIG.contains("qM") ) {
				daList = ruggedObj.getWarmStartDefenderMinCutEdges(1);
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

			try {
				ruggedObj.run();
			} catch (MalformedGraphException e) {
				e.printStackTrace();
			} catch (LPSolverException e) {
				e.printStackTrace();
			}

			System.out.flush();
			System.err.flush();

			return ruggedObj;
	}
	
	public static void main(String[] args) throws MalformedGraphException, LPSolverException ,FileNotFoundException{		

		Configuration.initialize(108374);
		
		//this is the graph to be used as the domain
		AbstractBaseGraph graph;
		
		//The Graphs class stores some graph types you can use
		//this is the madagascar graph
		//there is also a grid graph and a randomly distributed 
		graph = Graphs.getMadagascarGraph();
		
		
		//the resources are kept in a list of doubles
		List<Double> resources = new ArrayList<Double>();
		resources.add(2.0);	//this adds two resources of type 1
		resources.add(1.0);	//this adds one resource of type 2
	
		//this describes the edges coverage of each resource type
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(1.0);	//type 1 can cover 1 edge
		coverage.add(2.0);	//type 2 can cover 2 edges
		
		//this describes the coverage probablity of each resource along each edge
		//i would suggest not including this in your initial tests as it makes the code run slower
		List<Double> probability = new ArrayList<Double>();
		probability.add(0.5);
		probability.add(0.5);
		
		RuggedBetterResponsesCutoff solution = runSolver(graph,resources, coverage, null);
		
		//change to the next line to include probablity coverage 
		//runSolver(graph,resources, coverage, probability);
		
		
		//GameValue 
		solution.getGameValue();
		
		//runtime
		solution.getRunTime();
		
		//defender solution
		List<Double> strategy = solution.getDefenderStrategy(); //list of probabilities of each pure strategy
		List<DefenderAllocation> dalist = solution.getDefenderAllocations(); //list of pure strategies (defender allocations)
		
		
		//this will print out all the edges assigned to each resource for each strategy
		int i =0;
		for(DefenderAllocation da : dalist){
			System.out.println("Strategy"+i+" played with probablity "+strategy.get(i));
			for(String r1 :da.getResourceMap().keySet()){
				System.out.println(r1+ ":" + da.getResourceMap().get(r1)+"\n");
			}
			i++;
		}
		
	
		//attacker solution
		List<Double> strategy_a = solution.getAdversaryStrategy();
		List<AdversaryPath> ap = solution.getAdversaryPaths();


	
}
}
