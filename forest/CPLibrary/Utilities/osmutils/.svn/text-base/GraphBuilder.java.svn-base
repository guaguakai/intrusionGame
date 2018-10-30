package osmutils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.TagCollectionImpl;



/**
 * This is graph builder based on {@link Node} and {@link Way}. It is used to
 * preprocess OSM data.
  * @author Libor Wagner
 * 
 */
public class GraphBuilder extends DataContainer {

	public GraphBuilder() {
		super();
	}

	/**
	 * Add node and snap it to the nearest edge. All nodes which are allready
	 * present are rejected.
	 * 
	 * @param node
	 */
	public void addAndSnap(ExtNode node) {

		// Check
		if (nodesById.containsKey(node.getId()))
			return;

		// Get nearest path builder.
		ExtPath path = getNearestPath(node.getLatLon());

		// Remove path
		remove(path);

		// Add node to projected position
		path.addOnProjection(node);

		// Divide path.
		ExtPath other = path.divide(node);

		add(path);
		add(other);
	}

	private ExtPath getNearestPath(LatLon point) {
		return getNearestPath(point, Double.MAX_VALUE);
	}
	
	
	private ExtPath getNearestPath(LatLon point, double radius) {

		ExtPath nearest = null;
		double distance = radius;

		for (ExtPath path : pathsById.values()) {

			double dist = path.distance(point);

			if (distance > dist) {
				distance = dist;
				nearest = path;
			}
		}

		return nearest;
	}

	/**
	 * Adds node into graph and connects it with nodes in radius.
	 * 
	 * @param node
	 * @param radius
	 * @param tags Tags for connection;
	 */
	public void addAndConnect(ExtNode node, double radius, Collection<Tag> tags) {

		// Check
		if (nodesById.containsKey(node.getId()))
			return;

		// Get nodes in radius
		List<ExtNode> nodes = getNodesInRadius(node.getLatLon(), radius);

		// Add edge from node to each node in radius.
		for (ExtNode node2 : nodes) {

			// Create new path
			ExtPath path = new ExtPath(node2, node);

			path.setTags(tags);

			// Add conection into graph.
			add(path);
		}

		nodesById.put(node.getId(), node);
	}

	public void addAndConnectToNearestPath(ExtNode node, Collection<Tag> tags) {

		addAndConnectToNearestPath(node, Double.MAX_VALUE, tags);
	}
	
	public void addAndConnectToNearestPath(ExtNode node, double radius, Collection<Tag> tags) {

		// Check
		if (!nodesById.containsKey(node.getId())) {

			// Create node
			ExtNode crossing = new ExtNode("p" + node.getId(), node.getLatLon());
			
			// Get nearest edge
			ExtPath path = getNearestPath(node.getLatLon(), radius);
			
			if (path == null) {
				System.out.println("Can't connect to to nearest edge!! "+node);
				for (Tag tag : node.getTags()) {
					System.out.println("  "+tag);
				}
				
				return;
			}

			// Remove path
			remove(path);

			// Add on projection
			path.addOnProjection(crossing);

			// Divide path.
			ExtPath other = path.divide(crossing);

			add(path);
			add(other);

			// Add path conectiong crossing and
			ExtPath connection = new ExtPath(crossing, node);
			connection.setTags(tags);

			add(connection);
		}
	}

	public void addAndConnectToNearestNode(ExtNode node, Collection<Tag> tags) {

		// Check
		if (!nodesById.containsKey(node.getId())) {

			// Get nearest node
			ExtNode nearest = getNearestNode(node.getLatLon());

			// Add path conectiong crossing and
			ExtPath connection = new ExtPath(nearest, node);
			connection.setTags(tags);

			add(connection);
		}

	}

	public ExtNode getNearestNode(LatLon point) {

		ExtNode nearest = null;
		double mindist = Double.MAX_VALUE;

		for (ExtNode node : getNodes()) {
			double dist = LatLon.distanceInMeters(point, node.getLatLon());
			if (dist < mindist) {
				nearest = node;
				mindist = dist;
			}
		}

		return nearest;

	}

