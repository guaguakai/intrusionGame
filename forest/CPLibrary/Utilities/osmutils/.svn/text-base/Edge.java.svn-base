package osmutils;

import java.io.Serializable;




/**
 * This class represent general {@link Graph} edge.
 *
 * @author Libor Wagner
 *
 */
public abstract class Edge implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3794583356995203336L;

	/** @return Unique id. */
	public String getId() {
		return getId(getFrom(),getTo());
	}

	public static String getId(Node node1, Node node2)
	{
	    return node1.getId() + ">" + node2.getId();
	}

	/** @return The starting node of this edge. */
	public abstract Node getFrom();

	/** @return The ending node of this edge. */
	public abstract Node getTo();

	/**
	 * Use this instead of computing directly form nodes as it may be different.
	 * @return Length of this edge in meters.
	 */
	public abstract double getLength();

	/** @return Reversed edge. */
	public abstract Edge getReverse();

	@Override
	public String toString() {
		return "e["+getId()+"]";
	}

}
