package model.teamBuildingModels;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;

import search.teamNode;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.INode;


public class CompactPath {
	
	public AbstractBaseGraph graph;
	public AdversaryPath ap;
	
	public List<ExtDWE> edges;		//unique path edges
	public List<ExtDWE> graphEdges;	//edges in corresponding graph
	
	public List<INode> nodes;
	
	public INode target;		//unique path target
	public INode graphtarget;	//target in corresponding graph
	
	public List<NodePair> intersections;
	
	public CompactPath(List<ExtDWE> path, INode end, AbstractBaseGraph<INode, ExtDWE> graph){
		this.ap=new AdversaryPath(end, path);
		this.graph = graph;
		this.intersections = new ArrayList<NodePair>();
		
		this.edges = path;
		this.graphEdges = new ArrayList<ExtDWE>();
		
		this.target = end;
		this.graphtarget = end;
		
		this.nodes = getVertices();
		
		getGraphPath(path);
		removeVirtual();
		
	}
		
	public CompactPath(GraphPath path){
		this.edges = getEdges(path);
		this.nodes = getVertices();
		this.target = (INode) path.getEndVertex();
		this.graphtarget = (INode) path.getEndVertex();
		
	}
	
	public CompactPath(AdversaryPath path, AbstractBaseGraph<INode, ExtDWE> graph ){
		
		this.ap=path;
		this.graph = graph;
		this.intersections = new ArrayList<NodePair>();

		this.graphEdges = new ArrayList<ExtDWE>();

		this.target = (INode) path.getTarget();
		this.graphtarget = (INode) path.getTarget();
		this.edges = getEdges(path);
		this.nodes = getVertices();
		
		getGraphPath(path);
		removeVirtual();
		
		
	}
	public CompactPath(model.urbansecModels.AdversaryPath path, AbstractBaseGraph<INode, ExtDWE> graph ){
		AdversaryPath ap = new AdversaryPath(path.getTarget(), path.getPath());
		this.ap=ap;
		this.graph = graph;
		this.intersections = new ArrayList<NodePair>();

		this.graphEdges = new ArrayList<ExtDWE>();

		this.target = (INode) path.getTarget();
		this.graphtarget = (INode) path.getTarget();
		this.edges = getEdges(ap);
		this.nodes = getVertices();
		
		getGraphPath(path);
		removeVirtual();
		
		
	}
	
	private void getGraphPath(Iterable<ExtDWE> path){
		for(ExtDWE edge : path){
			INode source = edge.getsource();
			INode target = edge.gettarget();
			
			graphEdges.add((ExtDWE) graph.getEdge(source, target));
		}
	}
	
	private void removeVirtual(){
		for(ExtDWE e : edges){
			if(e.getType()==EDGE_TYPE.VIRTUAL){
				edges.remove(e);
				break;
			}
		}
		for(INode n : nodes){
			if(n.getType()==NODE_TYPE.SOURCE){
				nodes.remove(n);
				break;
			}
		}
	}
	private List<ExtDWE> getEdges(AdversaryPath path){
		List<ExtDWE> PathEdges = new ArrayList<ExtDWE>();
		
		for(ExtDWE e : path.getPath()){
			if(e.getType()!=EDGE_TYPE.VIRTUAL){
				PathEdges.add(e);
			}
		}
		return PathEdges;
	}
	private List<ExtDWE> getEdges(GraphPath path){
		List<ExtDWE> PathEdges = new ArrayList<ExtDWE>();
		
		for(ExtDWE e : (List<ExtDWE>) path.getEdgeList()){
			if(e.getType()!=EDGE_TYPE.VIRTUAL){
				PathEdges.add(e);
			}
		}
		return PathEdges;
	}
	
	private List<INode> getVertices(){
		List<INode> PathNodes = new ArrayList<INode>();
		
		for(ExtDWE e : this.edges){
			if(!PathNodes.contains(e.getsource())){
				PathNodes.add(e.getsource());
			}
		}
		PathNodes.add(graphtarget);
		return PathNodes;
	}
	
	public List<ExtDWE> outgoingEdgesOf(INode n){
		if(!nodes.contains(n)){
			return null;
		}
		ArrayList<ExtDWE> edges = new ArrayList<ExtDWE>();
		
		for(ExtDWE e : (Set<ExtDWE>) this.graph.outgoingEdgesOf(n)){
			if(graphEdges.contains(e)){
				edges.add(e);
			}
		}
		
		return edges;
	}
	public int distance(INode n1, INode n2){
		if(n1.equals(n2)) { return 0; }
		
		return Math.abs(nodes.indexOf(n1)-nodes.indexOf(n2));
		
		
	}
	public boolean equals(CompactPath p){
		return p.graphEdges.equals(this.graphEdges);
			
	}
	
	public AdversaryPath toAP(){
		return ap;
	}
	public String toString(){
		String result = "";
		
		result = ""+edges+","+target.getId();
		
		return result;
	}
}
