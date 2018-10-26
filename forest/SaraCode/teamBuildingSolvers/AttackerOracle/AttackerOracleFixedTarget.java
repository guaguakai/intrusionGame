package teamBuildingSolvers.AttackerOracle;

import java.util.List;

import teamBuildingSolvers.DefenderOracle.BRDefender;
import teamBuildingSolvers.DefenderOracle.BRDefenderP;
import lpWrapper.LPSolverException;
import model.graphutils.INode;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.TeamBuildingProblem;

public class AttackerOracleFixedTarget {
	private TeamBuildingProblem team;
	
	private boolean prob=false;
	
	public boolean oracle=false;
	
	public BRAdversaryFixedTargetP AOp;
	public BRAdversaryFixedTarget AO;
	
	public AttackerOracleFixedTarget(TeamBuildingProblem team, INode source, INode target) {
		
		this.team=team;
		this.prob = this.team.isProbability;
		
		
		if(this.prob){
			AOp = new BRAdversaryFixedTargetP(team, source, target);
		}
		AO = new BRAdversaryFixedTarget(team, source, target);
		
	}
	public void loadProblem(){
		if(prob){
			AOp.loadProblem();
		}
		AO.loadProblem();
		
	}
	public void solve() throws LPSolverException{
		if(oracle){
			AOp.solve();
		}else{
			AO.solve();
		}
	}
	public void setDefenderStrategy(List<Double> defenderStrategy) {
		if(prob){
			AOp.setDefenderStrategy(defenderStrategy);
		}
		AO.setDefenderStrategy(defenderStrategy);
		
		
	}
	public void resetLP() {
		if(prob){
			AOp.resetLP();
		}
		AO.resetLP();
		
		
	}
	public double getAdversaryPayoff() {
		if(oracle&&prob){
			return AOp.getAdversaryPayoff();
		}else{
			return AO.getAdversaryPayoff();
		}
	}
	public void updateDefenderAllocations() {
		
		AO.updateDefenderAllocations();
		
		
	}
	public AdversaryPath getAdversaryPath() {
		if(oracle&&prob){
			return AOp.getAdversaryPath();
		}else{
			return AO.getAdversaryPath();
		}
	}
	public INode getTarget() {
		if(oracle&&prob){
			return AOp.getTarget();
		}else{
			return AO.getTarget();
		}
	}
	public void writeProb(String string) {
		if(oracle&&prob){
			AOp.writeProblem();
		}else{
			AO.writeProb(string);
		}
		
	}
	public void writeSol(String string) {
		if(prob){
			//:(
		}else{
			AO.writeSol(string);
		}
		
	}
	public long getLoadTime() {
		if(oracle&&prob){
			return (long) 0;
		}else{
			return AO.getLoadTime();
		}
	}
	public long getRunTime() {
		if(oracle&&prob){
			return (long) AOp.runTime;
		}else{
			return AO.getRunTime();
		}
	}
	public void end() {
		if(oracle&&prob){
			AOp.end();
		}else{
			AO.end();
		}
		
	}
}
