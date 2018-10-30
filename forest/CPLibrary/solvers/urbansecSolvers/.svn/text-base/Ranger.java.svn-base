package urbansecSolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lpWrapper.Configuration;
import lpWrapper.MIProblem;
import model.graphutils.ExtDWE;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode;
import model.urbansecModels.DefenderAllocation;
import model.urbansecModels.UrbanSecProblem;

public class Ranger extends MIProblem {
	List<INode> targetSet;
	INode source;
	private UrbanSecProblem usProblem;
	private List<DefenderAllocation> defenderAllocations;
	private List<Double> defenderStrategy;
	private List<Double> rangerCoverage;
	private boolean isSamplingDone;
	List<List<ExtDWE>> buckets;
	
	private Map<String, Integer> varMap;
	private Map<String, Integer> rowMap;
	
	public Ranger(UrbanSecProblem usProblem) {
		super();
		this.usProblem = usProblem;
		isSamplingDone = false;
		buckets = null;
		varMap = new HashMap<String, Integer>();
		rowMap = new HashMap<String, Integer>();
	}

	public INode getSource() {
		return source;
	}

	public void setSource(INode source) {
		this.source = source;
	}

	public List<INode> getTarget() {
		return targetSet;
	}
	public void setTarget(INode target) {
		this.targetSet = new ArrayList<INode>();
		this.targetSet.add(target);
	}
	public void setTarget(List<INode> target) {
		this.targetSet = target;
	}

	@Override
	protected void setProblemType() {
		// TODO Auto-generated method stub
		setProblemType(PROBLEM_TYPE.LP, OBJECTIVE_TYPE.MAX);
	}

	@Override
	protected void setRowBounds() {
		// TODO Auto-generated method stub
		for ( INode curTarget : targetSet ) {
			addAndSetRow("ObjRow"+curTarget.getId(), BOUNDS_TYPE.UPPER, -Configuration.MM, -curTarget.getPayoff());
			rowMap.put("ObjRow"+curTarget.getId(), this.numRows);
		}

		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);

		// define duv rows
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		for ( ExtDWE curEdge : edgeList ) {
			addAndSetRow("d"+curEdge.getId(), BOUNDS_TYPE.UPPER, -Configuration.MM, 0);
			rowMap.put("d"+curEdge.getId(), this.numRows);
		}

