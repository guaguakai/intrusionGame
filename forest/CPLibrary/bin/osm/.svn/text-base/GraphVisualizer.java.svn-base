package osm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * Externalizes the graph to a KML file for Google Earth.
 * @author Ondrej Vanek
 *
 */
public abstract class GraphVisualizer {

	private boolean darkgraphLines = true;


	protected static final double EPS = 0.000000001;
	private static final boolean SHOW_NAMES = false;
	//private AbstractGraph<INode, ExtDWE> graph;

	/**
	 * @param graph What graph to visualize.
	 * @param ranger The algorithm file with results and sources/targets of the adversary.
	 */
	//	public GraphVisualizer(AbstractGraph<INode, ExtDWE> graph){
	//		this.graph=  graph;
	//
	//	}

	public void generateGraphFile(String filename, GraphAdapter adapter){
		//if(graph!=null){

		final Kml kml = KmlFactory.createKml();
		final Document document = kml.createAndSetDocument().withName(filename).withOpen(true);

		createStyles(document);

		for(ExtDWE link: getEdges()){	
			String description = link.toString();
			createLink(document, link,"#lineStyle",description);	
		}

		for(INode node: getNodes()){ //sorry for the horrible syntax :)
			String style = getStyle(node.getType());
			if(node.getType()!=NODE_TYPE.SOURCE){
				createNodePlacemark(document, node, style,
						(adapter == null )? "":String.valueOf(getDescription(adapter,node)));
				//adapter.getNodeOSMIDMapping().get(node.getId())));
			}
		}

		if(adapter!=null){
			if(adapter.getSources()!=null){
				for(INode node:adapter.getSources()){
					createNodePlacemark(document, node,"#sourceStyle",
							(adapter == null )? "":String.valueOf(getDescription(adapter,node)));
					//adapter.getNodeOSMIDMapping().get(node.getId())));
				}
			}
		}
		//
		//				if(adapter.getTargets()!=null){
		//					for(INode node:adapter.getTargets()){
		//						createNodePlacemark(document, node, "#targetStyle",
		//								(adapter == null )? "":String.valueOf(adapter.getNodeOSMIDMapping().get(node.getId())));
		//					}
		//				}
		//			}

		document.setOpen(false);

		try {
			if(!filename.contains("/")) filename = "./output/"+filename; 
			kml.marshal(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void generateGraphFile(String filename){
		//if(graph!=null){

		final Kml kml = KmlFactory.createKml();
		final Document document = kml.createAndSetDocument().withName(filename).withOpen(true);

		createStyles(document);

		for(ExtDWE link: getEdges()){	
			String description = link.toString();
			createLink(document, link,"#lineStyle",description);	
		}

		for(INode node: getNodes()){ //sorry for the horrible syntax :)
			String style = getStyle(node.getType());
			if(node.getType() != NODE_TYPE.SOURCE){
				createNodePlacemark(document, node, style, String.valueOf(node.getOsmId()));						
				//adapter.getNodeOSMIDMapping().get(node.getId())));
			} else {
				createNodePlacemark(document, node,"#sourceStyle",String.valueOf(node.getOsmId()));						
			}
		}

		document.setOpen(false);

		try {
			if(!filename.contains("/")) filename = "./output/"+filename; 
			kml.marshal(new File(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


	}

	private String getStyle(NODE_TYPE type) {
		if(type == NODE_TYPE.SOURCE){
			return  "#sourceStyle";

		}else if(type== NODE_TYPE.TARGET){
			return  "#targetStyle";
		}else{
			return  "#nodeStyle";
		}
	}

	/**
	 * Creates a set of styles for graph visualization.
	 * @param document
	 */
	private void createStyles(final Document document) {
		if(darkgraphLines) {
			document.createAndAddStyle().withId("lineStyle").createAndSetLineStyle().withColor("ff222222").withWidth(2.0);
		}else {
			document.createAndAddStyle().withId("lineStyle").createAndSetLineStyle().withColor("ffa0ffff").withWidth(2.0);
		}
		document.createAndAddStyle().withId("sourceStyle").
		createAndSetIconStyle().withColor("ffff0000").withScale(1.1d).createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png");
		document.createAndAddStyle().withId("targetStyle").
		createAndSetIconStyle().withColor("ff0000ff").withScale(1.1d).createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png");
		document.createAndAddStyle().withId("nodeStyle").
		createAndSetIconStyle().withColor("ffafffff").withScale(0.2d).createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png");

	}

	/**
	 * Creates an edge visualization.
	 * @param document kml document.
	 * @param link 
	 * @param style what style to use. Defined in {@link #createStyles(Document)}.
	 * @param description 
	 */
	protected void createLink(final Document document, ExtDWE link, String style, String description) {
		if(link.getType().equals(EDGE_TYPE.VIRTUAL)){
			return;
		}
		Placemark placemark = document.createAndAddPlacemark().withName("edge"+ link.getId()).withStyleUrl(style).withDescription(description);
		placemark.createAndSetLineString().
		addToCoordinates(getEdgeSource(link).getLat(),getEdgeSource(link).getLon(),0).
		addToCoordinates(getEdgeTarget(link).getLat(), getEdgeTarget(link).getLon(),0);
	}

	/**
	 * Creates a placemark for a node.
	 * @param document
	 * @param node
	 * @param style Styles defined in {@link #createStyles(Document)}.
	 */
	private void createNodePlacemark(final Document document, INode node,String style,String description) {
		Placemark placemark = document.createAndAddPlacemark().withStyleUrl(style).withDescription("ID: "+node.getId()+"\n"+description);
		if(SHOW_NAMES){
			placemark.withName(""+node.getId());
		}
		List<Coordinate> coords = new ArrayList<Coordinate>();
		coords.add(new Coordinate(node.getLat(),node.getLon(),0.0));
		placemark.createAndSetPoint().withCoordinates(coords);
	}

	protected abstract Collection<ExtDWE> getEdges();

	protected abstract Collection<INode> getNodes();

	protected abstract INode getEdgeSource(ExtDWE edge);

	protected abstract INode getEdgeTarget(ExtDWE edge);

	protected abstract String getDescription(GraphAdapter adapter, INode node);

	protected void createKMLDocument(String filename, final Kml kml,
			final Document document) {
		document.setOpen(false);
		try {
			kml.marshal(new File("./output/"+filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}

