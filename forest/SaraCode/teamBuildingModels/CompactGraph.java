package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;

import org.jgrapht.Graphs;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.WeightedMultigraph;

public class CompactGraph {
	public AbstractBaseGraph<INode, ExtDWE> CompactGraph;
	public AbstractBaseGraph<INode, ExtDWE> ReducedGraph;
	public AbstractBaseGraph<INode, ExtDWE> graph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
	
	private static int[][] distances;
	private static NodePair[][] intersects;
	
	public List<CompactPath> attackerPaths;
	public HashMap<INode, List<EDGE_TYPE>> terrainMap;
	public HashMap<INode, INode> targetMap;
	
	public CompactGraph(){
		attackerPaths = new ArrayList<CompactPath>();
		targetMap = new HashMap<INode, INode>();
		
	}
	public void inputCompactPaths(List<CompactPath> attackerPaths){
		this.attackerPaths=attackerPaths;
	}
	
	public void inputAdversaryPaths( List<AdversaryPath> lstAP, TeamBuildingProblem teamProb){
		for(AdversaryPath ap : lstAP){
			CompactPath cp = new CompactPath(ap, teamProb.getGraph());
			attackerPaths.add(cp);
		}
		
	}
	public void addPaths(List<AdversaryPath> lstap){
		if(lstap!=null){
		for(AdversaryPath ap : lstap){
			CompactPath c = new CompactPath(ap, graph);
			attackerPaths.add(c);
		}}
	}
	public AbstractBaseGraph<INode, ExtDWE> getCompactGraph(TeamBuildingProblem teamProb){
		if(CompactGraph==null){
			return buildCompactGraph(teamProb);
		}
		return CompactGraph;
	}
	public AbstractBaseGraph<INode, ExtDWE> buildCompactGraph(TeamBuildingProblem teamProb){
		CompactGraph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		Graphs.addGraph(this.graph, teamProb.getGraph());
		
		if(attackerPaths.isEmpty()){
			
			 getMinCutPaths(teamProb); 
			
			//first clean up the target graph so that resources cannot use virtual edges
			this.graph = removeVirtualEdges(this.graph); 
			
			//compute the distances between each paths
			computeDistances();
			
			
			Node[] targets = new Node[distances[0].length];
			List<ExtDWE> edges = new ArrayList<ExtDWE>();
			
			for(int i=0;i<distances[0].length;i++){
				
				Node target1 = (Node) attackerPaths.get(i).target;
				Node n1 = new Node(NODE_TYPE.TARGET, target1.payoff);

				CompactGraph.addVertex(n1);
				
				attackerPaths.get(i).target = n1;
				targetMap.put(n1, target1);
				targets[i]=n1;
			}
			
			int maxcoverage=0;
			for(double cov : teamProb.ResourceCoverage){
				if( cov > maxcoverage){
					maxcoverage = (int)cov;
				}
			}
			
			
			for(int i=0;i<targets.length-1;i++){
				for(int j=i+1;j<targets.length;j++){
					
					Node n1 = targets[i];
					Node n2 = targets[j];
					
					if(distances[i][j]<=maxcoverage && distances[i][j]>0){
						ExtDWE e1 = CompactGraph.addEdge(n1, n2);
						CompactGraph.setEdgeWeight(e1, distances[i][j]);
						edges.add(e1);
					}
					if(distances[j][i]<=maxcoverage && distances[j][i]>0){
						ExtDWE e2 = CompactGraph.addEdge(n2, n1);
						CompactGraph.setEdgeWeight(e2, distances[j][i]);
						edges.add(e2);
					}
					
				}
			}

		}
		//System.out.println("num-paths"+this.attackerPaths.size());
		return CompactGraph;
	}
	
	/**Computing shortest attacker paths that go through each edge in the mincut 
	 * for each target*/
	public void getMinCutPaths(TeamBuildingProblem teamProb){
		
		Set<ExtDWE> minCut;
		
		//get mincut for each individual target
		for (INode targetNode : teamProb.getTargetNodesSet()) {
			minCut = computeSingleMinCut(targetNode, teamProb);
			
			//for each edge in the mincut get shortest path through mincut
			for(ExtDWE edge : minCut){
				INode s = edge.getsource();
				INode t = edge.gettarget();
				List<ExtDWE> lstPaths1 = new ArrayList<ExtDWE>();
				
				if(!s.equals( teamProb.getSource())){
					BellmanFordShortestPath shortestPath1 = new BellmanFordShortestPath(graph, teamProb.getSource());
					lstPaths1 = shortestPath1.getPathEdgeList(s);
				}
				
				lstPaths1.add(edge);
				
				if(!t.equals(targetNode)){
					BellmanFordShortestPath shortestPath2 = new BellmanFordShortestPath(graph, t);
					List<ExtDWE> lstPaths2 = shortestPath2.getPathEdgeList(targetNode);
					lstPaths1.addAll(lstPaths2);	
				}
				
				
				CompactPath ap = new CompactPath(lstPaths1, targetNode, graph);
				boolean contains = false;
				for(CompactPath p : attackerPaths){
					if(p.equals(ap)){
						contains = true;
						break;
					}
				}
				
				if(!contains){
					attackerPaths.add(ap);
				}
			}
			//minCutEdges.addAll(minCut);
			minCut.clear();
				
				
		}
			
		
	}
	private Set<ExtDWE> computeSingleMinCut(INode targetNode, TeamBuildingProblem teamProb) {
		MinCut minCutObj = new MinCut(teamProb);

		minCutObj.setTarget(targetNode);

		minCutObj.resetLP();

		Set<ExtDWE> minCutSet = minCutObj.getMinCut();
		return minCutSet;
		
		
	}
	
