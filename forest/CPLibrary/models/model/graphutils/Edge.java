package model.graphutils;


import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;


public class Edge extends DefaultWeightedEdge implements Comparable, IEdge {
	private static final long serialVersionUID = 1L;
	
	public DefaultEdge p = new DefaultEdge();
	
	private static int counter = 1;

	private int id;
	
	protected double coverage;
	
	private EDGE_TYPE type = EDGE_TYPE.NORMAL;
	
	public INode getsource()
	{
		return (INode)this.getSource();
	}
	public INode gettarget()
	{
		return (INode)this.getTarget();
	}
	public double getweight()
	{
		return this.getWeight();

	}	
	
	public String toString() {
		if ( this.type == EDGE_TYPE.NORMAL ) {
			return ""+id;
		} else {
			return ""+id+"(V)";
		}
		 
		//return ((new Integer(id)).toString() + ":" + this.getSource() +"->"+this.getTarget());
		
	}
	public int getId() {
		return id;
	}

	public Edge() {
		super();
		this.id = counter;
		counter++;
	}

	/**
	 * @return the coverage
	 */
	public double getCoverage() {
		return coverage;
	}
	/**
	 * @param coverage the coverage to set
	 */
	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (id != other.id)
			return false;
		return true;
	}
	public static void reset() {
		counter=1;
		
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the type
	 */
	public EDGE_TYPE getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(EDGE_TYPE type) {
		this.type = type;
	}
	
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		Edge other = (Edge) arg0;
		if ( this.equals(arg0)) {
			return 0;
		}
		else if ( this.getId() < other.getId()) {
			return -1;
		}
		else 
		return 1;
	}
	
}

