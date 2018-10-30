package teamBuildingSolvers.AttackerOracle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lpWrapper.LPSolverException;
import lpWrapper.Configuration;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.teamBuildingModels.TeamBuildingProblem;
import model.teamBuildingModels.AdversaryPath;

public class BRAdversary {
	/**
	 * loadProblem() -> setDefenderStrategy(); -> solve();
	 */
	private List<AttackerOracleFixedTarget> listBRAdvFT;
	private TeamBuildingProblem usProblem;
	private double curObjective;
	private int brAdvOptimalFTindex;
	public double[] brAdvFTargetPayoffs;
	
	public BRAdversary(TeamBuildingProblem usProblem2) throws MalformedGraphException {
		super();
		this.curObjective = -Configuration.MM;
		this.brAdvOptimalFTindex = -1;
		this.usProblem = usProblem2;
		List<INode> targetNodesSet = this.usProblem.getTargetNodesSet();
		this.listBRAdvFT = new ArrayList<AttackerOracleFixedTarget>(targetNodesSet.size());
		INode source = this.usProblem.getSource();
		for ( int t=0; t<targetNodesSet.size(); t++){
			this.listBRAdvFT.add(new AttackerOracleFixedTarget(this.usProblem, source, targetNodesSet.get(t)));
		}
		brAdvFTargetPayoffs = new double[listBRAdvFT.size()];
	}
	public void loadProblem() {
		for ( Iterator<AttackerOracleFixedTarget> brAdvFTiter = listBRAdvFT.iterator(); brAdvFTiter.hasNext(); ) {
			AttackerOracleFixedTarget brAdvFTcurTarget = brAdvFTiter.next();
			brAdvFTcurTarget.loadProblem();
		}
	}
	public void setDefenderStrategy (List<Double> defenderStrategy) {
		for ( Iterator<AttackerOracleFixedTarget> brAdvFTiter = listBRAdvFT.iterator(); brAdvFTiter.hasNext(); ) {
			AttackerOracleFixedTarget brAdvFTcurTarget = brAdvFTiter.next();
			brAdvFTcurTarget.setDefenderStrategy(defenderStrategy);
		}		
	}

