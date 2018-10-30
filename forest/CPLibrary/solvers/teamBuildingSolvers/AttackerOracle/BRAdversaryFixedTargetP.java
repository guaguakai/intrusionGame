package teamBuildingSolvers.AttackerOracle;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cpWrapper.CPProblem;
import cpWrapper.CPProblem.OBJECTIVE_TYPE;
import lpWrapper.Configuration;
import lpWrapper.AMIProblem.BOUNDS_TYPE;
import lpWrapper.AMIProblem.VARIABLE_TYPE;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;
import model.graphutils.IEdge.EDGE_TYPE;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;

public class BRAdversaryFixedTargetP extends CPProblem {

	private TeamBuildingProblem team;
	
	private INode source;
	private INode target;
	
	private List<Double> defenderStrategy;	
	private int numDefAllocations;
	
	List<ExtDWE> edgeList;
	List<INode> nodesList;

	IloNumExpr ObjectiveFunction;
	
	public BRAdversaryFixedTargetP(TeamBuildingProblem team, INode source, INode target) {
		super();
		this.team = team;
		
		this.source = source;
		this.target = target;
		
		this.numDefAllocations = 0;
		
		this.edgeList = new ArrayList<ExtDWE>(this.team.getGraph().edgeSet());
		this.nodesList = new ArrayList<INode>(this.team.getGraph().vertexSet());
		
		this.cp.setName("BRAdv"+target.getId());
		
		this.defenderStrategy = null;
		this.redirectOutput(null);

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
		this.cp.setName("BRAdv"+target.getId());
	}
	
	public void setDefenderStrategy(List<Double> defenderStrategy){
		this.defenderStrategy = defenderStrategy;
		this.numDefAllocations = defenderStrategy.size();
		
		this.Objective();
		
	}
	
