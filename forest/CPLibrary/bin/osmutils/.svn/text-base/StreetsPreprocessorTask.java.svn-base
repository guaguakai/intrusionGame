package osmutils;

import java.util.List;

import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.Selector;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

//TODO RFCT komentar tridy
public class StreetsPreprocessorTask implements OsmImportTask,OsmWaySink {
	
	
	private GraphBuilder builder;
	
	public StreetsPreprocessorTask(GraphBuilder builder) {
		this.builder = builder;
	}
	
	
	public void finish() {
		builder.extractLargestConnectedComponent();
	}

	
	public Selector getSelector() {
		TagFilter filter = new TagFilter();
		filter.addIncludedTag("highway", "*");
		filter.addExcludedTag("area", "yes");
		
		// Do not include paths
		filter.addExcludedTag("highway", "path");
		filter.addExcludedTag("highway", "cycleway");
		filter.addExcludedTag("highway", "footway");
		filter.addExcludedTag("highway", "bridleway");
		filter.addExcludedTag("highway", "byway");
		filter.addExcludedTag("highway", "steps");
		filter.addExcludedTag("highway", "pedestrian");
		filter.addExcludedTag("highway", "track");
		
		return new TagSelector(filter,false, true, false, true);
	}

	
	public void sink(Way way, MemoryDataSet dataset) {
		OsmInterpreter interpreter = new OsmInterpreter(dataset);
		
		List<ExtPath> paths = interpreter.interpret(way);
		
		for (ExtPath path : paths) {
			builder.add(path);
		}
		
	}

}
