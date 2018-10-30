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
import java.util.Set;

import org.jgrapht.graph.AbstractBaseGraph;

import cpWrapper.CPProblem;
import cpWrapper.CPProblem.OBJECTIVE_TYPE;
import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import lpWrapper.AMIProblem.BOUNDS_TYPE;
import lpWrapper.AMIProblem.VARIABLE_TYPE;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.teamBuildingModels.CompactAllocation;
import model.teamBuildingModels.CompactPath;
import model.teamBuildingModels.NodePair;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;


public class MatrixCompactDefender_P extends CPProblem {
	
	private TeamBuildingProblem team;
	private double[] escapeProb;

	public NodePair[][] distances;
	public List<NodePair> intersections;
	
	public int numIntersection;
	public int numPaths;
	
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private List<CompactPath> adversaryPaths;
	
	public boolean buildTeam = false;
	
	private int numAdvPaths;
	
	boolean loaded=false;
	IloNumExpr ObjectiveFunction;
	
	private Map<String,IloNumExpr> tmpVars = new HashMap<String,IloNumExpr>();
	
	private Map<NodePair,Map<Integer,List<NodePair>>> overflowMap;
	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public MatrixCompactDefender_P(TeamBuildingProblem usProblem) {
		super();
		this.team = usProblem;
		this.numAdvPaths = 0;
		setProb();
		this.redirectOutput(null);
		this.overflowMap = new HashMap<NodePair,Map<Integer,List<NodePair>>>();
		this.intersections = new ArrayList<NodePair>();
	}
	
	public void setProb(){
		this.escapeProb=new double[this.team.CoverageProbability.size()];
		
		for(int i=0;i<escapeProb.length;i++){
			escapeProb[i]=1-this.team.CoverageProbability.get(i);
		}
	}
	
	public void setAdversaryPaths(List<Double> strategy, List<CompactPath> paths){
		this.adversaryStrategy = strategy;
		this.adversaryPaths = paths;
		
		if(loaded){
			this.Objective();
		}
	}
	
	public void Objective() {
		
		IloNumExpr[] targetCoverage = new IloNumExpr[numPaths];
		double[] objCoeff = new double[numPaths];
		
		
		for (int i=0;i<numPaths;i++) {	
			double CoverageProb = this.adversaryStrategy.get(i);
			INode t = adversaryPaths.get(i).graphtarget;
			objCoeff[i] = -1*t.getPayoff()*CoverageProb;
			
			targetCoverage[i] = this.tmpVars.get("PathValue"+(t.getId())+","+(i));
		}
		
		ObjectiveFunction = this.scalProdSumArray(objCoeff, this.subArray(targetCoverage));
		this.updateObjective(ObjectiveFunction);
	
	}
	
	public CompactAllocation setTargetCoverage(){
		
		CompactAllocation da = new CompactAllocation();
		da.team=this.team;
		da.overflowMap = overflowMap;
		
		double coverage=0;
		
		for (int j=0;j<numPaths;j++) {
			INode t = adversaryPaths.get(j).graphtarget;
			INode g = adversaryPaths.get(j).target;
			
			coverage = this.cp.getValue(this.tmpVars.get("PathValue"+(t.getId())+","+(j)));
			da.addCoverage(g, coverage);
		}
		
		for (int i=0;i<numIntersection;i++) {
		for (int j=i;j<numIntersection;j++) {	
			if(this.cp.getValue(this.tmpVars.get("IP("+(i)+","+(j)+")")) > Configuration.EPSILON){
				da.addIntersectionCoverage(distances[i][j]);
			}
			
			for (int k=0;k<this.team.getNumResourceTypes();k++){
				double d = this.cp.getValue(this.getVar("Ip("+(i)+","+(j)+")"+(k+1)));
				if(this.cp.getValue(this.getVar("Ip("+(i)+","+(j)+")"+(k+1))) > Configuration.EPSILON){
					da.coverIntersection(distances[i][j], k);
				}
				
			}
		}
		}
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
		
		double gamevalue=0;
		double coverage=0;
		

		for(int i=0;i<this.adversaryStrategy.size();i++){
			double CoverageProb = this.adversaryStrategy.get(i);
			INode target = this.adversaryPaths.get(i).graphtarget;		
			
			coverage = this.cp.getValue(this.tmpVars.get("PathValue"+(target.getId())+","+(i)));
			gamevalue -= target.getPayoff()*(1-coverage)*CoverageProb;
		}
		
		return gamevalue;
	}
	

