package PQsearch;

import java.util.*;
import java.io.*;

public class TeamsPermuteCheck {
	
	/**
	 * Calculates the cost of a certain team of resources (cost array stored in function)
	 * @param resources team of resources
	 * @return returns total cost of a team of resources
	 */
	public static int calcCost(int [] resources){
		
		int [] cost = TeamStats.cost;
		int costamount = 0;
		
		for(int i=0; i<3; i++){
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
			if(savedteams[i][0] == team[0] && savedteams[i][1] == team[1] && savedteams[i][2] == team[2])
				newteam = false;
		}
		
		return newteam;
	}
	
	public static String create(double budget) throws FileNotFoundException{
		
		int [][] teams = new int[1000][];
		//variable to add new teams
		int count = 1;
		
		//initialize root
		Node root = new Node("initial", null, 0);
		root.setValue(0);
		Tree teamtree = new Tree(root);
		teams[0] = root.getResources();
		
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		Node temp;
		
		//PrintWriter output = new PrintWriter(new File("output.txt"));
		String output = "";
		
		while(!stack.isEmpty()){
			temp = stack.pop();
			for(int i=0; i<3; i++){
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
				if(((Node) temp.getChildren().get(i)).getValue() <= budget && checkarray(teams, ((Node) temp.getChildren().get(i)).getResources(), count)){
					teams[count] = ((Node) temp.getChildren().get(i)).getResources();
					count++;
					stack.push((Node) temp.getChildren().get(i));
				}
			}
		}
		
		for(int i=0; i<count; i++){
			output=output+"Resources: ";
			for(int j=0; j<3; j++)
				
				
				output=output+teams[i][j]+" ";
			output=output+calcCost(teams[i]);
			output=output+"\n";
		}
		//output.close();
		return output;
		
	}
public static List<ArrayList<Double>> createList(double budget){
		
		List<ArrayList<Double>> output = new ArrayList<ArrayList<Double>>();
		
		int [][] teams = new int[1000][];
		//variable to add new teams
		int count = 1;
		
		//initialize root
		Node root = new Node("initial", null, 0);
		root.setValue(0);
		Tree teamtree = new Tree(root);
		teams[0] = root.getResources();
		
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		Node temp;
		
		while(!stack.isEmpty()){
			temp = stack.pop();
			for(int i=0; i<3; i++){
				teamtree.addNode(Integer.toString(i), temp, i);
			}
			for(int i=0; i<temp.getChildren().size(); i++){
				//calculate cost of the resources at each node
				((Node) temp.getChildren().get(i)).setValue(calcCost(((Node) temp.getChildren().get(i)).getResources()));
				if(((Node) temp.getChildren().get(i)).getValue() <= budget && checkarray(teams, ((Node) temp.getChildren().get(i)).getResources(), count)){
					teams[count] = ((Node) temp.getChildren().get(i)).getResources();
					count++;
					stack.push((Node) temp.getChildren().get(i));
				}
			}
		}
		
		for(int i=0; i<count; i++){
			ArrayList<Double> team = new ArrayList();
			for(int j=0; j<3; j++){
				team.add((double) teams[i][j]);
			}
			output.add(team);
		}
		
		return output;
		
	}

}
