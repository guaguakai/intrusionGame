package model.urbansecModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpWrapper.Configuration;
import lpWrapper.LPSolverException;
import lpWrapper.MIProblem;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;

import org.jgrapht.graph.AbstractBaseGraph;

public class MinCut extends MIProblem {
		
	protected AbstractBaseGraph<INode, ExtDWE> graph;		
	protected INode target;
	protected INode source;
	
	Map<String, Integer> varMap;
	Map<String, Integer> rowMap;
	
//	Set<ExtDWE> disabledEdges;
	
	public INode getTarget() {
		return target;
	}

	public void setTarget(INode target) {
		this.target = target;
	}
			
	public AbstractBaseGraph<INode, ExtDWE> getGraph() {
		return graph;
	}

	public void setGraph(AbstractBaseGraph<INode, ExtDWE> graph) {
		this.graph = graph;
	}

	public INode getSource() {
		return source;
	}

	public void setSource(INode source) {
		this.source = source;
	}

	public MinCut(AbstractBaseGraph<INode, ExtDWE> graph) {
		super();
		this.graph = graph;
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		this.target = null;
		this.source = null;
//		this.disabledEdges = new HashSet<ExtDWE>();
	}

	
	public MinCut(UrbanSecProblem usProblem) {
		super();
		this.graph = usProblem.getGraph();
		this.source = usProblem.getSource();
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		this.target = null;
//		this.disabledEdges = new HashSet<ExtDWE>();
	}
	
//	public void disableEdge(ExtDWE e) {					
//		if ( this.varMap == null || this.varMap.isEmpty() ) {
//			this.disabledEdges.add(e);
//		} else {
//			this.resetColumnBound(this.varMap.get("d" + e.getId()), BOUNDS_TYPE.FIXED, 0.0, 0.0);
//		}
//	}
//	
//	public void enableEdge(ExtDWE e) {
//		this.resetColumnBound(this.varMap.get("d" + e.getId()), BOUNDS_TYPE.LOWER, 0.0, 0.0);		
//	}
	
	public Set<ExtDWE> getMinCut() {
		Set<ExtDWE> minCutSet = new HashSet<ExtDWE>();
		
//		for ( ExtDWE e : this.disabledEdges) {
//			this.disableEdge(e);
//		}
		
		try {
			this.solve();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for ( ExtDWE edge : this.graph.edgeSet() ) {
			Double dijValue = this.getColumnPrimal(this.varMap.get("d" + edge.getId()));
			if ( dijValue > Configuration.EPSILON) {
				minCutSet.add(edge);
			}
		}
		return minCutSet;
	}

	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		this.setProblemName("MinCut");
		setProblemType(PROBLEM_TYPE.LP, OBJECTIVE_TYPE.MIN);
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		
		if ( this.target == null) {
			throw new RuntimeException("Target is not set.");
		}
		if ( this.source == null ) {
			throw new RuntimeException("Source is not set.");
		}
		
		// adding d_ij
		for ( ExtDWE edge : this.graph.edgeSet() ) {
			if ( edge.getType() == EDGE_TYPE.VIRTUAL ) {
				addAndSetColumn("d"+edge.getId(), BOUNDS_TYPE.FIXED, 0.0, 0.0, VARIABLE_TYPE.CONTINUOUS, 1.0);
			} else {
				addAndSetColumn("d"+edge.getId(), BOUNDS_TYPE.LOWER, 0.0, Double.POSITIVE_INFINITY, VARIABLE_TYPE.CONTINUOUS, 1.0);	
			}						
			this.varMap.put("d"+edge.getId(), this.numCols);
		}
		// adding p_i
		for ( INode node : this.graph.vertexSet() ) {
			if ( node.equals(this.source )) {
				addAndSetColumn("p"+node.getId(), BOUNDS_TYPE.FIXED, 1.0, 1.0, VARIABLE_TYPE.CONTINUOUS, 0.0);
			} else if ( node.equals(this.target) ) {
				addAndSetColumn("p"+node.getId(), BOUNDS_TYPE.FIXED, 0.0, 0.0, VARIABLE_TYPE.CONTINUOUS, 0.0);
			} else {
				addAndSetColumn("p"+node.getId(), BOUNDS_TYPE.LOWER, 0, Double.POSITIVE_INFINITY, VARIABLE_TYPE.CONTINUOUS, 0.0);
			}
			this.varMap.put("p"+node.getId(), this.numCols);			
		}
		
	}

	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		
		List<Integer> firstRowIndices = new ArrayList<Integer>();
		List<Double> firstRowValues = new ArrayList<Double>();

		
		for ( ExtDWE edge : this.graph.edgeSet() ) {
			addAndSetRow("dRow"+edge.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
			rowMap.put("dRow"+edge.getId(), this.numRows);
			
			firstRowIndices.clear();
			firstRowValues.clear();
			
			// d_ij - p_i + p_j >= 0
			firstRowIndices.add(varMap.get("d" + edge.getId())); //d
			firstRowValues.add(1.0);

			firstRowIndices.add(varMap.get("p" + this.graph.getEdgeSource(edge).getId())); //-p_i
			firstRowValues.add(-1.0);

			firstRowIndices.add(varMap.get("p" + this.graph.getEdgeTarget(edge).getId())); //p_j
			firstRowValues.add(1.0);
			
			setMatRow(this.numRows, firstRowIndices, firstRowValues);			
		}

	}

	@Override
	protected void generateData() {
		// TODO Auto-generated method stub
		// everything done in setColBounds and setRowBounds()
		return;		
	}

}
