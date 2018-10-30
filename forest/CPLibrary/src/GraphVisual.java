
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JFrame;

import model.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;

import org.jgrapht.graph.AbstractBaseGraph;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class GraphVisual extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2707712944901661771L;

	public GraphVisual(AbstractBaseGraph g){
		
		HashMap<INode, Object> nodeMap = new HashMap<INode, Object>();
		//AbstractBaseGraph g = RandomGraph( 10, 1, 1, 0.2);	
		
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();

		graph.getModel().beginUpdate();
		try
		{
			 for(Iterator<INode> nodes = g.vertexSet().iterator();nodes.hasNext();){
		        	INode n = nodes.next();
		        	if(!n.getType().equals(NODE_TYPE.SOURCE)){
		        		Object v = graph.insertVertex(graph.getDefaultParent(), n.toString(), n, (n.getLat())*500, (n.getLon())*500, 30, 30);
		        		nodeMap.put(n, v);
		        	}
		        }
			
			for(Iterator<ExtDWE> edges = g.edgeSet().iterator();edges.hasNext();){
	        	ExtDWE e = edges.next();
	        	if(!e.getsource().equals(NODE_TYPE.SOURCE)){
	        		Object n1 = nodeMap.get(e.getsource());
	        		Object n2 = nodeMap.get(e.gettarget());

	        		Object e1 = graph.insertEdge(parent, null, e.toString(), n1, n2);
	        	}
	        	
	        }
			
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		HashMap<String, Object> edge = new HashMap<String, Object>();
    	edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
    	edge.put(mxConstants.STYLE_STARTARROW, mxConstants.ARROW_BLOCK);
    	mxStylesheet edgeStyle = new mxStylesheet();
    	edgeStyle.setDefaultEdgeStyle(edge);
    	graph.setStylesheet(edgeStyle);
    	
		mxGraphComponent graphComponent = new mxGraphComponent(graph);
		getContentPane().add(graphComponent);
	}
	 private static AbstractBaseGraph<INode, ExtDWE> RandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r){
			Configuration.initialize(108374);
			Random random = new Random();
			return GraphGenerator.getRandomGeometricGraph(numNodes, nrOfSources, nrOfTargets, r,
				10, random);
			
			//System.out.println("Number of Edges: " + team.getGraph().edgeSet().size());
		}
	public void run()
	{
		//GraphVisual frame = new GraphVisual();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(400, 320);
		this.setVisible(true);
	}

}