package teamBuildingSolvers.DefenderOracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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


public class BRDefender extends MIProblem {
	private TeamBuildingProblem usProblem;
	private List<Double> adversaryStrategy; //coefficients for the variables in the objective
	private int numAdvPaths;
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;
	public DefenderAllocation prevDA;
	public boolean newStrategy = false;

	/**
	 * Ordering of function calls: 
	 * loadProblem(); -> setAdversaryStrategy(); -> solve(); -> getDefenderAllocation(); getIntersect();
	 * addAdversaryPaths() -> setAdversaryStrategy(); -> solve();
	 * If adversaryPaths are deleted, delete paths from usProblem, call resetLP(); -> setAdversaryStrategy(); -> solve(); 
	 */
	public BRDefender(TeamBuildingProblem usProblem) {
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
	
	
	public void setPrevDa(DefenderAllocation da){
		prevDA = da;
		newStrategy = true;
		int DAindex = this.usProblem.getActProfile().getDefenderAllocations().indexOf(da);
		
		addAndSetRow("OldDa"+DAindex,BOUNDS_TYPE.UPPER, 0.0, this.usProblem.getTotalCoverage()-1);
		this.rowMap.put("OldDa"+DAindex, this.numRows);
		
		
		int bt1 = this.rowMap.get("SumLambda");
		int bt2 = this.rowMap.get("OldDa"+DAindex);
		List<Integer> oldRowIndices = new ArrayList<Integer>();
		List<Double> oldRowValues = new ArrayList<Double>();
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 					//for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type i
				for ( ExtDWE e : this.usProblem.getGraph().edgeSet() ) {			//for each edge e
					oldRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					if(prevDA.ResourceCoversEdge(e, "Resource"+(k+1)+","+(i+1))){
						oldRowValues.add(1.0);
					}else{
						oldRowValues.add(0.0);
					}
					
				}
			}
		}
		//SETTING THE OLD DEFENDER ALLOCATION
		setMatRow(this.rowMap.get("OldDa"+DAindex), oldRowIndices, oldRowValues);
		
	}
	
	public void resetDa(){
	
		newStrategy = false;
		
		List<Integer> oldRowIndices = new ArrayList<Integer>();
		List<Double> oldRowValues = new ArrayList<Double>();
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 					//for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type i
				for ( ExtDWE e : this.usProblem.getGraph().edgeSet() ) {			//for each edge e
					oldRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					oldRowValues.add(0.0);
					
				}
			}
		}
		//SETTING THE OLD DEFENDER ALLOCATION
		setMatRow(this.rowMap.get("OldDa"), oldRowIndices, oldRowValues);
		
	}
	
	
	public void setAdversaryStrategy(List<Double> adversaryStrategy) {
		//coefficients for the variables in the objective
		final int numEdges = this.usProblem.getGraph().edgeSet().size();
		this.adversaryStrategy = adversaryStrategy;
		
		int buffer = this.usProblem.getTotalResources();
		int rowbuffer = this.rowMap.get("SumLambda")-1;
		
		// Updating the objective
		// obj: max \sum_j (z_j a_j T_{t_j} - a_jT_{t_j}) <- second term is constant and hence is not added
		for ( int j=0; j<this.numAdvPaths; j++) {
			double prob = this.adversaryStrategy.get(j);
			double payoffOnTarget = this.usProblem.getActProfile().getPayoffOfPathOnTarget(j);
			double objCoeff = prob*payoffOnTarget;
			
			int path = this.varMap.get("z"+(j+1));
			int p = numEdges*buffer+numEdges+j;
			
			setObjectiveCoef( path, objCoeff);	
			
			int index = this.rowMap.get("setZ"+(j+1));
			
			if ( Configuration.TRUNCATELPS ) {
				if ( objCoeff <= Configuration.EPSILON ) {
					this.disableRow(index); //this.disableRow(1+rowbuffer + j+1);
				} else {
					this.enableRow(index);
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
			
			
			/*
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
			}*/
			
			for ( ExtDWE edge : this.usProblem.getActProfile().getAdversaryPath(j+curAdvPaths-1).getPath() ) {
				
				int eId = edge.getId();
				
				iaS.add(this.varMap.get("pl"+ eId));
					arS.add(-1.0);
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
						
		//int curDefAllocation = this.usProblem.getActProfile().getNumberOfDefenderAllocations()-1;
		//curDefAllocation = this.usProblem.getActProfile().getDefenderAllocations().indexOf(da);
		
		for ( int j=0; j<this.numAdvPaths; j++) {
			//double probability = this.usProblem.getActProfile().getProbability(curDefAllocation, j);
			double test = this.usProblem.getActProfile().getPayoffOfPathOnTarget(j);
			sumConstants += (adversaryStrategy.get(j)*this.usProblem.getActProfile().getPayoffOfPathOnTarget(j));
		}
		return curObjective - sumConstants;
	}
	
	public double get2realDefenderPayoff(DefenderAllocation da) {
		// getObjective - a_jT_{t_j}
		final double curObjective = this.getLPObjective();
		double sumConstants = 0.0;
					
		//(1-zij)*Ttj.doubleValue()
		
		int curDefAllocation = this.usProblem.getActProfile().getNumberOfDefenderAllocations()-1;
		curDefAllocation = this.usProblem.getActProfile().getDefenderAllocations().indexOf(da);
		
		for ( int j=0; j<this.numAdvPaths; j++) {
			double probability = this.usProblem.getActProfile().getProbability(curDefAllocation, j);
			double Tj = this.usProblem.getActProfile().getPayoffOfPathOnTarget(j);
			
			sumConstants += (1-probability)*(adversaryStrategy.get(j)*this.usProblem.getActProfile().getPayoffOfPathOnTarget(j));
		}
		return -sumConstants;
	}
	
	public double getrealDefenderPayoff(DefenderAllocation da) {
		// getObjective - a_jT_{t_j}
		final double curObjective = this.getLPObjective();
		double sumConstants = 0.0;
					
		//(1-zij)*Ttj.doubleValue()
		
		int curDefAllocation = this.usProblem.getActProfile().getNumberOfDefenderAllocations()-1;
		curDefAllocation = this.usProblem.getActProfile().getDefenderAllocations().indexOf(da);
		
		for ( int j=0; j<this.numAdvPaths; j++) {
			double probability = this.usProblem.getActProfile().getProbability(curDefAllocation, j);
			double Tj = this.usProblem.getActProfile().getPayoffOfPathOnTarget(j);
			
			sumConstants += (1-probability)*(adversaryStrategy.get(j)*this.usProblem.getActProfile().getPayoffOfPathOnTarget(j));
		}
		return -sumConstants;
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
		
		List<Integer> thisRowIndices = new ArrayList<Integer>();
		List<Double> thisRowValues = new ArrayList<Double>();
		//List<Double> oldRowValues = new ArrayList<Double>();
		
		List<Double> sumValues = new ArrayList<Double>();

		// Add elems for \lambda <= k
		firstRowIndices.clear();
		firstRowValues.clear();
		
		//creates a row of zeroes and ones
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 					//for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type i
				for ( ExtDWE e : this.usProblem.getGraph().edgeSet() ) {			//for each edge e
					firstRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					firstRowValues.add(0.0);
					sumValues.add(1.0);
				}
			}
		}
		
		//sets coeffs for edges
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 					//for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type i
				for ( ExtDWE e : this.usProblem.getGraph().edgeSet() ) {			//for each edge e
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(1.0);
					
				}
				setMatRow(this.rowMap.get("SumLambda"+(k+1)+","+(i+1)), firstRowIndices, firstRowValues);
				setMatRow(this.rowMap.get("SumLambda"+(k+1)+","+(i+1)), thisRowIndices, thisRowValues);
				
				//this.updateAdversaryPaths();
				
				thisRowIndices.clear();
				thisRowValues.clear();
			}
		}
		
		//SETTING TOTAL SUM OF EDGES
		setMatRow(this.rowMap.get("SumLambda"), firstRowIndices, sumValues);
		
//		//SETTING THE OLD DEFENDER ALLOCATION
//		setMatRow(this.rowMap.get("OldDa"), firstRowIndices, firstRowValues);
		
		//SETTING PATH CONSTRAINTS ON RESOURCES
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Set<ExtDWE> out;
		Set<ExtDWE> in;
		
		Collections.sort(edgeList);
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
				for ( ExtDWE e: edgeList) {	
					thisRowIndices.clear();
					thisRowValues.clear();
					
					for ( ExtDWE e2: edgeList) {	
						//out = this.usProblem.getGraph().outgoingEdgesOf(this.getto(e));
						out = this.usProblem.getGraph().edgesOf(this.getto(e));
						//in = this.usProblem.getGraph().incomingEdgesOf(this.getfrom(e));
						in = this.usProblem.getGraph().edgesOf(this.getfrom(e));
						
						if(e==e2){
							thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
							thisRowValues.add(-1.0);
						}else if(out.contains(e2)||in.contains(e2)){
							thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
							thisRowValues.add(1.0);
						}
					}
					setMatRow(this.rowMap.get("Path"+(k+1)+","+(i+1)+","+e.getId()), firstRowIndices, firstRowValues);
					setMatRow(this.rowMap.get("Path"+(k+1)+","+(i+1)+","+e.getId()), thisRowIndices, thisRowValues);
				}
			}
		}
		
		//SETTING COVERAGE PROBABILITY ON EACH EDGE
		for ( ExtDWE e: edgeList) {	
			List<Integer> iaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			// add z_j
			
			iaS.add(this.varMap.get("pl"+e.getId()));
			arS.add(1.0);

			for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ 					//for each resource type
				for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ 	//for each resource of that type	
					iaS.add(this.varMap.get("l"+","+(k+1)+","+(i+1)+","+ e.getId()));
		
					if(this.usProblem.isProbability){
						arS.add(-this.usProblem.CoverageProbability.get(k));
					
					}else{
						arS.add(-1.0);
					}
				}
			}
			
			this.setMatRow(this.rowMap.get("PLambda"+e.getId()), iaS, arS);
		}
		
			
				firstRowIndices.clear();
				firstRowValues.clear();
				sumValues.clear();

		this.updateAdversaryPaths();
	}
	public void getPath(int length,int depth, INode n, List<INode> end, List<INode> set){
		if(!set.contains(n)){
			set.add(n);
		}
		if(depth==length){
			if(!end.contains(n)){
				end.add(n);
			}
		}else{
			for(ExtDWE nxt: this.usProblem.getGraph().outgoingEdgesOf(n)){
				if(this.getto(nxt)!=null){
					getPath(length, depth+1,this.getto(nxt), end, set);
				}
			}
		}
	}
	
	public INode getto(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(usProblem.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: this.usProblem.getGraph().incomingEdgesOf(curNode)){
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}
	public INode getfrom(ExtDWE e){
		
		List<INode> nodesList = new ArrayList<INode>(usProblem.getGraph().vertexSet());
		Collections.sort(nodesList);
		for ( Iterator<INode> nodeIter = nodesList.iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			for(ExtDWE newedge: this.usProblem.getGraph().outgoingEdgesOf(curNode)){
				if(newedge==e){
					return curNode;
				}
			}
		}
		return null;
		
	}
	@Override
	public void setColBounds() {
		/*
		 * Columns: \lambda_e, z_j
		 * Number of Columns: No. of edges + No. of Adversary Paths
		 */
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type
			
				for ( ExtDWE e: edgeList) {			
					if(e.getType().equals(EDGE_TYPE.NORMAL)){
						addAndSetColumn("l"+","+(k+1)+","+(i+1) +","+(e.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
					}else{
						addAndSetColumn("l"+","+(k+1)+","+(i+1) +","+(e.getId()),BOUNDS_TYPE.FIXED, 0, 0, VARIABLE_TYPE.INTEGER, 0);
					}
					this.varMap.put("l"+","+(k+1)+","+(i+1) +","+(e.getId()), this.numCols);
				}
			}
		}
		for ( ExtDWE e: edgeList) {			
			if(e.getType().equals(EDGE_TYPE.NORMAL)){
				addAndSetColumn("pl"+(e.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);
			}else{
				addAndSetColumn("pl"+(e.getId()),BOUNDS_TYPE.FIXED, 0, 0, VARIABLE_TYPE.INTEGER, 0);
			}
			this.varMap.put("pl"+(e.getId()), this.numCols);
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
		
		for (int j=0;j<this.usProblem.getNumResourceTypes();j++){
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(j);i++){
				//SETS THE ROWS FOR THE EDGE SETS SUM LAMBDA
				addAndSetRow("SumLambda"+(j+1)+","+(i+1), BOUNDS_TYPE.FIXED, this.usProblem.ResourceCoverage.get(j), this.usProblem.ResourceCoverage.get(j));
				rowMap.put("SumLambda"+(j+1)+","+(i+1), this.numRows);
				
				for ( ExtDWE e: edgeList) {	
					if(usProblem.ResourceCoverage.get(j).equals(1.0)){
						addAndSetRow("Path"+(j+1)+","+(i+1)+","+e.getId(), BOUNDS_TYPE.LOWER, Double.NEGATIVE_INFINITY, 0.0);
					}else{
						addAndSetRow("Path"+(j+1)+","+(i+1)+","+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
					}
					rowMap.put("Path"+(j+1)+","+(i+1)+","+e.getId(), this.numRows);
				}
				
					
					
			}
		}
		for ( ExtDWE e: edgeList) {	
			addAndSetRow("PLambda"+e.getId(),BOUNDS_TYPE.UPPER, -Configuration.MM, 0.0);
			this.rowMap.put("PLambda"+e.getId(), this.numRows);
		}
		
		addAndSetRow("SumLambda", BOUNDS_TYPE.FIXED, this.usProblem.getTotalCoverage(), this.usProblem.getTotalCoverage());
		rowMap.put("SumLambda", this.numRows);
		
//		addAndSetRow("OldDa",BOUNDS_TYPE.UPPER, this.usProblem.getTotalCoverage()-1, 0.0);
//		this.rowMap.put("OldDa", this.numRows);
		
		
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}

	
}
