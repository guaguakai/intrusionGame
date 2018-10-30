package osm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.MyLink;
import model.graphutils.MyNode;
import model.graphutils.Node;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import osmutils.ExtNode;
import osmutils.ExtPath;
import osmutils.GraphBuilder;
import edu.uci.ics.jung.graph.AbstractGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class GraphAdapter {

	private GraphBuilder osmGraphBuilder;
	private int nodeIndex = 2;
	private int edgeIndex = 1;

	private AbstractGraph<INode, ExtDWE> graph;
	private Map<Integer, INode> nodes;
	private Map<Integer, MyLink> edges;

	private Map<ExtDWE, ExtPath> edgeMapping = new HashMap<ExtDWE, ExtPath>();
	// private Map<MyNode,ExtNode> nodeMapping = new HashMap<MyNode, ExtNode>();
	/**
	 * Maps from original OSM ids to our ids (e.g. 457439875394 -> 12) and from
	 * our ids to OSM as well (eq. 12 -> 457439875394)!! There should be no
	 * overlap, the OSM ids have 8nr. digits.
	 */
	private Map<Integer, Integer> nodeOSMIDMapping = new HashMap<Integer, Integer>();
	// private boolean filter =false;
	private INode virtualSource;
	private List<INode> targets = new ArrayList<INode>();
	private List<INode> sources = new ArrayList<INode>();

	private List<INode> usTargets = new ArrayList<INode>();
	private List<INode> usSources = new ArrayList<INode>();

	private Map<Integer, INode> reversedUSMapping;
	private Map<Integer, INode> nodeUSMapping;

	/**
	 * @param builder
	 *            The builder has to be initialized and has to contain the
	 *            parsed graph.
	 * @param filterNodes
	 *            <code>true</code> if the nodes that has only one successor and
	 *            one predecessor should be filtered. <code>false</code> if not.
	 */
	public GraphAdapter(GraphBuilder builder/* boolean filterNodes */) {
		if (builder == null) {
			throw new NullPointerException("Builder cannot be null!!!");
		}
		this.osmGraphBuilder = builder;
		// this.filter = filterNodes;

		if (testNodesOK()) {
			createGraph();
		}
	}

	/**
	 * Creates nodes and edges.
	 */
	private void createGraph() {
		nodes = fillNodes();
		edges = fillEdges(nodes);
		System.out.println("Number of unfiltered nodes: " + nodes.size());
		System.out.println("Number of unfiltered edges: " + edges.size());

		graph = refreshGraph(nodes, edges);

	}

	/**
	 * Removes all nodes with degree 2 (resp. 4) and redirects the edges.
	 * 
	 * @param nodes
	 * @param edges
	 * @return
	 */
	public AbstractGraph<INode, ExtDWE> filterGraph() {
		Map<INode, List<ExtDWE>> outEdges = new HashMap<INode, List<ExtDWE>>();

		outEdges = getAllOutEdges();

		List<INode> nodesToRemove = new LinkedList<INode>();		

//		// Remove Nodes of Degree One
//		for (INode node : nodes.values()) { // all nodes with degree 2
//			if (outEdges.get(node).size() == 1) {
//				if (!sources.contains(node) && !targets.contains(node)) {
//					// but not the targets or sources!
//					nodesToRemove.add(node);
//				}
//			}
//		}
//		System.out.println("Nodes of degree 1 found: " + nodesToRemove.size());
//		for (INode node : nodesToRemove) {
//			ExtDWE edge = outEdges.get(node).get(0);
//			assert(edge.getsource() == node);
//			INode v = edge.gettarget();
//			List<MyLink> inEs = getInEdges(v);
//			// remove out edge of node
//			for ( MyLink link : inEs ) {
//				if ( link.source == node) {
//					edges.remove(link.getId());
//					break;
//				}
//			}
//			// remove in edges of node
//			inEs = getInEdges(node);
//			for ( MyLink link : inEs) {
//				edges.remove(link.getId());
//			}			
//			// remove node itself
//			nodes.remove(node.getId());
//		}
//		graph = refreshGraph(nodes, edges);
				
		// Remove nodes of degree 2
		outEdges = getAllOutEdges();
		nodesToRemove.clear();
		List<ExtDWE> edgesToRemove = new LinkedList<ExtDWE>();
		for (INode node : nodes.values()) { // all nodes with degree 2
			if (outEdges.get(node).size() == 2) {
				if (!sources.contains(node) && !targets.contains(node)) {
					// but not the targets or sources!
					nodesToRemove.add(node);
				}
			}
		}
		System.out.println("Nodes of degree 2 found: " + nodesToRemove.size());

		for (INode node : nodesToRemove) {
			List<MyLink> outEs = getOutEdges(node); // get in and out edges
			List<MyLink> inEs = getInEdges(node);

			// System.out.println("Node removed: "+node.id);
			nodes.remove(node.getId()); // remove this node

			boolean cycle = true;
			for (MyLink outlink : outEs) { // lengthen out edges
				for (MyLink inlink : inEs) {
					if (!outlink.target.equals(inlink.source)) {
						outlink.source = inlink.source;
						edgesToRemove.add(inlink);
						cycle = false;
					}
				}
			}
			if (cycle) { // if this is a cycle...
				for (MyLink link : outEs) { // remove out edges
					edges.remove(link.getId());
				}
			}
			for (MyLink link : inEs) { // remove in edges
				edges.remove(link.getId());
			}
		}

		graph = refreshGraph(nodes, edges);
		
		System.gc();
		return graph;
	}

	/**
	 * If there are multiple edges between two vertices, remove all but one.
	 * 
	 * @return
	 */
	public AbstractGraph<INode, ExtDWE> mergeMultipleEdges() {

		Map<INode, List<ExtDWE>> outEdges = new HashMap<INode, List<ExtDWE>>();

		outEdges = getAllOutEdges(); // get all edges from given node.

		Map<INode, List<ExtDWE>> duplicate = new HashMap<INode, List<ExtDWE>>();

		for (Entry<INode, List<ExtDWE>> entry : outEdges.entrySet()) { // find
																		// duplicate
																		// edges
			INode node = entry.getKey();
			List<ExtDWE> edges = entry.getValue();

			for (ExtDWE l1 : edges) {
				for (ExtDWE l2 : edges) { // edges are duplicate if the id is
											// diff, but target is the same
					if (l1.getId() != l2.getId()
							&& graph.getDest(l1).getId() == graph.getDest(l2)
									.getId()) {
						List<ExtDWE> list = duplicate.get(node);
						if (list == null)
							list = new ArrayList<ExtDWE>();
						list.add(l2);
						duplicate.put(node, list);
					}
				}
			}
		}

		// remove all but the first one from duplicate edges
		for (List<ExtDWE> list : duplicate.values()) {
			for (int i = 1; i < list.size(); i++) { // leave the first one,
													// remove the rest with no
													// remorse.
				edges.remove(list.get(i).getId());
			}
		}

		graph = refreshGraph(nodes, edges);
		return graph;

	}

	private List<MyLink> getInEdges(INode node) {
		List<MyLink> in = new ArrayList<MyLink>();

		for (MyLink link : edges.values()) {
			if (link.target.equals(node)) {
				in.add(link);
			}
		}
		return in;
	}

	private List<MyLink> getOutEdges(INode node) {
		List<MyLink> out = new ArrayList<MyLink>();

		for (MyLink link : edges.values()) {
			if (link.source.equals(node)) {
				out.add(link);
			}
		}
		return out;
	}

	private Map<INode, List<ExtDWE>> getAllOutEdges() {
		Map<INode, List<ExtDWE>> outEdges = new HashMap<INode, List<ExtDWE>>();
		for (ExtDWE link : edges.values()) {
			INode s = graph.getSource(link);

			if (!outEdges.containsKey(s)) {
				outEdges.put(s, new LinkedList<ExtDWE>());
			}
			outEdges.get(s).add(link);
		}
		return outEdges;
	}

	private AbstractGraph<INode, ExtDWE> refreshGraphBackup(
			Map<Integer, INode> nodes, Map<Integer, MyLink> edges) {
		AbstractGraph<INode, ExtDWE> graph = new DirectedSparseMultigraph<INode, ExtDWE>();
		if (nodes.size() == 0) {
			throw new RuntimeException("Number of nodes is 0!!!");
		}
		for (INode node : nodes.values()) {
			graph.addVertex(node);
			// System.out.println("Vertex added: "+node);
		}
		for (MyLink edge : edges.values()) {

			if (edge.source == null || edge.target == null) {
				System.err.println("Graph structure is inconsistent!");
				continue;
			}
			graph.addEdge(edge, edge.source, edge.target, EdgeType.DIRECTED);
		}
		// System.out.println("Edge added: "+edge);

		return graph;
	}

	private AbstractGraph<INode, ExtDWE> refreshGraph(
			Map<Integer, INode> nodes, Map<Integer, MyLink> edges) {
		AbstractGraph<INode, ExtDWE> graph = new DirectedSparseMultigraph<INode, ExtDWE>();
		if (nodes.size() == 0) {
			throw new RuntimeException("Number of nodes is 0!!!");
		}
		for (INode node : nodes.values()) {
			graph.addVertex(node);
			// System.out.println("Vertex added: "+node);
		}
		for (MyLink edge : edges.values()) {

			if (edge.source == null || edge.target == null) {
				System.err.println("Graph structure is inconsistent!");
				continue;
			}
			graph.addEdge(edge, edge.source, edge.target, EdgeType.DIRECTED);
		}
		// System.out.println("Edge added: "+edge);
		return graph;
	}

	/**
	 * Test for integer IDs.
	 * 
	 * @return
	 */
	private boolean testNodesOK() {
		for (ExtNode node : osmGraphBuilder.getNodes()) {

			try {
				int id = Integer.parseInt(node.getId());
				id++;
			} catch (Exception e) {
				System.out.println("Wrong id: " + node.getId());
				System.out.println("Nodes are bad.");
				return false;
			}
		}
		System.out.println("Nodes are OK.");
		return true;
	}

	/**
	 * Call {@link GraphAdapter#fillNodes()} before!
	 * 
	 * @param nodes
	 * @return
	 */
	private Map<Integer, MyLink> fillEdgesBackup(Map<Integer, INode> nodes) {
		Map<Integer, MyLink> edges = new HashMap<Integer, MyLink>();

		/** Code by manish to disable some edge types */
		String[] HW_TYPES = { "primary", "primary_link", "secondary",
				"secondary_link"
		// , "tertiary" , "tertiary_link"
		// , "trunk" , "trunk_link"
		// , "motorway" , "motorway_link"
		// , "unclassified"
		};
		Set<String> HIGHWAY_TYPES = new HashSet<String>(Arrays.asList(HW_TYPES));

		for (ExtPath edge : osmGraphBuilder.getPaths()) {

			boolean addEdge = false;
			for (Tag t : edge.getTags()) {
				if (t.getKey() == "highway") {
					if (HIGHWAY_TYPES.contains(t.getValue())) {
						addEdge = true;
						break;
					}
				}
			}
			if (!addEdge) {
				continue;
			}

			List<ExtNode> vertices = edge.getNodes();
			Integer v1ID = Integer.valueOf(vertices.get(0).getId());
			Integer v2ID = Integer.valueOf(vertices.get(1).getId());
			INode source = nodes.get(nodeOSMIDMapping.get(v1ID));
			INode target = nodes.get(nodeOSMIDMapping.get(v2ID));

			if (source == null || target == null) {
				System.err
						.println("Something is wrong, there is no source and target for edge"
								+ edge);
			}

			MyLink link = new MyLink(edgeIndex, 1.0, 1.0, (MyNode) source,
					(MyNode) target);
			edges.put(edgeIndex, link);
			edgeMapping.put(link, edge);
			edgeIndex++;

			MyLink link2 = new MyLink(edgeIndex, 1.0, 1.0, (MyNode) target,
					(MyNode) source);
			edges.put(edgeIndex, link2);
			edgeMapping.put(link2, edge);
			edgeIndex++;
		}

		return edges;
	}

	private Map<Integer, MyLink> fillEdges(Map<Integer, INode> nodes) {
		Map<Integer, MyLink> edges = new HashMap<Integer, MyLink>();
		for (ExtPath edge : osmGraphBuilder.getPaths()) {

			List<ExtNode> vertices = edge.getNodes();
			Integer v1ID = Integer.valueOf(vertices.get(0).getId());
			Integer v2ID = Integer.valueOf(vertices.get(1).getId());
			INode source = nodes.get(nodeOSMIDMapping.get(v1ID));
			INode target = nodes.get(nodeOSMIDMapping.get(v2ID));

			if (source == null || target == null) {
				System.err
						.println("Something is wrong, there is no source and target for edge"
								+ edge);
			}

			MyLink link = new MyLink(edgeIndex, 1.0, 1.0, (MyNode) source,
					(MyNode) target);
			edges.put(edgeIndex, link);
			edgeMapping.put(link, edge);
			edgeIndex++;

			MyLink link2 = new MyLink(edgeIndex, 1.0, 1.0, (MyNode) target,
					(MyNode) source);
			edges.put(edgeIndex, link2);
			edgeMapping.put(link2, edge);
			edgeIndex++;

		}
		return edges;
	}

	private Map<Integer, INode> fillNodes() {
		Map<Integer, INode> nodes = new HashMap<Integer, INode>();

		for (ExtNode node : osmGraphBuilder.getNodes()) {
			int id = -1;
			try {
				id = Integer.parseInt(node.getId());
			} catch (Exception e) {
				System.err.println("Node id parsing gone bad: " + node.getId());
			}

			MyNode myNode = new MyNode(nodeIndex, 0);
			myNode.setLatLon(node.getLatLon().getYCoord(), node.getLatLon()
					.getXCoord());
			myNode.setOSMID(id);

			nodes.put(nodeIndex, myNode);
			// nodeMapping.put(myNode,node);
			nodeOSMIDMapping.put(id, nodeIndex);
			if (nodeOSMIDMapping.containsKey(nodeIndex)) {
				System.err.println("Duplicate nodeINDEX!!!");
			}
			nodeOSMIDMapping.put(nodeIndex, id);
			nodeIndex++;
		}
		return nodes;
	}

	/**
	 * @return the graph
	 */
	public AbstractGraph<INode, ExtDWE> getGraph() {

		return graph;
	}

	/**
	 * @return the nodes
	 */
	public Map<Integer, INode> getNodes() {
		return nodes;
	}

	/**
	 * @return the edges
	 */
	public Map<Integer, MyLink> getEdges() {
		return edges;
	}

	/**
	 * @return the osmGraphBuilder
	 */
	public GraphBuilder getOsmGraphBuilder() {
		return osmGraphBuilder;
	}

	/**
	 * @return the edgeMapping
	 */
	public Map<ExtDWE, ExtPath> getEdgeMapping() {
		return edgeMapping;
	}

	/**
	 * @return the nodeIDMapping
	 */
	public Map<Integer, Integer> getNodeOSMIDMapping() {
		return nodeOSMIDMapping;
	}

	public List<INode> initTargets(int[] targetIds) throws Exception {
		int nextInt = model.Configuration.randomGenerator.nextInt();
		System.out.println("Random seed : " + nextInt);
		for (Integer id : targetIds) {

			INode targetNode = nodes.get(getNodeOSMIDMapping().get(id));
			if (targetNode == null) {
				throw new Exception(
						"Target initialization failed. There are no such nodes!");
			}
			if (targetNode instanceof MyNode) {
				int reward = model.Configuration.randomGenerator.nextInt(100);
				((MyNode) targetNode).freward = reward;
				System.out.println("Target val set to: " + reward);
			}
			targets.add(targetNode);
		}
		System.out.println("Targets initialised: " + targets);

		return targets;
	}

	public List<INode> initTargets(int[] targetIds, int[] rewards)
			throws Exception {
		if (targetIds.length != rewards.length)
			throw new IllegalArgumentException(
					"targetIds and rewards must have the same length!");
		int nextInt = model.Configuration.randomGenerator.nextInt();
		System.out.println("Random seed : " + nextInt);
		for (int i = 0; i < targetIds.length; i++) {
			int id = targetIds[i];
			int reward = rewards[i];
			INode targetNode = nodes.get(getNodeOSMIDMapping().get(id));
			if (targetNode == null) {
				throw new Exception(
						"Target initialization failed. There are no such nodes!");
			}
			if (targetNode instanceof MyNode) {
				// int reward = r2.nextInt(100);
				((MyNode) targetNode).freward = reward;
				System.out.println("Target val set to: " + reward);
			}
			targets.add(targetNode);
		}
		System.out.println("Targets initialised: " + targets);

		return targets;
	}

	public List<INode> initTargets2(int[] targetIds) throws Exception {
		int nextInt = model.Configuration.randomGenerator.nextInt();

		System.out.println("Random seed : " + nextInt);
		for (Integer id : targetIds) {

			INode targetNode = nodes.get(getNodeOSMIDMapping().get(id));

			if (targetNode instanceof MyNode) {
				if (targetNode.getId() == 17) {
					((MyNode) targetNode).freward = 598;
					System.out.println("Target" + targetNode.getId()
							+ " val set to: " + 598);
				} else {
					((MyNode) targetNode).freward = 404;
					System.out.println("Target" + targetNode.getId()
							+ " val set to: " + 404);
				}
			}
			targets.add(targetNode);
		}
		System.out.println("Targets initialised: " + targets);

		return targets;
	}

	public List<INode> initSources(int[] sourceIds) throws Exception {
		for (Integer sourceId : sourceIds) {
			for (INode node : nodes.values()) {
				if (((MyNode) node).getOsmId() == sourceId) {
					sources.add(node);
				}
			}
			// INode sourceNode =
			// nodes.get(getNodeOSMIDMapping().get(sourceId));
			// if(sourceNode==null ){
			// throw new
			// Exception("Source initialization failed. There are no such nodes!");
			// }

		}

		System.out.println("Sources initialised: " + sources);
		// virtualSource = addVirtualSource();
		return sources;

	}

	/**
	 * Adds virtual source to real ones.
	 * 
	 * @param graph
	 * @param sources
	 * @return
	 */
	public INode addVirtualSource() {
		INode virtualSource = new MyNode(1);
		graph.addVertex(virtualSource);

		for (INode source : sources) {
			ExtDWE edge = new MyLink(edgeIndex, 0, 0, (MyNode) virtualSource,
					(MyNode) source);
			edgeIndex++;
			if (graph.getEdges().contains(edge)) {
				System.out
						.println("Error when adding virtual source. Duplicate sources?");
			}
			graph.addEdge(edge, virtualSource, source, EdgeType.DIRECTED);
		}
		this.virtualSource = virtualSource;
		return virtualSource;
	}

	public INode getVirtualSource() {
		return virtualSource;
	}

	public List getTargets() {
		return targets;
	}

	/**
	 * @return the sources
	 */
	public List<INode> getSources() {
		return sources;
	}

	public AbstractBaseGraph<INode, ExtDWE> getUSGraph() {
		DirectedWeightedMultigraph<INode, ExtDWE> usgraph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		ExtDWE.reset();

		nodeUSMapping = new HashMap<Integer, INode>();
		reversedUSMapping = new HashMap<Integer, INode>();
		System.out.println("====Graph conversion started!=====");
		System.out.println("Original # of nodes: " + nodes.size());
		System.out.println("Original # of edges: " + edges.size());
		for (INode node : nodes.values()) {
			NODE_TYPE type = NODE_TYPE.INTERMEDIATE;
			int payoff = 0;

			if (targets.contains(node)) {
				type = NODE_TYPE.TARGET;
				payoff = node.getPayoff();

			}

			Node newnode = new Node(type, payoff);
			newnode.setOsmId(((MyNode) node).getOsmId());
			if (newnode.getType() == NODE_TYPE.TARGET)
				usTargets.add(newnode);
			newnode.setLatLon(node.getLat(), node.getLon());
			nodeUSMapping.put(node.getId(), newnode);
			reversedUSMapping.put(newnode.getId(), node);
			usgraph.addVertex(newnode);
		}
		for (MyLink link : edges.values()) {
			INode source = nodeUSMapping.get(link.source.getId());
			INode target = nodeUSMapping.get(link.target.getId());
			usgraph.addEdge(source, target);
		}

		INode virtualSourceNode = new Node(NODE_TYPE.SOURCE, 0);
		virtualSourceNode.setLatLon(72.8030821, 18.9048225);
		usgraph.addVertex(virtualSourceNode);

		for (INode source : sources) {
			INode newsource = nodeUSMapping.get(source.getId());
			usSources.add(newsource);
			ExtDWE edge = usgraph.addEdge(virtualSourceNode, newsource);
			edge.setType(EDGE_TYPE.VIRTUAL);
		}
		System.out.println("Converted # of nodes: " + nodes.size());
		System.out.println("Converted # of edges: " + edges.size());

		System.out.println(nodeUSMapping);
		
		return usgraph;

	}

	public static DirectedSparseMultigraph<MyNode, MyLink> convert(
			AbstractBaseGraph<INode, ExtDWE> graph) {
		ExtDWE.reset();
		DirectedSparseMultigraph<MyNode, MyLink> newgraph = new DirectedSparseMultigraph<MyNode, MyLink>();

		Map<INode, MyNode> nodeMapping = new HashMap<INode, MyNode>();
		for (INode node : graph.vertexSet()) {
			MyNode n = new MyNode(node.getId(), node.getPayoff());
			n.setLatLon(node.getLat(), node.getLon());
			newgraph.addVertex(n);
			nodeMapping.put(node, n);
		}

		int edgeId = 0;

		for (ExtDWE edge : graph.edgeSet()) {
			// create directed edge for

			MyNode source = nodeMapping.get(graph.getEdgeSource(edge));
			MyNode target = nodeMapping.get(graph.getEdgeTarget(edge));

			MyLink link = new MyLink(edgeId, 0, 0, source, target);
			newgraph.addEdge(link, source, target, EdgeType.DIRECTED);
			edgeId++;

			link = new MyLink(edgeId, 0, 0, target, source);
			newgraph.addEdge(link, target, source, EdgeType.DIRECTED);
			edgeId++;
		}
		return newgraph;
	}

	/**
	 * @return the nodeUSMapping
	 */
	public Map<Integer, INode> getReversedNodeUSMapping() {
		return reversedUSMapping;
	}

	/**
	 * @return the nodeUSMapping
	 */
	public Map<Integer, INode> getNodeUSMapping() {
		return nodeUSMapping;
	}

	public void initSourcesRandomly(int sourceNo) throws Exception {
		if (sources == null)
			sources = new ArrayList<INode>();
		else
			sources.clear();

		List<INode> nodesList = new ArrayList<INode>(nodes.values());
		for (int i = 0; i < sourceNo; i++) {
			INode sourceNode = nodesList
					.get(model.Configuration.randomGenerator.nextInt(nodesList
							.size() - 1));

			if (targets.contains(sourceNode) || sources.contains(sourceNode)) {
				i--;
				continue;
			}
			if (sourceNode == null) {
				throw new Exception(
						"Source initialization failed. There are no such nodes!");
			}
			sources.add(sourceNode);
		}
		System.out.println("Sources initialised: " + sources);
	}

	public void initTargetsRandomly(int targetsNo, int maxPayoff)
			throws Exception {
		if (targets == null)
			targets = new ArrayList<INode>();
		else
			targets.clear();
		List<INode> nodesList = new ArrayList<INode>(nodes.values());
		for (int i = 0; i < targetsNo; i++) {

			INode targetNode = nodesList
					.get(model.Configuration.randomGenerator.nextInt(nodesList
							.size()));
			if (sources.contains(targetNode) || targets.contains(targetNode)) {
				i--;
				continue;
			}
			if (targetNode == null) {
				throw new Exception(
						"Target initialization failed. There are no such nodes!");
			}
			if (targetNode instanceof MyNode) {
				int payoff = model.Configuration.randomGenerator
						.nextInt(maxPayoff);
				((MyNode) targetNode).freward = payoff;
				System.out.println("Target OSM ID: "
						+ ((MyNode) targetNode).getOsmId() + " payoff: "
						+ payoff);
			}
			targets.add(targetNode);
		}
		System.out.println("Targets initialised: " + targets);
	}

	/**
	 * @return the usTargets
	 */
	public List<INode> getUsTargets() {
		return usTargets;
	}

	/**
	 * @return the usSources
	 */
	public List<INode> getUsSources() {
		return usSources;
	}

	public void getTransitGraph() {

		filterGraph();
		AbstractBaseGraph<INode, ExtDWE> usGraph = getUSGraph();

	}
}
