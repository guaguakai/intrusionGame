package osmutils;

import java.util.List;

import org.openstreetmap.osm.data.coordinates.LatLon;


/**
 * Path is sequence of {@link Node}s.
 *
 * @author Libor Wagner
 * @author Ondrej Milenovsky
 */
public class Path extends Edge {

	private static final long serialVersionUID = 1127945564593209153L;

	/** Starting node of this edge. */
	protected Node from;

	/** Ending node of this edge. */
	protected Node to;

	/** Array of nodes in this way. */
	protected LatLon[] points;

	/** Cumulative sum of segment lengths in meters. */
	protected double[] 	cumLength;




	public Path(Node from, Node to, List<LatLon> points) {
		super();

		// Check
		if (!from.getLatLon().equals(points.get(0)) || !to.getLatLon().equals(points.get(points.size() - 1))) {
			throw new RuntimeException("Path creation error.");
		}

		this.from = from;
		this.to = to;

		this.points = new LatLon[points.size()];

		int i = 0;
		for (LatLon latLon : points) {
			this.points[i++] = latLon;
		}

		updateCumLength();
	}

	// TODO RFCT duplicated code, two almost the same constructors (see above)
	public Path(Node from, Node to, LatLon[] points) {
		super();

		// Check
		if (!from.getLatLon().equals(points[0]) || !to.getLatLon().equals(points[points.length - 1])) {
			throw new RuntimeException("Path creation error.");
		}

		this.from = from;
		this.to = to;

		this.points = points;

		updateCumLength();
	}

	// Compute cumulative sum from segments.
	protected void updateCumLength() {
		cumLength = new double[points.length];
		cumLength[0] = 0;
		double dist;
		for (int i = 1; i < cumLength.length; i++) {
			dist = LatLon.distanceInMeters(points[i-1], points[i]);
			cumLength[i] = cumLength[i-1] + dist;
		}
	}


	/** @return Unique id. */
	public String getId() {
		return getFrom().getId()+">"+getTo().getId();
 	}

	/**
	 * Get real length of this way in meters computed over all nodes.
	 *
	 * @return Length of this way in meters.
	 */
	public double getLength() {
		return cumLength[cumLength.length - 1];
	}


	/**
	 * Get position of point on this way in given distance from starting node.
	 *
	 * @param distance Distance from starting node, in meters.
	 * @return Position on this way, null if out of way.
	 */
	public LatLon getLatLonOnDistance(double distance) {

		// Input check
		if (distance < 0)
			return null;

		double diff; // Difference between cumulative length and given distance.


		for (int i = 0; i < cumLength.length; i++) {
			diff = distance - cumLength[i];
			if (diff < 0) {
				diff = distance - cumLength[i-1];


				double slen = LatLon.distanceInMeters(points[i-1], points[i]);
				double ratio = diff/slen;

				double vlon = points[i].lon() - points[i-1].lon();
				double vlat = points[i].lat() - points[i-1].lat();

				double lon = points[i-1].lon() + ratio*vlon;
				double lat = points[i-1].lat() + ratio*vlat;

				return new LatLon(lat, lon);

			} else if (diff == 0) {
				return points[i];
			}
		}

		return null;

	}

	/**
	 * Get first node of this way.
	 * @return First node.
	 */
	public Node getFrom() {
		return from;
	}


	/**
	 * Get last node of this way.
	 * @return Last node.
	 */
	public Node getTo() {
		return to;
	}

	/**
	 * Get all nodes in the path.
	 * @return Nodes in this path.
	 */
	public LatLon[] getPoints() {
		return points;
	}

	/**returns first point in the incomming path from node1 to node2*/
	public static LatLon getFirstIncomingPoint(Node node1, Node node2)
	{
        Path path = (Path)node1.getIncomingEdges().get(node2.getId());
        return path.getPoints()[path.getPoints().length - 2];
	}

    /**returns first point in the outgoing path from node1 to node2*/
    public static LatLon getFirstOutgoingPoint(Node node1, Node node2)
    {
        Path path = (Path)node1.getOutgoingEdges().get(node2.getId());
        return path.getPoints()[1];
    }

	@Override
	public Path getReverse() {

		LatLon[] reverse = new LatLon[points.length];

		for (int i = 0; i < points.length; i++) {
			reverse[points.length - i - 1] = points[i];
		}

		return new Path(to, from, reverse);
	}

}
