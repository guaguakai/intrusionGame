package search;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.AbstractBaseGraph;

import teamBuildingSolvers.CompactProblem;
import teamBuildingSolvers.Rugged;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;
import teamBuildingSolvers.RuggedCutoff;
import teamBuildingSolvers.CompactOracle.CompactDefender;
import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.ActionProfile;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.CompactGraph;
import model.teamBuildingModels.TeamBuildingProblem;

public class teamNode implements Comparable<teamNode>{
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	
	/**Properties of the Game/Domain */
	
	public static boolean useCompact = false;
	
	public static int [] resourcecost = {5, 6, 7, 8};
	public static int [] resourcecoverage = {2, 3, 4, 5};
	public static double [] resourceprob = {0.5,0.6, 0.7, 0.8};
	public static double budget;
	
	private static double minCostEdge;
	private static double highProb= 1.0;
	public static int maxVResources = Integer.MAX_VALUE;
	
	public static boolean warmstart=false;
	public static boolean probs = false;
	
	
	
	public static boolean useMinCut = false;
	private static boolean pad = false; //set to true if using virtual resources 
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	
	/** number of resources of each type in current node
	 * 
	 */
	public int [] resources;
	
	private String name;
	private teamNode parent;
	private ArrayList<teamNode> childNodes;
	public TeamBuildingProblem teamObj;
	public RuggedBetterResponsesCutoff solution;
	public List<model.urbansecModels.AdversaryPath> warmstartPaths;
	public CompactGraph cgraph;
	
	/**Lists used for input to the rugged algorithm**/
	List<Double> coverage = new ArrayList<Double>();
	public List<Double> resourcesList = new ArrayList<Double>();
	public List<Double> resourcesProb = new ArrayList<Double>();
	
	/**Is set to true if the node is a leaf node**/
	public boolean leaf=false;
	public boolean optimalValue = false;
	
	public int cost;
	private int depth;
	private double evaluation;
	public double lb;
	public double benefit;
	
	public int resourceindex; 	//index of new resource
	public double evalTime;		//time for evaluation of node
	public teamNode(String name, teamNode parent, int resourceindex, double budget, boolean pad){
		
		this.pad = pad;
		this.name = name;
		this.budget=budget;
		this.resourceindex = resourceindex;
		
		resources = new int[resourcecost.length];
		
		if(parent == null){ 		//if this is the root node
			cost=0;
			for(int i=0; i<resourcecost.length; i++)
				resources[i] = 0; 	//no resources
		}else{
			for(int i=0; i<resourcecost.length; i++){
				resources[i] = parent.getResources()[i]; 
			}
			resources[resourceindex]++;
			cost = parent.cost+resourcecost[resourceindex];
			this.maxVResources=parent.maxVResources;
		}
		
		this.parent = parent;
		this.childNodes = new ArrayList<teamNode>();
		
		leaf=isLeaf();
		
		for(int i=0;i<resourcecoverage.length;i++){
			coverage.add((double) resourcecoverage[i]);
			resourcesList.add((double) resources[i]);
			resourcesProb.add(resourceprob[i]);
		}
		
		if(pad){
			resourcesProb.add(highProb);
		}

		if(!leaf && pad){
			coverage.add(1.0);
			resourcesList.add((double) padTeam());
		}
		
		if(probs){
			teamObj = new TeamBuildingProblem(resourcesList, coverage, resourcesProb);
		}else{
			teamObj = new TeamBuildingProblem(resourcesList, coverage);
		}
		
		if(parent == null){
			this.depth = 0;
		}else{
			depth = parent.getDepth()+1;
			teamObj.setGraph(parent.teamObj.getGraph());
			if(warmstart){
				for(AdversaryPath ap : parent.teamObj.getActProfile().getAdversaryPaths()){
					teamObj.getActProfile().addAdversaryPath(ap);
				}
			}
			if(parent.cgraph!=null){
				this.cgraph=parent.cgraph;
				}
		}
		
	} 
	
