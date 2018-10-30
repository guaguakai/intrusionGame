package osm;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;
import model.graphutils.INode.NODE_TYPE;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OSMParser {

	String osmFile = null;
	Map<String, OsmNode> osmNodesMap = null;
	Map<String, OsmWay> osmWaysMap = null;

	Rectangle2D.Double boundingBox = null;

	public Rectangle2D.Double getBoundingBox() {
		return boundingBox;
	}

	boolean inWay = false;
	OsmWay curOsmWay = null;

	int nodeCount = 0, wayCount = 0;

	public OSMParser() {
		super();
		osmNodesMap = new HashMap<String, OsmNode>();
		osmWaysMap = new HashMap<String, OsmWay>();
	}

	public Map<String, OsmWay> getOsmWaysMap() {
		return osmWaysMap;
	}

	public Map<String, OsmNode> getOsmNodesMap() {
		return osmNodesMap;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public int getWayCount() {
		return wayCount;
	}

	public String getOsmFile() {
		return osmFile;
	}

	public void setOsmFile(String osmFile) {
		this.osmFile = osmFile;
	}

	public void parse() {
		if (osmFile == null) {
			System.out.println("OSM File not set.");
			return;
		}

		final Set<String> highwayValues = new HashSet<String>();
		
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new DefaultHandler() {

				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {

					// System.out.println("Start Element: " + qName);

					if (qName.toLowerCase().equals("bound")) {
						int numAttributes = attributes.getLength();
						String boundingBoxString = null;
						for (int i = 0; i < numAttributes; i++) {
							if (attributes.getQName(i).toLowerCase()
									.equals("box")) {
								boundingBoxString = attributes.getValue(i)
										.toLowerCase();
							}
						}
						String[] boxCoordinates = boundingBoxString.split(",");
						Double minLat = null, maxLat = null, minLon = null, maxLon = null;
						minLon = new Double(boxCoordinates[0]);
						maxLon = new Double(boxCoordinates[1]);
						minLat = new Double(boxCoordinates[2]);
						maxLat = new Double(boxCoordinates[3]);

						boundingBox = new Rectangle2D.Double(minLon, maxLat,
								maxLon - minLon, maxLat - minLat);
					} else if (qName.toLowerCase().equals("node")) {
						// start of a node
						nodeCount++;
						if (nodeCount % 10000 == 1) {
							System.out.println("New Node [Count: " + nodeCount
									+ "]");
						}

						OsmNode newNode = new OsmNode();

						int numAttributes = attributes.getLength();
						// System.out.println("Attributes: ");
						String lat = null, lon = null, id = null;
						for (int i = 0; i < numAttributes; i++) {
							if (attributes.getQName(i).toLowerCase()
									.equals("id")) {
								id = attributes.getValue(i).toLowerCase();
								// System.out.println("id: " + id);
							}
							if (attributes.getQName(i).toLowerCase()
									.equals("lat")) {
								lat = attributes.getValue(i).toLowerCase();
								// System.out.println("lat: " + lat);
							}
							if (attributes.getQName(i).toLowerCase()
									.equals("lon")) {
								lon = attributes.getValue(i).toLowerCase();
								// System.out.println("lon: " + lon);
							}
						}
						newNode.setId(id);
						/***MANISH: SWITCH LATER ***/						
						newNode.setLatLonValue(lon, lat);

						osmNodesMap.put(id, newNode);

						// MANISH DEBUG
						if (newNode.getId() == null) {
							System.out.println("CUR OSM NODE ID IS NULL.");
							System.exit(1);
						} else if (newNode.getLatLonValue() == null) {
							System.out.println("CUR OSM NODE LATLON IS NULL.");
							System.exit(1);
						}
					} else if (qName.toLowerCase().equals("way")) {
						// MANISH DEBUG
						if (inWay == true) {
							System.out.println("Recursive Way !! Id: "
									+ curOsmWay.getId());
							System.exit(1);
						}

						wayCount++;
						if (wayCount % 10000 == 1) {
							System.out.println("New Way [Count: " + wayCount
									+ "]");
						}

						inWay = true;

						curOsmWay = new OsmWay();

						int numAttributes = attributes.getLength();
						// System.out.println("Attributes: ");
						String id = null;
						for (int i = 0; i < numAttributes; i++) {
							if (attributes.getQName(i).toLowerCase()
									.equals("id")) {
								id = attributes.getValue(i).toLowerCase();
							}
						}
						curOsmWay.setId(id);
					} else if (qName.toLowerCase().equals("nd")) {
						// MANISH DEBUG
						if (inWay == false) {
							System.out.println("IN WAY IS FALSE AND ND FOUND");
							System.exit(1);
						}

						int numAttributes = attributes.getLength();
						// System.out.println("Attributes: ");
						String nodeId = null;
						for (int i = 0; i < numAttributes; i++) {
							if (attributes.getQName(i).toLowerCase()
									.equals("ref")) {
								nodeId = attributes.getValue(i).toLowerCase();
							}
						}

						// MANISH DEBUG
						if (!osmNodesMap.containsKey(nodeId)) {
							System.out.println("Node ID not found in Way: "
									+ curOsmWay.getId() + "; " + nodeId);
							System.exit(1);
						}

						curOsmWay.addNode(osmNodesMap.get(nodeId));
					} else if (qName.toLowerCase().equals("tag")) {
						if (inWay == true) {
							// This tag is of the current way

							int numAttributes = attributes.getLength();
							// System.out.println("Attributes: ");
							String key = null, value = null;
							for (int i = 0; i < numAttributes; i++) {
								if (attributes.getQName(i).toLowerCase()
										.equals("k")) {
									key = attributes.getValue(i).toLowerCase();
								}
								if (attributes.getQName(i).toLowerCase()
										.equals("v")) {
									value = attributes.getValue(i)
											.toLowerCase();
								}
								// System.out.println(key + ":" + value);
								if ( key.equals("highway")) {
									highwayValues.add(value);
								}
							}
							curOsmWay.addTags(key, value);
						}
					}
				}

				public void endElement(String uri, String localName,
						String qName) throws SAXException {

					if (qName.toLowerCase().equals("way")) {
						// MANISH DEBUG
						if (curOsmWay.getId() == null) {
							System.out.println("CUR OSM PATH ID IS NULL.");
							System.exit(1);
						}

						inWay = false;
						osmWaysMap.put(curOsmWay.getId(), curOsmWay);
					}

					// System.out.println("End Element: " + qName);
				}

				public void characters(char ch[], int start, int length)
						throws SAXException {

					// System.out.println("Value: "
					// + new String(ch, start, length));
				}
			};

			saxParser.parse(osmFile, handler);
			
			System.out.println("Values for highway tag: " + highwayValues);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AbstractBaseGraph<INode, ExtDWE> getGraph() {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		// Adds only highways
		// of following types

		String[] HW_TYPES = { "primary" , "primary_link"
		 , "secondary" , "secondary_link"
		 , "tertiary"  , "tertiary_link"
		 , "trunk" , "trunk_link"
		 , "motorway" , "motorway_link",
		 "road"
//		 , "residential"
//		 , "unclassified" 
		};
		Set<String> HIGHWAY_TYPES = new HashSet<String>(Arrays.asList(HW_TYPES));

		Map<OsmNode, INode> nodesMap = new HashMap<OsmNode, INode>();

		Map<String, Integer> hwTypeCount = new HashMap<String, Integer>();
		for (String str : HW_TYPES) {
			hwTypeCount.put(str, 0);
		}

		for (Entry<String, OsmWay> entryWay : osmWaysMap.entrySet()) {
			OsmWay way = entryWay.getValue();

			if (way.getTags() == null) {
				continue;
			}

			if (way.getTags().keySet().contains("highway")) {
				String value = way.getTags().get("highway");
				if (HIGHWAY_TYPES.contains(value)) {
					List<OsmNode> osmNodeList = way.getOsmNodeList();
					if (osmNodeList.size() >= 2) {
						boolean oneWay = false;

						if (way.getTags().keySet().contains("oneway")) {
							if (way.getTags().get("oneway").equals("yes")
									|| way.getTags().get("oneway").equals("1")) {
								oneWay = true;
							}
						}
						
						oneWay = false;

						// only now add this way to the graph
						hwTypeCount.put(value, hwTypeCount.get(value) + 1);

						for (OsmNode o : osmNodeList) {
							if (!nodesMap.containsKey(o)) {
								// generate new node and add to graph
								INode newNode = new Node(
										NODE_TYPE.INTERMEDIATE, 0);
								newNode.setLatLon(o.getLatLonValue().getLat(),
										o.getLatLonValue().getLon());
								newNode.setOsmId(new Long(o.getId()));
								graph.addVertex(newNode);
								nodesMap.put(o, newNode);
							}
						}
						for (int i = 1; i < osmNodeList.size(); i++) {
							graph.addEdge(nodesMap.get(osmNodeList.get(i - 1)),
									nodesMap.get(osmNodeList.get(i)));
							if (oneWay == false) {
								graph.addEdge(nodesMap.get(osmNodeList.get(i)),
										nodesMap.get(osmNodeList.get(i - 1)));
							}
						}
					}
				}
			}
		}

		System.out.println("HW TYPE COUNT: " + hwTypeCount);

		return graph;
	}

}
