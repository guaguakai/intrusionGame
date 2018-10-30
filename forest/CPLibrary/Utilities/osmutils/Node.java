package osmutils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osm.data.coordinates.LatLon;



/**
 * This class represents general {@link Graph} node.
 *
 * @author Libor Wagner
 *
 */
public class Node implements Comparable<Node>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -6890944062990244443L;

	/** Unique id. */
	private String id;

	/** Position on the globe. */
	protected LatLon latLon;

	/** List of incoming edges to this node by opposite node. */
	private Map<String, Edge> incomingEdges;

	/** List of outgoing edges from this node by opposite node. */
	private Map<String, Edge> outgoingEdges;


	/**
	 * Create new instance of node.
	 *
	 * @param id Id of new node.
	 * @param latLon Position of new node.
	 * @param labels Labels for this nodes.
	 */
	public Node(String id, LatLon latLon) {
		this.id = id;
		this.latLon = latLon;
		this.outgoingEdges = new HashMap<String, Edge>(2);
		this.incomingEdges = new HashMap<String, Edge>(2);

	}

	/** @return Unique i.d */
	public String getId() {
		return id;
	}

	/** @return Map of outgoing edges  by opposite node. */
	public Map<String, Edge> getOutgoingEdges() {
		return outgoingEdges;
	}

	/** @return Map of incoming edges  by opposite node. */
	public Map<String, Edge> getIncomingEdges() {
		return incomingEdges;
	}

	/** @return Position on the globe. */
	public LatLon getLatLon() {
		return latLon;
	}

	/** @return List of nodes which are connected with this node by outgoing edge. */
	public List<Node> getNeighbours() {
		List<Node> neighbours = new LinkedList<Node>();

        for (Edge edge : outgoingEdges.values()) {
            neighbours.add(edge.getTo());
        }

        return neighbours;
	}



	
	public boolean equals(Object obj) {
		if (obj instanceof Node) {
			return getId().equals(((Node)obj).getId());
		} else {
			return false;
		}
	}

	
	public int hashCode() {
		return id.hashCode();
	}

	
	public String toString() {
		return "n["+getId()+"]";
	}

	
	public int compareTo(Node o)
	{
	    return Integer.parseInt(getId()) - Integer.parseInt(o.getId());
	}

}
