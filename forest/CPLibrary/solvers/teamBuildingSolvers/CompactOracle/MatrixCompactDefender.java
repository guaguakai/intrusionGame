package teamBuildingSolvers.CompactOracle;

import ilog.concert.IloException;

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
import model.teamBuildingModels.NodePair;
import model.teamBuildingModels.PathConstraint;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;


public class MatrixCompactDefender extends MIProblem {
	
	private TeamBuildingProblem usProblem;

	public NodePair[][] distances;
	public List<NodePair> intersections;
	
	public int numIntersection;
	public int numPaths;
	
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private List<CompactPath> adversaryPaths;
	
	//public List<ArrayList<NodePair>> resourceAllocation = new ArrayList<ArrayList<NodePair>>();
	public static PathConstraint constraints = new PathConstraint();
	
	public boolean buildTeam = false;
	
	private int numAdvPaths;
	
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;	
	private Map<NodePair,Map<Integer,List<NodePair>>> overflowMap;
	public Map<String, INode> resourceAllocation = new HashMap<String, INode>();
	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public MatrixCompactDefender(TeamBuildingProblem usProblem) {
		super();
		this.usProblem = usProblem;
		this.numAdvPaths = 0;
		setProblemName("CompactDef");
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		this.overflowMap = new HashMap<NodePair,Map<Integer,List<NodePair>>>();
		this.intersections = new ArrayList<NodePair>();
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
		
		int index = this.varMap.get("Index");//(this.usProblem.getNumResourceTypes()+1)*((numIntersection)*(numIntersection+1))/2	;
		
		// Updating the objective
		for(int i=0;i<this.adversaryStrategy.size();i++){
			
			double CoverageProb = this.adversaryStrategy.get(i);
			INode target = this.adversaryPaths.get(i).target;		
			double objCoeff = target.getPayoff()*CoverageProb;
			
			setObjectiveCoef( index+i+1, objCoeff);
			
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
		da.team=this.usProblem;
		da.overflowMap = overflowMap;
		
		double coverage=0;
		
		for (int j=0;j<numPaths;j++) {
			INode t = adversaryPaths.get(j).graphtarget;
			INode g = adversaryPaths.get(j).target;
			
			coverage = getColumnPrimal(this.varMap.get("PathValue"+(t.getId())+","+(j)));
			da.addCoverage(g, coverage);
		}
		
		for (int i=0;i<numIntersection;i++) {
		for (int j=i;j<numIntersection;j++) {	
			if(getColumnPrimal(this.varMap.get("IP("+(i)+","+(j)+")")) > Configuration.EPSILON){
				da.addIntersectionCoverage(distances[i][j]);
			}
			
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){
			for (int m=0;m<this.usProblem.getNumDefenderResources().get(k);m++){		
				double d = getColumnPrimal(this.varMap.get("Ip("+(i)+","+(j)+")"+(k+1)+","+(m+1)));
				if(getColumnPrimal(this.varMap.get("IP("+(i)+","+(j)+")"+(k+1)+","+(m+1))) > Configuration.EPSILON){
					da.coverIntersection(distances[i][j], k);
					//da.addResource("k("+(k+1)+"),("+(m+1)+")", distances[i][j].nodeset[0]);
					//da.addResource("k("+(k+1)+"),("+(m+1)+")", distances[i][j].nodeset[1]);
					da.addResource((k+1)+","+(m+1), distances[i][j]);
				}
				
			}
			}
		}
		}
		return da;
		
	}

	
	public double getValue(){
		
		double gamevalue=0;
		double coverage=0;
		

		for(int i=0;i<this.adversaryStrategy.size();i++){
			double CoverageProb = this.adversaryStrategy.get(i);
			INode target = this.adversaryPaths.get(i).graphtarget;		
			
			coverage = getColumnPrimal(this.varMap.get("PathValue"+(target.getId())+","+(i)));
			gamevalue -= target.getPayoff()*(1-coverage)*CoverageProb;
		}
		
		return gamevalue;
	}
	

	/**
	 * AdversaryPath is already added from [numAdvPaths to getNumberOfAdversaryPaths) in usProblem.
	 * Note: call resetLP() if defender allocations have been deleted. Call setAdversaryStrategy() after resetLP().
	 */
	
