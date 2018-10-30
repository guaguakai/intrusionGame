package model.graphutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import model.urbansecModels.DefenderAllocation;
import model.urbansecModels.UrbanSecProblem;

public class MinCutPerTarget extends MIProblem {
	private UrbanSecProblem usProblem;
	private INode source;
	private INode target;

	public MinCutPerTarget(UrbanSecProblem usProblem, INode source, INode target) {
		super();
		this.usProblem = usProblem;
		this.source = source;
		this.target = target;
		this.setProblemName("MinCutTarget"+this.target.getId());
	}

	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		setProblemType(PROBLEM_TYPE.LP,OBJECTIVE_TYPE.MIN);
	}

	@Override
	protected void setRowBounds() {		
		// TODO Auto-generated method stub
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);	
		for (ExtDWE e: edgeList) {
			addAndSetRow("RowD"+e.getId(), BOUNDS_TYPE.LOWER, 0, 0);
		}
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		// add columns for each edge
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		for ( ExtDWE e: edgeList) {			
			addAndSetColumn("d"+(e.getId()),BOUNDS_TYPE.DOUBLE, 0, Double.MAX_VALUE, VARIABLE_TYPE.CONTINUOUS, 1);
		}
		// add columns for each row
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);
		for ( INode n: nodeList) {
			if ( n == this.source) {
				addAndSetColumn("p"+(n.getId()),BOUNDS_TYPE.DOUBLE, 1, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			} else if ( n == this.target) {
				addAndSetColumn("p"+(n.getId()),BOUNDS_TYPE.DOUBLE, 0, 0, VARIABLE_TYPE.CONTINUOUS, 0);
			} else {
				addAndSetColumn("p"+(n.getId()),BOUNDS_TYPE.DOUBLE, 0, Double.MAX_VALUE, VARIABLE_TYPE.CONTINUOUS, 0);
			}
		}
	}

	@Override
	protected void generateData() {
		// TODO Auto-generated method stub
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);		 
		List<Integer> firstRowIndices = new ArrayList<Integer>(3);
		List<Double> firstRowValues = new ArrayList<Double>(3);

		// Add elems for each edge
		for (ExtDWE e: edgeList) {
			firstRowIndices.clear();
			firstRowValues.clear();
			// d_{ij} - p_i + p_j >= 0;
			firstRowIndices.add(e.getId());
			firstRowValues.add(1.0);
			firstRowIndices.add(edgeList.size() + this.usProblem.getGraph().getEdgeSource(e).getId());
			firstRowValues.add(-1.0);
			firstRowIndices.add(edgeList.size() + this.usProblem.getGraph().getEdgeTarget(e).getId());
			firstRowValues.add(1.0);
			setMatRow(e.getId(), firstRowIndices, firstRowValues);
		}	
	}

	public List<ExtDWE> getMinCut() {
		List<ExtDWE> minCut = new ArrayList<ExtDWE>();
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		for (ExtDWE e: edgeList) {
			double edgeInCut = this.getColumnPrimal(e.getId());
			//System.out.println("Edge" + e.getId() + ": " + edgeInCut);
			if ( edgeInCut > Configuration.EPSILON) {
				minCut.add(e);
			}
		}
//		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
//		Collections.sort(nodeList);
//		for ( INode n: nodeList) {
//			double nodeRC = this.getColumnPrimal(edgeList.size() + n.getId());
//			System.out.println("Node" + n.getId() + ": " + nodeRC);
//		}
		return minCut;
	}	
	
	public List<DefenderAllocation> getDefenderAllocations(int resources){
		List<DefenderAllocation> daList = new ArrayList<DefenderAllocation>();
		List<ExtDWE> minCut = getMinCut();
		return getAllDefAllocs(minCut, daList, resources);
		
	}
	
	public static List<DefenderAllocation> getAllDefAllocs(
			List<ExtDWE> edges,List<DefenderAllocation> daList, int resources) {
		if(resources==0) return daList;
		if(daList.size()==0){
			for(ExtDWE edge:edges){
				DefenderAllocation da = new DefenderAllocation();
				da.addEdgeToAllocation(edge);
				daList.add(da);
			}
			return getAllDefAllocs(edges, daList, resources-1);
		}else{
			List<DefenderAllocation> newDaList = new ArrayList<DefenderAllocation>();
			for(Iterator<DefenderAllocation> iter = daList.iterator();iter.hasNext();){
				DefenderAllocation da = iter.next();
				iter.remove();
				for(ExtDWE edge: edges){
					DefenderAllocation danew = new DefenderAllocation();
					danew.setAllocation(new HashSet<ExtDWE>(da.getAllocation()));
					if(!danew.getAllocation().contains(edge)){
						danew.addEdgeToAllocation(edge);
						if(!newDaList.contains(danew)){
							newDaList.add(danew);
						}
					}
				}
			}
			return getAllDefAllocs(edges, newDaList, resources-1);
		}

	}
}
