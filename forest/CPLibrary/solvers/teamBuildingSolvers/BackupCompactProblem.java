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
import teamBuildingSolvers.CompactOracle.*;
import teamBuildingSolvers.DefenderOracle.BRDefender;
import teamBuildingSolvers.DefenderOracle.DefenderOracle;

public class BackupCompactProblem {
	
	public TeamBuildingProblem teamProb;
	
	public CompactDefender CompactDef;
	private CompactDefenderP CompactDefP;
	public MatrixCompactDefender CompactDef_E;
	private MatrixCompactDefender_P CompactDef_EP;
	
	public CompactStrategy strategy;

	public boolean useMincut = true;
	
	public static List<CompactPath> attackerPaths;
	public HashMap<INode, INode> targetMap;
	
	private static int[][] distances;
	private static NodePair[][] intersects;
	
	
	int maxcoverage = 0;
	
	public AbstractBaseGraph<INode, ExtDWE> graph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
	public AbstractBaseGraph<INode, ExtDWE> CompactGraph;
	
	public double runtime=0;
	public List<Double> resources;
	
	public List<DefenderAllocation> lstDA = new ArrayList<DefenderAllocation>();
	public List<CompactAllocation> lDA = new ArrayList<CompactAllocation>();
	
	public HashMap<String,Object[][]> constraints = new HashMap<String,Object[][]>();
	public static boolean useConstraints = true;
	
	public BackupCompactProblem(TeamBuildingProblem teamProb){
		
		this.teamProb = teamProb;
		
		this.CompactDef = new CompactDefender(teamProb, constraints);
		this.CompactDefP = new CompactDefenderP(teamProb);
		this.CompactDef_E = new MatrixCompactDefender(teamProb);
		this.CompactDef_EP = new MatrixCompactDefender_P(teamProb);
		this.strategy = new CompactStrategy();
		
		CompactAllocation.brDef=this.CompactDef_E;
		setMaxCoverage();
		Graphs.addGraph(this.graph, teamProb.getGraph());
		attackerPaths = new ArrayList<CompactPath>();
		targetMap = new HashMap<INode, INode>();
	}
	
	private void setMaxCoverage(){
		for(double cov : teamProb.ResourceCoverage){
			if( cov > maxcoverage){
				this.maxcoverage = (int)cov;
			}
		}
	}
	
	
	/**
	 * Cleans up graph to remove virtual edges and sources
	 * @param graph
	 * @return cleaned up graph with no virtual edges or sources
	 */
	public AbstractBaseGraph<INode,ExtDWE> removeVirtualEdges(AbstractBaseGraph<INode,ExtDWE> graph){
		
		List<ExtDWE> toRemove = new ArrayList<ExtDWE>();
		List<INode> nodestoRemove = new ArrayList<INode>();
		
		for(ExtDWE e : graph.edgeSet()){
			if(e.getType()==EDGE_TYPE.VIRTUAL){
				toRemove.add(e);
				if(!nodestoRemove.contains(e.getsource())){
				nodestoRemove.add(e.getsource());
			}
			if(!nodestoRemove.contains(e.gettarget())){
				nodestoRemove.add(e.gettarget());
			}
		
			}
		}
		graph.removeAllEdges(toRemove);

		return graph;
	}
	/**
	 * Cleans up graph to remove virtual edges and sources
	 * @param graph
	 * @return cleaned up graph with no virtual edges or sources
	 */
	public UndirectedGraph<INode,ExtDWE> removeVirtualEdges(UndirectedGraph<INode,ExtDWE> graph){
		
		List<ExtDWE> toRemove = new ArrayList<ExtDWE>();
		for(ExtDWE e : graph.edgeSet()){
			if(e.getType()==EDGE_TYPE.VIRTUAL || e.getsource().getType()==NODE_TYPE.SOURCE){
				toRemove.add(e);			
			}
		}
		graph.removeAllEdges(toRemove);
		

		for(INode v : graph.vertexSet()){
			if(v.getType()==NODE_TYPE.SOURCE){
				graph.removeVertex(v);
				break;
			}
		}

		return graph;
	}

	
	public double solve(int type) throws LPSolverException{
		switch (type){
			case 0: return run();
			case 1:	if(teamProb.isProbability) { return run_E(); }else{ return run_E();} 
			case 2: return runCutoff(); 
		}
		return run_E();
			
			
			//return 
			
	}

