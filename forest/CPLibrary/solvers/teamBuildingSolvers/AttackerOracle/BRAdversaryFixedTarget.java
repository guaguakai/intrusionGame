package teamBuildingSolvers.AttackerOracle;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;


public class BRAdversaryFixedTarget extends MIProblem {
	/**
	 * loadProblem() -> setDefenderStrategy(); -> solve(); -> getAdversaryPath(); getIntersect(); getAdversaryPayoff();
	 * if setSource / setTarget is called after loading problem, then call resetLP(); -> setDefenderStrategy(); -> solve()
	 * addDefenderAllocations(); -> setDefenderStrategy(); -> solve();
	 */
	private INode source;
	private INode target;
	private List<Double> defenderStrategy;	
	private TeamBuildingProblem usProblem;
	private int numDefAllocations;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;

	//	public BRAdversaryFixedTarget(UrbanSecProblem usProblem) {
	//		super();
	//		this.usProblem = usProblem;
	//		this.numDefAllocations = this.usProblem.getActProfile().getNumberOfDefenderAllocations();
	//	}

	public BRAdversaryFixedTarget(TeamBuildingProblem usProblem2, INode source, INode target) {
		super();
		this.usProblem = usProblem2;
		this.source = source;
		this.target = target;
		this.numDefAllocations = 0;
		setProblemName("BRAdv"+target.getId());
		this.defenderStrategy = null;
		this.redirectOutput(null);
		this.varMap = new HashMap<String, Integer>();
		this.rowMap = new HashMap<String, Integer>();
		
//		try {
//			this.cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Network);
//		} catch (IloException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Override
	public void resetLP() {
		this.numDefAllocations = 0;
		this.varMap.clear();
		this.rowMap.clear();
		super.resetLP();
	}

	public void setDefenderStrategy(List<Double> defenderStrategy) {
		this.defenderStrategy = defenderStrategy;
		final int numEdges = this.usProblem.getGraph().edgeSet().size();
		final int numNodes = this.usProblem.getGraph().vertexSet().size();
		// set coefficients in the objective 
		// obj: max \sum_i (x_i - x_iz_i) -> max \sum_i (-x_i z_i). The other term is a constant.
		int rowOffset = 0;
		for ( int da=1; da<=this.numDefAllocations; da++){
			double objCoeff = -1*this.defenderStrategy.get(da-1);
			setObjectiveCoef( numEdges+da, objCoeff);
			if ( Configuration.TRUNCATELPS ) {
				if ( Math.abs(objCoeff) <= Configuration.EPSILON ) {
					// for all edges corresponding to this z_i 
					// remove row
					for ( int tempIndex = 0; tempIndex < this.usProblem.getActProfile().getDefenderAllocation(da-1).getAllocation().size(); tempIndex ++) {
						this.disableRow(numNodes + rowOffset+1);
						rowOffset ++;
					}

				} else {
					// for all edges corresponding to this z_i
					// add row
					for ( int tempIndex = 0; tempIndex < this.usProblem.getActProfile().getDefenderAllocation(da-1).getAllocation().size(); tempIndex ++) {
						this.enableRow(numNodes + rowOffset+1);
						rowOffset ++;
					}
				}
			}
		}
	}

	public INode getSource() {
		return source;
	}

	public void setSource(INode source) {
		this.source = source;
	}

	public INode getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		this.target = target;
		setProblemName("BRAdv"+target.getId());
	}

	public AdversaryPath getAdversaryPath() {
		AdversaryPath ap = new AdversaryPath();
		boolean notExistsCycle = true;
		for ( ExtDWE curEdge : this.usProblem.getGraph().edgeSet()){
			int eId = curEdge.getId();			
			
			if(getColumnPrimal(this.varMap.get("g"+eId))>Configuration.EPSILON){
				notExistsCycle = ap.addEdgeToPath(curEdge);
				if ( notExistsCycle == false ) {
					System.err.println("Cycle exists in path.");
					System.err.flush();
				}
			}
		}
		
		if ( notExistsCycle == false ) {
			ap.removeCycleFromPath(this.usProblem.getSource());
		}
		
		ap.setTarget(this.target);
		return ap;
	}	
	
