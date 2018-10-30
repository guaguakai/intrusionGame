package model.graphutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import model.Configuration;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;


import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.MinCut;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class GraphGenerator {

	public static AbstractBaseGraph<INode, ExtDWE> genFullyConnectedGraph(
			int numNodes) {
		return genFullyConnectedGraph(numNodes, 10);
	}

	public static AbstractBaseGraph<INode, ExtDWE> genTestGraph() {
		Node.resetCounter();
		ExtDWE.reset();
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		INode source = new Node(NODE_TYPE.SOURCE, 0);
		INode target1 = new Node(NODE_TYPE.TARGET, 10);
		INode target2 = new Node(NODE_TYPE.TARGET, 5);

		INode n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		INode n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		INode n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		INode n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);

		graph.addVertex(source);
		graph.addVertex(target1);
		// graph.addVertex(target2);
		graph.addVertex(n1);
		graph.addVertex(n2);
//		graph.addVertex(n3);
		// graph.addVertex(n4);

		ExtDWE e1 = graph.addEdge(source, n1);
		// e1.setType(EDGE_TYPE.VIRTUAL);
		ExtDWE e2 = graph.addEdge(source, n2);
		ExtDWE e3 = graph.addEdge(n1, target1);
		ExtDWE e4 = graph.addEdge(n2, target1);
//		ExtDWE e5 = graph.addEdge(n3, n1);

		AdversaryPath ap = new AdversaryPath(target1);
		ap.addEdgeToPath(e1);
		ap.addEdgeToPath(e2);
		ap.addEdgeToPath(e3);
		ap.addEdgeToPath(e4);
//		ap.addEdgeToPath(e5);

		System.out.println("AP with cycle: " + ap);
		ap.removeCycleFromPath(source);
		System.out.println("AP without cycle: " + ap);

		// ExtDWE e6 = graph.addEdge(n3, target1);
		// ExtDWE e7 = graph.addEdge(target1, target2);
		// ExtDWE e4 = graph.addEdge(n1, n3);
		// ExtDWE e5 = graph.addEdge(n1, n4);
		// ExtDWE e6 = graph.addEdge(n4, n5);
		// ExtDWE e7 = graph.addEdge(n2, target);
		// ExtDWE e8 = graph.addEdge(n4, target);

		// for ( ExtDWE e : graph.edgeSet() ) {
		// graph.setEdgeWeight(e, 1.0);
		// }

		// graph.setEdgeWeight(arg0, arg1)

		try {
			lpWrapper.Configuration.loadLibrariesCplex();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Couldn't load Cplex.\n");
			e.printStackTrace();
		}

		MinCut minCutObj = new MinCut(graph);
		minCutObj.setSource(source);
		minCutObj.setTarget(target1);
		minCutObj.loadProblem();
		// minCutObj.writeProb("minCut1");
		System.out.println("Min Cut: " + minCutObj.getMinCut());

		// minCutObj.disableEdge(e1);
		// try {
		// minCutObj.updateObjective();
		// } catch (Exception e9) {
		// // TODO Auto-generated catch block
		// e9.printStackTrace();
		// }
		// minCutObj.writeProb("minCut2");
		// minCutObj.disableEdge(e7);
		// minCutObj.disableEdge(e2);

		// System.out.println("Min Cut: " + minCutObj.getMinCut());

		List<ExtDWE> lstEdges = DijkstraShortestPath.findPathBetween(graph,
				source, target1);
		double totalPathWeight = 0.0;
		System.out.println("Shortest path edge weights: ");
		for (ExtDWE e : lstEdges) {
			System.out.println(e + " : " + graph.getEdgeWeight(e));
			totalPathWeight += graph.getEdgeWeight(e);
		}
		System.out.println("Shortest path: " + lstEdges + ": "
				+ totalPathWeight);

		return graph;
	}

	public static AbstractBaseGraph<INode, ExtDWE> genFullyConnectedGraph(
			int numNodes, int targetval) {
		Node.resetCounter();
		ExtDWE.reset();
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		// set source, then nodes, then target
		List<INode> nodeList = new ArrayList<INode>(numNodes);
		INode source = new Node(NODE_TYPE.SOURCE, 0);
		nodeList.add(source);
		graph.addVertex(source);
		for (int i = 1; i < numNodes - 2; i++) {
			INode n = new Node(NODE_TYPE.INTERMEDIATE, 0);
			nodeList.add(n);
			graph.addVertex(n);
		}
		INode target = new Node(NODE_TYPE.TARGET, targetval);
		nodeList.add(target);
		graph.addVertex(target);

		INode target2 = new Node(NODE_TYPE.TARGET, targetval);
		nodeList.add(target2);
		graph.addVertex(target2);

		for (int i = 0; i < numNodes - 1; i++) {
			for (int j = i + 1; j < numNodes; j++) {
				// for ( int j=0; j<numNodes; j++) {
				// if(i!=j){
				ExtDWE e = graph.addEdge(nodeList.get(i), nodeList.get(j));
				// }
			}
		}
		return graph;
	}

	public static AbstractBaseGraph<INode, ExtDWE> genFullyConnectedGraph(
			int numNodes, int targetsNo, int targetVal, Random random) {
		Node.resetCounter();
		ExtDWE.reset();
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		// set source, then nodes, then target
		List<INode> nodeList = new ArrayList<INode>(numNodes);

		INode source = new Node(NODE_TYPE.SOURCE, 0);
		nodeList.add(source);
		graph.addVertex(source);

		for (int i = 1; i < numNodes - targetsNo; i++) {
			INode n = new Node(NODE_TYPE.INTERMEDIATE, 0);
			nodeList.add(n);
			graph.addVertex(n);
		}

		for (int i = 0; i < targetsNo; i++) {
			INode target = new Node(NODE_TYPE.TARGET,
					random.nextInt(targetVal) + 1);
			nodeList.add(target);
			graph.addVertex(target);
		}

		for (int i = 0; i < numNodes - 1; i++) {
			for (int j = i + 1; j < numNodes; j++) {
				// for ( int j=0; j<numNodes; j++) {
				// if(i!=j){
				graph.addEdge(nodeList.get(i), nodeList.get(j));
				// }
			}
		}
		return graph;
	}

	public static AbstractBaseGraph<INode, ExtDWE> getPonyTailGraph(int length,
			int minedges, int maxedges, double targetProb, Random random) {
		Node.resetCounter();
		ExtDWE.reset();
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		// set source, then nodes, then target
		List<INode> nodeList = new ArrayList<INode>();
		INode source = new Node(NODE_TYPE.SOURCE, 0);
		nodeList.add(source);
		graph.addVertex(source);
		int targets = 0;
		for (int i = 1; i < length; i++) { // generate vertices
			if (random.nextDouble() > targetProb && i < length - 1) { // last
																		// one
																		// is
																		// target
				// as well as some in the middle
				INode node = new Node(NODE_TYPE.INTERMEDIATE, 0);
				nodeList.add(node);
				graph.addVertex(node);
			} else {
				targets++;
				INode node = new Node(NODE_TYPE.TARGET, random.nextInt(100));
				nodeList.add(node);
				graph.addVertex(node);
			}
		}

		for (int j = 0; j < nodeList.size() - 1; j++) {
			int nrOfEdges = random.nextInt(maxedges - minedges) + minedges;
			for (int k = 0; k < nrOfEdges; k++) {
				graph.addEdge(nodeList.get(j), nodeList.get(j + 1));
			}
		}
		// System.out.println("Targets: "+ targets);
		return graph;
	}

	/**
	 * 
	 * @param length
	 * @param minedges
	 * @param maxedges
	 * @param targetProb
	 * @return
	 */
	public static AbstractBaseGraph<INode, ExtDWE> getExamplePonny() {
		Node.resetCounter();
		ExtDWE.reset();
		// Random random= new Random(3);
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		// set source, then nodes, then target
		INode source = new Node(NODE_TYPE.SOURCE, 0);
		graph.addVertex(source);

		INode target = new Node(NODE_TYPE.TARGET, 1);
		graph.addVertex(target);
		for (int k = 0; k < 3; k++) {
			graph.addEdge(source, target);
		}

		INode target2 = new Node(NODE_TYPE.TARGET, 10000);
		graph.addVertex(target2);
		graph.addEdge(target, target2);
		// for(int k = 0;k<2;k++){
		// }

		return graph;
	}

	public static INode addVirtualTarget(AbstractBaseGraph<INode, ExtDWE> graph) {
		INode virtualTarget = new Node(NODE_TYPE.VIRTUAL_TARGET, 0);
		graph.addVertex(virtualTarget);

		for (INode node : graph.vertexSet()) {
			if (node.getType() == NODE_TYPE.TARGET) {
				ExtDWE newEdge = graph.addEdge(node, virtualTarget);
				newEdge.setType(EDGE_TYPE.VIRTUAL);
			}
		}
		return virtualTarget;
	}

	public static INode addVirtualSource(AbstractBaseGraph<INode, ExtDWE> graph) {
		INode virtualSource = new Node(NODE_TYPE.INTERMEDIATE, 0);
		virtualSource.setLatLon(-1, -1);
		graph.addVertex(virtualSource);

		for (INode node : graph.vertexSet()) {
			if (node.getType() == NODE_TYPE.SOURCE) {
				ExtDWE newEdge = graph.addEdge(virtualSource, node);
				newEdge.setType(EDGE_TYPE.VIRTUAL);
				node.setType(NODE_TYPE.INTERMEDIATE);
			}
		}
		virtualSource.setType(NODE_TYPE.SOURCE);

		return virtualSource;
	}
	public static void printGraph(AbstractBaseGraph g){
		
		String nodes = "";
		Iterator<INode> n = g.vertexSet().iterator();
		while(n.hasNext()){
			INode node = n.next();
			nodes+= node.getId()+",";
		}
		
		String edges = "";
		Iterator<ExtDWE> e = g.edgeSet().iterator();
		while(e.hasNext()){
			ExtDWE edge = e.next();
			edges+= "("+edge.getsource().getId()+","+edge.gettarget().getId()+"),";
		}
		
		String targets = "";
		Iterator<INode> t = getTargetSet(g).iterator();
		while(t.hasNext()){
			INode node = t.next();
			targets+= node.getId()+",";
		}
		
		String sources = "";
		Iterator<INode> s = getSources(g).iterator();
		while(s.hasNext()){
			INode node = s.next();
			sources+= node.getId()+",";
		}
		
		System.out.println("{"+nodes+"};");
		System.out.println("{"+edges+"};");
		System.out.println("{"+targets+"};");
		System.out.println("{"+sources+"};");
	}
	public static AbstractBaseGraph<INode, ExtDWE> getDenseRandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r, int min, int max){
		Configuration.initialize(108374);
		int size= 0;
		AbstractBaseGraph graph=null;
		while(size<min || size>max){
			Random random = new Random();
			graph = getRandomGeometricGraph(numNodes, nrOfSources, nrOfTargets, r,
					50, random);
		
			//GraphGenerator.removeNodesWithDegree4(graph, GraphGenerator.getTargetSet(graph), GraphGenerator.getSourceSet(graph));
		
			size=GraphGenerator.getMinCutPaths(graph).size();
		}
		System.out.println(size);
		return graph;
	}
	public static AbstractBaseGraph<INode, ExtDWE> getRandomGeometricGraph(
			int numNodes, int nrOfSources, int nrOfTargets, double r,
			int maxTargetVal, Random random) {
		// r should be between 0 and 1.
		if (nrOfSources + nrOfTargets > numNodes) {
			System.out.println("numNodes: " + numNodes);
			System.out.println("numSources: " + nrOfSources);
			System.out.println("numTargets: " + nrOfTargets);
			throw new RuntimeException(
					"Can't generate this graph which has more sources and targets than nodes");
		}

		Node.resetCounter();
		ExtDWE.reset();
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		Map<INode, LatLon> nodeMap = new HashMap<INode, LatLon>();

		for (int i = 0; i < numNodes; i++) {
			INode node = new Node(NODE_TYPE.INTERMEDIATE, 0);
			LatLon ll = new LatLon(random.nextDouble(), random.nextDouble());
			node.setLatLon(ll.getLat(), ll.getLon());
			nodeMap.put(node, ll);
			graph.addVertex(node);
		}

		for (INode u : nodeMap.keySet()) {
			for (INode v : nodeMap.keySet()) {
				if (u.equals(v))
					continue;
				if (nodeMap.get(u).getDistance(nodeMap.get(v)) < r) {
					graph.addEdge(u, v);
				}
			}
		}

		List<INode> nodes = new LinkedList<INode>();
		nodes.addAll(nodeMap.keySet());
		Collections.shuffle(nodes, random);
		for (int i = 0; i < nrOfTargets; i++) {
			Node target = (Node) nodes.remove(0);
			target.setType(NODE_TYPE.TARGET);
			target.setPayoff(Configuration.randomGenerator
					.nextInt(maxTargetVal));
		}

		// System.out.println("lstSets size: " + lstSets.size());

		connectDisconnectedGraph(graph);

		INode source = new Node(NODE_TYPE.SOURCE, 0);
		source.setLatLon(-1, -1);
		graph.addVertex(source);

		int countSources = 0;
		Collections.shuffle(nodes, random);
		while (countSources < nrOfSources) {
			Node sourceToAdd = (Node) nodes.remove(0);
			if (sourceToAdd.getType() == NODE_TYPE.TARGET) {
				continue;
			}
			ExtDWE newEdge = graph.addEdge(source, sourceToAdd);
			newEdge.setType(EDGE_TYPE.VIRTUAL);
			countSources++;
		}

		return graph;
	}
	public static Set<INode> getSources(
			AbstractBaseGraph<INode, ExtDWE> graph) {
		Set<INode> sources = new HashSet<INode>();
		for (ExtDWE u : graph.edgeSet()) {
			if (u.getType() == EDGE_TYPE.VIRTUAL) {
				sources.add(u.gettarget());
			}
		}
		return sources;
	}
	public static Set<INode> getSourceSet(
			AbstractBaseGraph<INode, ExtDWE> graph) {
		Set<INode> sources = new HashSet<INode>();
		for (INode u : graph.vertexSet()) {
			if (u.getType() == NODE_TYPE.SOURCE) {
				sources.add(u);
			}
		}
		return sources;
	}
	
	public static Set<INode> getSourceSet(
			AbstractBaseGraph<INode, ExtDWE> graph, Set<Long> sourceOSMIDs) {
		Set<INode> sources = new HashSet<INode>();
		for (INode u : graph.vertexSet()) {
			if (sourceOSMIDs.contains(u.getOsmId())) {
				sources.add(u);
			}
		}
		return sources;
	}

	public static Set<INode> getTargetSet(
			AbstractBaseGraph<INode, ExtDWE> graph) {
		Set<INode> targets = new HashSet<INode>();
		for (INode u : graph.vertexSet()) {
			if (u.getType() == NODE_TYPE.TARGET) {
				targets.add(u);
			}
		}
		return targets;
	}
	
	public static Set<INode> getTargetSet(
			AbstractBaseGraph<INode, ExtDWE> graph, Set<Long> targetOSMIds) {
		Set<INode> targets = new HashSet<INode>();
		for (INode u : graph.vertexSet()) {
			if (targetOSMIds.contains(u.getOsmId())) {
				targets.add(u);
			}
		}
		return targets;
	}

	public static void removeNodesWithDegree2(
			AbstractBaseGraph<INode, ExtDWE> graph, Set<INode> sources,
			Set<INode> targets) {
		Set<INode> removeNodeSet = new HashSet<INode>();
		for (INode n : graph.vertexSet()) {
			if ((targets != null && targets.contains(n))
					|| (sources != null && sources.contains(n))) {
				continue;
			}
			Set<ExtDWE> inlinks = graph.incomingEdgesOf(n);
			Set<ExtDWE> outlinks = graph.outgoingEdgesOf(n);

			if (inlinks.size() <= 1 && outlinks.size() <= 1) {
				// remove this node
				boolean removeNode = true;
				if (targets != null && targets.contains(n)) {
					removeNode = false;
				}
				if (sources != null && sources.contains(n)) {
					removeNode = false;
				}
				if (removeNode) {
					removeNodeSet.add(n);
				}
			}
		}

		for (INode n : removeNodeSet) {
			if (graph.inDegreeOf(n) == 1 && graph.outDegreeOf(n) == 1) {
				ExtDWE i_e = (ExtDWE) graph.incomingEdgesOf(n).toArray()[0];
				ExtDWE o_e = (ExtDWE) graph.outgoingEdgesOf(n).toArray()[0];

				if (i_e.getsource() != o_e.gettarget()) {
					graph.addEdge(i_e.getsource(), o_e.gettarget());
					graph.addEdge(o_e.gettarget(), i_e.getsource());
					System.err.println("SHOULD NOT HAPPEN");
				}
				graph.removeVertex(n);
			} else if (graph.inDegreeOf(n) == 0 || graph.outDegreeOf(n) == 0) {
				graph.removeVertex(n);
			}
		}
	}

	public static boolean verifyFiltering(
			AbstractBaseGraph<INode, ExtDWE> graph, Set<INode> sources,
			Set<INode> targets) {
		System.out.print("Verifying ...");
		for (INode u : graph.vertexSet()) {
			if (targets.contains(u) || sources.contains(u)) {
				continue;
			}
			if (graph.inDegreeOf(u) <= 2 && graph.outDegreeOf(u) <= 2) {
				return false;
			}
		}
		return true;
	}

	public static void removeNodesWithDegree4(
			AbstractBaseGraph<INode, ExtDWE> graph, Set<INode> targets,
			Set<INode> sources) {

		Set<INode> removeNodeSet = new HashSet<INode>();
		for (INode n : graph.vertexSet()) {
			Set<ExtDWE> inlinks = graph.incomingEdgesOf(n);
			Set<ExtDWE> outlinks = graph.outgoingEdgesOf(n);

			if (inlinks.size() <= 2 && outlinks.size() <= 2) {
				// remove this node
				boolean removeNode = true;
				if (targets != null && targets.contains(n)) {
					removeNode = false;
				}
				if (sources != null && sources.contains(n)) {
					removeNode = false;
				}
				if (removeNode) {
					removeNodeSet.add(n);
				}
			}
		}
		for (INode n : removeNodeSet) {
			Set<ExtDWE> inArraySet = graph.incomingEdgesOf(n);
			Set<ExtDWE> outArraySet = graph.outgoingEdgesOf(n);

			if (graph.inDegreeOf(n) == 2 && graph.outDegreeOf(n) == 2) {
				ExtDWE[] inArray = inArraySet.toArray(new ExtDWE[0]);
				if (inArray[0].getsource() != inArray[1].getsource()) {
					graph.addEdge(inArray[0].getsource(),
							inArray[1].getsource());
					graph.addEdge(inArray[1].getsource(),
							inArray[0].getsource());
				}
				graph.removeVertex(n);
			} else if (graph.inDegreeOf(n) <= 1 || graph.outDegreeOf(n) <= 1) {
				graph.removeVertex(n);
			}

		}

	}

	public static void connectDisconnectedGraph(
			AbstractBaseGraph<INode, ExtDWE> graph) {
		ConnectivityInspector<INode, ExtDWE> conn = new ConnectivityInspector<INode, ExtDWE>(
				(DirectedGraph<INode, ExtDWE>) graph);
		List<Set<INode>> lstSets = conn.connectedSets();

		System.out.println("Components: " + lstSets.size());

		while (lstSets.size() != 1) {
			int index1 = model.Configuration.randomGenerator.nextInt(lstSets
					.size());
			int index2 = model.Configuration.randomGenerator.nextInt(lstSets
					.size());
			while (index2 == index1) {
				index2 = model.Configuration.randomGenerator.nextInt(lstSets
						.size());
			}

			int n1_index = model.Configuration.randomGenerator.nextInt(lstSets
					.get(index1).size());
			int n2_index = model.Configuration.randomGenerator.nextInt(lstSets
					.get(index2).size());

			INode n1 = (INode) lstSets.get(index1).toArray()[n1_index];
			INode n2 = (INode) lstSets.get(index2).toArray()[n2_index];

			ExtDWE e1 = graph.addEdge(n1, n2);
			ExtDWE e2 = graph.addEdge(n2, n1);

			conn = new ConnectivityInspector<INode, ExtDWE>(
					(DirectedGraph<INode, ExtDWE>) graph);

			lstSets = conn.connectedSets();
			// System.out.println(lstSets.size());
		}
	}
	private static Set<ExtDWE> computeSingleMinCut(INode targetNode, AbstractBaseGraph graph) {
		MinCut minCutObj = new MinCut(graph);
		for ( Iterator<INode> nodeIter = graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.SOURCE ) {
				minCutObj.setSource(curNode);
				break;
			}
		}
		minCutObj.setTarget(targetNode);

		minCutObj.resetLP();

		Set<ExtDWE> minCutSet = minCutObj.getMinCut();
		return minCutSet;
		
		
	}
	public static Set<ExtDWE> getMinCutPaths(AbstractBaseGraph graph){
		
		Set<ExtDWE> minCut = new HashSet<ExtDWE>();
		List<INode> targets = new ArrayList<INode>();
		
		for ( Iterator<INode> nodeIter = graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.TARGET ) {
				targets.add(curNode);
			}
		}
		//get mincut for each individual target
		for (INode targetNode : targets) {
			minCut.addAll( computeSingleMinCut(targetNode, graph));
				
		}
			return minCut;
		
	}
	public static AbstractBaseGraph<INode, ExtDWE> getGridGraph(int width,
			int length, boolean diagonal, int nrOfSources, int nrOfTargets,
			int maxTargetVal, Random random) {

		if ((width * length) < (nrOfSources + nrOfTargets)) {
			throw new IllegalArgumentException(
					"Graph is not big enough for so many sources and targets, man! Do your math!");
		}
		Node.resetCounter();
		ExtDWE.reset();
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		Map<LatLon, INode> map = new HashMap<LatLon, INode>();

		for (int y = 0; y < width; y++) {
			for (int x = 0; x < length; x++) {
				INode node = new Node();
				LatLon ll = new LatLon(x, y);
				node.setLatLon(ll.getLat(), ll.getLon());
				map.put(ll, node);
				graph.addVertex(node);
			}
		}

		for (int y = 0; y < width; y++) {
			for (int x = 0; x < length; x++) {
				LatLon llfrom = new LatLon(x, y);
				LatLon llto = new LatLon(x + 1, y);
				INode from = map.get(llfrom);
				INode to = map.get(llto);
				if (from != null && to != null) {
					graph.addEdge(from, to);
					graph.addEdge(to, from);
				}

			}
		}

		for (int y = 0; y < width; y++) {
			for (int x = 0; x < length; x++) {
				LatLon llfrom = new LatLon(x, y);
				LatLon llto = new LatLon(x, y + 1);
				INode from = map.get(llfrom);
				INode to = map.get(llto);
				if (from != null && to != null) {
					graph.addEdge(from, to);
					graph.addEdge(to, from);
				}

			}
		}

		if (diagonal) {
			for (int y = 0; y < width; y++) {
				for (int x = 0; x < length; x++) {
					LatLon llfrom = new LatLon(x, y);
					LatLon llto = new LatLon(x + 1, y + 1);
					INode from = map.get(llfrom);
					INode to = map.get(llto);
					if (from != null && to != null) {
						graph.addEdge(from, to);
						graph.addEdge(to, from);
					}

				}
			}

			for (int y = 0; y < width; y++) {
				for (int x = 1; x <= length; x++) {
					LatLon llfrom = new LatLon(x, y);
					LatLon llto = new LatLon(x - 1, y + 1);
					INode from = map.get(llfrom);
					INode to = map.get(llto);
					if (from != null && to != null) {
						graph.addEdge(from, to);
						graph.addEdge(to, from);
					}
				}
			}
		}

		List<INode> nodes = new LinkedList<INode>();
		nodes.addAll(map.values());
		Collections.shuffle(nodes, random);
		for (int i = 0; i < nrOfTargets; i++) {
			Node target = (Node) nodes.remove(0);
			target.setType(NODE_TYPE.TARGET);
			target.setPayoff(random.nextInt(maxTargetVal));
		}

		List<Node> sources = new ArrayList<Node>();
		for (int i = 0; i < nrOfSources; i++) {
			sources.add((Node) nodes.remove(0));
		}

		INode virtual = new Node(NODE_TYPE.SOURCE, 0);
		virtual.setLatLon(-1, -1);
		graph.addVertex(virtual);
		for (Node n : sources) {
			ExtDWE e = graph.addEdge(virtual, n);
			e.setType(EDGE_TYPE.VIRTUAL);
		}
		return graph;
	}

	public static void main(String[] args) {
		Configuration.initialize(0);
		// AbstractBaseGraph<INode, ExtDWE> graph =
		// GraphGenerator.getGridGraph(3,
		// 3, true, 3, 3, 100, Configuration.randomGenerator);
		AbstractBaseGraph<INode, ExtDWE> graph = GraphGenerator.genTestGraph();
		System.out.println(graph);
		for (INode n : graph.vertexSet()) {
			System.out.println(n.getType());
		}
	}

	public static AbstractBaseGraph<INode, ExtDWE> getMumbaiGraph() {
		// TODO Auto-generated method stub
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		List<INode> nodeList = new ArrayList<INode>();
		for (int x = 0; x < 108; x++) {
			INode v = new Node();
			v.setType(NODE_TYPE.INTERMEDIATE);
			nodeList.add(v);
			graph.addVertex(v);
		}

		int[] sourceIndexes = { 27, 43 };
		int[] targetIndexes = { 26, 105 };

		INode virtual = new Node();
		virtual.setType(NODE_TYPE.SOURCE);
		graph.addVertex(virtual);
		for (int i : sourceIndexes) {
			ExtDWE e = graph.addEdge(virtual, nodeList.get(i - 1));
			e.setType(EDGE_TYPE.VIRTUAL);
		}

		for (int i : targetIndexes) {
			nodeList.get(i - 1).setType(NODE_TYPE.TARGET);
			nodeList.get(i - 1).setPayoff(100);
		}
		nodeList.get(25).setPayoff(100);
		nodeList.get(104).setPayoff(200);

		int[][] edgeSet = { { 1, 2 }, { 2, 3 }, { 2, 4 }, { 4, 5 }, { 4, 6 },
				{ 6, 8 }, { 7, 8 }, { 8, 9 }, { 9, 10 }, { 10, 11 },
				{ 10, 12 }, { 12, 13 }, { 13, 14 }, { 14, 17 }, { 15, 17 },
				{ 17, 18 }, { 13, 18 }, { 16, 17 }, { 18, 19 }, { 19, 20 },
				{ 19, 24 }, { 24, 25 }, { 23, 24 }, { 20, 23 }, { 21, 23 },
				{ 20, 21 }, { 21, 22 }, { 3, 20 }, { 3, 5 }, { 12, 26 },
				{ 26, 49 }, { 26, 32 }, { 31, 32 }, { 30, 31 }, { 29, 30 },
				{ 9, 29 }, { 28, 29 }, { 8, 27 }, { 27, 28 }, { 27, 33 },
				{ 5, 6 }, { 33, 35 }, { 33, 34 }, { 28, 34 }, { 34, 37 },
				{ 34, 36 }, { 36, 37 }, { 37, 38 }, { 36, 39 }, { 38, 39 },
				{ 30, 37 }, { 31, 38 }, { 39, 40 }, { 40, 43 }, { 43, 45 },
				{ 45, 47 }, { 47, 48 }, { 47, 46 }, { 45, 46 }, { 46, 44 },
				{ 44, 43 }, { 42, 44 }, { 40, 42 }, { 41, 40 }, { 32, 41 },
				{ 41, 49 }, { 49, 51 }, { 42, 50 }, { 50, 52 }, { 52, 53 },
				{ 51, 52 }, { 50, 51 }, { 51, 68 }, { 68, 69 }, { 67, 68 },
				{ 67, 69 }, { 67, 66 }, { 69, 70 }, { 66, 70 }, { 70, 72 },
				{ 71, 72 }, { 70, 73 }, { 73, 106 }, { 72, 106 }, { 106, 75 },
				{ 72, 76 }, { 75, 76 }, { 74, 75 }, { 73, 74 }, { 74, 82 },
				{ 82, 81 }, { 81, 85 }, { 81, 80 }, { 76, 77 }, { 80, 78 },
				{ 78, 79 }, { 81, 84 }, { 84, 85 }, { 85, 86 }, { 85, 87 },
				{ 86, 87 }, { 87, 88 }, { 86, 83 }, { 84, 83 }, { 82, 83 },
				{ 83, 89 }, { 94, 89 }, { 89, 90 }, { 90, 91 }, { 90, 93 },
				{ 92, 93 }, { 93, 94 }, { 94, 95 }, { 95, 96 }, { 96, 97 },
				{ 98, 96 }, { 103, 98 }, { 98, 99 }, { 99, 95 }, { 94, 100 },
				{ 99, 100 }, { 100, 101 }, { 101, 102 }, { 102, 99 },
				{ 102, 103 }, { 64, 103 }, { 64, 104 }, { 102, 104 },
				{ 104, 105 }, { 101, 105 }, { 105, 66 }, { 65, 66 },
				{ 63, 65 }, { 63, 64 }, { 61, 63 }, { 61, 62 }, { 59, 61 },
				{ 59, 64 }, { 59, 60 }, { 57, 59 }, { 57, 58 }, { 56, 57 },
				{ 56, 59 }, { 56, 55 }, { 55, 54 }, { 55, 108 }, { 108, 54 },
				{ 107, 108 }, { 107, 65 }, { 49, 107 } };

		for (int[] edge : edgeSet) {
			graph.addEdge(nodeList.get(edge[0] - 1), nodeList.get(edge[1] - 1));
			graph.addEdge(nodeList.get(edge[1] - 1), nodeList.get(edge[0] - 1));
		}

		System.out.println("No. of edges: " + graph.edgeSet().size());
		System.out.println("No. of nodes: " + graph.vertexSet().size());

		return graph;
	}

	public static AbstractBaseGraph<INode, ExtDWE> getMadagascarGraph() {
		// TODO Auto-generated method stub
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		List<INode> nodeList = new ArrayList<INode>();
		for (int x = 0; x < 108; x++) {
			INode v = new Node();
			v.setType(NODE_TYPE.INTERMEDIATE);
			nodeList.add(v);
			graph.addVertex(v);
		}

		int[] targetIndexes = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98};
		int[] sourceIndexes = { 99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224};

		INode virtual = new Node();
		virtual.setType(NODE_TYPE.SOURCE);
		graph.addVertex(virtual);
		for (int i : sourceIndexes) {
			ExtDWE e = graph.addEdge(virtual, nodeList.get(i - 1));
			e.setType(EDGE_TYPE.VIRTUAL);
		}

		for (int i : targetIndexes) {
			nodeList.get(i - 1).setType(NODE_TYPE.TARGET);
			nodeList.get(i - 1).setPayoff(100);
		}
		nodeList.get(25).setPayoff(100);
		nodeList.get(104).setPayoff(200);

		int[][] edgeSet = {{391,101},{101,103},{103,102},{101,108},{108,100},{100,368},{108,368},{368,367},{367,107},{367,114},{367,392},{368,113},{113,99},{113,90},{90,112},{112,4},{90,116},{116,117},{117,97},{97,127},{127,6},{127,370},{370,5},{5,111},{370,126},{126,393},{126,124},{127,125},{125,128},{125,129},{129,241},{241,130},{241,130},{241,132},{132,131},{229,228},{229,227},{227,240},{240,123},{240,224},{126,224},{224,118},{118,402},{224,119},{119,225},{225,381},{381,3},{225,226},{227,226},{228,230},{226,230},{230,121},{225,121},{121,122},{122,233},{233,120},{122,233},{233,369},{369,109},{109,2},{2,1},{230,231},{230,137},{137,406},{231,133},{133,136},{136,140},{136,91},{121,232},{232,234},{234,134},{134,235},{235,371},{371,135},{232,371},{122,371},{235,236},{236,141},{141,248},{236,238},{238,407},{407,17},{238,237},{143,237},{237,239},{239,142},{239,147},{147,17},{237,245},{245,244},{244,408},{408,246},{245,242},{242,145},{242,243},{243,146},{146,144},{243,244},{243,73},{73,14},{246,14},{246,17},{17,247},{247,248},{247,249},{248,249},{249,394},{14,153},{153,15},{153,254},{254,255},{255,267},{254,256},{255,256},{255,267},{267,19},{256,257},{257,18},{257,16},{16,258},{258,152},{152,92},{258,259},{259,260},{260,151},{260,150},{260,372},{259,372},{372,253},{259,249},{253,251},{253,252},{252,149},{252,148},{148,377},{253,251},{252,251},{251,250},{250,380},{380,8},{250,262},{262,261},{261,98},{98,129},{261,139},{139,7},{7,386},{386,387},{387,388},{7,138},{138,390},{390,389},{138,379},{379,9},{9,254},{9,115},{267,409},{409,268},{268,269},{269,157},{269,159},{268,158},{158,159},{159,160},{160,21},{21,20},{160,161},{161,22},{22,162},{22,273},{22,274},{273,272},{272,24},{272,271},{271,270},{268,410},{410,270},{274,23},{23,76},{76,32},{76,156},{23,276},{276,25},{275,276},{276,277},{277,27},{277,279},{275,279},{279,163},{163,13},{279,265},{265,266},{266,13},{13,378},{378,263},{156,411},{411,263},{266,155},{155,264},{264,154},{154,164},{264,377},{264,12},{12,74},{12,11},{11,10},{270,169},{169,170},{170,183},{183,187},{187,280},{187,34},{34,185},{185,284},{284,188},{188,36},{36,280},{185,184},{184,171},{170,171},{184,72},{72,26},{276,26},{280,35},{35,281},{281,89},{89,182},{89,283},{283,282},{281,282},{284,294},{294,291},{291,285},{285,189},{189,26},{285,286},{286,287},{287,186},{186,28},{287,288},{288,29},{287,290},{290,289},{289,295},{291,295},{295,352},{352,396},{352,373},{373,298},{298,37},{37,191},{191,298},{373,69},{69,38},{38,353},{290,190},{190,376},{376,172},{172,168},{168,301},{301,39},{301,354},{288,93},{288,31},{31,30},{30,33},{33,354},{354,165},{301,165},{165,40},{165,71},{71,166},{166,359},{40,357},{357,41},{357,359},{359,356},{356,44},{356,42},{356,360},{360,375},{360,43},{375,75},{44,361},{361,374},{374,70},{361,362},{362,385},{385,365},{365,83},{83,401},{166,45},{71,167},{167,46},{167,173},{173,353},{353,47},{353,94},{282,79},{79,181},{79,180},{180,80},{80,193},{80,297},{297,179},{297,192},{192,196},{192,382},{382,291},{192,178},{178,296},{296,295},{296,351},{351,198},{198,207},{198,305},{305,77},{305,304},{304,54},{54,404},{404,177},{177,176},{176,302},{77,302},{94,404},{404,52},{52,96},{96,48},{96,95},{95,176},{96,175},{175,405},{405,49},{405,51},{51,50},{96,174},{174,53},{194,80},{194,109},{209,212},{212,221},{221,224},{193,210},{210,324},{324,226},{324,81},{81,218},{218,224},{224,223},{223,351},{223,215},{215,403},{403,307},{223,315},{315,222},{315,307},{307,308},{307,198},{308,67},{308,309},{309,55},{55,302},{53,309},{309,313},{313,65},{65,206},{230,321},{321,247},{321,320},{320,82},{320,319},{319,325},{215,251},{251,325},{325,253},{253,242},{242,403},{253,329},{329,398},{398,68},{310,313},{310,68},{398,78},{78,348},{348,60},{60,347},{78,311},{311,237},{311,59},{59,347},{347,206},{347,312},{312,240},{312,241},{241,61},{61,239},{206,205},{205,66},{66,346},{346,58},{346,203},{346,202},{202,201},{201,102},{201,343},{343,340},{340,56},{56,399},{343,344},{344,57},{57,238}
 };

		for (int[] edge : edgeSet) {
			graph.addEdge(nodeList.get(edge[0] - 1), nodeList.get(edge[1] - 1));
			graph.addEdge(nodeList.get(edge[1] - 1), nodeList.get(edge[0] - 1));
		}

		System.out.println("No. of edges: " + graph.edgeSet().size());
		System.out.println("No. of nodes: " + graph.vertexSet().size());

		return graph;
	}

	
	public static void setTargetAndSources(
			final AbstractBaseGraph<INode, ExtDWE> graph, Set<Long> sourceIDs,
			Set<Long> targetIDs, int maxTargetVal) {
		for (INode n : graph.vertexSet()) {
			if (sourceIDs.contains(n.getOsmId())) {
				n.setType(NODE_TYPE.SOURCE);
			}
			if (targetIDs.contains(n.getOsmId())) {
				n.setType(NODE_TYPE.TARGET);
				n.setPayoff(model.Configuration.randomGenerator
						.nextInt(maxTargetVal));
			}
		}
	}
	
	public static void setTargetAndSources(final AbstractBaseGraph<INode, ExtDWE> graph, int numSources,
			int numTargets, int maxTargetVal, Random random) {
		List<INode> nodeList = new ArrayList<INode>(graph.vertexSet());
		
		Collections.shuffle(nodeList, random);
		for ( int i = 0; i<numTargets; i++) {
			nodeList.get(i).setType(NODE_TYPE.TARGET);
			nodeList.get(i).setPayoff(random.nextInt(maxTargetVal));
		}
		for ( int i = numTargets; i<numTargets + numSources; i++) {
			nodeList.get(i).setType(NODE_TYPE.SOURCE);
		}
	}

	public static void setTargetAndSourcesBasedOnDegree(
			final AbstractBaseGraph<INode, ExtDWE> graph, int numSources,
			int numTargets, int maxTargetVal, Random random) {
		// find nodes with least outDegree and set as Sources
		List<INode> nodeList = new ArrayList<INode>(graph.vertexSet());

		// ascending order
		Collections.sort(nodeList, new Comparator<INode>() {
			@Override
			public int compare(INode arg0, INode arg1) {
				if (graph.outDegreeOf(arg0) < graph.outDegreeOf(arg1)) {
					return -1;
				} else
					return 1;
			}
		});

		System.out.println("Nodelist size: " + nodeList.size()
				+ ", numSources: " + numSources);

		for (int i = 0; i < numSources; i++) {
			nodeList.get(i).setType(NODE_TYPE.SOURCE);
		}

		// find nodes with maximum inDegree and set as Targets
		// descending order
		Collections.sort(nodeList, new Comparator<INode>() {
			@Override
			public int compare(INode arg0, INode arg1) {
				if (graph.inDegreeOf(arg0) > graph.inDegreeOf(arg1)) {
					return -1;
				} else
					return 1;
			}
		});
		for (int i = 0; i < numTargets; i++) {
			nodeList.get(i).setType(NODE_TYPE.TARGET);
			nodeList.get(i).setPayoff(random.nextInt(maxTargetVal));
		}

	}

	public static void removeNodesNorthEastOf(LatLon latLon, AbstractBaseGraph<INode, ExtDWE> graph) {
		// TODO Auto-generated method stub
		List<INode> removeNodeList = new ArrayList<INode>();
		for ( INode u : graph.vertexSet() ) {
			if (u.getLat() > latLon.getLat()) {
//				System.out.println(u.getLat() + ", " + u.getLon() + ", " + latLon.getLat() + ", " + latLon.getLon());
				removeNodeList.add(u);
			} else if (u.getLon() > latLon.getLon()) {
//				System.out.println(u.getLat() + ", " + u.getLon() + ", " + latLon.getLat() + ", " + latLon.getLon());
				removeNodeList.add(u);
			}

		}
		graph.removeAllVertices(removeNodeList);		
	}

}
