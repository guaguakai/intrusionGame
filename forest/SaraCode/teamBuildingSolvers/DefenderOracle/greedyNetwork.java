package teamBuildingSolvers.DefenderOracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import lpWrapper.AMIProblem.BOUNDS_TYPE;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;


public class greedyNetwork extends MIProblem {
	private TeamBuildingProblem usProblem;
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private int numChannels;
	double budget;
	List<List<INode>> channels;
	private Map<INode, Double> traffic;
	private Map<INode, Double> data;
	
	
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;
	

	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public greedyNetwork(TeamBuildingProblem usProblem) {
		super();
		this.usProblem = usProblem;
		this.numAdvPaths = 0;
		setProblemName("network");
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}

	@Override
	public void resetLP() {
		this.numAdvPaths = 0;
		this.varMap.clear();
		this.rowMap.clear();
		super.resetLP();
	}

	public void setAdversaryStrategy(List<Double> adversaryStrategy) {
		//coefficients for the variables in the objective
		final int numEdges = this.usProblem.getGraph().edgeSet().size();
		final int numV = this.usProblem.getGraph().vertexSet().size();
		this.adversaryStrategy = adversaryStrategy;
		
		int buffer = this.usProblem.getTotalResources();
		int rowbuffer = this.rowMap.get("SumLambda")-1;
		
		int index = 0;
		if(this.numAdvPaths>0){
			index = this.varMap.get("z1");
		}
		
		// Updating the objective
		// obj: max \sum_j (z_j a_j T_{t_j} - a_jT_{t_j}) <- second term is constant and hence is not added
		for ( int j=0; j<this.numAdvPaths; j++) {
			double prob = this.adversaryStrategy.get(j);
			double payoffOnTarget = this.usProblem.getActProfile().getPayoffOfPathOnTarget(j);
			double objCoeff = prob*payoffOnTarget;
			
			//setObjectiveCoef( (numEdges+numV)*buffer+j+1, objCoeff);	
			//int s = this.rowMap.get("setZ"+(j+1));
			setObjectiveCoef( index+j, objCoeff);
			
			
			if ( Configuration.TRUNCATELPS ) {
				if ( objCoeff <= Configuration.EPSILON ) {
					this.disableRow(1+rowbuffer + j+1);
				} else {
					this.enableRow(1+rowbuffer + j+1);
				}			
			}
		}
	}

	/**
	 * AdversaryPath is already added from [numAdvPaths to getNumberOfAdversaryPaths) in usProblem.
	 * Note: call resetLP() if defender allocations have been deleted. Call setAdversaryStrategy() after resetLP().
	 */
	public void updateAdversaryPaths() {
		final int numEdges = this.usProblem.getGraph().edgeSet().size();
		final int curAdvPaths = this.numAdvPaths;
		final int colsToAdd = (this.usProblem.getActProfile().getNumberOfAdversaryPaths() - this.numAdvPaths);
		this.numAdvPaths += colsToAdd;
		
		final int numResourceTypes = this.usProblem.getNumResourceTypes();
		
		if ( colsToAdd == 0) {
			return;
		}
		// Add cols first	
		
		for ( int j=0; j<colsToAdd; j++) {
			addAndSetColumn("z"+(j+curAdvPaths+1), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			this.varMap.put("z"+(j+curAdvPaths+1), this.numCols);
		}
		
		// Add rows next
		final int rowsToAdd = colsToAdd;
		
		for ( int i=1; i<=rowsToAdd; i++) {
			addAndSetRow("setZ"+(i+curAdvPaths),BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
			this.rowMap.put("setZ"+(i+curAdvPaths), this.numRows);
		}
		

		// Now generate data and add that
		
		for ( int j=1; j<=colsToAdd; j++) {
			List<Integer> iaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			// add z_j
			
			iaS.add(this.varMap.get("z"+(j+curAdvPaths)));
//			iaS.add(numEdges+curAdvPaths+j);
			arS.add(1.0);
			// add \sum_e (A_{je} \lambda_e) 
			// A_{je} is 1 only for elements in the path of the adversary
			
			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 					//for each resource type
				for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type	
					for ( ExtDWE edge : this.usProblem.getActProfile().getAdversaryPath(j+curAdvPaths-1).getPath() ) {
						
						int eId = edge.getId();
						
						iaS.add(this.varMap.get("l"+","+(k+1)+","+(i+1)+","+ eId));
						
						if(this.usProblem.isProbability){
							arS.add(-this.usProblem.CoverageProbability.get(k));
						}else{
							arS.add(-1.0);
						}
					}
				}
			}
			
			this.setMatRow(this.rowMap.get("setZ"+(j+curAdvPaths)), iaS, arS);
		}
		
	}

