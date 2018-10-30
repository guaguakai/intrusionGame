

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.*;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;
import teamBuildingSolvers.Rugged;
import teamBuildingSolvers.RuggedCutoff;

public class AaronTest3 {
	
	
	public static void runRugged() throws LPSolverException,
			MalformedGraphException, FileNotFoundException {
		
		//read in input and print output
		Scanner scanner = new Scanner(new File("C:/Users/Aaron/workspace/TeamFormation/output.txt"));
		PrintWriter output; 
		
		//name of each team
		String teamName = "";
		 //skip first line
		scanner.nextLine();
		int teamresource=0;
		int j=0;
		
		//start runtime
		long timeforteam;
		
		//set coverages
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(2.0);
		coverage.add(3.0);
		//coverage.add(1.0);
		coverage.add(5.0);
		
		while(scanner.hasNext() && j <=30){
			if(j>10){
			long starttime = System.currentTimeMillis();
			
			//skip resources
			scanner.next();
			
			
			//add team resources
			List<Double> resources = new ArrayList<Double>();
			teamresource = scanner.nextInt();
			resources.add((double) teamresource);
			teamName += teamresource;
			teamresource = scanner.nextInt();
			resources.add((double) teamresource);
			teamName += teamresource;
			//teamresource = scanner.nextInt();
			//resources.add((double) teamresource);
			//teamName += teamresource;
			teamresource = scanner.nextInt();
			resources.add((double) teamresource);
			teamName += teamresource;
			//System.out.println("Team: "+teamName);
			
			
			//skip over the cost and heuristic
			scanner.nextLine();
			
			output = new PrintWriter(new File("outputTeamCutoffValues"+teamName+".txt"));
	
			for(int i=1; i<150; i++){
				long starttimeteam = System.currentTimeMillis();
				TeamBuildingProblem uspObj = new TeamBuildingProblem(resources, coverage);
				genTestGraph2(uspObj);
		
				//rugged
				/*Rugged ruggedObj = new Rugged(uspObj);
				ruggedObj.run();*/
				
				//ruggedcutoff
				RuggedCutoff ruggedObj = new RuggedCutoff(uspObj, i);
				ruggedObj.run();
		
				uspObj.getGraph().edgeSet().size();		
				timeforteam = System.currentTimeMillis()-starttimeteam;
		
				output.print(teamName+","+ruggedObj.getGameValue()+","+ruggedObj.getCutoff()+","+ruggedObj.getNumberOfIterations()+","+(long)(timeforteam/(long)1000));
				output.println();
				System.out.println("Team: "+teamName+" Payoff: "+ruggedObj.getGameValue()+" Cutoff: "+ruggedObj.getCutoff());
				if(ruggedObj.getcutoffUsed() == false){
					break;
				}
			}
			teamName = "";
			
			
			
			long totaltime = System.currentTimeMillis()-starttime;
			output.println("Total Runtime: "+(long)(totaltime/(long)1000));
			output.close();
			}
			j++;
		}		

	}

	private static List<ExtDWE> genTestGraph2(TeamBuildingProblem uspObj) {
		
		/*//Graph 3_+1
		Node n1 = new Node(NODE_TYPE.TARGET, 5);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.SOURCE, 0);
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n6 = new Node(NODE_TYPE.SOURCE, 0);
		Node n7 = new Node(NODE_TYPE.SOURCE, 0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);
		uspObj.getGraph().addVertex(n5);
		uspObj.getGraph().addVertex(n6);
		uspObj.getGraph().addVertex(n7);
		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n4, n3);
		ExtDWE e2 = uspObj.getGraph().addEdge(n3, n2);
		ExtDWE e3 = uspObj.getGraph().addEdge(n2, n1);
		ExtDWE e4 = uspObj.getGraph().addEdge(n6, n5);
		ExtDWE e5 = uspObj.getGraph().addEdge(n5, n1);
		ExtDWE e6 = uspObj.getGraph().addEdge(n7, n1);
		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);
		edgeList.add(e6);
		return edgeList;*/
		
		/*//Graph 3
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);		
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);	
		Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);	
		Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);	
		Node n8 = new Node(NODE_TYPE.TARGET, 5);
		Node n9 = new Node(NODE_TYPE.TARGET, 5);
		
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

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1);
		ExtDWE e2 = uspObj.getGraph().addEdge(n0, n2);
		ExtDWE e3 = uspObj.getGraph().addEdge(n0, n3);
		ExtDWE e4 = uspObj.getGraph().addEdge(n1, n4);
		ExtDWE e5 = uspObj.getGraph().addEdge(n2, n5);
		ExtDWE e6 = uspObj.getGraph().addEdge(n3, n6);
		ExtDWE e7 = uspObj.getGraph().addEdge(n1, n5);
		ExtDWE e8 = uspObj.getGraph().addEdge(n2, n4);
		ExtDWE e9 = uspObj.getGraph().addEdge(n2, n6);
		ExtDWE e10 = uspObj.getGraph().addEdge(n3, n5);
		ExtDWE e11 = uspObj.getGraph().addEdge(n4, n7);
		ExtDWE e12 = uspObj.getGraph().addEdge(n5, n8);
		ExtDWE e13 = uspObj.getGraph().addEdge(n6, n9);
		
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
		
		return edgeList;*/
		
		//Graph 4
		/*Node n0 = new Node(NODE_TYPE.SOURCE, 0);
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
		
		return edgeList;*/
		
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
		
		/*//Graph 2
		Node n0 = new Node(NODE_TYPE.TARGET, 10);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.SOURCE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.SOURCE, 0);
		uspObj.getGraph().addVertex(n0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);
		uspObj.getGraph().addVertex(n5);
		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n3, n2);
		ExtDWE e2 = uspObj.getGraph().addEdge(n2, n1);
		ExtDWE e3 = uspObj.getGraph().addEdge(n1, n0);
		ExtDWE e4 = uspObj.getGraph().addEdge(n5, n4);
		ExtDWE e5 = uspObj.getGraph().addEdge(n4, n0);
		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);
		return edgeList;*/
		
		/*//Graph 1
		Node n0 = new Node(NODE_TYPE.TARGET, 10);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.SOURCE, 0);
		Node n3 = new Node(NODE_TYPE.SOURCE, 0);
		uspObj.getGraph().addVertex(n0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n0, n3);
		ExtDWE e2 = uspObj.getGraph().addEdge(n2, n1);
		ExtDWE e3 = uspObj.getGraph().addEdge(n1, n0);
		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		return edgeList;*/		
		
		/*Node n1 = new Node(NODE_TYPE.SOURCE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.TARGET, 10);
		Node n5 = new Node(NODE_TYPE.TARGET, 100);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);
		uspObj.getGraph().addVertex(n5);
		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n1, n2);
		ExtDWE e2 = uspObj.getGraph().addEdge(n1, n3);
		ExtDWE e3 = uspObj.getGraph().addEdge(n2, n3);
		ExtDWE e4 = uspObj.getGraph().addEdge(n3, n2);
		ExtDWE e5 = uspObj.getGraph().addEdge(n2, n4);
		ExtDWE e6 = uspObj.getGraph().addEdge(n3, n5);
		// ExtDWE e7 = uspObj.getGraph().addEdge(n2, n5);
		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);
		edgeList.add(e6);
		// edgeList.add(e7);
		return edgeList;*/
	}
private static List<ExtDWE> genTestGraph(TeamBuildingProblem uspObj) {
			
		//Graph 3
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
			lpWrapper.Configuration
					.loadLibrariesCplex();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Couldn't load Cplex.\n");
			e.printStackTrace();
		}
		runRugged();
		
		

	}
}
