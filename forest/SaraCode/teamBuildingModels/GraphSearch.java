package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.IEdge.EDGE_TYPE;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedSubgraph;

public class GraphSearch {
	private AbstractBaseGraph<INode, ExtDWE> graph;
	
	public GraphSearch(AbstractBaseGraph<INode, ExtDWE> graph2) {
		this.graph = graph2;
	}
	public List<ExtDWE> findPath(int length, ExtDWE e){
		pathNode root = new pathNode(e);
		List<pathNode> end = new ArrayList<pathNode>();
		PriorityQueue<pathNode> set = new PriorityQueue();
		getPath(length, 1, root, set, end);
		
		if(end.size()!=0){
			return end.get(0).path;
		}else{
			List<ExtDWE> bend = new ArrayList<ExtDWE>();
			PriorityQueue<pathNode> bset = new PriorityQueue();
			getbackPath(length-set.peek().path.size()+1, 1, root, bset, end);
			
			bend=set.poll().path;
			bend.remove(e);
			bend.addAll(bset.poll().path);
			return bend;
		}
		
	}
	public void getPath(int length,int depth, pathNode n, PriorityQueue<pathNode> set, List<pathNode> end){
		set.add(n);
		if(depth==length){
			if(!end.contains(n)){
				end.add(n);
			}
		}else{
			for(ExtDWE nxt: graph.outgoingEdgesOf(n.edge.gettarget())){
				if(nxt!=null){
					pathNode child = new pathNode(nxt,n);
					getPath(length, depth+1,child,set, end);
				}
			}
		}
	}
	
	public void getbackPath(int length,int depth, pathNode n, PriorityQueue<pathNode> set, List<pathNode> end){
		set.add(n);
		if(depth==length){
			if(!end.contains(n)){
				end.add(n);
			}
		}else{
			for(ExtDWE nxt: graph.incomingEdgesOf(n.edge.getsource())){
				if(nxt!=null && nxt.getType()!=EDGE_TYPE.VIRTUAL){
					pathNode child = new pathNode(nxt,n);
					getbackPath(length, depth+1,child, set, end);
				}
			}
		}
	}
	public INode getto(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(graph.vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: graph.incomingEdgesOf(curNode)){
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}
	public INode getfrom(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(graph.vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: graph.outgoingEdgesOf(curNode)){
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}


}