	/**Constructor to create a child team node from a parent */
	public teamNode(String name, teamNode parent, int resourceindex){
		this(name, parent, resourceindex, parent.budget, parent.pad);
	}
	public void setResources(int[] resources){
		this.resources=resources;
		for(int i=0;i<resourcecoverage.length;i++){
		resourcesList.add((double) resources[i]);}
		teamObj.setNumDefenderResources(resourcesList);
	}
	public static void setTeams(int[] costs, int[] coverage, double[] prob, boolean useP){
		resourcecost = costs;
		resourcecoverage = coverage;
		resourceprob = prob;
		
		probs = useP;
	}
	public void setMaxVirtualResources(int max){
		this.maxVResources=max;
	}
	
	/** fills the remaining budget of the team with virtual resources*/
	public int padTeam(){
		int vr = (int)( (budget-cost)/minCostEdge);
		if(useMinCut && maxVResources<vr){
			return maxVResources;
		}
		return vr;
	}
	
	/** Returns true if this is a leaf node (saturates the budget)*/
	public Boolean isLeaf(){
		for(int i=0;i<resourcecost.length;i++){
			if(resourcecost[i]+cost<=budget){
				return false;
			}
		}
		return true;
		
	}
	
	public void setBudget(double budget){
		this.budget = budget;
	}
	
	public void addResources(int index){
		resources[index]++;
	}
	
	public int getFullCoverage(){
		return teamObj.getTotalCoverage();
	}
	

	/** Gets ACTUAL value of the team using betterResponse
	 * This method gets called if the node is a leaf node
	 */
	public void setFullBetterRuggedEvaluation() throws MalformedGraphException, LPSolverException{
		
		TeamBuildingProblem team = null;
		if(this.probs){
			team = new TeamBuildingProblem(resourcesList, coverage, resourcesProb);
		}else{
			team = new TeamBuildingProblem(resourcesList, coverage);	
		}
		team.setGraph(teamObj.getGraph());
		
		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(team);
		

		List<model.teamBuildingModels.DefenderAllocation> daList = null;
		List<model.teamBuildingModels.AdversaryPath> apList = null;
		
		ruggedObj.setEnableBetterAttackerOracle(true);
		apList = ruggedObj.getWarmStartAttackerShortestPath();
						 
		
		ruggedObj.warmStart(daList, apList);
		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
		solution = ruggedObj;
		evalTime = ruggedObj.getRunTime();
		//ruggedObj.end();
		optimalValue = true;
		benefit=(0-evaluation)/cost;
	}
	