	public boolean distancesComputed = false;
	/**Methods for computing path distances from each other */
	public void computeDistances(){
		
		distancesComputed=true;
		
		distances = new int[attackerPaths.size()][attackerPaths.size()];
		intersects = new NodePair[attackerPaths.size()][attackerPaths.size()];
		
		//for each attacker path
		for(int i=0;i<attackerPaths.size()-1;i++){
			CompactPath p1 = attackerPaths.get(i);	//create a compact path
					
			//create the self intersection (only covering that path) 
			NodePair self = new NodePair();
			self.nodeset[0]=p1.target;
			
			self.pathset[0]= p1;
			self.pathset[1]= p1;
			
			self.distance=1;
			self.self = true;
			
			distances[i][i]=1;
			intersects[i][i]=self;
			
			if(i==attackerPaths.size()-2){
				NodePair self2 = new NodePair();
				self2.pathset[0]= p1;
				self2.pathset[1]= p1;
				
				self2.distance=1;
				self2.self = true;
				distances[i+1][i+1]=1;
				intersects[i+1][i+1]=self;
			}
			
			//for each unseen attacker path
			for(int j=i+1;j<attackerPaths.size();j++){
				CompactPath p2 = attackerPaths.get(j);
				NodePair p = new NodePair();
				p.pathset[0]=p1;
				p.pathset[1]=p2;
				
				//if the paths share an edge
				if(shareEdge(p1,p2,p)||shareEdge(p2,p1,p)){
					distances[i][j] = 1; //it only costs 1 edge to cover
				
				//if the paths share a vertex
				}else if(shareVertex(p1,p2,p)||shareVertex(p2,p1,p)){
					distances[i][j] = 2; //takes at least 2 resources to cover
				}else{
					distances[i][j] = pathDistance(p1,p2,p)+2; //costs the distance plus one edge for each path
					
					if(distances[i][j]<0){ distances[i][j] = pathDistance(p2,p1,p)+2;}
				}
				
				p.setDistance(distances[i][j]);
				distances[j][i] = distances[i][j];
				
				intersects[i][j] = p;
				intersects[j][i] = p;

			}
		}
	}
	private boolean shareVertex(CompactPath p1, CompactPath p2, NodePair p){
		List<ExtDWE> edges= new ArrayList<ExtDWE>();
		
		for(INode v : p1.nodes){
			if(p2.nodes.contains(v) && v!=null){
				
				for( ExtDWE e :this.graph.edgesOf(v)){
					if(p1.edges.contains(e)){
						p.danglingedges.add(e);
						edges.add(e);
					
					}else if( p2.edges.contains(e)){
						p.danglingedges.add(e);
						edges.add(e);
					}
				}
				
				p.danglingnodes.put(v, edges);
				p.listDanglingNodes();
				p.nodeset[0]=(v);
				p.nodeset[1]=(v);
				
				return true;
			}
		}
		
		return false;
	}
	private boolean shareEdge(CompactPath p1, CompactPath p2, NodePair p){
		
		for(ExtDWE e : p1.edges){
			if(p2.edges.contains(e)){
				
				p.nodeset[0]=(e.getsource());
				p.nodeset[1]=(e.gettarget());
				
				p.edgeset.add(e);
				//p.danglingedges.put(e, null);
				p.shareEdge=true;
				p.listDanglingNodes();
				return true;
			}
		}
		
		return false;
	}
	private int pathDistance(CompactPath p1, CompactPath p2, NodePair p){
		
		int minPathDistance = Integer.MAX_VALUE;
		
		for(INode n1 : p1.nodes){
			BellmanFordShortestPath shortestPath = new BellmanFordShortestPath(graph, n1 );
			
			for(INode n2 : p2.nodes){
				List path = shortestPath.getPathEdgeList(n2);
				
				if(path!=null){
					int dist = path.size();
					if(dist<minPathDistance){
						
						p.nodeset[0]=(n1);
						p.nodeset[1]=(n2);
						
						p.edgeset=path;
						
						
						minPathDistance = dist;
					}
				}
			}
			
		}
		
		List<ExtDWE> edges1= new ArrayList<ExtDWE>();
		//for( ExtDWE e :this.graph.edgesOf(p.nodeset[0])){
		for( ExtDWE e :this.graph.edgesOf(p.nodeset[0])){
			if(p1.edges.contains(e)){
				p.danglingedges.add(e);
				edges1.add(e);
			}
		}
		p.danglingnodes.put(p.nodeset[0],edges1);
		
		List<ExtDWE> edges2= new ArrayList<ExtDWE>();
		for( ExtDWE e :this.graph.edgesOf(p.nodeset[1])){
			if(p2.edges.contains(e)){
				p.danglingedges.add(e);
				edges2.add(e);
			}
		}
		p.danglingnodes.put(p.nodeset[1],edges2);
		
		p.listDanglingNodes();
		return minPathDistance;
		
	}
	
	
	public AbstractBaseGraph<INode, ExtDWE> buildReducedGraph(TeamBuildingProblem teamProb){
		ReducedGraph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		AbstractBaseGraph<INode, ExtDWE> OGgraph = teamProb.getGraph();
		
		

		for(CompactPath cp : attackerPaths){
			AdversaryPath ap = cp.ap;
			
			ReducedGraph.edgeSet().addAll(ap.getPath());
			ReducedGraph.vertexSet().addAll(cp.nodes);
		}
		
		for(INode n : ReducedGraph.vertexSet()){
			
		}
			
		ReducedGraph.vertexSet().add(teamProb.getSource());	
		return ReducedGraph;
	}
	private int getAllPaths(CompactPath p1, CompactPath p2, int maxPathDist){
		
		int minPathDistance = Integer.MAX_VALUE;
		
		for(INode n1 : p1.nodes){
			BellmanFordShortestPath shortestPath = new BellmanFordShortestPath(graph, n1 );
			
			for(INode n2 : p2.nodes){
				List path = shortestPath.getPathEdgeList(n2);
				shortestPath.
				if(path!=null){
					int dist = path.size();
					if(dist<minPathDistance){
						
						p.nodeset[0]=(n1);
						p.nodeset[1]=(n2);
						
						p.edgeset=path;
						
						
						minPathDistance = dist;
					}
				}
			}
			
		}
		
		List<ExtDWE> edges1= new ArrayList<ExtDWE>();
		//for( ExtDWE e :this.graph.edgesOf(p.nodeset[0])){
		for( ExtDWE e :this.graph.edgesOf(p.nodeset[0])){
			if(p1.edges.contains(e)){
				p.danglingedges.add(e);
				edges1.add(e);
			}
		}
		p.danglingnodes.put(p.nodeset[0],edges1);
		
		List<ExtDWE> edges2= new ArrayList<ExtDWE>();
		for( ExtDWE e :this.graph.edgesOf(p.nodeset[1])){
			if(p2.edges.contains(e)){
				p.danglingedges.add(e);
				edges2.add(e);
			}
		}
		p.danglingnodes.put(p.nodeset[1],edges2);
		
		p.listDanglingNodes();
		return minPathDistance;
		
	}
	
	
	/**
	 * Cleans up graph to remove virtual edges and sources
	 * @param graph
	 * @return cleaned up graph with no virtual edges or sources
	 */
	private AbstractBaseGraph<INode,ExtDWE> removeVirtualEdges(AbstractBaseGraph<INode,ExtDWE> graph){
		
		List<ExtDWE> toRemove = new ArrayList<ExtDWE>();
		List<INode> nodestoRemove = new ArrayList<INode>();
		
		for(ExtDWE e : graph.edgeSet()){
			if(e.getType()==EDGE_TYPE.VIRTUAL){
				toRemove.add(e);
				if(!nodestoRemove.contains(e.getsource())){
				nodestoRemove.add(e.getsource());
			}
			if(!nodestoRemove.contains(e.gettarget())){
				nodestoRemove.add(e.gettarget());
			}
		
			}
		}
		graph.removeAllEdges(toRemove);

		return graph;
	}
	
	//~~~~~~~~~~~~~~~~~~//
	
	public HashMap<ExtDWE, Integer> getWeightedMincut(TeamBuildingProblem teamProb){
		
		Set<ExtDWE> minCut;
		HashMap<ExtDWE, Integer> minCutEdges = new HashMap<ExtDWE,Integer>();
		
		//get mincut for each individual target
		for (INode targetNode : teamProb.getTargetNodesSet()) {
			minCut = computeSingleMinCut(targetNode, teamProb);
			int value = targetNode.getPayoff();
			
			for(ExtDWE edge : minCut){
				if(minCutEdges.containsKey(edge)){
					minCutEdges.put(edge,minCutEdges.get(edge)*value);
				}else{
					minCutEdges.put(edge,value);
				}
			}
			//minCutEdges.addAll(minCut);
			minCut.clear();
				
				
		}
		return minCutEdges;
	}

}
