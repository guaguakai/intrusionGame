package PQsearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import lpWrapper.LPSolverException;
import model.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.graphutils.INode.NODE_TYPE;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import teamBuildingSolvers.RuggedBetterResponses;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;
import teamBuildingSolvers.RuggedCutoff;

public class CompareParallel {
	
	public static boolean useProb=true;
	
	public static void runParallelPQ(int teamCostBound, AbstractBaseGraph<INode, ExtDWE> graph, int initialcutoff) throws LPSolverException,
	MalformedGraphException, FileNotFoundException {

		//number of cores available
		int cores = Runtime.getRuntime().availableProcessors();
		PrintWriter output2 = null;
		try {
			output2 = new PrintWriter(new FileWriter(new File("ParallelRunTimes.txt"), true));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output2.println("Cores: "+cores);
		output2.println("team, eval, cutoff, runtime");
		output2.close();
		//System.out.println(cores);
		
		teamNode optimal = new teamNode();
		ArrayList<teamNode> teams = readTeams(teamCostBound);
		
		double startTime = System.currentTimeMillis();
		
		//create executor service to run threads
		ExecutorService executor = Executors.newFixedThreadPool(cores);
		
		//pq for teams
		PriorityQueue<teamNode> pq1 = new PriorityQueue<teamNode>();
		//int pqsize = pq.size();
		int teamsize = teams.size();
		//random int generator
		Random randomGenerator = new Random();
		
		// set coverages
		List<Double> coverage = TeamStats.getCoverage();
//		coverage.add(2.0);
//		coverage.add(3.0);
//		// coverage.add(1.0);
//		coverage.add(5.0);
		
		//for (int i = 0; i < pqsize; i++) {
		for(int i=0; i<teamsize; i++){
			teamNode n = teams.remove(0);
			//int random = randomGenerator.nextInt(teams.size());
			//teamNode n = teams.remove(random);
			
			//create new thread and add it to executor queue
			//teamRunnable t = new teamRunnable(n, coverage, initialcutoff, graph);
			//teamThread t = new teamThread(n, coverage, uspObj);
			//executor.submit(t);
			//executor.execute(t);
			
			//try to run callable here to return node
			teamCallable task = new teamCallable(n, coverage, initialcutoff, graph);
			
			Future<teamNode> futureNode =  executor.submit(task);
			
			//System.out.println(i+" : "+" team: "+t.getNode().getName()+" utility: "+t.getNode().getEvaluation());
			try {
				//System.out.println(i+" : "+" team: "+futureNode.get().getName()+" utility: "+futureNode.get().getEvaluation());
				
				pq1.add(futureNode.get());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//pq1.add(t.getNode());
			//System.out.println(n.getName()+", "+n.getEvaluation());
		}
		
		executor.shutdown();
		//executor.shutdownNow();
		// Wait until all threads are finish
		while (!executor.isTerminated()) {
		
		}
		//System.out.println("\nFinished all threads\n");
		
		//sequential program
		while (!pq1.isEmpty()) {
			teamNode test = pq1.poll();
			//System.out.println("Team: "+test.getName());
		
			//rugged Cutoff
			RuggedCutoff ruggedObj;
			
			// ruggedcutoff
			do {
				
				ruggedObj = new RuggedCutoff(test.getTeamObj(), test.getCutoff() + 25);
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
		
			/*System.out.println(test.getName() + ", " + test.getEvaluation()
					+ ", " + optimal.getName() + ", " + optimal.getEvaluation()
					+ ", " + test.getCutoff());*/
			pq1.add(test);
		
			
			//break out of loop if the cutoff isn't used and the current node is the optimal node
			if(ruggedObj.getcutoffUsed() == false && optimal.getEvaluation() >= pq1.peek().getEvaluation()) {
				//System.out.println("We done break yo");
				break;
			}
		
		}
		
		double totalTime = (System.currentTimeMillis() - startTime) / 1000;
		
		PrintWriter output = new PrintWriter(new File("outputOptimalTeam"
				+ optimal.getName() + "_" + teamCostBound + ".txt"));
		
		output.print("Team: " + optimal.getName() + " Payoff: "
				+ optimal.getEvaluation() + " Cutoff: " + optimal.getCutoff()
				+ " Runtime: " + totalTime);
		output.println();
		
		PrintWriter output3 = null;
		try {
			output3 = new PrintWriter(new FileWriter(new File("ParallelRunTimes.txt"), true));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output3.println("Team: " + optimal.getName() + " Payoff: "
				+ optimal.getEvaluation() + " Cutoff: " + optimal.getCutoff()
				+ " Total Runtime: " + totalTime);
		output3.close();
		
		System.out.println("Parallel: "+totalTime);
		
		output.close();
		
		}
	
	public static teamNode runSequentialPQ(AbstractBaseGraph<INode, ExtDWE> graph, int teamCostBound, int initialcutoff, String output) throws LPSolverException,
					MalformedGraphException, FileNotFoundException {
		
		//output writer
		double tick = System.currentTimeMillis();
		PrintWriter output2 = null;
		try {
			output2 = new PrintWriter(new FileWriter(new File("SequentialRunTimes.txt"), true));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		// set coverages
		List<Double> coverage = TeamStats.getCoverage();
//		coverage.add(2.0);
//		coverage.add(3.0);
//		coverage.add(4.0);

		
		List<Double> Prob = TeamStats.getProbability();
//		Prob.add(0.7);
//		Prob.add(0.9);
//		Prob.add(0.7);
////		
		
		ArrayList<teamNode> teams = readTeams(teamCostBound, output);
		double startTime = System.currentTimeMillis();
		teamNode optimal = new teamNode();
		PriorityQueue<teamNode> pq1 = new PriorityQueue<teamNode>();
		int teamsize = teams.size();
		long starttime = 0;
		long endtimesingle = 0;
		System.out.println("team size: "+teams.size());

		
		output2.println("team, utility, cutoff, runtime");
		//for (int i = 0; i < pqsize; i++) {
		for(int i=0; i<teamsize; i++){
			//teamNode n = pq.poll();
			teamNode n = teams.remove(0);
			//System.out.println(n.getName()+","+n.getEvaluation());

			starttime = System.currentTimeMillis();
			
			TeamBuildingProblem uspObj = new TeamBuildingProblem(n.getResourceList(), coverage, Prob);
//			if(useProb){
//				uspObj = new TeamBuildingProblem(n.getResourceList(), coverage, Prob);
//			}else{
//				uspObj = new TeamBuildingProblem(
//					n.getResourceList(), coverage);
//			}
			uspObj.setGraph(graph);

			RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(uspObj, initialcutoff);
			//ruggedObj.setEnableBetterDefenderOracle(false);
			ruggedObj.run();
			n.setTeamObj(uspObj);
			n.setEvaluation(ruggedObj.getGameValue());
			n.setCutoff(initialcutoff);
			endtimesingle = System.currentTimeMillis();
			//System.out.println(i+" : "+" team: "+n.getName()+" utility: "+n.getEvaluation()+" Runtime: "+(endtimesingle-starttime)+","+ruggedObj.getIteration()+","+ruggedObj.getcutoffUsed());
			output2.println(n.getName()+", "+n.getEvaluation()+", "+n.getCutoff()+", "+(endtimesingle-starttime));
			pq1.add(n);
			
			System.out.println(n.getName()+", "+n.getEvaluation());
		}

		while (!pq1.isEmpty()) {
			teamNode test = pq1.poll();
			System.out.println("test is: "+test.getName()+" "+test.getCutoff());
			
			//rugged Cutoff
			RuggedBetterResponsesCutoff ruggedObj;
			
			// ruggedcutoff
			do {
				TeamBuildingProblem uspObj = new TeamBuildingProblem(test.getResourceList(), coverage, Prob);
				uspObj.setGraph(graph);
				ruggedObj = new RuggedBetterResponsesCutoff(uspObj, test.getCutoff() + 25);
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
					System.out.println(ruggedObj.getNumberOfIterations());
					break;
				}
				if((System.currentTimeMillis()-tick)/1000 > 36000){
					test.setEvaluation(999);
					break;
				}
				System.out.println(test.getEvaluation()+","+pq1.peek().getEvaluation());
				//System.out.println(test.getCutoff()+" Used"+ruggedObj.getcutoffUsed());
			} while (test.getEvaluation() >= pq1.peek().getEvaluation());

			//uspObj.getGraph().edgeSet().size();
			
			//set optimal equal to current node if optimal node is not set
			//or the optimal node's evaluation is higher than the current node
			if (optimal.getName().equals("")) {
				optimal = test;
				System.out.println(optimal.getEvaluation()+", "+optimal.getName());
			} else if (optimal.getEvaluation() < test.getEvaluation()
					|| optimal.getName().equals(test.getName())) {
				optimal = test;
				// System.out.println(optimal.getEvaluation()+", "+optimal.getName());
			}

			/*System.out.println(test.getName() + ", " + test.getEvaluation()
					+ ", " + optimal.getName() + ", " + optimal.getEvaluation()
					+ ", " + test.getCutoff());*/
			
			pq1.add(test);
			//System.out.println(ruggedObj.getcutoffUsed());
			
			//break out of loop if the cutoff isn't used and the current node is the optimal node
			if(ruggedObj.getcutoffUsed() == false && optimal.getEvaluation() >= pq1.peek().getEvaluation()) {
				//System.out.println("We done break yo");
				System.out.println("END:" + optimal.getCutoff()+", "+ruggedObj.getGameValue());
				break;
			}

		}

		double totalTime = (System.currentTimeMillis() - startTime) / 1000;
		optimal.time=totalTime;
		
		return optimal;
		/*
		PrintWriter output = new PrintWriter(new File("outputOptimalTeam"
				+ optimal.getName() + "_" + teamCostBound + ".txt"));

		output.print("Team: " + optimal.getName() + " Payoff: "
				+ optimal.getEvaluation() + " Cutoff: " + optimal.getCutoff()
				+ " Runtime: " + totalTime);
		output.println();
		
		output2.println("Team: " + optimal.getName() + " Payoff: "
				+ optimal.getEvaluation() + " Cutoff: " + optimal.getCutoff()
				+ " Total Runtime: " + totalTime);
		output2.close();

		System.out.println("Sequential: "+totalTime);
		
		output.close();*/
		
	}
	
	public static teamNode runBruteForce(AbstractBaseGraph<INode, ExtDWE> graph, int teamCostBound) throws LPSolverException,
			MalformedGraphException, FileNotFoundException {
		
		//output writer
		PrintWriter output2 = null;
		try {
			output2 = new PrintWriter(new FileWriter(new File("BruteForceRunTimes.txt"), true));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		// set coverages
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(2.0);
		coverage.add(3.0);
		// coverage.add(1.0);
		coverage.add(5.0);
		
		ArrayList<teamNode> teams = readTeams(teamCostBound);
		double startTime = System.currentTimeMillis();
		teamNode optimal = new teamNode();
		PriorityQueue<teamNode> pq1 = new PriorityQueue<teamNode>();
		int teamsize = teams.size();
		long starttime = 0;
		long endtimesingle = 0;
		
		output2.println("team, utility, cutoff, runtime");
		for(int i=0; i<teamsize; i++){
			//teamNode n = pq.poll();
			teamNode n = teams.remove(0);
			//System.out.println(n.getName()+","+n.getEvaluation());

			starttime = System.currentTimeMillis();
			
			TeamBuildingProblem uspObj = new TeamBuildingProblem(
					n.getResourceList(), coverage);
			uspObj.setGraph(graph);

			RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(uspObj);
			//ruggedObj.setEnableBetterDefenderOracle(false);
			ruggedObj.run();
			n.setTeamObj(uspObj);
			n.setEvaluation(ruggedObj.getGameValue());
			endtimesingle = System.currentTimeMillis();
			System.out.println(i+" : "+" team: "+n.getName()+" utility: "+n.getEvaluation()+" Runtime: "+(endtimesingle-starttime)+" Iteration: "+ruggedObj.getNumberOfIterations());
			output2.println(n.getName()+", "+n.getEvaluation()+", "+n.getCutoff()+", "+(endtimesingle-starttime));
			
			// System.out.println(n.getName()+", "+n.getEvaluation());
		}
		
		double totalTime = (System.currentTimeMillis() - startTime) / 1000;
		
		return optimal;
		
		/*
		output2.println("Team: " + optimal.getName() + " Payoff: "
				+ optimal.getEvaluation() + " Cutoff: " + optimal.getCutoff()
				+ " Total Runtime: " + totalTime);
		output2.close();

		System.out.println("Brute Force: "+totalTime);
		*/
	}
	
	private static AbstractBaseGraph<INode, ExtDWE> RandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r){
		Configuration.initialize(108374);
		Random random = new Random();
		return GraphGenerator.getRandomGeometricGraph(numNodes, nrOfSources, nrOfTargets, r,
			10, random);
		
		//System.out.println("Number of Edges: " + team.getGraph().edgeSet().size());
	}
	
	public static ArrayList<teamNode> readTeams(int teamCostBound, String output) throws FileNotFoundException{
		//read in teams
				// read in input and print output
				Scanner scanner = new Scanner(output);
						//new File("output.txt"));
						
				double startTime = System.currentTimeMillis();
						
				// name of each team
				//int teamCostBound = 0;
				String teamName = "";
				// skip first line
				scanner.nextLine();
				int teamresource = 0;
				int teamcost = 0;
				int numberofTeams = 0;
				double bound = 0;
				teamNode optimal = new teamNode();
				ArrayList<teamNode> teams = new ArrayList<teamNode>();
				
				
						
						while (scanner.hasNext()) {
							// skip team name
							scanner.next();
							
							numberofTeams++;
						
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
							}
							teamName = "";
						}
						
						return teams;
	}

	public static void main(String[] args) throws FileNotFoundException, MalformedGraphException, LPSolverException {
		// TODO Auto-generated method stub
		try {
			lpWrapper.Configuration.loadLibrariesCplex();
		} catch (IOException e) {
			System.err.println("Couldn't load Cplex.\n");
			e.printStackTrace();
		}
		Configuration.initialize(108374);
		double density = 0.1;
		int teamCostBound = 0;
		int cutoff = 5;
		
		File file = new File("ParallelRunTimes.txt");
		File file2 = new File("SequentialRunTimes.txt");
		File file3 = new File("BruteForceRunTimes.txt");
		
		//loop for nodes in graph
		for(int j=20;j<21;j+=5){
			//loop for density of graph
			for(double k=.15; k<.16; k+=.05){
				density = k;
				AbstractBaseGraph graph = RandomGraph( 25+j, 5, 5, density);	
				//loop for different team costs allowed
				for(int i=0; i<1; i+=5){
					teamCostBound -= i;
					//for(int t=0; t<2; t++){
						
						//write output
						PrintWriter output = null;
						try {
							output = new PrintWriter(new FileWriter(new File("ParallelRunTimes.txt"), true));
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						PrintWriter output2 = null;
						try {
							output2 = new PrintWriter(new FileWriter(new File("SequentialRunTimes.txt"), true));
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						PrintWriter output3 = null;
						try {
							output3 = new PrintWriter(new FileWriter(new File("BruteForceRunTimes.txt"), true));
						} catch (FileNotFoundException e1) {
							e1.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						//System.out.println("Num of Nodes: "+(j+25)+" Edges: "+graph.edgeSet().size());
						output.println("Num of Nodes: "+(j+25)+" Edges: "+graph.edgeSet().size()+" Density: "+density+" TeamCostBound: "+teamCostBound+" Threads: "+1);
						output.close();
						output2.println("Num of Nodes: "+(j+25)+" Edges: "+graph.edgeSet().size()+" Density: "+density+" TeamCostBound: "+teamCostBound+" Threads: "+1);
						output2.close();
						output3.println("Num of Nodes: "+(j+25)+" Edges: "+graph.edgeSet().size()+" Density: "+density+" TeamCostBound: "+teamCostBound+" Threads: "+1);
						output3.close();
						runParallelPQ(teamCostBound, graph, cutoff);
						System.gc();
						runSequentialPQ(graph, teamCostBound, cutoff);
						System.gc();
						runBruteForce(graph, teamCostBound);
					//}
					teamCostBound = 0;
				}
			}
		}
		
	}

}