	/**
	 * AdversaryPath is already added from [numAdvPaths to getNumberOfAdversaryPaths) in usProblem.
	 * Note: call resetLP() if defender allocations have been deleted. Call setAdversaryStrategy() after resetLP().
	 */
	
	

	public void addRowConstraints() {
		
		

		//Constraints on number of resources for each type
		for (int k=0;k<this.team.getNumResourceTypes();k++){
			List<IloIntVar> path = new ArrayList<IloIntVar>();
			
			for (int i=0;i<numIntersection;i++) {
			for (int j=i;j<numIntersection;j++) {	
			 	path.add(this.getVar("Ip("+(i)+","+(j)+")"+(k+1)));
			}
			}
			this.addConstraint(this.sumVarArray(path), this.team.getNumDefenderResources().get(k).intValue());
			//this.getVar("Resource"+","+(k+1))
			
		}
	
		
		
		for (int i=0;i<numIntersection;i++) {
		for (int j=i;j<numIntersection;j++) {
			List<IloIntVar> overflowVars = new ArrayList<IloIntVar>();
			List<Double> probability = new ArrayList<Double>();
			
			NodePair n = distances[i][j];
			
			for (int k=0;k<this.team.getNumResourceTypes();k++){ 	
				Double coverage = Math.abs(this.team.ResourceCoverage.get(k));
				Double prob = 1-(1-Math.pow(this.team.CoverageProbability.get(k),(coverage)));
				
				List<NodePair> Overflow = new ArrayList<NodePair>();
				
				if(coverage>=0 && i!=j && Math.abs(n.distance)<=coverage){ //if it is possible to cover the intersection with resource 
					for (int i2=0;i2<numIntersection;i2++) {
					for (int j2=i2;j2<numIntersection;j2++) {
						NodePair n2 = distances[i2][j2];
						
						Integer index;
							
						
						//intersection ij can be covered by resource k
						if(i==i2&&j==j2){
							
							if(!n2.shareEdge){
								double d = n.distance-2;
								prob = 1-(1-Math.pow(this.team.CoverageProbability.get(k),(coverage-d)));
							}
							overflowVars.add(this.getVar("Ip("+(i2)+","+(j2)+")"+(k+1)));
							probability.add(prob);
						}else if(!n2.self){
							if(!n2.shareEdge){
								double d = n.distance-2;
								prob = 1-(1-Math.pow(this.team.CoverageProbability.get(k),(coverage-d)));
							}
						//intersection ij can be covered by overflow from other covered intersections	
							if(Math.abs(n2.distance)-1<=coverage){
								
								int dist = (n2.getDistance(n, coverage.intValue(), this.team.getGraph()));
//								
								//int dist = (n2.getoldDistance(n, coverage.intValue(), this.team.getGraph()));
								//int extra = 2;
								
//								if(dist==-1){
//									extra=1;}
//								
//								if(dist==0){
//									extra=0;}
								
								//int totalDist = Math.abs(n2.distance+n.distance+dist-extra);
								int totalDist = Math.abs(dist);
								if(totalDist<=coverage){
									overflowVars.add(this.getVar("Ip("+(i2)+","+(j2)+")"+(k+1)));
									probability.add(prob);
									
									Overflow.add(n2);
									List<ExtDWE> edges = n2.intersectionDistances.get(n);
									
				
								
								}
							}	
						}
					}	
					}

					if(overflowMap.containsKey(n)){
						overflowMap.get(n).put(k,Overflow);
					}else{
						HashMap<Integer,List<NodePair>> nodes = new HashMap<Integer,List<NodePair>>();
						nodes.put(k, Overflow);
						overflowMap.put(n, nodes);
					}
					//Overflow.clear();
				}else{
					overflowVars.add(this.getVar("Ip("+(i)+","+(j)+")"+(k+1)));
					probability.add(prob);
				}
			
				
			}
			
			IloNumExpr v=this.getVar("IP("+(i)+","+(j)+")");
			
			//this.addLEConstraint(this.getVar("IP("+(i)+","+(j)+")"),this.scalProdSumArray(probability, overflowVars));
			//this.addConstraint(this.getVar("IP("+(i)+","+(j)+")"),this.productArray(this.exponentArray(probability, overflowVars)));
			tmpVars.put(("IP("+(i)+","+(j)+")"),this.productArray(this.exponentArray(probability, overflowVars)));
			//overflowVars.clear();
			

		}
		}
		
		//Coverage of each attacker path
		
		
		
		for (int m=0;m<numPaths;m++) {	
			List<IloNumExpr> advPaths = new ArrayList<IloNumExpr>();
			//IloIntVar[] adv = new IloIntVar[numPaths];
			
			INode t = adversaryPaths.get(m).graphtarget;
			
			//this.getVar("PathValue"+(t.getId())+","+(m));

			
			for (int j=0;j<numIntersection;j++) {
				int i2=m;
				int j2=j;
				if(m>j){
					j2=m;
					i2=j;
				}
				
				//IloNumExpr v=this.getVar("IP("+(i2)+","+(j2)+")");
				IloNumExpr v=this.tmpVars.get("IP("+(i2)+","+(j2)+")");
				
				advPaths.add(v);
	
			}
			
			//this.addLEConstraint(this.getVar("PathValue"+(t.getId())+","+(m)),this.sumVarArray(advPaths));
			//this.addConstraint(this.getVar("PathValue"+(t.getId())+","+(m)),this.productArray(advPaths));
			tmpVars.put(("PathValue"+(t.getId())+","+(m)),this.productExprArray(advPaths));
		}
		
		if(buildTeam){
		
			IloIntVar[] resources = new IloIntVar[this.team.getNumResourceTypes()];
			double[] costs = new double[this.team.getNumResourceTypes()];
			for (int k=0;k<this.team.getNumResourceTypes();k++){ 	
				resources[k] = this.getVar("Resource"+","+(k+1));
				costs[k]= this.team.costs.get(k);
				
			}
			
			this.addConstraint(this.scalProdSumArray(costs, resources),(int)this.team.budget);
			
		
		}

		//this.setObjective();
	}
	
	

	
	

