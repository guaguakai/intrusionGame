package search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.CompactGraph;
import model.teamBuildingModels.MinCut;
import model.teamBuildingModels.TeamBuildingProblem;


public class Search {

	public TeamBuildingProblem teamObj;
	public double expandedNodes = 0;
	
	static int [] cost = teamNode.resourcecost;

	ArrayList<int[]> savedteams = new ArrayList<int[]>();

	static LinkedList<teamNode> queue = new LinkedList<teamNode>();
	public PriorityQueue<teamNode> stack = new PriorityQueue<teamNode>();
	
	public Set<ExtDWE> minCutEdges;
	public CompactGraph cg;
	
	double budget;
	int cutoff;
	double starttime;
	public double endtime = 10000;
	
	public Search(TeamBuildingProblem Obj, double budget, int cutoff, double time){
		this.teamObj=Obj;
		this.budget = budget;
		this.cutoff=cutoff;
		this.starttime = time;
	}
	
	public Search(TeamBuildingProblem Obj, double budget, int cutoff){
		this(Obj, budget, cutoff, 0.0);
	}
	public Search(TeamBuildingProblem Obj, double budget){
		this(Obj, budget, 0, 0.0);
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
	public List<AdversaryPath> solveAdversaryPaths(){
		
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
	
	private teamNode max(List<teamNode> lstT){
		teamNode n = null;
		
		double val=0;
		for(teamNode t : lstT){
			if(t.benefit>val){
				n=t;
				val=t.benefit;
			}
		}
		return n;
	}
	public teamNode submodularSearch(teamNode start) throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		double tock;
		
		teamNode best=start;
		best.setEvaluation(Double.NEGATIVE_INFINITY);
		
		List<teamNode> resources = new ArrayList<teamNode>();
		List<teamNode> resources2 = new ArrayList<teamNode>();
		List<teamNode> resources3 = new ArrayList<teamNode>();
		
		double[] benefit = new double[best.teamObj.getNumResourceTypes()];
		int[] ordering = new int[best.teamObj.getNumResourceTypes()];
		
		//All sets of size 1
		for(int i=0;i<best.teamObj.getNumResourceTypes();i++){
			teamNode child = new teamNode("", start, i);
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
	public teamNode cutoffSearch(teamNode start) throws MalformedGraphException, LPSolverException{
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
	
	public int fullDefIter = 0;
	List<AdversaryPath> lstAP=null;
	public teamNode PQSearch(teamNode start) throws MalformedGraphException, LPSolverException{
		
		double tick = System.currentTimeMillis();
		double tock;

		cg = new CompactGraph();
		if(lstAP!=null){
			cg.addPaths(lstAP);
		}
		cg.buildCompactGraph(teamObj);
		start.cgraph=cg;
		teamNode best=start;
		best.setEvaluation(Double.NEGATIVE_INFINITY);
		
		getTeams(best);		
		best = stack.poll();
		
		
//		best.setFullBetterRuggedEvaluation();
//		stack.add(best);
//
//		
//		best = stack.poll();
//		while(!best.optimalValue){
//			System.out.println(best.getName());
//			best.setFullBetterRuggedEvaluation();
//			stack.add(best);
//			best = stack.poll();
//			System.out.println(best);
//		}
//		
		
		if(best.visited){// || (best.getEvaluation()<0   // && !best.teamObj.isProbability)||t==2){
			best.setFullBetterRuggedEvaluation();
			fullDefIter++;
			System.out.println("full" + best);
		}else{
			best.setCompactEvaluation(2);
			best.visited=true;
			System.out.println(best);
		}
		System.out.println(best.toString());
		stack.add(best);
		
		best=stack.poll();
		while(!best.optimalValue){
			tock = (System.currentTimeMillis()-tick)/1000;
			if(tock>endtime){
				best.setEvaluation(99999);
				return best;
			}
			if(best.visited){// || (best.getEvaluation()<0)){// && !best.teamObj.isProbability)||t==2){
				best.setFullBetterRuggedEvaluation();
				fullDefIter++;
				System.out.println("full"+best);
			}else{
				best.setCompactEvaluation(2);
				best.visited=true;
				System.out.println(best);
			}
			stack.add(best);
			best = stack.poll();
		}
		return best;
	}
	

	public int t;
	public double teamTime;
	public void getTeams(teamNode start) throws MalformedGraphException, LPSolverException{
		
		double tick = System.currentTimeMillis();
		queue.add(start);
		
		while(!queue.isEmpty()){
			teamNode n = queue.pop();
			
			if(n.leaf){
					n.setCompactEvaluation(t);
					//n.setFullBetterRuggedEvaluation();
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
		double tock = System.currentTimeMillis();
		teamTime = (tock-tick)/1000;
		
		System.out.println("DoneTeams:" + teamTime);

	}
	
	
	
}