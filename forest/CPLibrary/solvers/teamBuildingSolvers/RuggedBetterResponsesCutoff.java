package teamBuildingSolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import lpWrapper.Configuration;
import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.GraphSearch;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.MinCut;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import utilities.Pair;

public class RuggedBetterResponsesCutoff extends Rugged {
	private double cutoff = Double.POSITIVE_INFINITY;
	private boolean cutoffUsed=false;
	public int deforaclecount=0;
	public int betterdeforaclecount=0;
	
	protected int numWarmStartConvergeIterations;

	protected int maxWarmStartConvergeIterations;
	protected int kShortestPaths;
	protected int numRoundsBeforeDiscard;

	protected boolean enableDiscardUselessActions;
	protected boolean enableSkeleton;
	protected boolean skeletonBetterDefenderOracle;
	protected boolean skeletonBetterAttackerOracle;

	protected boolean enableWarmStartConverge;
	protected boolean onlyWarmStartConverge;

	protected static boolean enableBetterDefenderOracle;
	protected boolean enableBetterAttackerOracle;

	Set<ExtDWE> minCutEdges;
	RuggedBetterResponsesCutoff parentObj = null;

	protected double betterResponseEpsilon;
	
	public double getBetterResponseEpsilon() {
		return betterResponseEpsilon;
	}

	public void setBetterResponseEpsilon(double betterResponseEpsilon) {
		this.betterResponseEpsilon = betterResponseEpsilon;
	}

	public double getFinalConvergenceEpsilon() {
		return finalConvergenceEpsilon;
	}

	public void setFinalConvergenceEpsilon(double finalConvergenceEpsilon) {
		this.finalConvergenceEpsilon = finalConvergenceEpsilon;
	}

	protected double finalConvergenceEpsilon;

	public boolean isOnlyWarmStartConverge() {
		return onlyWarmStartConverge;
	}

	public void setOnlyWarmStartConverge(boolean onlyWarmStartConverge) {
		this.onlyWarmStartConverge = onlyWarmStartConverge;
	}

	public int getNumWarmStartConvergeIterations() {
		return numWarmStartConvergeIterations;
	}

	public void setNumWarmStartConvergeIterations(
			int numWarmStartConvergeIterations) {
		this.numWarmStartConvergeIterations = numWarmStartConvergeIterations;
	}

	public int getNumRoundsBeforeDiscard() {
		return numRoundsBeforeDiscard;
	}

	public void setNumRoundsBeforeDiscard(int numRoundsBeforeDiscard) {
		this.numRoundsBeforeDiscard = numRoundsBeforeDiscard;
	}

	public boolean isEnableDiscardUselessActions() {
		return enableDiscardUselessActions;
	}

	public void setEnableDiscardUselessActions(
			boolean enableDiscardUselessActions) {
		this.enableDiscardUselessActions = enableDiscardUselessActions;
	}

	public boolean isEnableSkeleton() {
		return enableSkeleton;
	}
	
	public void setParent(RuggedBetterResponsesCutoff parent){
		this.parentObj=parent;
	}
	
	public void setEnableSkeleton(boolean enableSkeleton,
			boolean skeletonBetterDefenderOracle,
			boolean skeletonBetterAttackerOracle) {
		this.enableSkeleton = enableSkeleton;
		this.skeletonBetterDefenderOracle = skeletonBetterDefenderOracle;
		this.skeletonBetterAttackerOracle = skeletonBetterAttackerOracle;
	}

	public int getMaxWarmStartConvergeIterations() {
		return maxWarmStartConvergeIterations;
	}

	public void setMaxWarmStartConvergeIterations(
			int maxWarmStartConvergeIterations) {
		this.maxWarmStartConvergeIterations = maxWarmStartConvergeIterations;
	}

	public boolean isEnableWarmStartConverge() {
		return enableWarmStartConverge;
	}

	public void setEnableWarmStartConverge(boolean enableWarmStartConverge) {
		this.enableWarmStartConverge = enableWarmStartConverge;
	}

	public boolean isEnableBetterDefenderOracle() {
		return enableBetterDefenderOracle;
	}

	public static void setEnableBetterDefenderOracle(boolean enableBetterDefenderOracle) {
		enableBetterDefenderOracle = enableBetterDefenderOracle;
	}

	public boolean isEnableBetterAttackerOracle() {
		return enableBetterAttackerOracle;
	}

	public void setEnableBetterAttackerOracle(boolean enableBetterAttackerOracle) {
		this.enableBetterAttackerOracle = enableBetterAttackerOracle;
	}

	public RuggedBetterResponsesCutoff(TeamBuildingProblem usProblem) {
		super(usProblem);
		this.maxWarmStartConvergeIterations = -1;
		this.numWarmStartConvergeIterations = 0;
		this.enableBetterAttackerOracle = true;
		enableBetterDefenderOracle = false;
		this.enableSkeleton = false;
		this.enableDiscardUselessActions = false;
		this.numRoundsBeforeDiscard = 50;
		this.kShortestPaths = 2;
		this.skeletonBetterDefenderOracle = true;
		this.skeletonBetterAttackerOracle = true;
		this.onlyWarmStartConverge = false;
		this.enableWarmStartConverge = false;
		this.minCutEdges = null;
		this.betterResponseEpsilon = 0.001;
		this.finalConvergenceEpsilon = 0.001;
	}
	public RuggedBetterResponsesCutoff(TeamBuildingProblem usProblem, int cutoff){
		this(usProblem);
		this.cutoff=cutoff;
	}

	public int getkShortestPaths() {
		return kShortestPaths;
	}

