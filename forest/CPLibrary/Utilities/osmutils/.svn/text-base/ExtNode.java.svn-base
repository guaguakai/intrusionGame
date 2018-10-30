package osmutils;

import java.util.Collection;
import java.util.LinkedList;

import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


/**
 * @author Libor Wagner
 *
 */
public class ExtNode extends Node implements Primitive {
	
	private static final long serialVersionUID = -6612099071684339787L;
	
	protected Collection<Tag> tags;
	
	public ExtNode(String id, LatLon latlon) {
		super(id, latlon);
		this.tags = new LinkedList<Tag>();
	}

	public Collection<Tag> getTags() {
		return tags;
	}

	public void setTags(Collection<Tag> tags) {
		this.tags = tags;
	}

	public void setLatLon(LatLon latLon) {
		super.latLon = latLon;
	}

	
	public PrimitiveType getPrimitiveType() {
		return PrimitiveType.Node;
	}
	
}
