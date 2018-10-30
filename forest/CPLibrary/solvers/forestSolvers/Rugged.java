package teamBuildingSolvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import teamBuildingSolvers.AttackerOracle.BRAdversary;
import teamBuildingSolvers.DefenderOracle.DefenderOracle;
import lpWrapper.Configuration;
import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.Measure;

public class Rugged {
	public TeamBuildingProblem usProblem;
	protected CoreLP clp;
	protected DefenderOracle brDef;
	protected BRAdversary brAdv;
	protected double gameValue = -Double.MAX_VALUE;
	protected int iterationNo;
	protected long loadTime;
	protected long runTime;
	protected long totalBRDefTime, totalBRAdvTime, totalCLTime;
	protected double bestDefPayoffFound;
	protected double bestAdvPayoffFound;

	//set to false to only print solution
	//set to true to print game value, allocations and utilities
	public boolean printInfo = false;
	
	//set to true to print out LP problem and solution
	public boolean print_DProb = false;
	
	protected Measure measure = new  Measure();

	public Rugged(TeamBuildingProblem usProblem) {
		super();
		this.usProblem = usProblem;
		clp = new CoreLP(usProblem);
		brDef = new DefenderOracle(usProblem);
		brAdv = new BRAdversary(usProblem);
		iterationNo = 0;
		totalBRDefTime = 0;
		totalBRAdvTime = 0;
		totalCLTime = 0;
		bestDefPayoffFound = Configuration.MM;
		bestAdvPayoffFound = Configuration.MM;
	}
	protected void loadCLProblem() {
		clp.loadProblem();
	}

	protected void loadBestResponseProblems() {
		brDef.loadProblem();
		brAdv.loadProblem();
	}

	public void run() throws LPSolverException, MalformedGraphException {
		totalBRDefTime = 0;
		totalBRAdvTime = 0;
		totalCLTime = 0;

		long curTime = System.currentTimeMillis();
		this.loadBestResponseProblems();
		this.loadTime = System.currentTimeMillis() - curTime;
		measure.loadTime = loadTime;
		curTime = System.currentTimeMillis();
		double gameValueCheck;
		
		//Initial DEFENDER allocation
		//this.brDef.writeProb("def");
		this.brDef.solve();
		totalBRDefTime += this.brDef.getRunTime();
		
		DefenderAllocation da = this.brDef.getDefenderAllocation();
		boolean allocationAdded = this.usProblem.getActProfile().addDefenderAllocation(da);	// allocationAdded = false if da already generated before
		
		//Initial ATTACKER response
		this.brAdv.updateDefenderAllocations();
		//this.brAdv.writeProb("adv");
		this.brAdv.solve();

		totalBRAdvTime += this.brAdv.getRunTime();
		AdversaryPath ap = this.brAdv.getAdversaryPath();
		boolean pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded = false if ap already generated before
		this.brDef.updateAdversaryPaths();

		//LOAD LP
		this.loadCLProblem();
		this.loadTime += this.clp.getLoadTime();
		this.clp.writeProb("clp");
		this.clp.solve();
		this.clp.writeSol("clpsol");
		totalCLTime += this.clp.getRunTime();
		this.brAdv.setDefenderStrategy(this.clp.getDefenderStrategy());
		this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());
		
		
		if(printInfo){
			System.out.println("DA: "+ da);
			System.out.println("AP: "+ ap);
			System.out.println("Defender Strategy: " + this.clp.getDefenderStrategy());
			System.out.println("Attacker Strategy: " + this.clp.getAdversaryStrategy());
		}
		
		//is set to true if all defender strategies end up enumerated
		boolean enumeratedAll = false;
		
