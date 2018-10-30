package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.HamiltonianCycle;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.traverse.DepthFirstIterator;

import com.sun.corba.se.impl.orbutil.graph.Graph;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;

public class NodePair {
	
	public boolean self = false;
	
	public CompactPath[] pathset = new CompactPath[2];
	public INode[] nodeset = new INode[2];
	
	public boolean shareEdge = false;
	
	public List<ExtDWE> edgeset = new ArrayList<ExtDWE>();			//edges in intersection
	public List<INode>	nodeintersection = new ArrayList<INode>(); //nodes in intersection
	public HashMap<INode,List<ExtDWE>> danglingnodes = new HashMap<INode,List<ExtDWE>>();
	public List<INode> danglers = new ArrayList<INode>();
	public List<ExtDWE> danglingedges = new ArrayList<ExtDWE>();	//edges around intersection (required to cover paths)
	
	public HashMap<NodePair,List<ExtDWE>> intersectionDistances;
	public HashMap<NodePair,Integer> intersectionSize;
	
	public int distance;
	
	public NodePair(){
		intersectionDistances = new HashMap<NodePair, List<ExtDWE>>();
		intersectionSize = new HashMap<NodePair,Integer>();
	}
	
	public void setDistance(int d){
		this.distance = d;
	}
	
	public List<ExtDWE> getEdgeSet(){
		if(edgeset.isEmpty()){
			Collections.shuffle(pathset[0].edges, model.Configuration.randomGenerator);
			edgeset.add(pathset[0].edges.get(0));
		}
		return edgeset;
	}
	public void getNodes(){
		
		if(!edgeset.isEmpty() && nodeintersection.isEmpty()){
			nodeintersection.add(edgeset.get(0).getsource());
			for(ExtDWE e : edgeset){
				nodeintersection.add(e.gettarget());
			}
		}
		
	}
	
	public boolean hasTargets(INode t1, INode t2){
		if(pathset[0].target.equals(t1) && pathset[1].target.equals(t2)){
			return true;
		}
		if(pathset[1].target.equals(t1) && pathset[0].target.equals(t2)){
			return true;
		}
		return false;
	}
	
	public INode otherTarget(INode target){
		if(pathset[0].target.equals(target)){
			return pathset[1].target;
		}
		return pathset[0].target;
	}
	public INode otherNode(INode n){
		if(nodeset[0].equals(n)){
			return nodeset[1];
		}
		return nodeset[0];
	}
	
	public int getTreeCoverage(List<NodePair> possiblePairs, List<ExtDWE> edges){
		possiblePairs.add(this);
		
		SimpleWeightedGraph graph = new SimpleWeightedGraph<INode, ExtDWE>(ExtDWE.class);
		HashMap<ExtDWE,List<ExtDWE>> edgeMap = new HashMap<ExtDWE,List<ExtDWE>>();
		HashMap<NodePair,INode> nodeMap = new HashMap<NodePair,INode>();
		int coverage = 0;
		Node zero = new Node();
		graph.addVertex(zero);
		
		for(NodePair p : possiblePairs){
			Node n = new Node();
			nodeMap.put(p,n);
			
			graph.addVertex(n);
			
			coverage += p.distance;
			edges.addAll(p.edgeset);
			
			ExtDWE e1 = (ExtDWE) graph.addEdge(zero, n);
			graph.setEdgeWeight(e1,0);
		}	
		for(NodePair p : possiblePairs){
		for(NodePair p2 : possiblePairs){
			if(!p2.equals(p)){

				ExtDWE e = (ExtDWE) graph.addEdge(nodeMap.get(p), nodeMap.get(p2));
				double weight = p.intersectionSize.get(p2);
				if(weight>=0){
					graph.setEdgeWeight(e, weight);
					edgeMap.put(e,p.intersectionDistances.get(p2));
				}
			}
		}
		}
		
		HamiltonianCycle tree = new HamiltonianCycle();
		List<INode> output = tree.getApproximateOptimalForCompleteGraph(graph);
		
		for(int i =0;i<output.size()-1;i++){
			INode n = output.get(i);
			if(edgeMap.containsKey(graph.getEdge(n, output.get(i+1)))){
				edges.addAll(edgeMap.get(graph.getEdge(n, output.get(i+1))));
			}
		}
		
		
		return edges.size();
		
	}
	/**
	public void getActualCoverage(List<NodePair> possiblePairs, List<ExtDWE> edges, int k){

		List<INode> output = new ArrayList<INode>();
		AbstractBaseGraph graph = new SimpleWeightedGraph<INode, ExtDWE>(ExtDWE.class);
		HashMap<ExtDWE,List<ExtDWE>> edgeMap = new HashMap<ExtDWE,List<ExtDWE>>();
		List<ExtDWE> queue = new ArrayList<ExtDWE>();
				
		int coverage = this.distance+2;
		Node zero = new Node();

		edges.addAll(this.edgeset);
		
		
		for(NodePair p : possiblePairs){
			Node n = new Node();
			edges.addAll(p.edgeset);
			
			ExtDWE e1 = (ExtDWE) graph.addEdge(zero, n);
			graph.setEdgeWeight(e1,this.intersectionDistances.get(p).size());
			
			for(NodePair p2 : possiblePairs){
				Node n2 = new Node();
				ExtDWE e = (ExtDWE) graph.addEdge(n, n2);
				graph.setEdgeWeight(e, p.intersectionDistances.get(p2).size());
				edgeMap.put(e,p.intersectionDistances.get(p2));
			}
		}
		
		queue.addAll(graph.outgoingEdgesOf(zero));
		
		while(!queue.isEmpty()){
			ExtDWE current = queue.remove(0);
			double weight = 0;
			if(coverage+weight<k){
				output.add(current.gettarget())
			}
			for(ExtDWE e : graph.outgoingEdgesOf(current)){
				if(e.getweight())
			};
		}
		
		INode next = zero;
		
		while(coverage<=k){
			graph.outgoingEdgesOf(next);
		}
		
		for(int i =0;i<output.size()-1;i++){
			INode n = output.get(i);
			if(edgeMap.containsKey(graph.getEdge(n, output.get(i+1)))){
				edges.addAll(edgeMap.get(graph.getEdge(n, output.get(i+1))));
			}
		}
		
		
		return edges.size();
	}
	**/
	
