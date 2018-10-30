package model.graphutils;

public interface INode extends Comparable<INode>{	
	public enum NODE_TYPE {SOURCE, OFFICE, INTERMEDIATE, TARGET, VIRTUAL_TARGET, WATER;
		
	public String toString(){
		switch(this){
		case SOURCE: return "S";
		case INTERMEDIATE: return "I";
		case TARGET: return "T";
		case VIRTUAL_TARGET: return "V";
		case OFFICE: return "O";
		case WATER: return "W";
		default: return "";
		}	
	}
	};
	
	public NODE_TYPE getType();
	
	public void setType(NODE_TYPE t);
	
	public int getPayoff();
	
	public void setPayoff(int payoff);
	
	public int getId() ;
	
	public int hashCode();
	
	public boolean equals(Object obj);
	
	public String toString();
	
	public void setLatLon(double lat, double lon);

	public double getLat();
	
	public void setOsmId(long id);

	public long getOsmId();
	
	public double getLon();
}
