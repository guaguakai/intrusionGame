package teamBuildingSolvers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.CompactAllocation;
import model.teamBuildingModels.CompactPath;
import model.teamBuildingModels.CompactStrategy;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.MinCut;
import model.teamBuildingModels.NodePair;
import model.teamBuildingModels.PathConstraint;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.CompactGraph;
import teamBuildingSolvers.CompactOracle.*;
import teamBuildingSolvers.DefenderOracle.BRDefender;
import teamBuildingSolvers.DefenderOracle.DefenderOracle;

public class CompactProblem {
	
	public TeamBuildingProblem teamProb;
	public CompactDefender CompactDef;
	public CompactLB CompactLB;
	public CompactStrategy strategy;
	public CompactGraph compactGraph;
	public static List<CompactPath> attackerPaths;
	public static List<AdversaryPath> lstAP;

	public double runtime=0;
	public List<Double> resources;
	
	public List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();
	public List<CompactAllocation> lDA = new ArrayList<CompactAllocation>();
	
	public HashMap<String,Object[][]> constraints = new HashMap<String,Object[][]>();
	public static boolean useConstraints = true;
	
	public CompactProblem(TeamBuildingProblem teamProb){
		
		this.teamProb = teamProb;
		this.CompactDef = new CompactDefender(teamProb, constraints);
		this.CompactLB = new CompactLB(teamProb, constraints);
		this.strategy = new CompactStrategy();
		this.compactGraph=new CompactGraph();
	}
	

	public double solve(int type) throws LPSolverException{
		switch (type){
			case 0: return run();
			case 1: return run_nocut();
			case 2: return runCutoff(); 
			case 3: return run_LB();
		}
		return run();
	}

	/** 
	 * Runs the Solver using the paths as targets
	 * @return
	 * @throws LPSolverException
	 */
	
	public double cutGen(int cuts) throws LPSolverException{
		CompactStrategy
		double tick = System.currentTimeMillis();

		
		CompactDef.graph = compactGraph.CompactGraph;		
		CompactDef.targetMap = compactGraph.targetMap;
		CompactDef.setAdversaryPaths(uniform, compactGraph.attackerPaths);
		CompactDef.loadProblem();

		CompactAllocation da=null;
		Boolean infeasible = true;
		double iter=0;

		
		while(infeasible){
			CompactDef.solve();
			
			da = CompactDef.setTargetCoverage();
			CompactDef.setAllocation(da);
			infeasible = (useConstraints  && (iter<cuts) && da.feasibleAllocation(CompactDef));
			iter++;
		}
		
		double DefValue = CompactDef.getValue();
		strategy.addDefenderAllocation(da);
	}
	public double run(int cuts) throws LPSolverException{
		
		double tick = System.currentTimeMillis();
		
		compactGraph.addPaths(lstAP);
		compactGraph.getCompactGraph(teamProb);
		attackerPaths=compactGraph.attackerPaths;
		
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<compactGraph.attackerPaths.size();i++){
			uniform.add(1.0/compactGraph.attackerPaths.size());
		}
		
		for(CompactPath ap : compactGraph.attackerPaths){
			teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		
		
		strategy.addAllAdversaryPath(compactGraph.attackerPaths);
		strategy.addAttackerStrategy(uniform);
		
		CompactDef.graph = compactGraph.CompactGraph;		
		CompactDef.targetMap = compactGraph.targetMap;
		CompactDef.setAdversaryPaths(uniform, compactGraph.attackerPaths);
		CompactDef.loadProblem();

		CompactAllocation da=null;
		Boolean infeasible = true;
		
		double[] roc = new double[10];
		
		double prev=0;
		double val=0;
		double diff=0;
		double iter=0;
		double change=0;
		
		while(infeasible){
			//prev=val;
			//CompactDef.writeProb("pathProb.lp");
			
			//double t1=System.currentTimeMillis();
			CompactDef.solve();
			//double t2=System.currentTimeMillis();
			//CompactDef.writeSol("pathProbSol.lp");
			da = CompactDef.setTargetCoverage();
			CompactDef.setAllocation(da);
			//CompactDef.writeProb("pathProb.lp");
//			val = CompactDef.getValue();
//			if(iter<roc.length){
//				roc[(int) iter]=val;
//				change = (val - roc[(int) iter])/(iter+1);
//			}else{
//				double f = roc[(int) ((iter-roc.length)%roc.length)];
//				change = (val - roc[(int) ((iter-roc.length)%roc.length)])/roc.length;
//				roc[(int) (iter%roc.length)]=val;
//			}
//			diff=prev-val;
			//System.out.println(val+" "+change+" "+diff+" "+(t2-t1)); //(iter<roc.length || change>0.1)
			infeasible = (useConstraints  && (iter<cuts) && da.feasibleAllocation(CompactDef));
			//CompactDef.writeProb("pathProb.lp");
			iter++;
			
		}
		
		double DefValue = CompactDef.getValue();
		strategy.addDefenderAllocation(da);
		
		
		CompactLP clp = new CompactLP(strategy);
		clp.loadProblem();
		clp.solve();
		strategy.setDefenderStrategy(clp.getDefenderStrategy());
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		
		double CLPValue = clp.getLPObjective();
		int i=1;
		while(Math.abs(CLPValue - DefValue) > 0.01){
			
			CompactDef.setAdversaryPaths(strategy.getAttackerStrategy(), compactGraph.attackerPaths);
			CompactDef.setObjective();
			
			infeasible = true;
			//while(infeasible){
				//CompactDef.writeProb("pathProb.lp");
				CompactDef.solve();
				//CompactDef.writeSol("pathProbSol.lp");
				da = CompactDef.setTargetCoverage();
				CompactDef.setAllocation(da);
				//infeasible =(useConstraints && da.feasibleAllocation(CompactDef));
				//CompactDef.writeProb("pathProb.lp");
			//}
			
			DefValue = CompactDef.getValue();
			strategy.addDefenderAllocation(da);
			
			clp.generateData();
			clp.solve();
			
			strategy.setDefenderStrategy(clp.getDefenderStrategy());
			strategy.addAttackerStrategy(clp.getAdversaryStrategy());
			
			CLPValue = clp.getLPObjective();
		}
		
		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		resources = CompactDef.getAllocation();
		CompactDef.end();
		return CLPValue;
	}
	
