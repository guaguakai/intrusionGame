package model.graphutils;

import model.graphutils.INode.NODE_TYPE;

/**
 *
 * @author inducer
 */
public class MyNode implements INode{
	static int VERTEX_COUNT = 1;
	public int id; // good coding practice would have this as private
    public double freward;
    public double fpenalty;
    public double lreward;
    public double lpenalty;
    
    private double prob;
    
    public NODE_TYPE type;
    
    private double lat;
    private double lon;
	private Long osmid;

    public MyNode(int id) {
        this(id, 0);
    }

    public MyNode(int id, double payoff) {
        this.id = id;
        this.freward = payoff;
        this.fpenalty = 0;
        this.lreward = 0;
        this.lpenalty = -payoff;
    }

    public MyNode(double payoff) {
        this.id = VERTEX_COUNT++;
        this.freward = payoff;
        this.fpenalty = 0;
        this.lreward = 0;
        this.lpenalty = -payoff;
    }

    public String toString() { // Always a good idea for debugging
    	if(freward>0) return "T"+id+"|P:"+freward+"|OSM"+osmid;
        return "V" + id; // JUNG2 makes good use of these.
    }

	/**
	 * @return the prob
	 */
	public double getProb() {
		return prob;
	}

	/**
	 * @param prob the prob to set
	 */
	public void setProb(double prob) {
		this.prob = prob;
	}
	
	public void setLatLon(double lat, double lon){
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	
	public NODE_TYPE getType() {
		if(freward>0){
			return NODE_TYPE.TARGET;
		}
		return NODE_TYPE.INTERMEDIATE;

	}
	
	public int getPayoff() {
		return (int) freward;
	}
	
	public void setPayoff(int payoff) {
		this.freward = payoff;
        this.fpenalty = 0;
        this.lreward = 0;
        this.lpenalty = -payoff;
	}
	
	public int getId() {
		return id;
	}

	
	public int compareTo(INode arg0) {
		if(this.equals(arg0)) return 0;
		else if(this.id>arg0.getId()){
			return 1;
		}else if(this.id<arg0.getId()){
			return -1;
		}
		return 0;
	}

	public void setOSMID(long id2) {
		this.osmid = id2;
	}
	
	public long getOsmId(){
		return osmid;
	}

	@Override
	public void setType(NODE_TYPE t) {
		// TODO Auto-generated method stub
		this.type = t;
	}

	@Override
	public void setOsmId(long id) {
		// TODO Auto-generated method stub
		this.setOSMID(id);
	}
}
