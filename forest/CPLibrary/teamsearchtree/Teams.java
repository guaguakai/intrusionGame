import java.util.*;
import java.io.*;

public class Teams {
	
	public static int calcCost(int [] resources){
		int [] cost = {6, 8, 10, 30};
		int costamount = 0;
		
		for(int i=0; i<4; i++){
			costamount += resources[i]*cost[i];
		}
		
		return costamount;
	}
	
	public static boolean checkarray(int [][] savedteams, int [] team){
		boolean newteam = true;
		
		for(int i=0; i<savedteams.length; i++){
			if(savedteams[i][0] == team[0] && savedteams[i][1] == team[1] && savedteams[i][2] == team[2] && savedteams[i][3] == team[3])
				newteam = false;
		}
		
		return newteam;
	}
	
	public static void main(String [] args) throws FileNotFoundException{
		int [] coverage = {2, 3, 1, 5};
		int [] interdiction = {0, 1, 1, 0};
		int [] cost = {6, 8, 10, 30};
		
		int [][] teams = new int[4][];
		//variable to add new teams
		int count = 0;
		
		//initialize root
		Node root = new Node("initial", null, 0);
		root.setValue(0);
		Tree teamtree = new Tree(root);
		
		Stack<Node> stack = new Stack<Node>();
		stack.push(root);
		Node temp;
		
		PrintWriter output = new PrintWriter(new File("output.txt"));
		
		while(!stack.isEmpty()){
			temp = stack.pop();
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
				if(((Node) temp.getChildren().get(i)).getValue() <= 100){
					output.print(((Node) temp.getChildren().get(i)).getName()+",");
					for(int j=0; j<4; j++)
						output.print(((Node) temp.getChildren().get(i)).getResources()[j]+",");
					output.print(((Node) temp.getChildren().get(i)).getValue());
					
					output.println();
					stack.push((Node) temp.getChildren().get(i));
				}
			}
		}
		
		
		
		
	}

}