	public double run_nocut() throws LPSolverException{
		
		double tick = System.currentTimeMillis();
		
		compactGraph.getCompactGraph(teamProb);
		attackerPaths=compactGraph.attackerPaths;
		
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<compactGraph.attackerPaths.size();i++){
			uniform.add(1.0/compactGraph.attackerPaths.size());
		}
		
		for(CompactPath ap : compactGraph.attackerPaths){
			teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		
		
		strategy.addAllAdversaryPath(compactGraph.attackerPaths);
		strategy.addAttackerStrategy(uniform);
		
		CompactDef.graph = compactGraph.CompactGraph;
		CompactDef.useConstraints=false;
		CompactDef.targetMap = compactGraph.targetMap;
		CompactDef.setAdversaryPaths(uniform, compactGraph.attackerPaths);
		CompactDef.loadProblem();

		CompactAllocation da=null;

		CompactDef.writeProb("pathProb.lp");
		CompactDef.solve();
		CompactDef.writeSol("pathProbSol.lp");
		da = CompactDef.setTargetCoverage();
		CompactDef.setAllocation(da);
		CompactDef.writeProb("pathProb.lp");

	
		
		double DefValue = CompactDef.getValue();
		strategy.addDefenderAllocation(da);
		
		
		CompactLP clp = new CompactLP(strategy);
		clp.loadProblem();
		clp.solve();
		strategy.setDefenderStrategy(clp.getDefenderStrategy());
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		
		double CLPValue = clp.getLPObjective();
		int i=1;
		while(Math.abs(CLPValue - DefValue) > 0.01){
			
			CompactDef.setAdversaryPaths(strategy.getAttackerStrategy(), compactGraph.attackerPaths);
			CompactDef.setObjective();
			
			CompactDef.writeProb("pathProb.lp");
			CompactDef.solve();
			CompactDef.writeSol("pathProbSol.lp");
			da = CompactDef.setTargetCoverage();
			CompactDef.setAllocation(da);
			CompactDef.writeProb("pathProb.lp");

			
			DefValue = CompactDef.getValue();
			strategy.addDefenderAllocation(da);
			
			clp.generateData();
			clp.solve();
			
			strategy.setDefenderStrategy(clp.getDefenderStrategy());
			strategy.addAttackerStrategy(clp.getAdversaryStrategy());
			
			CLPValue = clp.getLPObjective();
		}

		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		resources = CompactDef.getAllocation();
		return CLPValue;
	}
	
	public double run_LB() throws LPSolverException{
		CompactStrategy strategy = new CompactStrategy();
		
		double tick = System.currentTimeMillis();
		
		compactGraph.getCompactGraph(teamProb);
		attackerPaths=compactGraph.attackerPaths;
		
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<compactGraph.attackerPaths.size();i++){
			uniform.add(1.0/compactGraph.attackerPaths.size());
		}
		