	public void setkShortestPaths(int kShortestPaths) {
		this.kShortestPaths = kShortestPaths;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	//~~~~~~~~~~~~~~~~~~WARM START METHODS~~~~~~~~~~~~~~~~~~~//
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	
	public void setWarmStart(CompactProblem Obj){
		List<AdversaryPath> lstAP = Obj.getAdversaryPaths();
		List<DefenderAllocation>
	}
	
	public void setWarmStart(RuggedBetterResponsesCutoff Obj, int index){
		//List<DefenderAllocation> lstDA = getWarmStartDefenderRandomEdges(Obj.usProblem.getTotalCoverage());
		List<DefenderAllocation> lstDA = getWarmStartDefenderParentSolution(Obj,index );
		List<AdversaryPath> lstAP = Obj.getAdversaryPaths();
		//List<AdversaryPath> lstAP = getWarmStartAttackerShortestPath();
		
		this.warmStart(lstDA, lstAP);
	}
	
	public List<DefenderAllocation> getWarmStartDefenderRandomEdges(int count) {
		List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();

		while (lstDA.size() < count) {
			DefenderAllocation da = new DefenderAllocation();
			List<ExtDWE> lstEdges = new ArrayList<ExtDWE>(this.usProblem
					.getGraph().edgeSet());
			Collections.shuffle(lstEdges, model.Configuration.randomGenerator);
			
			
			for(int i=0; i < this.usProblem.getNumResourceTypes();i++){
				for(int j =0;j<this.usProblem.getNumDefenderResources().get(i);j++){
					int k=this.usProblem.ResourceCoverage.get(i).intValue();
					List<ExtDWE> coverage = this.usProblem.getUnWeightedSet(k, da);
					for( ExtDWE e : coverage){
						da.addEdgeToAllocation(e);
						da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
					}
					
				}
			}
			
			while (da.size() < this.usProblem.getTotalCoverage()
					&& lstEdges.size() > 0) {
				ExtDWE e = lstEdges.remove(0);
				if(e.getType()==EDGE_TYPE.NORMAL){
					da.addEdgeToAllocation(e);
				}
			}
			lstDA.add(da);
		}
		return lstDA;
	}
	
	public List<DefenderAllocation> getWarmStartDefenderParentSolution(RuggedBetterResponsesCutoff Obj, int index) {
		List<DefenderAllocation> oldDA = Obj.getDefenderAllocations();
		List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();
		
		
		
		for( DefenderAllocation da : oldDA){
			DefenderAllocation newDa = new DefenderAllocation();
			
			//copy parent defender allocation
			List<ExtDWE> lstEdges = new ArrayList<ExtDWE>(this.usProblem
					.getGraph().edgeSet());
			for( ExtDWE e : lstEdges){
				if(da.getAllocation().contains(e) && newDa.size()<this.usProblem.getTotalCoverage()){
					newDa.addEdgeToAllocation(e);
				}
			}
			
			
			Set<ExtDWE> mincut;
			
			if(this.minCutEdges==null){
				this.computeMinCut();
			}
			mincut = this.minCutEdges;
			
			ExtDWE Edge = null;
			if(!mincut.isEmpty()){
				for(ExtDWE e : mincut){
					if(e.getType()!=EDGE_TYPE.VIRTUAL && !da.contains(e)){
						Edge = e;
						break;
					}
				}
				mincut.remove(Edge);
				
			}
			if(Edge==null){
				List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
				Collections.shuffle(edgeList);
				
				Edge = edgeList.remove(0);
				
				while(Edge.getType()==EDGE_TYPE.VIRTUAL && da.contains(Edge)){
					Edge=edgeList.remove(0);
				}
			}
			
			if(Edge==null){
				System.out.println("ohno!");
			}
			int k = this.usProblem.ResourceCoverage.get(index).intValue();
			int j = this.usProblem.getNumDefenderResources().get(index).intValue();

			List<ExtDWE> coverage = this.usProblem.getRSet(Edge,k);
		
			if(!this.usProblem.isValidPath(coverage)){
				System.out.print("notvalid2");
			}
			if(coverage.size()<k){
				System.out.print("hey2");
			}
			for( ExtDWE e : coverage){
				da.addEdgeToAllocation(e);
				da.addEdgetoResource(e,"Resource"+(index+1)+","+(j+1));
			}
			

			lstDA.add(newDa);	
		}
		return lstDA;
	}
	
	
	public List<AdversaryPath> getWarmStartAttackerShortestPath() {
		for (ExtDWE e : this.usProblem.getGraph().edgeSet()) {
			this.usProblem.getGraph().setEdgeWeight(e, 1.0);
		}
		List<AdversaryPath> lstAP = new ArrayList<AdversaryPath>();
		lstAP = new ArrayList<AdversaryPath>();
		for (INode target : this.usProblem.getTargetNodesSet()) {
			lstAP.add(new AdversaryPath(target, DijkstraShortestPath
					.findPathBetween(this.usProblem.getGraph(),
							this.usProblem.getSource(), target)));
		}

		for (ExtDWE e : this.usProblem.getGraph().edgeSet()) {
			this.usProblem.getGraph().setEdgeWeight(e, 0.0);
		}
		return lstAP;
	}

	public List<AdversaryPath> getWarmStartAttackerRanger() {
		AbstractBaseGraph<INode, ExtDWE> graph = this.usProblem.getGraph();
		Ranger rangerObj = new Ranger(this.usProblem);
		rangerObj.setTarget(this.usProblem.getTargetNodesSet());
		rangerObj.setSource(this.usProblem.getSource());
		rangerObj.loadProblem();

		Map<ExtDWE, Double> rangerCoverage = rangerObj.getRangerCoverage();
		for (ExtDWE e : graph.edgeSet()) {
			this.usProblem.getGraph().setEdgeWeight(e, rangerCoverage.get(e));
		}

		List<AdversaryPath> lstAP = new ArrayList<AdversaryPath>();
		lstAP = new ArrayList<AdversaryPath>();
		for (INode target : this.usProblem.getTargetNodesSet()) {
			lstAP.add(new AdversaryPath(target, DijkstraShortestPath
					.findPathBetween(this.usProblem.getGraph(),
							this.usProblem.getSource(), target)));
		}

		for (ExtDWE e : graph.edgeSet()) {
			this.usProblem.getGraph().setEdgeWeight(e, 0.0);
		}

		return lstAP;
	}

	/*
	public List<DefenderAllocation> getWarmStartDefenderRanger() {
		// Ranger buckets
		Ranger rangerObj = new Ranger(this.usProblem);
		rangerObj.setTarget(this.usProblem.getTargetNodesSet());
		rangerObj.setSource(this.usProblem.getSource());
		rangerObj.loadProblem();
		// rangerObj.writeProb("RANGER-now");
		try {
			rangerObj.solve();
		} catch (LPSolverException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();
		lstDA = rangerObj.getDefenderAllocations();

		return lstDA;
	}
	*/
	/*
	ArrayList<ArrayList<ExtDWE>> minfrontier = new ArrayList<ArrayList<ExtDWE>>();
	
	public List<ExtDWE> mincutSet(int k, ExtDWE e, DefenderAllocation da){
		List<ExtDWE> maxpath = new ArrayList<ExtDWE>();
		for( Iterator<ExtDWE> edgeIter = this.usProblem.nextE(e).iterator(); edgeIter.hasNext();){
			ArrayList<ExtDWE> path = new ArrayList<ExtDWE>();
			ExtDWE curEdge = edgeIter.next();
			if(curEdge.getType() == EDGE_TYPE.NORMAL){
			if(da.contains(curEdge)){
				if(!edgeIter.hasNext()){
					path.add(curEdge);
					minfrontier.add(path);
				}
			}else{
				path.add(curEdge);
				minfrontier.add(path);
			}
		}
		}
		for( Iterator<ExtDWE> edgeIter = this.usProblem.prevE(e).iterator(); edgeIter.hasNext();){
			ArrayList<ExtDWE> path = new ArrayList<ExtDWE>();
			ExtDWE curEdge = edgeIter.next();
			if(curEdge.getType() == EDGE_TYPE.NORMAL){
			if(da.contains(curEdge)){
				if(!edgeIter.hasNext()){
					path.add(0,curEdge);
					minfrontier.add(path);
				}
			}else{
				path.add(0,curEdge);
				minfrontier.add(path);
			}
		}
		}
		while(minfrontier.size()>0){
			ArrayList<ExtDWE> path = minfrontier.remove(0);
			
			if(path.size()==k){
				minfrontier.clear();
				return path;
			}else if(path.size()>maxpath.size()){
				maxpath=path;
			}
			minSet(k,path, da);
		}
		minfrontier.clear();
		return maxpath;
	}
	public void minSet(int k, List<ExtDWE> path, DefenderAllocation da){
	
		
		for( Iterator<ExtDWE> edgeIter = this.usProblem.nextE(path.get(path.size()-1)).iterator(); edgeIter.hasNext();){
			ArrayList<ExtDWE> path2 = new ArrayList<ExtDWE>();
			path2.addAll(path);
			ExtDWE curEdge = edgeIter.next();
			if(curEdge != null && curEdge.getType() == EDGE_TYPE.NORMAL){
				if(da.contains(curEdge)){
					if(!edgeIter.hasNext()){
						path2.add(curEdge);
						minfrontier.add(path2);
					}
				}else{
					path2.add(curEdge);
					minfrontier.add(path2);
				}
			}
		}
		for( Iterator<ExtDWE> edgeIter = this.usProblem.prevE(path.get(0)).iterator(); edgeIter.hasNext();){
			ArrayList<ExtDWE> path2 = new ArrayList<ExtDWE>();
			path2.addAll(path);
			ExtDWE curEdge = edgeIter.next();
			if(curEdge != null && curEdge.getType() == EDGE_TYPE.NORMAL){
				if(da.contains(curEdge)){
					if(!edgeIter.hasNext()){
						path2.add(0,curEdge);
						minfrontier.add(path2);
					}
				}else{
					path2.add(0,curEdge);
					minfrontier.add(path2);
				}
			}
		}
	}
	*/
	public List<DefenderAllocation> getWarmStartDefenderMinCutEdges(int count) {
		List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();
		Set<ExtDWE> edgeSet = new HashSet<ExtDWE>();

		MinCut minCutObj = new MinCut(this.usProblem);

		List<INode> targetSet = new ArrayList<INode>();
		int maxWeight = 0;
		INode maxWeightTarget = null;
		for (INode target : this.usProblem.getTargetNodesSet()) {
			if (maxWeightTarget == null || maxWeight < target.getPayoff()) {
				maxWeight = target.getPayoff();
				maxWeightTarget = target;
			}
		}
		targetSet.add(maxWeightTarget);

		for (INode targetNode : targetSet) {
			minCutObj.setTarget(targetNode);

			minCutObj.resetLP();

			// minCutObj.writeProb("MinCutProb");

			Set<ExtDWE> minCutSet = minCutObj.getMinCut();
			edgeSet.addAll(minCutSet);

			for (ExtDWE edge : minCutSet) {
				edgeSet.addAll(DijkstraShortestPath.findPathBetween(
						this.usProblem.getGraph(), this.usProblem.getSource(),
						this.usProblem.getGraph().getEdgeSource(edge)));

				edgeSet.addAll(DijkstraShortestPath.findPathBetween(
						this.usProblem.getGraph(), this.usProblem.getGraph()
								.getEdgeTarget(edge), targetNode));
			}

		}

		while (lstDA.size() < count) {
			DefenderAllocation da = new DefenderAllocation();
			List<ExtDWE> lstEdges = new ArrayList<ExtDWE>(edgeSet);
			Collections.shuffle(lstEdges, model.Configuration.randomGenerator);
			
			for(int i=0;i<this.usProblem.getNumResourceTypes();i++){
				int k = (int) this.usProblem.ResourceCoverage.get(i).doubleValue();
				for(int j=0;j<this.usProblem.getNumDefenderResources().get(i);j++){
					
					ExtDWE Edge = null;
					if(lstEdges.size()>0){
						for(ExtDWE e : lstEdges){
							if(e.getType()!=EDGE_TYPE.VIRTUAL && !da.contains(e)){
								Edge = e;
								break;
							}
						}
						lstEdges.remove(Edge);

					}else{
						
						List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
						Collections.shuffle(edgeList);
						
						Edge = edgeList.remove(0);
						
						while(Edge.getType()==EDGE_TYPE.VIRTUAL && da.contains(Edge)){
							Edge=edgeList.remove(0);
						}		
					}
					List<ExtDWE> coverage = this.usProblem.getRSet(Edge,k);
						for( ExtDWE e : coverage){
							da.addEdgeToAllocation(e);
							da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
							if(lstEdges.contains(e)){ lstEdges.remove(e);}
						}
				}
			}
			
			
			
			while (da.size() < this.usProblem.getTotalCoverage()
					&& lstEdges.size() > 0) {
				da.addEdgeToAllocation(lstEdges.remove(0));
			}
			
			lstDA.add(da);
		}

		return lstDA;
	}
	public void distributeResources(DefenderAllocation da, List<ExtDWE> lstEdges){
		for(int i=0; i < this.usProblem.getNumResourceTypes();i++){
			for(int j =0;j<this.usProblem.getNumDefenderResources().get(i);j++){
				da.addEdgeToAllocation(lstEdges.get(0));
				for(int k=1;k<this.usProblem.ResourceCoverage.get(i);k++){
					
					Set<ExtDWE> next = this.usProblem.nextE(lstEdges.remove(0));	//need to finish
					for(ExtDWE n : next){
						if(lstEdges.contains(n)){
							da.addEdgeToAllocation(n);
							lstEdges.remove(n);
						}
					}
				}
			}
		}
	}
	
	
	
	public void discardActionsNotInSupport() {
		Map<DefenderAllocation, Double> defStrategyMap = this.clp
				.getDefenderStrategyAsMap();
		for (DefenderAllocation da : defStrategyMap.keySet()) {
			if (defStrategyMap.get(da) < Configuration.EPSILON) {
				this.usProblem.getActProfile().deleteDefenderAllocation(da);
			}
		}

		Map<AdversaryPath, Double> advStrategyMap = this.clp
				.getAttackerStrategyAsMap();
		for (AdversaryPath ap : advStrategyMap.keySet()) {
			if (advStrategyMap.get(ap) < Configuration.EPSILON) {
				this.usProblem.getActProfile().deleteAdversaryPath(ap);
			}
		}

		this.clp.resetLP();
		this.brDef.resetLP();
		this.brAdv.resetLP();
	}

	private void computeMinCut() {
		if (this.minCutEdges != null) { // && !this.minCutEdges.isEmpty()) {
			return;
		}
		if (this.minCutEdges == null) {
			this.minCutEdges = new HashSet<ExtDWE>();
		}

		List<INode> targetSet = this.usProblem.getTargetNodesSet();
		MinCut minCutObj = new MinCut(this.usProblem);

		for (INode targetNode : targetSet) {
			minCutObj.setTarget(targetNode);

			minCutObj.resetLP();

			Set<ExtDWE> minCutSet = minCutObj.getMinCut();
			this.minCutEdges.addAll(minCutSet);
		}
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	//~~~~~~~~~~~BETTER DEFENDER RESPONSE ~~~~~~~~~~~~~~~~~~~//
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	
	public Pair<DefenderAllocation, Double> getBetterDefenderResponseNP1() {
		List<DefenderAllocation> defAllocations = this.getDefenderAllocations();
		List<AdversaryPath> advPaths = this.getAdversaryPaths();
		
		Map<AdversaryPath, Double> advStrategy = this.clp.getAttackerStrategyAsMap();
		Map<DefenderAllocation, Double> defStrategy = this.clp.getDefenderStrategyAsMap();

		Map<INode, Set<AdversaryPath>> remaining = new HashMap<INode, Set<AdversaryPath>>();
		Map<INode, Double> caughtProb = new HashMap<INode, Double>();
		Map<INode, ExtDWE> predecessor = new HashMap<INode, ExtDWE>();
		Map<INode, PQNode> mapping = new HashMap<INode, PQNode>();

		for (INode u : this.usProblem.getGraph().vertexSet()) {
			if (u.getType() == NODE_TYPE.SOURCE) {
				caughtProb.put(u, 0.0);
				remaining.put(u,
						new HashSet<AdversaryPath>(advPaths));
			} else {
				caughtProb.put(u, Double.POSITIVE_INFINITY);
			}
		}

		Set<INode> visited = new HashSet<INode>();
		Set<INode> addedToPQ = new HashSet<INode>();

		PQNode srcPQ = new PQNode(this.usProblem.getSource(), 0.0);
		mapping.put(this.usProblem.getSource(), srcPQ);

		PriorityQueue<PQNode> pq = new PriorityQueue<PQNode>();
		pq.offer(srcPQ);
		addedToPQ.add(this.usProblem.getSource());

		int numTargetsCovered = 0;
		int numTargets = this.usProblem.getTargetNodesSet().size();

		// System.out.println("Set targets: " +
		// this.usProblem.getTargetNodesSet());

		while (!pq.isEmpty()) {
			PQNode pq_u = pq.poll();
			INode u = pq_u.getN();

			// System.out.println("Dequeing: " + u);

			visited.add(u);
			if (u.getType() == NODE_TYPE.TARGET) {
				// System.out.println("Target reached: " + u);
				numTargetsCovered++;
			}

			if (numTargetsCovered == numTargets) {
				// System.out.println("Breaking since all targets reached.");
				break;
			}

			// System.out.println("At node: " + u);

			for (ExtDWE e : this.usProblem.getGraph().outgoingEdgesOf(u)) {
				INode v = this.usProblem.getto(e);
				// System.out.println("considering: " + u + ", " + e + ", " +
				// v);

				if (visited.contains(v)) {
					continue;
				}
				Double uvWeight = 0.001;
				Set<AdversaryPath> apRemaining = new HashSet<AdversaryPath>();
				for (AdversaryPath ap : remaining.get(u)) {
					if (ap.isInPath(e) && advStrategy.get(ap) > 0) {
						uvWeight += advStrategy.get(ap);
					} else {
						apRemaining.add(ap);
					}
				}
				boolean valueChanged = false;
				double caught_v = caughtProb.get(u) + uvWeight;
				if (caught_v > caughtProb.get(v)) {
					caughtProb.put(v, caught_v);
					predecessor.put(v, e);
					valueChanged = true;
					// update remaining of node v
					remaining.put(v, apRemaining);
				} else {
					caught_v = caughtProb.get(v);
				}

				if (valueChanged && addedToPQ.contains(v)) {
					pq.remove(mapping.get(v));
					// System.out.println("removing from PQ: " + v);
					addedToPQ.remove(v);
				}
				if (!addedToPQ.contains(v)) {
					PQNode pq_v = new PQNode(v, caught_v);
					// System.out.println("adding to PQ: " + v);
					pq.offer(pq_v);
					mapping.put(v, pq_v);
					addedToPQ.add(v);
				}

				// System.out.println("e: " + e);
				// System.out.println("cp: " + caughtProb);
				// System.out.println("rem: " + remaining);
				// System.out.println("pred: " + predecessor);
			}
		}

		// find the best path
		INode targetToAttack = null;
		double maxPayoff = Double.NEGATIVE_INFINITY;
		double advPayoff;
		for (INode t : this.usProblem.getTargetNodesSet()) {
			advPayoff = (1 - caughtProb.get(t)) * t.getPayoff();
			if (advPayoff > maxPayoff) {
				maxPayoff = advPayoff;
				targetToAttack = t;
			}
		}

		// get path
		INode source = this.usProblem.getSource();

		AdversaryPath apBRPath = new AdversaryPath();
		apBRPath.setTarget(targetToAttack);

		INode temp = targetToAttack;
		while (temp != source) {
			if (predecessor.get(temp) == null) {
				System.out.println("OH NO!! I will crash now. :(");
			}
			ExtDWE e = predecessor.get(temp);
			apBRPath.addEdgeToPath(e);
			temp = this.usProblem.getfrom(e); 
		}

		// System.out.println("path: " + apBRPath);

		Pair<DefenderAllocation, Double> returnPair = new Pair<DefenderAllocation, Double>();
		return returnPair;
	}

	public Pair<DefenderAllocation, Double> getBetterDefenderResponseNP() {
	// greedy computation for defender computation
			Set<ExtDWE> mincut = this.minCutEdges;
			
			Map<AdversaryPath, Double> advStrategy = this.clp.getAttackerStrategyAsMap();

			// for each edge, have a weighted parameter storing the prob of
			// path*weight of target for every path it intercepts
			Map<ExtDWE, Double> mapEdges = new HashMap<ExtDWE, Double>();

			Set<AdversaryPath> apNotCoveredYet = new HashSet<AdversaryPath>();
			for (AdversaryPath ap : advStrategy.keySet()) {
				//if (advStrategy.get(ap) > Configuration.EPSILON) {
					apNotCoveredYet.add(ap);
				//}
			}

			DefenderAllocation da = new DefenderAllocation();

			//while (da.size() < this.usProblem.getTotalCoverage() && apNotCoveredYet.size() > 0) {
			for(int i=0;i<this.usProblem.getNumResourceTypes();i++){
				int k = (int) this.usProblem.ResourceCoverage.get(i).doubleValue();
				
				for(int j=0;j<this.usProblem.getNumDefenderResources().get(i);j++){
					if(apNotCoveredYet.size() > 0){
						mapEdges.clear();

						// set weights for each edge
						for (AdversaryPath ap : apNotCoveredYet) {
							for (ExtDWE e : ap.getPath()) {
								if (e.getType() == EDGE_TYPE.VIRTUAL) {
									continue;
								}
								
								Double d = advStrategy.get(ap) * ap.getTargetPayoff();
								Double d2 = 0.0;
								if (mapEdges.containsKey(e)) {
									d2 = mapEdges.get(e);
								}
								mapEdges.put(e, d + d2);
							}
						}
						
						// find edge with max weight
						double maxWeight = Double.NEGATIVE_INFINITY;
						ExtDWE maxWeightEdge = null;

						for (ExtDWE edge : mapEdges.keySet()) {
							if (mapEdges.get(edge) > maxWeight
									&& edge.getType() == EDGE_TYPE.NORMAL) {
								maxWeightEdge = edge;
								maxWeight = mapEdges.get(edge);
							}
						}
						//GraphSearch search = new GraphSearch(this.usProblem.getGraph());
						// now add edges to DefenderAllocation
						//List<ExtDWE> coverage = this.usProblem.getSet2(maxWeightEdge,k, mapEdges);
						List<ExtDWE> coverage = this.usProblem.getSet(maxWeightEdge,k, mapEdges);
						if(!this.usProblem.isValidPath(coverage)){
							System.out.print("notvalid");
						}
						if(coverage.size()<k){
							//System.out.print("hey");
						}
								//search.findPath(k, maxWeightEdge);
								//this.usProblem.getRandom(maxWeightEdge,k);
								//this.usProblem.getSet(maxWeightEdge,k, mapEdges);
						for( ExtDWE e : coverage){
							da.addEdgeToAllocation(e);
							da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
						}
						
						// now remove paths from apNotCoveredYet which have edge in them
						Set<AdversaryPath> markPathsToRemove = new HashSet<AdversaryPath>();
						for (AdversaryPath ap : apNotCoveredYet) {
							for( ExtDWE e : coverage){
								if (ap.isInPath(e)) {
									markPathsToRemove.add(ap);
								}
							}
						}
						
						apNotCoveredYet.removeAll(markPathsToRemove);
						
						
					}else{
						
						if(this.minCutEdges==null || mincut==null){
							this.computeMinCut();
							mincut = this.minCutEdges;
						}
						
						ExtDWE Edge = null;
						if(!mincut.isEmpty()){
							for(ExtDWE e : mincut){
								if(e.getType()!=EDGE_TYPE.VIRTUAL && !da.contains(e)){
									Edge = e;
									break;
								}
							}
							mincut.remove(Edge);
							
						}
						if(Edge==null){
							List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
							Collections.shuffle(edgeList);
							
							Edge = edgeList.remove(0);
							
							while(Edge.getType()==EDGE_TYPE.VIRTUAL && da.contains(Edge)){
								Edge=edgeList.remove(0);
							}
						}
						
						if(Edge==null){
							System.out.println("ohno!");
						}
						List<ExtDWE> coverage = this.usProblem.getRSet(Edge,k);
						
						//GraphSearch search = new GraphSearch(this.usProblem.getGraph());
						//List<ExtDWE> coverage = this.usProblem.getUnWeightedSet(k, da);
						//List<ExtDWE> coverage = this.usProblem.getRandomSet2(edge, k, da);
						//search.findPath(k, edge);
						/*
						while(coverage.size()<k && !edgeList.isEmpty()){
							edge = edgeList.remove(0);
							while(edge.getType()==EDGE_TYPE.VIRTUAL){
								edge=edgeList.remove(0);
							}
							//coverage = search.findPath(k, edge);
							//coverage = this.usProblem.getRandomSet2(edge, k, da);
							
						}*/
								//this.usProblem.getRandom(edge, k);
								//this.usProblem.getUnWeightedSet(k, da);
						
						if(!this.usProblem.isValidPath(coverage)){
							System.out.print("notvalid2");
						}
						if(coverage.size()<k){
							System.out.print("hey2");
						}
						for( ExtDWE e : coverage){
							da.addEdgeToAllocation(e);
							da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
						}
					}
			}

		
			}

			// now all the edges that will be added will not increase
			// defender-payoff in the next round, but try to find best
			// "potential edges" to add.

			/*
			if (da.size() < this.usProblem.getNumDefenderResources()) {
				this.computeMinCut();
				for (ExtDWE e : this.minCutEdges) {
					da.addEdgeToAllocation(e);
					if (da.size() == this.usProblem.getNumDefenderResources()) {
						break;
					}
				}
			}*/


			double defPayoff = getdefPayoffForAllocation(da);
			Pair<DefenderAllocation, Double> returnPair = new Pair<DefenderAllocation, Double>();
			returnPair.setKey(da);
			returnPair.setValue(defPayoff);

			System.gc();

			return returnPair;
		}
	
	public Pair<DefenderAllocation, Double> getBetterDefenderResponsetestNP() {
		
		DefenderAllocation da = new DefenderAllocation();
		
		for(int i=0;i<this.usProblem.getNumResourceTypes();i++){
			int k = (int) this.usProblem.ResourceCoverage.get(i).doubleValue();
			
			for(int j=0;j<this.usProblem.getNumDefenderResources().get(i);j++){
				List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
				Collections.shuffle(edgeList);
				ExtDWE edge = edgeList.remove(0);
				
				while(edge.getType()==EDGE_TYPE.VIRTUAL && da.contains(edge)){
					edge=edgeList.remove(0);
				}
				
				GraphSearch search = new GraphSearch(this.usProblem.getGraph());
				List<ExtDWE> coverage = search.findPath(k, edge);
				while(coverage.size()<k && !edgeList.isEmpty()){
					edge = edgeList.remove(0);
					while(edge.getType()==EDGE_TYPE.VIRTUAL){
						edge=edgeList.remove(0);
					}
					coverage = search.findPath(k, edge);
					
				}
						//this.usProblem.getRandom(edge, k);
						//this.usProblem.getUnWeightedSet(k, da);
				for( ExtDWE e : coverage){
					da.addEdgeToAllocation(e);
					da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
				}
			}
		}
		double defPayoff = getPayoffForAllocation(da);
		Pair<DefenderAllocation, Double> returnPair = new Pair<DefenderAllocation, Double>();
		returnPair.setKey(da);
		returnPair.setValue(defPayoff);

		System.gc();

		return returnPair;
	}
	public Pair<DefenderAllocation, Double> getBetterDefenderResponsenop() {
		// greedy computation for defender computation
		Map<AdversaryPath, Double> advStrategy = this.clp
				.getAttackerStrategyAsMap();

		// for each edge, have a weighted parameter storing the prob of
		// path*weight of target for every path it intercepts
		Map<ExtDWE, Double> mapEdges = new HashMap<ExtDWE, Double>();

		Set<AdversaryPath> apNotCoveredYet = new HashSet<AdversaryPath>();
		for (AdversaryPath ap : advStrategy.keySet()) {
			if (advStrategy.get(ap) > Configuration.EPSILON) {
				apNotCoveredYet.add(ap);
			}
		}

		DefenderAllocation da = new DefenderAllocation();

		while (da.size() < this.usProblem.getTotalCoverage()
				&& apNotCoveredYet.size() > 0) {
		for(int i=0;i<this.usProblem.getNumResourceTypes();i++){
			int k = (int) this.usProblem.ResourceCoverage.get(i).doubleValue();
			
			for(int j=0;j<this.usProblem.getNumDefenderResources().get(i);j++){
				if(apNotCoveredYet.size() > 0){
					mapEdges.clear();

					// set weights for each edge
					for (AdversaryPath ap : apNotCoveredYet) {
						for (ExtDWE e : ap.getPath()) {
							if (e.getType() == EDGE_TYPE.VIRTUAL) {
								continue;
							}
							Double d = advStrategy.get(ap) * ap.getTargetPayoff();
							Double d2 = 0.0;
							if (mapEdges.containsKey(e)) {
								d2 = mapEdges.get(e);
							}
							mapEdges.put(e, d + d2);
						}
					}
					// find edge with max weight
					double maxWeight = Double.NEGATIVE_INFINITY;
					ExtDWE maxWeightEdge = null;

					for (ExtDWE edge : mapEdges.keySet()) {
						if (mapEdges.get(edge) > maxWeight
								&& edge.getType() == EDGE_TYPE.NORMAL) {
							maxWeightEdge = edge;
							maxWeight = mapEdges.get(edge);
						}
					}
					// now add edges to DefenderAllocation
					List<ExtDWE> coverage = this.usProblem.getSet(maxWeightEdge,k, mapEdges);
					for( ExtDWE e : coverage){
						da.addEdgeToAllocation(e);
						da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
						da.addProbability(e,this.usProblem.CoverageProbability.get(i));
					}
					
					// now remove paths from apNotCoveredYet which have edge in them
					Set<AdversaryPath> markPathsToRemove = new HashSet<AdversaryPath>();
					for (AdversaryPath ap : apNotCoveredYet) {
						for( ExtDWE e : coverage){
							if (ap.isInPath(e)) {
								markPathsToRemove.add(ap);
							}
						}
					}
					apNotCoveredYet.removeAll(markPathsToRemove);	
				}
			}
		}
		}
		double defPayoff = getPayoffForAllocation(da);
		Pair<DefenderAllocation, Double> returnPair = new Pair<DefenderAllocation, Double>();
		returnPair.setKey(da);
		returnPair.setValue(defPayoff);

		System.gc();

		return returnPair;
	}
	
	public Pair<DefenderAllocation, Double> getBetterDefenderResponsenn() {
		// greedy computation for defender computation
		Map<AdversaryPath, Double> advStrategy = this.clp.getAttackerStrategyAsMap();
		Map<DefenderAllocation, Double> defStrategy = this.clp.getDefenderStrategyAsMap();

		
		Set<AdversaryPath> apNotCoveredYet = new HashSet<AdversaryPath>();
		for (AdversaryPath ap : advStrategy.keySet()) {
			if (advStrategy.get(ap) > Configuration.EPSILON) {
				apNotCoveredYet.add(ap);
			}
		}
		
		// for each edge, have a weighted parameter storing the prob of
		// path*weight of target for every path it intercepts
		Map<ExtDWE, Double> mapEdges = new HashMap<ExtDWE, Double>();

		DefenderAllocation da = new DefenderAllocation();

		while(da.allocationSize() < this.usProblem.getTotalResources()){			

		for(int i=0;i<this.usProblem.getNumResourceTypes();i++){
			//get coverage of current resource type
			int k = (int) this.usProblem.ResourceCoverage.get(i).doubleValue();
			
			//for each resource of that type
			for(int j=0;j<this.usProblem.getNumDefenderResources().get(i);j++){
				mapEdges.clear();

					// set weights for each edge
					for (AdversaryPath ap : apNotCoveredYet) {
						for (ExtDWE e : ap.getPath()) {
							if (e.getType() == EDGE_TYPE.VIRTUAL) {
								continue;
							}
							
							Double d = advStrategy.get(ap) * ap.getTargetPayoff();
							Double pnew = 1-(1-da.getProbability(e))*(1-this.usProblem.CoverageProbability.get(i));
							Double pold = da.getProbability(e);
							Double prev  = (pnew-pold)*d;
							
							if(mapEdges.containsKey(e)){
								prev = prev + mapEdges.get(e);
							}
							mapEdges.put(e,  prev);
						}
					}
					// find edge with max weight gain
					double maxWeight = Double.NEGATIVE_INFINITY;
					ExtDWE maxWeightEdge = null;

					for (ExtDWE edge : mapEdges.keySet()) {
						if (mapEdges.get(edge) > maxWeight
								&& edge.getType() == EDGE_TYPE.NORMAL) {
							maxWeightEdge = edge;
							maxWeight = mapEdges.get(edge);
						}
					}
					// now add edges to DefenderAllocation
					List<ExtDWE> coverage = this.usProblem.getSet(maxWeightEdge,k, mapEdges);
					for( ExtDWE e : coverage){
						da.addEdgeToAllocation(e);
						da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
						da.addProbability(e,this.usProblem.CoverageProbability.get(i));
					}
					
					/*
					// now remove paths from apNotCoveredYet which have edge in them
					Set<AdversaryPath> markPathsToRemove = new HashSet<AdversaryPath>();
					for (AdversaryPath ap : apNotCoveredYet) {
						for( ExtDWE e : coverage){
							if (ap.isInPath(e)) {
								markPathsToRemove.add(ap);
							}
						}
					}
					apNotCoveredYet.removeAll(markPathsToRemove);
					
					*/
				}
			}
		}
		double defPayoff = getPayoffForAllocation(da);
		Pair<DefenderAllocation, Double> returnPair = new Pair<DefenderAllocation, Double>();
		returnPair.setKey(da);
		returnPair.setValue(defPayoff);

		System.gc();

		return returnPair;
	}
	
	public Pair<DefenderAllocation, Double> getBetterDefenderResponse() {
		// greedy computation for defender computation
		Set<ExtDWE> mincut = this.minCutEdges;
		Map<AdversaryPath, Double> advStrategy = this.clp.getAttackerStrategyAsMap();
		Map<DefenderAllocation, Double> defStrategy = this.clp.getDefenderStrategyAsMap();

		// for each edge, have a weighted parameter storing the prob of
		// path*weight of target for every path it intercepts
		Map<ExtDWE, Double> mapEdges = new HashMap<ExtDWE, Double>();
		Map<ExtDWE, Double> mapEdges2 = new HashMap<ExtDWE, Double>();
		
		Set<AdversaryPath> apNotCoveredYet = new HashSet<AdversaryPath>();
		for (AdversaryPath ap : advStrategy.keySet()) {
			if (advStrategy.get(ap) > Configuration.EPSILON) {
				apNotCoveredYet.add(ap);
			}
		}
		
		DefenderAllocation da = new DefenderAllocation();
		
		//while(da.allocationSize() < this.usProblem.getTotalResources()){			
		//int test = da.allocationSize();		
		for(int i=0;i<this.usProblem.getNumResourceTypes();i++){
			//get coverage of current resource type
			int k = (int) this.usProblem.ResourceCoverage.get(i).doubleValue();
			double prob = this.usProblem.CoverageProbability.get(i);
			
			//for each resource of that type
			//double test3 = this.usProblem.getNumDefenderResources().get(i);
			for(int j=0;j<this.usProblem.getNumDefenderResources().get(i);j++){
				if(apNotCoveredYet.size() > 0){	
				mapEdges.clear();
				mapEdges2.clear();

					// set weights for each edge
					for (AdversaryPath ap : apNotCoveredYet) {
						for (ExtDWE e : ap.getPath()) {
							if (e.getType() == EDGE_TYPE.VIRTUAL) {
								continue;
							}
							
							if(prob == 2.0){
								Double d = advStrategy.get(ap) * ap.getTargetPayoff();
								Double d2 = 0.0;
								if (mapEdges.containsKey(e)) {
									d2 = mapEdges.get(e);
								}
								mapEdges.put(e, d + d2);
							}else{
								Double dq = advStrategy.get(ap) * ap.getTargetPayoff();
								//Double pnew = 1-(1-da.getProbability(e))*(1-this.usProblem.CoverageProbability.get(i));
								Double pold = da.getProbability(e);
								//Double prev  = (pnew-pold)*d;
								//double pold=0;
								for(DefenderAllocation allocation : defStrategy.keySet()){
									pold += allocation.getProbability(e)*defStrategy.get(allocation);
								}
								Double prev = (1-pold)*prob*dq;
								
								if(mapEdges2.containsKey(e)){
									prev = prev + mapEdges2.get(e);
								}
								mapEdges2.put(e,  prev);
							}
						}
					}
					// find edge with max weight gain
					double maxWeight = Double.NEGATIVE_INFINITY;
					ExtDWE maxWeightEdge = null;

					for (ExtDWE edge : mapEdges2.keySet()) {
						if (mapEdges2.get(edge) > maxWeight
								&& edge.getType() == EDGE_TYPE.NORMAL) {
							maxWeightEdge = edge;
							maxWeight = mapEdges2.get(edge);
						}
					}
					// now add edges to DefenderAllocation
					List<ExtDWE> coverage = this.usProblem.getSet(maxWeightEdge,k, mapEdges2);
					for( ExtDWE e : coverage){
						if(!da.contains(e))	{
							da.addEdgeToAllocation(e);
						}
						da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
						//da.addProbability(e,this.usProblem.CoverageProbability.get(i));
						da.addResourcetoEdge(e, i);
					}
					
					
					// now remove paths from apNotCoveredYet which have edge in them
					Set<AdversaryPath> markPathsToRemove = new HashSet<AdversaryPath>();
					for (AdversaryPath ap : apNotCoveredYet) {
						for( ExtDWE e : coverage){
							if (ap.isInPath(e) && prob == 1.0) {
								markPathsToRemove.add(ap);
							}
						}
					}
					apNotCoveredYet.removeAll(markPathsToRemove);
					
				}else{
					// fill the rest of the allocation with random edges
					// randomly add edges to da
					//this.usProblem.getUnWeightedSet(k);
					/*
					List<ExtDWE> coverage = this.usProblem.getUnWeightedSet(k, da);
					for( ExtDWE e : coverage){
						da.addEdgeToAllocation(e);
						da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
					}
					*/

					if(this.minCutEdges==null || mincut==null){
						this.computeMinCut();
						mincut = this.minCutEdges;
					}
					
					ExtDWE Edge = null;
					if(!mincut.isEmpty()){
						for(ExtDWE e : mincut){
							if(e.getType()!=EDGE_TYPE.VIRTUAL && !da.contains(e)){
								Edge = e;
								break;
							}
						}
						mincut.remove(Edge);
						
					}
					if(Edge==null){
						List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
						Collections.shuffle(edgeList);
						
						Edge = edgeList.remove(0);
						
						while(Edge.getType()==EDGE_TYPE.VIRTUAL && da.contains(Edge)){
							Edge=edgeList.remove(0);
						}
					}
					
					
					List<ExtDWE> coverage = this.usProblem.getRSet(Edge,k);

					if(!this.usProblem.isValidPath(coverage)){
						System.out.print("notvalid2");
					}
					if(coverage.size()<k){
						System.out.print("hey2");
					}
					for( ExtDWE e : coverage){
						da.addEdgeToAllocation(e);
						da.addEdgetoResource(e,"Resource"+(i+1)+","+(j+1));
					}
				}
				}
			}
		//}
		double defPayoff = getPayoffForAllocation(da);
		Pair<DefenderAllocation, Double> returnPair = new Pair<DefenderAllocation, Double>();
		returnPair.setKey(da);
		returnPair.setValue(defPayoff);

		System.gc();

		return returnPair;
	}
	/*
	
	public Pair<DefenderAllocation, Double> getBetterDefenderResponseOld() {
		// This function adds k edges by doing the computation once
		// Map<ExtDWE, Double> attEdgeUsage = this.clp.getAttackersEdgeUsage();
		Map<AdversaryPath, Double> advStrategy = this.clp
				.getAttackerStrategyAsMap();

		// for each edge, have a weighted parameter storing the prob of
		// path*weight of target for every path it intercepts
		Map<ExtDWE, Double> mapEdges = new HashMap<ExtDWE, Double>();
		Map<ExtDWE, Set<AdversaryPath>> mapEdgesToPath = new HashMap<ExtDWE, Set<AdversaryPath>>();

		for (AdversaryPath ap : advStrategy.keySet()) {
			if (advStrategy.get(ap) <= Configuration.EPSILON) {
				continue;
			}
			for (ExtDWE e : ap.getPath()) {
				if (!mapEdgesToPath.containsKey(e)) {
					mapEdgesToPath.put(e, new HashSet<AdversaryPath>());
				}

				mapEdgesToPath.get(e).add(ap);

				Double d = advStrategy.get(ap) * ap.getTargetPayoff();
				Double d2 = 0.0;
				if (mapEdges.containsKey(e)) {
					d2 = mapEdges.get(e);
				}
				mapEdges.put(e, d + d2);
			}
		}

		// TODO:
		// multiple prob with target weight
		List<Entry<ExtDWE, Double>> list = new LinkedList<Entry<ExtDWE, Double>>(
				mapEdges.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<ExtDWE, Double>>() {
			@Override
			public int compare(Entry<ExtDWE, Double> arg0,
					Entry<ExtDWE, Double> arg1) {
				if (arg0.getValue() > arg1.getValue()) {
					return -1;
				} else
					return 1;
			}
		});

		// choose the top k edges used by the attacker
		Set<AdversaryPath> pathsAlreadyCovered = new HashSet<AdversaryPath>();
		DefenderAllocation da = new DefenderAllocation();
		int count = 0;
		for (Iterator<Entry<ExtDWE, Double>> it = list.iterator(); it.hasNext();) {
			if (count == this.usProblem.getNumDefenderResources()) {
				break;
			}
			Map.Entry<ExtDWE, Double> entry = (Map.Entry<ExtDWE, Double>) it
					.next();

			ExtDWE addingEdge = entry.getKey();
			if (addingEdge.getType() == EDGE_TYPE.VIRTUAL) {
				continue;
			}

			boolean addingEdgeHasValue = false;

			for (AdversaryPath ap : mapEdgesToPath.get(addingEdge)) {
				if (!pathsAlreadyCovered.contains(ap)) {
					addingEdgeHasValue = true;
					break;
				}
			}

			if (addingEdgeHasValue) {
				da.addEdgeToAllocation(addingEdge);
				pathsAlreadyCovered.addAll(mapEdgesToPath.get(addingEdge));
				count++;
			}
		}

		if (da.size() < this.usProblem.getNumDefenderResources()) {
			List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem
					.getGraph().edgeSet());
			Collections.shuffle(edgeList, model.Configuration.randomGenerator);
			int index = 0;
			while (da.size() < this.usProblem.getNumDefenderResources()) {
				// get and add random edge
				if (index == this.usProblem.getGraph().edgeSet().size()) {
					break;
				}
				if (edgeList.get(index).getType() == EDGE_TYPE.VIRTUAL) {
					index++;
					continue;
				}
				da.addEdgeToAllocation(edgeList.get(index));
				index++;
			}
		}

		double defPayoff = getPayoffForAllocation(da);

		Pair<DefenderAllocation, Double> returnPair = new Pair<DefenderAllocation, Double>();
		returnPair.setKey(da);
		returnPair.setValue(defPayoff);

		// if ( da.size() < this.usProblem.getNumDefenderResources() ) {
		// System.out.println("DA size less than resources.");
		// System.out.flush();
		// }

		return returnPair;
	}
*/
	
	//~~~~~~~~~~~~~~~~~ADVERSARY PAYOFF~~~~~~~~~~~~~~~~~~~~~//

	
	public double getPayoffForAllocation(DefenderAllocation da) {
		Map<AdversaryPath, Double> advStrategy = this.clp
				.getAttackerStrategyAsMap();

		double defPayoff = 0.0;
		for (AdversaryPath ap : this.usProblem.getActProfile()
				.getAdversaryPaths()) {
			boolean intersect = false;
			double pij = this.usProblem.getActProfile().computeProbability(da, ap);
			defPayoff -= (advStrategy.get(ap)*(1-pij)*ap.getTargetPayoff());

		}

		return defPayoff;
	}	
	
	public double getdefPayoffForAllocation(DefenderAllocation da) {
		Map<AdversaryPath, Double> advStrategy = this.clp
				.getAttackerStrategyAsMap();

		double defPayoff = 0.0;
		for (AdversaryPath ap : this.usProblem.getActProfile()
				.getAdversaryPaths()) {
			boolean intersect = false;
			for (ExtDWE e : da.getAllocation()) {
				if (ap.isInPath(e)) {
					intersect = true;
					break;
				}
			}

			if (intersect == false) {
				defPayoff -= (advStrategy.get(ap) * ap.getTargetPayoff());
			}
		}

		return defPayoff;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	//~~~~~~~~~~~BETTER ATTACKER RESPONSE ~~~~~~~~~~~~~~~~~~~//
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	
	public Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> getBetterAttackerResponses() {
		List<DefenderAllocation> defAllocations = this.getDefenderAllocations();
		Map<DefenderAllocation, Double> defStrategy = this.clp
				.getDefenderStrategyAsMap();

		Map<INode, Set<DefenderAllocation>> remaining = new HashMap<INode, Set<DefenderAllocation>>();
		Map<INode, Double> caughtProb = new HashMap<INode, Double>();
		Map<INode, ExtDWE> predecessor = new HashMap<INode, ExtDWE>();
		Map<INode, PQNode> mapping = new HashMap<INode, PQNode>();

		// System.out.println("Def strategy: " + defStrategy);

		// System.gc();

		for (INode u : this.usProblem.getGraph().vertexSet()) {
			if (u.getType() == NODE_TYPE.SOURCE) {
				caughtProb.put(u, 0.0);
				remaining.put(u,
						new HashSet<DefenderAllocation>(defAllocations));
			} else {
				caughtProb.put(u, Double.POSITIVE_INFINITY);
			}
		}

		Set<INode> visited = new HashSet<INode>();
		Set<INode> addedToPQ = new HashSet<INode>();

		PQNode srcPQ = new PQNode(this.usProblem.getSource(), 0.0);
		mapping.put(this.usProblem.getSource(), srcPQ);

		PriorityQueue<PQNode> pq = new PriorityQueue<PQNode>();
		pq.offer(srcPQ);
		addedToPQ.add(this.usProblem.getSource());

		int numTargetsCovered = 0;
		int numTargets = this.usProblem.getTargetNodesSet().size();

		// System.out.println("Set targets: " +
		// this.usProblem.getTargetNodesSet());

		while (!pq.isEmpty()) {
			PQNode pq_u = pq.poll();
			INode u = pq_u.getN();

			// System.out.println("Dequeing: " + u);

			visited.add(u);
			if (u.getType() == NODE_TYPE.TARGET) {
				// System.out.println("Target reached: " + u);
				numTargetsCovered++;
			}

			if (numTargetsCovered == numTargets) {
				// System.out.println("Breaking since all targets reached.");
				break;
			}

			// System.out.println("At node: " + u);

			for (ExtDWE e : this.usProblem.getGraph().outgoingEdgesOf(u)) {
				INode v = this.usProblem.getto(e);
				// System.out.println("considering: " + u + ", " + e + ", " +
				// v);

				if (visited.contains(v)) {
					continue;
				}
				Double uvWeight = 0.001;
				Set<DefenderAllocation> daRemaining = new HashSet<DefenderAllocation>();
				for (DefenderAllocation da : remaining.get(u)) {
					if (da.contains(e) && defStrategy.get(da) > 0) {
						uvWeight += defStrategy.get(da);
					} else {
						daRemaining.add(da);
					}
				}
				boolean valueChanged = false;
				double caught_v = caughtProb.get(u) + uvWeight;
				if (caught_v < caughtProb.get(v)) {
					caughtProb.put(v, caught_v);
					predecessor.put(v, e);
					valueChanged = true;
					// update remaining of node v
					remaining.put(v, daRemaining);
				} else {
					caught_v = caughtProb.get(v);
				}

				if (valueChanged && addedToPQ.contains(v)) {
					pq.remove(mapping.get(v));
					// System.out.println("removing from PQ: " + v);
					addedToPQ.remove(v);
				}
				if (!addedToPQ.contains(v)) {
					PQNode pq_v = new PQNode(v, caught_v);
					// System.out.println("adding to PQ: " + v);
					pq.offer(pq_v);
					mapping.put(v, pq_v);
					addedToPQ.add(v);
				}

				// System.out.println("e: " + e);
				// System.out.println("cp: " + caughtProb);
				// System.out.println("rem: " + remaining);
				// System.out.println("pred: " + predecessor);
			}
		}

		// find the best path
		INode targetToAttack = null;
		double maxPayoff = Double.NEGATIVE_INFINITY;
		double advPayoff;
		for (INode t : this.usProblem.getTargetNodesSet()) {
			advPayoff = (1 - caughtProb.get(t)) * t.getPayoff();
			if (advPayoff > maxPayoff) {
				maxPayoff = advPayoff;
				targetToAttack = t;
			}
		}

		// get path
		INode source = this.usProblem.getSource();

		AdversaryPath apBRPath = new AdversaryPath();
		apBRPath.setTarget(targetToAttack);

		INode temp = targetToAttack;
		while (temp != source) {
			if (predecessor.get(temp) == null) {
				System.out.println("OH NO!! I will crash now. :(");
			}
			ExtDWE e = predecessor.get(temp);
			apBRPath.addEdgeToPath(e);
			temp = this.usProblem.getfrom(e); 
		}

		// System.out.println("path: " + apBRPath);

		Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> returnPair = new Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>>(
				null, new Pair<AdversaryPath, Double>(apBRPath, maxPayoff));
		return returnPair;
	}

	public Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> getBetterAttackerResponsesOld() {
		Map<ExtDWE, Double> defCoverage = this.clp.getDefenderCoverage();
		Map<DefenderAllocation, Double> defStrategy = this.clp
				.getDefenderStrategyAsMap();

		for (ExtDWE e : this.usProblem.getGraph().edgeSet()) {
			if (defCoverage.containsKey(e) && defCoverage.get(e) > 0) {
				this.usProblem.getGraph().setEdgeWeight(e, defCoverage.get(e));
			} else {
				this.usProblem.getGraph().setEdgeWeight(e, 0.0);
			}
		}

		// choose the shortest path to all targets
		List<AdversaryPath> lstAP = new ArrayList<AdversaryPath>();
		for (INode target : this.usProblem.getTargetNodesSet()) {
			lstAP.add(new AdversaryPath(target, DijkstraShortestPath
					.findPathBetween(this.usProblem.getGraph(),
							this.usProblem.getSource(), target)));

			// Manish
			// System.out.println("Source: " + this.usProblem.getSource());
			// System.out.println("Target: " + target);
			// System.out.println("Shortest Path: " + DijkstraShortestPath
			// .findPathBetween(this.usProblem.getGraph(),
			// this.usProblem.getSource(), target));
			// System.out.println("lstAPPrint: " + lstAP);
		}

		// now get the best target / value pair
		double bestAdvPayoff = Double.NEGATIVE_INFINITY;
		AdversaryPath bestAdvPath = null;
		for (AdversaryPath ap : lstAP) {

			// compute advPayoff for this ap
			double advPayoff = 0.0;
			for (DefenderAllocation da : this.usProblem.getActProfile()
					.getDefenderAllocations()) {
				boolean intersect = false;
				for (ExtDWE e : da.getAllocation()) {
					if (ap.isInPath(e)) {
						intersect = true;
					}
				}
				if (intersect == false) {
					advPayoff += (ap.getTargetPayoff() * defStrategy.get(da));
				}
			}

			// System.out.println("1. AdvPayoff: " + advPayoff + ", " +
			// bestAdvPayoff);

			if (advPayoff > bestAdvPayoff) {
				bestAdvPayoff = advPayoff;
				bestAdvPath = ap;

				// System.out.println("2. AdvPayoff: " + advPayoff + ", " +
				// bestAdvPayoff + ", " + bestAdvPath);
				// System.out.println(ap);
			}

		}

		Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> returnPair = new Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>>(
				lstAP, new Pair<AdversaryPath, Double>(bestAdvPath,
						bestAdvPayoff));

		// System.out.println("BETTER ADV RETURN");
		// System.out.println(returnPair);

		return returnPair;
	}

	public void betterResponsesRun() throws LPSolverException,
			MalformedGraphException {

		double gameValueCheck;

		boolean allocationAdded, pathAdded;
		DefenderAllocation da = null;
		AdversaryPath ap = null;

		long stTime = System.currentTimeMillis();

		while (true) {
			numWarmStartConvergeIterations++;

			gameValueCheck = this.gameValue;
			double defenderPayoff = Double.MIN_VALUE;

			long stTime1 = 0;
			// System.out.println("ITERATION : "+ iterationNo);

			stTime1 = System.currentTimeMillis();
			Pair<DefenderAllocation, Double> betterDefResponse;
			Pair<DefenderAllocation, Double> betterDefResponse2;
			if(this.usProblem.isProbability){
				betterDefResponse = getBetterDefenderResponse();
				//betterDefResponse2 = getBetterDefenderResponseNP();
				//if(!betterDefResponse.equals(betterDefResponse2)){
				//	int i =0;
				//}
			}else{
				betterDefResponse = getBetterDefenderResponseNP();
			}
			long betterRDefTime = System.currentTimeMillis() - stTime1;

			da = betterDefResponse.getKey();
			defenderPayoff = betterDefResponse.getValue();

			measure.betterDefenderOracleTimes.add(betterRDefTime);
			measure.defenderBetterRVal.add(defenderPayoff);

			allocationAdded = this.usProblem.getActProfile()
					.addDefenderAllocation(da);

			this.clp.updateDefenderAllocations();

			this.clp.solve();
			this.gameValue = this.clp.getLPObjective();

			// List<Double> defStrategy = this.clp.getDefenderStrategy();

			double adversaryPayoff = Double.MIN_VALUE;

			stTime1 = System.currentTimeMillis();
			Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> betterAdvResponse = getBetterAttackerResponses();
			long betterRAdvTime = System.currentTimeMillis() - stTime1;

			ap = betterAdvResponse.getValue().getKey();
			adversaryPayoff = betterAdvResponse.getValue().getValue();
			pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap);

			measure.betterAdversaryOracleTimes.add(betterRAdvTime);
			measure.adversaryBetterRVal.add(adversaryPayoff);

			double diffPercentage = Math.abs(gameValueCheck / 100 / 10); // 0.1%
			// if both best responses were run then
			if (maxWarmStartConvergeIterations != -1
					&& numWarmStartConvergeIterations > maxWarmStartConvergeIterations) {
				break;
			}

			if ((Math.abs(defenderPayoff - gameValueCheck) <= diffPercentage && Math
					.abs(-adversaryPayoff - gameValueCheck) <= diffPercentage)
					|| (allocationAdded == false && pathAdded == false)) {
				// if (allocationAdded == false && pathAdded == false) {
				// if (Math.abs(defenderPayoff - gameValueCheck) <=
				// diffPercentage
				// && Math.abs(-adversaryPayoff - gameValueCheck) <=
				// diffPercentage) {

				measure.warmStartTime = System.currentTimeMillis() - stTime;
				measure.warmStartIterations = numWarmStartConvergeIterations;

				break;
				// }
			}

			this.clp.updateAdversaryPaths();

			this.clp.solve();
			this.gameValue = this.clp.getLPObjective();

			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp
					.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp
					.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfDefenderAllocations());

			if (numWarmStartConvergeIterations % 100 == 0) {
				System.out.println("BetterResponseConverge Iteartion No: "
						+ numWarmStartConvergeIterations + "; Time: "
						+ (System.currentTimeMillis() - stTime)
						+ "; GameValue: " + gameValue);
			}

		}

		long brTime = System.currentTimeMillis() - stTime;

		System.out.println("BetterResponseConverge GameValue: "
				+ this.gameValue);
		System.out.println("BetterResponseConverge Size of matrix: "
				+ this.getDefenderAllocations().size() + " x "
				+ this.getAdversaryPaths().size());
		System.out.println("BetterResponseConverge Load Time for problems: "
				+ this.getLoadTime() / 1000.0);
		this.printRunTimeBreakup();
		System.out.println("BetterResponseConverge Average Path Length: "
				+ this.getAveragePathLength());
		System.out.println("BetterResponseConverge Run Time: "
				+ (brTime * 1.0 / 1000));
		System.out.println("BetterResponseConverge Number of Iterations: "
				+ numWarmStartConvergeIterations);
		System.out.println("BetterResponseConverge Total Time: "
				+ ((this.getLoadTime() + brTime) / 1000.00));

	}

	private AbstractBaseGraph<INode, ExtDWE> addNodesAndEdgesFromEdgeSet(
			Set<ExtDWE> edgeSet) {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		for (ExtDWE e : edgeSet) {
			INode u = this.usProblem.getGraph().getEdgeSource(e);
			INode v = this.usProblem.getGraph().getEdgeTarget(e);
			if (!graph.containsVertex(u)) {
				graph.addVertex(u);
			}
			if (!graph.containsVertex(v)) {
				graph.addVertex(v);
			}
			ExtDWE eAdded = graph.addEdge(u, v);
			eAdded.setType(e.getType());
		}
		return graph;
	}

	public AbstractBaseGraph<INode, ExtDWE> getShortestPathSkeleton() {
		// compute skeleton
		KShortestPaths<INode, ExtDWE> kspObj = new KShortestPaths<INode, ExtDWE>(
				this.usProblem.getGraph(), this.usProblem.getSource(),
				this.kShortestPaths);

		// DijkstraShortestPath<INode, ExtDWE>
		Set<ExtDWE> edgeSet = new HashSet<ExtDWE>();

		for (INode target : this.usProblem.getTargetNodesSet()) {
			List<GraphPath<INode, ExtDWE>> lstPaths = kspObj.getPaths(target);
			for (GraphPath<INode, ExtDWE> gp : lstPaths) {
				edgeSet.addAll(gp.getEdgeList());
			}
		}

		return this.addNodesAndEdgesFromEdgeSet(edgeSet);
	}

	public AbstractBaseGraph<INode, ExtDWE> getMinCutSkeleton() {
		Set<ExtDWE> edgeSet = new HashSet<ExtDWE>();

		// EdmondsKarpMaximumFlow<V, E>

		// List<INode> targetSet = this.usProblem.getTargetNodesSet();

		MinCut minCutObj = new MinCut(this.usProblem);

		List<INode> targetSet = new ArrayList<INode>();
		int maxWeight = 0;
		INode maxWeightTarget = null;
		for (INode target : this.usProblem.getTargetNodesSet()) {
			if (maxWeightTarget == null || maxWeight < target.getPayoff()) {
				maxWeight = target.getPayoff();
				maxWeightTarget = target;
			}
		}
		targetSet.add(maxWeightTarget);

		for (INode targetNode : targetSet) {
			minCutObj.setTarget(targetNode);

			minCutObj.resetLP();

			// minCutObj.writeProb("MinCutProb");

			Set<ExtDWE> minCutSet = minCutObj.getMinCut();
			edgeSet.addAll(minCutSet);

			// minCutObj.writeSol("MinCutSol");

			// find sources of these edges and find shortest path from source to
			// these nodes
			// find targets of these edges and find shortest path from these
			// nodes to target
			for (ExtDWE edge : minCutSet) {
				edgeSet.addAll(DijkstraShortestPath.findPathBetween(
						this.usProblem.getGraph(), this.usProblem.getSource(),
						this.usProblem.getGraph().getEdgeSource(edge)));

				edgeSet.addAll(DijkstraShortestPath.findPathBetween(
						this.usProblem.getGraph(), this.usProblem.getGraph()
								.getEdgeTarget(edge), targetNode));
			}

		}

		// for (ExtDWE edge : this.usProblem.getGraph().edgesOf(
		// this.usProblem.getSource())) {
		// edgeSet.add(edge);
		// }

		return this.addNodesAndEdgesFromEdgeSet(edgeSet);
	}

	/*
	public void warmStartUsingSkeleton() {

		List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();
		List<AdversaryPath> lstAP = new ArrayList<AdversaryPath>();

		int MAX_SAMPLES_FOR_DEFENDER = 5;

		MinCut minCutObj = new MinCut(this.usProblem);
		minCutObj.setSource(this.usProblem.getSource());

		Set<ExtDWE> edgeSet = this.usProblem.getGraph().edgeSet();
		
		List<INode> targetSetToSolveMincut = null;
		
		// targetSetToSolveMincut = this.usProblem.getTargetNodesSet();

		// max weight target only
		INode maxWeightTarget = null;
		int maxTargetPayoff = Integer.MIN_VALUE;
		for (INode t : this.usProblem.getTargetNodesSet()) {
			if (t.getPayoff() > maxTargetPayoff) {
				maxTargetPayoff = t.getPayoff();
				maxWeightTarget = t;
			}
		}
		targetSetToSolveMincut = new ArrayList<INode>();
		targetSetToSolveMincut.add(maxWeightTarget);

		for (INode target : targetSetToSolveMincut) {

			minCutObj.setTarget(target);
			minCutObj.resetLP();

			Set<ExtDWE> minCutEdges = minCutObj.getMinCut();

			// generate pure strategies for the defender
			int numDefResources = this.usProblem.getNumDefenderResources();
			if (minCutEdges.size() < numDefResources) {
				DefenderAllocation da = new DefenderAllocation(minCutEdges);

				List<ExtDWE> lstEdges = new ArrayList<ExtDWE>(this.usProblem
						.getGraph().edgeSet());
				Collections.shuffle(lstEdges,
						model.Configuration.randomGenerator);

				while (da.size() < numDefResources && lstEdges.size() > 0) {
					da.addEdgeToAllocation(lstEdges.remove(0));
				}
				lstDA.add(da);
			} else {
				List<ExtDWE> minCutEdgesList = new ArrayList<ExtDWE>(
						minCutEdges);
				while (lstDA.size() < MAX_SAMPLES_FOR_DEFENDER) {
					Collections.shuffle(minCutEdgesList,
							model.Configuration.randomGenerator);
					DefenderAllocation da = new DefenderAllocation(
							minCutEdgesList.subList(0, numDefResources));
					lstDA.add(da);
				}
			}

			// get pure strategies for the attacker
			
			for (DefenderAllocation da : lstDA) {
				for (ExtDWE e : edgeSet) {
					this.usProblem.getGraph().setEdgeWeight(e, 1.0);
					if (da.contains(e)) {
						this.usProblem.getGraph().setEdgeWeight(e,
								edgeSet.size() + 1);
					}
				}

				lstAP.add(new AdversaryPath(target, DijkstraShortestPath
						.findPathBetween(this.usProblem.getGraph(),
								this.usProblem.getSource(), target)));
			}
		}

		List<DefenderAllocation> daList = this.getWarmStartDefenderRanger();
		lstDA.addAll(daList);

		for (DefenderAllocation da : daList) {
			for (ExtDWE e : edgeSet) {
				this.usProblem.getGraph().setEdgeWeight(e, 1.0);
				if (da.contains(e)) {
					this.usProblem.getGraph().setEdgeWeight(e,
							edgeSet.size() + 1);
				}
			}
			for (INode target : targetSetToSolveMincut) { //this.usProblem.getTargetNodesSet()) {
				lstAP.add(new AdversaryPath(target, DijkstraShortestPath
						.findPathBetween(this.usProblem.getGraph(),
								this.usProblem.getSource(), target)));
			}
		}

		// this.warmStart(daList, null);

		// generate pure strategies for the attacker

		this.warmStart(lstDA, lstAP);

	}

	public void warmStartUsingSkeletonOld() throws LPSolverException {
		// AbstractBaseGraph<INode, ExtDWE> graph = getShortestPathSkeleton();
		long stTimeSkeleton = System.currentTimeMillis();

		AbstractBaseGraph<INode, ExtDWE> graph = getMinCutSkeleton();
		// AbstractBaseGraph<INode, ExtDWE> graph = this.usProblem.getGraph();

		long ftTimeSkeleton = System.currentTimeMillis();
		System.out.println("Skeleton generation time: "
				+ (ftTimeSkeleton - stTimeSkeleton));
		System.out.flush();

		// System.out.println("Skeleton: " + graph);

		UrbanSecProblem uspObj = new UrbanSecProblem();
		uspObj.setGraph(graph);
		uspObj.setNumDefenderResources(this.usProblem.getNumDefenderResources());

		// System.out.println(graph);

		RuggedBetterResponses ruggedObj = new RuggedBetterResponses(uspObj);
		ruggedObj.setEnableSkeleton(false, true, true);
		ruggedObj.finalConvergenceEpsilon = 0.001;
		// ruggedObj.setEnableWarmStartConverge(true);
		ruggedObj
				.setEnableBetterDefenderOracle(this.skeletonBetterDefenderOracle);
		ruggedObj
				.setEnableBetterAttackerOracle(this.skeletonBetterAttackerOracle);

		System.out.println("Going to start solving Skeleton...");
		System.out.flush();
		stTimeSkeleton = System.currentTimeMillis();

		try {
			ruggedObj.run();
		} catch (MalformedGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ftTimeSkeleton = System.currentTimeMillis();
		System.out.println("Skeleton solved in: "
				+ (ftTimeSkeleton - stTimeSkeleton) + " :milliseconds");
		System.out.flush();

		// System.out.println("Skeleton Graph: " +
		// ruggedObj.usProblem.getGraph());

		List<DefenderAllocation> lstDA = ruggedObj.getDefenderAllocations();
		List<AdversaryPath> lstAP = ruggedObj.getAdversaryPaths();

		// System.out.println("-- Skeleton DA: " + lstDA + ", "
		// + ruggedObj.getDefenderStrategy());
		// System.out.println("-- Skeleton AP: " + lstAP + ", "
		// + ruggedObj.getAdversaryStrategy());

		List<DefenderAllocation> lstDArugged = new ArrayList<DefenderAllocation>();
		List<AdversaryPath> lstAPrugged = new ArrayList<AdversaryPath>();

		List<Double> ruggedDefStrategy = ruggedObj.getDefenderStrategy();
		List<Double> ruggedAdvStrategy = ruggedObj.getAdversaryStrategy();

		for (int index = 0; index < ruggedDefStrategy.size(); index++) {
			if (ruggedDefStrategy.get(index) > 0.00001) {
				DefenderAllocation da = lstDA.get(index);
				DefenderAllocation daNew = new DefenderAllocation();
				for (ExtDWE fakeEdge : da.getAllocation()) {
					ExtDWE trueEdge = this.getTrueEdge(graph, fakeEdge);
					daNew.addEdgeToAllocation(trueEdge);
					// MANISH DEBUG
					// System.out.println("FE: " + fakeEdge + ", TE: " +
					// trueEdge);
				}
				lstDArugged.add(daNew);
			}
		}

		for (int index = 0; index < ruggedAdvStrategy.size(); index++) {
			if (ruggedAdvStrategy.get(index) > 0.00001) {
				AdversaryPath ap = lstAP.get(index);
				AdversaryPath apNew = new AdversaryPath();
				for (ExtDWE fakeEdge : ap.getPath()) {
					apNew.addEdgeToPath(this.getTrueEdge(graph, fakeEdge));
				}
				apNew.setTarget(ap.getTarget());
				lstAPrugged.add(apNew);
			}
		}

		System.out.println("Skeleton DefenderPayoff: "
				+ ruggedObj.getGameValue());
		System.out.println("Skeleton Size of matrix: "
				+ ruggedObj.getDefenderAllocations().size() + " x "
				+ ruggedObj.getAdversaryPaths().size());
		System.out.println("Skeleton Load Time for problems: "
				+ ruggedObj.getLoadTime() / 1000.0);
		ruggedObj.printRunTimeBreakup();
		System.out.println("Skeleton Average Path Length: "
				+ ruggedObj.getAveragePathLength());
		System.out.println("Skeleton Run Time: "
				+ (ruggedObj.getRunTime() * 1.0 / 1000));
		System.out.println("Skeleton Number of Iterations: "
				+ ruggedObj.getNumberOfIterations());
		System.out.println("Skeleton Total Time: "
				+ (ruggedObj.getTotalTime() / 1000.00));

		System.out.flush();

		// System.out.println("-- WarmStart DA: " + lstDArugged);
		// System.out.println("-- WarmStart AP: " + lstAPrugged);

		this.warmStart(lstDArugged, lstAPrugged);

		List<DefenderAllocation> daList = this.getWarmStartDefenderRanger();
		this.warmStart(daList, null);

		System.out.println("Skeleton size: "
				+ (lstDArugged.size() + daList.size()) + ", " + lstAPrugged);
	}
*/
	
	private ExtDWE getTrueEdge(AbstractBaseGraph<INode, ExtDWE> fakeGraph,
			ExtDWE fakeEdge) {
		for (ExtDWE e : this.usProblem.getGraph().edgeSet()) {
			INode fakeEdgeSource = (INode) fakeGraph.getEdgeSource(fakeEdge);
			INode fakeEdgeTarget = (INode) fakeGraph.getEdgeTarget(fakeEdge);

			if (this.usProblem.getGraph().getEdgeSource(e)
					.equals(fakeEdgeSource)
					&& this.usProblem.getGraph().getEdgeTarget(e)
							.equals(fakeEdgeTarget)) {
				return e;
			}
		}

		throw new RuntimeException("Couldn't find matching edge in true graph.");
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~MAIN SOLVER HERE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

	DefenderAllocation aacurrentDa;
	AdversaryPath aacuurentPath;
	
	List<Double> aaDefStrat;
	List<Double> aaAdvStrat;
	
	List<DefenderAllocation> aaDefAll;
	List<AdversaryPath> aaAdvAll;
	
	public boolean heuristic = true;
	public int optimalAdvIter = 0;
	public void run() throws LPSolverException, MalformedGraphException {
		
		totalBRDefTime = 0;
		totalBRAdvTime = 0;
		totalCLTime = 0;
		long timeVar;

		int ITERATION_COUNT_FOR_PRINT = 100;

		long curTime = System.currentTimeMillis();
		//System.out.print("Loading BR Problems ... ");
		this.loadBestResponseProblems();
		this.loadTime = System.currentTimeMillis() - curTime;
		//System.out.println("done in " + (loadTime) + " milliseconds.");
		measure.loadTime = loadTime;
		curTime = System.currentTimeMillis();
		double gameValueCheck;
		
		
		/*
		if (this.enableSkeleton == true) {
			System.out.println("Going to compute the skeleton and solve it.");
			System.out.flush();
			long skStTime = System.currentTimeMillis();
			this.warmStartUsingSkeleton();
			System.out.println("Skeleton solved in: "
					+ (System.currentTimeMillis() - skStTime));
			System.out.flush();
		}*/

		//System.out.println("Initializing brDef ... ");
		System.out.flush();
		timeVar = System.currentTimeMillis();

		DefenderAllocation da = null;
		boolean allocationAdded = false;
		
		
		//WARM START THE DEFENDER
		if (this.usProblem.getActProfile().getNumberOfDefenderAllocations() == 0) {
			this.brDef.writeProb("DAAR");
			this.brDef.solve();
			totalBRDefTime += this.brDef.getRunTime();
			da = this.brDef.getDefenderAllocation();
			allocationAdded = this.usProblem.getActProfile()
					.addDefenderAllocation(da); // allocationAdded = false if da
												// already generated before

			//System.out.println("Done with brDef init in: "
			//		+ (System.currentTimeMillis() - timeVar));
			System.out.flush();
			this.brAdv.updateDefenderAllocations();
		}
		
		List<AdversaryPath> lstAP = null;
		AdversaryPath ap = null;
		boolean pathAdded = false;
		
		//WARM START THE ATTACKER
		if (this.usProblem.getActProfile().getNumberOfAdversaryPaths() == 0) {
			System.out.flush();
			timeVar = System.currentTimeMillis();
			this.brAdv.updateDefenderAllocations();
			this.brAdv.solve();
			totalBRAdvTime += this.brAdv.getRunTime();
			ap = this.brAdv.getAdversaryPath();
			pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap);
			System.out.flush();
			this.brDef.updateAdversaryPaths();
		}

		this.loadCLProblem();
		this.loadTime += this.clp.getLoadTime();
		
		this.clp.updateAdversaryPaths();
		this.clp.updateDefenderAllocations();
		
		//SOLVING THE CLP
		System.out.flush();
		timeVar = System.currentTimeMillis();
		this.clp.solve();
		totalCLTime += this.clp.getRunTime();
		System.out.flush();

		//SETTING THE STRATEGIES
		this.brAdv.setDefenderStrategy(this.clp.getDefenderStrategy());
	//	this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());

		aacurrentDa =da;
		aacuurentPath = ap;
		aaDefStrat=this.clp.getDefenderStrategy();
		aaAdvStrat=this.clp.getAdversaryStrategy();
		aaDefAll = this.usProblem.getActProfile().getDefenderAllocations();
		aaAdvAll = this.usProblem.getActProfile().getAdversaryPaths();
		
		if (onlyWarmStartConverge == true) {
			System.out.println("Only warm starting needs to be done ... ");
			System.out.flush();
			long wmStTime = System.currentTimeMillis();
			this.betterResponsesRun();
			this.runTime = System.currentTimeMillis() - curTime;
			System.out.println("Done with warm starting in: "
					+ (System.currentTimeMillis() - wmStTime));
			System.out.flush();
			return;
		}

		if (enableWarmStartConverge == true) {
			System.out.println("Warm starting needs to be done ... ");
			System.out.flush();
			long wmStTime = System.currentTimeMillis();
			this.betterResponsesRun();
			this.brDef.updateAdversaryPaths();
			this.brAdv.updateDefenderAllocations();
			this.clp.updateDefenderAllocations();
			this.clp.updateAdversaryPaths();
			this.clp.solve();
			totalCLTime += this.clp.getRunTime();
			this.brAdv.setDefenderStrategy(this.clp.getDefenderStrategy());
			this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());
			System.out.println("Done with warm starting in: "
					+ (System.currentTimeMillis() - wmStTime));
			System.out.flush();
		}

		int localNumIterationsBeforeDiscarding = 0;

		//System.out.println("Starting main loop ...");
		System.out.flush();
		timeVar = System.currentTimeMillis();

		boolean printWhenGapLessThanTen = true;
		boolean printWhenGapLessThanFive = true;
		boolean printWhenGapLessThanThree = true;
		boolean printWhenGapLessThanTwo = true;
		boolean printWhenGapLessThanOne = true;
		boolean printWhenGapLessThanPointOne = true;
		boolean printWhenGapLessThanPointOOne = true;

		int wsDefStrat = this.usProblem.getActProfile()
				.getNumberOfDefenderAllocations();
		int wsAdvStrat = this.usProblem.getActProfile()
				.getNumberOfAdversaryPaths();

		//System.out.println("Warm Start Sizes in Rugged: " + wsDefStrat + ", "
		//		+ wsAdvStrat);

		int countBestResponseAdvRun = 0;
		int MAX_BEST_RESPONSE_RUN = 10; //Integer.MAX_VALUE;
		boolean flagBetterResponseRun = true;
			
		
		//BEGINING OF LOOP
		while (true) {
			iterationNo++;
			localNumIterationsBeforeDiscarding++;

			gameValueCheck = this.gameValue;
			double defenderPayoff = Double.MIN_VALUE;
			long stTime = 0;

			if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0)
				System.out.println("ITERATION : " + iterationNo);

			
		//BETTER DEFENDER ORACLE
			if (enableBetterDefenderOracle == true) {
				stTime = System.currentTimeMillis();
				Pair<DefenderAllocation, Double> betterDefResponse;
				Pair<DefenderAllocation, Double> betterDefResponse2;
				if(this.usProblem.isProbability){
					betterDefResponse = getBetterDefenderResponse();
					//betterDefResponse2 = getBetterDefenderResponseNP();
					//if(!betterDefResponse.equals(betterDefResponse2)){
					//	int i =0;
					//}
				}else{
					betterDefResponse = getBetterDefenderResponseNP();
				}
				long betterRDefTime = System.currentTimeMillis() - stTime;
				// System.out.println("Better Def response done in " +
				// betterRDefTime + " milliseconds.");
				betterdeforaclecount++;
				da = betterDefResponse.getKey();
				defenderPayoff = betterDefResponse.getValue();

				// MANISH DEBUG
				// System.out.println("6." + da);

				measure.betterDefenderOracleTimes.add(betterRDefTime);
				measure.defenderBetterRVal.add(defenderPayoff);

				// MANISH - CHANGE HERE
				if (defenderPayoff > this.gameValue) {
					// + this.betterResponseEpsilon) {
					allocationAdded = this.usProblem.getActProfile()
							.addDefenderAllocation(da);
				} else {
					allocationAdded = false;
				}

			}
			
		//DEFENDER ORACLE
			if (enableBetterDefenderOracle == false || allocationAdded == false
					|| defenderPayoff < this.clp.getLPObjective()
			 + Configuration.MIP_TOLERANCE		//DO NOT UNCOMMENT
			) {
				// need to run best response

				

				List<Double> sr = this.clp.getAdversaryStrategy();
				this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());
				
				// System.out.print("Getting best def response ... ");
				//this.brDef.writeProb("DAar1"+iterationNo);
				this.brDef.solve();
				//this.brDef.writeProb("DAar2"+iterationNo);
				//this.brDef.writeSol("file");
				
				totalBRDefTime += this.brDef.getRunTime();
				deforaclecount++;
				
				da = this.brDef.getDefenderAllocation();
				defenderPayoff = this.brDef.getDefenderPayoff();

				if (bestDefPayoffFound > defenderPayoff) {
					bestDefPayoffFound = defenderPayoff;
				}

				// // MANISH
				// if (defenderPayoff > this.gameValue
				// + this.betterResponseEpsilon) {
				allocationAdded = this.usProblem.getActProfile().addDefenderAllocation(da);
				// } else {
				// allocationAdded = false;
				// }

				measure.defenderOracleTimes.add(this.brDef.getRunTime());
				measure.defenderBRVal.add(defenderPayoff);
			} else {
				measure.defenderOracleTimes.add((long) 0);
				measure.defenderBRVal.add(0.0);
			}

			
		//CLP
			this.clp.updateDefenderAllocations();
			this.brAdv.updateDefenderAllocations();

