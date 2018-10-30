package cpWrapper;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import cpWrapper.CPProblem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.AbstractBaseGraph;

import lpWrapper.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;


public class BRDefenderP extends CPProblem {
	
	public double[] escapeProb;
	
	public int numResources;
	
	public TeamBuildingProblem team;
	public AbstractBaseGraph graph;
	List<ExtDWE> edgeList;
	HashMap<ExtDWE, IloIntVar[]> edgeMap;
	
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private int numAdvPaths;
	
	IloNumExpr ObjectiveFunction;
	
	public BRDefenderP(TeamBuildingProblem team){
		super();
		this.team = team;
		this.graph = team.getGraph();
		this.edgeList = new ArrayList<ExtDWE>(this.team.getGraph().edgeSet());
		this.numResources=team.getTotalResources();
		this.initializePaths();
		this.getEscapeProb();
		
	}
	
	private void getEscapeProb(){
		escapeProb = new double[team.CoverageProbability.size()];
		for(int i=0; i<team.CoverageProbability.size();i++){
			escapeProb[i] = 1-team.CoverageProbability.get(i);
		}
	}
	
	public double getDefenderPayoff(){
		try {
			return this.cp.getObjValue();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public void setAdversaryStrategy(List<Double> adversaryStrategy){
		this.adversaryStrategy=adversaryStrategy;
		this.numAdvPaths=adversaryStrategy.size();
		
		this.Objective();
	}
	private void initializePaths(){
		
		this.team.getMinCutPaths();
		
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<this.team.getActProfile().getNumberOfAdversaryPaths();i++){
			uniform.add(1.0/this.team.getActProfile().getNumberOfAdversaryPaths());
		}
		this.adversaryStrategy=uniform;
		this.numAdvPaths=uniform.size();
	}
	

	public DefenderAllocation getDefenderAllocation(){
		DefenderAllocation da = new DefenderAllocation();
		for (int k=0;k<this.team.getNumResourceTypes();k++){ //for each resource type k
			for (int i=0;i< this.team.getNumDefenderResources().get(k);i++){ //for each resource of that type i
				for (ExtDWE edge : edgeList ) {
					if ( cp.getValue(this.getVar("Lambda"+edge.getId()+","+k+","+i)) > Configuration.EPSILON){
						if(!da.contains(edge))	{
							da.addEdgeToAllocation(edge);
						}
						da.addEdgetoResource(edge,"Resource"+(k+1)+","+(i+1));
						da.addProbability(edge,this.team.CoverageProbability.get(k));
						da.addResourcetoEdge(edge, k);
					}
				}
			}
		}
		return da;
		
	}
	public void Objective(){
		
		IloNumExpr[] payoffs = new IloNumExpr[numAdvPaths];	//expected payoff for each attacker path
		IloNumExpr[] PathIntersect;
		
		IloNumExpr[] edgeCoverage = getEdgeCoverage(); 		//defender coverage of all edges
		IloNumExpr[] adversaryCoverage = null;
		IloNumExpr probAlongPath = null;
		
		for(int i=0;i<numAdvPaths;i++){
			double apProbability = adversaryStrategy.get(i);
			adversaryCoverage = getAdversaryPath(i, edgeCoverage);				//coverage of all edges by adversary path AP
			probAlongPath = this.productArray(adversaryCoverage);//probability of success of an attacker path
			
			//payoff on path i
			payoffs[i] = this.product(probAlongPath, -team.getActProfile().getAdversaryPath(i).getTargetPayoff()*apProbability);	
			
		}
		
		if(payoffs.length>1){
			ObjectiveFunction = this.sumExprArray(payoffs);
		}else{
			ObjectiveFunction = payoffs[0];
		}
		this.updateObjective(ObjectiveFunction);

	}
	
	/**
	 * This method returns the probability that the defender fails at covering an edge
	 * corresponds to an attacker successfully traversing an edge
	 * 
	 * @return array[e] = P(e,X) = (1-Probability of coverage)^m_k
	 */
	private IloNumExpr[] getEdgeCoverage(){
		
		IloNumExpr[] edgeCoverage = new IloNumExpr[edgeList.size()]; //probability of failure on each edge in the graph
		
		int j=0;
		for(ExtDWE e : edgeList){
			
		
			IloIntVar[] resourcesOnEdge = new IloIntVar[this.team.getTotalResources()];
			int m =0;
			for (int k=0;k<this.team.getNumResourceTypes();k++){
				for (int i=0;i< this.team.getNumDefenderResources().get(k);i++){
					resourcesOnEdge[m]=this.getVar("Lambda"+e.getId()+","+k+","+i);
					m++;
				}
			}
			edgeCoverage[j] = this.productArray(this.exponentArray(escapeProb, resourcesOnEdge));
			j++;
		}
		return edgeCoverage;
	}
	
	private IloNumExpr[] getAdversaryPath(int i, IloNumExpr[] DefEdges){
		
		AdversaryPath ap = this.team.getActProfile().getAdversaryPath(i);
		IloNumExpr[] pathCoverage = new IloNumExpr[ap.size()];
		
		int j=0,k=0;
		
		for(ExtDWE e : edgeList ){
			if(ap.isInPath(e)){
				pathCoverage[k] = DefEdges[j];
				k++;
			}
			j++;
		}
		return pathCoverage;
		
	}
	private double[] getAdversaryCoverage(int i){
		double[] adversaryCoverage = new double[edgeList.size()];
		
		AdversaryPath ap = this.team.getActProfile().getAdversaryPath(i);
		double apProbability = adversaryStrategy.get(i);
		
		int j=0;
		for(ExtDWE e : edgeList ){
			if(ap.isInPath(e)){
				adversaryCoverage[j] = apProbability;
			}else{
				adversaryCoverage[j] = 0;
			}
			j++;
		}
		return adversaryCoverage;
	}
	
	public void addDecisonVariables(){
		for(ExtDWE e : edgeList){
			for(int k=0;k<team.getNumResourceTypes();k++){
				for(int i=0;i<this.team.getNumDefenderResources().get(k).intValue();i++){
							
					if(e.getType().equals(EDGE_TYPE.NORMAL)){
						this.addDecisionVar("Lambda"+e.getId()+","+k+","+i, 0, 1);
					}else{
						this.addDecisionVar("Lambda"+e.getId()+","+k+","+i, 0, 0);
					}
				}
			}
		}
	}
	
	public void addRowConstraints(){
		//A resource can only cover x number of edges
		for (int k=0;k<this.team.getNumResourceTypes();k++){
			for (int i=0;i< this.team.getNumDefenderResources().get(k);i++){
				
				IloIntVar[] edges = new IloIntVar[edgeList.size()];
				
				for(int e=0;e<edgeList.size();e++){
					edges[e] = this.varMap.get("Lambda"+edgeList.get(e).getId()+","+k+","+i);
				}
				this.addConstraint(this.sumVarArray(edges), this.team.ResourceCoverage.get(k).intValue());
			}
		}
		
		
		ArrayList<IloIntVar> path = new ArrayList<IloIntVar>();
		ArrayList<Double> pathConst = new ArrayList<Double>();
		
		Set<ExtDWE> out;
		Set<ExtDWE> in;

		for (int k=0;k<this.team.getNumResourceTypes();k++){ 					//for each resource type k
			int coverage = this.team.ResourceCoverage.get(k).intValue();
			
			if(coverage!=1){
			for (int i=0;i< this.team.getNumDefenderResources().get(k);i++){ 	//for each resource of that type i
				for ( ExtDWE e: edgeList) {	
					path.clear();
					pathConst.clear();
					
					for ( ExtDWE e2: edgeList) {	
						out = this.team.getGraph().edgesOf(e.gettarget());
						in = this.team.getGraph().edgesOf(e.getsource());
						
						if(e==e2){
							path.add(this.varMap.get("Lambda"+e2.getId()+","+k+","+i));
							pathConst.add(-1.0);
						}else if(out.contains(e2)||in.contains(e2)){
							path.add(this.varMap.get("Lambda"+e2.getId()+","+k+","+i));
							pathConst.add(1.0);
						}
					}
					IloNumExpr exrp = this.scalProdSumArray(pathConst, path);					
					this.addBoundConstraint(exrp,0,Integer.MAX_VALUE);
					
				}
			}
			}
		}
		
		//Fix the number of resources "sumlambda"
		this.addConstraint(this.sumVarArray(this.getDecisionVars()),this.team.getTotalCoverage());
		
		
	}
	public void writeProblem(){
		String constraints = this.constraints.toString();
		String constraintVal = this.constraintVal.toString();
		String objective = this.objectiveFunction.toString();
		String vars = this.variables.toString();
		
		System.out.println(objective+"\n"+constraints+"\n"+constraintVal  +"\n"+vars);
	}
	
	
	public void loadProblem() {
		try {			
			this.objectiveType=OBJECTIVE_TYPE.MAX;
			this.addDecisonVariables();
			this.Objective();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	

}
