package teamBuildingSolvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import model.graphutils.ExtDWE;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;

public class CoreLP extends MIProblem {
	/**
	 * Order of calls: loadProblem() -> solve() -> getDefenderStrategy(); getAdversaryStrategy()
	 * if deletion in defender allocations and/or adversary paths; resetLP() -> solve();
	 * addDefenderAllocation() / addAdversaryPaths() -> solve();  
	 */

	private TeamBuildingProblem usProblem;
	private int numDefAllocations, numAdvPaths;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;

	public CoreLP(TeamBuildingProblem usProblem2) {
		super();
		this.usProblem = usProblem2;
		setProblemName("CoreLP");
		this.numDefAllocations = 0; //this.usProblem.getActProfile().getNumberOfDefenderAllocations();
		this.numAdvPaths = 0; //this.usProblem.getActProfile().getNumberOfAdversaryPaths();
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
	}

	@Override
	public void resetLP() {
		this.numDefAllocations = 0;
		this.numAdvPaths = 0;
		this.varMap.clear();
		this.rowMap.clear();
		super.resetLP();		
	}

	/**
	 * usProblem should have been updated already with the additional DefenderAllocations. 
	 * This will add the defenderAllocations from indices numDefAllocations to usProblem.actProfile.setDefenderAllocations.size()-1.
	 * @param startIndex
	 */
	public void updateDefenderAllocations() {
		final int curDefAlloc = this.numDefAllocations;
		final int colsToAdd = (this.usProblem.getActProfile().getNumberOfDefenderAllocations() - this.numDefAllocations);

		this.numDefAllocations += colsToAdd;
		// Add columns in the LP first
		if ( colsToAdd == 0 ) {
			return;
		}
		// Add elems in the LP now
		for ( int j=1; j<=colsToAdd; j++ ) {
			List<Integer> iaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			// Add Elems for SumX = 1. Row=1, Columns=1+curDefAlloc+j, Elem=1.0
			iaS.add(1);
			arS.add(1.0);
			// Add Elems for |A| rows
			for ( int i=1; i<=this.numAdvPaths; i++){
				// (1-z_{ij})T_{t_j}
				Double Ttj = this.usProblem.getActProfile().getPayoffOfPathOnTarget(i-1);
				Boolean bzij = this.usProblem.getActProfile().getIntersect(j+curDefAlloc-1, i-1);
				Double zij = (double) ((bzij.booleanValue() == true)?1:0);
				if(this.usProblem.isProbability){
					zij = this.usProblem.getActProfile().getProbability(j+curDefAlloc-1, i-1);

				}
				// adding x_i
				iaS.add(i+1);
				arS.add((1-zij)*Ttj.doubleValue());
			}
			
			this.setMatCol("x"+(curDefAlloc+j), VARIABLE_TYPE.CONTINUOUS, 0, 0, 1, iaS, arS);
			this.varMap.put("x"+(curDefAlloc+j), this.numCols);
			
			//this.setMatCol(j+curDefAlloc+1, iaS, arS);
		}
	}

