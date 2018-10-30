package osmutils;

import java.util.Iterator;

import org.openstreetmap.osm.data.DataSetFilter;
import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.Selector;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;



/**
 * This class is used together with {@link DataSetFilter} to produce new filtered {@link DataSet}.
 * 
 * Usage:
 * <pre>
 * 		// Selector construction
 *      TagSelector selector = new TagSelector();
 *  	selector.addIncludeTag("highway","*");
 *      selector.addExcludeTag("highway","pathway");
 *      
 *      // Filtering
 *      DataSetFilter filter = new DataSetFilter(selector);
 *      DataSet out = filter.filter(in);
 * </pre>
 * 
 * @author Libor Wagner
 *
 */
public class TagSelector implements Selector {
	
	/** Include used nodes and ways. */
	private boolean used;
	
	/** Include nodes. */
	private boolean nodes;
	
	/** Include ways. */
	private boolean ways;
	
	/** Include relations. */
	private boolean relations;
	
	/** Tag filter to filter tag. */
	private TagFilter filter;
	
	
	public TagSelector(TagFilter filter) {
		this(filter, true, true, true, true);
	}
	
	public TagSelector(
			TagFilter filter,
			boolean nodes, 
			boolean ways,
			boolean relations,
			boolean used) {
		
		this.used = used;
		this.nodes = nodes;
		this.ways = ways;
		this.relations = relations;
		
		this.filter = filter;
	}


	
	public boolean isAllowed(IDataSet ds, Way way) {
		
		return ways && filter.allow(way.getTags());
	}

	
	public boolean isAllowed(IDataSet ds, Node node) {
		
		if (nodes && filter.allow(node.getTags())) {
			return true;
		} else if (used) {
			
			Iterator<Way> it = ds.getWaysForNode(node.getId()); 
			
			for (;it.hasNext();) {
				if (isAllowed(ds, it.next())) {
					return true;
				}
			}
		}
		
		return false;
	}

	
	public boolean isAllowed(IDataSet ds, Relation relation) {
		
		return relations && filter.allow(relation.getTags());
	}
	
	// TODO RFCT muze se tahle metoda dat pryc ?
	@Deprecated
	public int getMissingCnt() {
		return 0;
	}
	
	public TagFilter getTagFilter() {
		return filter;
	}

}
