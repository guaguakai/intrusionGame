import java.util.*;
import java.io.*;

public class TeamEval {
	
	/**
	 * Calculates the cost of a certain team of resources (cost array stored in function)
	 * @param resources team of resources
	 * @return returns total cost of a team of resources
	 */
	public static int calcCost(int [] resources){
		int [] cost = {6, 8, 10, 30};
		int costamount = 0;
		
		for(int i=0; i<4; i++){
			costamount += resources[i]*cost[i];
		}
		
		return costamount;
	}
	
	
	/**
	 * Function to determine if a certain team has already been created before
	 * @param savedteams array having currently saved teams
	 * @param team team to check if it is new
	 * @param count how many teams haved been saved so far
	 * @return boolean value to determine if team has already been created
	 */
	public static boolean checkarray(int [][] savedteams, int [] team, int count){
		boolean newteam = true;
		
		for(int i=0; i<count; i++){
			if(savedteams[i][0] == team[0] && savedteams[i][1] == team[1] && savedteams[i][2] == team[2] && savedteams[i][3] == team[3])
				newteam = false;
		}
		
		return newteam;
	}
	
	public static double calcEval(int [] resources){
		//arrays for defender resource properties
		int [] coverage = {2, 3, 1, 5};
		int [] interdiction = {0, 1, 1, 0};
		int [] cost = {6, 8, 10, 30};
		
		double eval=0;
		
		//calculate team effectiveness heuristic
		for(int i=0; i<4; i++){
			eval += resources[i]*((double)(coverage[i]+interdiction[i])/cost[i]);
		}
		
		
		return eval;
	}
	
	public static void main(String [] args) throws FileNotFoundException{
		int [] coverage = {2, 3, 1, 5};
		int [] interdiction = {0, 1, 1, 0};
		int [] cost = {6, 8, 10, 30};
		
		int [][] teams = new int[1000][];
		//variable to add new teams
		int count = 1;
		
		//initialize root
		Node root = new Node("initial", null, 0);
		root.setValue(0);
		Tree teamtree = new Tree(root);
		teams[0] = root.getResources();
		
		PriorityQueue<Node> pq = new PriorityQueue<Node>();
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		pq.add(root);
		Node temp;
		Node child;
		
		PrintWriter output = new PrintWriter(new File("input.txt"));
		
		while(!pq.isEmpty()){
			temp = pq.poll();
			for(int i=0; i<4; i++){
				teamtree.addNode(Integer.toString(i), temp, i);
			}
			
			/*for(int i=0; i<temp.getChildren().size(); i++){
				((Node) temp.getChildren().get(i)).setValue(calcCost(((Node) temp.getChildren().get(i)).getResources()));
				System.out.print("Resources:");
				for(int j=0; j<4; j++)
					System.out.print(((Node) temp.getChildren().get(i)).getResources()[j]+",");
				System.out.print(((Node) temp.getChildren().get(i)).getValue());
				System.out.println();
			}*/
			
			for(int i=0; i<temp.getChildren().size(); i++){
				//calculate cost of the resources at each node
				((Node) temp.getChildren().get(i)).setValue(calcCost(((Node) temp.getChildren().get(i)).getResources()));
				if(((Node) temp.getChildren().get(i)).getValue() <= 100 && checkarray(teams, ((Node) temp.getChildren().get(i)).getResources(), count)){
					teams[count] = ((Node) temp.getChildren().get(i)).getResources();
					count++;
					((Node) temp.getChildren().get(i)).setEvaluation(calcEval(((Node) temp.getChildren().get(i)).getResources()));
					child = (Node) temp.getChildren().get(i);
					pq.add(child);
				}
			}
		}
		
		for(int i=0; i<count; i++){
			output.print("Resources: ");
			for(int j=0; j<4; j++)
				output.print(teams[i][j]+" ");
			output.print(calcCost(teams[i])+" ");
			output.print(calcEval(teams[i]));
			output.println();
		}
		
		
		
		
	}

}