			// update problem data structures
			// update and solve Core-LP model
			// avoids repetition of the same allocation
			// avoids repetition of the same path

			this.clp.solve();

			this.gameValue = this.clp.getLPObjective();
			totalCLTime += this.clp.getRunTime();
			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp
					.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp
					.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfDefenderAllocations());

			// update BRAdv
			List<Double> defStrategy = this.clp.getDefenderStrategy();
			List<Double> adStrategy = this.clp.getAdversaryStrategy();
			this.brAdv.setDefenderStrategy(defStrategy);

			
			double adversaryPayoff = Double.MIN_VALUE;
			long betterRAdvTime = 0L;
			if(iterationNo > cutoff){
				cutoffUsed=true;
			}
			if (iterationNo <= cutoff && enableBetterAttackerOracle == true
					&& flagBetterResponseRun) {

				stTime = System.currentTimeMillis();
				// System.out.print("Getting better att response ... ");
				Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> betterAdvResponse = getBetterAttackerResponses();
				betterRAdvTime = System.currentTimeMillis() - stTime;

				if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
					System.out.println("BETTER ADV done in " + betterRAdvTime
							+ " milliseconds");
					System.out.flush();
				}

				ap = betterAdvResponse.getValue().getKey();
				adversaryPayoff = betterAdvResponse.getValue().getValue();

				// MANISH DEBUG
				// System.out.println("1." + ap);

				// MANISH HERE
				if (adversaryPayoff > (-gameValue) - .1) { // +
														// this.betterResponseEpsilon))
														// {
					pathAdded = this.usProblem.getActProfile()
							.addAdversaryPath(ap);
					countBestResponseAdvRun = 0;
				} else {
					pathAdded = false;
					countBestResponseAdvRun++;
					if ( countBestResponseAdvRun == MAX_BEST_RESPONSE_RUN)
						flagBetterResponseRun = false;						
				}

				measure.betterAdversaryOracleTimes.add(betterRAdvTime);
				measure.adversaryBetterRVal.add(adversaryPayoff);
			}

			boolean apBestResponseOracleRun = false;

			if (iterationNo <= cutoff && (enableBetterAttackerOracle == false || pathAdded == false
					|| adversaryPayoff < -this.clp.getLPObjective()))
			// + Configuration.MIP_TOLERANCE)
			{
				// solve adversary problem
				// System.out.print("Getting best att response ... ");
				
				if(this.heuristic){
				this.brAdv.solve();
				
				if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
					System.out.println("BEST ADV done in "
							+ this.brAdv.getRunTime() + " milliseconds.");
					System.out.flush();
				}
				totalBRAdvTime += this.brAdv.getRunTime();
				adversaryPayoff = this.brAdv.getAdversaryPayoff();
				
				if ((adversaryPayoff >= (-gameValue) - .1) || !this.usProblem.isProbability){
					lstAP = this.brAdv.getAdversaryPaths();
					ap = this.brAdv.getAdversaryPath();
				
					for(AdversaryPath a: lstAP){
					pathAdded = this.usProblem.getActProfile().addAdversaryPath(a);
						if(pathAdded){
							//pathAdded=true;
							break;
						}
					}
					//pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap);

				}
				}
				if (this.usProblem.isProbability && ((((adversaryPayoff < (-gameValue) - .001) || !pathAdded) && allocationAdded==false) || !this.heuristic)){
					
					System.out.print(optimalAdvIter+1);
					//~~~~~~~~~~~~~~~~~~~~~~~~~//
					this.brAdv.OptimalSolve2(-gameValue);
					this.optimalAdvIter++;
					
					if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
						System.out.println("BEST ADV done in "
								+ this.brAdv.getRunTime() + " milliseconds.");
						System.out.flush();
					}
					totalBRAdvTime += this.brAdv.getRunTime();
					adversaryPayoff = this.brAdv.getAdversaryPayoff();
					
					lstAP = this.brAdv.getAdversaryPaths();
					ap = this.brAdv.getAdversaryPath();
					
					pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap);
				}
				
				
		
				if (bestAdvPayoffFound > adversaryPayoff) {
					bestAdvPayoffFound = adversaryPayoff;
				}

				if ( flagBetterResponseRun == false ) {
					countBestResponseAdvRun --;
					if ( countBestResponseAdvRun == 0)
						flagBetterResponseRun = true;
				}
				
				// get solution
				//if(iterationNo <= cutoff){
				
				// } else {
				// pathAdded = false;
				// }
				

				//boolean added = this.usProblem.getActProfile().addAdversaryPath(ap);
