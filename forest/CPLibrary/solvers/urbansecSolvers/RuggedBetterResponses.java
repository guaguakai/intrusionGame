package urbansecSolvers;

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
import model.urbansecModels.AdversaryPath;
import model.urbansecModels.DefenderAllocation;
import model.urbansecModels.MinCut;
import model.urbansecModels.UrbanSecProblem;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import utilities.Pair;

public class RuggedBetterResponses extends Rugged {

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

	protected boolean enableBetterDefenderOracle;
	protected boolean enableBetterAttackerOracle;

	Set<ExtDWE> minCutEdges;

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

	public void setEnableBetterDefenderOracle(boolean enableBetterDefenderOracle) {
		this.enableBetterDefenderOracle = enableBetterDefenderOracle;
	}

	public boolean isEnableBetterAttackerOracle() {
		return enableBetterAttackerOracle;
	}

	public void setEnableBetterAttackerOracle(boolean enableBetterAttackerOracle) {
		this.enableBetterAttackerOracle = enableBetterAttackerOracle;
	}

	public RuggedBetterResponses(UrbanSecProblem usProblem) {
		super(usProblem);
		this.maxWarmStartConvergeIterations = -1;
		this.numWarmStartConvergeIterations = 0;
		this.enableBetterAttackerOracle = true;
		this.enableBetterDefenderOracle = true;
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

	public int getkShortestPaths() {
		return kShortestPaths;
	}

	public void setkShortestPaths(int kShortestPaths) {
		this.kShortestPaths = kShortestPaths;
	}

	public List<DefenderAllocation> getWarmStartDefenderRandomEdges(int count) {
		List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();

		while (lstDA.size() < count) {
			DefenderAllocation da = new DefenderAllocation();
			List<ExtDWE> lstEdges = new ArrayList<ExtDWE>(this.usProblem
					.getGraph().edgeSet());
			Collections.shuffle(lstEdges, model.Configuration.randomGenerator);
			while (da.size() < this.usProblem.getNumDefenderResources()
					&& lstEdges.size() > 0) {
				da.addEdgeToAllocation(lstEdges.remove(0));
			}
			lstDA.add(da);
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
			while (da.size() < this.usProblem.getNumDefenderResources()
					&& lstEdges.size() > 0) {
				da.addEdgeToAllocation(lstEdges.remove(0));
			}
			lstDA.add(da);
		}

		return lstDA;
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

	public Pair<DefenderAllocation, Double> getBetterDefenderResponse() {
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
		
		while (da.size() < this.usProblem.getNumDefenderResources()
				&& apNotCoveredYet.size() > 0) {
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

			// System.out.println("MaxWeight: " + maxWeight);
			// System.out.println("MaxWeightEdge: " + maxWeightEdge);

			// now add edge to DefenderAllocation
			da.addEdgeToAllocation(maxWeightEdge);

			// now remove paths from apNotCoveredYet which have edge in them
			Set<AdversaryPath> markPathsToRemove = new HashSet<AdversaryPath>();
			for (AdversaryPath ap : apNotCoveredYet) {
				if (ap.isInPath(maxWeightEdge)) {
					markPathsToRemove.add(ap);
				}
			}
			apNotCoveredYet.removeAll(markPathsToRemove);
		}

		// now all the edges that will be added will not increase
		// defender-payoff in the next round, but try to find best
		// "potential edges" to add.

		if (da.size() < this.usProblem.getNumDefenderResources()) {
			this.computeMinCut();
			for (ExtDWE e : this.minCutEdges) {
				da.addEdgeToAllocation(e);
				if (da.size() == this.usProblem.getNumDefenderResources()) {
					break;
				}
			}
		}

		// fill the rest of the allocation with random edges
		if (da.size() < this.usProblem.getNumDefenderResources()) {
			// randomly add edges to da
			List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem
					.getGraph().edgeSet());
			Collections.shuffle(edgeList);

			while (da.size() < this.usProblem.getNumDefenderResources()) {
				ExtDWE newEdge = edgeList.remove(0);
				if (newEdge.getType() == EDGE_TYPE.NORMAL) {
					da.addEdgeToAllocation(newEdge);
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

	public double getPayoffForAllocation(DefenderAllocation da) {
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
				INode v = e.gettarget();
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
			temp = e.getsource();
		}

		// System.out.println("path: " + apBRPath);

		Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> returnPair = new Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>>(
				null, new Pair<AdversaryPath, Double>(apBRPath, maxPayoff));
		return returnPair;
	}

	public double getOptimalDefValue() {
		return this.clp.getLPObjective();
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
			Pair<DefenderAllocation, Double> betterDefResponse = getBetterDefenderResponse();
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

	public void run() throws LPSolverException, MalformedGraphException {
		// System.out.println("Wrapper started. Please hold, this may take a while...");
		// System.out.println("RUGGED resources: "
		// + this.usProblem.getNumDefenderResources());
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

		if (this.enableSkeleton == true) {
			System.out.println("Going to compute the skeleton and solve it.");
			System.out.flush();
			long skStTime = System.currentTimeMillis();
			this.warmStartUsingSkeleton();
			System.out.println("Skeleton solved in: "
					+ (System.currentTimeMillis() - skStTime));
			System.out.flush();
		}

		//System.out.println("Initializing brDef ... ");
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

			//System.out.println("Done with brDef init in: "
			//		+ (System.currentTimeMillis() - timeVar));
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

//			System.out.println("Done with brAdv init in: "
//					+ (System.currentTimeMillis() - timeVar));
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

//		System.out.println("Done with clp init in: "
//				+ (System.currentTimeMillis() - timeVar));
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

		//System.out.println("Starting main loop ...");
		System.out.flush();
		timeVar = System.currentTimeMillis();

		boolean printWhenGapLessThanTen = false;
		boolean printWhenGapLessThanFive = false;
		boolean printWhenGapLessThanThree = false;
		boolean printWhenGapLessThanTwo = false;
		boolean printWhenGapLessThanOne = false;
		boolean printWhenGapLessThanPointOne = false;
		boolean printWhenGapLessThanPointOOne = false;

		int wsDefStrat = this.usProblem.getActProfile()
				.getNumberOfDefenderAllocations();
		int wsAdvStrat = this.usProblem.getActProfile()
				.getNumberOfAdversaryPaths();

//		System.out.println("Warm Start Sizes in Rugged: " + wsDefStrat + ", "
//				+ wsAdvStrat);

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
				Pair<DefenderAllocation, Double> betterDefResponse = getBetterDefenderResponse();
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
			// + Configuration.MIP_TOLERANCE
			) {
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
					&& flagBetterResponseRun) {

				stTime = System.currentTimeMillis();
				// System.out.print("Getting better att response ... ");
				Pair<List<AdversaryPath>, Pair<AdversaryPath, Double>> betterAdvResponse = getBetterAttackerResponses();
				betterRAdvTime = System.currentTimeMillis() - stTime;

				if (iterationNo % ITERATION_COUNT_FOR_PRINT == 0) {
//					System.out.println("BETTER ADV done in " + betterRAdvTime
//							+ " milliseconds");
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
//					System.out.println("BEST ADV done in "
//							+ this.brAdv.getRunTime() + " milliseconds.");
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

			// if both best responses were run then
			if (Math.abs(defenderPayoff - (-adversaryPayoff)) <= this.finalConvergenceEpsilon
					|| (allocationAdded == false && pathAdded == false)) {
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

//		System.out.println("Done with main loop in: "
//				+ (System.currentTimeMillis() - timeVar));
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
//		System.out.println("Def WS Cov: " + defWSCov);
//		System.out.println("Adv WS Cov: " + advWSCov);
		System.out.flush();
	}

	public int getNumberOfEdgesCoveredByDefender() {
		Map<ExtDWE, Double> defCov = this.clp.getDefenderCoverage();
		int count = 0;
		for ( ExtDWE e : defCov.keySet() ) {
			count += ((defCov.get(e) > Configuration.EPSILON) ? 1 : 0);
		}
		return count;
	}
}

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
