package teamBuildingSolvers.CompactOracle;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lpWrapper.Configuration;
import lpWrapper.AMIProblem.BOUNDS_TYPE;
import lpWrapper.AMIProblem.VARIABLE_TYPE;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.teamBuildingModels.CompactAllocation;
import model.teamBuildingModels.CompactPath;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.graph.AbstractBaseGraph;

import cpWrapper.CPProblem;
import cpWrapper.CPProblem.OBJECTIVE_TYPE;

public class CompactDefenderP extends CPProblem{

	private TeamBuildingProblem team;
	
	public AbstractBaseGraph<INode, ExtDWE> graph;
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private List<CompactPath> adversaryPaths;
	public List<INode> targetList;
	public boolean buildTeam = false;
	public HashMap<INode, INode> targetMap;
	
	private int numAdvPaths;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;	
	
	private double[] escapeProb;
	boolean loaded=false;
	IloNumExpr ObjectiveFunction;
	
	public HashMap<INode, List<INode>> overflowMap;
	
	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public CompactDefenderP(TeamBuildingProblem team) {
		super();
		this.team = team;
		setProb();
		this.numAdvPaths = 0;
		this.varMap = new HashMap<String, Integer>();
		this.overflowMap = new HashMap<INode, List<INode>>();
	
	}
	public void setProb(){
		this.escapeProb=new double[this.team.CoverageProbability.size()];
		
		for(int i=0;i<escapeProb.length;i++){
			escapeProb[i]=1-this.team.CoverageProbability.get(i);
		}
	}
	public void setGraph(AbstractBaseGraph<INode, ExtDWE> graph){
		this.graph = graph;
		targetList = new ArrayList<INode>(this.graph.vertexSet());
	}
	public void setAdversaryPaths(List<Double> strategy, List<CompactPath> paths){
		this.adversaryStrategy = strategy;
		this.adversaryPaths = paths;
		
		if(loaded){
			this.Objective();
		}
	}
	
	public void Objective(){
		
		IloNumExpr[] targetCoverage = new IloNumExpr[targetList.size()];
		double[] objCoeff = new double[targetList.size()];
		
		for(int i=0;i<this.adversaryStrategy.size();i++){
			double CoverageProb = this.adversaryStrategy.get(i);
			INode target = this.adversaryPaths.get(i).target;		
			objCoeff[i] = target.getPayoff()*CoverageProb;
			
			targetCoverage[i] = this.getVar("Path"+target.getId());
		}
		
		ObjectiveFunction = this.scalProdSumArray(objCoeff, targetCoverage);
		this.updateObjective(ObjectiveFunction);
		
	}
	
	public CompactAllocation setTargetCoverage(){
		
		CompactAllocation da = new CompactAllocation();
		
		double coverage=0;
		
		List<INode> targetList = new ArrayList<INode>(this.graph.vertexSet());
		Collections.sort(targetList);
		
		for(INode v : targetList){
			coverage = this.cp.getValue((this.getVar("Path"+(v.getId()))));
			da.addCoverage(v, coverage);
		}
		setAllocation(da);
		return da;
		
	}
	
	public List<Double> getAllocation(){
		
		List<Double> resources = new ArrayList<Double>();
		for (int k=0;k<this.team.getNumResourceTypes();k++){ 	
			double result = this.cp.getValue(this.getVar("Resource"+","+(k+1)));
				resources.add(result);
		}
		return resources;
	}
	
	public double getValue(){
		try {
			return this.cp.getObjValue();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Double.NEGATIVE_INFINITY;
	}
	
public Map<INode, List<Double>> setAllocation(CompactAllocation da){
		
		
		
		for(CompactPath ap : adversaryPaths){
			INode v = ap.target;
			INode target = ap.graphtarget;
			
			List<Double> resources = new ArrayList<Double>();
			for (int k=0;k<this.team.getNumResourceTypes();k++){ 	
				double result = this.cp.getValue(this.getVar("Path"+(v.getId())+","+(k+1)));
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
	
	public void addRowConstraints(){

		
		IloIntVar[] path = new IloIntVar[targetList.size()];	
		
		for (int k=0;k<this.team.getNumResourceTypes();k++){		
				for(int v=0;v<targetList.size();v++){
					path[v] = this.getVar("Path"+targetList.get(v).getId()+","+(k+1));
				}
				this.addConstraint(this.sumVarArray(path), this.team.getNumDefenderResources().get(k).intValue());

		}
		
		//-------//

		List<IloIntVar> overflowCoverage = new ArrayList<IloIntVar>();
		IloNumExpr[] pathCoverage = new IloNumExpr[targetList.size()];
		IloNumExpr[] resourceCoverage = new IloNumExpr[this.team.getNumResourceTypes()];
		
		for(int v=0;v<targetList.size();v++){
			for (int k=0;k<this.team.getNumResourceTypes();k++){ 	
				double coverage = this.team.ResourceCoverage.get(k);
				overflowCoverage.add( this.getVar( "Path"+(targetList.get(v).getId())+","+(k+1) ));
			
				for(int v2=0;v2<targetList.size();v2++){
					if(v2!=v && (coverage >= getWeight(targetList.get(v2),targetList.get(v)))){
						overflowCoverage.add( this.getVar( "Path"+(targetList.get(v2).getId())+","+(k+1) ));
					}
				}
				resourceCoverage[k]=this.sumVarArray(overflowCoverage);
			}
			
			this.addLEConstraint(this.getVar("Path"+targetList.get(v).getId()),this.sumVarArray(overflowCoverage) );
			overflowCoverage.clear();
		}
		
		
	}
	public void addDecisionVars(){
		for ( INode v: targetList) {
			this.addDecisionVar("Path"+v.getId(), 0, 1);
			for (int k=0;k<this.team.getNumResourceTypes();k++){ //for each resource type
				this.addDecisionVar("Path"+v.getId()+","+(k+1), 0, this.team.getNumDefenderResources().get(k).intValue());
			}
		}
		
	}
	public void loadProblem() {
		try {			
			this.objectiveType=OBJECTIVE_TYPE.MAX;
			this.addDecisionVars();
			this.addRowConstraints();
			this.Objective();
			this.loaded=true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	@Override
	public void writeProb(String string){
		String constraints = this.constraints.toString();
		String constraintVal = this.constraintVal.toString();
		String objective = this.objectiveFunction.toString();
		String vars = this.variables.toString();
		
		System.out.println(objective+"\n"+constraints+"\n"+constraintVal  +"\n"+vars);
	}
}