	public void listDanglingNodes(){
		for(List<ExtDWE> edges : danglingnodes.values()){
			for(ExtDWE e : edges){
				if(e.getsource().equals(nodeset[0]) || e.getsource().equals(nodeset[1])){
					danglers.add(e.gettarget());
				}else{
					danglers.add(e.getsource());
				}
			}
		}
		if(shareEdge){
			danglers.add(nodeset[0]);
			danglers.add(nodeset[1]);
		}
	}
	public boolean hasNode(ExtDWE e, INode n){
		if(e.getsource().equals(n)||e.gettarget().equals(n)){
			return true;
		}
		return false;
	}
	public int getoldDistance(NodePair p, int k, AbstractBaseGraph graph){
		
		if(intersectionSize.containsKey(p)){
			return intersectionSize.get(p);

		}
		
		int minDist = Integer.MAX_VALUE;
		int curr;
		List<ExtDWE> edges = new ArrayList<ExtDWE>();
		INode shared=shareVertex(p);
		if(shared!=null){
			//if(shareEdge(p, edges)){
			if(sharedEdges(p, edges)){
				intersectionDistances.put(p, edges);
				p.intersectionDistances.put(this, edges);
				
				intersectionSize.put(p, -edges.size());
				p.intersectionSize.put(this, -edges.size());
				return -edges.size();
			}
			minDist = 0;
		}else{
			
			for(INode n : this.nodeset){
				BellmanFordShortestPath<INode, ExtDWE> shortestPath = new BellmanFordShortestPath(graph, n, k);
			
				for(INode n2 : p.nodeset){
					edges = shortestPath.getPathEdgeList(n2);
					if(edges==null){
						curr=Integer.MAX_VALUE;
					}else{
						curr = edges.size();
					}
					if(curr<minDist){
						minDist=curr;
					}
				}
			}
		}
		intersectionDistances.put(p, edges);
		p.intersectionDistances.put(this, edges);
		
		intersectionSize.put(p,  Math.abs(minDist));
		p.intersectionSize.put(this,  Math.abs(minDist));
		
		return Math.abs(minDist);
	}
	public int getDistance(NodePair p, int k,AbstractBaseGraph graph){
		getNodes();
		
		int minDist = Integer.MAX_VALUE;
		int curr;
		
		List<ExtDWE> edges = new ArrayList<ExtDWE>();
		List<ExtDWE> path = new ArrayList<ExtDWE>();
		
		List<ExtDWE> possibleedges = new ArrayList<ExtDWE>();
		
		int extra=2;
		if(this.shareEdge){ extra--;}
		if(p.shareEdge){ extra--;}
		
		//SHARED VERTEX
		INode shared=shareVertex(p);
		if(shared!=null){
			
			//SHARED DANGLING EDGE
			if(listSharedEdges(p, possibleedges)){
				for(ExtDWE e : possibleedges){
					for(INode n : e.getNodes()){
						if(!shared.equals(n) && edges.isEmpty()){
							BellmanFordShortestPath<INode, ExtDWE> shortestPath = new BellmanFordShortestPath(graph, n, k);
							
							INode n2 = this.otherNode(n); 	//other node in this intersection
							INode n3 = p.otherNode(n);		//other node in intersection p
							
							List<ExtDWE> newPath;
							if(n2!=shared){
								newPath = shortestPath.getPathEdgeList(n2);
								if(newPath!=null && newPath.size()<edgeset.size()+1){
									path.addAll(newPath);
									edges.add(e);
									path.add(e);
									path.addAll(p.edgeset);
									break;
								}
							
								
							}else if(n3!=shared){
								newPath = shortestPath.getPathEdgeList(n3);
								if(newPath!=null && newPath.size()<=p.edgeset.size()+1){
									path.addAll(edgeset);
									edges.add(e);
									path.add(e);
									path.addAll(newPath);
									break;
								}
							}
									
						}
					}
				}
				if(edges.isEmpty()){
					path.addAll(edgeset);
					path.add(possibleedges.get(0));
					path.add((ExtDWE) graph.getEdge(possibleedges.get(0).gettarget(),possibleedges.get(0).getsource()));
					path.addAll(p.edgeset);
				}
			}else{
				path.addAll(edgeset);
				path.addAll(p.edgeset);
			}
			
				intersectionDistances.put(p, path);
				p.intersectionDistances.put(this, path);
				
				intersectionSize.put(p, Math.abs(path.size()+extra));
				p.intersectionSize.put(this, Math.abs(path.size()+extra));
				
				return path.size()+extra;
		}
		
		
//		if(sharedEdges(p, edges)){
//			path.addAll(edgeset);
//			path.addAll(edges);
//			path.addAll(p.edgeset);
//			
//			intersectionDistances.put(p, path);
//			p.intersectionDistances.put(this, path);
//			
//			intersectionSize.put(p, path.size()+2);
//			p.intersectionSize.put(this, path.size()+2);
//			return edges.size()+2;
//		}
		
		//needs to reach other intersection through an edge in either p1 or p2
		//start node is then one node away from a node in the intersection
	
		for(INode n : danglers){
			BellmanFordShortestPath<INode, ExtDWE> shortestPath = new BellmanFordShortestPath(graph, n, k);
			
				
			//needs to reach other intersection through an edge in either p1 or p2
			//start node is then one node away from a node in the second intersection
				
			for(INode n2 :p.danglers){

					if(n.equals(n2)){
						curr = 0;
						edges = new ArrayList<ExtDWE>();
					}else{
						edges = shortestPath.getPathEdgeList(n2);
						if(edges==null){
							curr=Integer.MAX_VALUE;
						}else{
							curr = edges.size();
						}
					}
					
					if(curr<minDist){
						minDist=curr;

						
						path.addAll(edgeset);
						
						if(!shareEdge){		
							for(ExtDWE e : danglingedges){
								if(hasNode(e,n)) {
									path.add(e);
									break;
								}
							}
						}
						path.addAll(edges);
						if(!p.shareEdge){
							for(ExtDWE e : p.danglingedges){
								if(hasNode(e,n2)) {
									path.add(e);
									break;
								}
							}
						}	
						path.addAll(p.edgeset);
						
		
					
				}
			}
		}
//		intersectionDistances.put(p, edges);
//		p.intersectionDistances.put(this, edges);
		
		intersectionDistances.put(p, path);
		p.intersectionDistances.put(this, path);
		
		
		intersectionSize.put(p, Math.abs(minDist+extra));
		p.intersectionSize.put(this, Math.abs(minDist+extra));
		return minDist+extra;
		
		//return minDist;
	}


