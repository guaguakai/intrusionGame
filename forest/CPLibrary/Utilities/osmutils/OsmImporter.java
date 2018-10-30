package osmutils;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.osm.data.DataSetFilter;
import org.openstreetmap.osm.data.MemoryDataSet;
import org.openstreetmap.osm.data.Selector;
import org.openstreetmap.osm.data.coordinates.Bounds;
import org.openstreetmap.osm.io.FileLoader;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;


/**
 * This class imports data from an osm file using a set of {@link OsmImportTask}s.
 * 
 * @author Libor Wagner
 * 
 */
public class OsmImporter {

	/** List of interpreters used to interpret. */
	private List<OsmImportTask> tasks;

	/** Main dataset all selections are made from this. */
	private MemoryDataSet dataset;
	
	/** Verbose flag. */
	private boolean verbose;

	/** New instance of {@link OsmImporter}. */
	public OsmImporter() {
		this.tasks = new LinkedList<OsmImportTask>();
		this.verbose = false;
	}

	/**
	 * Add new {@link OsmImportTask} to task list.
	 * 
	 * @param New
	 *            Task.
	 */
	public void addTask(OsmImportTask task) {
		tasks.add(task);
	}

	/**
	 * Parse osm file.
	 * 
	 * @param osmfile
	 */
	public void parseOsm(File osmfile) {
		FileLoader loader = new FileLoader(osmfile);
		dataset = loader.parseOsm();
		runTasks();
	}

	/** @return Main dataset. */
	public MemoryDataSet getDataset() {
		return dataset;
	}

	/**
	 * @param dataset
	 *            Main dataset.
	 */
	public void setDataset(MemoryDataSet dataset) {
		this.dataset = dataset;
	}

	/** Run all tasks */
	public void runTasks() {
		
		if (dataset == null) {
			throw new RuntimeException("No dataset was set!");
		}
		
		for (OsmImportTask task : tasks) {

			long s_time = 0;
			if (verbose) {
				System.out.println("Running " + task.getClass().getSimpleName());
				s_time = System.currentTimeMillis();
			}
			
			MemoryDataSet filtered;

			Selector selector = task.getSelector();
			if (selector != null) {
				DataSetFilter filter = new DataSetFilter(selector);
				filtered = filter.filterDataSet(dataset);

				if (task instanceof OsmWaySink) {
					
					if (verbose) {
						System.out.printf(" Task is sinking %d ways.\n", filtered.getWaysCount());
					}
					
					Iterator<Way> itw = filtered.getWays(Bounds.WORLD);
					for (; itw.hasNext();) {
						((OsmWaySink) task).sink(itw.next(), dataset);
					}
				}

			}

			task.finish();
			
			if (verbose) {
				System.out.printf(" Task finished in %03f s.\n", ((double)(System.currentTimeMillis() - s_time))/1000);
			}

		}
	}
	
	/** Set verbose flag */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
