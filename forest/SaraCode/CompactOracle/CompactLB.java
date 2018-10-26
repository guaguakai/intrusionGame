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
import model.teamBuildingModels.Constraint;
import model.teamBuildingModels.NodePair;
import model.teamBuildingModels.PathConstraint;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;


public class CompactLB extends MIProblem {
	
	private TeamBuildingProblem teamObj;

	public AbstractBaseGraph<INode, ExtDWE> graph;
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	public List<CompactPath> adversaryPaths;
	
	public boolean buildTeam = false;
	public HashMap<INode, INode> targetMap;
	
	private int numAdvPaths;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;	
	
	public HashMap<INode, List<INode>> overflowMap;
	
	public static List<Constraint> constraints = new ArrayList<Constraint>();// = new HashMap<String,Object[]>();
	public boolean useConstraints = true;
	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public CompactLB(TeamBuildingProblem usProblem, HashMap<String,Object[][]> constraints) {
		super();
		this.teamObj = usProblem;
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
		//final int index = this.usProblem.getNumResourceTypes()*numTargets;
		final int index = 0;
		

		// Updating the objective
		for(int i=0;i<this.adversaryStrategy.size();i++){
			//double CoverageProb = 1.0/targetList.size();
			double CoverageProb = this.adversaryStrategy.get(i);
			INode target = this.adversaryPaths.get(i).target;		
			double objCoeff = target.getPayoff()*CoverageProb;
			//int index = this.varMap.get("PathValue"+(target.getId()));
			
			setObjectiveCoef( index+i+1, objCoeff);
			//i++;
			
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
		for (int k=0;k<this.teamObj.getNumResourceTypes();k++){ 
			if(this.teamObj.getNumDefenderResources().get(k)>0){	
				double result = getColumnPrimal(this.varMap.get("Resource"+","+(k+1)));
				resources.add(result);
			}else{
				double result = 0;
				resources.add(result);
			}
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
	

	public void setAllocation(CompactAllocation da){
		da.team=this.teamObj;
		
		for (int k=0;k<this.teamObj.getNumResourceTypes();k++){ 
		for (int i=0;i<this.teamObj.getNumDefenderResources().get(k);i++){
			List<INode> targets = new ArrayList<INode>();
			for(CompactPath ap : adversaryPaths){
				INode v = ap.target;
				INode target = ap.graphtarget;
				
				double result = getColumnPrimal(this.varMap.get("Path"+(v.getId())+","+(k+1)+","+(i+1)));
				if ( result > Configuration.EPSILON){
					targets.add(v);
				
					for(CompactPath ap2 : adversaryPaths){
						INode v2 = ap2.target;
						if(!v2.equals(v)){
							double overflow = getColumnPrimal(this.varMap.get("OV"+(v.getId())+","+(v2.getId())+","+(k+1)+","+(i+1)));
							if ( overflow > Configuration.EPSILON){
								targets.add(v2);
							}
						}
					}
				}
			
			da.defenderAlloc.put("R,"+(k+1)+","+(i+1),targets);
			}}
			//da.addAllocation(target, resources);
			
			
		}
		//return da.defenderAllocation;
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

		for(INode v : targetList){
				int j = this.varMap.get("PathValue"+(v.getId()));
				RowIndices.add(j);
				RowValues.add(1.0);
				
		}
		
		setMatRow(this.rowMap.get("SumResources"), RowIndices, RowValues);		
		
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
		
		
		for ( INode v: targetList) {	
			addAndSetColumn("PathValue"+(v.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			this.varMap.put("PathValue"+(v.getId()), this.numCols);
		}
		
		
	}
	
	@Override
	public void setRowBounds() {
		
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		int resources = teamObj.getTotalResources();
		
		addAndSetRow("SumResources", BOUNDS_TYPE.FIXED, resources, resources);
		rowMap.put("SumResources", this.numRows);		

	
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}

	
}