	public List<ExtDWE> getEdges(Double double1){
		List<ExtDWE> edges = new ArrayList<ExtDWE>();
		
		
		if(edgeset.size()+2>double1 && !self && !shareEdge){ 
			return null; 
		}
		
		if(edgeset.isEmpty()){
			int d2=0;
			while(d2<double1){
				if(d2 >= pathset[0].edges.size()){
					edges.add(pathset[0].edges.get(pathset[0].edges.size()-1));
				}else{
					edges.add(pathset[0].edges.get(d2));
				}
				d2++;
			}
			return edges;
		}
		
		edges.addAll(edgeset);
		
		if(shareEdge){
			int x = pathset[0].edges.indexOf(edgeset.get(0));
			int l = pathset[0].edges.size();
			
			if(l>double1){
				if(l-x<double1){ return pathset[0].edges.subList((int) (l-double1), l);}
				return pathset[0].edges.subList(x,(int) (x+double1) );
			}
			
			int x2 = pathset[1].edges.indexOf(edgeset.get(0));
			int l2 = pathset[1].edges.size();
			
			if(l2>double1){	
				if(l2-x2<double1){ return pathset[1].edges.subList((int) (l2-double1), l2);}
				return pathset[1].edges.subList(x2,(int) (x2+double1) );
			}
			
			return pathset[0].edges;
		}

		int n1 = pathset[0].nodes.indexOf(nodeset[0]);
		int up1 = 1;
		int n2 = pathset[1].nodes.indexOf(nodeset[1]);
		int up2 = 1;
		
		ExtDWE e1;
		if(pathset[0].edges.size()/2>=n1 || n1==0){
			e1 = (ExtDWE) pathset[0].graph.getEdge(pathset[0].nodes.get(n1), pathset[0].nodes.get(n1+1));
			edges.add(e1);
			n1++;
		}else{
			e1 = (ExtDWE) pathset[0].graph.getEdge(pathset[0].nodes.get(n1-1), pathset[0].nodes.get(n1));
			edges.add(e1);
			n1--;
			up1=-1;
		}
		
		ExtDWE e2;
		if(pathset[1].edges.size()/2>n2){
			e2 = (ExtDWE) pathset[1].graph.getEdge(pathset[1].nodes.get(n2), pathset[1].nodes.get(n2+1));
			edges.add(e2);
			n2++;
		}else{
			e2 = (ExtDWE) pathset[1].graph.getEdge(pathset[1].nodes.get(n2-1), pathset[1].nodes.get(n2));
			edges.add(e2);
			n2--;
			up2=-1;
		}

		
		int i=0;
		while(edges.size()<double1){
			
			if(n1<pathset[0].nodes.size()-1){
				ExtDWE edge = (ExtDWE) pathset[0].graph.getEdge(pathset[0].nodes.get(n1), pathset[0].nodes.get(n1+up1));
				if(edge==null){
					edge = (ExtDWE) pathset[0].graph.getEdge(pathset[0].nodes.get(n1+up1), pathset[0].nodes.get(n1));
				}
				edges.add(edge);
				n1=n1+up1;
				
				if(edges.size()==double1){ break;}
			}
			
			if(n2<pathset[1].nodes.size()-1){
				ExtDWE edge = (ExtDWE) pathset[1].graph.getEdge(n2, pathset[1].nodes.get(n2+up2));
				if(edge.equals(null)){
					edge = (ExtDWE) pathset[1].graph.getEdge(pathset[1].nodes.get(n2+up2), pathset[1].nodes.get(n2));
				}
				edges.add(edge);
				n2=n2+up2;
				
				if(edges.size()==double1){ break;}
			}

		}
		return edges;
	}
	public List<ExtDWE> getEdges(NodePair p){
		List<ExtDWE> edges = new ArrayList<ExtDWE>();
		
		
		return edges;
	}
	
