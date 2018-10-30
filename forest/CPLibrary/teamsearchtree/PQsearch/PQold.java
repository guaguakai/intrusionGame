package PQsearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.graph.AbstractBaseGraph;

import teamBuildingSolvers.RuggedBetterResponsesCutoff;


public class PQold {
	static double totalTime;
	
	public PQold(){
		
	}
	
	public static void CreateTeams(double budget) throws FileNotFoundException{
		TeamsPermuteCheck.create(budget);
	}
	public static teamNode Search(AbstractBaseGraph graph, double mincost) throws LPSolverException,MalformedGraphException, FileNotFoundException {
		
		
		// read in input and print output
		Scanner scanner = new Scanner(new File("output.txt"));

		double startTime = System.currentTimeMillis();
		
		// name of each team
		int teamCostBound = (int) mincost;
		String teamName = "";
		// skip first line
		scanner.nextLine();
		int teamresource = 0;
		int teamcost = 0;
		teamNode optimal = new teamNode();
		ArrayList<teamNode> teams = new ArrayList<teamNode>();
		PriorityQueue<teamNode> pq = new PriorityQueue<teamNode>();
		
		// set coverages
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(2.0);
		coverage.add(3.0);
		// coverage.add(1.0);
		coverage.add(5.0);
		
		List<Double> Prob = new ArrayList<Double>();
		Prob.add(0.5);
		Prob.add(0.5);
		// coverage.add(1.0);
		Prob.add(0.5);
		
		while (scanner.hasNext()) {
			// skip resources
			scanner.next();
		
			// add team resources
			List<Double> resources = new ArrayList<Double>();
			teamresource = scanner.nextInt();
			resources.add((double) teamresource);
			teamName += teamresource;
			teamresource = scanner.nextInt();
			resources.add((double) teamresource);
			teamName += teamresource;
			// teamresource = scanner.nextInt();
			// resources.add((double) teamresource);
			// teamName += teamresource;
			teamresource = scanner.nextInt();
			resources.add((double) teamresource);
			teamName += teamresource;
			// System.out.println("Team: "+teamName);
		
			// skip over the cost and heuristic
			teamcost = scanner.nextInt();
			scanner.nextLine();
		
			if (teamcost > teamCostBound) {
				teamNode n = new teamNode(teamName, resources);
				// System.out.println(n.getName());
				teams.add(n);
				pq.add(n);
			}
			teamName = "";
		}
		
		PriorityQueue<teamNode> pq1 = new PriorityQueue<teamNode>();
		int pqsize = pq.size();
		int teamsize = teams.size();
		
		long starttime = 0;
		long endtimesingle = 0;
		// System.out.println("pq size: "+pq.size());
		
		//for (int i = 0; i < pqsize; i++) {
		for(int i=0; i<teamsize; i++){
			teamNode n = pq.poll();
			//teamNode n = teams.get(0);
			//System.out.println(n.getName()+","+n.getEvaluation());
		
			starttime = System.currentTimeMillis();
			
			TeamBuildingProblem uspObj = new TeamBuildingProblem(
					n.getResourceList(), coverage);
			uspObj.setGraph(graph);
		
			RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(uspObj, 5);
			ruggedObj.run();
			n.setEvaluation(ruggedObj.getGameValue());
			n.setCutoff(5);
			endtimesingle = System.currentTimeMillis();
			//System.out.println(i+" : "+" team: "+n.getName()+" utility: "+n.getEvaluation()+" Runtime: "+(endtimesingle-starttime));
			pq1.add(n);
			
			// System.out.println(n.getName()+", "+n.getEvaluation());
		}
		
		while (!pq1.isEmpty()) {
			teamNode test = pq1.poll();
			System.out.println("Team: "+test.getName());
		
			TeamBuildingProblem uspObj = new TeamBuildingProblem(
					test.getResourceList(), coverage);
			uspObj.setGraph(graph);
		
			//rugged Cutoff
			RuggedBetterResponsesCutoff ruggedObj;
			
			// ruggedcutoff
			do {
				
				
				TeamBuildingProblem uspObj1 = new TeamBuildingProblem(test.getResourceList(), coverage);
				uspObj1.setGraph(graph);
				
				ruggedObj = new RuggedBetterResponsesCutoff(uspObj1, test.getCutoff() + 25);
				if(test.getEvaluation() < optimal.getEvaluation()){
					//System.out.println("we shouldnt test this node");
					break;
				}
				ruggedObj.run();
				test.setEvaluation(ruggedObj.getGameValue());
				if (ruggedObj.getcutoffUsed() == true) {
					//System.out.println("We done add more yo");
					if(test.getCutoff() < 50)
						test.setCutoff(test.getCutoff() + 25);
					else
						test.setCutoff(test.getCutoff() + 100);
				} else if (ruggedObj.getcutoffUsed() == false) {
					//System.out.println("We done break yo");
					test.setCutoff(ruggedObj.getNumberOfIterations());
					break;
				}
				// System.out.println(test.getEvaluation()+","+pq1.peek().getEvaluation());
				//System.out.println(test.getCutoff()+" Used"+ruggedObj.getcutoffUsed());
			} while (test.getEvaluation() >= pq1.peek().getEvaluation());
		
			//uspObj.getGraph().edgeSet().size();
			
			//set optimal equal to current node if optimal node is not set
			//or the optimal node's evaluation is higher than the current node
			if (optimal.getName().equals("")) {
				optimal = test;
				// System.out.println(optimal.getEvaluation()+", "+optimal.getName());
			} else if (optimal.getEvaluation() < test.getEvaluation()
					|| optimal.getName().equals(test.getName())) {
				optimal = test;
				// System.out.println(optimal.getEvaluation()+", "+optimal.getName());
			}
			/*
			System.out.println(test.getName() + ", " + test.getEvaluation()
					+ ", " + optimal.getName() + ", " + optimal.getEvaluation()
					+ ", " + test.getCutoff());*/
			pq1.add(test);
		
			
			//break out of loop if the cutoff isn't used and the current node is the optimal node
			if(ruggedObj.getcutoffUsed() == false) {
				//System.out.println("We done break yo");
				break;
			}
		
		}
		
		totalTime = (System.currentTimeMillis() - startTime) / 1000;
		return optimal;
		
		/*PrintWriter output = new PrintWriter(new File("outputOptimalTeam"
				+ optimal.getName() + "_" + teamCostBound + ".txt"));
		
		output.print("Team: " + optimal.getName() + " Payoff: "
				+ optimal.getEvaluation() + " Cutoff: " + optimal.getCutoff()
				+ " Runtime: " + totalTime);
		output.println();
		
		
		output.close();*/
		
		}

}
