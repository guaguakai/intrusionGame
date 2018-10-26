package teamBuildingSolvers.CompactOracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import model.teamBuildingModels.PathConstraint;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;


public class BackupCompactDefender extends MIProblem {
	
	private TeamBuildingProblem usProblem;

	public AbstractBaseGraph<INode, ExtDWE> graph;
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private List<CompactPath> adversaryPaths;
	
	public boolean buildTeam = false;
	public HashMap<INode, INode> targetMap;
	
	private int numAdvPaths;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;	
	
	public HashMap<INode, List<INode>> overflowMap;
	
	public static HashMap<List<INode>,Integer> constraints = new HashMap<List<INode>,Integer>();
	
	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public BackupCompactDefender(TeamBuildingProblem usProblem) {
		super();
		this.usProblem = usProblem;
		this.numAdvPaths = 0;
		setProblemName("CompactDef");
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		this.overflowMap = new HashMap<INode, List<INode>>();
	}

	@Override
	public void resetLP() {
		this.numAdvPaths = 0;
		this.varMap.clear();
		this.rowMap.clear();
		super.resetLP();
	}
	
	public void setAdversaryPaths(List<Double> strategy, List<CompactPath> paths){
		this.adversaryStrategy = strategy;
		this.adversaryPaths = paths;
	}
	
	public void setObjective() {
		//coefficients for the variables in the objective
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		
		final int numTargets = this.graph.vertexSet().size();
		final int index = this.usProblem.getNumResourceTypes()*numTargets;

		// Updating the objective
		for(int i=0;i<this.adversaryStrategy.size();i++){
			//double CoverageProb = 1.0/targetList.size();
			double CoverageProb = this.adversaryStrategy.get(i);
			INode target = this.adversaryPaths.get(i).target;		
			double objCoeff = target.getPayoff()*CoverageProb;
			//int index = this.varMap.get("PathValue"+(target.getId()));
			
			setObjectiveCoef( index+i+1, objCoeff);
			//i++;
			
			if ( Configuration.TRUNCATELPS ) {
				if ( objCoeff <= Configuration.EPSILON ) {
					this.disableRow(index+i+1); //this.disableRow(1+rowbuffer + j+1);
				} else {
					this.enableRow(index+i+1);
				}			
			}
			
		}
	
	}
	
	public CompactAllocation setTargetCoverage(){
		
		CompactAllocation da = new CompactAllocation();
		
		double coverage=0;
		
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		for(INode v : targetList){
			coverage = getColumnPrimal(this.varMap.get("PathValue"+(v.getId())));
			da.addCoverage(v, coverage);
		}
		
		return da;
		
	}
	
