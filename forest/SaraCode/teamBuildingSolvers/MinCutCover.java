package teamBuildingSolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.AbstractBaseGraph;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import lpWrapper.AMIProblem.BOUNDS_TYPE;
import lpWrapper.AMIProblem.VARIABLE_TYPE;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.teamBuildingModels.CompactAllocation;
import model.teamBuildingModels.CompactPath;
import model.teamBuildingModels.Constraint;
import model.teamBuildingModels.NodePair;
import model.teamBuildingModels.PathConstraint;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;


public class MinCutCover extends MIProblem {
	
	private TeamBuildingProblem teamProblem;

	public AbstractBaseGraph<INode, ExtDWE> graph;
	public HashMap<ExtDWE, Integer> edgeMap = new HashMap<ExtDWE, Integer>();
	public HashMap<INode, INode> targetMap;
	
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;	
	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from teamProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public MinCutCover(TeamBuildingProblem teamProblem, HashMap<ExtDWE, Integer> edgeMap) {
		super();
		this.teamProblem = teamProblem;
		this.edgeMap = edgeMap;
		setProblemName("MinCutOracle");
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}

	@Override
	public void resetLP() {
		this.varMap.clear();
		this.rowMap.clear();
		super.resetLP();
	}
	
	public void setObjective() {
		//coefficients for the variables in the objective
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		
		final int numTargets = this.graph.vertexSet().size();
		//final int index = this.teamProblem.getNumResourceTypes()*numTargets;
		final int index = this.teamProblem.getTotalResources()*numTargets*numTargets;
		

		// Updating the objective
		int i=0;
		for(ExtDWE edge : this.edgeMap.keySet()){
			
			int targetVal = this.edgeMap.get(edge);
			double objCoeff = targetVal;

			setObjectiveCoef( index+i+1, objCoeff);
			i++;
			
			if ( Configuration.TRUNCATELPS ) {
				if ( objCoeff <= Configuration.EPSILON ) {
					//this.disableRow(index+i+1); //this.disableRow(1+rowbuffer + j+1);
				} else {
					this.enableRow(index+i+1);
				}			
			}
			
		}
	
	}
	
	public double getCutWeight(){
		
		return this.getLPObjective();
		
	}
	
	

	/**
	 * AdversaryPath is already added from [numAdvPaths to getNumberOfAdversaryPaths) in teamProblem.
	 * Note: call resetLP() if defender allocations have been deleted. Call setAdversaryStrategy() after resetLP().
	 */

