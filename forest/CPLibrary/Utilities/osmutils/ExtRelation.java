package osmutils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

public class ExtRelation implements Primitive {

	protected String id;
	protected List<Primitive> members;
	protected Collection<Tag> tags;
	
	public ExtRelation(String id) {
		this.id = id;
		this.members = new LinkedList<Primitive>();
	}
	
	public void add(Primitive primitive) {
		members.add(primitive);
	}
	
	public void addAll(List<? extends Primitive> primitives) {
		members.addAll(primitives);
	}
	
	public List<Primitive> getMembers() {
		return members;
	}
	
	
	public String getId() {
		return id;
	}

	
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.Relation;
	}

	
	public Collection<Tag> getTags() {
		return tags;
	}

	
	public void setTags(Collection<Tag> tags) {
		this.tags = tags;
		
	}
	
	
	
}
