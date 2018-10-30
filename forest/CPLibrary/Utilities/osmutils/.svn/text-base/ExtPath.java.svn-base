package osmutils;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;



public class ExtPath extends Path implements Primitive {
		
	private static final long serialVersionUID = -2921975471119778720L;

	/** Array of nodes in this way. */ 
	protected LinkedList<ExtNode> nodes;
	
	protected Collection<Tag> tags;
	
	public Collection<Tag> getTags() {
		return tags;
	}

	public void setTags(Collection<Tag> tags) {
		this.tags = tags;
	}

	/**
	 * Creates new instance of way from given nodes. 
	 * 
	 * @param nodes Nodes of the way
	 */
	public ExtPath(LinkedList<ExtNode> nodes) {
		super(nodes.getFirst(), nodes.getLast(), new LatLon[] {nodes.getFirst().getLatLon(), nodes.getLast().getLatLon()});
		this.nodes = nodes;
		this.tags = new LinkedList<Tag>();
	}
	
	public ExtPath(ExtNode from, ExtNode to) {
		super(from, to, new LatLon[] {from.getLatLon(), to.getLatLon()});
		nodes = new LinkedList<ExtNode>();
		nodes.add(from);
		nodes.add(to);
		this.tags = new LinkedList<Tag>();
	}
	
	/** @return Unique id. */
	public String getId() {
		return getFrom().getId()+">"+getTo().getId();
 	}
	
	/**
	 * Get first node of this way.
	 * @return First node.
	 */
	public ExtNode getFrom() {
		return nodes.getFirst();
	}
	
	/**
	 * Get last node of this way.
	 * @return Last node.
	 */
	public ExtNode getTo() {
		return nodes.getLast();
	}
	
	
	public List<ExtNode> getNodes() {
		return nodes;
	}
	
	public double distance(LatLon point) {
		double distance = Double.MAX_VALUE;
		LatLon projection = null;
		
		LatLon start = null;
		for (ExtNode node : nodes) {
			LatLon end = node.getLatLon();
			
			if (start != null) {
				LatLon p = project(point, start, end);
				// Check optimality.
				if ((p != null) && (point.distance(p) < distance)) {
					projection = p;
					distance = projection.distance(point);
				}
			}
			
			start = end;
		}
		
		if (projection != null)
			return LatLon.distanceInMeters(point, projection);
		else 
			return Double.POSITIVE_INFINITY;
	}
	
	public LatLon project(LatLon point, LatLon start, LatLon end) {
		// Check input
		if (start.lat() == end.lat() && start.lon() == end.lon()) {
			System.err.println("The path contains nodes on the same spot!!!");
			return null;
		}

		// Compute length of edge.
		double length = start.distance(end);

		// Compute u
		double u = ((point.lat() - start.lat()) * (end.lat() - start.lat()) + (end
				.lon() - start.lon())
				* (point.lon() - start.lon()))
				/ length;

		// Check if projection is on the edge.
		if (u < 0.0 || u > 1.0)
			return null;

		
		// Compute projection.
		double plat = start.lat() + u * (end.lat() - start.lat());
		double plon = start.lon() + u * (end.lon() - start.lon());
		
		return new LatLon(plat, plon);
	}
	
	/**
	 * Add point on closest projection to the builded path.
	 * 
	 * @param point
	 * @return False if no such projection, True otherwise.
	 */
	public boolean addOnProjection(ExtNode node) {
		
		double distance = Double.MAX_VALUE;
		LatLon projection = null;
		int pos = 0;
		
		
		LatLon point = node.getLatLon();
		int i = 0;
		
		LatLon start = null;
		for (ExtNode n : nodes) {
			LatLon end = n.getLatLon();
			
			if (start != null) {
				LatLon p = project(point, start, end);
				
				// Check optimality.
				if (p != null && point.distance(p) < distance) {
					projection = p;
					distance = projection.distance(point);
					pos = i;
				}
			}
			start = end;
			i++;
		}
		
		if (projection != null) {
			node.setLatLon(projection);
			this.nodes.add(pos, node);
			return true;
		} else 
			return false;
	}
	
	public ExtPath divide(ExtNode on) {
		return divide(nodes.indexOf(on));
	}
	
	public ExtPath divide(int index) {
		
		// Get half
		LinkedList<ExtNode> half = new LinkedList<ExtNode>(nodes.subList(index, nodes.size()));
		
		// Remove half from nodes but left one node.
		nodes.removeAll(half.subList(1, half.size()));
		
		ExtPath other = new ExtPath(half);
		other.setTags(getTags());
		
		return other;
	}
	
	public void connect(ExtPath other, ExtNode on) {
		
		
		
		if (other.getFrom().getId().equals(on.getId()) && getTo().getId().equals(on.getId())) {
			// ->this->on->other->
			other.nodes.removeFirst();
			nodes.addAll(other.nodes);
		
		} else if (other.getTo().getId().equals(on.getId()) && getFrom().getId().equals(on.getId())) {
			// ->other->on->this->
			other.nodes.removeLast();
			nodes.addAll(0, other.nodes);
		
		} else if (other.getTo().getId().equals(on.getId()) && getTo().getId().equals(on.getId())) {
			// ->this->on<-other<-
			other.nodes.removeLast();
			for (Iterator<ExtNode> iter = other.nodes.descendingIterator(); iter.hasNext();) 
				nodes.addLast(iter.next());
			
		} else if (other.getFrom().getId().equals(on.getId()) && getFrom().getId().equals(on.getId())) {
			// <-this<-o->other->
			other.nodes.removeFirst();
			for (Iterator<ExtNode> iter = other.nodes.iterator(); iter.hasNext();)
				nodes.addFirst(iter.next());
		}
	}
	
	
	public double getLength() {
		double length = 0;
		ExtNode prev = null;
		for (ExtNode curr : nodes) {
			if (prev != null) {
				length += LatLon.distanceInMeters(prev.getLatLon(), curr.getLatLon());
			}
			prev = curr;
		}
		return length;
	}

	
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.Path;
	}
	
    
    public LatLon getLatLonOnDistance(double distance) {
    	
    	this.points = new LatLon[nodes.size()];
		
		int i = 0;
		for (ExtNode node : nodes) {
			this.points[i++] = node.getLatLon();
		}
		
		updateCumLength();
    	
    	return super.getLatLonOnDistance(distance);
    }
}
