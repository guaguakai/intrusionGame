package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.graphutils.INode;

public class PathConstraint {

	public HashMap<String, HashMap<List<NodePair>,Double>> constraints;
	public HashMap<String, HashMap<List<INode>,Double>> constraint;
	
	public PathConstraint(){
		constraints = new HashMap<String, HashMap<List<NodePair>,Double>>();
		constraint = new HashMap<String, HashMap<List<INode>,Double>>();
	}
	
	public HashMap<List<NodePair>, Double> get(String resource){
		return constraints.get(resource);
	}
	
	public void add(String resource, List<NodePair> paths, double d){
		if(constraints.containsKey(resource)){
			constraints.get(resource).put(paths, d);
		}else{
			HashMap<List<NodePair>, Double> c = new HashMap<List<NodePair>,Double>();
			c.put(paths, d);
			constraints.put(resource, c);
		}
	}
	public void add(String resource, List<INode> nodes, double d){
		if(constraint.containsKey(resource)){
			constraint.get(resource).put(nodes, d);
		}else{
			HashMap<List<INode>, Double> c = new HashMap<List<INode>,Double>();
			c.put(nodes, d);
			constraint.put(resource, c);
		}
	}
	public Set<String> getKeys(){
		return constraints.keySet();
	}
}