	/**
	 * usProblem should have been updated already with the additional AdversaryPaths.
	 * This will add the defenderAllocations from [numAdvPaths, usProblem.actProfile.setAdversaryPaths.size()).
	 * Call resetLP() if defender allocations / adversary paths have been deleted. 
	 * @param startIndex
	 */
	public void updateAdversaryPaths() {
		final int curAdvPaths = this.numAdvPaths;
		final int rowsToAdd = (this.usProblem.getActProfile().getNumberOfAdversaryPaths() - this.numAdvPaths);
		this.numAdvPaths += rowsToAdd;
		if ( rowsToAdd == 0 ) {
			return;
		}
		// Add rows to LP first
		for ( int i=1; i<=rowsToAdd; i++) {
			addAndSetRow("AP"+(i+curAdvPaths), BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
			this.rowMap.put("AP"+(i+curAdvPaths), this.numRows);			
		}
		// Add elems in the LP now
		// Add Elems for |A| rows
		for ( int i=1; i<=rowsToAdd; i++){
			List<Integer> jaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			// d
			jaS.add(this.varMap.get("d"));
			arS.add(1.0);
			// (1-z_{ij})T_{t_j}
			Double Ttj = this.usProblem.getActProfile().getPayoffOfPathOnTarget(i+curAdvPaths-1);
			for ( int j=1; j<=this.numDefAllocations; j++ ) {
				Boolean bzij = this.usProblem.getActProfile().getIntersect(j-1, i+curAdvPaths-1);
				double zij = (bzij.booleanValue() == true)?1:0;
				if(this.usProblem.isProbability){
					zij = this.usProblem.getActProfile().getProbability(j-1, i+curAdvPaths-1);
				}

				//int zij = (bzij.booleanValue() == true)?1:0;
												
				jaS.add(this.varMap.get("x" + j));
				arS.add((1-zij)*Ttj.doubleValue());
			}
			this.setMatRow(this.rowMap.get("AP" + (i+curAdvPaths)), jaS, arS);
			// this.setMatRow(i+curAdvPaths+1, jaS, arS);
		}
	}
	
	public Map<ExtDWE, Double> getDefenderCoverage() {
		Map<ExtDWE, Double> defCoverage = new HashMap<ExtDWE, Double>();
		List<Double> defStrat = this.getDefenderStrategy();
		for ( int daIndex=0; daIndex<this.numDefAllocations; daIndex++ ) {
			DefenderAllocation da = this.usProblem.getActProfile().getDefenderAllocation(daIndex);
			for ( ExtDWE e : da.getAllocation() ) {
				double newCov = defStrat.get(daIndex);
				if ( defCoverage.containsKey(e) ) {
					 newCov += defCoverage.get(e); 
				}				
				defCoverage.put(e, newCov);
			}		
		}		
		return defCoverage;		
	}
	
	public Map<ExtDWE, Double> getAttackersEdgeUsage() {
		Map<ExtDWE, Double> attCoverage = new HashMap<ExtDWE, Double>();
		List<Double> attStrat = this.getAdversaryStrategy();
		for ( int apIndex=0; apIndex<this.numAdvPaths; apIndex++ ) {
			AdversaryPath ap = this.usProblem.getActProfile().getAdversaryPath(apIndex);
			for ( ExtDWE e : ap.getPath() ) {
				double newCov = attStrat.get(apIndex);
				if ( attCoverage.containsKey(e) ) {
					 newCov += attCoverage.get(e); 
				}				
				attCoverage.put(e, newCov);
			}		
		}
		return attCoverage;
	}
	
	public Map<DefenderAllocation, Double> getDefenderStrategyAsMap() {
		Map<DefenderAllocation, Double> defStrat = new HashMap<DefenderAllocation, Double>();
		for ( int da=1; da<=this.numDefAllocations; da++) {
			defStrat.put(this.usProblem.getActProfile().getDefenderAllocation(da-1), getColumnPrimal(this.varMap.get("x" + da)));
		}
		return defStrat;
	}
	
	public List<Double> getDefenderStrategy() {
		List<Double> ds = new ArrayList<Double>();
		for ( int da=1; da<=this.numDefAllocations; da++) {
			ds.add(getColumnPrimal(this.varMap.get("x" + da)));
		}
		return ds;
	}

	public Map<AdversaryPath, Double> getAttackerStrategyAsMap() {
		Map<AdversaryPath, Double> advStrat = new HashMap<AdversaryPath, Double>();
		for ( int ap=1; ap<=this.numAdvPaths; ap++) {
			advStrat.put(this.usProblem.getActProfile().getAdversaryPath(ap-1), getRowDual(this.rowMap.get("AP" + ap)));
		}
		return advStrat;
	}
	
	public List<Double> getAdversaryStrategy() {
		List<Double> as = new ArrayList<Double>();
		for ( int ap=1; ap<=this.numAdvPaths; ap++) {
			as.add(getRowDual(this.rowMap.get("AP" + ap)));
		}
		return as;
	}

	@Override
	public void generateData() {
		/*
		 *  max d
		 *  s.t.
		 *  Row 1: \sum_i x_i = 1 
		 *  Row 2..Row 2+|A|-1: d+\sum_i (1-z_{ij})T_{t_j} x_i) <= 0 \forall j
		 */
		this.updateAdversaryPaths();
		this.updateDefenderAllocations();
	}

	@Override
	public void setColBounds() {
		// Columns: d, x
		// Number of columns: 1 + no. of defender allocations
		addAndSetColumn("d", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("d", this.numCols);
	}

	@Override
	public void setRowBounds() {
		// Rows: Row1: 1^T.x = 1
		// Rows: for all paths: d + \sum_i(1-z_{ij})
		addAndSetRow("SumX", BOUNDS_TYPE.FIXED, 1.0, 1.0);
		rowMap.put("SumX", this.numRows);
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.LP, OBJECTIVE_TYPE.MAX);
	}

}
