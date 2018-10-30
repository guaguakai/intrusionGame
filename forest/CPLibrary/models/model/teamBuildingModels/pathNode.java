package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.List;

import search.teamNode;
import model.graphutils.ExtDWE;
import model.graphutils.INode;

public class pathNode implements Comparable<pathNode>{
	public ExtDWE edge;
	public int distance;
	
	List<ExtDWE> path = new ArrayList<ExtDWE>();
	
	public pathNode(ExtDWE e, pathNode parent){
		this.edge = e;
		if(parent!=null){
			this.path.addAll(parent.path);
		}
		this.path.add(e);
	}
	public pathNode(ExtDWE e){
		this(e, null);
	}
	
	@Override
	public int compareTo(pathNode o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;

	    //this optimization is usually worthwhile, and can
	    //always be added
	    if (this == o) return EQUAL;

	    //primitive numbers follow this form
	    if (this.path.size() > o.path.size() ) return BEFORE;
	    if (this.path.size() < o.path.size() ) return AFTER;

	    return EQUAL;
	}

}
