package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;

public class PathCoverage extends MIProblem {
	public List<CompactPath> ap;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;
	TeamBuildingProblem team;
	int type;
	public PathCoverage(TeamBuildingProblem team, List<CompactPath> ap, int type){
		super();
		this.ap = ap;
		this.team = team;
		this.type=type;
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}
	
	@Override
	public void resetLP() {
		this.varMap.clear();
		this.rowMap.clear();
		super.resetLP();
	}
	
	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
		
	}

	@Override
	protected void setColBounds() {
		
		addAndSetColumn("edges",BOUNDS_TYPE.FIXED, this.team.ResourceCoverage.get(type-1), this.team.ResourceCoverage.get(type-1), VARIABLE_TYPE.INTEGER, 0);
		this.varMap.put("edges", this.numCols);
		
		
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.team.getGraph().edgeSet());
		Collections.sort(edgeList);
		
		for ( ExtDWE e: edgeList) {			
			if(e.getType().equals(EDGE_TYPE.NORMAL)){
				addAndSetColumn("l"+","+(e.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			}else{
				addAndSetColumn("l"+","+(e.getId()),BOUNDS_TYPE.FIXED, 0, 0, VARIABLE_TYPE.INTEGER, 0);
			}
			this.varMap.put("l"+","+(e.getId()), this.numCols);
		}
		
		List<INode> nodeList = new ArrayList<INode>(this.team.getGraph().vertexSet());
		Collections.sort(nodeList);

			for(INode n : nodeList){	
				addAndSetColumn("n"+","+(n.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
				this.varMap.put("n"+","+(n.getId()), this.numCols);
			}
			

		
	}

	@Override
	protected void setRowBounds() {
		addAndSetRow("NumEdges", BOUNDS_TYPE.FIXED, 0, 0);
		rowMap.put("NumEdges", this.numRows);
		
		List<INode> nodeList = new ArrayList<INode>(this.team.getGraph().vertexSet());
		Collections.sort(nodeList);
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.team.getGraph().edgeSet());
		Collections.sort(edgeList);
		
		for(ExtDWE e: edgeList){
			addAndSetRow("Node"+"t"+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
			rowMap.put("Node"+"t"+e.getId(), this.numRows);
					
			addAndSetRow("Node"+"s"+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
			rowMap.put("Node"+"s"+e.getId(), this.numRows);

		}
		
		
		addAndSetRow("SumNodes", BOUNDS_TYPE.FIXED, 1, 1);
		rowMap.put("SumNodes", this.numRows);
		
		
		
	}

	@Override
	protected void generateData() {
		List<Integer> thisRowIndices = new ArrayList<Integer>();
		List<Double> thisRowValues = new ArrayList<Double>();
		
		thisRowIndices.add(this.varMap.get("edges"));
		thisRowValues.add(1.0);
		
		for ( ExtDWE e : this.team.getGraph().edgeSet() ) {			//for each edge e
			thisRowIndices.add(this.varMap.get("l"+"," + e.getId())); 
			thisRowValues.add(-1.0);
		}
		setMatRow(this.rowMap.get("NumEdges"), thisRowIndices, thisRowValues);
		
		thisRowIndices.clear();
		thisRowValues.clear();
		
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.team.getGraph().edgeSet());
		Collections.sort(edgeList);
		List<INode> nodeList = new ArrayList<INode>(this.team.getGraph().vertexSet());
		Collections.sort(nodeList);
		Set<ExtDWE> out;
		Set<ExtDWE> in;
		
		for(ExtDWE e : edgeList){
			INode n1 = e.getsource();
			INode n2 = e.gettarget();
					
			thisRowIndices.add(this.varMap.get("l"+"," + e.getId())); 
			thisRowValues.add(-1.0);
					
			thisRowIndices.add(this.varMap.get("n"+"," + n1.getId())); 
			thisRowValues.add(1.0);
	
			setMatRow(this.rowMap.get("Node"+"s"+e.getId()), thisRowIndices, thisRowValues);
			thisRowIndices.clear();
			thisRowValues.clear();
					
			thisRowIndices.add(this.varMap.get("l"+","+ e.getId())); 
			thisRowValues.add(-1.0);
					
			thisRowIndices.add(this.varMap.get("n"+","+ n2.getId())); 
			thisRowValues.add(1.0);

			setMatRow(this.rowMap.get("Node"+"t"+e.getId()), thisRowIndices, thisRowValues);
			thisRowIndices.clear();
			thisRowValues.clear();

		}
				
		for(INode n : nodeList){
				
				thisRowIndices.add(this.varMap.get("n"+"," + n.getId())); 
				thisRowValues.add(1.0);		
		}
		
		thisRowIndices.add(this.varMap.get("edges"));
		thisRowValues.add(-1.0);
		
		setMatRow(this.rowMap.get("SumNodes"), thisRowIndices, thisRowValues);
		thisRowIndices.clear();
		thisRowValues.clear();
			
			
		
		
		Set<ExtDWE> all = new HashSet<ExtDWE>();
		
		for ( ExtDWE e: edgeList) {	
			thisRowIndices.add(this.varMap.get("l"+"," + e.getId())); 
			thisRowValues.add(-1.0);
					
			out = this.team.getGraph().outgoingEdgesOf(e.gettarget());
			in = this.team.getGraph().incomingEdgesOf(e.getsource());
			all.addAll(in);
					
					for(ExtDWE edge : out){
						if(!all.contains(edge)){
							all.add(edge);
						}
					}
					
					
			for(ExtDWE e2 : all){
				if((e==e2)){ 
					thisRowIndices.add(this.varMap.get("l"+","+ e2.getId())); 
					thisRowValues.add(-1.0);
				}else if((out.contains(e2) ||in.contains(e2))){
					thisRowIndices.add(this.varMap.get("l"+","+ e2.getId())); 
					thisRowValues.add(1.0);
				}
					
		
			}

		}
		thisRowIndices.clear();
		thisRowValues.clear();
		
		addAndSetColumn("Z", BOUNDS_TYPE.DOUBLE, 0, this.ap.size(), VARIABLE_TYPE.INTEGER, 0);
		this.varMap.put("Z", this.numCols);
		
		addAndSetRow("sumZ",BOUNDS_TYPE.FIXED, 0.0, 0.0);
		this.rowMap.put("sumZ", this.numRows);
		
		int j=0;
		for(CompactPath path : ap){
			
			addAndSetColumn("z"+(j+1), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			this.varMap.put("z"+(j+1), this.numCols);
			
			addAndSetRow("setZ"+(j),BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
			this.rowMap.put("setZ"+(j), this.numRows);
			
			
			
			thisRowIndices.add(this.varMap.get("z"+(j+1)));
			thisRowValues.add(1.0);
			
			
			
			for(ExtDWE e : path.graphEdges){
				thisRowIndices.add(this.varMap.get("l"+","+ e.getId())); 
				thisRowValues.add(-1.0);
			}
			
			this.setMatRow(this.rowMap.get("setZ"+(j)), thisRowIndices, thisRowValues);
			j++;
			thisRowIndices.clear();
			thisRowValues.clear();

		}
		
		setObjective();
		
	}
	
	public void setObjective(){
		int index = this.varMap.get("Z");
		
		for(int i=0;i<ap.size();i++){
			setObjectiveCoef( index+1+i, 1.0);
		}

	}
}
