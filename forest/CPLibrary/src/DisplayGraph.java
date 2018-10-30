import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Dimension;

import org.jgrapht.graph.AbstractBaseGraph;

import com.mxgraph.model.mxGraphModel;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;
import model.teamBuildingModels.DefenderAllocation;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;

	public class DisplayGraph extends JFrame {
		private mxGraph graph;
		private AbstractBaseGraph g;
		private double smallest_lat, smallest_lon;
		private List <Integer> highlighted_edges = new ArrayList<Integer>();
		private static final long serialVersionUID = -2707712944901661771L;
		private HashMap<Integer, mxICell> id_to_edge = new HashMap<Integer, mxICell>();
		public mxGraphComponent graphComponent;
		//public mxGraphModel model;
		
		//draw graph (graph obj)
		
		//method to draw single allocation
		//constructor should take in AbstractBaseGraph and optionally 
		//list of probabilities and list of pure strategies
		public DisplayGraph(AbstractBaseGraph g) {
			//find min value for lat and long
			//minimum is origin, every other node is distance from origin
			//g.vertexSet() to get latitude and longitude
			
			this.g = g;
			this.graph = new mxGraph();
			this.graphComponent = new mxGraphComponent(graph);
			//this.model
			
		}
		public void run()
		{
			
			//GraphVisual frame = new GraphVisual();
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setSize(1000, 1000);
			this.setVisible(true);
		}
		//scheduler (mixed strategy, time frame)
		public List<DefenderAllocation> schedule(RuggedBetterResponsesCutoff solution, int days) {
			List<Double> strategy = solution.getDefenderStrategy();
			List<DefenderAllocation> dalist = solution.getDefenderAllocations();
			List<DefenderAllocation> sol = new ArrayList<DefenderAllocation>();
			for (int day_num = 1; day_num <= days; day_num++) {
				List<Double> cumulative_probabilities = new ArrayList<Double>();
				int i = 0;
				double val = 0;
				//Map<Integer, Double> strategies_to_probabilities = new HashMap<Integer, Double>();
				for (DefenderAllocation da : dalist) {
					//System.out.println("Strategy" + i + " played with probablity " + strategy.get(i));
					val += strategy.get(i);
					cumulative_probabilities.add(val);
					i++;
				}
				//generate random numbers
				int strategy_num = 0;
				Random r = new Random();
				double random_double = r.nextDouble();
				for (int count = 0; count < strategy.size(); count++) {
					double v = cumulative_probabilities.get(count);
					if (random_double > v){
						strategy_num = count;
					}
				}
				System.out.println("Playing strategy " + strategy_num + " on day " + day_num);
				sol.add(sol.size(), dalist.get(strategy_num));
			}
			return sol;
		}
		//dynamically change graph
		//allow user to add edges
		
		//clean up graph, all in one window
		//get rid of numbers next to edges
		//not open a new graph every time
		//user can enter number of days to schedule
		/*
		mxGraph.prototype.getImage = function(state)
				{
				  if (state != null && state.style != null)
				  {
				    return state.style[mxConstants.STYLE_IMAGE];
				  }
				  return null;
				}
		*/
		public void clear_highlight(DefenderAllocation da, int day_num) {
			graph.getModel().beginUpdate();
			try {
			//((mxGraphModel)graph.getModel()).clear();
			//clear vertices
			//graph.getChildVertices(graph.getDefaultParent())
			//graph.getModel().remove(graph.getChildVertices(graph.getDefaultParent()));
			//graph.getModel().remove(graph.getChildEdges(graph.getDefaultParent()));
			graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
			graph.removeCells(graph.getChildEdges(graph.getDefaultParent()));
			this.highlight(da, day_num, true);
			}
			finally {
				graph.getModel().endUpdate();
			}
		}
		public void update_highlight(DefenderAllocation da, int day_num) {
			
			//CREATE A STYLESHEET
			//graph.refresh();
			//graph.removeCells(graph.getChildEdges(graph.getDefaultParent()));
			//this.graph = new mxGraph();
			this.setTitle("Day Number " + day_num);
			Object parent = graph.getDefaultParent();
			HashMap<INode, Object> nodeMap = new HashMap<INode, Object>();
			//Object parent = graph.getDefaultParent();
			smallest_lat = 10000;
			smallest_lon = 10000;
			
			
			for(Iterator<INode> nodes = g.vertexSet().iterator();nodes.hasNext();){
	        	INode n = nodes.next();
	        	if(!n.getType().equals(NODE_TYPE.SOURCE)){
	        		//Object v = graph.insertVertex(graph.getDefaultParent(), "", n, 500*(n.getLat() - smallest_lat), 500*(n.getLon() - smallest_lon), 7, 7);
	        		//nodeMap.put(n, v);
	        		//System.out.println("latitude is " + (n.getLat() - smallest_lat)  + " long is " + (n.getLon() - smallest_lon) + " " + n.getId());
	        	
	        	}
	        }
			graph.getModel().beginUpdate();
			//try {
			
			for(Iterator<ExtDWE> edges = g.edgeSet().iterator();edges.hasNext();){
	        	ExtDWE e = edges.next();
	        	if(!e.getsource().equals(NODE_TYPE.SOURCE)){
	        		for (String r1 : da.getResourceMap().keySet()) {
						int resource = Integer.parseInt(da.getResourceMap().get(r1).toString().substring(1, da.getResourceMap().get(r1).toString().indexOf('(')));
							
				        		if (e.getId() == resource) {
				        			//change color to red
				        			Object n1 = nodeMap.get(e.getsource());
					        		Object n2 = nodeMap.get(e.gettarget());
					        		System.out.println(e.getId() + " should be highlighted");
					        		highlighted_edges.add(e.getId());
					        		
					        		System.out.println("Highlighting edge # " + e.getId());
					        		Object e3 = graph.getEdges(n1, n2);
					        		mxICell o_edge = id_to_edge.get(e.getId());
					        		//
					        		//System.out.println(o_edge.toString());
					        		//o_edge.setStyle(mxConstants.STYLE_STROKECOLOR + "red");
					        		//o_edge.setStyle(mxConstants.STYLE_FILLCOLOR + "red");
					        		//graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "red", new Object[]{o_edge});
					        		//o_edge.style.mxConstants.STYLE_STROKECOLOR = "red";
				
					        		//graph.resetEdge(e3);
					        		//graph.setCellStyle(mxConstants.STYLE_FILLCOLOR, "#00FF00", o_edge);
					        		
					        		
					        		mxICell e1 = (mxICell)graph.insertEdge(parent, null, "", n1, n2, "strokeColor=red");
					        		id_to_edge.remove(e.getId());
					        		id_to_edge.put(e.getId(), e1);
					        		//Object e1 = graph.insertEdge(parent, null, "", n1, n2, "strokeColor=red");
					        		//id_to_edge.put(e.getId(), e1);
				        			//graphComponent.getCellAt(n.getLat() + smallest_lat, n.getLon() + smallest_lon);
					        		break;
				        		}
				        		else {
				        			if (highlighted_edges.contains(e.getId())) {
				        				//change color to black
				        				System.out.println("Unhighlighting edge # " + e.getId());
				        				highlighted_edges.remove(highlighted_edges.indexOf(e.getId()));
				        			}
				        			Object n1 = nodeMap.get(e.getsource());
					        		Object n2 = nodeMap.get(e.gettarget());
					        		
					        		mxICell o_edge = id_to_edge.get(e.getId());
					        		//System.out.println(o_edge.toString());
					        		
					        		//graph.setCellStyle(mxConstants.STYLE_STROKECOLOR = "black", o_edge);
					        		//o_edge.setStyle(mxConstants.STYLE_STROKECOLOR + "blue");
					        		//o_edge.setStyle(mxConstants.STYLE_FILLCOLOR + "blue");
					        		
					        		//graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, "red", new Object[]{o_edge});
					        		//graphComponent.refresh();
					        		
					        		//graph.repaint();
					        		//Object o_edge = id_to_edge.get(e.getId());
					        		
					        		//System.out.println(o_edge.toString());
					        		graph.removeCells(new Object[]{o_edge});
					        		//graph.removeCells(new Object[]{o_edge});
					        		
					        		//mxICell e1 = (mxICell) graph.insertEdge(parent, null, "", n1, n2);
					        		
					        		//graph.setCellStyle(mxConstants.STYLE_STROKECOLOR = "red", new Object[]{o_edge});
					        		
					        		mxICell e1 = (mxICell)graph.insertEdge(parent, null, "", n1, n2);
					        		//System.out.println(e1.getClass());
					        		//validate(new Object[]{o_edge});
					        		id_to_edge.remove(e.getId());
					        		id_to_edge.put(e.getId(), e1);
					        		
					        	
				        		}
				    
				}
	        		
	        	}
	        	
	        }
			//}
			//finally {
			graph.getModel().endUpdate();
			
			//graph.refresh();
			//this.setVisible(true);
			//}
			
		}
		
		//update highlighted paths when user wants to see another schedule
		public void highlight(DefenderAllocation da, int day_num, boolean first_highlight) {
			//graph.removeCells(graph.getChildEdges(graph.getDefaultParent()));
			//graph.removeCells(graph.getChildCells(graph.getDefaultParent(), true, true));
			
			//this.graph = new mxGraph();
			this.setTitle("Day Number " + day_num);
			HashMap<INode, Object> nodeMap = new HashMap<INode, Object>();
			//this.graph = new mxGraph();
			Object parent = graph.getDefaultParent();
			smallest_lat = 10000;
			smallest_lon = 10000;
			for(Iterator<INode> nodes = g.vertexSet().iterator();nodes.hasNext();){
				INode n = nodes.next();
				if(!n.getType().equals(NODE_TYPE.SOURCE)){
					if (n.getLat() < smallest_lat) {
						smallest_lat = n.getLat();
					}
					if (n.getLon() < smallest_lon) {
						smallest_lon = n.getLon();
					}
				}
			}

			graph.getModel().beginUpdate();
			try
			{
				 for(Iterator<INode> nodes = g.vertexSet().iterator();nodes.hasNext();){
			        	INode n = nodes.next();
			        	if(!n.getType().equals(NODE_TYPE.SOURCE)){
			        		Object v = graph.insertVertex(graph.getDefaultParent(), "", n, 500*(n.getLat() - smallest_lat), 500*(n.getLon() - smallest_lon), 7, 7);
			        		nodeMap.put(n, v);
			        		//System.out.println("latitude is " + (n.getLat() - smallest_lat)  + " long is " + (n.getLon() - smallest_lon) + " " + n.getId());
			        	
			        	}
			        }
				//take off edge numbers from graph display
				 //JFrame that takes input
				 //user can see one graph at a time, and click next to see the others
				 //update function that would use new schedule
				for(Iterator<ExtDWE> edges = g.edgeSet().iterator();edges.hasNext();){
		        	ExtDWE e = edges.next();
		        	if(!e.getsource().equals(NODE_TYPE.SOURCE)){
		        		for (String r1 : da.getResourceMap().keySet()) {
							int resource = Integer.parseInt(da.getResourceMap().get(r1).toString().substring(1, da.getResourceMap().get(r1).toString().indexOf('(')));
							
					        		if (e.getId() == resource) {
					        			Object n1 = nodeMap.get(e.getsource());
						        		Object n2 = nodeMap.get(e.gettarget());
						        		System.out.println(e.getId() + " should be highlighted");
						        		highlighted_edges.add(e.getId());
						        		mxICell e1 = (mxICell) graph.insertEdge(parent, null, "", n1, n2, "strokeColor=red");
						        		
						        		//System.out.println("Inserting edge " + e.getId());
						        		id_to_edge.put(e.getId(), e1);
					        			//graphComponent.getCellAt(n.getLat() + smallest_lat, n.getLon() + smallest_lon);
						        		break;
					        		}
					        		else if (e.getId() != resource){
					        			Object n1 = nodeMap.get(e.getsource());
						        		Object n2 = nodeMap.get(e.gettarget());
						        		
						        		mxICell e1 = (mxICell) graph.insertEdge(parent, null, "", n1, n2);
						        		System.out.println("Inserting edge " + e.getId());
						        		id_to_edge.put(e.getId(), e1);
						        		break;
					        		}
					    
					}
		        		
		        	}
		        	
		        }
				
			}
			finally
			{
				graph.getModel().endUpdate();
				//this.run();
			}
			//if (first_highlight) {
			HashMap<String, Object> edge = new HashMap<String, Object>();
	    	edge.put(mxConstants.STYLE_ENDARROW, mxConstants.ARROW_CLASSIC);
	    	edge.put(mxConstants.STYLE_STARTARROW, mxConstants.ARROW_BLOCK);
	    	edge.put("strokeColor = black", mxConstants.STYLE_FONTCOLOR);
	    	mxStylesheet edgeStyle = new mxStylesheet();
	    	edgeStyle.setDefaultEdgeStyle(edge);
	    	graph.setStylesheet(edgeStyle);
	    	
	    	//HashMap<String, Object> red_edge = new HashMap<String, Object>();
	    	
	    	
	    	//mxStylesheet red_edgeStyle = new mxStylesheet();
	    	
	    	//graph.refresh();
	    	//graph.container.style.backgroundColor = "black";
	    	
			mxGraphComponent graphComponent = new mxGraphComponent(graph);
			getContentPane().add(graphComponent);
			//}
			
		}
		
	}