	private boolean shareEdge(NodePair p, List<ExtDWE> intersection){
		
		
		if(this.nodeset[0].equals(p.nodeset[0]) && this.nodeset[1].equals(p.nodeset[1])){
			sharedEdges(p, intersection);
			return true;
		}
		
		if(this.nodeset[1].equals(p.nodeset[0]) && this.nodeset[0].equals(p.nodeset[1])){
			return true;
		}
		
		return false;
	}
	
	private boolean listSharedEdges(NodePair p,  List<ExtDWE> intersection){
		
		boolean shareEdges = false;
		for(ExtDWE e : this.danglingedges){
			for(ExtDWE ep : p.danglingedges){
				if(e.equals(ep)){
					intersection.add(e);
					shareEdges = true;
				}
			}
		}
		return shareEdges;
	}
	private boolean sharedEdges(NodePair p,  List<ExtDWE> intersection){
		
		for(ExtDWE e : this.danglingedges){
			for(ExtDWE ep : p.danglingedges){
				if(e.equals(ep)){
					intersection.add(e);
					return true;
				}
			}
		}
		return false;
	}
	public INode shareTarget(NodePair p){
		INode v = p.pathset[0].target;
			if(this.pathset[0].target.equals(v) || this.pathset[1].target.equals(v)){
				return v;
			}
		v = p.pathset[1].target;
			if(this.pathset[0].target.equals(v) || this.pathset[1].target.equals(v)){
				return v;
			}
		return null;
		
	}
	public INode shareVertex(NodePair p){
		
		for(INode v : p.nodeset){
			if(this.nodeset[0].equals(v) || this.nodeset[1].equals(v)){
				return v;
			}
		}
		
		return null;
	}
	
	public String toString(){
		String result ="("+pathset[0].graphtarget.getId()+","+pathset[1].graphtarget.getId()+","+this.distance+")";
		
		
		return result;
	}

}
