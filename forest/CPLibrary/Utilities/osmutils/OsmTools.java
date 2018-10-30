package osmutils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


/**
 * Some useful tools for OSM.
 * @author Libor Wagner 
 *
 */
public class OsmTools {
	
	/**
	 * Cleans tag collection from unwanted tags. 
	 * @param in Collection of tags.
	 * @param keys Keys to be left in cleaned collection.
	 * @return Cleaned collection.
	 */
	public static Collection<Tag> cleanTags(Collection<Tag> in, Set<String> keys) {
		Collection<Tag> out = new LinkedList<Tag>();
		
		for (Tag tag : in) {
			if (keys.contains(tag.getKey())) {
				out.add(tag);
			}
 		}
		
		return out;
	}
	
	
	/**
	 * Extract {@link LatLon} from OSM {@link Node}.
	 * @param osmNode Input node.
	 * @return LatLon of the input node.
	 */
	public static LatLon extractLatLon(Node osmNode) {
		return new LatLon(osmNode.getLatitude(), osmNode.getLongitude());
	}
	
	/**
	 * Extracts tag value if any from collection.
	 * @param tags Tag collection.
	 * @param key 
	 * @return Value of tag with given key of empty string if no such tag.
	 */
	public static String getTagValue(Collection<Tag> tags, String key) {
		String value = "";
		
		for (Tag tag : tags) {
			if (tag.getKey().equals(key)) {
				value = tag.getValue();
				return value;
			}
		}
		
		return value;
	}
	
	/**
	 * Merge two tag collections tags from tagsB are copied to tagsA.
	 * @param tagsA
	 * @param tagsB
	 * @return tagsA
	 */
	public static Collection<Tag> mergeTags(Collection<Tag> tagsA, Collection<Tag> tagsB) {
		
		for (Tag tag : tagsB) {
			if (!contain(tagsA, tag.getKey())) {
				tagsA.add(tag);
			}
		}
		
		return tagsA;
	}
	
	/**
	 * Check existence of key in tag collection.
	 * @param tags
	 * @param key
	 * @return True if tags contains key, false otherwise.
	 */
	public static boolean contain(Collection<Tag> tags, String key) {
		
		for (Tag tag : tags) {
			if (tag.getKey().equals(key)) {
				return true;
			}
		}
		
		return false;
	}
	
	
}