	public void addConstraintMap(){
		for(String s : constraints.getKeys()){
			if(!this.rowMap.containsKey("Constraint("+s+constraints.get(s)+")")){
				HashMap<List<NodePair>,Double> m = constraints.get(s);
				for(List<NodePair> np : m.keySet()){
					addConstraints(s, np, np.size()-1);
				}
			}
		}
	}
	public void addConstraints(String s, List<NodePair> lstnp, double coverage){	

		List<Integer> RowIndices = new ArrayList<Integer>();
		List<Double> RowValues = new ArrayList<Double>();
	
		for(NodePair np : lstnp){
		
			for (int i=0;i<numIntersection;i++) {
			for (int j=i;j<numIntersection;j++) {	
				
				if(np.equals(distances[i][j])){
					
					RowIndices.add(this.varMap.get("IP("+(i)+","+(j)+")"+s));
					RowValues.add(1.0);
					
				}
			}
			}
			
		}
		
		addAndSetRow("Constrain("+s+lstnp+")", BOUNDS_TYPE.UPPER, coverage, coverage); //or fixed
		rowMap.put("Constrain("+s+lstnp+")", this.numRows);		
		
		setMatRow(this.rowMap.get("Constrain("+s+lstnp+")"), RowIndices, RowValues);
		RowIndices.clear();
		RowValues.clear();
	}
	@Override
	public void generateData() {
		
		List<Integer> RowIndices = new ArrayList<Integer>();
		List<Double> RowValues = new ArrayList<Double>();
		
		//Constraints on number of resources for each type
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){
		for (int m=0;m<this.usProblem.getNumDefenderResources().get(k);m++){	
			for (int i=0;i<numIntersection;i++) {
			for (int j=i;j<numIntersection;j++) {	
			 	
				int index = this.varMap.get("Ip("+(i)+","+(j)+")"+(k+1)+","+(m+1));
				RowIndices.add(index);
				RowValues.add(1.0);
			}
			}
		
			
			setMatRow(this.rowMap.get("SumResources"+(k+1)+","+(m+1)), RowIndices, RowValues);
			RowIndices.clear();
			RowValues.clear();
			
		}
		}
		RowIndices.clear();
		RowValues.clear();
		
		List<Integer> IIndices = new ArrayList<Integer>();
		List<Double> IValues = new ArrayList<Double>();
		
