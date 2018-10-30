package osm;

import model.graphutils.LatLon;

public class OsmNode {
	LatLon latLonValue = null;
	String id = null;
	String name = null;
	
	public OsmNode() {
		super();
		latLonValue = null;
		id = null;
		name = null;		
	}
	
	@Override
	public String toString() {
		return "OsmNode [latLonValue=" + latLonValue + ", id=" + id + ", name="
				+ name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		OsmNode other = (OsmNode) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public LatLon getLatLonValue() {
		return latLonValue;
	}
	public void setLatLonValue(LatLon latLonValue) {
		this.latLonValue = latLonValue;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
	public void setLatLonValue(String lat, String lon){
		this.latLonValue = new LatLon(new Double(lat), new Double(lon));
	}
}
