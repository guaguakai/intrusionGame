package search;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import teamBuildingSolvers.RuggedCutoff;
import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.MalformedGraphException;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.Node;
import model.teamBuildingModels.TeamBuildingProblem;


public class BruteForce {

	TeamBuildingProblem teamObj;
	
//	int [] coverage = {2, 3,  4};
//	int [] interdiction = {0, 1,  0};
//	int [] cost = {6, 8, 10};
	
	public double endtime = 10000;
	
	int [][] teams = new int[1000][];
	
	static LinkedList<teamNode> queue = new LinkedList<teamNode>();
	//teamNode root;
	double budget;
	
	public BruteForce(TeamBuildingProblem Obj, double money){
		this.teamObj=Obj;
		//root = new teamNode("root",null,0);
		//root.teamObj=Obj;
		budget = money;
	}
	
	/**
	 * Calculates the cost of a certain team of resources (cost array stored in function)
	 * @param resources team of resources
	 * @return returns total cost of a team of resources
	 */
//	public int calcCost(int [] resources){
//		//int [] cost = {6, 8, 10, 30};
//		int costamount = 0;
//		
//		for(int i=0; i<3; i++){
//			costamount += resources[i]*this.cost[i];
//		}
//		
//		return costamount;
//	}
	
	
	/**
	 * Function to determine if a certain team has already been created before
	 * @param savedteams array having currently saved teams
	 * @param team team to check if it is new
	 * @param count how many teams haved been saved so far
	 * @return boolean value to determine if team has already been created
	 */
	public static boolean isSame(int[] a, int[] b){
		for(int i=0;i<a.length;i++){
			if(a[i] != b[i]){
				return false;
			}
		}
		return true;
	}
	public static boolean checkarray(ArrayList<int[]> savedteams, int [] team){
		boolean newteam = true;
		int count = savedteams.size();
		
		for(int i=0; i<count; i++){
			if(isSame(savedteams.get(i), team)){
				return false;
			}
			//if(savedteams.get(i)[0] == team[0] && savedteams.get(i)[1] == team[1] && savedteams.get(i)[2] == team[2] && savedteams.get(i)[3] == team[3] && savedteams.get(i)[4] == team[4] && savedteams.get(i)[5] == team[5])
			//if(savedteams.get(i).equals(team))
			//newteam = false;

			
			
		}
		
		return newteam;
	}
	ArrayList<int[]> savedteams = new ArrayList<int[]>();

	
	public teamNode Search(teamNode start) throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		double tock;
		teamNode best=start;
		best.setEvaluation(Double.NEGATIVE_INFINITY);
		
		queue.add(start);
		while(!queue.isEmpty()){
			teamNode n = queue.pop();
			//System.out.println("team"+n.resourcesList.toString()+": "+n.getEvaluation());
			if(best.getEvaluation()<n.getEvaluation()){ 
					best =n;
			}
			tock = (System.currentTimeMillis()-tick)/1000;
			
			if(tock>endtime){
				System.out.println("OutofTime");
				n.setEvaluation(99999);
				return n;
			}
			if(!n.leaf){
				ExpandNode(n);
			}
		}
		
		return best;
	}
	
	public void ExpandNode(teamNode n) throws MalformedGraphException, LPSolverException{
		for(int i=0;i<3;i++){
			teamNode child = new teamNode("", n, i,budget, false);
			if(checkarray(savedteams, child.getResources()) && child.getValue()<=budget){
				child.setFullBetterRuggedEvaluation();
				n.addChildren(child);
				System.out.println("team"+child.resourcesList.toString()+": "+child.getEvaluation()+child.solution.getTotalTime());
				savedteams.add(child.getResources());
			}
		}
		
		ArrayList<teamNode> children = n.getChildren();
		
		/*if(children.isEmpty()){
			n.setFullRuggedEvaluation();
			n.leaf=true;
			n.addChildren(n);
		}*/
		
		for (teamNode child : children){
				
				if(queue.isEmpty()){
					queue.add(child);
					
				}else{
					
					int size = queue.size();
					int j=0;
					while(!queue.contains(child)){
						if(child.getEvaluation() > queue.get(j).getEvaluation()){ 
							queue.add(j, child); 
							break;
						}else if(child.getEvaluation() == queue.get(j).getEvaluation()){
							//condition for favoring one node with same cost as another node here
							if(child.getValue()<queue.get(j).getValue()){
								queue.add(j, child);
								break;
							}
						
						}
						if(j==size-1){
							queue.add(child);
							break;
						}
						j++;
						
					}
				}
				
				
				//child.path= (LinkedList<node>) n.path.clone();
				//child.path.add(child);		
				
		}
	
	}			
	public teamNode smartSearch(teamNode start) throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		double tock;
		teamNode best=start;
		best.setEvaluation(Double.NEGATIVE_INFINITY);
		
		queue = getALLTeams(start);
		int i=1;
		while(!queue.isEmpty()){
			teamNode n = queue.pop();
			//if(i>170){
				n.setFullBetterRuggedEvaluation();//}
			i++;
			System.out.println("full" + n);
			try {
			    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("allBF.csv", true)));
			    out.println(n.toString());
			    out.close();
			} catch (IOException e) {
			    
			}
			if(best.getEvaluation()<n.getEvaluation()){ 
					best =n;
			}
			tock = (System.currentTimeMillis()-tick)/1000;
			
			if(tock>endtime){
				System.out.println("OutofTime");
				n.setEvaluation(99999);
				return n;
			}
		}
		
		return best;
	}
	
	public List<teamNode> testAttacker(teamNode start) throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		double tock;
		teamNode best=start;
		best.setEvaluation(Double.NEGATIVE_INFINITY);
		
		List<teamNode> teams = new ArrayList<teamNode>();
		
		queue = getALLTeams(start);
		
		while(!queue.isEmpty()){
			teamNode n = queue.pop();
			n.setFullBetterRuggedEvaluation();
			System.out.println(n.getEvaluation()+","+n.evalTime);
			n.setheuristicRuggedEvaluation();
			System.out.println(n.heuristicEval+","+n.Hrt);
			teams.add(n);
			
		}
		
		return teams;
	}
	
	public LinkedList<teamNode> getALLTeams(teamNode start) throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		double tock;
		
		LinkedList<teamNode> stack = new LinkedList<teamNode>();
		
		queue.add(start);
		
		while(!queue.isEmpty()){
			teamNode n = queue.pop();
			
			
			if(n.leaf){
					stack.add(n);
					System.out.println(n);
			}else{
				
				for(int i=0;i<teamNode.resourcecost.length;i++){
					teamNode child = new teamNode("", n, i);
					if(checkarray(savedteams, child.getResources()) && child.getValue()<=budget){
						savedteams.add(child.getResources());	//add team to saved teams		
						queue.add(child);
					}
				}
			}
		}
		return stack;
	}
	
private static List<ExtDWE> genTestGraph(TeamBuildingProblem uspObj) {
		
		//Graph 4
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
		
	}
private static List<ExtDWE> gensmallTestGraph(TeamBuildingProblem uspObj) {
	
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
	
	
}
