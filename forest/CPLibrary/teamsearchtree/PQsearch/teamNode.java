package PQsearch;

import java.util.ArrayList;
import java.util.List;

import model.teamBuildingModels.TeamBuildingProblem;

public class teamNode implements Comparable<teamNode>{

	private String name;
	private ArrayList<teamNode> childNodes;
	private teamNode parent;
	private int [] resources = new int[TeamStats.getCoverage().size()];
	private ArrayList<Double> resourcelist;
	private int cost;
	private int depth;
	private double evaluation;
	private int cutoff;
	private TeamBuildingProblem teamObj;
	public double time;
	
	public teamNode(){
		this.name = "";
		depth = 0;
		cutoff = 0;
		evaluation = 0;
	}
	
	public teamNode(String name, List<Double> resources){
		this.name = name;
		resourcelist = (ArrayList<Double>) resources;
		parent = null;
		this.childNodes = new ArrayList<teamNode>();
		depth = 0;
		cutoff = 0;	
	}
	
	public teamNode(String name, teamNode parent, int resourceindex){
		this.name = name;
		if(parent == null){
			for(int i=0; i<resources.length; i++)
				resources[i] = 0; 
			//this.value = keyValue;
		}else{
			//this.value = parent.getValue()+keyValue;
			for(int i=0; i<resources.length; i++)
				resources[i] = parent.getResources()[i]; 
			resources[resourceindex]++;
		}
		this.parent = parent;
		this.childNodes = new ArrayList<teamNode>();
		if(parent == null){
			this.depth = 0;
		}else{
			this.depth = parent.getDepth()+1;
		}
	} 
	
	public TeamBuildingProblem getTeamObj(){
		return teamObj;
	}
	
	public void setTeamObj(TeamBuildingProblem teamObj){
		this.teamObj = teamObj;
	}
	
	public void setCutoff(int cutoffvalue){
		cutoff = cutoffvalue;
	}
	public int getCutoff(){
		return cutoff;
	}
	
	public void addResources(int index){
		resources[index]++;
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
	
	public ArrayList getChildren(){
		return childNodes;
	}
	
	public void addChildren(teamNode node){
		this.childNodes.add(node);
	}

	public ArrayList<Double> getResourceList(){
		return resourcelist;
	}
	public String toString(){
		return resourcelist.toString()+","+cost+","+evaluation;
	}
	@Override
	public int compareTo(teamNode o) {
		final int BEFORE = 1;
	    final int EQUAL = 0;
	    final int AFTER = -1;

	    //this optimization is usually worthwhile, and can
	    //always be added
	    if (this == o) return EQUAL;

	    //primitive numbers follow this form
	    if (this.evaluation < o.evaluation) return BEFORE;
	    if (this.evaluation > o.evaluation) return AFTER;

	    return EQUAL;
	}

	
}
