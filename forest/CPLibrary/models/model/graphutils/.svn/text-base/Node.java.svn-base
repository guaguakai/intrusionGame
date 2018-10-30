package model.graphutils;

public class Node implements INode {
	private int id;
	public NODE_TYPE type;
	public int payoff;	

	private LatLon latLon;
	
	private long osmId;
	
	private static int counter = 1;
	@Override
	public int getId() {
		return id;
	}

	@Override
	public NODE_TYPE getType() {
		return type;
	}
//	public void setType(Configuration.TYPE type) {
//		this.type = type;
//	}
	@Override
	public int getPayoff() {
		return payoff;
	}
//	public void setPayoff(int payoff) {
//		this.payoff = payoff;
//	}
	@Override
	public String toString() {
		if(type==NODE_TYPE.TARGET){
			return type.toString()+(new Integer(id)).toString() + "|P:"+payoff;
		}
		return type.toString()+(new Integer(id)).toString();
	}
	public Node() {
		this(NODE_TYPE.INTERMEDIATE,0);
	}
	
	public Node(NODE_TYPE type, int payoff) {
		super();
		this.id = counter;
		counter++;
		this.type = type;
		this.payoff = payoff;
	}
	
	public Node(NODE_TYPE type, int payoff,int id)
	{
		super();
		this.id = id;
		this.type = type; 
		this.payoff = payoff;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public void setLatLon(double lat, double lon) {
		latLon = new LatLon(lat, lon);
		
	}

	
	@Override
	public double getLat() {
		if(latLon!=null) return latLon.getLat();
		return 0;
	}

	
	@Override
	public double getLon() {
		if(latLon!=null) return latLon.getLon();
		return 0;
	}
	
	public static void resetCounter(){
		counter=1;
	}

	
	@Override
	public int compareTo(INode arg0) {
		if(this.equals(arg0)) return 0;
		else if(this.id>arg0.getId()){
			return 1;
		}else if(this.id<arg0.getId()){
			return -1;
		}
		return 0;
	}
	
	public static void reset(){
		counter=1;
	}

	/**
	 * @return the osmId
	 */
	@Override
	public long getOsmId() {
		return osmId;
	}

	/**
	 * @param osmId the osmId to set
	 */
	public void setOsmId(long osmId) {
		this.osmId = osmId;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(NODE_TYPE type) {
		this.type = type;
	}

	/**
	 * @param payoff the payoff to set
	 */
	public void setPayoff(int payoff) {
		this.payoff = payoff;
	}
	
	

}