	public DefenderAllocation getDefenderAllocation() {
		DefenderAllocation da = new DefenderAllocation();
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
				for ( Iterator<ExtDWE> edgeIter = this.usProblem.getGraph().edgeSet().iterator(); edgeIter.hasNext(); ) {
					ExtDWE edge = edgeIter.next();
					int eId = edge.getId();
					if ( getColumnPrimal(this.varMap.get("l"+","+(k+1)+","+(i+1)+"," + eId)) > Configuration.EPSILON){
					if(!da.contains(edge))	{
						da.addEdgeToAllocation(edge);
					}
					da.addEdgetoResource(edge,"Resource"+(k+1)+","+(i+1));
					//da.addProbability(edge,this.usProblem.CoverageProbability.get(k));
					if(this.usProblem.isProbability){
						da.addResourcetoEdge(edge, k);
					}
					}
				}
			}
		}
		return da;
	}

	public double getDefenderPayoff() {
		// getObjective - a_jT_{t_j}
		final double curObjective = this.getLPObjective();
		double sumConstants = 0.0;
						
		for ( int j=0; j<this.numAdvPaths; j++) {
			double test = this.usProblem.getActProfile().getPayoffOfPathOnTarget(j);
			sumConstants += (adversaryStrategy.get(j)*this.usProblem.getActProfile().getPayoffOfPathOnTarget(j));
		}
		return curObjective - sumConstants;
	}

	@Override
	public void generateData() {
		/* Problem:
		 * max_{z,\lambda} -\sum_j{(1-z_j)a_jT_{t_j} = max_{z,\lambda} \sum_j (z_j a_j T_{t_j})
		 * s.t. z_j - \sum_e{A_{je} \lambda_e} <= 0

		 * 		\sum_e \lambda_e <= k
		 *		\lambda_e \in {0,1}
		 *		z_j \in [0,1]
		 */
		final int numEdges = this.usProblem.getGraph().edgeSet().size();

		List<Integer> thisRowIndices = new ArrayList<Integer>();
		List<Double> thisRowValues = new ArrayList<Double>();
		
	
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);

		
		int c=0;	
		for(List<INode> channel : channels){
			thisRowIndices.add(this.varMap.get("s"+","+ c)); 
			thisRowValues.add(1.0);
				
				
			for(INode n : channel){
				thisRowIndices.add(this.varMap.get("a"+","+ n.getId())); 
				thisRowValues.add(-1.0);
					
			}
				
			setMatRow(this.rowMap.get("Channel"+(c)), thisRowIndices, thisRowValues);
			thisRowIndices.clear();
			thisRowValues.clear();
			c++;
		}
		
		for(INode n : nodeList){
		
			thisRowIndices.add(this.varMap.get("a"+","+ n.getId())); 
			thisRowValues.add(traffic.get(n));
		}
		
		setMatRow(this.rowMap.get("Cost"), thisRowIndices, thisRowValues);
		thisRowIndices.clear();
		thisRowValues.clear();
		
	}

	@Override
	public void setColBounds() {
		/*
		 * Columns: \lambda_e, z_j
		 * Number of Columns: No. of edges + No. of Adversary Paths
		 */
		
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);

		for(INode n : nodeList){	
			addAndSetColumn("a"+","+(n.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			this.varMap.put("a"+","+(n.getId()), this.numCols);
		}
		for (int c=0;c<numChannels;c++){
			addAndSetColumn("s"+","+(c),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			this.varMap.put("s"+","+(c), this.numCols);
			
			addAndSetColumn("tau"+","+(c),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			this.varMap.put("tau"+","+(c), this.numCols);
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
		//addAndSetRow("SumLambda", BOUNDS_TYPE.FIXED, this.usProblem.getTotalCoverage(), this.usProblem.getGraph().edgeSet().size());
		//rowMap.put("SumLambda", this.numRows);
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);
		
		for (int c=0;c<numChannels;c++){
			
			addAndSetRow("Channel"+(c),BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
			this.rowMap.put("Channel"+(c), this.numRows);
			
	
		}
			addAndSetRow("Cost",BOUNDS_TYPE.UPPER, budget, budget);
			this.rowMap.put("Cost", this.numRows);
		
		
		
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}

	
}
