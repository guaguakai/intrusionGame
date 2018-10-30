package visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JApplet;

import model.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import osm.OSMParser;

public class Visualize extends JApplet {
	
	private static final int DIM_X = 530;
	private static final int DIM_Y = 320;
	
	private static final int BORDER = 75;
	
	private static final int NODE_SIZE = 10;
	
	private static final Color     DEFAULT_BG_COLOR = Color.decode( "#FAFBFF" );
    private static final Dimension DEFAULT_SIZE = new Dimension( DIM_X + 2*BORDER, DIM_Y + 2*BORDER);

    // 
    private JGraphModelAdapter m_jgAdapter;
    
    private  AbstractBaseGraph<INode, ExtDWE> graph = null;
    
    public void setGraph() {
    	OSMParser o = new OSMParser();
		long stTime = System.currentTimeMillis();
		o.setOsmFile("/Users/manish_jain/Documents/EclipseWorkspace/StackelbergGame/data/Bombay_full.osm");
		o.parse();
		long ftTime = System.currentTimeMillis();
		
		System.out.println("Time Taken for parsing OSM file: " + (ftTime - stTime));
		System.out.println("Node count: " + o.getNodeCount());
		System.out.println("Way count: " + o.getWayCount());
		
		stTime = System.currentTimeMillis();
		graph = o.getGraph();		
		ftTime = System.currentTimeMillis();
				
		System.out.println("Time Taken for generating graph: " + (ftTime - stTime));
		System.out.println("Vertex count: " + graph.vertexSet().size());
		System.out.println("Edge count: " + graph.edgeSet().size());
		
		Configuration.initialize(0);
		GraphGenerator.setTargetAndSources(graph, 2, 2, 100, model.Configuration.randomGenerator);
				
		System.out.println("Vertex count: " + graph.vertexSet().size());
		System.out.println("Edge count: " + graph.edgeSet().size());
		
		System.exit(1);
    }
    
    public void setTestGraph() {
    	if ( Configuration.randomGenerator == null ) {
    		Configuration.initialize(0);
    	}    	
//    	graph = GraphGenerator.getRandomGeometricGraph(10, 2, 2, 0.3, 100, Configuration.randomGenerator);    	
    	graph = GraphGenerator.getGridGraph(5, 5, false, 3, 4, 100, Configuration.randomGenerator);    	
    }
    
    public void init(  ) {
        // create a JGraphT graph
    	    	    	
    	final String SUFFIX = "";
    	
    	setGraph();
    	
    	Rectangle2D.Double rect = GraphBounds.getBounds(graph);
    	System.out.println("Rect: " + rect);
    	
    	ListenableGraph g = new ListenableDirectedGraph( DefaultEdge.class );
        // create a visualization using JGraph, via an adapter
        m_jgAdapter = new JGraphModelAdapter( g );
        JGraph jgraph = new JGraph( m_jgAdapter );
        adjustDisplaySettings( jgraph );
        getContentPane(  ).add( jgraph );
        resize( DEFAULT_SIZE );        
        
        for ( INode u : graph.vertexSet() ) {      	
        	g.addVertex(SUFFIX + u.getId());                	
        }
        for ( ExtDWE edge : graph.edgeSet() ) {
        	g.addEdge(SUFFIX + graph.getEdgeSource(edge).getId(), SUFFIX + graph.getEdgeTarget(edge).getId());
        }
        
        double minLon = rect.getX() ;
        double maxLon = minLon + rect.getWidth();
        double minLat = rect.getY();
        double maxLat = minLat + rect.getHeight();
        
        System.out.println(minLon + ", " + maxLon +", " + minLat + ", " + maxLat);
        
        double lat, lon;
               
        for ( INode u : graph.vertexSet() ) {        	
        	        	
        	lat = (maxLat - u.getLat())/(maxLat - minLat) * DIM_Y + BORDER;
        	lon = (u.getLon() - minLon)/(maxLon - minLon) * DIM_X + BORDER;
        	
        	positionVertexAt(SUFFIX + u.getId(), lon, lat);
        }
        
    }
    
    private void adjustDisplaySettings( JGraph jg ) {
        jg.setPreferredSize( DEFAULT_SIZE );

        Color  c        = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter( "bgcolor" );
        }
         catch( Exception e ) {}

        if( colorStr != null ) {
            c = Color.decode( colorStr );
        }

        jg.setBackground( c );
    }


    private void positionVertexAt( Object vertex, double x, double y ) {
        DefaultGraphCell cell = m_jgAdapter.getVertexCell( vertex );
        Map              attr = cell.getAttributes(  );        
        
        // Rectangle        b    = (Rectangle) GraphConstants.getBounds( attr );

        // GraphConstants.setBounds( attr, new Rectangle( x, y, (int)b.getWidth(), (int)b.getHeight() ) );
        GraphConstants.setBounds( attr, new Rectangle2D.Double( x, y, NODE_SIZE, NODE_SIZE ));

        Map cellAttr = new HashMap(  );
        cellAttr.put( cell, attr );
                
        m_jgAdapter.edit(cellAttr, null, null, null);
    }

	
}
