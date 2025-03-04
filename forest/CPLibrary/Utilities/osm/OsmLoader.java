package osm;

import java.io.File;
import java.util.Collection;

import org.openstreetmap.osm.data.MemoryDataSet;

import osmutils.ExtNode;
import osmutils.ExtPath;
import osmutils.GraphBuilder;
import osmutils.OsmImporter;
import osmutils.StreetsPreprocessorTask;

public class OsmLoader {
	
	private static String MAP_FILENAME = "MapData/map.osm";
	private GraphBuilder graphbuilder;
	
	public OsmLoader(){
		
	}
	
	public OsmLoader(String filename){
		MAP_FILENAME = filename;
	}
	
	public static void main(String[] args) {
		OsmLoader test = new OsmLoader();
		test.run();
	}

	public void run() {
		File osmfile = new File(MAP_FILENAME);
		graphbuilder = new GraphBuilder();
		StreetsPreprocessorTask task = new StreetsPreprocessorTask(graphbuilder);
		OsmImporter importer = new OsmImporter();
		importer.setVerbose(true);
		importer.addTask(task);
		importer.parseOsm(osmfile);
		MemoryDataSet dataset = importer.getDataset();
		//dataset.getNodeHelper().getMap().
		System.out.println("Nodes: " +graphbuilder.getNodes().size());
		System.out.println("Edges: "+ graphbuilder.getPaths().size());
		int max = 0;
		for(ExtPath path:graphbuilder.getPaths()){
			//System.out.println("Path " + path.getId() + ", "+ path.getNodes());
			//max++;
			//if(max>10) break;
			if(path.getNodes().size()>2){
				System.out.println(path);
			}
		}
		
	}
	
	public Collection<ExtPath> getEdges(){
		if(graphbuilder==null){
			throw new NullPointerException("Graph is not initialized!");
		}
		
		return graphbuilder.getPaths();
	}
	
	public Collection<ExtNode> getNodes(){
		if(graphbuilder==null){
			throw new NullPointerException("Graph is not initialized!");
		}
		
		return graphbuilder.getNodes();
	}
	
	public GraphBuilder getOSMGraphBuilder(){
		if(graphbuilder==null){
			throw new NullPointerException("Graph is not initialized!");
		}
		return graphbuilder;
	}
}