	/**
	 * This function returns the true adversary payoff when attacking the set target.
	 * The objective is NOT the true payoff (look at the LP and the objective for greater understanding).
	 * @return
	 */
	public double getAdversaryPayoff() {		
		double advPayoff = this.getLPObjective();
		// obj is just \sum_i (-x_i z_i)
		// now add \sum_i x_i
		// and multiply with T_{t_m}
		if ( !(defenderStrategy == null || defenderStrategy.size() == 0 )) {
			for (Double xi : defenderStrategy){
				advPayoff += xi.doubleValue();
			}
			// even without any additions, sum of defender strategy should be 1.0
		}
		advPayoff = advPayoff * target.getPayoff();
		return advPayoff;
	}
	
	public double getrealAdversaryPayoff() {		
		double advPayoff = 0.0;
		// obj is just \sum_i (-x_i z_i)
		// now add \sum_i x_i
		// and multiply with T_{t_m}
		
		AdversaryPath ap = getAdversaryPath();
		boolean notcontainsAp = this.usProblem.getActProfile().addAdversaryPath(ap);

		
		int curAP = this.usProblem.getActProfile().getAdversaryPaths().indexOf(ap);
		
		if ( !(defenderStrategy == null || defenderStrategy.size() == 0 )) {
		for ( int j=0; j<this.defenderStrategy.size(); j++) {
			double probability = this.usProblem.getActProfile().getProbability(j, curAP);
			double Tj = target.getPayoff();
				
			advPayoff += (1-probability)*(this.defenderStrategy.get(j).doubleValue()*Tj);
		}}
		if(notcontainsAp){
			this.usProblem.getActProfile().deleteAdversaryPath(ap);
		}
		return advPayoff;
	}

	/**
	 * usProblem should already have been updated with the defenderAllocation.
	 * It will add the defender allocations in the usProblem from indices [numDefAllocations, usProblem.getActProfile().getDefenderAllocations.size() )
	 * Note: call resetLP() if defender allocations have been deleted. Call setDefenderStrategy() after resetLP(). 
	 * @param startIndex
	 */
	public void updateDefenderAllocations() {
		final int numEdges = this.usProblem.getGraph().edgeSet().size();
		final int colsToAdd = (this.usProblem.getActProfile().getNumberOfDefenderAllocations() - this.numDefAllocations);
		final int curDefAllocs = this.numDefAllocations;
		// Add cols first		
		this.numDefAllocations += colsToAdd;
		if ( colsToAdd == 0 ) {
			return;
		}

		//z_i
		for ( int da=1; da<=colsToAdd; da++) {
			addAndSetColumn("z"+(da+curDefAllocs), BOUNDS_TYPE.DOUBLE, 0, 1,VARIABLE_TYPE.CONTINUOUS,0);
			this.varMap.put("z"+(da+curDefAllocs), this.numCols);
		}
		// Add rows next, and generate associated data
		// Rows: zi constraints per defender allocation
		for (int da=1; da<=colsToAdd; da++) {
			/*
			for ( int eId=1; eId<=numEdges; eId++) {
				addAndSetRow(numNodes+((da+curDefAllocs-1)*numEdges+eId),"Dz"+(da+curDefAllocs)+"g"+eId,BOUNDS_TYPE.LOWER, -1, Configuration.MM);
			}
			 */
			for ( ExtDWE curEdge : this.usProblem.getActProfile().getDefenderAllocation(da+curDefAllocs-1).getAllocation()) {
				
				
				addAndSetRow("z"+(da+curDefAllocs)+"g"+curEdge.getId(),BOUNDS_TYPE.LOWER, 0, Configuration.MM);
				this.rowMap.put("z"+(da+curDefAllocs)+"g"+curEdge.getId(), this.numRows);
				
				
				// then add elements
				List<Integer> jaS = new ArrayList<Integer>(2);
				List<Double> arS = new ArrayList<Double>(2);
				// add zi 		
				
//				if ( !this.varMap.containsKey("z"+(da+curDefAllocs)) ) {
//					System.out.println("1");
//					System.out.println(curEdge.getId());
//					System.out.println(da);
//					System.out.println(curDefAllocs);
//					System.exit(1);
//				}
				
				jaS.add(this.varMap.get("z"+(da+curDefAllocs)));
				arS.add(+1.0);
				// add -\gamma_e
				
//				if ( !this.varMap.containsKey("g" + curEdge.getId()) ) {
//					System.out.println("2");
//					System.out.println(curEdge.getId());
//					System.out.println(this.varMap);
//					System.exit(1);
//				}
				
				jaS.add(this.varMap.get("g" + curEdge.getId()));
				//arS.add(-1.0);
				if(this.usProblem.isProbability){
					arS.add(-this.usProblem.getActProfile().getDefenderAllocation(da+curDefAllocs-1).getProbability(curEdge));
				}else{
					arS.add(-1.0);
				}
					
					// add Row data
				this.setMatRow(this.numRows, jaS, arS);
			}
		}		
	}