		double defenderPayoff = this.gameValue;
		double adversaryPayoff = this.gameValue;
		
		
		while ( true ) {
			iterationNo ++;		
			
			gameValueCheck = this.gameValue;

			//DEFENDER ORACLE
			this.brDef.solve();
			totalBRDefTime += this.brDef.getRunTime();
			
			
			// get solution
			da = this.brDef.getDefenderAllocation();
			allocationAdded = this.usProblem.getActProfile().addDefenderAllocation(da);	// allocationAdded = false if da already generated before
			
			if(allocationAdded){
				defenderPayoff = this.brDef.getDefenderPayoff();
//				if (allocationAdded && defenderPayoff <= this.gameValue) {
//					allocationAdded = false;
//				}
			}
			
			if(printInfo && allocationAdded){
				System.out.println("DA: "+this.usProblem.getActProfile().getDefenderAllocations());
			}
			
			//if allocation is not added we have already generated this strategy
			//if we are using probabilities, we need to generate a new one
			//also we check to make sure we havent already enumerated all of them
			
			
			//get defender payoff for this strategy
			//defenderPayoff = this.brDef.getrealDefenderPayoff(da);
			//double defenderPayoff2 = this.brDef.getDefenderPayoff();
			
			
				System.out.println("Def-payoff: " + defenderPayoff);
			
			
			if ( bestDefPayoffFound > defenderPayoff) {
				bestDefPayoffFound = defenderPayoff;
			}
			
			if(allocationAdded){
			measure.defenderOracleTimes.add((long) this.brDef.getRunTime());
			measure.defenderBRVal.add(defenderPayoff);
			this.clp.updateDefenderAllocations();
			this.brAdv.updateDefenderAllocations();
			}
			
			// update problem data structures
			// update and solve Core-LP model
			// avoids repetition of the same allocation
			// avoids repetition of the same path 
			//boolean pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded = false if ap already generated before			


			this.clp.solve();
			this.clp.writeProb("clp");
			this.clp.writeSol("clpsol");
			this.gameValue = this.clp.getLPObjective();
			totalCLTime += this.clp.getRunTime();
			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile().getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile().getNumberOfDefenderAllocations());
			
			
			//System.out.println("Defender Strategy: " + this.clp.getDefenderStrategy());
			//System.out.println("Attacker Strategy: " + this.clp.getAdversaryStrategy());
			
			
			// update BRAdv
			List<Double> defStrategy = this.clp.getDefenderStrategy();
			this.brAdv.setDefenderStrategy(defStrategy);	
			

			// solve adversary problem
			this.brAdv.solve();
			totalBRAdvTime += this.brAdv.getRunTime();
			adversaryPayoff = this.brAdv.getAdversaryPayoff();
			
			System.out.println("Adv-payoff: " + adversaryPayoff);
			
			if ( bestAdvPayoffFound > adversaryPayoff ) {
				bestAdvPayoffFound = adversaryPayoff;
			}

			if ( iterationNo % 500 == 0 ) {
				System.out.println("Iteration No: " + iterationNo + "GV/BRDef/BRAdv: " + this.gameValue + "/" + defenderPayoff + "/" + adversaryPayoff);
			}
			

			// get solution
			ap = this.brAdv.getAdversaryPath();
			pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded = false if ap already generated before
			
			if(printInfo){	
				System.out.println("AP: "+ap);	
				if(pathAdded){
					System.out.println("addedAP: "+ap);
				}
			}
			
			measure.adversaryOracleTimes.add(this.brAdv.getRunTime());
			measure.adversaryBRVal.add(this.brAdv.getAdversaryPayoff());

			this.clp.updateAdversaryPaths();
			this.brDef.updateAdversaryPaths();