	/** 
	 * Runs the Solver using the paths as targets
	 * @return
	 * @throws LPSolverException
	 */
	
	public double run() throws LPSolverException{
		
		double tick = System.currentTimeMillis();
		
		this.CompactGraph = this.getCompactGraph();
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<attackerPaths.size();i++){
			uniform.add(1.0/attackerPaths.size());
		}
		
		for(CompactPath ap : attackerPaths){
			teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		
		
		strategy.addAllAdversaryPath(attackerPaths);
		strategy.addAttackerStrategy(uniform);
		
		CompactDef.graph = this.CompactGraph;		
		CompactDef.targetMap = this.targetMap;
		CompactDef.setAdversaryPaths(uniform, attackerPaths);
		CompactDef.loadProblem();

		CompactAllocation da=null;
		Boolean infeasible = true;
		
		while(infeasible){
			CompactDef.writeProb("pathProb.lp");
			CompactDef.solve();
			CompactDef.writeSol("pathProbSol.lp");
			da = CompactDef.setTargetCoverage();
			CompactDef.setAllocation(da);
			CompactDef.writeProb("pathProb.lp");
			infeasible = (useConstraints && da.feasibleAllocation(CompactDef));
			CompactDef.writeProb("pathProb.lp");
		}
		
		
		//CompactDef.writeProb("def");
		//CompactDef.solve();
		//CompactDef.writeSol("defsol");
		//CompactAllocation da = CompactDef.setTargetCoverage();
		
		double DefValue = CompactDef.getValue();
		
		strategy.addDefenderAllocation(da);
		
		
		CompactLP clp = new CompactLP(strategy);
		clp.loadProblem();
		//clp.writeProb("clp");
		clp.solve();
		//clp.writeSol("clpsol");
		strategy.setDefenderStrategy(clp.getDefenderStrategy());
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		
		double CLPValue = clp.getLPObjective();
		int i=1;
		while(Math.abs(CLPValue - DefValue) > 0.01){
			
			CompactDef.setAdversaryPaths(strategy.getAttackerStrategy(), attackerPaths);
			//CompactDef.resetLP();
			//CompactDef.loadProblem();
			CompactDef.setObjective();
			
			infeasible = true;
			while(infeasible){
				CompactDef.writeProb("pathProb.lp");
				CompactDef.solve();
				CompactDef.writeSol("pathProbSol.lp");
				da = CompactDef.setTargetCoverage();
				CompactDef.setAllocation(da);
				infeasible =(useConstraints && da.feasibleAllocation(CompactDef));
				CompactDef.writeProb("pathProb.lp");
			}
			
			
			//CompactDef.writeProb("def"+i);
			//CompactDef.solve();
			//CompactDef.writeSol("defsol"+i);
			//da = CompactDef.setTargetCoverage();
			//CompactDef.setAllocation(da);
			DefValue = CompactDef.getValue();
			
			strategy.addDefenderAllocation(da);
			
			clp.generateData();
			//clp.writeProb("clp"+i);
			clp.solve();
			//clp.writeSol("clpsol"+i);
			
			strategy.setDefenderStrategy(clp.getDefenderStrategy());
			strategy.addAttackerStrategy(clp.getAdversaryStrategy());
			
			CLPValue = clp.getLPObjective();
			//System.out.println(CLPValue);
			//i++;
		}
		
		//CompactDef.writeProb("def");


		
		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		resources = CompactDef.getAllocation();
		//System.out.print(resources);
		return CLPValue;
	}
	public RuggedBetterResponsesCutoff ruggedObj=null;
	public double runCutoff() throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		this.CompactGraph = this.getCompactGraph();
		
		
		for(CompactPath ap : attackerPaths){
			this.teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<attackerPaths.size();i++){
			uniform.add(1.0/attackerPaths.size());
		}
		
