package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.List;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;

public class PathSet {
	
	public static List<PathSet> allPaths;
	
	public int numPaths;
	public int distance;
	public List<INode> nodeset;
	public List<CompactPath> pathset;
	
	
	
	public PathSet(int numPaths){
		this.numPaths = numPaths;
		nodeset = new ArrayList<INode>();
		pathset = new ArrayList<CompactPath>();
		
	}

}