	public AdversaryPath getAdversaryPath() {
		AdversaryPath ap = new AdversaryPath();
		boolean notExistsCycle = true;
		for ( ExtDWE curEdge : edgeList){
			int eId = curEdge.getId();			
			
			if(cp.getValue(this.varMap.get("g"+eId))>Configuration.EPSILON){
				notExistsCycle = ap.addEdgeToPath(curEdge);
				if ( notExistsCycle == false ) {
					System.err.println("Cycle exists in path.");
					System.err.flush();
				}
			}
		}
		
		if ( notExistsCycle == false ) {
			ap.removeCycleFromPath(this.team.getSource());
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
		try {
			return this.cp.getObjValue();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	public void Objective(){
		//team.getActProfile().updateProbability(this.team);
		
		if(numDefAllocations>0){
			IloNumExpr[] payoffs = new IloNumExpr[numDefAllocations];	//expected payoff for each attacker path
			//IloNumExpr payoffs;
			
			//defender coverage of all edges
			IloNumExpr[] Coverage = null;
			IloNumExpr probAlongPath = null;
			
			for(int i=0;i<numDefAllocations;i++){
				double doProbability = defenderStrategy.get(i);
				Coverage = getEdgeCoverage(i); //coverage of all edges by defender allocation i
				
			
				probAlongPath = this.productArray(Coverage);//probability of success of an attacker path
				
				//payoff on path i
				payoffs[i] = this.product(probAlongPath, target.getPayoff()*doProbability);	
				
			}
			
			//Coverage = getEdgeCoverage(); //coverage of all edges by defender allocation i
			//probAlongPath = this.productArray(Coverage);//probability of success of an attacker path
			
			//payoff on path i
			//payoffs = this.product(probAlongPath, target.getPayoff());	
		
			if(payoffs.length>1){
				ObjectiveFunction = this.sumExprArray(payoffs);
			}else{
				ObjectiveFunction = payoffs[0];
			}
			//ObjectiveFunction = payoffs;
		}else{
			
			try {
				ObjectiveFunction=this.cp.constant( target.getPayoff());
			} catch (IloException e) {
				e.printStackTrace();
			}
		}
		
		this.updateObjective(ObjectiveFunction);

	}
	
	private IloNumExpr[] getEdgeCoverage(int i){
	
		DefenderAllocation da = this.team.getActProfile().getDefenderAllocation(i);
		double[] edgeCoverage = new double[edgeList.size()];
		IloIntVar[] pathCoverage = new IloIntVar[edgeList.size()];
		
		int j=0;
		for(ExtDWE e : edgeList){
			pathCoverage[j] = this.getVar("g"+e.getId());
			if(da.contains(e)){
				edgeCoverage[j]=(1-da.getProbability(e));
			}else{
				edgeCoverage[j]=1;
			}
			j++;
		}
		
		return this.exponentArray(edgeCoverage,pathCoverage);
		
	}
	
	private IloNumExpr[] getEdgeCoverage(){
		
		double[] edgeCoverage = new double[edgeList.size()];
		IloIntVar[] pathCoverage = new IloIntVar[edgeList.size()];
		
		int j=0;
		for(ExtDWE e : edgeList){
			pathCoverage[j] = this.getVar("g"+e.getId());
			edgeCoverage[j]=(1-this.team.getActProfile().getEdgeProbability(e));

			j++;
		}
		
		return this.exponentArray(edgeCoverage,pathCoverage);
		
	}

	public void addRowConstraints(){

		// Add data for Flow constraints
		ArrayList<IloIntVar> path = new ArrayList<IloIntVar>();
		ArrayList<Double> pathConst = new ArrayList<Double>();
		
		for ( INode curNode : nodesList ) {
			int curNodeId = curNode.getId();
			
			if ( !curNode.equals(target) ) {
				// don't add outgoing nodes from target
				for (Iterator<ExtDWE> edgeIter = this.team.getGraph().outgoingEdgesOf(curNode).iterator(); edgeIter.hasNext();) {
					ExtDWE curEdge = edgeIter.next();
					
					path.add(this.varMap.get("g" + curEdge.getId()));
					pathConst.add(+1.0);

				}
			}
			if ( !curNode.equals(source)) {
				// don't add incoming edges from source
				Set<ExtDWE> incomingEdgesOf = this.team.getGraph().incomingEdgesOf(curNode);
				for (Iterator<ExtDWE> edgeIter = incomingEdgesOf.iterator(); edgeIter.hasNext();) {
					ExtDWE curEdge = edgeIter.next();
					
					path.add(this.varMap.get("g" + curEdge.getId()));
					pathConst.add(-1.0);
				}
			}
			Integer bound = 0;
			if ( curNode.equals(source) ) {
				// First Row: exit out of source = 1
				bound = 1;
			}
			else if ( curNode.equals(target) ) {
				// Second Row: inflow into sink = 1				
				bound = -1;					
			}
			else {
				bound = 0;
			}
			
			IloNumExpr exrp = this.scalProdSumArray(pathConst, path);					
			this.addConstraint(exrp,bound);
			path.clear();
			pathConst.clear();
			
		}
		//this.updateDefenderAllocations();
	}
	
	public void addDecisonVariables(){
		for(ExtDWE e : edgeList){
			if(!e.getType().equals(EDGE_TYPE.DUAL)){
				this.addDecisionVar("g"+e.getId(), 0, 1);
			}else{
				this.addDecisionVar("g"+e.getId(), 0, 0);
			}
		}
	}

	public void loadProblem() {
		try {			
			this.objectiveType=OBJECTIVE_TYPE.MAX;
			this.addDecisonVariables();
			this.addRowConstraints();
			this.Objective();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void resetLP() {
		this.numDefAllocations = 0;
		this.varMap.clear();
		this.end();
		this.initialize();
		this.loadProblem();
	}
	
	public void writeProblem(){
		String constraints = this.constraints.toString();
		String constraintVal = this.constraintVal.toString();
		String objective = this.objectiveFunction.toString();
		String vars = this.variables.toString();
		
		System.out.println(objective+"\n"+constraints+"\n"+constraintVal  +"\n"+vars);
	}
}
