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
	
	
	public BRAdversaryFixedTargetP AOp;
	public BRAdversaryFixedTarget AO;
	
	public AttackerOracleFixedTarget(TeamBuildingProblem team, INode source, INode target) {
		
		this.team=team;
		this.prob = this.team.isProbability;
		
		if(this.prob){
			AOp = new BRAdversaryFixedTargetP(team, source, target);
		}else{
			AO = new BRAdversaryFixedTarget(team, source, target);
		}	
	}
	public void loadProblem(){
		if(prob){
			AOp.loadProblem();
		}else{
			AO.loadProblem();
		}
	}
	public void solve() throws LPSolverException{
		if(prob){
			AOp.solve();
		}else{
			AO.solve();
		}
	}
	public void setDefenderStrategy(List<Double> defenderStrategy) {
		if(prob){
			AOp.setDefenderStrategy(defenderStrategy);
		}else{
			AO.setDefenderStrategy(defenderStrategy);
		}
		
	}
	public void resetLP() {
		if(prob){
			AOp.resetLP();
		}else{
			AO.resetLP();
		}
		
	}
	public double getAdversaryPayoff() {
		if(prob){
			return AOp.getAdversaryPayoff();
		}else{
			return AO.getAdversaryPayoff();
		}
	}
	public void updateDefenderAllocations() {
		if(!prob){
			AO.updateDefenderAllocations();
		}
		
	}
	public AdversaryPath getAdversaryPath() {
		if(prob){
			return AOp.getAdversaryPath();
		}else{
			return AO.getAdversaryPath();
		}
	}
	public INode getTarget() {
		if(prob){
			return AOp.getTarget();
		}else{
			return AO.getTarget();
		}
	}
	public void writeProb(String string) {
		if(prob){
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
		if(prob){
			return (long) 0;
		}else{
			return AO.getLoadTime();
		}
	}
	public long getRunTime() {
		if(prob){
			return (long) AOp.runTime;
		}else{
			return AO.getRunTime();
		}
	}
	public void end() {
		if(prob){
			AOp.end();
		}else{
			AO.end();
		}
		
	}
}