	@Override
	protected void setRowBounds() {
		/*
		 * Rows: 
		 * Rows N-2: inflow = outflow per node, = 1 for source and sink
		 * Rows No. of edges X No. of defender allocations: zi >= gamma_e + X_{ie} - 1
		 * Node Ids in Graph should be from 1 to numNodes (or graph.vertexSet().size())
		 * Edge Ids in Graph should be from 1 to numEdges (or graph.edgeSet().size())
		 */
		// Rows: Flow constraints for all nodes
		List<INode> nodesList = new ArrayList<INode>(usProblem.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( INode curNode : nodesList) {
			int curNodeId = curNode.getId(); 
			// there should be just one source
			double lbound =0;
			double ubound = 0;
			if ( curNode.equals(source) ) {
				// First Row: exit out of source = 1
				lbound = 1.0;	ubound  = 1.0;		
			}
			else if ( curNode.equals(target) ) {
				// Second Row: inflow into sink = 1				
				lbound = -1.0;	ubound  = -1.0;					
			}
			else {
				lbound = 0.0;	ubound  = 0.0;	
			}
			addAndSetRow("SumSo"+curNodeId, BOUNDS_TYPE.FIXED,lbound,ubound);
			this.rowMap.put("SumSo"+curNodeId, this.numRows);
		}
	}

	@Override
	protected void setColBounds() {
		/*
		 * Columns: \gamma_e - one per edge; z_i <- one per defender allocation
		 */
		
		// gamma_e
		for ( ExtDWE e : this.usProblem.getGraph().edgeSet() ){
			addAndSetColumn("g"+(e.getId()), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
			this.varMap.put("g"+(e.getId()), this.numCols);
		}
	}

	@Override
	protected void generateData() {
		List<Integer> firstRowIndices = new ArrayList<Integer>();
		List<Double> firstRowValues = new ArrayList<Double>();

		// Add data for Flow constraints
		List<INode> nodesList = new ArrayList<INode>(usProblem.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			firstRowIndices.clear();
			firstRowValues.clear();

			INode curNode = nodeIter.next();
			int curNodeId = curNode.getId();
			if ( !curNode.equals(target) ) {
				// don't add outgoing nodes from target
				for (Iterator<ExtDWE> edgeIter = this.usProblem.getGraph().outgoingEdgesOf(curNode).iterator(); edgeIter.hasNext();) {
				//for (Iterator<ExtDWE> edgeIter = this.usProblem.getGraph().edgesOf(curNode).iterator(); edgeIter.hasNext();) {
					ExtDWE curEdge = edgeIter.next();
					
//					if ( !this.varMap.containsKey("g" + curEdge.getId()) ) {
//						System.out.println("1NO: " + "g" + curEdge.getId());
//						System.exit(1);
//					}
					
					firstRowIndices.add(this.varMap.get("g" + curEdge.getId()));
					firstRowValues.add(+1.0);

				}
			}
			if ( !curNode.equals(source)) {
				// don't add incoming edges from source
				Set<ExtDWE> incomingEdgesOf = this.usProblem.getGraph().incomingEdgesOf(curNode);
				//Set<ExtDWE> incomingEdgesOf = this.usProblem.getGraph().edgesOf(curNode);
				for (Iterator<ExtDWE> edgeIter = incomingEdgesOf.iterator(); edgeIter.hasNext();) {
					ExtDWE curEdge = edgeIter.next();
					
//					if ( !this.varMap.containsKey("g" + curEdge.getId()) ) {
//						System.out.println("2NO: " + "g" + curEdge.getId());
//						System.exit(1);
//					}
					
					firstRowIndices.add(this.varMap.get("g" + curEdge.getId()));
					firstRowValues.add(-1.0);
				}
			}
			
//			System.out.println("a -> " + curNodeId);
//			System.out.println("b -> " + this.rowMap.get("SumSo"+curNodeId));
			
			// setMatRow(curNodeId, firstRowIndices, firstRowValues);
			setMatRow(this.rowMap.get("SumSo"+curNodeId), firstRowIndices, firstRowValues);
		}
		this.updateDefenderAllocations();
	}

	@Override
	protected void setProblemType() {
		setProblemType( PROBLEM_TYPE.MIP,  OBJECTIVE_TYPE.MAX);		
	}

}
