package osmutils;

import org.openstreetmap.osm.data.Selector;


/**
 * Task for {@link OsmImporter}.
 * @author Libor Wagner
 *
 */
public interface OsmImportTask {
	
	/** @return Selector of elements for this task. */
	public Selector getSelector();
	
	/** This is called after all selected elements were sinked. */
	public void finish();
}
