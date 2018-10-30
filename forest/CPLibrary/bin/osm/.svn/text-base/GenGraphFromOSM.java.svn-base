package osm;

import java.util.List;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.urbansecModels.AdversaryPath;
import model.urbansecModels.DefenderAllocation;

import org.jgrapht.graph.AbstractBaseGraph;

public class GenGraphFromOSM {
	protected static final boolean FILTER_NODES = true;
	protected static final boolean MERGE_MULTIPLE_EDGES = true;
	GraphAdapter adapter;
	AbstractBaseGraph<INode, ExtDWE> usGraph;
	GraphVisualizerUS vis;
	String osmFileName;
	
	public GenGraphFromOSM() {
		super();
		vis = null;
	}	
	
	public GraphAdapter getAdapter() {
		return adapter;
	}


	public AbstractBaseGraph<INode, ExtDWE> getGraph(String osmFileName,
			int numSources, int numTargets, int maxPayoff) {
		System.out.println(">>>>>>>>> " + osmFileName + " >>>>>>>>>");

		this.osmFileName = osmFileName;
		initAdapter(osmFileName);

		boolean ok = initProblem(new int[]{}, new int[] {});
		if (!ok) {
			System.err.println("Couldn't initialize problem");
			throw new RuntimeException("Couldn't initialize problem");
		}

		if (FILTER_NODES) {
			adapter.filterGraph();
		}
		if (MERGE_MULTIPLE_EDGES) {
			adapter.mergeMultipleEdges();
		}

		try {
			adapter.initSourcesRandomly(numSources);
			adapter.initTargetsRandomly(numTargets, maxPayoff);
			adapter.addVirtualSource();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.usGraph = adapter.getUSGraph();
		return this.usGraph;		
	}
	
	public AbstractBaseGraph<INode, ExtDWE> getGraph(String osmFileName,
			int[] sources, int[] targets, int[] targetRewards) {

		System.out.println(">>>>>>>>> " + osmFileName + " >>>>>>>>>");

		this.osmFileName = osmFileName;
		initAdapter(osmFileName);

		boolean ok = initProblem(targets, sources);
		if (!ok) {
			System.err.println("Couldn't initialize problem");
			throw new RuntimeException("Couldn't initialize problem");
		}

		if (FILTER_NODES) {
			adapter.filterGraph();
		}
		if (MERGE_MULTIPLE_EDGES) {
			adapter.mergeMultipleEdges();
		}

		try {
			adapter.initSources(sources);
			// initSourcesRandomly(SOURCE_NO);
			// adapter.initTargetsRandomly(TARGETS_NO);
			adapter.initTargets(targets, targetRewards);
			adapter.addVirtualSource();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.usGraph = adapter.getUSGraph();
		return this.usGraph;
	}

	protected boolean initProblem(int[] targets, int[] sources) {
		try {
			adapter.initTargets2(targets);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		try {
			adapter.initSources(sources);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected void initAdapter(String graphFilename) {
		OsmLoader loader = new OsmLoader(graphFilename);
		loader.run();
		adapter = new GraphAdapter(loader.getOSMGraphBuilder());
	}

	public GraphVisualizerUS getGraphVisualizer(AbstractBaseGraph<INode, ExtDWE> graph) {
		vis = new GraphVisualizerUS(graph);
		return vis;
	}
	
	public GraphVisualizerUS getGraphVisualizer() {
		vis = new GraphVisualizerUS(usGraph);
		return this.vis;
	}

	public void visualizeDefender(String fileName,
			List<DefenderAllocation> allocations, List<Double> strategy) {
		if ( vis == null) {
			this.getGraphVisualizer();
		}
		vis.generateDefenderAllocationFile(fileName, allocations, strategy);
	}

	public void visualizeAttacker(String fileName,
			List<AdversaryPath> paths, List<Double> strategy) {
		if ( vis == null) {
			this.getGraphVisualizer();
		}
		vis.generateAdversaryFile(fileName, paths, strategy);
	}

	public static GraphVisualizerUS visualizeGraph(String fileName, AbstractBaseGraph<INode, ExtDWE> usGraph) {
		GraphVisualizerUS visNew = new GraphVisualizerUS(usGraph);
		visNew.generateGraphFile(fileName);
		return visNew;
	}
	
	public void visualizeGraph(String fileName) {
		if ( vis == null) {
			this.getGraphVisualizer();
		}
		vis.generateGraphFile(fileName, adapter);
	}
	
	public void visualize(List<DefenderAllocation> defenderAllocations, List<Double> defStrategy, List<AdversaryPath> adversaryPaths, List<Double> advStrategy) {
		String name = "";
		if ( osmFileName.contains("/")) {
			name = osmFileName.split("/")[1];
		} else {
			name = osmFileName;
		}
		this.visualize(name, defenderAllocations, defStrategy, adversaryPaths, advStrategy);
	}
	
	public void visualize(String name, List<DefenderAllocation> defenderAllocations, List<Double> defStrategy, List<AdversaryPath> adversaryPaths, List<Double> advStrategy) {
		this.visualizeGraph(name + ".kml");
		this.visualizeDefender(name + "_DEF.kml", defenderAllocations, defStrategy);
		this.visualizeAttacker(name + "_ADV.kml", adversaryPaths, advStrategy);
	}
}
