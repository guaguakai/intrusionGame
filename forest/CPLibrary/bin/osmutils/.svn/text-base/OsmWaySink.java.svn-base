package osmutils;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

//TODO RFCT co je to sink ? potopit ?
/**
 * This interface should be implemented by {@link OsmImportTask} if it want to sink {@link Way}s.
 * @author Libor Wagner
 *
 */
public interface OsmWaySink {
	/** 
	 * @param way Osm Way. 
	 * @param dataset Full osm Dataset. 
	 */
	public void sink(Way way, MemoryDataSet dataset);
}