	public List<Double> getAllocation(){
		
		List<Double> resources = new ArrayList<Double>();
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 	
			double result = getColumnPrimal(this.varMap.get("Resource"+","+(k+1)));
				resources.add(result);
		}
		return resources;
	}
	
	public double getValue(){
		double gamevalue=0;
		double coverage=0;
		
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		//double CoverageProb = 1.0/targetList.size();
		
		for(int i=0;i<this.adversaryStrategy.size();i++){
			double CoverageProb = this.adversaryStrategy.get(i);
			INode target = this.adversaryPaths.get(i).target;		
			
			coverage = getColumnPrimal(this.varMap.get("PathValue"+(target.getId())));
			gamevalue -= target.getPayoff()*(1-coverage)*CoverageProb;
		}
		
		return gamevalue;
	}
	
	public Map<INode, List<Double>> setAllocation(CompactAllocation da){
		
		
		
		for(CompactPath ap : adversaryPaths){
			INode v = ap.target;
			INode target = ap.graphtarget;
			
			List<Double> resources = new ArrayList<Double>();
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 	
				double result = getColumnPrimal(this.varMap.get("Path"+(v.getId())+","+(k+1)));
				if ( result > Configuration.EPSILON){
					resources.add(result);
				}else{
					resources.add(0.0);
				}
			}
			
			da.addAllocation(target, resources);
			
			
		}
		return da.defenderAllocation;
	}

	/**
	 * AdversaryPath is already added from [numAdvPaths to getNumberOfAdversaryPaths) in usProblem.
	 * Note: call resetLP() if defender allocations have been deleted. Call setAdversaryStrategy() after resetLP().
	 */

	@Override
	public void generateData() {
		
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		
		List<Integer> RowIndices = new ArrayList<Integer>();
		List<Double> RowValues = new ArrayList<Double>();
		
		
		//Constraints on number of resources for each type
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 	
			for(INode v : targetList){
				int i = this.varMap.get("Path"+(v.getId())+","+(k+1));
				RowIndices.add(i);
				RowValues.add(1.0);
			}
			int i = this.varMap.get("Resource"+","+(k+1));
			RowIndices.add(i);
			RowValues.add(-1.0);
			
			setMatRow(this.rowMap.get("SumResources"+(k+1)), RowIndices, RowValues);
			RowIndices.clear();
			RowValues.clear();
			
		}
	
		RowIndices.clear();
		RowValues.clear();
		
		
		//Coverage of each attacker path
		for(INode current : targetList){
			
			List<INode> overflow = new ArrayList<INode>();
			RowIndices.add(this.varMap.get("PathValue"+current.getId()));
			RowValues.add(1.0);
			
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 	
				double coverage = this.usProblem.ResourceCoverage.get(k);
				
				for(INode v : targetList){
					
					//add the value of the current target
					if(current.equals(v)){
						RowIndices.add(this.varMap.get("Path"+(v.getId())+","+(k+1)));
						
						if(this.usProblem.isProbability){
							RowValues.add(-(1-Math.pow(this.usProblem.CoverageProbability.get(k),(coverage))));
							//RowValues.add(-this.usProblem.CoverageProbability.get(k));
						}else{
							RowValues.add(-1.0);
						}
						//RowValues.add(-1.0);
					
					//add the value of the overflow targets
					}else{
						if(coverage >= getWeight(v,current)){
							RowIndices.add(this.varMap.get("Path"+(v.getId())+","+(k+1)));
							//RowValues.add(-1.0);
							if(this.usProblem.isProbability){
								RowValues.add(-(1-Math.pow(this.usProblem.CoverageProbability.get(k),(coverage))));
								//RowValues.add(-this.usProblem.CoverageProbability.get(k));
							}else{
								RowValues.add(-1.0);
							}
							overflow.add(v);
						}
					}
				}
					
			}
			setMatRow(this.rowMap.get("TotalValue"+(current.getId())), RowIndices, RowValues);
			overflowMap.put(current, overflow);
			RowIndices.clear();
			RowValues.clear();
			
			
		}
		
		if(this.usProblem.buildteam){
		
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 	
				int i = this.varMap.get("Resource"+","+(k+1));
				RowIndices.add(i);
				RowValues.add(this.usProblem.costs.get(k));
				
			}
			
			setMatRow(this.rowMap.get("Budget"), RowIndices, RowValues);
			
		}
		RowIndices.clear();
		RowValues.clear();
		
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

		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type
			for ( INode v: targetList) {			
					//double numResources = this.usProblem.getNumDefenderResources().get(k);
					
					addAndSetColumn("Path"+(v.getId())+","+(k+1),BOUNDS_TYPE.DOUBLE, 0, Double.POSITIVE_INFINITY, VARIABLE_TYPE.INTEGER, 0);
					this.varMap.put("Path"+(v.getId())+","+(k+1), this.numCols);
			}
		}
		for ( INode v: targetList) {	
			addAndSetColumn("PathValue"+(v.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			this.varMap.put("PathValue"+(v.getId()), this.numCols);
		}
		
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type
			if(!this.usProblem.buildteam){
				double numResources = this.usProblem.getNumDefenderResources().get(k);
				addAndSetColumn("Resource"+","+(k+1),BOUNDS_TYPE.FIXED, numResources, numResources, VARIABLE_TYPE.INTEGER, 0);
				
			}else{
				addAndSetColumn("Resource"+","+(k+1),BOUNDS_TYPE.DOUBLE, 0, Double.POSITIVE_INFINITY, VARIABLE_TYPE.INTEGER, 0);
			}
			this.varMap.put("Resource"+","+(k+1), this.numCols);
		}
		
	}
	@Override
	public void setRowBounds() {
		
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		for ( INode v: targetList) {	
			addAndSetRow("TotalValue"+(v.getId()), BOUNDS_TYPE.UPPER, 0.0, 0.0); //or fixed
			rowMap.put("TotalValue"+(v.getId()), this.numRows);			
			
		}
		
		//restrictions on the number of resources of each type
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type	

			double numResources = 0;
			
			addAndSetRow("SumResources"+(k+1), BOUNDS_TYPE.FIXED, numResources, numResources);
			rowMap.put("SumResources"+(k+1), this.numRows);			
		}
		
		if(this.usProblem.buildteam){
		
			double budget = this.usProblem.budget;
			addAndSetRow("Budget", BOUNDS_TYPE.UPPER, budget, budget);
			rowMap.put("Budget", this.numRows);	
			
		}
		
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}

	
}
