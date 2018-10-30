package cyberSolvers;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.AbstractBaseGraph;

import lpWrapper.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.IEdge.EDGE_TYPE;
import model.teamBuildingModels.DefenderAllocation;
import cpWrapper.CPProblem;
import cpWrapper.CPProblem.OBJECTIVE_TYPE;

public class Pomdp extends CPProblem{
	
	public int timeHorizon;
	public AbstractBaseGraph network;
	
	List<ExtDWE> edgeList;
	List<INode> nodeList;
	IloNumExpr ObjectiveFunction;
	
	public Pomdp(AbstractBaseGraph graph){
		super();
		network=graph;
		this.nodeList =  new ArrayList<INode>(this.network.vertexSet());
		this.edgeList = new ArrayList<ExtDWE>(this.network.edgeSet());
	
	}
	public double getExpectedPayoff(){
		try {
			return this.cp.getObjValue();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Double.NEGATIVE_INFINITY;
	}
	public void getPolicy(){
	
		for(int t=0;t<timeHorizon;t++){
			for(INode n : nodeList){	
				System.out.println("a"+n.getId()+","+t+": "+cp.getValue(this.getVar("a"+n.getId()+","+t)));
				System.out.println("b"+n.getId()+","+t+": "+cp.getValue(this.getNumVar("b"+n.getId()+","+t)));
			}
		}
		
	}
	public void Objective(){
		
		ArrayList<IloNumExpr> obj = new ArrayList<IloNumExpr>();
		
		for(int t=0;t<timeHorizon;t++){
			for(INode n : nodeList){	
				double prob = 0.8*n.getPayoff();
				
				
				IloNumVar b = this.numMap.get("b"+n.getId()+","+(t));
				IloIntVar a = this.varMap.get("a"+n.getId()+","+(t));

				IloNumExpr o = this.product(a,this.product(b, prob));
				
				obj.add(o);
				
			}
		}
		ObjectiveFunction = this.sumVarArray(obj);
		this.updateObjective(ObjectiveFunction);

	}
	
	public void addDecisonVariables(){
		
		for(int t=0;t<timeHorizon;t++){
			for(INode n : nodeList){		
				this.addDecisionVar("a"+n.getId()+","+t, 0, 1);
				
				this.addNumDecisionVar("b"+n.getId()+","+t, 0, 1);
				this.addNumDecisionVar("bn"+n.getId()+","+t, 0, 1);
				
				this.addNumDecisionVar("tb"+n.getId()+","+t, 0, 1);
				this.addNumDecisionVar("tbn"+n.getId()+","+t, 0, 1);
				
				this.addNumDecisionVar("n"+n.getId()+","+t, 0, 1);
				
			}
		}
	}
	
	public void addRowConstraints(){
		//A resource can only cover x number of edges
		for(int t=1;t<timeHorizon;t++){
		
			for(INode n : nodeList){	
				//IloIntVar bn = this.varMap.get("b"+n.getId()+","+t);
				
				ArrayList<IloNumExpr> nodes = new ArrayList<IloNumExpr>();
				ArrayList<IloNumExpr> nodesN = new ArrayList<IloNumExpr>();
				
				for(INode n2 : nodeList){
					//if( true && !n.equals(n2) && network.containsEdge(n2, n)){
					double transition=0.0;	
					if(n.equals(n2)){
						transition=0.5;
					}else if(n.getId()==1){
						if(n2.getId()==2){
							transition=0.3;
						}
						transition=0.7;
					}
						//double transition = network.getEdgeWeight(network.getEdge(n2, n));
						double prob = 0.8;
						double fprob = 0.1;
						
						IloNumExpr[] nodeVars = new IloNumExpr[3];
						IloNumExpr[] nodeNVars = new IloNumExpr[3];
						
						IloNumVar b = this.numMap.get("b"+n2.getId()+","+(t-1));
						IloNumVar bn = this.numMap.get("b"+n2.getId()+","+(t-1));
						
						IloNumVar normal = this.numMap.get("n"+n2.getId()+","+(t-1));
						
						IloIntVar a = this.varMap.get("a"+n2.getId()+","+(t-1));
						
						IloNumExpr bt = this.product(b, transition);
						IloNumExpr bnt = this.product(b, 1-transition);
						
						IloNumExpr ap = this.product(a, prob);
						IloNumExpr anp = this.product(a, fprob);
						
						nodeVars[0] = this.product(bt, ap);
						nodeVars[1] = this.product(this.product(bt, a),-1.0);
						nodeVars[2] = bt;
						

						nodeNVars[0] = this.product(bnt, anp);
						nodeNVars[1] = this.product(this.product(bnt, a),-1.0);
						nodeNVars[2] = bnt;
						
						IloNumExpr nodej = this.sumExprArray(nodeVars);
						IloNumExpr nodeNj = this.sumExprArray(nodeNVars);
						
						nodes.add(nodej);
						
						nodesN.add(nodeNj);

					//}
				}
				IloNumVar tbn = this.numMap.get("tb"+n.getId()+","+t);
				this.addConstraint(this.sumVarArray(nodes), tbn);
				
				IloNumVar tbnN = this.numMap.get("tbn"+n.getId()+","+t);
				this.addConstraint(this.sumVarArray(nodesN), tbnN);
				
				IloNumVar bn = this.numMap.get("b"+n.getId()+","+t);
				this.addConstraint(this.product(this.numMap.get("n"+n.getId()+","+t), bn), tbn);
				
				IloNumVar bnN = this.numMap.get("bn"+n.getId()+","+t);
				this.addConstraint(this.product(this.numMap.get("n"+n.getId()+","+t), bnN), tbnN);

			}
			
		}
		for(int t=0;t<timeHorizon;t++){
			ArrayList<IloNumExpr> resources= new ArrayList<IloNumExpr>();
			for(INode n : nodeList){				
				resources.add(this.varMap.get("a"+n.getId()+","+t));
				
				this.addConstraint(this.sumVars(this.numMap.get("b"+n.getId()+","+t),this.numMap.get("bn"+n.getId()+","+t)),this.numMap.get("n"+n.getId()+","+t));
			}
			this.addConstraint(this.sumVarArray(resources), 1);
		}
		
		for(INode n : nodeList){
			this.addConstraint(this.numMap.get("b"+n.getId()+","+0), 0.5);
		}
		
	}
	public void writeProblem(){
		String constraints = this.constraints.toString();
		String constraintVal = this.constraintVal.toString();
		String objective = this.objectiveFunction.toString();
		String vars = this.variables.toString();
		
		System.out.println(objective+"\n"+constraints+"\n"+constraintVal  +"\n"+vars);
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
	
	
}
