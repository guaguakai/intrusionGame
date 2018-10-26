package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class TeamBuildingProblem {
	private ActionProfile actProfile;
	//private int numDefenderResources;
	
	private int numDefenderResourceTypes;
	private List<Double> numDefenderResources;
	public List<Double> ResourceCoverage;
	public List<EDGE_TYPE> ResourceEdges;
	public List<NODE_TYPE> ResourceNodes;
	public List<Boolean> ResourceInterdiction; 
	public List<Double> CoverageProbability;
	//public Double CoverageProbability =1.0;
	public boolean isProbability = true;
	
	public double budget = 0;
	public List<Double> costs;
	public boolean buildteam = false;
	
	private AbstractBaseGraph<INode, ExtDWE> graph;
	private List<INode> listTargetNodes;
	
	private INode source = null;
	private INode virtualTarget = null;
	//Limits problem to one source --- make virtual source if necessary.
	//All id's from 1 to |N|, 1 to |E|	
	
	static List<Double> empty;
	//= new ArrayList();
	public TeamBuildingProblem() {
		
		this(empty, empty);
	}
	public TeamBuildingProblem(double budget, List<Double> costs, List<Double> ResourceCoverage){
		this(empty, ResourceCoverage, null, new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class));
		this.budget = budget;
		this.costs=costs;
		buildteam=true;
		
	}
	public TeamBuildingProblem(List<Double> numDefenderResources, List<Double> ResourceCoverage) {
		this(numDefenderResources, ResourceCoverage, null, new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class));
	}
	
	public TeamBuildingProblem(List<Double> numDefenderResources, List<Double> ResourceCoverage,  List<Double> ResourceProbability) {
		this(numDefenderResources, ResourceCoverage, ResourceProbability, new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class));
	}
	
	public TeamBuildingProblem(List<Double> numDefenderResources, List<Double> ResourceCoverage, List<Double> ResourceProbability,
			AbstractBaseGraph<INode, ExtDWE> graph) {
		super();
		this.numDefenderResources = numDefenderResources;
		this.ResourceCoverage = ResourceCoverage;
		this.numDefenderResourceTypes = ResourceCoverage.size();
		this.actProfile = new ActionProfile();
		this.graph = graph;
		this.listTargetNodes = new ArrayList<INode>();
		if(ResourceProbability==null){
			this.isProbability=false;
			this.CoverageProbability = new ArrayList<Double>();
			for(int i=0;i<numDefenderResourceTypes;i++){
				CoverageProbability.add(1.0);
			}
		}else{
			this.CoverageProbability = ResourceProbability;
		}
		DefenderAllocation.numResourceTypes=this.numDefenderResourceTypes;
		DefenderAllocation.ResourceProbability=this.CoverageProbability;
	}
	public void setTeam(int[] num, int[] cover){
		List<Double> resources = new ArrayList<Double>();
		for(int i=0;i<num.length;i++){
			resources.add((double) num[i]);
		}
		List<Double> coverage = new ArrayList<Double>();
		for(int i=0;i<cover.length;i++){
			coverage.add((double) cover[i]);
		}
		this.numDefenderResources = resources;
		this.ResourceCoverage = coverage;
	}
	public List<Double> getNumDefenderResources() {
		return numDefenderResources;
	}
	public void setEdgeTypes(List<EDGE_TYPE> types){
		this.ResourceEdges=types;
	}
	
	public void setNodeTypes(List<NODE_TYPE> types){
		this.ResourceNodes=types;
	}
	
	public int getTotalCoverage(){
		int k=0;
		for(int i=0;i<this.getNumResourceTypes();i++){
			k+= this.numDefenderResources.get(i)*this.ResourceCoverage.get(i);
		}
		return k;
	}
	
	public int getTotalResources(){
		int k=0;
		for(int i=0;i<this.getNumResourceTypes();i++){
			k+= this.numDefenderResources.get(i);
		}
		return k;
	}
	public int getNumResourceTypes(){
		return numDefenderResourceTypes;
	}
	public void setNumDefenderResources(List<Double> numDefenderResources) {
		this.numDefenderResources = numDefenderResources;
	}
	public ActionProfile getActProfile() {
		return actProfile;
	}
	
	public void addNode(Node n) {
		this.graph.addVertex(n);
	}
	public void addEdge(INode n1, INode n2){
		this.graph.addEdge(n1, n2);
	}
	
	public int maxTarget=Integer.MIN_VALUE;
	public int getMaxTarget(){
		if(maxTarget>0){
			return maxTarget;
		}
		
		for (INode n : listTargetNodes){
			if(n.getPayoff()>=maxTarget){
				maxTarget=n.getPayoff();
			}
		}
		return maxTarget;
	}
	public INode getVirtualTarget() throws MalformedGraphException {
		if ( this.virtualTarget != null ) {
			return this.virtualTarget;
		}
		for ( Iterator<INode> nodeIter = this.graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.VIRTUAL_TARGET ) {
				this.virtualTarget = curNode;
				return this.virtualTarget;
			}
		}
		throw new MalformedGraphException();
	}
	
	public INode getSource() throws MalformedGraphException {
		if ( this.source != null ) {
			return this.source;
		}
		for ( Iterator<INode> nodeIter = this.graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.SOURCE ) {
				this.source = curNode;
				return this.source;
			}
		}
		throw new MalformedGraphException();
	}
	
	public int getSources() throws MalformedGraphException {
		int sources =0;
		for ( ExtDWE e : graph.edgeSet() ) {
			if ( e.getType() == EDGE_TYPE.VIRTUAL ) {
				sources++;
			}
		}
		return sources;
		
		//throw new MalformedGraphException();
	}
	
	public List<INode> getTargetNodesSet() throws MalformedGraphException {
		if ( this.listTargetNodes.size() == 0 ) {
			for ( Iterator<INode> nodeIter = this.graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
				INode curNode = nodeIter.next();
				if ( curNode.getType() == NODE_TYPE.TARGET ) {
					this.listTargetNodes.add(curNode);
				}
			}
		}
		if ( this.listTargetNodes.size() == 0)
			throw new MalformedGraphException();
		return this.listTargetNodes;
	}
	
	public void setGraph(AbstractBaseGraph<INode, ExtDWE> graph) {
		this.graph = graph;
	}
	
	public AbstractBaseGraph<INode, ExtDWE> getGraph() {		
		return graph;
	}
	public AbstractBaseGraph<INode, ExtDWE> copyGraph() {
		AbstractBaseGraph<INode, ExtDWE> newgraph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class) ;
		Set<INode> vset = graph.vertexSet();
		for(INode v : vset){
			if(v.getType().equals(NODE_TYPE.SOURCE)){ 
				newgraph.addVertex(v);
				flipPath(newgraph, v);
			}
		}
		return newgraph;
	}
	
	public int getDistancetoOffice(ExtDWE e){
		int dist=Integer.MAX_VALUE;
		
		for(INode n : this.graph.vertexSet()){
			if(n.getType()==NODE_TYPE.OFFICE){
				BellmanFordShortestPath<INode, ExtDWE> shortestPath = new BellmanFordShortestPath(graph, n);
			
				List<ExtDWE> edges = shortestPath.getPathEdgeList(e.getsource());
				if(!(edges==null)){
					dist = edges.size();
				}
			}
		}
		return dist;
	}
	public void flipPath(AbstractBaseGraph<INode, ExtDWE> newgraph, INode n){
		for(ExtDWE e : graph.outgoingEdgesOf(n)){
			if(!newgraph.vertexSet().contains(e.gettarget())){
				newgraph.addVertex(e.gettarget());
			}
			newgraph.addEdge(e.gettarget(), n);
			flipPath(newgraph, e.gettarget());
		}
	}
	public Set<ExtDWE> nextE(ExtDWE e){
		return this.getGraph().outgoingEdgesOf(getto(e));
	}
	
	public Set<ExtDWE> prevE(ExtDWE e){
		//return this.getGraph().incomingEdgesOf(getfrom(e));
		return graph.incomingEdgesOf(e.getsource());
	}
	
	public INode getto(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(this.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: this.getGraph().incomingEdgesOf(curNode)){
				
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}
	
	public INode getfrom(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(this.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: this.getGraph().outgoingEdgesOf(curNode)){
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}

	public List<ExtDWE> getRandom(ExtDWE e, int k){
		List<ExtDWE> path = new ArrayList<ExtDWE>();
		path.add(e);
		ExtDWE edge = e;
		
		while(path.size()<k){
			Iterator<ExtDWE> edgeIter = nextE(edge).iterator(); 
			if(edgeIter.hasNext()){
				edge = edgeIter.next();
				path.add(edge);
			}else{
				path.remove(edge);
				edge=path.get(path.size()-1);
			}
			
		}
		return path;
	}
	
	public List<ExtDWE> getSet(ExtDWE e, int k, Map<ExtDWE, Double> mapEdges){
		double before;
		double after;
		ExtDWE maxWeightEdgeB = null; 
		ExtDWE maxWeightEdgeA = null; 
		
		List<ExtDWE> path = new ArrayList<ExtDWE>();
		path.add(e);
		ExtDWE pedge = e;
		ExtDWE nedge = e;
		
		for(int j=0;j<k-1;j++){
			before = Double.NEGATIVE_INFINITY;
			after= Double.NEGATIVE_INFINITY;
			maxWeightEdgeB = null; 
			maxWeightEdgeA = null; 
			
			Set<ExtDWE> prev = prevE(pedge);
			for( Iterator<ExtDWE> edgeIter = prevE(pedge).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(mapEdges.containsKey(curEdge) && before < mapEdges.get(curEdge) && !path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					before = mapEdges.get(curEdge);
					 maxWeightEdgeB = curEdge; 
				}
			}
			Set<ExtDWE> nxt = nextE(nedge);
			for( Iterator<ExtDWE> edgeIter = nextE(nedge).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(mapEdges.containsKey(curEdge) && after < mapEdges.get(curEdge) && !path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					after = mapEdges.get(curEdge);
					maxWeightEdgeA = curEdge; 
				}
			}
			if(maxWeightEdgeA==null && maxWeightEdgeB==null){
				return path;
			}
			if(before<after){
				path.add(maxWeightEdgeA);
				nedge = maxWeightEdgeA;
			}else{
				path.add(0,maxWeightEdgeB);
				pedge = maxWeightEdgeB;
			}
		}
		if(!isValidPath(path)){
			System.out.print(path);
		}
		return path;
	}
	public List<ExtDWE> getRSet(ExtDWE e, int k){
		double before;
		double after;
		ExtDWE maxWeightEdgeB = null; 
		ExtDWE maxWeightEdgeA = null; 
		
		List<ExtDWE> path = new ArrayList<ExtDWE>();
		path.add(e);
		ExtDWE pedge = e;
		ExtDWE nedge = e;
		
		for(int j=0;j<k-1;j++){
			before = Double.NEGATIVE_INFINITY;
			after= Double.NEGATIVE_INFINITY;
			maxWeightEdgeB = null; 
			maxWeightEdgeA = null; 
			if(pedge==null){
				System.out.print("ohno!");
			}
			Set<ExtDWE> prev = prevE(pedge);
			for( Iterator<ExtDWE> edgeIter = prevE(pedge).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(!path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					maxWeightEdgeB = curEdge; 
					break;
				}
			}
			Set<ExtDWE> nxt = nextE(nedge);
			for( Iterator<ExtDWE> edgeIter = nextE(nedge).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(!path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					maxWeightEdgeA = curEdge; 
					break;
				}
			}
			if(maxWeightEdgeA==null && maxWeightEdgeB==null){
				return path;
			}
			if(maxWeightEdgeA!=null){
				path.add(maxWeightEdgeA);
				nedge = maxWeightEdgeA;
			}else{
				path.add(0,maxWeightEdgeB);
				pedge = maxWeightEdgeB;
			}
		}
		if(!isValidPath(path)){
			System.out.print(path);
		}
		return path;
	}
	public List<ExtDWE> getSet2(ExtDWE e, int k, Map<ExtDWE, Double> mapEdges){

		ExtDWE forward = e; 
		ExtDWE backward = e; 
		
		ExtDWE maxforward = null; 
		ExtDWE maxbackward = null; 
		
		double after=0;
		double before=0;
		
		List<ExtDWE> path = new ArrayList<ExtDWE>();
		path.add(e);
		ExtDWE edge = e;
		
		while(path.size()<k){

			for( Iterator<ExtDWE> edgeIter = prevE(backward).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(mapEdges.containsKey(curEdge) && !path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					before = mapEdges.get(curEdge);
					maxbackward = curEdge; 
				}
			}

			for( Iterator<ExtDWE> edgeIter = nextE(forward).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(mapEdges.containsKey(curEdge) && !path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					after = mapEdges.get(curEdge);
					maxforward = curEdge; 
				}
			}
			if(maxbackward == backward && maxforward == forward){
				return path;
			}
			
			if(before >= after && maxbackward!=null && !path.contains(maxbackward)){
				path.add(0,maxbackward);
				backward=maxbackward;
				
			}else if(maxforward!=null && !path.contains(maxforward)){
				path.add(maxforward);
				forward=maxforward;
			}


		}
		if(!isValidPath(path)){
			System.out.print(path);
		}
		return path;
	}
	public List<ExtDWE> getRandomSet2(ExtDWE e, int k, DefenderAllocation da){

		ExtDWE forward = e; 
		ExtDWE backward = e; 
		
		List<ExtDWE> path = new ArrayList<ExtDWE>();
		path.add(e);
		ExtDWE edge = e;
		
		while(path.size()<k){

			for( Iterator<ExtDWE> edgeIter = prevE(backward).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(!da.contains(curEdge) && !path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					backward = curEdge; 
					break;
				}
			}
			for( Iterator<ExtDWE> edgeIter = nextE(forward).iterator(); edgeIter.hasNext();){
				ExtDWE curEdge = edgeIter.next();
				if(!da.contains(curEdge) && !path.contains(curEdge) && curEdge.getType()==EDGE_TYPE.NORMAL){
					forward = curEdge; 
					break;
				}
			}
			if(backward==null && forward==null){
				return path;
			}
			if(backward!=null && !path.contains(backward)){
				path.add(0,backward);
			}
			if(path.size()<k && forward!=null &&!path.contains(forward)){
					path.add(forward);
			}
			
		}

		return path;
	}
	
	public boolean isValidPath(List<ExtDWE> path){
		for(int i=0;i<path.size()-1;i++){
			if(!nextE(path.get(i)).contains(path.get(i+1))){
				return false;
			}
		}
		
		return true;
	}
	//GET RANDOM SUBPATH
	List<ArrayList<ExtDWE>> frontier = new ArrayList<ArrayList<ExtDWE>>();
	
	public List<ExtDWE> getUnWeightedSet(int k, DefenderAllocation da){
		List<ExtDWE> maxpath = new ArrayList<ExtDWE>();
		for( Iterator<ExtDWE> edgeIter = graph.outgoingEdgesOf(this.getSource()).iterator(); edgeIter.hasNext();){
			ArrayList<ExtDWE> path = new ArrayList<ExtDWE>();
			ExtDWE curEdge = edgeIter.next();
			if(curEdge.getType()==EDGE_TYPE.NORMAL && !da.contains(curEdge)){
				path.add(curEdge);
				frontier.add(path);
			}else{
				startSet(k,curEdge,da);
			}	
		}
		while(frontier.size()>0){
			ArrayList<ExtDWE> path = frontier.remove(0);
			
			if(path.size()==k){
				frontier.clear();
				return path;
			}else if(path.size()>maxpath.size()){
				maxpath=path;
			}
			Set(k,path,da);
		}
		frontier.clear();
		return maxpath;
	}
	public void Set(int k, List<ExtDWE> path, DefenderAllocation da){
		for( Iterator<ExtDWE> edgeIter = nextE(path.get(path.size()-1)).iterator(); edgeIter.hasNext();){
			ArrayList<ExtDWE> path2 = new ArrayList<ExtDWE>();
			path2.addAll(path);
			ExtDWE curEdge = edgeIter.next();
			if(curEdge != null && !da.contains(curEdge)){
				path2.add(curEdge);
				frontier.add(path2);
			}
		}
	}
	public void startSet(int k, ExtDWE edge, DefenderAllocation da){
		for( Iterator<ExtDWE> edgeIter = nextE(edge).iterator(); edgeIter.hasNext();){
			ArrayList<ExtDWE> path2 = new ArrayList<ExtDWE>();
			ExtDWE curEdge = edgeIter.next();
			if(curEdge != null){
				if(!da.contains(curEdge)){
					path2.add(curEdge);
					frontier.add(path2);
				}else{
					startSet(k,curEdge,da);
				}
			}
		}
	}
	
	public void getMinCutPaths(){
		
		Set<ExtDWE> minCut;
		
		//get mincut for each individual target
		for (INode targetNode : this.getTargetNodesSet()) {
			minCut = computeSingleMinCut(targetNode);
			
			//for each edge in the mincut get shortest path through mincut
			for(ExtDWE edge : minCut){
				INode s = edge.getsource();
				INode t = edge.gettarget();
				List<ExtDWE> lstPaths1 = new ArrayList<ExtDWE>();
				
				if(!s.equals( this.getSource())){
					BellmanFordShortestPath shortestPath1 = new BellmanFordShortestPath(graph, this.getSource());
					lstPaths1 = shortestPath1.getPathEdgeList(s);
				}
				
				lstPaths1.add(edge);
				
				if(!t.equals(targetNode)){
					BellmanFordShortestPath shortestPath2 = new BellmanFordShortestPath(graph, t);
					List<ExtDWE> lstPaths2 = shortestPath2.getPathEdgeList(targetNode);
					lstPaths1.addAll(lstPaths2);	
				}
				
				
				AdversaryPath ap = new AdversaryPath(targetNode,lstPaths1);
				this.getActProfile().addAdversaryPath(ap);
				
			}
			//minCutEdges.addAll(minCut);
			minCut.clear();
				
				
		}
			
		
	}
	

	private Set<ExtDWE> computeSingleMinCut(INode targetNode) {
		MinCut minCutObj = new MinCut(this);

		minCutObj.setTarget(targetNode);

		minCutObj.resetLP();

		Set<ExtDWE> minCutSet = minCutObj.getMinCut();
		return minCutSet;
		
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(listTargetNodes.size()==0) getTargetNodesSet();
		return "UrbanSecProblem [numDefenderResources=" + numDefenderResources
				+ ", graph=" + graph + ", listTargetNodes=" + listTargetNodes
				+ "]";
	}
	
	
}