	/**
	 * Returns all nodes within are given by center and radius.
	 * 
	 * @param center
	 *            Center of the area.
	 * @param radius
	 *            Radius from the center.
	 * @return Nodes in radius.
	 */
	public List<ExtNode> getNodesInRadius(LatLon center, double radius) {

		List<ExtNode> inRadius = new LinkedList<ExtNode>();

		for (ExtNode node : nodesById.values()) {
			if (LatLon.distanceInMeters(center, node.getLatLon()) <= radius)
				inRadius.add(node);
		}

		return inRadius;
	}

	public void relaxe(TagFilter filter) {

		List<ExtNode> nodes = new LinkedList<ExtNode>();

		for (ExtNode node : nodesById.values()) {

			List<ExtPath> paths = getPathsByNodeId(node.getId());

			// Check if no intersection
			if (paths.size() == 2 && !filter.allow(node.getTags())) {
				ExtPath path1 = paths.get(0);
				ExtPath path2 = paths.get(1);

				remove(path1);
				remove(path2);

				path1.connect(path2, node);

				add(path1);

				nodes.add(node);
			}

		}

	}

	public GraphBuilder extractLargestConnectedComponent() {

		GraphBuilder largest = new GraphBuilder();
		Set<String> visited = new HashSet<String>();

		for (ExtNode node : getNodes()) {
			if (!visited.contains(node.getId())) {
				// visited.add(node.getId());
				GraphBuilder test = new GraphBuilder();
				Queue<ExtNode> open = new LinkedList<ExtNode>();

				open.add(node);

				while (!open.isEmpty()) {

					ExtNode curr = open.poll();

					if (!visited.contains(curr.getId())) {
						visited.add(curr.getId());

						// Expand
						List<ExtPath> paths = getPathsByNodeId(curr.getId());

						for (ExtPath path : paths) {

							test.add(path);

							if (path.getFrom().equals(curr)) {
								open.add(path.getTo());
							} else {
								open.add(path.getFrom());
							}
						}
					}
				}

				if (largest.getElementCount() < test.getElementCount()) {
					largest = test;
				}
			}
		}

		replace(largest);

		return this;

	}

	public void collapseEdges(double minLenght, TagFilter filter) {

		DisjointSets<ExtNode> disjointSets = new DisjointSets<ExtNode>();
		List<ExtPath> shorts = new LinkedList<ExtPath>();

		for (ExtPath path : getPaths()) {
			if ( (!(filter != null) || filter.allow(path.getTags())) && path.getLength() < minLenght) {
				disjointSets.union(path.getFrom(), path.getTo());
				shorts.add(path);
			}
		}

		// Find connected components.

		for (List<ExtNode> list : disjointSets.getDisjointSets()) {

			collapseNodes(list);
		}
	}

	public void collapseNodes(List<ExtNode> nodes) {

		// Set of edges that will remain and will lead to new collapsed node
		Set<ExtPath> remain = new HashSet<ExtPath>();

		// Find all remaining edges and find centroid;
		double lat = 0;
		double lon = 0;
		
		for (ExtNode node : nodes) {
			lat += node.getLatLon().lat();
			lon += node.getLatLon().lon();
			
			for (ExtPath path : getPathsByNodeId(node.getId())) {
				if (!(nodes.contains(path.getFrom()) && nodes.contains(path.getTo()))) {
					remain.add(path);
				} 
			}
		}
		
		Collection<Tag> tags = new TagCollectionImpl();
		
		String newId = "";
		for (ExtNode node : nodes) {
			OsmTools.mergeTags(tags, node.getTags());
			remove(node);
			newId += node.getId()+",";
		}
		newId = newId.substring(0, newId.length() - 1);
		
		LatLon centroid = new LatLon(lat/nodes.size(), lon/nodes.size());
		ExtNode node = new ExtNode(newId, centroid);
		node.setTags(tags);
		add(node);
		
		for (ExtPath path : remain) {
			LinkedList<ExtNode> pathNodes;
			
			if (nodes.contains(path.getTo())) {
				
				pathNodes = path.nodes;
				pathNodes.set(pathNodes.indexOf(path.getTo()), node);
				
				
			} else {
				pathNodes = path.nodes;
				pathNodes.set(pathNodes.indexOf(path.getFrom()), node);
			}
			
			ExtPath newPath = new ExtPath(pathNodes);
			newPath.setTags(path.getTags());
			add(newPath);
		}
		
	}

	public Collection<ExtNode> getNodes() {
		return nodesById.values();
	}

	public Collection<ExtPath> getPaths() {
		return pathsById.values();
	}

}
