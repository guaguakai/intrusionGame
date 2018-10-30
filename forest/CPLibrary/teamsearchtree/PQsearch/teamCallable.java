package PQsearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.graphutils.INode.NODE_TYPE;
import model.teamBuildingModels.TeamBuildingProblem;
import teamBuildingSolvers.RuggedCutoff;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;

public class teamCallable implements Callable<teamNode> {

	// set coverages
	private List<Double> coverage = new ArrayList<Double>();
		
	private teamNode n;
	private TeamBuildingProblem teamObj;
	private AbstractBaseGraph<INode, ExtDWE> graph;
		
	//set to -100 because we would never get close to this negative of a value
	private double bound = -100;
	private boolean cutoffUsed = true;
	
	public teamCallable(teamNode n, List<Double> coverage, int cutoff, AbstractBaseGraph<INode, ExtDWE> graph){
		this.n = n;
		this.coverage = coverage;
		n.setCutoff(cutoff);
		this.graph = graph;
	}
	
	@Override
	public teamNode call() throws Exception {
		// TODO Auto-generated method stub
		PrintWriter output = null;
		try {
			output = new PrintWriter(new FileWriter(new File("ParallelRunTimes.txt"), true));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//run through node
		long starttime = System.currentTimeMillis();
		teamObj = new TeamBuildingProblem(
				n.getResourceList(), coverage);
		teamObj.setGraph(graph);
		
		//RuggedCutoff ruggedObj = new RuggedCutoff(teamObj, 5);
		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(teamObj, n.getCutoff() + 25);
		//ruggedObj.setEnableBetterDefenderOracle(false);
		
		try {
			ruggedObj.run();
		} catch (MalformedGraphException e) {
			e.printStackTrace();
		} catch (LPSolverException e) {
			e.printStackTrace();
		}
		
		n.setTeamObj(teamObj);
		n.setEvaluation(ruggedObj.getGameValue());
		long endtime = System.currentTimeMillis();
		output.println(n.getName()+", "+n.getEvaluation()+", "+n.getCutoff()+", "+(endtime-starttime));
		//System.out.println("Team: "+n.getName()+" Eval: "+n.getEvaluation()+" Cutoff: "+n.getCutoff()+" Runtime:"+(endtime-starttime));
		n.setCutoff(n.getCutoff()+25);
		output.close();
		
		return n;
	}
	
	

}
