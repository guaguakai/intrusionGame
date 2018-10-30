package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.List;

import model.graphutils.INode;

public class Constraint {
//nodes,  (int) f.getLPObjective(), type)
	public String constraints;
	public List<Integer> rowIndices;
	public List<Double> rowValues;
	
	public List<INode> nodes;
	public int numPaths;
	public int resourceType;
	
	
	public Constraint(List<INode> nodes, int numPaths, int type){
		this.nodes=nodes;
		this.numPaths=numPaths;
		this.resourceType=type;	
	}
	public void addRowIndicies(List<Integer> indicies){
		List<Integer> rowIndices = new ArrayList<Integer>();
		for(Integer i : indicies){
			int i2 = i;
			rowIndices.add(i2);
		}
		this.rowIndices=rowIndices;
	}
	public void addRowValues(List<Double> values){
		List<Double> rowValues = new ArrayList<Double>();
		for(Double i : values){
			Double i2 = i;
			rowValues.add(i2);
		}
		this.rowValues = rowValues;
	}
	public String toString(){
		String s = constraints;
		return s;		
	}
}
