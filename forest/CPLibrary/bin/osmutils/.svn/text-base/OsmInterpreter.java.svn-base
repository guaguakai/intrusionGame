package osmutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;


//TODO RFCT komentar tridy, je pouzita jednou pri uprave mapy nebo pokazdy pri spusteni programu ?
public class OsmInterpreter {
	
	private MemoryDataSet dataset;
	
	private Map<Long, ExtNode> nodesByOsmId;
	private Map<Long, List<ExtPath>> pathsByOsmId;
	private Map<Long, ExtRelation> relationsByOsmId;

	

	public OsmInterpreter(MemoryDataSet dataset) {
		this.dataset = dataset;
		this.nodesByOsmId = new HashMap<Long, ExtNode>();
		this.pathsByOsmId = new HashMap<Long, List<ExtPath>>();
		this.relationsByOsmId = new HashMap<Long, ExtRelation>();
		
	}
	
	public void setDataset(MemoryDataSet dataset) {
		this.dataset = dataset;
	}
	
	public void clear() {
		nodesByOsmId.clear();
		pathsByOsmId.clear();
		relationsByOsmId.clear();
	}
	
	public ExtNode interpret(Node node) {
		
		ExtNode ext = nodesByOsmId.get(node.getId());
		
		if (ext == null) {
			
			LatLon latlon = new LatLon(node.getLatitude(), node.getLongitude());
			ext = new ExtNode(node.getId()+"", latlon);
			
			// Extract info 
			ext.setTags(node.getTags());
			
			nodesByOsmId.put(node.getId(), ext);
		}
		
		return ext;
	}
	
	public List<ExtPath> interpret(Way way) {
		
		List<ExtPath> ext = pathsByOsmId.get(way.getId());
		
		if (ext == null) {
			ext = new LinkedList<ExtPath>();
			
			// Extract info
			Collection<Tag> tags = way.getTags(); 
			
			List<WayNode> nodes = way.getWayNodes();
			
			ExtNode prev = null;
			for (WayNode wayNode : nodes) {
				Node node = dataset.getNodeByID(wayNode.getNodeId());
				if (node != null) {
					ExtNode curr = interpret(node);
					
					if (prev != null) {
						ExtPath path = new ExtPath(prev,curr);
						path.setTags(tags);
						ext.add(path);
					}
					
					prev = curr;
				}
			}
			
			pathsByOsmId.put(way.getId(), ext);
		}
		
		
		return ext;
		
	}
	
	public ExtRelation interpret(Relation relation) {
		
		ExtRelation ext = relationsByOsmId.get(relation.getId());
		
		if (ext == null) {
			ext = new ExtRelation(relation.getId()+"");
			
			
			List<RelationMember> members = relation.getMembers();
			
			for (RelationMember relationMember : members) {
				switch (relationMember.getMemberType()) {
				case Node:
					Node node = dataset.getNodeByID(relationMember.getMemberId());
					if (node != null)
						ext.add(interpret(node));
					break;
				case Way:
					Way way = dataset.getWaysByID(relationMember.getMemberId());
					if (way != null)
						ext.addAll(interpret(way));
					break;
				case Relation:
					Relation rel = dataset.getRelationByID(relationMember.getMemberId());
					if (relation != null)
						ext.add(interpret(rel));
					break;
				default:
					break;
				}
			}
			
			ext.setTags(relation.getTags());
			
			relationsByOsmId.put(relation.getId(), ext);
		}
		
		return ext;
	}
	
	
	
}