		for(CompactPath ap : compactGraph.attackerPaths){
			teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		
		
		strategy.addAllAdversaryPath(compactGraph.attackerPaths);
		strategy.addAttackerStrategy(uniform);
		
		CompactLB.graph = compactGraph.CompactGraph;
		CompactLB.useConstraints=false;
		CompactLB.targetMap = compactGraph.targetMap;
		CompactLB.setAdversaryPaths(uniform, compactGraph.attackerPaths);
		CompactLB.loadProblem();

		CompactAllocation da=null;
		
		CompactLB.writeProb("pathProblb.lp");
		CompactLB.solve();

		da = CompactLB.setTargetCoverage();
	

		double DefValue = CompactLB.getValue();
		strategy.addDefenderAllocation(da);
		
		
		CompactLP clp = new CompactLP(strategy);
		clp.loadProblem();
		clp.solve();
		strategy.setDefenderStrategy(clp.getDefenderStrategy());
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		
		double CLPValue = clp.getLPObjective();
		int i=1;
		while(Math.abs(CLPValue - DefValue) > 0.01){
			
			CompactLB.setAdversaryPaths(strategy.getAttackerStrategy(), compactGraph.attackerPaths);
			CompactLB.setObjective();
			
			CompactLB.writeProb("pathProb.lp");
			CompactLB.solve();
			CompactLB.writeSol("pathProbSol.lp");
			da = CompactLB.setTargetCoverage();

			DefValue = CompactLB.getValue();
			strategy.addDefenderAllocation(da);
			
			clp.generateData();
			clp.solve();
			
			strategy.setDefenderStrategy(clp.getDefenderStrategy());
			strategy.addAttackerStrategy(clp.getAdversaryStrategy());
			
			CLPValue = clp.getLPObjective();
		}

		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		//resources = CompactLB.getAllocation();
		return CLPValue;
	}
	
	public RuggedBetterResponsesCutoff ruggedObj=null;
	public double runCutoff() throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		compactGraph.getCompactGraph(teamProb);
		attackerPaths=compactGraph.attackerPaths;
		
		for(CompactPath ap : attackerPaths){
			this.teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<compactGraph.attackerPaths.size();i++){
			uniform.add(1.0/compactGraph.attackerPaths.size());
		}
		
		ruggedObj = new RuggedBetterResponsesCutoff(teamProb, 0);
		ruggedObj.run();
		double value = ruggedObj.gameValue;
		ruggedObj.end();
		return value;
	}
	
	public double runFullDef() throws LPSolverException{
		
		double tick = System.currentTimeMillis();
		compactGraph.getCompactGraph(teamProb);
		
		DefenderOracle brDef = new DefenderOracle(teamProb);
		brDef.loadProblem();
		//brDef.writeProb("da");
		brDef.solve();
		
		
		DefenderAllocation da = brDef.getDefenderAllocation();
		boolean allocationAdded = this.teamProb.getActProfile().addDefenderAllocation(da);	// allocationAdded = false if da already generated before
	
		for(CompactPath ap : compactGraph.attackerPaths){
			this.teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<compactGraph.attackerPaths.size();i++){
			uniform.add(1.0/compactGraph.attackerPaths.size());
		}
		
		
		CoreLP clp = new CoreLP(teamProb);
		
		//LOAD LP
		clp.loadProblem();
		clp.solve();
		double CLPValue = clp.getLPObjective();
		
		brDef.updateAdversaryPaths();
		brDef.setAdversaryStrategy(clp.getAdversaryStrategy());
		
		while ( true ) {
			//DEFENDER ORACLE
			
			//brDef.writeProb("da");
			brDef.solve();
		
			
			
			// get solution
			da = brDef.getDefenderAllocation();
			allocationAdded = teamProb.getActProfile().addDefenderAllocation(da);	// allocationAdded = false if da already generated before
			double defenderPayoff = brDef.getDefenderPayoff();

			clp.solve();

			CLPValue = clp.getLPObjective();

			// update BRDef
			List<Double> advStrategy = clp.getAdversaryStrategy();

			brDef.updateAdversaryPaths();
			brDef.setAdversaryStrategy(advStrategy);	
			
			double diffPercentage = Math.abs(CLPValue/100/10); // 0.1%
			
			if((Math.abs(defenderPayoff-CLPValue) <= diffPercentage) ||
					(allocationAdded == false)){
				long stopTime = System.currentTimeMillis();
				break;				
			}
		


		}
		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		strategy.defenderStrategy = clp.getDefenderStrategy();
		//resources = CompactDefP.getAllocation();
		//System.out.print(resources);
		return CLPValue;
		
	}



	public List<AdversaryPath> getAdversaryPaths(){
		List<AdversaryPath> lstPaths = new ArrayList<AdversaryPath>();
		
		for(CompactPath cp : compactGraph.attackerPaths){
			lstPaths.add(cp.toAP());
		}
		return lstPaths;
	}
	
	
	
}