	public void addDecisionVars() {

		for (int i=0;i<numIntersection;i++) {
		for (int j=i;j<numIntersection;j++) {	
			for (int k=0;k<this.team.getNumResourceTypes();k++){ //for each resource type
				double coverage = Math.abs(this.team.ResourceCoverage.get(k));
				NodePair np = distances[i][j];
				
				int bound = 0;
				if( Math.abs(np.distance)<=coverage){
					bound= this.team.getNumDefenderResources().get(k).intValue();
				}
				
				this.addDecisionVar("Ip("+(i)+","+(j)+")"+(k+1), 0, bound);
		
			}
			
			//this.addDecisionVar("IP("+(i)+","+(j)+")", 0, 1);
		}
		}

		
//		for (int i=0;i<numPaths;i++) {	
//			INode t = adversaryPaths.get(i).graphtarget;
//			this.addDecisionVar("PathValue"+(t.getId())+","+(i), 0, 1);
//		}
		
		
		for (int k=0;k<this.team.getNumResourceTypes();k++){ //for each resource type
			if(!buildTeam){
				int numResources = this.team.getNumDefenderResources().get(k).intValue();
				this.addDecisionVar("Resource"+","+(k+1), numResources, numResources);
			}else{
				this.addDecisionVar("Resource"+","+(k+1), 0, Integer.MAX_VALUE);
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
		String tet="";
		for(int i=0;i<this.constraintsLB.size();i++){
			tet += "\n" + this.constraintsLB.get(i).toString() +"<="+this.constraintsUP.get(i).toString();;
		}
		
		System.out.println(objective+"\n"+constraints+"\n"+constraintVal  +"\n"+vars+"\n"+tet);
	}
	

	
}
