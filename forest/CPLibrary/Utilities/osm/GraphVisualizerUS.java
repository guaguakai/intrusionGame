package osm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.urbansecModels.AdversaryPath;
import model.urbansecModels.DefenderAllocation;

import org.jgrapht.graph.AbstractGraph;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;

/**
 * Externalizes the graph to a KML file for Google Earth.
 * @author Ondrej Vanek
 *
 */
public class GraphVisualizerUS extends GraphVisualizer {

	private AbstractGraph<INode, ExtDWE> graph;

	//	/**
	//	 * @param graph What graph to visualize.
	//	 * @param ranger The algorithm file with results and sources/targets of the adversary.
	//	 */
		public GraphVisualizerUS(AbstractGraph<INode, ExtDWE> graph){
			this.graph=  graph;	
		}


	public GraphVisualizerUS() {
			
		}


	protected  Collection<ExtDWE> getEdges(){
		return graph.edgeSet();

	}

	/**
	 * @param graph the graph to set
	 */
	public void setGraph(AbstractGraph<INode, ExtDWE> graph) {
		this.graph = graph;
	}

	protected  Collection<INode> getNodes(){
		return graph.vertexSet();
	}

	protected  INode getEdgeSource(ExtDWE edge){
		return graph.getEdgeSource(edge);
	}

	protected  INode getEdgeTarget(ExtDWE edge){
		return graph.getEdgeTarget(edge);
	}

	@Override
	protected String getDescription(GraphAdapter adapter, INode node) {
		try{
			
			return ""+adapter.getReversedNodeUSMapping().get(node.getId())+"|OSM:"+node.getOsmId();
			
		}catch(Exception e){
				//adapter.getNodeOSMIDMapping().get(node.getId());
			return ""+adapter.getNodeOSMIDMapping().get(adapter.getNodeUSMapping().get(node.getId()).getId());
		}
	}

	/**
	 * Generates the file with coverages on the edges.
	 * @param defenderStrategy 
	 * @param defenderAllocations 
	 */
	public void generateDefenderAllocationFile(String filename,List<DefenderAllocation> defenderAllocations, Iterable<Double> defenderStrategy){
		//if(graph!=null){
		final Kml kml = KmlFactory.createKml();
		final Document document = kml.createAndSetDocument().withName(filename).withOpen(true);

		document.createAndAddStyle().withId("coverageStyle").createAndSetLineStyle().withColor("5500ff00").withWidth(10.0);

		int idx= 0;
		Map<ExtDWE,Double> edges = new HashMap<ExtDWE, Double>();
		for(Iterator<Double> iter = defenderStrategy.iterator();iter.hasNext();){
			double prob = iter.next();
			if(prob>EPS){
				DefenderAllocation defenderAllocation = defenderAllocations.get(idx);
				for(ExtDWE edge: defenderAllocation.getAllocation()){
					if(edges.keySet().contains(edge)){
						double oldval = edges.get(edge);
						edges.put(edge,oldval+prob);
					}else{
						edges.put(edge, prob);
					}
				}			
			}
			idx++;
		}

		for(ExtDWE link: edges.keySet()){
			double coverageProb = edges.get(link);
			if(coverageProb>0){ //create an edge only if the probability is grater than zero.
				createLink(document, link, "#coverageStyle",link.toString()+" | alloc: "+ coverageProb);
			}
		}

		createKMLDocument(filename, kml, document);

		
	}

	public void generateAdversaryFile(String filename,List<AdversaryPath> adversaryPaths,List<Double> adversaryStrategy) {
		final Kml kml = KmlFactory.createKml();
		final Document document = kml.createAndSetDocument().withName(filename).withOpen(true);

		document.createAndAddStyle().withId("adversaryStyle").createAndSetLineStyle().withColor("550000aa").withWidth(10.0);

		int idx= 0;
		Map<ExtDWE,Double> edges = new HashMap<ExtDWE, Double>();
		for(Iterator<Double> iter = adversaryStrategy.iterator();iter.hasNext();){
			double prob = iter.next();
			if(prob>EPS){
				AdversaryPath adversaryPath = adversaryPaths.get(idx);
				for(ExtDWE edge: adversaryPath.getPath()){
					if(edges.keySet().contains(edge)){
						double oldval = edges.get(edge);
						edges.put(edge,oldval+prob);
					}else{
						edges.put(edge, prob);
					}
				}			
			}
			idx++;
		}

		for(ExtDWE link: edges.keySet()){
			double coverageProb = edges.get(link);
			if(coverageProb>0){ //create an edge only if the probability is grater than zero.
				createLink(document, link, "#adversaryStyle",link.toString()+" | prob: "+ coverageProb);
			}
		}
		createKMLDocument(filename, kml, document);
	}
}
