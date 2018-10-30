package PQsearch;

import java.util.ArrayList;
import java.util.List;

public class teamNodeold implements Comparable<teamNodeold>{

	private String name;
	private ArrayList<teamNode> childNodes;
	private teamNode parent;
	private int [] resources = new int[3];
	private ArrayList<Double> resourcelist;
	private ArrayList<Double> Prob;
	private int cost;
	private int depth;
	private double evaluation;
	private int cutoff;
	
	public teamNodeold(){
		this.name = "";
		depth = 0;
		cutoff = 0;
		evaluation = 0;
	}
	
	public teamNodeold(String name, List<Double> resources, List<Double> prob2){
		this.name = name;
		resourcelist = (ArrayList<Double>) resources;
		Prob = (ArrayList<Double>) prob2;
		parent = null;
		this.childNodes = new ArrayList<teamNode>();
		depth = 0;
		cutoff = 0;	
	}
	public teamNodeold(String name, List<Double> resources){
		this.name = name;
		resourcelist = (ArrayList<Double>) resources;
		parent = null;
		this.childNodes = new ArrayList<teamNode>();
		depth = 0;
		cutoff = 0;	
	}
	public teamNodeold(String name, teamNode parent, int resourceindex){
		this.name = name;
		if(parent == null){
			for(int i=0; i<3; i++)
				resources[i] = 0; 
			//this.value = keyValue;
		}else{
			//this.value = parent.getValue()+keyValue;
			for(int i=0; i<3; i++)
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
	public double diversity(){
		ArrayList<Double> team = new ArrayList<Double>();
		team.addAll(resourcelist);
		
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

	
	@Override
	public int compareTo(teamNodeold o) {
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
