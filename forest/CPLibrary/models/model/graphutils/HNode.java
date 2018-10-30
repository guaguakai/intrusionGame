package model.graphutils;

import model.graphutils.INode.NODE_TYPE;

public class HNode implements Comparable<HNode>{	
	public enum NODE_TYPE {SOURCE, IDS, MALWARE, DATA;
		
	public String toString(){
		switch(this){
		case SOURCE: return "S";
		case IDS: return "I";
		case MALWARE: return "T";
		case DATA: return "V";
		default: return "";
		}	
	}
	};
	
	private int id;
	public NODE_TYPE type;
	
	public HNode(NODE_TYPE t){
		type=t;
	}
	public NODE_TYPE getType() {
		return type;
	}
	
	public void setType(NODE_TYPE type) {
		this.type = type;
	}
	
	
	public int getId() {
		return id;
	}
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HNode other = (HNode) obj;
		if (id != other.id)
			return false;
		return true;
	}
	

	@Override
	public int compareTo(HNode arg0) {
		if(this.equals(arg0)) return 0;
		else if(this.id>arg0.getId()){
			return 1;
		}else if(this.id<arg0.getId()){
			return -1;
		}
		return 0;
	}
	

}
