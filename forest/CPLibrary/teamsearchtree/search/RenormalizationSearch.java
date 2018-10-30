package search;

import java.util.ArrayList;

public class RenormalizationSearch {
	ArrayList<int[]> savedteams = new ArrayList<int[]>();
	ArrayList<teamNode> runnableteams = new ArrayList<teamNode>();
	
	/**
	 * Function to determine if a certain team has already been created before
	 * @param savedteams array having currently saved teams
	 * @param team team to check if it is new
	 * @param count how many teams have been saved so far
	 * @return boolean value to determine if team has already been created
	 */
	public static boolean checkarray(ArrayList<int[]> savedteams, int [] team){
		boolean newteam = true;
		int count = savedteams.size();
		
		for(int i=0; i<count; i++){
			if(savedteams.get(i)[0] == team[0] && savedteams.get(i)[1] == team[1] && savedteams.get(i)[2] == team[2])
				newteam = false;
		}
		
		return newteam;
	}
	
	public void createTeams(teamNode root){
		
		teamNode n = root;
		if(!n.leaf){
			
			for(int i=0;i<teamNode.resourcecost.length;i++){
				
				teamNode child = new teamNode("", n, i);
				
				if(checkarray(savedteams, child.getResources()) && child.getValue()<=teamNode.budget){
					//child.setEvaluation(cutoff);			//evaluate node
					n.addChildren(child);					//add child to parent node
					savedteams.add(child.getResources());	//add team to saved teams
					
				}
				if(child.leaf){
					child.setEvaluation(0);
					runnableteams.add(child);
					
				}
			}
		}
	}

	public void 
}