//				for(AdversaryPath a: lstAP){
//					boolean added = this.usProblem.getActProfile().addAdversaryPath(a);
//					if(added){
//						pathAdded=true;
//					}
//				}
				
				apBestResponseOracleRun = true;

				measure.adversaryOracleTimes.add(this.brAdv.getRunTime());
				measure.adversaryBRVal.add(this.brAdv.getAdversaryPayoff());
			} else {
				measure.adversaryOracleTimes.add(0L);
				measure.adversaryBRVal.add(0.0);
			}

			if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
				System.out.println("Iteration No: " + iterationNo
						+ "GV/BRDef/BRAdv: " + this.gameValue + "/"
						+ defenderPayoff + "/" + adversaryPayoff);
				System.out.flush();
			}

			
			aacurrentDa =da;
			aacuurentPath = ap;
			aaDefStrat=this.clp.getDefenderStrategy();
			aaAdvStrat=this.clp.getAdversaryStrategy();
			aaDefAll = this.usProblem.getActProfile().getDefenderAllocations();
			aaAdvAll = this.usProblem.getActProfile().getAdversaryPaths();
			
			double diffPayoffs = defenderPayoff - (-adversaryPayoff);
//			if (diffPayoffs < 10 && printWhenGapLessThanTen) {
//				//System.out.println("GAP10: "
//				//		+ ((long) System.currentTimeMillis() - curTime));
//				printWhenGapLessThanTen = false;
//			}
//			if (diffPayoffs < 5 && printWhenGapLessThanFive) {
//				System.out.println("GAP5: "
//						+ ((long) System.currentTimeMillis() - curTime));
//				printWhenGapLessThanFive = false;
//			}
//			if (diffPayoffs < 3 && printWhenGapLessThanThree) {
//				//System.out.println("GAP3: "
//				//		+ ((long) System.currentTimeMillis() - curTime));
//				printWhenGapLessThanThree = false;
//			}
//			if (diffPayoffs < 2 && printWhenGapLessThanTwo) {
//				//System.out.println("GAP2: "
//				//		+ ((long) System.currentTimeMillis() - curTime));
//				printWhenGapLessThanTwo = false;
//			}
//
//			if (diffPayoffs < 1 && printWhenGapLessThanOne) {
//				System.out.println("GAP1: "
//						+ ((long) System.currentTimeMillis() - curTime));
//				printWhenGapLessThanOne = false;
//			}
//			if (diffPayoffs < 0.10 && printWhenGapLessThanPointOne) {
//				System.out.println("GAP0.1: "
//						+ ((long) System.currentTimeMillis() - curTime));
//				printWhenGapLessThanPointOne = false;
//			}
//			if (diffPayoffs < 0.010 && printWhenGapLessThanPointOOne) {
//				//System.out.println("GAP0.01: "
//				//		+ ((long) System.currentTimeMillis() - curTime));
//				printWhenGapLessThanPointOOne = false;
//			}

			// if both best responses were run then
			if ((Math.abs(defenderPayoff - (-adversaryPayoff)) <= 0.001 && cutoff!=0) //this.finalConvergenceEpsilon
					|| (Math.abs(defenderPayoff - this.gameValue) <= 0.01 && cutoff==0) || (allocationAdded == false && pathAdded == false)) {
				long stopTime = System.currentTimeMillis();
				measure.totalTime = stopTime - curTime;
				measure.iterations = iterationNo;
				//measure.isOK();
				double d = Math.abs(defenderPayoff - this.gameValue);
				// MANISH DEBUG
				// System.out.println("3. Breaking"
				// + this.usProblem.getActProfile()
				// .getNumberOfAdversaryPaths()
				// + ", "
				// + this.usProblem.getActProfile()
				// .getNumberOfDefenderAllocations());

				//System.out.println("WRAPPER Game value: "
				//		+ clp.getLPObjective());
				// measure.write();
				break;
				// }
			}
			// if (allocationAdded == false && pathAdded == false) {
			// if (Math.abs(defenderPayoff - gameValueCheck) <=
			// diffPercentage
			// && Math.abs(-adversaryPayoff - gameValueCheck) <=
			// diffPercentage) {

			// add all paths from better response set
			// List<AdversaryPath> apList = betterAdvResponse.getKey();
			// for ( AdversaryPath apListIter : apList ) {
			// this.usProblem.getActProfile().addAdversaryPath(apListIter); //
			// pathAdded
			// }
			// add all paths generated from bestResponseOracle against all
			// targets
			// if ( apBestResponseOracleRun ) {
			// List<AdversaryPath> apListBR =
			// this.brAdv.getAllTargetAdversaryPaths();
			// for ( AdversaryPath apListIter : apListBR ) {
			// this.usProblem.getActProfile().addAdversaryPath(apListIter); //
			// pathAdded
			// }
			// }

			if (enableDiscardUselessActions == true
					&& localNumIterationsBeforeDiscarding > numRoundsBeforeDiscard) {
				this.discardActionsNotInSupport();
				localNumIterationsBeforeDiscarding = 0;
			}

			// System.out.print("Updating clp ... ");
			this.clp.updateAdversaryPaths();
			// System.out.print("done.\nUpdating BRDef ... ");
			this.brDef.updateAdversaryPaths();
			// System.out.println("done.");

			// System.out.print("Solving clp ... ");
			this.clp.solve();
			// System.out.println("done in " + this.clp.getRunTime() +
			// " milliseconds");

			// MANISH DEBUG
			// System.out.println("4." + this.clp.getDefenderStrategy() + ", "
			// + this.clp.getAdversaryStrategy());
			// System.out.println("4.5. "
			// + this.usProblem.getActProfile()
			// .getNumberOfDefenderAllocations()
			// + ", "
			// + this.usProblem.getActProfile()
			// .getNumberOfAdversaryPaths());

			totalCLTime += this.clp.getRunTime();
			this.gameValue = this.clp.getLPObjective();
			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp
					.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp
					.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfDefenderAllocations());

			List<Double> d = this.clp.getDefenderStrategy();
			
			// update BRDef
			List<Double> advStrategy = this.clp.getAdversaryStrategy();
			// this.brDef.updateAdversaryPaths();
			this.brDef.setAdversaryStrategy(advStrategy);

			// System.out.println("Defender Strategy: " +
			// this.clp.getDefenderStrategy());
			// System.out.println("Attacker Strategy: " +
			// this.clp.getAdversaryStrategy());

			System.gc();

			// if (iterationNo % 250 == 0) {
			// this.writeMeasure();
			// }

			// MANISH DEBUG
			if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
				System.out.println("---------");
			}

			/**
			 * Read Instructions on resetLP if defender allocations / adversary
			 * paths need to be removed. Maintain the order of the calls -- the
			 * set of commands for addDefenderAllocations are called together,
			 * and same for addAdversaryPaths. Their order can be interchanged
			 * but not interleaved.
			 */
		}

		// boolean ok = measure.isOK();
		// if (!ok) {
		// measure.write();
		// System.err
		// .println(" ===== MEASURE NOT OK !!!!! ===================");
		// System.exit(1);
		// }

		//System.out.println("Done with main loop in: "
		//		+ (System.currentTimeMillis() - timeVar));
		System.out.flush();

		this.runTime = System.currentTimeMillis() - curTime;

		List<Double> defStrat = this.getDefenderStrategy();
		double defWSCov = 0.0;
		for (int i = 0; i < wsDefStrat; i++) {
			defWSCov += defStrat.get(i);
		}
		List<Double> advStrat = this.getAdversaryStrategy();
		double advWSCov = 0.0;
		for (int i = 0; i < wsAdvStrat; i++) {
			advWSCov += advStrat.get(i);
		}
		//System.out.println("Def WS Cov: " + defWSCov);
		//System.out.println("Adv WS Cov: " + advWSCov);
		System.out.flush();
	}
	
	public void run1() throws LPSolverException, MalformedGraphException {
		// System.out.println("Wrapper started. Please hold, this may take a while...");
		// System.out.println("RUGGED resources: "
		// + this.usProblem.getNumDefenderResources());
		totalBRDefTime = 0;
		totalBRAdvTime = 0;
		totalCLTime = 0;
		long timeVar;

		int ITERATION_COUNT_FOR_PRINT = 100;

		long curTime = System.currentTimeMillis();
		System.out.print("Loading BR Problems ... ");
		this.loadBestResponseProblems();
		this.loadTime = System.currentTimeMillis() - curTime;
		System.out.println("done in " + (loadTime) + " milliseconds.");
		measure.loadTime = loadTime;
		curTime = System.currentTimeMillis();
		double gameValueCheck;
		
		/*
		if (this.enableSkeleton == true) {
			System.out.println("Going to compute the skeleton and solve it.");
			System.out.flush();
			long skStTime = System.currentTimeMillis();
			this.warmStartUsingSkeleton();
			System.out.println("Skeleton solved in: "
					+ (System.currentTimeMillis() - skStTime));
			System.out.flush();
		}*/

		System.out.println("Initializing brDef ... ");
		System.out.flush();
		timeVar = System.currentTimeMillis();

		DefenderAllocation da = null;
		boolean allocationAdded = false;
		if (this.usProblem.getActProfile().getNumberOfDefenderAllocations() == 0) {
			this.brDef.solve();
			totalBRDefTime += this.brDef.getRunTime();
			da = this.brDef.getDefenderAllocation();
			allocationAdded = this.usProblem.getActProfile()
					.addDefenderAllocation(da); // allocationAdded = false if da
												// already generated before

			System.out.println("Done with brDef init in: "
					+ (System.currentTimeMillis() - timeVar));
			System.out.flush();

			// MANISH DEBUG
			// System.out.println("Init strat def: " + da);

			this.brAdv.updateDefenderAllocations();
		}

		AdversaryPath ap = null;
		boolean pathAdded = false;
		if (this.usProblem.getActProfile().getNumberOfAdversaryPaths() == 0) {
			// System.out.println("Initializing brAdv ... ");
			System.out.flush();
			timeVar = System.currentTimeMillis();
			this.brAdv.solve();
			totalBRAdvTime += this.brAdv.getRunTime();
			ap = this.brAdv.getAdversaryPath();
			pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap);

			System.out.println("Done with brAdv init in: "
					+ (System.currentTimeMillis() - timeVar));
			System.out.flush();

			// MANISH DEBUG
			// System.out.println("Init strat adv: " + ap);

			this.brDef.updateAdversaryPaths();
		}

		this.loadCLProblem();
		this.loadTime += this.clp.getLoadTime();

		// System.out.println("Initializing clp ... ");
		System.out.flush();
		timeVar = System.currentTimeMillis();
		this.clp.solve();
		totalCLTime += this.clp.getRunTime();

		System.out.println("Done with clp init in: "
				+ (System.currentTimeMillis() - timeVar));
		System.out.flush();

		this.brAdv.setDefenderStrategy(this.clp.getDefenderStrategy());
		this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());

		// MANISH DEBUG
		// System.out.println("Init clp strat: " +
		// this.clp.getAdversaryStrategy()
		// + ", " + this.clp.getDefenderStrategy());

		// System.out.println("DA: "+ da);
		// System.out.println("AP: "+ ap);
		// System.out.println("Defender Strategy: " +
		// this.clp.getDefenderStrategy());
		// System.out.println("Attacker Strategy: " +
		// this.clp.getAdversaryStrategy());

		if (onlyWarmStartConverge == true) {
			System.out.println("Only warm starting needs to be done ... ");
			System.out.flush();
			long wmStTime = System.currentTimeMillis();
			this.betterResponsesRun();
			this.runTime = System.currentTimeMillis() - curTime;
			System.out.println("Done with warm starting in: "
					+ (System.currentTimeMillis() - wmStTime));
			System.out.flush();
			return;
		}

		if (enableWarmStartConverge == true) {
			System.out.println("Warm starting needs to be done ... ");
			System.out.flush();
			long wmStTime = System.currentTimeMillis();
			this.betterResponsesRun();
			this.brDef.updateAdversaryPaths();
			this.brAdv.updateDefenderAllocations();
			this.clp.updateDefenderAllocations();
			this.clp.updateAdversaryPaths();
			this.clp.solve();
			totalCLTime += this.clp.getRunTime();
			this.brAdv.setDefenderStrategy(this.clp.getDefenderStrategy());
			this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());
			System.out.println("Done with warm starting in: "
					+ (System.currentTimeMillis() - wmStTime));
			System.out.flush();
		}

		int localNumIterationsBeforeDiscarding = 0;

		System.out.println("Starting main loop ...");
		System.out.flush();
		timeVar = System.currentTimeMillis();

		boolean printWhenGapLessThanTen = true;
		boolean printWhenGapLessThanFive = true;
		boolean printWhenGapLessThanThree = true;
		boolean printWhenGapLessThanTwo = true;
		boolean printWhenGapLessThanOne = true;
		boolean printWhenGapLessThanPointOne = true;
		boolean printWhenGapLessThanPointOOne = true;

		int wsDefStrat = this.usProblem.getActProfile()
				.getNumberOfDefenderAllocations();
		int wsAdvStrat = this.usProblem.getActProfile()
				.getNumberOfAdversaryPaths();

		System.out.println("Warm Start Sizes in Rugged: " + wsDefStrat + ", "
				+ wsAdvStrat);

		int countBestResponseAdvRun = 0;
		int MAX_BEST_RESPONSE_RUN = 10; //Integer.MAX_VALUE;
		boolean flagBetterResponseRun = true;
				
		while (true) {
			iterationNo++;
			localNumIterationsBeforeDiscarding++;

			gameValueCheck = this.gameValue;
			double defenderPayoff = Double.MIN_VALUE;
			long stTime = 0;

			if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0)
				System.out.println("ITERATION : " + iterationNo);

			if (enableBetterDefenderOracle == true) {
				// System.out.print("Getting better def response ... ");
				stTime = System.currentTimeMillis();
				Pair<DefenderAllocation, Double> betterDefResponse = getBetterDefenderResponseNP();
				long betterRDefTime = System.currentTimeMillis() - stTime;
				// System.out.println("Better Def response done in " +
				// betterRDefTime + " milliseconds.");

				da = betterDefResponse.getKey();
				defenderPayoff = betterDefResponse.getValue();

				// MANISH DEBUG
				// System.out.println("6." + da);

				measure.betterDefenderOracleTimes.add(betterRDefTime);
				measure.defenderBetterRVal.add(defenderPayoff);

				// MANISH - CHANGE HERE
				if (defenderPayoff > this.gameValue) {
					// + this.betterResponseEpsilon) {
					allocationAdded = this.usProblem.getActProfile()
							.addDefenderAllocation(da);
				} else {
					allocationAdded = false;
				}

			}

			if (enableBetterDefenderOracle == false || allocationAdded == false
					|| defenderPayoff < this.clp.getLPObjective()
			 + Configuration.MIP_TOLERANCE
			) {
				this.deforaclecount++;
				// need to run best response

				// MANISH DEBUG
				// System.out.println("5.5: " + allocationAdded + ", "
				// + defenderPayoff + ", " + this.clp.getLPObjective());

				// System.out.print("Getting best def response ... ");
				this.brDef.solve();
				totalBRDefTime += this.brDef.getRunTime();
				// System.out.println("done in " + this.brDef.getRunTime() +
				// " milliseconds.");
				// get solution
				da = this.brDef.getDefenderAllocation();

				// MANISH DEBUG
				// System.out.println("5." + da);

				defenderPayoff = this.brDef.getDefenderPayoff();

				if (bestDefPayoffFound > defenderPayoff) {
					bestDefPayoffFound = defenderPayoff;
				}

				// // MANISH
				// if (defenderPayoff > this.gameValue
				// + this.betterResponseEpsilon) {
				allocationAdded = this.usProblem.getActProfile()
						.addDefenderAllocation(da);
				// } else {
				// allocationAdded = false;
				// }

				// allocationAdded = this.usProblem.getActProfile()
				// .addDefenderAllocation(da);

				measure.defenderOracleTimes.add(this.brDef.getRunTime());
				measure.defenderBRVal.add(defenderPayoff);
			} else {
				measure.defenderOracleTimes.add((long) 0);
				measure.defenderBRVal.add(0.0);
			}

			// System.out.println("DA: "+ da);

			// allocationAdded = false if da
			// already generated before
			// System.out.print("Updating clp ... ");
			this.clp.updateDefenderAllocations();
			// System.out.print("done.\nUpdating BRAdv ... ");
			this.brAdv.updateDefenderAllocations();
			// System.out.println("done.");

			// update problem data structures
			// update and solve Core-LP model
			// avoids repetition of the same allocation
			// avoids repetition of the same path
			// boolean pathAdded =
			// this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded
			// = false if ap already generated before

			// System.out.print("Solving clp ... ");
			this.clp.solve();
			// System.out.println("done in " + this.clp.getRunTime() +
			// " milliseconds.");
			this.gameValue = this.clp.getLPObjective();
			totalCLTime += this.clp.getRunTime();
			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp
					.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp
					.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfDefenderAllocations());

			// update BRAdv
			List<Double> defStrategy = this.clp.getDefenderStrategy();
			this.brAdv.setDefenderStrategy(defStrategy);

			// MANISH DEBUG
			// System.out.println("7." + this.clp.getAdversaryStrategy() + ", "
			// + defStrategy);

			double adversaryPayoff = Double.MIN_VALUE;
			long betterRAdvTime = 0L;

			if (enableBetterAttackerOracle == true
					&& flagBetterResponseRun ) {

				stTime = System.currentTimeMillis();
				// System.out.print("Getting better att response ... ");
				Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> betterAdvResponse = getBetterAttackerResponses();
				betterRAdvTime = System.currentTimeMillis() - stTime;

				if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
					System.out.println("BETTER ADV done in " + betterRAdvTime
							+ " milliseconds");
					System.out.flush();
				}

				ap = betterAdvResponse.getValue().getKey();
				adversaryPayoff = betterAdvResponse.getValue().getValue();

				// MANISH DEBUG
				// System.out.println("1." + ap);

				// MANISH HERE
				if (adversaryPayoff > (-gameValue)) { // +
														// this.betterResponseEpsilon))
														// {
					pathAdded = this.usProblem.getActProfile()
							.addAdversaryPath(ap);
					countBestResponseAdvRun = 0;
				} else {
					pathAdded = false;
					countBestResponseAdvRun++;
					if ( countBestResponseAdvRun == MAX_BEST_RESPONSE_RUN)
						flagBetterResponseRun = false;						
				}

				measure.betterAdversaryOracleTimes.add(betterRAdvTime);
				measure.adversaryBetterRVal.add(adversaryPayoff);
			}

			boolean apBestResponseOracleRun = false;

			if (enableBetterAttackerOracle == false || pathAdded == false
					|| adversaryPayoff < -this.clp.getLPObjective())
			// + Configuration.MIP_TOLERANCE)
			{
				// solve adversary problem
				// System.out.print("Getting best att response ... ");
				this.brAdv.solve();
				if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
					System.out.println("BEST ADV done in "
							+ this.brAdv.getRunTime() + " milliseconds.");
					System.out.flush();
				}
				totalBRAdvTime += this.brAdv.getRunTime();
				adversaryPayoff = this.brAdv.getAdversaryPayoff();

				if (bestAdvPayoffFound > adversaryPayoff) {
					bestAdvPayoffFound = adversaryPayoff;
				}

				if ( flagBetterResponseRun == false ) {
					countBestResponseAdvRun --;
					if ( countBestResponseAdvRun == 0)
						flagBetterResponseRun = true;
				}
				
				// get solution
				ap = this.brAdv.getAdversaryPath();
				// pathAdded =
				// this.usProblem.getActProfile().addAdversaryPath(ap);
				// System.out.println("AP: "+ ap);

				// MANISH DEBUG
				// System.out.println("2." + ap);

				// MANISH HERE
				// if (adversaryPayoff > (-gameValue +
				// this.betterResponseEpsilon)) {
				pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap);
				// } else {
				// pathAdded = false;
				// }

				apBestResponseOracleRun = true;

				measure.adversaryOracleTimes.add(this.brAdv.getRunTime());
				measure.adversaryBRVal.add(this.brAdv.getAdversaryPayoff());
			} else {
				measure.adversaryOracleTimes.add(0L);
				measure.adversaryBRVal.add(0.0);
			}

			if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
				System.out.println("Iteration No: " + iterationNo
						+ "GV/BRDef/BRAdv: " + this.gameValue + "/"
						+ defenderPayoff + "/" + adversaryPayoff);
				System.out.flush();
			}

			double diffPayoffs = defenderPayoff - (-adversaryPayoff);
			if (diffPayoffs < 10 && printWhenGapLessThanTen) {
				System.out.println("GAP10: "
						+ ((long) System.currentTimeMillis() - curTime));
				printWhenGapLessThanTen = false;
			}
			if (diffPayoffs < 5 && printWhenGapLessThanFive) {
				System.out.println("GAP5: "
						+ ((long) System.currentTimeMillis() - curTime));
				printWhenGapLessThanFive = false;
			}
			if (diffPayoffs < 3 && printWhenGapLessThanThree) {
				System.out.println("GAP3: "
						+ ((long) System.currentTimeMillis() - curTime));
				printWhenGapLessThanThree = false;
			}
			if (diffPayoffs < 2 && printWhenGapLessThanTwo) {
				System.out.println("GAP2: "
						+ ((long) System.currentTimeMillis() - curTime));
				printWhenGapLessThanTwo = false;
			}

			if (diffPayoffs < 1 && printWhenGapLessThanOne) {
				System.out.println("GAP1: "
						+ ((long) System.currentTimeMillis() - curTime));
				printWhenGapLessThanOne = false;
			}
			if (diffPayoffs < 0.10 && printWhenGapLessThanPointOne) {
				System.out.println("GAP0.1: "
						+ ((long) System.currentTimeMillis() - curTime));
				printWhenGapLessThanPointOne = false;
			}
			if (diffPayoffs < 0.010 && printWhenGapLessThanPointOOne) {
				System.out.println("GAP0.01: "
						+ ((long) System.currentTimeMillis() - curTime));
				printWhenGapLessThanPointOOne = false;
			}
			boolean check = false;
			// if both best responses were run then
			if (Math.abs(defenderPayoff - (-adversaryPayoff)) <= this.finalConvergenceEpsilon
					|| (allocationAdded == false && pathAdded == false)) {
				//runDefOracle();
				check = true;
			}
				
			if (check && (Math.abs(defenderPayoff - (-adversaryPayoff)) <= this.finalConvergenceEpsilon
					|| (allocationAdded == false && pathAdded == false))) {
				
				long stopTime = System.currentTimeMillis();
				measure.totalTime = stopTime - curTime;
				measure.iterations = iterationNo;
				measure.isOK();

				// MANISH DEBUG
				// System.out.println("3. Breaking"
				// + this.usProblem.getActProfile()
				// .getNumberOfAdversaryPaths()
				// + ", "
				// + this.usProblem.getActProfile()
				// .getNumberOfDefenderAllocations());

				System.out.println("WRAPPER Game value: "
						+ clp.getLPObjective());
				// measure.write();
				break;
				// }
			}
			// if (allocationAdded == false && pathAdded == false) {
			// if (Math.abs(defenderPayoff - gameValueCheck) <=
			// diffPercentage
			// && Math.abs(-adversaryPayoff - gameValueCheck) <=
			// diffPercentage) {

			// add all paths from better response set
			// List<AdversaryPath> apList = betterAdvResponse.getKey();
			// for ( AdversaryPath apListIter : apList ) {
			// this.usProblem.getActProfile().addAdversaryPath(apListIter); //
			// pathAdded
			// }
			// add all paths generated from bestResponseOracle against all
			// targets
			// if ( apBestResponseOracleRun ) {
			// List<AdversaryPath> apListBR =
			// this.brAdv.getAllTargetAdversaryPaths();
			// for ( AdversaryPath apListIter : apListBR ) {
			// this.usProblem.getActProfile().addAdversaryPath(apListIter); //
			// pathAdded
			// }
			// }

			if (enableDiscardUselessActions == true
					&& localNumIterationsBeforeDiscarding > numRoundsBeforeDiscard) {
				this.discardActionsNotInSupport();
				localNumIterationsBeforeDiscarding = 0;
			}

			// System.out.print("Updating clp ... ");
			this.clp.updateAdversaryPaths();
			// System.out.print("done.\nUpdating BRDef ... ");
			this.brDef.updateAdversaryPaths();
			// System.out.println("done.");

			// System.out.print("Solving clp ... ");
			this.clp.solve();
			// System.out.println("done in " + this.clp.getRunTime() +
			// " milliseconds");

			// MANISH DEBUG
			// System.out.println("4." + this.clp.getDefenderStrategy() + ", "
			// + this.clp.getAdversaryStrategy());
			// System.out.println("4.5. "
			// + this.usProblem.getActProfile()
			// .getNumberOfDefenderAllocations()
			// + ", "
			// + this.usProblem.getActProfile()
			// .getNumberOfAdversaryPaths());

			totalCLTime += this.clp.getRunTime();
			this.gameValue = this.clp.getLPObjective();
			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp
					.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp
					.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile()
					.getNumberOfDefenderAllocations());

			// update BRDef
			List<Double> advStrategy = this.clp.getAdversaryStrategy();
			// this.brDef.updateAdversaryPaths();
			this.brDef.setAdversaryStrategy(advStrategy);

			// System.out.println("Defender Strategy: " +
			// this.clp.getDefenderStrategy());
			// System.out.println("Attacker Strategy: " +
			// this.clp.getAdversaryStrategy());

			System.gc();

			// if (iterationNo % 250 == 0) {
			// this.writeMeasure();
			// }

			// MANISH DEBUG
			if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
				System.out.println("---------");
			}

			/**
			 * Read Instructions on resetLP if defender allocations / adversary
			 * paths need to be removed. Maintain the order of the calls -- the
			 * set of commands for addDefenderAllocations are called together,
			 * and same for addAdversaryPaths. Their order can be interchanged
			 * but not interleaved.
			 */
		}

		// boolean ok = measure.isOK();
		// if (!ok) {
		// measure.write();
		// System.err
		// .println(" ===== MEASURE NOT OK !!!!! ===================");
		// System.exit(1);
		// }

		System.out.println("Done with main loop in: "
				+ (System.currentTimeMillis() - timeVar));
		System.out.flush();

		this.runTime = System.currentTimeMillis() - curTime;

		List<Double> defStrat = this.getDefenderStrategy();
		double defWSCov = 0.0;
		for (int i = 0; i < wsDefStrat; i++) {
			defWSCov += defStrat.get(i);
		}
		List<Double> advStrat = this.getAdversaryStrategy();
		double advWSCov = 0.0;
		for (int i = 0; i < wsAdvStrat; i++) {
			advWSCov += advStrat.get(i);
		}
		System.out.println("Def WS Cov: " + defWSCov);
		System.out.println("Adv WS Cov: " + advWSCov);
		System.out.flush();
	}
	public void runDefOracle() throws LPSolverException{
		DefenderAllocation da = null;
		boolean allocationAdded = false;
		this.brDef.solve();
		totalBRDefTime += this.brDef.getRunTime();
		da = this.brDef.getDefenderAllocation();
		allocationAdded = this.usProblem.getActProfile()
					.addDefenderAllocation(da); // allocationAdded = false if da
												// already generated before
		this.brAdv.updateDefenderAllocations();

		AdversaryPath ap = null;
		boolean pathAdded = false;

		this.brAdv.solve();
		totalBRAdvTime += this.brAdv.getRunTime();
		ap = this.brAdv.getAdversaryPath();
		pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap);

		this.brDef.updateAdversaryPaths();

		this.clp.solve();
		this.gameValue = this.clp.getLPObjective();
		totalCLTime += this.clp.getRunTime();
		measure.coreLPtimes.add(this.clp.getRunTime());
		measure.coreLPGV.add(gameValue);
		measure.adversarySuppSetSize.add(getNonZeros(this.clp
				.getAdversaryStrategy()));
		measure.defenderSuppSetSize.add(getNonZeros(this.clp
				.getDefenderStrategy()));
		measure.adversaryStrategiesSize.add(this.usProblem.getActProfile()
				.getNumberOfAdversaryPaths());
		measure.defenderStrategiesSize.add(this.usProblem.getActProfile()
				.getNumberOfDefenderAllocations());

		this.brAdv.setDefenderStrategy(this.clp.getDefenderStrategy());
		this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());

		
	}
	
	public double getOptimalDefValue() {
		return this.clp.getLPObjective();
	}
	
	public int getNumberOfEdgesCoveredByDefender() {
		Map<ExtDWE, Double> defCov = this.clp.getDefenderCoverage();
		int count = 0;
		for ( ExtDWE e : defCov.keySet() ) {
			count += ((defCov.get(e) > Configuration.EPSILON) ? 1 : 0);
		}
		return count;
	}

	public boolean getcutoffUsed() {
		return cutoffUsed;
	}
}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
//~~~~~~~~~~~~EXTRA GRAPH CLASS METHODS~~~~~~~~~~~~~~~~~~//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

class PQNode implements Comparable<PQNode> {
	INode n;
	Double w;

	public PQNode(INode n, Double w) {
		super();
		this.n = n;
		this.w = w;
	}

	public INode getN() {
		return n;
	}

	public void setN(INode n) {
		this.n = n;
	}

	public Double getW() {
		return w;
	}

	public void setW(Double w) {
		this.w = w;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((n == null) ? 0 : n.hashCode());
		result = prime * result + ((w == null) ? 0 : w.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PQNode other = (PQNode) obj;
		if (n == null) {
			if (other.n != null)
				return false;
		} else if (!n.equals(other.n))
			return false;
		if (w == null) {
			if (other.w != null)
				return false;
		} else if (!w.equals(other.w))
			return false;
		return true;
	}

	@Override
	public int compareTo(PQNode o) {
		// TODO Auto-generated method stub
		if (this.w < o.w) {
			return -1;
		} else if (this.w == o.w) {
			return 0;
		} else
			return 1;
	}

}
