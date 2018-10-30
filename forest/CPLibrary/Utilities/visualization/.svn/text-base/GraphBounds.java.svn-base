package visualization;



import java.awt.geom.Rectangle2D;

import model.graphutils.ExtDWE;
import model.graphutils.INode;

import org.jgrapht.graph.AbstractBaseGraph;

public class GraphBounds {

	public static Rectangle2D.Double getBounds(AbstractBaseGraph<INode, ExtDWE> graph) {
		Double minLat = Double.POSITIVE_INFINITY, maxLat = Double.NEGATIVE_INFINITY;
		Double minLon = Double.POSITIVE_INFINITY, maxLon = Double.NEGATIVE_INFINITY;
		
		for ( INode n : graph.vertexSet() ) {
			double lat = n.getLat();
			double lon = n.getLon();						
			
			if ( lat < minLat ) {
				minLat = lat;
			} 
			if ( lat > maxLat ) {
				maxLat = lat;
			}
			if ( lon < minLon ) {
				minLon = lon;
			} 
			if ( lon > maxLon ) {
				maxLon = lon;
			}			
		}		
		
		Rectangle2D.Double rect = new Rectangle2D.Double(minLon, minLat, maxLon - minLon, maxLat - minLat);
						
		return rect;
	}
	
}