			this.clp.solve();
			totalCLTime += this.clp.getRunTime();
			this.gameValue = this.clp.getLPObjective();
			System.out.println("GV: "+this.gameValue);
			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile().getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile().getNumberOfDefenderAllocations());

			// update BRDef
			List<Double> advStrategy = this.clp.getAdversaryStrategy();
			//this.brDef.updateAdversaryPaths(); 
			this.brDef.setAdversaryStrategy(advStrategy);	
			
			//System.out.println("Defender Strategy: " + this.clp.getDefenderStrategy());
			//System.out.println("Attacker Strategy: " + this.clp.getAdversaryStrategy());			

			//System.out.println("Iteration Details-Iter: " + iterationNo + ", Def Payoff: " + defenderPayoff + ", Neg Adv. Payoff: " + (-1*adversaryPayoff) + ", Clp Payoff: " + gameValue);
			//System.out.println("Best Details-Iter: " + iterationNo + ", Best Def Payoff: " + bestDefPayoffFound + ", Neg Best Adv. Payoff: " + (-1*bestAdvPayoffFound));			
			double diffPercentage = Math.abs(gameValueCheck/100/10); // 0.1%
			
			if((Math.abs(defenderPayoff-gameValueCheck) <= diffPercentage && Math.abs(-adversaryPayoff-gameValueCheck) <= diffPercentage) ||
					(allocationAdded == false && pathAdded == false)){
				long stopTime = System.currentTimeMillis();
				measure.totalTime = stopTime - curTime;
				measure.iterations = iterationNo;
				//measure.isOK();
				//if(allocationAdded == false && pathAdded == false){
				//System.out.flush();
				//System.err.println("Main condition reached.");
				//System.err.flush();
				// Convergence obtained
				//								System.out.println("Covergence stop results>>>");
				//								System.out.println("Adversary path: "+ ap +", target: "+ ap.getTarget());
				//								System.out.println("Adversary strategy set: "+ usProblem.getActProfile().getAdversaryPaths());
				//								System.out.println("Defender alloc: "+ da);
				//								System.out.println("Adversary payoff: "+ brAdv.getAdversaryPayoff());
				//								System.out.println("Defender payoff: "+ brDef.getDefenderPayoff());
				//								System.out.println("Defender strat set: "+ usProblem.getActProfile().getDefenderAllocations());
				//								System.out.println("Def probs: "+ clp.getDefenderStrategy());
				//								System.out.println("Adv probs: "+ clp.getAdversaryStrategy());
				//System.out.println("WRAPPER Game value: "+ clp.getLPObjective());
				//measure.write();
				//				System.err.println("DIFF: "+diffPercentage); // 0.1%
				//				System.err.println("One cond: "+Math.abs(defenderPayoff-gameValueCheck));
				//				System.err.println("Second cond: "+Math.abs(-adversaryPayoff-gameValueCheck));

				break;				
			}
			
			System.gc();
			
			boolean ok = true;
			measure.isOK();
			
			if(!ok){
				measure.write();
				System.err.println(" ===== MEASURE NOT OK !!!!! ===================");
				System.exit(1);
			}


			/**
			 * Read Instructions on resetLP if defender allocations / adversary paths need to be removed.
			 * Maintain the order of the calls -- the set of commands for addDefenderAllocations are called together, 
			 * and same for addAdversaryPaths. Their order can be interchanged but not interleaved. 
			 */
		}

		this.runTime = System.currentTimeMillis() - curTime;
	}
	
	public int getNumberOfEdgesCoveredByDefender() {
		Map<ExtDWE, Double> defCov = this.clp.getDefenderCoverage();
		int count = 0;
		for ( ExtDWE e : defCov.keySet() ) {
			count += ((defCov.get(e) > Configuration.EPSILON) ? 1 : 0);
		}
		return count;
	}

	public void end(){
		clp.end();
		brAdv.end();
		brDef.end();
		//System.err.println("CPLEX ended, kid :)");
		totalBRDefTime = 0;
		totalBRAdvTime = 0;
		totalCLTime = 0;
		Node.reset();
		ExtDWE.reset();
	}

	public double getGameValue() {
		return this.gameValue;
	}
	public double getDefenderPayoff() {
		return this.brDef.getDefenderPayoff();
	}
	public double getAdversaryPayoff() {
		return this.brAdv.getAdversaryPayoff();
	}
	public List<Double> getDefenderStrategy() {
		return this.clp.getDefenderStrategy();
	}
	public List<Double> getAdversaryStrategy() {
		return this.clp.getAdversaryStrategy(); 
	}
	public List<DefenderAllocation> getDefenderAllocations() {
		return this.usProblem.getActProfile().getDefenderAllocations();
	}
	public List<AdversaryPath> getAdversaryPaths() {
		return this.usProblem.getActProfile().getAdversaryPaths();
	}
	
	public Double getAveragePathLength() {
		List<Integer> pathLengths = new ArrayList<Integer>();
		for ( AdversaryPath ap : this.usProblem.getActProfile().getAdversaryPaths() ) {
			pathLengths.add(ap.size());
		}
		return utilities.Utilities.getAverageInteger(pathLengths);
	}
	
	public int getNumberOfIterations() {
		return this.iterationNo;
	}
	public long getLoadTime() {
		return loadTime;
	}
	public long getRunTime() {
		return runTime;
	}
	public long getTotalTime() {
		return (loadTime + runTime);
	}

	/**
	 * To add defender or adversary strategies before the {@link Rugged#run()} method is executed.
	 * @param daList list of {@link DefenderAllocation}s.
	 * @param apList list of {@link AdversaryPath}s.
	 */
	public void warmStart(List<DefenderAllocation> daList, List<AdversaryPath> apList){
		if(daList!=null){
			for(DefenderAllocation da: daList){
				usProblem.getActProfile().addDefenderAllocation(da);
			}
		}

		if(apList!=null){
			for(AdversaryPath ap: apList){
				usProblem.getActProfile().addAdversaryPath(ap);
			}
		}
	}
	public void printRunTimeBreakup() {		
		System.out.println("Time Breakup: " + this.totalCLTime/1000.0 + ", " + this.totalBRDefTime/1000.0 + ", " + this.totalBRAdvTime/1000.0);
	}

	protected int getNonZeros(List<Double> list) {
		int nz = 0;
		for(double ps: list){
			if(ps>Configuration.EPSILON) nz+=1;
		}
		return nz;
	}

	public void writeMeasure(){
		measure.write();
	}
	/**
	 * @return the measure
	 */
	public Measure getMeasure() {
		return measure;
	}



}