		for (int i=0;i<numIntersection;i++) {
		for (int j=i;j<numIntersection;j++) {
			NodePair n = distances[i][j];
			
			IIndices.add(this.varMap.get("IP("+(i)+","+(j)+")"));
			IValues.add(1.0);
			
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 
			for (int m=0;m<this.usProblem.getNumDefenderResources().get(k);m++){		
				
				RowIndices.add(this.varMap.get("IP("+(i)+","+(j)+")"+(k+1)+","+(m+1)));
				RowValues.add(1.0);
				
				IIndices.add(this.varMap.get("IP("+(i)+","+(j)+")"+(k+1)+","+(m+1)));
				IValues.add(-1.0);
				
				Double coverage = this.usProblem.ResourceCoverage.get(k);
				Double prob = this.usProblem.CoverageProbability.get(k);
				
				List<NodePair> Overflow = new ArrayList<NodePair>();
				
				if(coverage>=0 && i!=j && Math.abs(n.distance)<=coverage){ //if it is possible to cover the intersection with resource k
					for (int i2=0;i2<numIntersection;i2++) {
					for (int j2=i2;j2<numIntersection;j2++) {
						NodePair n2 = distances[i2][j2];
						
						Integer index;
							
	
						//intersection ij can be covered by resource k
						if(i==i2&&j==j2){
							index = this.varMap.get("Ip("+(i2)+","+(j2)+")"+(k+1)+","+(m+1));
			
							RowIndices.add(index);
							
							if(this.usProblem.isProbability){
								double distance = n.distance-2;
								if(n.shareEdge || n.distance == 2){
									distance = coverage;
								}
								
								RowValues.add(-(1-Math.pow(1-this.usProblem.CoverageProbability.get(k),coverage)));
								//RowValues.add(-this.usProblem.CoverageProbability.get(k));
							}else{
								RowValues.add(-1.0);
							}
							
						}else if(!n2.self){
							
						//intersection ij can be covered by overflow from other adjacent covered intersections	
							//INode shared = n2.shareVertex(n);
							INode shared = n2.shareTarget(n);
							if(shared!=null && (n2.distance + n.distance-1)<=coverage){
								INode other1 = n.otherTarget(shared);
								INode other2 = n2.otherTarget(shared);
								
								NodePair n3 = null; 
								
								outerloop:
								for (int i3=0;i3<numIntersection;i3++) {
								for (int j3=i3;j3<numIntersection;j3++) {
										n3 = distances[i3][j3];
										if(!n3.equals(n) && n3.equals(n2) && n3.hasTargets(other1, other2)  ){
											break outerloop;
										}
								}}
								
								if(Math.abs(n3.distance)<=coverage){
									index = this.varMap.get("Ip("+(i2)+","+(j2)+")"+(k+1)+","+(m+1));
									
									Overflow.add(n2);
									List<ExtDWE> edges = n2.intersectionDistances.get(n);
									RowIndices.add(index);
									
									if(this.usProblem.isProbability){
										double distance = n.distance-2+n2.distance-2;
										if(n.shareEdge || n.distance == 2){
											distance = distance+2;
										}
										if(n2.shareEdge || n2.distance == 2){
											distance = distance+2;
										}
										
										RowValues.add(-(1-Math.pow(1-this.usProblem.CoverageProbability.get(k),(coverage))));
										//RowValues.add(-this.usProblem.CoverageProbability.get(k));
									}else{
										RowValues.add(-1.0);
									}
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
					RowIndices.add(this.varMap.get("Ip("+(i)+","+(j)+")"+(k+1)+","+(m+1)));
					if(this.usProblem.isProbability){
						RowValues.add(-(1-Math.pow(1-this.usProblem.CoverageProbability.get(k),(coverage))));
						//RowValues.add(-this.usProblem.CoverageProbability.get(k));
					}else{
						RowValues.add(-1.0);
					}
				}
			
				
				//end of single resource of type k
				//resourceAllocation.
				
				setMatRow(this.rowMap.get("KIntersectionValue("+(i)+","+(j)+")"+(k+1)+","+(m+1)), RowIndices, RowValues);

				RowIndices.clear();
				RowValues.clear();
				
			}
			}
			
			
			
			setMatRow(this.rowMap.get("IntersectionValue("+(i)+","+(j)+")"), IIndices, IValues);

			IIndices.clear();
			IValues.clear();
			

		}
		}
		
		//Coverage of each attacker path
		
		for (int m=0;m<numPaths;m++) {	
			INode t = adversaryPaths.get(m).graphtarget;
			RowIndices.add(this.varMap.get("PathValue"+(t.getId())+","+(m)));
			RowValues.add(1.0);
			
			for (int j=0;j<numIntersection;j++) {
				int i2=m;
				int j2=j;
				if(m>j){
					j2=m;
					i2=j;
				}
				RowIndices.add(this.varMap.get("IP("+(i2)+","+(j2)+")"));
				RowValues.add(-1.0);
			}
			
			setMatRow(this.rowMap.get("TotalValue"+t.getId()+","+(m)), RowIndices, RowValues);
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
	
	

	@Override
	public void setColBounds() {

		
		
		
		for (int i=0;i<numIntersection;i++) {
		for (int j=i;j<numIntersection;j++) {	
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type
			for (int m=0;m<this.usProblem.getNumDefenderResources().get(k);m++){	
				
				double coverage = Math.abs(this.usProblem.ResourceCoverage.get(k));
				NodePair np = distances[i][j];
				
				double bound = 0;
				if( Math.abs(np.distance)<=coverage){
					//bound= Double.POSITIVE_INFINITY;
					bound = 1;
				}
				
				addAndSetColumn("Ip("+(i)+","+(j)+")"+(k+1)+","+(m+1),BOUNDS_TYPE.DOUBLE, 0, bound, VARIABLE_TYPE.INTEGER, 0);
				this.varMap.put("Ip("+(i)+","+(j)+")"+(k+1)+","+(m+1), this.numCols);
				
				addAndSetColumn("IP("+(i)+","+(j)+")"+(k+1)+","+(m+1),BOUNDS_TYPE.DOUBLE, 0, bound, VARIABLE_TYPE.INTEGER, 0);
				this.varMap.put("IP("+(i)+","+(j)+")"+(k+1)+","+(m+1), this.numCols);
			}
			}
			addAndSetColumn("IP("+(i)+","+(j)+")",BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			this.varMap.put("IP("+(i)+","+(j)+")", this.numCols);
		}
		}
		addAndSetColumn("Index",BOUNDS_TYPE.DOUBLE, 0, 0, VARIABLE_TYPE.CONTINUOUS, 0);
		this.varMap.put("Index", this.numCols);
		
		for (int i=0;i<numPaths;i++) {	
			INode t = adversaryPaths.get(i).graphtarget;
			addAndSetColumn("PathValue"+(t.getId())+","+(i),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			this.varMap.put("PathValue"+(t.getId())+","+(i), this.numCols);
		}
		
		
	}
	@Override
	public void setRowBounds() {
		
		
		for (int i=0;i<numPaths;i++) {	
			INode t = adversaryPaths.get(i).graphtarget;
			addAndSetRow("TotalValue"+t.getId()+","+(i), BOUNDS_TYPE.UPPER, 0.0, 0.0); //or fixed
			rowMap.put("TotalValue"+t.getId()+","+(i), this.numRows);			
			
		}
		
		for (int i=0;i<numIntersection;i++) {
		for (int j=i;j<numIntersection;j++) {		
			
			INode t = adversaryPaths.get(i).graphtarget;
			
			addAndSetRow("IntersectionValue("+(i)+","+(j)+")", BOUNDS_TYPE.UPPER, 0.0, 0.0); //or fixed
			rowMap.put("IntersectionValue("+(i)+","+(j)+")", this.numRows);	
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type	
				for (int m=0;m<this.usProblem.getNumDefenderResources().get(k);m++){	
					
					addAndSetRow("KIntersectionValue("+(i)+","+(j)+")"+(k+1)+","+(m+1), BOUNDS_TYPE.UPPER, 0.0, 0.0); //or fixed
					rowMap.put("KIntersectionValue("+(i)+","+(j)+")"+(k+1)+","+(m+1), this.numRows);	
				}
			}
			
		}}
		
		//restrictions on the number of resources of each type
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type	
		for (int m=0;m<this.usProblem.getNumDefenderResources().get(k);m++){	
			double numResources = 1;
			
			addAndSetRow("SumResources"+(k+1)+","+(m+1), BOUNDS_TYPE.FIXED, numResources, numResources);
			rowMap.put("SumResources"+(k+1)+","+(m+1), this.numRows);			
		}
		}

		
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}

	
}