		ruggedObj = new RuggedBetterResponsesCutoff(teamProb, 0);
		ruggedObj.run();
		double value = ruggedObj.gameValue;
		ruggedObj.end();
		return value;
	}
	
	public double runFullDef() throws LPSolverException{
		
		double tick = System.currentTimeMillis();
		this.CompactGraph = this.getCompactGraph();
		
		DefenderOracle brDef = new DefenderOracle(teamProb);
		brDef.loadProblem();
		//brDef.writeProb("da");
		brDef.solve();
		
		
		DefenderAllocation da = brDef.getDefenderAllocation();
		boolean allocationAdded = this.teamProb.getActProfile().addDefenderAllocation(da);	// allocationAdded = false if da already generated before
	
		for(CompactPath ap : attackerPaths){
			this.teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<attackerPaths.size();i++){
			uniform.add(1.0/attackerPaths.size());
		}
		
		
		CoreLP clp = new CoreLP(teamProb);
		
		
		//brDef.setAdversaryStrategy(uniform);
		
		
		
		
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
			


			// update problem data structures
			// update and solve Core-LP model
			// avoids repetition of the same allocation
			// avoids repetition of the same path 
			//boolean pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded = false if ap already generated before			


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
	
	public double runP() throws LPSolverException{
		
		double tick = System.currentTimeMillis();
		
		this.CompactGraph = this.getCompactGraph();
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<attackerPaths.size();i++){
			uniform.add(1.0/attackerPaths.size());
		}
		
	
		strategy.addAllAdversaryPath(attackerPaths);
		strategy.addAttackerStrategy(uniform);
		
		
		
		CompactDefP.setGraph(this.CompactGraph);
		CompactDefP.targetMap = this.targetMap;
		CompactDefP.setAdversaryPaths(uniform, attackerPaths);
		CompactDefP.loadProblem();
		//CompactDefP.writeProb("def");
		CompactDefP.solve();
		//CompactDefP.writeSol("defsol");
		CompactAllocation da = CompactDefP.setTargetCoverage();
		//CompactDef.setAllocation(da);
		double DefValue = CompactDefP.getValue();
		
		strategy.addDefenderAllocation(da);
		
		CompactLP clp = null;
		if(teamProb.buildteam){
			clp = new CompactLP(strategy, teamProb);
		}else{
			clp = new CompactLP(strategy);
		}
		clp.loadProblem();
		//clp.writeProb("clp");
		clp.solve();
		//clp.writeSol("clpsol");
		strategy.setDefenderStrategy(clp.getDefenderStrategy());
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		
		double CLPValue = clp.getLPObjective();
		int i=1;
		while(Math.abs(CLPValue + DefValue) > 0.01){
			
			CompactDefP.setAdversaryPaths(strategy.getAttackerStrategy(), attackerPaths);
			//CompactDefP.resetLP();
			//CompactDefP.loadProblem();
			//CompactDefP.writeProb("def"+i);
			CompactDefP.solve();
			//CompactDefP.writeSol("defsol"+i);
			da = CompactDefP.setTargetCoverage();
			//CompactDef.setAllocation(da);
			DefValue = CompactDefP.getValue();
			
			strategy.addDefenderAllocation(da);
			
			clp.generateData();
			//clp.writeProb("clp"+i);
			clp.solve();
			//clp.writeSol("clpsol"+i);
			
			strategy.setDefenderStrategy(clp.getDefenderStrategy());
			strategy.addAttackerStrategy(clp.getAdversaryStrategy());
			
			CLPValue = clp.getLPObjective();
			//System.out.println(CLPValue);
			//i++;
		}
		
		//CompactDef.writeProb("def");


		
		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		//resources = CompactDefP.getAllocation();
		//System.out.print(resources);
		return CLPValue;
	}
	
	//public HashMap<String, List<NodePair>> constraints = new HashMap<String, List<NodePair>>();
	
	
	public DefenderAllocation DA;
	
	/**
	 * Runs the Solver using the intersection points between paths as targets
	 * @return
	 * @throws LPSolverException
	 */
	public double run_E() throws LPSolverException{
		
		double tick = System.currentTimeMillis();
		
		CompactGraph = this.getCompactGraph();
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<attackerPaths.size();i++){
			uniform.add(1.0/attackerPaths.size());
		}
		
		for(CompactPath ap : attackerPaths){
			teamProb.getActProfile().addAdversaryPath(ap.toAP());
		}
		
		strategy.addAllAdversaryPath(attackerPaths);
		strategy.addAttackerStrategy(uniform);
		
		CompactDef_E.distances=intersects;
		CompactDef_E.numIntersection=intersects.length;
		CompactDef_E.numPaths=attackerPaths.size();
		
		CompactDef_E.setAdversaryPaths(uniform, attackerPaths);
		CompactDef_E.loadProblem();
		CompactDef_E.writeProb("def");
		
		
		CompactAllocation da=null;
		Boolean infeasible = true;
		
		while(infeasible){
			CompactDef_E.solve();
			CompactDef_E.writeSol("pathProbSol.lp");
			da = CompactDef_E.setTargetCoverage();
			infeasible = false;//da.checkAllocation(CompactDef_E.constraints);
			//CompactDef_E.addConstraintMap();
			CompactDef_E.writeProb("pathProb.lp");
		}
		
		boolean added = !lDA.contains(da);
		lDA.add(da);

		double DefValue = CompactDef_E.getValue();
		
		strategy.addDefenderAllocation(da);
		
		CompactLP clp=null;

		clp = new CompactLP(strategy);
		clp.loadProblem();
		clp.solve();

		strategy.setDefenderStrategy(clp.getDefenderStrategy());
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		
		double CLPValue = clp.getLPObjective();
		
		int i=1;
		while((Math.abs(CLPValue - DefValue) > 0.0001)){ // && added==true){
			
			
			CompactDef_E.setAdversaryPaths(strategy.getAttackerStrategy(), attackerPaths);
			CompactDef_E.setObjective();
			CompactDef_E.writeProb("def"+i);
			
			infeasible = true;
			while(infeasible){
				CompactDef_E.solve();
				CompactDef_E.writeSol("defsol"+i);
				da = CompactDef_E.setTargetCoverage();
				infeasible = false;//da.checkAllocation(constraints);
				//CompactDef_E.addConstraintMap(constraints);
				CompactDef_E.writeProb("pathProb.lp");
			}
			
			added = !lstDA.contains(da);
			lDA.add(da);

		
			DefValue = CompactDef_E.getValue();
			
			strategy.addDefenderAllocation(da);
			
			clp.generateData();
			//clp.writeProb("clp"+i);
			clp.solve();
			//clp.writeSol("clpsol"+i);
			
			strategy.setDefenderStrategy(clp.getDefenderStrategy());
			strategy.addAttackerStrategy(clp.getAdversaryStrategy());
			
			CLPValue = clp.getLPObjective();
			//System.out.println(CLPValue);
			//i++;
		}
		
		//this.teamProb.getActProfile().deleteAllDefenderAllocations();
		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		//resources = CompactDef_E.getAllocation();
		//System.out.print(resources);
		return CLPValue;
	}
	
	
	public double run_EP() throws LPSolverException{
		List<CompactAllocation> lstda = new ArrayList<CompactAllocation>();
		double tick = System.currentTimeMillis();
		
		CompactGraph = this.getCompactGraph();
		List<Double> uniform = new ArrayList<Double>();
		for(int i=0;i<attackerPaths.size();i++){
			uniform.add(1.0/attackerPaths.size());
		}
		
	
		strategy.addAllAdversaryPath(attackerPaths);
		strategy.addAttackerStrategy(uniform);
		
		
		
		CompactDef_EP.distances=intersects;
		CompactDef_EP.numIntersection=intersects.length;
		CompactDef_EP.numPaths=attackerPaths.size();
		
		CompactDef_EP.setAdversaryPaths(uniform, attackerPaths);
		CompactDef_EP.loadProblem();
		//CompactDef_EP.writeProb("def");
		CompactDef_EP.solve();
		//CompactDef_EP.writeSol("defsol");
		
		CompactAllocation da = CompactDef_EP.setTargetCoverage();
		boolean added = !lstda.contains(da);
		lstda.add(da);
		NodePair p = null;
	
		double DefValue = CompactDef_EP.getValue();
		
		strategy.addDefenderAllocation(da);
		
		
		CompactLP clp = new CompactLP(strategy);
		clp.loadProblem();
		//clp.writeProb("clp");
		clp.solve();
		//clp.writeSol("clpsol");
		strategy.setDefenderStrategy(clp.getDefenderStrategy());
		strategy.addAttackerStrategy(clp.getAdversaryStrategy());
		
		double CLPValue = clp.getLPObjective();
		int i=1;
		while((Math.abs(CLPValue - DefValue) > 0.01) && added){
			
			CompactDef_EP.setAdversaryPaths(strategy.getAttackerStrategy(), attackerPaths);
			CompactDef_EP.solve();
			da = CompactDef_EP.setTargetCoverage();
			DefValue = CompactDef_EP.getValue();
			
			if(!lstda.contains(da) && DefValue >= CLPValue){ 
				added = true;
			}else{
				added = false;
			};
			lstda.add(da);

			strategy.addDefenderAllocation(da);
			
			clp.generateData();
			clp.writeProb("clp"+i);
			clp.solve();
			clp.writeSol("clpsol"+i);
			
			strategy.setDefenderStrategy(clp.getDefenderStrategy());
			strategy.addAttackerStrategy(clp.getAdversaryStrategy());
			
			CLPValue = clp.getLPObjective();

		}
		
		
		double tock = System.currentTimeMillis();
		runtime = (tock - tick)/1000;
		//resources = CompactDef_EP.getAllocation();
		//System.out.print(resources);
		return CLPValue;
	}
	
	
	/**
	 * Creates the CompactGraph from the attacker paths that go through the mincut of each target
	 * @return
	 */
	
	boolean inputPaths = false;
	
	public void inputPaths( List<AdversaryPath> lstAP){
		inputPaths = true;
		for(AdversaryPath ap : lstAP){
			CompactPath cp = new CompactPath(ap, teamProb.getGraph());
			attackerPaths.add(cp);
		}
		
	}
	public AbstractBaseGraph<INode, ExtDWE> getCompactGraph(){
		
		//System.out.println("getting compact graph");
		
		if(CompactGraph==null || inputPaths){
			CompactGraph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
			if(!inputPaths || attackerPaths.size()<2 ) { getMinCutPaths(); }
			
			//first clean up the target graph so that resources cannot use virtual edges
			this.graph = removeVirtualEdges(this.graph); 
			
			
			//System.out.println("computing distances");
			//compute the distances between each paths
			computeDistances();
			
			
			Node[] targets = new Node[distances[0].length];
			List<ExtDWE> edges = new ArrayList<ExtDWE>();
			
			for(int i=0;i<distances[0].length;i++){
				
				Node target1 = (Node) attackerPaths.get(i).target;
				Node n1 = new Node(NODE_TYPE.TARGET, target1.payoff);

				CompactGraph.addVertex(n1);
				
				attackerPaths.get(i).target = n1;
				targetMap.put(n1, target1);
				targets[i]=n1;
			}
			
			
			
			for(int i=0;i<targets.length-1;i++){
				for(int j=i+1;j<targets.length;j++){
					
					Node n1 = targets[i];
					Node n2 = targets[j];
					
					if(distances[i][j]<=maxcoverage && distances[i][j]>0){
						ExtDWE e1 = CompactGraph.addEdge(n1, n2);
						CompactGraph.setEdgeWeight(e1, distances[i][j]);
						edges.add(e1);
					}
					if(distances[j][i]<=maxcoverage && distances[j][i]>0){
						ExtDWE e2 = CompactGraph.addEdge(n2, n1);
						CompactGraph.setEdgeWeight(e2, distances[j][i]);
						edges.add(e2);
					}
					
				}
			}

		}
		//System.out.println("num-paths"+this.attackerPaths.size());
		return CompactGraph;
	}

	/**Computer K shortest attacker paths to each target 
	public Set<ExtDWE> getKAttackerPaths(){
		
		
		int pathsPerTarget = numPaths/this.teamProb.getTargetNodesSet().size();
		if (pathsPerTarget <1){ pathsPerTarget = 1;}
		
		KShortestPaths<INode,ExtDWE> kPaths = new KShortestPaths<INode,ExtDWE>(graph, teamProb.getSource(),  pathsPerTarget);
		

		// DijkstraShortestPath<INode, ExtDWE>
		Set<ExtDWE> edgeSet = new HashSet<ExtDWE>();

		for (INode target : this.teamProb.getTargetNodesSet()) {
			List<GraphPath<INode, ExtDWE>> lstPaths = kPaths.getPaths(target);
			
			for (GraphPath<INode, ExtDWE> gp : lstPaths) {
				edgeSet.addAll(gp.getEdgeList());
				CompactPath ap = new CompactPath(gp);
				attackerPaths.add(ap);
			}
		}
		return edgeSet;
	}
	*/
	
	Set<ExtDWE> minCutEdges;
	
	/**Computing shortest attacker paths that go through each edge in the mincut 
	 * for each target*/
	public void getMinCutPaths(){
		
		Set<ExtDWE> minCut;
		
		//get mincut for each individual target
		for (INode targetNode : this.teamProb.getTargetNodesSet()) {
			minCut = computeSingleMinCut(targetNode);
			
			//for each edge in the mincut get shortest path through mincut
			for(ExtDWE edge : minCut){
				INode s = edge.getsource();
				INode t = edge.gettarget();
				List<ExtDWE> lstPaths1 = new ArrayList<ExtDWE>();
				
				if(!s.equals( teamProb.getSource())){
					BellmanFordShortestPath shortestPath1 = new BellmanFordShortestPath(graph, teamProb.getSource());
					lstPaths1 = shortestPath1.getPathEdgeList(s);
				}
				
				lstPaths1.add(edge);
				
				if(!t.equals(targetNode)){
					BellmanFordShortestPath shortestPath2 = new BellmanFordShortestPath(graph, t);
					List<ExtDWE> lstPaths2 = shortestPath2.getPathEdgeList(targetNode);
					lstPaths1.addAll(lstPaths2);	
				}
				
				
				CompactPath ap = new CompactPath(lstPaths1, targetNode, graph);
				boolean contains = false;
				for(CompactPath p : attackerPaths){
					if(p.equals(ap)){
						contains = true;
						break;
					}
				}
				
				if(!contains){
					attackerPaths.add(ap);
				}
			}
			//minCutEdges.addAll(minCut);
			minCut.clear();
				
				
		}
			
		
	}
	

	private Set<ExtDWE> computeSingleMinCut(INode targetNode) {
		MinCut minCutObj = new MinCut(this.teamProb);

		minCutObj.setTarget(targetNode);

		minCutObj.resetLP();

		Set<ExtDWE> minCutSet = minCutObj.getMinCut();
		return minCutSet;
		
		
	}
	
	public boolean distancesComputed = false;
	
	/**Methods for computing path distances from each other */
	public void computeDistances(){
		
		distancesComputed=true;
		
		distances = new int[attackerPaths.size()][attackerPaths.size()];
		intersects = new NodePair[attackerPaths.size()][attackerPaths.size()];
		
		//for each attacker path
		for(int i=0;i<attackerPaths.size()-1;i++){
			CompactPath p1 = attackerPaths.get(i);	//create a compact path
					
			//create the self intersection (only covering that path) 
			NodePair self = new NodePair();
			self.nodeset[0]=p1.target;
			
			self.pathset[0]= p1;
			self.pathset[1]= p1;
			
			self.distance=1;
			self.self = true;
			
			distances[i][i]=1;
			intersects[i][i]=self;
			
			if(i==attackerPaths.size()-2){
				NodePair self2 = new NodePair();
				self2.pathset[0]= p1;
				self2.pathset[1]= p1;
				
				self2.distance=1;
				self2.self = true;
				distances[i+1][i+1]=1;
				intersects[i+1][i+1]=self;
			}
			
			//for each unseen attacker path
			for(int j=i+1;j<attackerPaths.size();j++){
				CompactPath p2 = attackerPaths.get(j);
				NodePair p = new NodePair();
				p.pathset[0]=p1;
				p.pathset[1]=p2;
				
				//if the paths share an edge
				if(shareEdge(p1,p2,p)||shareEdge(p2,p1,p)){
					distances[i][j] = 1; //it only costs 1 edge to cover
				
				//if the paths share a vertex
				}else if(shareVertex(p1,p2,p)||shareVertex(p2,p1,p)){
					distances[i][j] = 2; //takes at least 2 resources to cover
				}else{
					distances[i][j] = pathDistance(p1,p2,p)+2; //costs the distance plus one edge for each path
					
					if(distances[i][j]<0){ distances[i][j] = pathDistance(p2,p1,p)+2;}
				}
				
				p.setDistance(distances[i][j]);
				distances[j][i] = distances[i][j];
				
				intersects[i][j] = p;
				intersects[j][i] = p;

			}
		}
	}
	
	private boolean shareVertex(CompactPath p1, CompactPath p2, NodePair p){
		List<ExtDWE> edges= new ArrayList<ExtDWE>();
		
		for(INode v : p1.nodes){
			if(p2.nodes.contains(v) && v!=null){
				
				for( ExtDWE e :this.graph.edgesOf(v)){
					if(p1.edges.contains(e)){
						p.danglingedges.add(e);
						edges.add(e);
					
					}else if( p2.edges.contains(e)){
						p.danglingedges.add(e);
						edges.add(e);
					}
				}
				
				p.danglingnodes.put(v, edges);
				p.listDanglingNodes();
				p.nodeset[0]=(v);
				p.nodeset[1]=(v);
				
				return true;
			}
		}
		
		return false;
	}
	
	private boolean shareEdge(CompactPath p1, CompactPath p2, NodePair p){
		
		for(ExtDWE e : p1.edges){
			if(p2.edges.contains(e)){
				
				p.nodeset[0]=(e.getsource());
				p.nodeset[1]=(e.gettarget());
				
				p.edgeset.add(e);
				//p.danglingedges.put(e, null);
				p.shareEdge=true;
				p.listDanglingNodes();
				return true;
			}
		}
		
		return false;
	}
	
	private int pathDistance(CompactPath p1, CompactPath p2, NodePair p){
		
		int minPathDistance = Integer.MAX_VALUE;
		
		for(INode n1 : p1.nodes){
			BellmanFordShortestPath shortestPath = new BellmanFordShortestPath(graph, n1 );
			
			for(INode n2 : p2.nodes){
				List path = shortestPath.getPathEdgeList(n2);
				
				if(path!=null){
					int dist = path.size();
					if(dist<minPathDistance){
						
						p.nodeset[0]=(n1);
						p.nodeset[1]=(n2);
						
						p.edgeset=path;
						
						
						minPathDistance = dist;
					}
				}
			}
			
		}
		
		List<ExtDWE> edges1= new ArrayList<ExtDWE>();
		//for( ExtDWE e :this.graph.edgesOf(p.nodeset[0])){
		for( ExtDWE e :this.graph.edgesOf(p.nodeset[0])){
			if(p1.edges.contains(e)){
				p.danglingedges.add(e);
				edges1.add(e);
			}
		}
		p.danglingnodes.put(p.nodeset[0],edges1);
		
		List<ExtDWE> edges2= new ArrayList<ExtDWE>();
		for( ExtDWE e :this.graph.edgesOf(p.nodeset[1])){
			if(p2.edges.contains(e)){
				p.danglingedges.add(e);
				edges2.add(e);
			}
		}
		p.danglingnodes.put(p.nodeset[1],edges2);
		
		p.listDanglingNodes();
		return minPathDistance;
		
	}
	

	public List<AdversaryPath> getAdversaryPaths(){
		List<AdversaryPath> lstPaths = new ArrayList<AdversaryPath>();
		
		for(CompactPath cp : attackerPaths){
			lstPaths.add(cp.toAP());
		}
		return lstPaths;
	}
	
	
	
}