	public List<model.urbansecModels.AdversaryPath> getWarmStartPaths(){
		
		if(parent.warmstartPaths==null){
			model.urbansecModels.UrbanSecProblem model = new model.urbansecModels.UrbanSecProblem(teamObj.getTotalCoverage());
			model.setGraph(teamObj.getGraph());
			urbansecSolvers.RuggedBetterResponses quickSolve = new urbansecSolvers.RuggedBetterResponses(model);
			
			try {
				quickSolve.run();
				warmstartPaths = quickSolve.getAdversaryPaths();
				return quickSolve.getAdversaryPaths();
				
			} catch (MalformedGraphException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LPSolverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return parent.warmstartPaths;
		
	}
	
	
	public double heuristicEval = -1;
	public double Hrt=0;
	public void setheuristicRuggedEvaluation() throws MalformedGraphException, LPSolverException{
		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(teamObj);
		

		List<model.teamBuildingModels.DefenderAllocation> daList = null;
		List<model.teamBuildingModels.AdversaryPath> apList = null;
		
		ruggedObj.setEnableBetterAttackerOracle(true);
		apList = ruggedObj.getWarmStartAttackerShortestPath();
						 
		
		//ruggedObj.warmStart(daList, apList);
		ruggedObj.heuristic=false;
		ruggedObj.run();
		
		heuristicEval = ruggedObj.getGameValue();
		solution = ruggedObj;
		Hrt = ruggedObj.getRunTime();
		//ruggedObj.end();
		optimalValue = true;
	}
	/**Gets ACTUAL value of the team using Rugged
	 * This method gets called if the node is a leaf node
	 **/
	public void setFullRuggedEvaluation() throws MalformedGraphException, LPSolverException{
		Rugged ruggedObj = new Rugged(teamObj);
		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
		optimalValue = true;
	}
	
	public boolean visited=false;
	/** Gets HEURISTIC value of the team using the CompactSolver*/

	public void setCompactEvaluation(int t) throws LPSolverException{
		
		
		CompactProblem	test = new CompactProblem(teamObj);
		if(cgraph!=null){
			test.compactGraph=cgraph;
		}
		//test.inputPaths(warmstartPaths);
		
		evaluation = test.solve(t);		
		lb=test.solve(3);
	}
	
	/** Gets HEURISTIC value of the team using the BetterResponseCutoff*/
	public void setBetterCutoffRuggedEvaluation(int cutoff) throws MalformedGraphException, LPSolverException{
		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(teamObj, cutoff);

		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
		solution = ruggedObj;
		ruggedObj.end();
	}
	
	/** Gets HEURISTIC value of the team using the RuggedCutoff*/
	public void setCutoffRuggedEvaluation(int cutoff) throws MalformedGraphException, LPSolverException{
		RuggedCutoff ruggedObj = new RuggedCutoff(teamObj, cutoff);
		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
	}
	
	/**this method gets the upper bound evaluation**/
//	public void setEvaluation(int cutoff) throws MalformedGraphException{
//		try{
//			
//			double tick = System.currentTimeMillis();
//			
//			if(leaf){
//				System.out.println("Leaf");
//				setFullBetterRuggedEvaluation();
//			}else{
//				if(useCompact){
//					setCompactEvaluation();
//				}else{
//					setBetterCutoffRuggedEvaluation(cutoff);
//				}
//			}
//			double tock = System.currentTimeMillis();
//			evalTime = (tock-tick)/1000;
//			
//		}catch(Exception e){}
//	}
	
	public void setEvaluation(double eval){
		evaluation=eval;
	}
	
	public double getEvaluation(){
		return evaluation;
	}
	
	/**Calculates the entropy of the team composition */
	public double diversity(){
		ArrayList<Double> team = (ArrayList<Double>) resourcesList;
		double size=0;
		for(Double t : team){
			size += t;
		}
		double diversity = 0;
		double n = team.size();
		for(Double t : team){
			if(t>0){
			diversity += -(t/size)*Math.log((t/size))/Math.log(n);}
		}
		return diversity;
	}
	
	public int [] getResources(){
		return resources;
	}
	
	public void setParent(teamNode parent){
		this.parent = parent;
	}
	
	public teamNode getParent(){
		return parent;
	}
	
	public void setDepth(int depth){
		this.depth = depth;
	}
	
	public int getDepth(){
		return depth;
	}
	
	public void setValue(int value){
		cost = value;
	}

	public int getValue(){
		return cost;
	}
	
	public String getName(){
		return name;
	}
	
	public ArrayList<teamNode> getChildren(){
		return childNodes;
	}
	
	public void addChildren(teamNode node){
		this.childNodes.add(node);
	}
	
	@Override
	public int compareTo(teamNode o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;

	    //this optimization is usually worthwhile, and can
	    //always be added
	    //if (this == o) return BEFORE;

	    //primitive numbers follow this form
	    if(this.optimalValue){
	    	if (this.evaluation >= o.evaluation - 0.1) return BEFORE;
	 	    if (this.evaluation < o.evaluation - 0.1) return AFTER;
	    }
	    if(o.optimalValue){
	    	if (this.evaluation - 0.1 <= o.evaluation) return AFTER;
	    	if (this.evaluation -0.1 > o.evaluation) return BEFORE;
	    }
	    if (this.evaluation >= o.evaluation) return BEFORE;
	    if (this.evaluation < o.evaluation) return AFTER;

	    return EQUAL;
	}
	public String toString(){
		return resourcesList.toString()+",c: "+cost+",gv: "+evaluation+" "+lb;
	}
}
