package search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.AbstractBaseGraph;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.MinCut;
import model.teamBuildingModels.TeamBuildingProblem;


public class incrementalSearch extends Search {

	
	public incrementalSearch(TeamBuildingProblem Obj, double budget, int cutoff, double time){
		super(Obj, budget);
	}
	
	public incrementalSearch(TeamBuildingProblem Obj, double budget, int cutoff){
		this(Obj, budget, cutoff, 0.0);
	}
	
	public void computeMinCut() {
		if (this.minCutEdges != null) { // && !this.minCutEdges.isEmpty()) {
			return;
		}
		if (this.minCutEdges == null) {
			this.minCutEdges = new HashSet<ExtDWE>();
		}

		List<INode> targetSet = this.teamObj.getTargetNodesSet();
		MinCut minCutObj = new MinCut(this.teamObj);

		for (INode targetNode : targetSet) {
			minCutObj.setTarget(targetNode);

			minCutObj.resetLP();

			Set<ExtDWE> minCutSet = minCutObj.getMinCut();
			this.minCutEdges.addAll(minCutSet);
		}
	}
	
	/**
	 * Calculates the cost of a certain team of resources (cost array stored in function)
	 * @param resources team of resources
	 * @return returns total cost of a team of resources
	 */
	public int calcCost(int [] resources){
		int costamount = 0;
		
		for(int i=0; i<3; i++){
			costamount += resources[i]*this.cost[i];
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
	public static boolean checkarray(ArrayList<int[]> savedteams, int [] team){
		boolean newteam = true;
		int count = savedteams.size();
		
		for(int i=0; i<count; i++){
			if(savedteams.get(i)[0] == team[0] && savedteams.get(i)[1] == team[1] && savedteams.get(i)[2] == team[2])
				newteam = false;
		}
		
		return newteam;
	}
	
	
	
	public teamNode Search(teamNode start) throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		double tock;
		
		teamNode best=start;
		best.setEvaluation(Double.NEGATIVE_INFINITY);
		
		queue.add(start);
		
		while(!queue.isEmpty()){
			teamNode n = queue.pop();
			
			//get current runtime & stop if time is greater than stopping time
			tock = (System.currentTimeMillis()-tick)/1000;
			if(tock>endtime){
				n.setEvaluation(99999);
				return n;
			}
			
			
			if(n.leaf){
					queue.clear();
					return n;
			}else{
				ExpandNode(n);
			}
		}
		
		return best;
	}
	
	
	public void ExpandNode(teamNode n) throws MalformedGraphException, LPSolverException{
		for(int i=0;i<teamNode.resourcecost.length;i++){
			
			teamNode child = new teamNode("", n, i);
			
			if(checkarray(savedteams, child.getResources()) && child.getValue()<=budget){
				child.setEvaluation(cutoff);			//evaluate node
				n.addChildren(child);					//add child to parent node
				savedteams.add(child.getResources());	//add team to saved teams
				expandedNodes++;						
				
				System.out.println(child.getDepth()   +", " + child.toString() + ", rt: "+child.evalTime );
			}
		}
		
		ArrayList<teamNode> children = n.getChildren();

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
							if(child.isLeaf()){
								queue.add(j, child);
								break;
							}
							if(child.getValue()>queue.get(j).getValue()){
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
		}
	}			
	
	
}
