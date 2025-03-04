package teamBuildingSolvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osm.data.projection.Epsg4326;

import lpWrapper.Configuration;
import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.Measure;

public class RuggedCutoff {
	private boolean cutoffUsed = true;
	protected TeamBuildingProblem usProblem;
	protected CoreLP clp;
	protected BRDefender brDef;
	protected BRAdversary brAdv;
	protected double gameValue = -Double.MAX_VALUE;
	protected int cutoff;
	protected int iterationNo;
	protected long loadTime;
	protected long runTime;
	protected long totalBRDefTime, totalBRAdvTime, totalCLTime;
	protected double bestDefPayoffFound;
	protected double bestAdvPayoffFound;

	protected Measure measure = new  Measure();

	public RuggedCutoff(TeamBuildingProblem usProblem, int cutoff) {
		super();
		this.usProblem = usProblem;
		clp = new CoreLP(usProblem);
		brDef = new BRDefender(usProblem);
		brAdv = new BRAdversary(usProblem);
		iterationNo = 0;
		totalBRDefTime = 0;
		totalBRAdvTime = 0;
		totalCLTime = 0;
		this.cutoff = cutoff;
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
		//System.out.println("Wrapper started. Please hold, this may take a while...");
		//System.out.println("RUGGED resources: "+ this.usProblem.getNumDefenderResources());
		//System.out.println("RUGGED resource coverage" + this.usProblem.ResourceCoverage);
		totalBRDefTime = 0;
		totalBRAdvTime = 0;
		totalCLTime = 0;

		long curTime = System.currentTimeMillis();
		this.loadBestResponseProblems();
		this.loadTime = System.currentTimeMillis() - curTime;
		measure.loadTime = loadTime;
		curTime = System.currentTimeMillis();
		double gameValueCheck ;

		//this.brDef.writeProb("defOracle" + iterationNo);
		this.brDef.solve();
		//this.brDef.writeProb("defOracle" + iterationNo+"2");
		
		totalBRDefTime += this.brDef.getRunTime();
		DefenderAllocation da = this.brDef.getDefenderAllocation();
		boolean allocationAdded = this.usProblem.getActProfile().addDefenderAllocation(da);	// allocationAdded = false if da already generated before
		this.brAdv.updateDefenderAllocations();		
		this.brAdv.solve();
		totalBRAdvTime += this.brAdv.getRunTime();
		AdversaryPath ap = this.brAdv.getAdversaryPath();
		boolean pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded = false if ap already generated before
		this.brDef.updateAdversaryPaths();

		this.loadCLProblem();
		this.loadTime += this.clp.getLoadTime();

		this.clp.solve();
		totalCLTime += this.clp.getRunTime();
		this.brAdv.setDefenderStrategy(this.clp.getDefenderStrategy());
		this.brDef.setAdversaryStrategy(this.clp.getAdversaryStrategy());

//		System.out.println("DA: "+ da);
//		System.out.println("AP: "+ ap);
//		System.out.println("Defender Strategy: " + this.clp.getDefenderStrategy());
//		System.out.println("Attacker Strategy: " + this.clp.getAdversaryStrategy());

		while ( true ) {
			iterationNo ++;		
			
			gameValueCheck = this.gameValue;
			//System.out.println("ITERATION : "+ iterationNo);
			

			this.brDef.solve();
			//this.brDef.writeProb("defOracle" + iterationNo);
			totalBRDefTime += this.brDef.getRunTime();
			double defenderPayoff = this.brDef.getDefenderPayoff();

			if ( bestDefPayoffFound > defenderPayoff) {
				bestDefPayoffFound = defenderPayoff;
			}

			// get solution
			da = this.brDef.getDefenderAllocation();
//			System.out.println("DA: "+ da);
			
			allocationAdded = this.usProblem.getActProfile().addDefenderAllocation(da);	// allocationAdded = false if da already generated before
			//System.out.print(allocationAdded+",");
			measure.defenderOracleTimes.add(this.brDef.getRunTime());
			measure.defenderBRVal.add(defenderPayoff);
			this.clp.updateDefenderAllocations();
			this.brAdv.updateDefenderAllocations();

			// update problem data structures
			// update and solve Core-LP model
			// avoids repetition of the same allocation
			// avoids repetition of the same path 
			//boolean pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded = false if ap already generated before			


			this.clp.solve();
			this.gameValue = this.clp.getLPObjective();
			totalCLTime += this.clp.getRunTime();
			measure.coreLPtimes.add(this.clp.getRunTime());
			measure.coreLPGV.add(gameValue);
			measure.adversarySuppSetSize.add(getNonZeros(this.clp.getAdversaryStrategy()));
			measure.defenderSuppSetSize.add(getNonZeros(this.clp.getDefenderStrategy()));
			measure.adversaryStrategiesSize.add(this.usProblem.getActProfile().getNumberOfAdversaryPaths());
			measure.defenderStrategiesSize.add(this.usProblem.getActProfile().getNumberOfDefenderAllocations());

			// update BRAdv
			List<Double> defStrategy = this.clp.getDefenderStrategy();
			this.brAdv.setDefenderStrategy(defStrategy);	
			
//			System.out.println("Defender Strategy: " + this.clp.getDefenderStrategy());
//			System.out.println("Attacker Strategy: " + this.clp.getAdversaryStrategy());
			
			// solve adversary problem
			this.brAdv.solve();
			
			//this.brAdv.writeProb("attOracle" + iterationNo);
			totalBRAdvTime += this.brAdv.getRunTime();
			double adversaryPayoff = this.brAdv.getAdversaryPayoff();
			if ( bestAdvPayoffFound > adversaryPayoff ) {
				bestAdvPayoffFound = adversaryPayoff;
			}
			
			if ( iterationNo % 500 == 0 ) {
				//System.out.println("Iteration No: " + iterationNo + "GV/BRDef/BRAdv: " + this.gameValue + "/" + defenderPayoff + "/" + adversaryPayoff);
			}

			//System.out.println("Iteration Details-Iter: " + iterationNo + ", Def Payoff: " + defenderPayoff + ", Neg Adv. Payoff: " + (-1*adversaryPayoff) + ", Clp Payoff: " + gameValue);
			//System.out.println("Best Details-Iter: " + iterationNo + ", Best Def Payoff: " + bestDefPayoffFound + ", Neg Best Adv. Payoff: " + (-1*bestAdvPayoffFound));			
			
			double diffPercentage = Math.abs(gameValueCheck/100/10); // 0.1%
			if((Math.abs(defenderPayoff-gameValueCheck) <= diffPercentage && Math.abs(-adversaryPayoff-gameValueCheck) <= diffPercentage) || (allocationAdded == false && pathAdded == false)){
				long stopTime = System.currentTimeMillis();
				measure.totalTime = stopTime - curTime;
				measure.iterations = iterationNo;
				measure.isOK();
				//System.out.println(iterationNo);
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
			
			
			
			// get solution
			if(iterationNo <= cutoff){
			ap = this.brAdv.getAdversaryPath();
//			System.out.println("AP: "+ ap);
			}else{
				cutoffUsed = true;
			}
			pathAdded = this.usProblem.getActProfile().addAdversaryPath(ap); //pathAdded = false if ap already generated before
			//System.out.println(pathAdded);
			
			if(true){	
				System.out.println("AP: "+ap);	
				if(pathAdded){
					System.out.println("addedAP: "+ap);
				}
			}
			System.out.println("Def-payoff: " + defenderPayoff);
			System.out.println("Ad-payoff: " + adversaryPayoff);
			
			measure.adversaryOracleTimes.add(this.brAdv.getRunTime());
			measure.adversaryBRVal.add(this.brAdv.getAdversaryPayoff());
			
			this.clp.updateAdversaryPaths();
			this.brDef.updateAdversaryPaths();
			

			this.clp.solve();
			totalCLTime += this.clp.getRunTime();
			this.gameValue = this.clp.getLPObjective();
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

			//			System.out.println("Defender Strategy: " + this.clp.getDefenderStrategy());
//			System.out.println("Attacker Strategy: " + this.clp.getAdversaryStrategy());			
			//}
			System.gc();
			
			boolean ok = measure.isOK();
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
	
	public void changeCutoff(int newcutoff){
		cutoff = newcutoff;
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
		//System.out.println("Time Breakup: " + this.totalCLTime/1000.0 + ", " + this.totalBRDefTime/1000.0 + ", " + this.totalBRAdvTime/1000.0);
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
	public int getCutoff() {

		return this.cutoff;
	}
	public boolean getcutoffUsed() {
		return cutoffUsed;
	}



}
