package teamBuildingSolvers.DefenderOracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.teamBuildingModels.TeamBuildingProblem;
import search.teamNode;

public class SubmodularDefender {
	
	TeamBuildingProblem teamObj;
	
	HashMap<List<int[]>,Double> setVals = new HashMap<List<int[]>, Double>();
	List<int[]> resources = new ArrayList<int[]>();
	List<int[]> resources2 = new ArrayList<int[]>();
	List<int[]> resources3 = new ArrayList<int[]>();
	
	public int[] copy(int[] b){
		int[] a = new int[b.length]; 
		for(int i=0;i<a.length;i++){
			a[i]=b[i];
		}
		return a;
	}
	public void buildSets(){
		double tick = System.currentTimeMillis();
		double tock;

		int[] start = new int[teamObj.getNumResourceTypes()];
		for(int i=0;i<teamObj.getNumResourceTypes();i++){
			start[i]=0;
		}
		
		//All sets of size 1
		for(int i=0;i<teamObj.getNumResourceTypes();i++){
			int[] child = copy(start);
			child[i] += 1;
			child.setFullBetterRuggedEvaluation();
			resources.add(child);
		}
		
		//All sets of size 2
		for(int r=0;r<resources.size();r++){
			for(int i=r;i<best.teamObj.getNumResourceTypes();i++){
				teamNode child = new teamNode("", resources.get(r), i);
				child.setFullBetterRuggedEvaluation();
				resources2.add(child);
			}
		}
		//All sets of size 3
		for(int r=0;r<resources2.size();r++){
			for(int i=r%(resources.size());i<best.teamObj.getNumResourceTypes();i++){
				teamNode child = new teamNode("", resources2.get(r), i);
				if(child.cost<budget){
					child.setFullBetterRuggedEvaluation();
					resources3.add(child);
				}
			}
		}
		List<teamNode> curSet = new ArrayList<teamNode>();
		curSet.addAll(resources);
		curSet.addAll(resources2);
		curSet.addAll(resources3);
		
		int[] team = new int[best.teamObj.getNumResourceTypes()];
		
		double spent=0;
		while(!curSet.isEmpty()){
			System.out.println(curSet.size());
			teamNode b = max(curSet);
			if((spent+b.cost)<budget){
				spent+=b.cost;
				for(int i=0;i<team.length;i++){
					team[i]+=b.resources[i];
				}
			}else{
				curSet.remove(b);
			}
			
		}
		best.setResources(team);
		//best.setFullBetterRuggedEvaluation();
		return best;
	}
}
