package teamBuildingSolvers.CompactOracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.teamBuildingModels.CompactStrategy;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;

public class CompactLP extends MIProblem {
	/**
	 * Order of calls: loadProblem() -> solve() -> getDefenderStrategy(); getAdversaryStrategy()
	 * if deletion in defender allocations and/or adversary paths; resetLP() -> solve();
	 * addDefenderAllocation() / addAdversaryPaths() -> solve();  
	 */
	private TeamBuildingProblem teamProb;
	private CompactStrategy strategy;
	private int numDefAllocations, numAdvPaths;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;
	
	public boolean compact = false;

	public CompactLP(CompactStrategy strategy, TeamBuildingProblem team) {
		super();
		this.strategy = strategy;
		setProblemName("CompactLP");
		this.numDefAllocations = 0; 
		this.numAdvPaths = 0; 
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		this.teamProb=team;
		
	}

	public CompactLP(CompactStrategy strategy) {
		super();
		this.strategy = strategy;
		setProblemName("CompactLP");
		this.numDefAllocations = 0; 
		this.numAdvPaths = 0; 
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		compact = true;
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
		
		final int curDefAlloc = this.numDefAllocations; //current number of columns
		int currAllocation =0;
		if(compact){
			currAllocation = this.strategy.getNumDefenderAllocations();
		}else{
			currAllocation = this.teamProb.getActProfile().getNumberOfDefenderAllocations();
		}
		final int colsToAdd = (currAllocation - this.numDefAllocations); //new allocations added
		this.numDefAllocations += colsToAdd;
		
		
		// Add columns in the LP first
		if ( colsToAdd == 0 ) {
			return;
		}
		for ( int j=1; j<=colsToAdd; j++ ) {
			List<Integer> iaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			
			//SumX = 1. Add so sum of mixed strategy = 1
			iaS.add(1);
			arS.add(1.0);
			
			// Add Elems for |A| rows
			for ( int i=1; i<=this.numAdvPaths; i++){
				
				// (1-z_{ij})T_{t_j}
				Double Ttj = this.strategy.PathPayoff(i-1);
				//Double zij = this.strategy.ProbabilityIntersect(j+curDefAlloc-1, i-1);
				Double zij = 0.0;
				if(compact){
					zij = this.strategy.ProbabilityIntersect(j+curDefAlloc-1, i-1);
				}else{
					zij = this.teamProb.getActProfile().getProbability(j+curDefAlloc-1, i-1);
				}
				// adding x_i
				iaS.add(i+1);
				arS.add((1-zij)*Ttj.doubleValue());
			}
			
			this.setMatCol("x"+(curDefAlloc+j), VARIABLE_TYPE.CONTINUOUS, 0, 0, 1, iaS, arS);
			this.varMap.put("x"+(curDefAlloc+j), this.numCols);

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
		
		List<INode> targetList = new ArrayList<INode>(this.strategy.getTargetSet());
		Collections.sort(targetList);
		
		final int rowsToAdd = targetList.size() - curAdvPaths;
		this.numAdvPaths += rowsToAdd;
		
		// Add rows to LP first
		for ( int i=1; i<=rowsToAdd; i++) {
			addAndSetRow("AP"+(i), BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
			this.rowMap.put("AP"+(i), this.numRows);			
		}
		
		// Add elems in the LP now
		for ( int i=1; i<=rowsToAdd; i++){
			List<Integer> jaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			
			// d
			jaS.add(this.varMap.get("d"));
			arS.add(1.0);
			
			// (1-z_{ij})T_{t_j}
			//get target value
			double Ttj = strategy.PathPayoff(i+curAdvPaths-1);
			
			//get defender coverage of target
			for ( int j=1; j<=this.numDefAllocations; j++ ) {
				Double zij =0.0;
				if(compact){
					zij = this.strategy.ProbabilityIntersect(j-1, i+curAdvPaths-1);	
				}else{
					zij  = this.teamProb.getActProfile().getProbability(j-1, i+curAdvPaths-1);
				}
				jaS.add(this.varMap.get("x" + j));
				arS.add((1-zij)*Ttj);
			}
			
			this.setMatRow(this.rowMap.get("AP" + (i)), jaS, arS);
		}
	}
	
	public List<Double> getDefenderStrategy() {
		List<Double> ds = new ArrayList<Double>();
		for ( int da=1; da<=this.numDefAllocations; da++) {
			ds.add(getColumnPrimal(this.varMap.get("x" + da)));
		}
		return ds;
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
