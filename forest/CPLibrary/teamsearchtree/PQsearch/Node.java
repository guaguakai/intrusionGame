package PQsearch;
import java.util.ArrayList;

public class Node implements Comparable<Node>{

	private String name;
	private ArrayList<Node> childNodes;
	private Node parent;
	private int [] resources = new int[4];
	private int cost;
	private int depth;
	private double evaluation;
	
	public Node(String name, Node parent, int resourceindex){
		this.name = name;
		if(parent == null){
			for(int i=0; i<4; i++)
				resources[i] = 0; 
			//this.value = keyValue;
		}else{
			//this.value = parent.getValue()+keyValue;
			for(int i=0; i<4; i++)
				resources[i] = parent.getResources()[i]; 
			resources[resourceindex]++;
		}
		this.parent = parent;
		this.childNodes = new ArrayList<Node>();
		if(parent == null){
			this.depth = 0;
		}else{
			this.depth = parent.getDepth()+1;
		}
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
	
	public void setParent(Node parent){
		this.parent = parent;
	}
	
	public Node getParent(){
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
	
	public void addChildren(Node node){
		this.childNodes.add(node);
	}

	@Override
	public int compareTo(Node o) {
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
	
}
