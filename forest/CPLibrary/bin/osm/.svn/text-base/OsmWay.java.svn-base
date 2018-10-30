package osm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OsmWay {
	List<OsmNode> osmNodeList = null;
	Map<String, String> tags = null;
	String id = null;
	String name = null;

	public Map<String, String> getTags() {
		return tags;
	}

	public String getId() {
		return id;
	}
	
	public void addTags(String key, String value) {
		if ( this.tags == null ) {
			this.tags = new HashMap<String, String>();
		}
		
		// MANISH DEBUG
		if ( tags.containsKey(key.toLowerCase() )) {
			System.out.println("Tag already exits: " + key + " in OsmWay: " + this.id);
			System.exit(1);
		}
		
		tags.put(key.toLowerCase(), value.toLowerCase());
	}
	
	public void addNode(OsmNode u) {
		if ( this.osmNodeList == null ) {
			this.osmNodeList = new ArrayList<OsmNode>();
		}
		osmNodeList.add(u);
	}

	public List<OsmNode> getOsmNodeList() {
		return osmNodeList;
	}

	public void setOsmNodeList(List<OsmNode> osmNodeList) {
		this.osmNodeList = osmNodeList;
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

	@Override
	public String toString() {
		return "OsmPath [osmNodeList=" + osmNodeList + ", id=" + id + ", name="
				+ name + "]";
	}

	public OsmWay() {		
		super();
		osmNodeList = new ArrayList<OsmNode>();
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
		OsmWay other = (OsmWay) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