		// sum_xe
		addAndSetRow("sumXe", BOUNDS_TYPE.FIXED, this.usProblem.getNumDefenderResources(), this.usProblem.getNumDefenderResources());
		rowMap.put("sumXe", this.numRows);
	}

	@Override
	protected void setColBounds() {
		// TODO Auto-generated method stub
		addAndSetColumn("obj", BOUNDS_TYPE.FREE, -Configuration.MM, Configuration.MM, VARIABLE_TYPE.CONTINUOUS, 1);
		varMap.put("obj", this.numCols);
		
		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);
		for ( INode curNode : nodeList) {
			if ( curNode == source ) {
				addAndSetColumn("d"+curNode.getId(), BOUNDS_TYPE.FIXED, 0, 0, VARIABLE_TYPE.CONTINUOUS, 0);
			} else {
				addAndSetColumn("d"+curNode.getId(), BOUNDS_TYPE.UPPER, -Configuration.MM, 1, VARIABLE_TYPE.CONTINUOUS, 0);							
			}
			varMap.put("d"+curNode.getId(), this.numCols);
		}
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		for ( ExtDWE curEdge : edgeList ) {
			if ( curEdge.getType() == EDGE_TYPE.VIRTUAL ) {
				addAndSetColumn("x"+curEdge.getId(), BOUNDS_TYPE.FIXED, 0, 0, VARIABLE_TYPE.CONTINUOUS, 0);				
			} else {
				addAndSetColumn("x"+curEdge.getId(), BOUNDS_TYPE.DOUBLE, 0, 1, VARIABLE_TYPE.CONTINUOUS, 0);				
			}
			varMap.put("x"+curEdge.getId(), this.numCols);
		}
	}

	@Override
	protected void generateData() {
		// TODO Auto-generated method stub
		int numNodes = this.usProblem.getGraph().vertexSet().size();
		int numEdges = this.usProblem.getGraph().edgeSet().size();
		List<Integer> firstRowIndices = new ArrayList<Integer>();
		List<Double> firstRowValues = new ArrayList<Double>();

		List<INode> nodeList = new ArrayList<INode>(this.usProblem.getGraph().vertexSet());
		Collections.sort(nodeList);
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);

		// First Row:
		// obj <= - (1 - d_t)T(t)
		
		int curRowNumber = 0;
		for ( INode curTarget : targetSet ) {
			int indexOfTarget = nodeList.indexOf(curTarget);
			firstRowIndices.clear();
			firstRowValues.clear();
//			firstRowIndices.add(1); //d
			firstRowIndices.add(varMap.get("obj")); //d
			firstRowValues.add(1.0);
//			firstRowIndices.add(indexOfTarget+1);
			firstRowIndices.add(varMap.get("d" + curTarget.getId()));
			firstRowValues.add((double) -curTarget.getPayoff());
			// setMatRow(curRowNumber + 1, firstRowIndices, firstRowValues);
			setMatRow(rowMap.get("ObjRow"+curTarget.getId()), firstRowIndices, firstRowValues);
			curRowNumber ++;
		}

		// set data for duv rows
		int rowNumber = 1;
		for ( ExtDWE curEdge : edgeList) {
			firstRowIndices.clear();
			firstRowValues.clear();
			int edgeSourceId = this.usProblem.getGraph().getEdgeSource(curEdge).getId();
			int edgeTargetId = this.usProblem.getGraph().getEdgeTarget(curEdge).getId();
			// dv						
			firstRowIndices.add(varMap.get("d"+edgeTargetId));
			firstRowValues.add(1.0);
			// du
			firstRowIndices.add(varMap.get("d"+edgeSourceId));
			firstRowValues.add(-1.0);
			// xe
			firstRowIndices.add(varMap.get("x"+curEdge.getId()));
			firstRowValues.add(-1.0);
			setMatRow(rowMap.get("d"+curEdge.getId()), firstRowIndices, firstRowValues);
			rowNumber ++;
		}

		// sumXe row
		firstRowIndices.clear();
		firstRowValues.clear();
		for ( ExtDWE curEdge : edgeList) {			
			firstRowIndices.add(varMap.get("x"+curEdge.getId()));
			firstRowValues.add(1.0);
		}
		setMatRow(rowMap.get("sumXe"), firstRowIndices, firstRowValues);
		
		
	}

	public Map<ExtDWE, Double> getRangerCoverage() {
		Map<ExtDWE, Double> rangerCoverageProb = new HashMap<ExtDWE, Double>();
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		int numNodes = this.usProblem.getGraph().vertexSet().size();
		for ( ExtDWE curEdge : edgeList) {			
//			double colPrimal = this.getColumnPrimal(1+numNodes + curEdge.getId());
			double colPrimal = this.getColumnPrimal(varMap.get("x" + curEdge.getId()));
			rangerCoverageProb.put(curEdge, colPrimal);
		}
		return rangerCoverageProb;
	}
	
	private List<List<ExtDWE>> getBuckets() {
		if ( this.buckets != null) {
			return this.buckets;
		}
		
		int numNodes = this.usProblem.getGraph().vertexSet().size();
		int numEdges = this.usProblem.getGraph().edgeSet().size();
		rangerCoverage = new ArrayList<Double>(numEdges);
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>(this.usProblem.getGraph().edgeSet());
		Collections.sort(edgeList);
		Map<ExtDWE, Integer> edgeToIndexMap = new HashMap<ExtDWE, Integer>();
		int count = 0;
		for ( ExtDWE curEdge : edgeList) {
//			double colPrimal = this.getColumnPrimal(1+numNodes + curEdge.getId());			
			double colPrimal = this.getColumnPrimal(varMap.get("x" + curEdge.getId()));
			rangerCoverage.add(colPrimal);
			edgeToIndexMap.put(curEdge, count);
		}

		// System.out.println("Ranger strategy: " + rangerCoverage);
//		System.out.println("NUM EDGES: " + numEdges + "  RANGER SIZE: " + rangerCoverage.size());

		List<Integer> rangerCoverageActual = new ArrayList<Integer>(numEdges);
		
		long sum = 0;
		double minsum=0;
		for ( Double d: rangerCoverage) {
			int cov = (int) (d* model.Configuration.RANGER_BUCKETS);
			minsum += d;
			rangerCoverageActual.add(cov);
			sum += cov;
		}
		System.out.println("Sum Probs from Ranger: " + sum/(double) model.Configuration.RANGER_BUCKETS);
		
		int leftOverProb = (int) (this.usProblem.getNumDefenderResources()* model.Configuration.RANGER_BUCKETS - sum); 
		rangerCoverageActual.set(numEdges-1, rangerCoverageActual.get(numEdges-1) + leftOverProb); 

//		System.out.println(rangerCoverageActual);

		// now sample
		// fill buckets with proper edges
		int numResources = this.usProblem.getNumDefenderResources();
		buckets = new ArrayList<List<ExtDWE>>(numResources);
		for ( int i=0; i<numResources; i++) {
			buckets.add(new ArrayList<ExtDWE>());
		}		

		int curEdgeIndex = 0;
		int sampleNumber=0;
		int cellsToFill = (int) rangerCoverageActual.get(0); //edgeList.get(curEdgeIndex).getId());
		int bucketNumber=0;
		while ( bucketNumber<numResources && curEdgeIndex < numEdges ) {
			while (sampleNumber < model.Configuration.RANGER_BUCKETS && cellsToFill > 0) {
				buckets.get(bucketNumber).add(edgeList.get(curEdgeIndex));
				sampleNumber++;
				cellsToFill --;
			}
			if  (sampleNumber == model.Configuration.RANGER_BUCKETS) {
				sampleNumber=0;
				bucketNumber++;
			}
			if ( cellsToFill == 0) {
				curEdgeIndex ++;
				if ( curEdgeIndex < numEdges) {
					cellsToFill = (int) (rangerCoverageActual.get(curEdgeIndex)); //edgeList.get(curEdgeIndex).getId()-1));
				}
				else {
					cellsToFill = 0;
				}
			}
		}		
		return buckets;
	}
	
	private void rangerBucketSampling() {
		//System.out.println("RANGER resources: "+ this.usProblem.getNumDefenderResources());
		if ( isSamplingDone == true ) 
			return;
		List<List<ExtDWE>> buckets = this.getBuckets();

//		System.out.println("Buckets: " + buckets);
		
//		System.out.println(buckets);
		int numResources = this.usProblem.getNumDefenderResources();
		// now slice
		defenderAllocations = new ArrayList<DefenderAllocation>();
		defenderStrategy = new ArrayList<Double>();
		DefenderAllocation prevAlloc = null;
		for ( int s=0; s< model.Configuration.RANGER_BUCKETS; s++) {
			DefenderAllocation newAlloc = new DefenderAllocation();
			for ( int b=0; b<numResources; b++) {
				
//				System.out.println(buckets.size() + " " + b + " " + s + " " + buckets.get(b).size());

				newAlloc.addEdgeToAllocation(buckets.get(b).get(s));
			}
			if ( newAlloc.equals(prevAlloc)) {
				Double prevProb = defenderStrategy.get(defenderStrategy.size()-1);
				defenderStrategy.set(defenderStrategy.size()-1, prevProb+1);
			} else {
				defenderAllocations.add(newAlloc);
				defenderStrategy.add(1.0);
				prevAlloc = newAlloc;
			}
		}

		// renormalize
		for ( int i=0; i<defenderStrategy.size(); i++) {
			defenderStrategy.set(i, defenderStrategy.get(i)*1.0 / model.Configuration.RANGER_BUCKETS );
		}

		isSamplingDone = true;
	}

	public List<Double> getRangerSample() {
		rangerBucketSampling();
		return rangerCoverage;
	}

	public List<DefenderAllocation>  getDefenderAllocations() {
		rangerBucketSampling();
		return defenderAllocations;
	}

	public List<Double> getDefenderStrategy() {
		rangerBucketSampling();
		return defenderStrategy;
	}
}