	@Override
	public void generateData() {
		/* Problem:
		 * max_{z,\lambda} -\sum_j{(1-z_j)a_jT_{t_j} = max_{z,\lambda} \sum_j (z_j a_j T_{t_j})
		 * s.t. z_j - \sum_e{A_{je} \lambda_e} <= 0

		 * 		\sum_e \lambda_e <= k
		 *		\lambda_e \in {0,1}
		 *		z_j \in [0,1]
		 */
		final int numEdges = this.teamProblem.getGraph().edgeSet().size();
		List<Integer> firstRowIndices = new ArrayList<Integer>();
		List<Double> firstRowValues = new ArrayList<Double>();
		List<Integer> thisRowIndices = new ArrayList<Integer>();
		List<Double> thisRowValues = new ArrayList<Double>();
		
		List<Double> sumValues = new ArrayList<Double>();

		// Add elems for \lambda <= k
		firstRowIndices.clear();
		firstRowValues.clear();
		
		//creates a row of zeroes and ones
		for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ 					//for each resource type k
			for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type i
				for ( ExtDWE e : this.teamProblem.getGraph().edgeSet() ) {			//for each edge e
					firstRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					firstRowValues.add(0.0);
					sumValues.add(1.0);
				}
			}
		}
		
		//sets coeffs for edges
		for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ 					//for each resource type k
			for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type i
				for ( ExtDWE e : this.teamProblem.getGraph().edgeSet() ) {			//for each edge e
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(1.0);
				}
				setMatRow(this.rowMap.get("SumLambda"+(k+1)+","+(i+1)), firstRowIndices, firstRowValues);
				setMatRow(this.rowMap.get("SumLambda"+(k+1)+","+(i+1)), thisRowIndices, thisRowValues);
				
				//this.updateAdversaryPaths();
				
				thisRowIndices.clear();
				thisRowValues.clear();
			}
		}
		
		//sets total sum
		setMatRow(this.rowMap.get("SumLambda"), firstRowIndices, sumValues);
		thisRowIndices.clear();
		thisRowValues.clear();
		
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.teamProblem.getGraph().edgeSet());
		List<ExtDWE> visted = new ArrayList<ExtDWE>();
		
		List<INode> nodeList = new ArrayList<INode>(this.teamProblem.getGraph().vertexSet());
		Collections.sort(nodeList);
		Set<ExtDWE> out;
		Set<ExtDWE> in;
		
		

		for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ //for each resource type k
			for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
				for(ExtDWE e : edgeList){
					INode n1 = e.getsource();
					INode n2 = e.gettarget();
					
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(-1.0);
					
					thisRowIndices.add(this.varMap.get("n"+","+(k+1)+","+(i+1) +"," + n1.getId())); 
					thisRowValues.add(1.0);
	
					setMatRow(this.rowMap.get("Node"+","+(k+1)+","+(i+1)+"s"+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
					
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(-1.0);
					
					thisRowIndices.add(this.varMap.get("n"+","+(k+1)+","+(i+1) +"," + n2.getId())); 
					thisRowValues.add(1.0);

					setMatRow(this.rowMap.get("Node"+","+(k+1)+","+(i+1)+"t"+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
					
					ExtDWE dual = this.teamProblem.getGraph().getEdge(n2, n1);
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(1.0);
					
					if(dual!=null){
						thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + dual.getId())); 
						thisRowValues.add(1.0);
					}
					
					setMatRow(this.rowMap.get("Edge"+","+(k+1)+","+(i+1)+","+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
				}
				
			}}
		
		
		for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ //for each resource type k
		for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
			for(INode n : nodeList){
				
				thisRowIndices.add(this.varMap.get("n"+","+(k+1)+","+(i+1) +"," + n.getId())); 
				thisRowValues.add(1.0);
				
			}
			setMatRow(this.rowMap.get("SumNODES"+","+(k+1)+","+(i+1)), thisRowIndices, thisRowValues);
			thisRowIndices.clear();
			thisRowValues.clear();
			
		}
		}	
		
		
		Set<ExtDWE> all = new HashSet<ExtDWE>();
		Set<ExtDWE> in2;
		
		Collections.sort(edgeList);
		
		for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ //for each resource type k
			for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
				for ( ExtDWE e: edgeList) {	
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(-1.0);
					
					out = this.teamProblem.getGraph().outgoingEdgesOf(this.getto(e));
					in = this.teamProblem.getGraph().incomingEdgesOf(this.getfrom(e));
					all.addAll(in);
					
					for(ExtDWE edge : out){
						if(!all.contains(edge)){
							all.add(edge);
						}
					}
					
					
					for(ExtDWE e2 : all){
						if((e==e2)){ //|| e2.gettarget().equals(e.getsource())) && e2.getType()!=EDGE_TYPE.VIRTUAL){
							thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
							thisRowValues.add(-1.0);
						}else if((out.contains(e2) ||in.contains(e2))){
							thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
							thisRowValues.add(1.0);
						}
					}
					
					setMatRow(this.rowMap.get("Path"+(k+1)+","+(i+1)+","+e.getId()), firstRowIndices, firstRowValues);
					setMatRow(this.rowMap.get("Path"+(k+1)+","+(i+1)+","+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
					all.clear();
					
				}
			}
		}
		
			
				firstRowIndices.clear();
				firstRowValues.clear();
				sumValues.clear();

		this.setObjective();
	}
	
	public double getWeight(INode n1, INode n2){
		if(graph.containsEdge(n1, n2)){
			for ( Iterator<ExtDWE> edgeItr = graph.edgesOf(n1).iterator(); edgeItr.hasNext();){
				ExtDWE edge = edgeItr.next();
				if(edge.gettarget().equals(n2)){
					return edge.getweight();
				}
			}
			return Double.POSITIVE_INFINITY;
			
		}else{
			return Double.POSITIVE_INFINITY;
		}
		
	}
	

	@Override
	public void setColBounds() {

		
			List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.teamProblem.getGraph().edgeSet());
			Collections.sort(edgeList);
			
			for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ //for each resource type
				for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type
				
					for ( ExtDWE e: edgeList) {			
						if(e.getType().equals(EDGE_TYPE.NORMAL)){
							addAndSetColumn("l"+","+(k+1)+","+(i+1) +","+(e.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
						}else{
							addAndSetColumn("l"+","+(k+1)+","+(i+1) +","+(e.getId()),BOUNDS_TYPE.FIXED, 0, 0, VARIABLE_TYPE.INTEGER, 0);
						}
						this.varMap.put("l"+","+(k+1)+","+(i+1) +","+(e.getId()), this.numCols);
					}
				}
			}
			List<INode> nodeList = new ArrayList<INode>(this.teamProblem.getGraph().vertexSet());
			Collections.sort(nodeList);
			for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ //for each resource type
				for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type
				
					for(INode n : nodeList){	
						addAndSetColumn("n"+","+(k+1)+","+(i+1) +","+(n.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
						this.varMap.put("n"+","+(k+1)+","+(i+1) +","+(n.getId()), this.numCols);
					}
				}
			}
		
	}
	@Override
	public void setRowBounds() {
		
		/*
		 * Rows: \sum_\lambda <= k 
		 * setting z_j: one for each path
		 * Number of Rows: 1 + |A|
		 */
		// LPX_FX to fix that always numResources are deployed
		
		//Fix the coverage of each resource 
		//addAndSetRow("SumLambda", BOUNDS_TYPE.FIXED, this.teamProblem.getTotalCoverage(), this.teamProblem.getGraph().edgeSet().size());
		//rowMap.put("SumLambda", this.numRows);
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.teamProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		
		for (int j=0;j<this.teamProblem.getNumResourceTypes();j++){
			for (int i=0;i< this.teamProblem.getNumDefenderResources().get(j);i++){
				//SETS THE ROWS FOR THE EDGE SETS SUM LAMBDA
				addAndSetRow("SumLambda"+(j+1)+","+(i+1), BOUNDS_TYPE.FIXED, this.teamProblem.ResourceCoverage.get(j), this.teamProblem.ResourceCoverage.get(j));
				rowMap.put("SumLambda"+(j+1)+","+(i+1), this.numRows);
				
				for ( ExtDWE e: edgeList) {	
					if(teamProblem.ResourceCoverage.get(j).equals(1.0)){
						addAndSetRow("Path"+(j+1)+","+(i+1)+","+e.getId(), BOUNDS_TYPE.LOWER, Double.NEGATIVE_INFINITY, 0.0);
					}else{
						addAndSetRow("Path"+(j+1)+","+(i+1)+","+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
					}
					rowMap.put("Path"+(j+1)+","+(i+1)+","+e.getId(), this.numRows);
				}
				
					
					
			}
		}
		
		List<INode> nodeList = new ArrayList<INode>(this.teamProblem.getGraph().vertexSet());
		Collections.sort(nodeList);

			
		for (int k=0;k<this.teamProblem.getNumResourceTypes();k++){ //for each resource type
			for (int i=0;i< this.teamProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type
				
				addAndSetRow("SumNODES"+","+(k+1)+","+(i+1), BOUNDS_TYPE.FIXED, this.teamProblem.ResourceCoverage.get(k)+1, this.teamProblem.ResourceCoverage.get(k)+1);
				rowMap.put("SumNODES"+","+(k+1)+","+(i+1), this.numRows);
				
				for(ExtDWE e: edgeList){
					addAndSetRow("Node"+","+(k+1)+","+(i+1)+"t"+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
					rowMap.put("Node"+","+(k+1)+","+(i+1)+"t"+e.getId(), this.numRows);
					
					addAndSetRow("Node"+","+(k+1)+","+(i+1)+"s"+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
					rowMap.put("Node"+","+(k+1)+","+(i+1)+"s"+e.getId(), this.numRows);
					
					double val =1.0;
					if(teamProblem.ResourceEdges!=null && !e.getType().equals(teamProblem.ResourceEdges.get(k))){
						val=0;
					}
					addAndSetRow("Edge"+","+(k+1)+","+(i+1)+","+e.getId(), BOUNDS_TYPE.UPPER, val, val);
					rowMap.put("Edge"+","+(k+1)+","+(i+1)+","+e.getId(), this.numRows);
				}
			}
		}
		
		addAndSetRow("SumLambda", BOUNDS_TYPE.FIXED, this.teamProblem.getTotalCoverage(), this.teamProblem.getTotalCoverage());
		rowMap.put("SumLambda", this.numRows);
		
		
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}
	public INode getto(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(teamProblem.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: this.teamProblem.getGraph().incomingEdgesOf(curNode)){
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}
	public INode getfrom(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(teamProblem.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: this.teamProblem.getGraph().outgoingEdgesOf(curNode)){
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}


	
}
