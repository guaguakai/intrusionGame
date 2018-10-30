package BF;
import java.util.ArrayList;
import java.util.List;

import teamBuildingSolvers.Rugged;
import teamBuildingSolvers.RuggedBetterResponses;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;
import teamBuildingSolvers.RuggedCutoff;
import lpWrapper.LPSolverException;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.TeamBuildingProblem;

public class teamNode implements Comparable<teamNode>{

	private String name;
	
	private ArrayList<teamNode> childNodes;
	private teamNode parent;
	
	private int [] resources = new int[4];
	int [] resourcecost = {6, 8, 30, 3};
	
	public TeamBuildingProblem teamObj;
	
	/**Lists used for input to the rugged algorithm**/
	List<Double> coverage = new ArrayList<Double>();
	public List<Double> resourcesList = new ArrayList<Double>();
	
	/**Is set to true if the node is a leaf node**/
	public boolean leaf=false;
	
	private double budget;
	private int cost;
	private int depth;
	private double evaluation;
	

	public teamNode(String name, teamNode parent, int resourceindex, double budget){
		this.name = name;
		this.parent = parent;
		this.budget=budget;
		
		if(parent == null){ 		//if this is the root node
			cost=0;
			for(int i=0; i<3; i++){
				resources[i] = 0;} 	//no resources
		}else{
			for(int i=0; i<3; i++){
				resources[i] = parent.getResources()[i]; 
			}
			resources[resourceindex]++;
			cost = parent.cost+resourcecost[resourceindex];
		}
		
		this.childNodes = new ArrayList<teamNode>();
		
		
		coverage.add(2.0);
		coverage.add(3.0);
		coverage.add(5.0);

		
		resourcesList.add((double) resources[0]);
		resourcesList.add((double) resources[1]);
		resourcesList.add((double) resources[2]);
		
		teamObj = new TeamBuildingProblem(resourcesList, coverage);
		
		if(parent == null){
			this.depth = 0;
		}else{
			depth = parent.getDepth()+1;
			teamObj.setGraph(parent.teamObj.getGraph());
		}
		
		leaf=isLeaf();
		
		
	} 
	
	public teamNode(String name, teamNode parent, int resourceindex){
		this(name, parent, resourceindex, parent.budget);
	}
	

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
	public void setFullBetterRuggedEvaluation() throws MalformedGraphException, LPSolverException{
		RuggedBetterResponses ruggedObj = new RuggedBetterResponses(teamObj);
		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
	}
	/**This method gets called if the node is a leaf node**/
	public void setFullRuggedEvaluation() throws MalformedGraphException, LPSolverException{
		Rugged ruggedObj = new Rugged(teamObj);
		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
	}
	
	public void setBetterCutoffRuggedEvaluation(int cutoff) throws MalformedGraphException, LPSolverException{
		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(teamObj, cutoff);
		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
	}
	
	public void setCutoffRuggedEvaluation(int cutoff) throws MalformedGraphException, LPSolverException{
		RuggedCutoff ruggedObj = new RuggedCutoff(teamObj, cutoff);
		ruggedObj.run();
		
		evaluation = ruggedObj.getGameValue();
	}
	
	/**this method gets the upper bound evaluation**/
	public void setRuggedEvaluation(int cutoff) throws MalformedGraphException, LPSolverException{
		if(leaf){
			setFullBetterRuggedEvaluation();
		}else{
			setBetterCutoffRuggedEvaluation(cutoff);
		}
	}
	
	public void setEvaluation(double eval){
		evaluation=eval;
	}
	
	public double getEvaluation(){
		return evaluation;
	}
	
	public int [] getResources(){
		return resources;
	}
	
	public void setValue(int value){
		cost = value;
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
	    if (this == o) return EQUAL;

	    //primitive numbers follow this form
	    if (this.evaluation < o.evaluation) return BEFORE;
	    if (this.evaluation > o.evaluation) return AFTER;

	    return EQUAL;
	}
	public String toString(){
		return resourcesList.toString()+","+cost;
	}
}
