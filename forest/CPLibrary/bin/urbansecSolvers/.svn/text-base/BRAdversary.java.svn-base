package urbansecSolvers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lpWrapper.LPSolverException;
import lpWrapper.Configuration;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.urbansecModels.AdversaryPath;
import model.urbansecModels.UrbanSecProblem;

public class BRAdversary {
	/**
	 * loadProblem() -> setDefenderStrategy(); -> solve();
	 */
	private List<BRAdversaryFixedTarget> listBRAdvFT;
	private UrbanSecProblem usProblem;
	private double curObjective;
	private int brAdvOptimalFTindex;
	
	public BRAdversary(UrbanSecProblem usProblem) throws MalformedGraphException {
		super();
		this.curObjective = -Configuration.MM;
		this.brAdvOptimalFTindex = -1;
		this.usProblem = usProblem;
		List<INode> targetNodesSet = this.usProblem.getTargetNodesSet();
		this.listBRAdvFT = new ArrayList<BRAdversaryFixedTarget>(targetNodesSet.size());
		INode source = this.usProblem.getSource();
		for ( int t=0; t<targetNodesSet.size(); t++){
			this.listBRAdvFT.add(new BRAdversaryFixedTarget(this.usProblem, source, targetNodesSet.get(t)));
		}
	}
	public void loadProblem() {
		for ( Iterator<BRAdversaryFixedTarget> brAdvFTiter = listBRAdvFT.iterator(); brAdvFTiter.hasNext(); ) {
			BRAdversaryFixedTarget brAdvFTcurTarget = brAdvFTiter.next();
			brAdvFTcurTarget.loadProblem();
		}
	}
	public void setDefenderStrategy (List<Double> defenderStrategy) {
		for ( Iterator<BRAdversaryFixedTarget> brAdvFTiter = listBRAdvFT.iterator(); brAdvFTiter.hasNext(); ) {
			BRAdversaryFixedTarget brAdvFTcurTarget = brAdvFTiter.next();
			brAdvFTcurTarget.setDefenderStrategy(defenderStrategy);
		}		
	}
	public void resetLP() {
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.resetLP();
		}
	}
	public void solve() throws LPSolverException {
		this.curObjective = -Configuration.MM;
		boolean atLeastOneSolnFound = false;
		this.brAdvOptimalFTindex = -1;
		
		//int curIndex = 0;
		for (int index = 0;index<listBRAdvFT.size();index++){
			BRAdversaryFixedTarget brAdvFTcurTarget = listBRAdvFT.get(index);
			
			// brAdvFTcurTarget.writeProb("BRADVWRITE");
			
			brAdvFTcurTarget.solve();
			double brAdvFTcurTargetAdvPayoff = brAdvFTcurTarget.getAdversaryPayoff();	
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
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
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
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			apList.add(brAdvFTcurTarget.getAdversaryPath());
		}
		return apList;
	}
	/**
	 * Function exists only for debugging 
	 */
	public List<Double> getAllTargetAdversaryPayoffs() {
		List<Double> advPayoffs = new ArrayList<Double>(listBRAdvFT.size());
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			advPayoffs.add(brAdvFTcurTarget.getAdversaryPayoff());
		}
		return advPayoffs;
	}
	
	public void writeProb(String fileName) {
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.writeProb(fileName + "T"+brAdvFTcurTarget.getTarget().getId());
		}
	}
	public void writeSol(String fileName) {
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.writeSol(fileName + "T"+brAdvFTcurTarget.getTarget().getId());
		}
	}
	public long getLoadTime() {
		long loadTime = 0;
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			loadTime += brAdvFTcurTarget.getLoadTime();
		}
		return loadTime;
	}
	public long getRunTime() {
		long runTime = 0;
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			runTime += brAdvFTcurTarget.getRunTime();
		}
		return runTime;		
	}
	public void end() {
		for ( BRAdversaryFixedTarget brAdvFTcurTarget : listBRAdvFT) {
			brAdvFTcurTarget.end();
		}
		// TODO Auto-generated method stub		
	}
	
}
