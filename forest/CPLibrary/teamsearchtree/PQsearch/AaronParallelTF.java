import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;
import teamBuildingSolvers.Rugged;
import teamBuildingSolvers.RuggedCutoff;

public class AaronParallelTF {

	public static void runRugged() throws LPSolverException,
			MalformedGraphException, FileNotFoundException {

		//number of cores available
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println(cores);
		
		// read in input and print output
		Scanner scanner = new Scanner(new File(
				"C:/Users/Aaron/workspace/TeamFormation/output.txt"));

		double startTime = System.currentTimeMillis();

		// name of each team
		int teamCostBound = 0;
		String teamName = "";
		// skip first line
		scanner.nextLine();
		int teamresource = 0;
		int teamcost = 0;
		int numberofTeams = 0;
		double bound = 0;
		teamNode optimal = new teamNode();
		ArrayList<teamNode> teams = new ArrayList<teamNode>();
		PriorityQueue<teamNode> pq = new PriorityQueue<teamNode>();

		// set coverages
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(2.0);
		coverage.add(3.0);
		// coverage.add(1.0);
		coverage.add(5.0);

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
				pq.add(n);
			}
			teamName = "";
		}
		
		//create executor service to run threads
		ExecutorService executor = Executors.newFixedThreadPool(cores);
		
		//pq for teams
		PriorityQueue<teamNode> pq1 = new PriorityQueue<teamNode>();
		int pqsize = pq.size();
		int teamsize = teams.size();
		AbstractBaseGraph<INode, ExtDWE> graph = genTestGraph();
		//random int generator
	    Random randomGenerator = new Random();
		
		
		//for (int i = 0; i < pqsize; i++) {
		for(int i=0; i<teamsize; i++){
			//teamNode n = pq.poll();
			int random = randomGenerator.nextInt(teams.size());
			teamNode n = teams.remove(random);
			//System.out.println("Team: "+n.getName());
			/*TeamBuildingProblem uspObj = new TeamBuildingProblem(n.getResourceList(), coverage);
			uspObj.setGraph(graph);*/
			//genTestGraph2(uspObj);
			
			//create new thread and add it to executor queue
			teamRunnable t = new teamRunnable(n, coverage, 20);
			//teamThread t = new teamThread(n, coverage, uspObj);
			
			//executor.submit(t);
			executor.execute(t);
			
			//System.out.println(i+" : "+t.getName()+" team: "+t.getNode().getName()+" utility: "+t.getNode().getEvaluation());
			pq1.add(t.getNode());
			//System.out.println(n.getName()+", "+n.getEvaluation());
		}
		
		executor.shutdown();
		//executor.shutdownNow();
		// Wait until all threads are finish
		while (!executor.isTerminated()) {
 
		}
		System.out.println("\nFinished all threads\n");
		
		

		//parallel program
		/*int i=0;
		ExecutorService executorPQ = Executors.newFixedThreadPool(4);
		bound = pq1.peek().getEvaluation();
		
		while (executorPQ.isTerminated() == false) {
			
			if(pq1.isEmpty() != true){
				i++;
				teamNode test = pq1.poll();
			
				//if(i%2 == 0 && !(i%4 == 0)){
					//bound = pq1.peek().getEvaluation();
				//}
				System.out.println("Team: "+test.getName()+" bound: "+bound);
	
				//TeamBuildingProblem uspObj = new TeamBuildingProblem(
					//	test.getResourceList(), coverage);
				//genTestGraph2(uspObj);
				
				//create new thread and add it to executor queue
				teamRunnable t = new teamRunnable(test, coverage, bound);
				//teamThread t = new teamThread(n, coverage, uspObj);
				
				executorPQ.submit(t);
				
				//reset test node
				test = t.getNode();
				System.out.println(t.getCutoffUsed());
				
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
	
				System.out.println(test.getName() + ", " + test.getEvaluation()
						+ ", " + optimal.getName() + ", " + optimal.getEvaluation()
						+ ", " + test.getCutoff());
				
				
				//pq1.add(test);
				
				//break out of loop if the cutoff isn't used and the current node is the optimal node
				if (t.getNode().getEvaluation() >= 0 && t.getCutoffUsed() == false) {
					System.out.println("We done break finale yo");
					executorPQ.shutdownNow();
					break;
				}
				
			}
			
			//System.out.println("Terminated: "+executorPQ.isTerminated());
			//System.out.println("Shutdown: "+executorPQ.isShutdown());
		}*/
		
		//sequential program
		while (!pq1.isEmpty()) {
			teamNode test = pq1.poll();
			System.out.println("Team: "+test.getName());

			TeamBuildingProblem uspObj = new TeamBuildingProblem(
					test.getResourceList(), coverage);
			genTestGraph2(uspObj);

			//rugged Cutoff
			RuggedCutoff ruggedObj;
			
			// ruggedcutoff
			do {
				
				
				TeamBuildingProblem uspObj1 = new TeamBuildingProblem(test.getResourceList(), coverage);
				genTestGraph2(uspObj1);
				
				ruggedObj = new RuggedCutoff(uspObj1, test.getCutoff() + 25);
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

			System.out.println(test.getName() + ", " + test.getEvaluation()
					+ ", " + optimal.getName() + ", " + optimal.getEvaluation()
					+ ", " + test.getCutoff());
			pq1.add(test);

			
			//break out of loop if the cutoff isn't used and the current node is the optimal node
			if(ruggedObj.getcutoffUsed() == false) {
				System.out.println("We done break yo");
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

		System.out.println(totalTime);
		
		output.close();

	}

	private static AbstractBaseGraph<INode, ExtDWE> genTestGraph(){
		
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		
		//Graph 5
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n8 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n9 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n10 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n11 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n12 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n13 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n14 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n15 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n16 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n17 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n18 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n19 = new Node(NODE_TYPE.TARGET, 5);
		Node n20 = new Node(NODE_TYPE.TARGET, 7);
		
		//add vertices
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
		graph.addVertex(n10);
		graph.addVertex(n11);
		graph.addVertex(n12);
		graph.addVertex(n13);
		graph.addVertex(n14);
		graph.addVertex(n15);
		graph.addVertex(n16);
		graph.addVertex(n17);
		graph.addVertex(n18);
		graph.addVertex(n19);
		graph.addVertex(n20);
		
		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		ExtDWE e2 = graph.addEdge(n0, n2);
		ExtDWE e3 = graph.addEdge(n0, n3);
		ExtDWE e4 = graph.addEdge(n0, n4);
		ExtDWE e5 = graph.addEdge(n1, n5);
		ExtDWE e6 = graph.addEdge(n1, n6);
		ExtDWE e7 = graph.addEdge(n2, n5);
		ExtDWE e8 = graph.addEdge(n2, n6);
		ExtDWE e9 = graph.addEdge(n2, n6);
		ExtDWE e10 = graph.addEdge(n3, n6);
		ExtDWE e11 = graph.addEdge(n3, n7);
		ExtDWE e12 = graph.addEdge(n3, n8);
		ExtDWE e13 = graph.addEdge(n4, n7);
		ExtDWE e14 = graph.addEdge(n4, n8);
		ExtDWE e15 = graph.addEdge(n5, n9);
		ExtDWE e16 = graph.addEdge(n6, n9);
		ExtDWE e17 = graph.addEdge(n6, n10);
		ExtDWE e18 = graph.addEdge(n7, n10);
		ExtDWE e19 = graph.addEdge(n8, n10);
		ExtDWE e20 = graph.addEdge(n8, n11);
		ExtDWE e21 = graph.addEdge(n9, n12);
		ExtDWE e22 = graph.addEdge(n9, n13);
		ExtDWE e23 = graph.addEdge(n9, n14);
		ExtDWE e24 = graph.addEdge(n10, n13);
		ExtDWE e25 = graph.addEdge(n10, n14);
		ExtDWE e26 = graph.addEdge(n11, n14);
		ExtDWE e27 = graph.addEdge(n11, n15);
		ExtDWE e28 = graph.addEdge(n12, n16);
		ExtDWE e29 = graph.addEdge(n12, n17);
		ExtDWE e30 = graph.addEdge(n13, n16);
		ExtDWE e31 = graph.addEdge(n13, n17);
		ExtDWE e32 = graph.addEdge(n14, n16);
		ExtDWE e33 = graph.addEdge(n14, n17);
		ExtDWE e34 = graph.addEdge(n14, n18);
		ExtDWE e35 = graph.addEdge(n15, n17);
		ExtDWE e36 = graph.addEdge(n15, n18);
		ExtDWE e37 = graph.addEdge(n16, n19);
		ExtDWE e38 = graph.addEdge(n16, n20);
		ExtDWE e39 = graph.addEdge(n17, n19);
		ExtDWE e40 = graph.addEdge(n17, n20);
		ExtDWE e41 = graph.addEdge(n18, n19);
		ExtDWE e42 = graph.addEdge(n18, n20);
		ExtDWE e43 = graph.addEdge(n1, n2);
		ExtDWE e44 = graph.addEdge(n3, n4);
		ExtDWE e45 = graph.addEdge(n6, n7);
		ExtDWE e46 = graph.addEdge(n7, n8);
		ExtDWE e47 = graph.addEdge(n9, n10);
		ExtDWE e48 = graph.addEdge(n10, n11);
		ExtDWE e49 = graph.addEdge(n12, n13);
		ExtDWE e50 = graph.addEdge(n14, n15);
		ExtDWE e51 = graph.addEdge(n16, n17);
		ExtDWE e52 = graph.addEdge(n17, n18);
		
		/*edgeList.add(e1);
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
		edgeList.add(e19);
		edgeList.add(e20);
		edgeList.add(e21);
		edgeList.add(e22);
		edgeList.add(e23);
		edgeList.add(e24);
		edgeList.add(e25);
		edgeList.add(e26);
		edgeList.add(e27);
		edgeList.add(e28);
		edgeList.add(e29);
		edgeList.add(e30);
		edgeList.add(e31);
		edgeList.add(e32);
		edgeList.add(e33);
		edgeList.add(e34);
		edgeList.add(e35);
		edgeList.add(e36);
		edgeList.add(e37);
		edgeList.add(e38);
		edgeList.add(e39);
		edgeList.add(e40);
		edgeList.add(e41);
		edgeList.add(e42);
		edgeList.add(e43);
		edgeList.add(e44);
		edgeList.add(e45);
		edgeList.add(e46);
		edgeList.add(e47);
		edgeList.add(e48);
		edgeList.add(e49);
		edgeList.add(e50);
		edgeList.add(e51);
		edgeList.add(e52);
		
		return edgeList;*/
		
		return graph;
	}
	
	private static List<ExtDWE> genTestGraph2(TeamBuildingProblem uspObj) {

		/*
		 * //Graph 3_+1 Node n1 = new Node(NODE_TYPE.TARGET, 5); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n4 = new Node(NODE_TYPE.SOURCE,
		 * 0); Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0); Node n6 = new
		 * Node(NODE_TYPE.SOURCE, 0); Node n7 = new Node(NODE_TYPE.SOURCE, 0);
		 * uspObj.getGraph().addVertex(n1); uspObj.getGraph().addVertex(n2);
		 * uspObj.getGraph().addVertex(n3); uspObj.getGraph().addVertex(n4);
		 * uspObj.getGraph().addVertex(n5); uspObj.getGraph().addVertex(n6);
		 * uspObj.getGraph().addVertex(n7); // Generate edges List<ExtDWE>
		 * edgeList = new ArrayList<ExtDWE>(); ExtDWE e1 =
		 * uspObj.getGraph().addEdge(n4, n3); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n3, n2); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n2, n1); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n6, n5); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n5, n1); ExtDWE e6 =
		 * uspObj.getGraph().addEdge(n7, n1); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); edgeList.add(e4);
		 * edgeList.add(e5); edgeList.add(e6); return edgeList;
		 */

		/*
		 * //Graph 3 Node n0 = new Node(NODE_TYPE.SOURCE, 0); Node n1 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n4 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n5 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n6 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n7 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n8 = new Node(NODE_TYPE.TARGET,
		 * 5); Node n9 = new Node(NODE_TYPE.TARGET, 5);
		 * 
		 * uspObj.getGraph().addVertex(n0); uspObj.getGraph().addVertex(n1);
		 * uspObj.getGraph().addVertex(n2); uspObj.getGraph().addVertex(n3);
		 * uspObj.getGraph().addVertex(n4); uspObj.getGraph().addVertex(n5);
		 * uspObj.getGraph().addVertex(n6); uspObj.getGraph().addVertex(n7);
		 * uspObj.getGraph().addVertex(n8); uspObj.getGraph().addVertex(n9);
		 * 
		 * // Generate edges List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		 * ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n0, n2); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n0, n3); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n1, n4); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n2, n5); ExtDWE e6 =
		 * uspObj.getGraph().addEdge(n3, n6); ExtDWE e7 =
		 * uspObj.getGraph().addEdge(n1, n5); ExtDWE e8 =
		 * uspObj.getGraph().addEdge(n2, n4); ExtDWE e9 =
		 * uspObj.getGraph().addEdge(n2, n6); ExtDWE e10 =
		 * uspObj.getGraph().addEdge(n3, n5); ExtDWE e11 =
		 * uspObj.getGraph().addEdge(n4, n7); ExtDWE e12 =
		 * uspObj.getGraph().addEdge(n5, n8); ExtDWE e13 =
		 * uspObj.getGraph().addEdge(n6, n9);
		 * 
		 * edgeList.add(e1); edgeList.add(e2); edgeList.add(e3);
		 * edgeList.add(e4); edgeList.add(e5); edgeList.add(e6);
		 * edgeList.add(e7); edgeList.add(e8); edgeList.add(e9);
		 * edgeList.add(e10); edgeList.add(e11); edgeList.add(e12);
		 * edgeList.add(e13);
		 * 
		 * return edgeList;
		 */

		/*
		// Graph 4
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n8 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n9 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n10 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n11 = new Node(NODE_TYPE.TARGET, 5);
		Node n12 = new Node(NODE_TYPE.TARGET, 3);
		Node n13 = new Node(NODE_TYPE.TARGET, 7);

		uspObj.getGraph().addVertex(n0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);
		uspObj.getGraph().addVertex(n5);
		uspObj.getGraph().addVertex(n6);
		uspObj.getGraph().addVertex(n7);
		uspObj.getGraph().addVertex(n8);
		uspObj.getGraph().addVertex(n9);
		uspObj.getGraph().addVertex(n10);
		uspObj.getGraph().addVertex(n11);
		uspObj.getGraph().addVertex(n12);
		uspObj.getGraph().addVertex(n13);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1);
		ExtDWE e2 = uspObj.getGraph().addEdge(n0, n2);
		ExtDWE e3 = uspObj.getGraph().addEdge(n0, n3);
		ExtDWE e4 = uspObj.getGraph().addEdge(n0, n4);
		ExtDWE e5 = uspObj.getGraph().addEdge(n1, n2);
		ExtDWE e6 = uspObj.getGraph().addEdge(n3, n4);
		ExtDWE e7 = uspObj.getGraph().addEdge(n1, n5);
		ExtDWE e8 = uspObj.getGraph().addEdge(n1, n6);
		ExtDWE e9 = uspObj.getGraph().addEdge(n2, n5);
		ExtDWE e10 = uspObj.getGraph().addEdge(n2, n6);
		ExtDWE e11 = uspObj.getGraph().addEdge(n3, n6);
		ExtDWE e12 = uspObj.getGraph().addEdge(n4, n6);
		ExtDWE e13 = uspObj.getGraph().addEdge(n4, n7);
		ExtDWE e14 = uspObj.getGraph().addEdge(n5, n8);
		ExtDWE e15 = uspObj.getGraph().addEdge(n5, n9);
		ExtDWE e16 = uspObj.getGraph().addEdge(n6, n8);
		ExtDWE e17 = uspObj.getGraph().addEdge(n6, n9);
		ExtDWE e18 = uspObj.getGraph().addEdge(n6, n10);
		ExtDWE e19 = uspObj.getGraph().addEdge(n6, n7);
		ExtDWE e20 = uspObj.getGraph().addEdge(n7, n9);
		ExtDWE e21 = uspObj.getGraph().addEdge(n7, n10);
		ExtDWE e22 = uspObj.getGraph().addEdge(n8, n11);
		ExtDWE e23 = uspObj.getGraph().addEdge(n8, n12);
		ExtDWE e24 = uspObj.getGraph().addEdge(n8, n13);
		ExtDWE e25 = uspObj.getGraph().addEdge(n9, n11);
		ExtDWE e26 = uspObj.getGraph().addEdge(n9, n12);
		ExtDWE e27 = uspObj.getGraph().addEdge(n10, n12);
		ExtDWE e28 = uspObj.getGraph().addEdge(n10, n13);

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
		edgeList.add(e19);
		edgeList.add(e20);
		edgeList.add(e21);
		edgeList.add(e22);
		edgeList.add(e23);
		edgeList.add(e24);
		edgeList.add(e25);
		edgeList.add(e26);
		edgeList.add(e27);
		edgeList.add(e28);

		return edgeList;
		*/
		
		//Graph 5
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n8 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n9 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n10 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n11 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n12 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n13 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n14 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n15 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n16 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n17 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n18 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n19 = new Node(NODE_TYPE.TARGET, 5);
		Node n20 = new Node(NODE_TYPE.TARGET, 7);
		
		uspObj.getGraph().addVertex(n0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);
		uspObj.getGraph().addVertex(n5);
		uspObj.getGraph().addVertex(n6);
		uspObj.getGraph().addVertex(n7);
		uspObj.getGraph().addVertex(n8);
		uspObj.getGraph().addVertex(n9);
		uspObj.getGraph().addVertex(n10);
		uspObj.getGraph().addVertex(n11);
		uspObj.getGraph().addVertex(n12);
		uspObj.getGraph().addVertex(n13);
		uspObj.getGraph().addVertex(n14);
		uspObj.getGraph().addVertex(n15);
		uspObj.getGraph().addVertex(n16);
		uspObj.getGraph().addVertex(n17);
		uspObj.getGraph().addVertex(n18);
		uspObj.getGraph().addVertex(n19);
		uspObj.getGraph().addVertex(n20);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1);
		ExtDWE e2 = uspObj.getGraph().addEdge(n0, n2);
		ExtDWE e3 = uspObj.getGraph().addEdge(n0, n3);
		ExtDWE e4 = uspObj.getGraph().addEdge(n0, n4);
		ExtDWE e5 = uspObj.getGraph().addEdge(n1, n5);
		ExtDWE e6 = uspObj.getGraph().addEdge(n1, n6);
		ExtDWE e7 = uspObj.getGraph().addEdge(n2, n5);
		ExtDWE e8 = uspObj.getGraph().addEdge(n2, n6);
		ExtDWE e9 = uspObj.getGraph().addEdge(n2, n6);
		ExtDWE e10 = uspObj.getGraph().addEdge(n3, n6);
		ExtDWE e11 = uspObj.getGraph().addEdge(n3, n7);
		ExtDWE e12 = uspObj.getGraph().addEdge(n3, n8);
		ExtDWE e13 = uspObj.getGraph().addEdge(n4, n7);
		ExtDWE e14 = uspObj.getGraph().addEdge(n4, n8);
		ExtDWE e15 = uspObj.getGraph().addEdge(n5, n9);
		ExtDWE e16 = uspObj.getGraph().addEdge(n6, n9);
		ExtDWE e17 = uspObj.getGraph().addEdge(n6, n10);
		ExtDWE e18 = uspObj.getGraph().addEdge(n7, n10);
		ExtDWE e19 = uspObj.getGraph().addEdge(n8, n10);
		ExtDWE e20 = uspObj.getGraph().addEdge(n8, n11);
		ExtDWE e21 = uspObj.getGraph().addEdge(n9, n12);
		ExtDWE e22 = uspObj.getGraph().addEdge(n9, n13);
		ExtDWE e23 = uspObj.getGraph().addEdge(n9, n14);
		ExtDWE e24 = uspObj.getGraph().addEdge(n10, n13);
		ExtDWE e25 = uspObj.getGraph().addEdge(n10, n14);
		ExtDWE e26 = uspObj.getGraph().addEdge(n11, n14);
		ExtDWE e27 = uspObj.getGraph().addEdge(n11, n15);
		ExtDWE e28 = uspObj.getGraph().addEdge(n12, n16);
		ExtDWE e29 = uspObj.getGraph().addEdge(n12, n17);
		ExtDWE e30 = uspObj.getGraph().addEdge(n13, n16);
		ExtDWE e31 = uspObj.getGraph().addEdge(n13, n17);
		ExtDWE e32 = uspObj.getGraph().addEdge(n14, n16);
		ExtDWE e33 = uspObj.getGraph().addEdge(n14, n17);
		ExtDWE e34 = uspObj.getGraph().addEdge(n14, n18);
		ExtDWE e35 = uspObj.getGraph().addEdge(n15, n17);
		ExtDWE e36 = uspObj.getGraph().addEdge(n15, n18);
		ExtDWE e37 = uspObj.getGraph().addEdge(n16, n19);
		ExtDWE e38 = uspObj.getGraph().addEdge(n16, n20);
		ExtDWE e39 = uspObj.getGraph().addEdge(n17, n19);
		ExtDWE e40 = uspObj.getGraph().addEdge(n17, n20);
		ExtDWE e41 = uspObj.getGraph().addEdge(n18, n19);
		ExtDWE e42 = uspObj.getGraph().addEdge(n18, n20);
		ExtDWE e43 = uspObj.getGraph().addEdge(n1, n2);
		ExtDWE e44 = uspObj.getGraph().addEdge(n3, n4);
		ExtDWE e45 = uspObj.getGraph().addEdge(n6, n7);
		ExtDWE e46 = uspObj.getGraph().addEdge(n7, n8);
		ExtDWE e47 = uspObj.getGraph().addEdge(n9, n10);
		ExtDWE e48 = uspObj.getGraph().addEdge(n10, n11);
		ExtDWE e49 = uspObj.getGraph().addEdge(n12, n13);
		ExtDWE e50 = uspObj.getGraph().addEdge(n14, n15);
		ExtDWE e51 = uspObj.getGraph().addEdge(n16, n17);
		ExtDWE e52 = uspObj.getGraph().addEdge(n17, n18);
		
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
		edgeList.add(e19);
		edgeList.add(e20);
		edgeList.add(e21);
		edgeList.add(e22);
		edgeList.add(e23);
		edgeList.add(e24);
		edgeList.add(e25);
		edgeList.add(e26);
		edgeList.add(e27);
		edgeList.add(e28);
		edgeList.add(e29);
		edgeList.add(e30);
		edgeList.add(e31);
		edgeList.add(e32);
		edgeList.add(e33);
		edgeList.add(e34);
		edgeList.add(e35);
		edgeList.add(e36);
		edgeList.add(e37);
		edgeList.add(e38);
		edgeList.add(e39);
		edgeList.add(e40);
		edgeList.add(e41);
		edgeList.add(e42);
		edgeList.add(e43);
		edgeList.add(e44);
		edgeList.add(e45);
		edgeList.add(e46);
		edgeList.add(e47);
		edgeList.add(e48);
		edgeList.add(e49);
		edgeList.add(e50);
		edgeList.add(e51);
		edgeList.add(e52);
		
		return edgeList;
		
		/*
		 * //Graph 2 Node n0 = new Node(NODE_TYPE.TARGET, 10); Node n1 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new Node(NODE_TYPE.SOURCE,
		 * 0); Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0); Node n5 = new
		 * Node(NODE_TYPE.SOURCE, 0); uspObj.getGraph().addVertex(n0);
		 * uspObj.getGraph().addVertex(n1); uspObj.getGraph().addVertex(n2);
		 * uspObj.getGraph().addVertex(n3); uspObj.getGraph().addVertex(n4);
		 * uspObj.getGraph().addVertex(n5); // Generate edges List<ExtDWE>
		 * edgeList = new ArrayList<ExtDWE>(); ExtDWE e1 =
		 * uspObj.getGraph().addEdge(n3, n2); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n2, n1); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n1, n0); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n5, n4); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n4, n0); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); edgeList.add(e4);
		 * edgeList.add(e5); return edgeList;
		 */

		/*
		 * //Graph 1 Node n0 = new Node(NODE_TYPE.TARGET, 10); Node n1 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n2 = new Node(NODE_TYPE.SOURCE,
		 * 0); Node n3 = new Node(NODE_TYPE.SOURCE, 0);
		 * uspObj.getGraph().addVertex(n0); uspObj.getGraph().addVertex(n1);
		 * uspObj.getGraph().addVertex(n2); uspObj.getGraph().addVertex(n3); //
		 * Generate edges List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		 * ExtDWE e1 = uspObj.getGraph().addEdge(n0, n3); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n2, n1); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n1, n0); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); return edgeList;
		 */

		/*
		 * Node n1 = new Node(NODE_TYPE.SOURCE, 0); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n4 = new Node(NODE_TYPE.TARGET,
		 * 10); Node n5 = new Node(NODE_TYPE.TARGET, 100);
		 * uspObj.getGraph().addVertex(n1); uspObj.getGraph().addVertex(n2);
		 * uspObj.getGraph().addVertex(n3); uspObj.getGraph().addVertex(n4);
		 * uspObj.getGraph().addVertex(n5); // Generate edges List<ExtDWE>
		 * edgeList = new ArrayList<ExtDWE>(); ExtDWE e1 =
		 * uspObj.getGraph().addEdge(n1, n2); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n1, n3); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n2, n3); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n3, n2); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n2, n4); ExtDWE e6 =
		 * uspObj.getGraph().addEdge(n3, n5); // ExtDWE e7 =
		 * uspObj.getGraph().addEdge(n2, n5); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); edgeList.add(e4);
		 * edgeList.add(e5); edgeList.add(e6); // edgeList.add(e7); return
		 * edgeList;
		 */
	}

	private static List<ExtDWE> genTestGraph(TeamBuildingProblem uspObj) {

		// Graph 3
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.TARGET, 5);

		uspObj.getGraph().addVertex(n0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1);
		ExtDWE e2 = uspObj.getGraph().addEdge(n0, n2);
		ExtDWE e3 = uspObj.getGraph().addEdge(n1, n3);
		ExtDWE e4 = uspObj.getGraph().addEdge(n2, n3);
		ExtDWE e5 = uspObj.getGraph().addEdge(n3, n4);

		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);

		return edgeList;

	}

	/**
	 * @param args
	 * @throws LPSolverException
	 * @throws MalformedGraphException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws MalformedGraphException,
			LPSolverException, FileNotFoundException {
		try {
			lpWrapper.Configuration.loadLibrariesCplex();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Couldn't load Cplex.\n");
			e.printStackTrace();
		}
		runRugged();

	}
}
