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


public class BRDefender extends MIProblem {
	private TeamBuildingProblem usProblem;
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
		List<Integer> firstRowIndices = new ArrayList<Integer>();
		List<Double> firstRowValues = new ArrayList<Double>();
		List<Integer> thisRowIndices = new ArrayList<Integer>();
		List<Double> thisRowValues = new ArrayList<Double>();
		
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
		
		//sets total sum
		setMatRow(this.rowMap.get("SumLambda"), firstRowIndices, sumValues);
		thisRowIndices.clear();
		thisRowValues.clear();
		
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		List<ExtDWE> visted = new ArrayList<ExtDWE>();
		
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);
		Set<ExtDWE> out;
		Set<ExtDWE> in;
		
		

		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
				for(ExtDWE e : edgeList){
					INode n1 = e.getsource();
					INode n2 = e.gettarget();
					
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(-1.0);
					
					thisRowIndices.add(this.varMap.get("n"+","+(k+1)+","+(i+1) +"," + n1.getId())); 
					thisRowValues.add(1.0);
	
					setMatRow(this.rowMap.get("Node"+","+(k+1)+","+(i+1)+"s"+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
					
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(-1.0);
					
					thisRowIndices.add(this.varMap.get("n"+","+(k+1)+","+(i+1) +"," + n2.getId())); 
					thisRowValues.add(1.0);

					setMatRow(this.rowMap.get("Node"+","+(k+1)+","+(i+1)+"t"+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
					
					ExtDWE dual = this.usProblem.getGraph().getEdge(n2, n1);
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(1.0);
					
					if(dual!=null){
						thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + dual.getId())); 
						thisRowValues.add(1.0);
					}
					
					setMatRow(this.rowMap.get("Edge"+","+(k+1)+","+(i+1)+","+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
				}
				
			}}
		
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type k
		for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
			for(INode n : nodeList){
				
				thisRowIndices.add(this.varMap.get("n"+","+(k+1)+","+(i+1) +"," + n.getId())); 
				thisRowValues.add(1.0);
				
			}
			setMatRow(this.rowMap.get("SumNODES"+","+(k+1)+","+(i+1)), thisRowIndices, thisRowValues);
			thisRowIndices.clear();
			thisRowValues.clear();
			
		}
		}	
		
		
		Set<ExtDWE> all = new HashSet<ExtDWE>();
		Set<ExtDWE> in2;
		
		Collections.sort(edgeList);
		
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type k
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type i
				for ( ExtDWE e: edgeList) {	
					thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e.getId())); 
					thisRowValues.add(-1.0);
					
					out = this.usProblem.getGraph().outgoingEdgesOf(this.getto(e));
					in = this.usProblem.getGraph().incomingEdgesOf(this.getfrom(e));
					all.addAll(in);
					
					for(ExtDWE edge : out){
						if(!all.contains(edge)){
							all.add(edge);
						}
					}
					
					
					for(ExtDWE e2 : all){
						if((e==e2)){ //|| e2.gettarget().equals(e.getsource())) && e2.getType()!=EDGE_TYPE.VIRTUAL){
							thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
							thisRowValues.add(-1.0);
						}else if((out.contains(e2) ||in.contains(e2))){
							thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
							thisRowValues.add(1.0);
						}
					}
					
//					for ( ExtDWE e2: edgeList) {	
//						//in = this.usProblem.getGraph().edgesOf(e.getsource());
//						//out = this.usProblem.getGraph().edgesOf(e.gettarget());
//						
//						
//						if(e==e2 || e2.gettarget().equals(e.getsource())){
//							thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
//							thisRowValues.add(-1.0);
//						}else if((out.contains(e2) ||in.contains(e2))){
////							for ( ExtDWE e3: edgeList) {	
////								
////								in2 = this.usProblem.getGraph().incomingEdgesOf(this.getfrom(e));
////								out2 = this.usProblem.getGraph().outgoingEdgesOf(this.getto(e));
////
////								if(e3==e2){
////									thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e3.getId())); 
////									thisRowValues.add(1.0);
////								}else if(out2.contains(e3) ||in2.contains(e3)){
//									thisRowIndices.add(this.varMap.get("l"+","+(k+1)+","+(i+1) +"," + e2.getId())); 
//									thisRowValues.add(1.0);
////								}
////							}	
//							

//						}
					//}
					setMatRow(this.rowMap.get("Path"+(k+1)+","+(i+1)+","+e.getId()), firstRowIndices, firstRowValues);
					setMatRow(this.rowMap.get("Path"+(k+1)+","+(i+1)+","+e.getId()), thisRowIndices, thisRowValues);
					thisRowIndices.clear();
					thisRowValues.clear();
					all.clear();
					
				}
			}
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
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type
			
				for(INode n : nodeList){	
					addAndSetColumn("n"+","+(k+1)+","+(i+1) +","+(n.getId()),BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.INTEGER, 0);
					this.varMap.put("n"+","+(k+1)+","+(i+1) +","+(n.getId()), this.numCols);
				}
			}
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
		
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);

			
		for (int k=0;k<this.usProblem.getNumResourceTypes();k++){ //for each resource type
			for (int i=0;i< this.usProblem.getNumDefenderResources().get(k);i++){ //for each resource of that type
				
				addAndSetRow("SumNODES"+","+(k+1)+","+(i+1), BOUNDS_TYPE.FIXED, this.usProblem.ResourceCoverage.get(k)+1, this.usProblem.ResourceCoverage.get(k)+1);
				rowMap.put("SumNODES"+","+(k+1)+","+(i+1), this.numRows);
				
				for(ExtDWE e: edgeList){
					addAndSetRow("Node"+","+(k+1)+","+(i+1)+"t"+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
					rowMap.put("Node"+","+(k+1)+","+(i+1)+"t"+e.getId(), this.numRows);
					
					addAndSetRow("Node"+","+(k+1)+","+(i+1)+"s"+e.getId(), BOUNDS_TYPE.LOWER, 0.0, 0.0);
					rowMap.put("Node"+","+(k+1)+","+(i+1)+"s"+e.getId(), this.numRows);
					
					double val =1.0;
					if(usProblem.ResourceEdges!=null && !e.getType().equals(usProblem.ResourceEdges.get(k))){
						val=0;
					}
					addAndSetRow("Edge"+","+(k+1)+","+(i+1)+","+e.getId(), BOUNDS_TYPE.UPPER, val, val);
					rowMap.put("Edge"+","+(k+1)+","+(i+1)+","+e.getId(), this.numRows);
				}
			}
		}
		
		addAndSetRow("SumLambda", BOUNDS_TYPE.FIXED, this.usProblem.getTotalCoverage(), this.usProblem.getTotalCoverage());
		rowMap.put("SumLambda", this.numRows);
		
		
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.MIP,OBJECTIVE_TYPE.MAX);
	}

	
}
