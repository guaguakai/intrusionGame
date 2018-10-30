package urbansecSolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.urbansecModels.AdversaryPath;
import model.urbansecModels.DefenderAllocation;
import model.urbansecModels.UrbanSecProblem;

public class BRDefender extends MIProblem {
	private UrbanSecProblem usProblem;
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private int numAdvPaths;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;

	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public BRDefender(UrbanSecProblem usProblem) {
		super();
		this.usProblem = usProblem;
		this.numAdvPaths = 0;
		setProblemName("BRDef");
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
		this.adversaryStrategy = adversaryStrategy;
		// Updating the objective
		// obj: max \sum_j (z_j a_j T_{t_j} - a_jT_{t_j}) <- second term is constant and hence is not added
		for ( int j=0; j<this.numAdvPaths; j++) {
			double prob = this.adversaryStrategy.get(j);
			double payoffOnTarget = this.usProblem.getActProfile().getPayoffOfPathOnTarget(j);
			double objCoeff = prob*payoffOnTarget;
			setObjectiveCoef( numEdges+j+1, objCoeff);	
			if ( Configuration.TRUNCATELPS ) {
				if ( objCoeff <= Configuration.EPSILON ) {
					this.disableRow(1 + j+1);
				} else {
					this.enableRow(1 + j+1);
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
			
//			if ( !this.varMap.containsKey("z"+(j+curAdvPaths)) ) {
//				System.out.println("1");
//				System.out.println(j);
//				System.out.println(curAdvPaths);
//				System.out.println(varMap);
//				System.exit(1);
//			}
			
			iaS.add(this.varMap.get("z"+(j+curAdvPaths)));
//			iaS.add(numEdges+curAdvPaths+j);
			arS.add(1.0);
			// add \sum_e (A_{je} \lambda_e) 
			// A_{je} is 1 only for elements in the path of the adversary
			for ( ExtDWE edge : this.usProblem.getActProfile().getAdversaryPath(j+curAdvPaths-1).getPath() ) {
				int eId = edge.getId();
				
//				if ( !this.varMap.containsKey("l" + eId) ) {
//					System.out.println("2");
//					System.out.println(eId);
//					System.out.println(this.usProblem.getGraph().edgeSet());
//					System.out.println(varMap);
//					System.exit(1);
//				}
				
				iaS.add(this.varMap.get("l" + eId));
				arS.add(-1.0);
			}
									
			this.setMatRow(this.rowMap.get("setZ"+(j+curAdvPaths)), iaS, arS);
//			this.setMatRow(1+curAdvPaths+j, iaS, arS);
		}
	}

	public DefenderAllocation getDefenderAllocation() {
		DefenderAllocation da = new DefenderAllocation();
		for ( Iterator<ExtDWE> edgeIter = this.usProblem.getGraph().edgeSet().iterator(); edgeIter.hasNext(); ) {
			ExtDWE edge = edgeIter.next();
			int eId = edge.getId();
			if ( getColumnPrimal(this.varMap.get("l" + eId)) > Configuration.EPSILON){
				da.addEdgeToAllocation(edge);
			}
		}
		return da;
	}

	public double getDefenderPayoff() {
		// getObjective - a_jT_{t_j}
		final double curObjective = this.getLPObjective();
		double sumConstants = 0.0;
						
		for ( int j=0; j<this.numAdvPaths; j++) {
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
		List<Integer> firstRowIndices = new ArrayList<Integer>();
		List<Double> firstRowValues = new ArrayList<Double>();

		// Add elems for \lambda <= k
		firstRowIndices.clear();
		firstRowValues.clear();
		for ( ExtDWE e : this.usProblem.getGraph().edgeSet() ) {					
			firstRowIndices.add(this.varMap.get("l" + e.getId()));
			firstRowValues.add(1.0);					
		}
		setMatRow(this.rowMap.get("SumLambda"), firstRowIndices, firstRowValues);	

		this.updateAdversaryPaths();
	}
	@Override
	public void setColBounds() {
		/*
		 * Columns: \lambda_e, z_j
		 * Number of Columns: No. of edges + No. of Adversary Paths
		 */
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		for ( ExtDWE e: edgeList) {			
			if(e.getType().equals(EDGE_TYPE.NORMAL)){
				addAndSetColumn("l"+(e.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			}else{
				addAndSetColumn("l"+(e.getId()),BOUNDS_TYPE.FIXED, 0, 0, VARIABLE_TYPE.INTEGER, 0);
			}
			this.varMap.put("l"+(e.getId()), this.numCols);
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
		addAndSetRow("SumLambda", BOUNDS_TYPE.FIXED, this.usProblem.getNumDefenderResources(), this.usProblem.getNumDefenderResources());
		rowMap.put("SumLambda", this.numRows);
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}
}