	public void resetLP() {
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.resetLP();
		}
	}
	
	public void solve() throws LPSolverException {
		this.curObjective = -Configuration.MM;
		boolean atLeastOneSolnFound = false;
		this.brAdvOptimalFTindex = -1;
		
		//int curIndex = 0;
		for (int index = 0;index<listBRAdvFT.size();index++){
			AttackerOracleFixedTarget brAdvFTcurTarget = listBRAdvFT.get(index);
			
			brAdvFTcurTarget.oracle=false;
			// brAdvFTcurTarget.writeProb("BRADVWRITE");
			//brAdvFTcurTarget.writeProb("adv");
			brAdvFTcurTarget.solve();
			double brAdvFTcurTargetAdvPayoff = brAdvFTcurTarget.getAdversaryPayoff();	
			brAdvFTargetPayoffs[index] = brAdvFTcurTargetAdvPayoff;
			
			if ( atLeastOneSolnFound == false || brAdvFTcurTargetAdvPayoff > this.curObjective) {
				this.curObjective = brAdvFTcurTargetAdvPayoff;
				this.brAdvOptimalFTindex  = index;
				atLeastOneSolnFound = true;
			}
			//curIndex++;
		}
		if ( this.brAdvOptimalFTindex == -1) {
			throw new LPSolverException();
		}
	}
	
	public int maxIndex(){
		double max = 0;
		int index=-1;
		for(int i=0;i<brAdvFTargetPayoffs.length;i++){
			if(brAdvFTargetPayoffs[i]>max){
				max=brAdvFTargetPayoffs[i];
				index=i;
			}
		}
		return index;
	}
	public void OptimalSolve2(double GV) throws LPSolverException{
		boolean atLeastOneSolnFound = false;
		this.brAdvOptimalFTindex = -1;
		
		//int curIndex = 0;
		for (int index = 0;index<listBRAdvFT.size();index++){
			AttackerOracleFixedTarget brAdvFTcurTarget = listBRAdvFT.get(index);
			
			brAdvFTcurTarget.oracle=true;
			// brAdvFTcurTarget.writeProb("BRADVWRITE");
			//brAdvFTcurTarget.writeProb("adv");
			brAdvFTcurTarget.solve();
			double brAdvFTcurTargetAdvPayoff = brAdvFTcurTarget.getAdversaryPayoff();	
			brAdvFTargetPayoffs[index] = brAdvFTcurTargetAdvPayoff;
			
			if ( atLeastOneSolnFound == false || brAdvFTcurTargetAdvPayoff > this.curObjective) {
				this.curObjective = brAdvFTcurTargetAdvPayoff;
				this.brAdvOptimalFTindex  = index;
				atLeastOneSolnFound = true;
				
				if(brAdvFTcurTargetAdvPayoff>=GV){
					break;
				}
			}
			
			//curIndex++;
		}
		if ( this.brAdvOptimalFTindex == -1) {
			throw new LPSolverException();
		}
	}
	public void OptimalSolve() throws LPSolverException{
		this.curObjective = -Configuration.MM;
		boolean atLeastOneSolnFound = false;
		this.brAdvOptimalFTindex = -1;
		
		//int curIndex = 0;
		for (int index = 0;index<listBRAdvFT.size();index++){
			AttackerOracleFixedTarget brAdvFTcurTarget = listBRAdvFT.get(index);
			
			brAdvFTcurTarget.oracle=true;
			// brAdvFTcurTarget.writeProb("BRADVWRITE");
			//brAdvFTcurTarget.writeProb("adv");
			brAdvFTcurTarget.solve();
			double brAdvFTcurTargetAdvPayoff = brAdvFTcurTarget.getAdversaryPayoff();	
			brAdvFTargetPayoffs[index] = brAdvFTcurTargetAdvPayoff;
			
			if ( atLeastOneSolnFound == false || brAdvFTcurTargetAdvPayoff > this.curObjective) {
				this.curObjective = brAdvFTcurTargetAdvPayoff;
				this.brAdvOptimalFTindex  = index;
				atLeastOneSolnFound = true;
			}
			//curIndex++;
		}
		if ( this.brAdvOptimalFTindex == -1) {
			throw new LPSolverException();
		}
	}
	/**
	 * Defender Allocations already in the usProblem
	 * Note: call resetLP() if defender allocations have been deleted.
	 * call setDefenderStrategy() after resetLP().
	 */
	public void updateDefenderAllocations() {
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.updateDefenderAllocations();
		}
	}
	public double getAdversaryPayoff() {
		return this.curObjective; //this.listBRAdvFT.get(this.brAdvFTindex).getAdversaryPayoff();
	}
	/** 
	 * gets the current best response adversary path
	 * @return
	 */
	public List<AdversaryPath> getAdversaryPaths(){
		List<AdversaryPath> listAP = new ArrayList<AdversaryPath>();
		
		for(int i=0;i<brAdvFTargetPayoffs.length;i++){
			if(brAdvFTargetPayoffs[i] >= this.curObjective){
				listAP.add(this.listBRAdvFT.get(i).getAdversaryPath());
			}
		}
		
		return listAP;
	}
	public AdversaryPath getAdversaryPath() throws LPSolverException {
		// can get adversary path set
		if ( this.brAdvOptimalFTindex == -1 ) {
			throw new LPSolverException();
		}
		return this.listBRAdvFT.get(this.brAdvOptimalFTindex).getAdversaryPath();
	}		
	/**
	 * this gets the adversary paths for all the different targets
	 * @return
	 */
	public List<AdversaryPath> getAllTargetAdversaryPaths() {
		List<AdversaryPath> apList = new ArrayList<AdversaryPath>(listBRAdvFT.size());
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			apList.add(brAdvFTcurTarget.getAdversaryPath());
		}
		return apList;
	}
	/**
	 * Function exists only for debugging 
	 */
	public List<Double> getAllTargetAdversaryPayoffs() {
		List<Double> advPayoffs = new ArrayList<Double>(listBRAdvFT.size());
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			advPayoffs.add(brAdvFTcurTarget.getAdversaryPayoff());
		}
		return advPayoffs;
	}
	
	public void writeProb(String fileName) {
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.writeProb(fileName + "T"+brAdvFTcurTarget.getTarget().getId());
		}
	}
	public void writeSol(String fileName) {
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.writeSol(fileName + "T"+brAdvFTcurTarget.getTarget().getId());
		}
	}
	public long getLoadTime() {
		long loadTime = 0;
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			loadTime += brAdvFTcurTarget.getLoadTime();
		}
		return loadTime;
	}
	public long getRunTime() {
		long runTime = 0;
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			runTime += brAdvFTcurTarget.getRunTime();
		}
		return runTime;		
	}
	public void end() {
		for ( AttackerOracleFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.end();
		}
		// TODO Auto-generated method stub		
	}
	
}
