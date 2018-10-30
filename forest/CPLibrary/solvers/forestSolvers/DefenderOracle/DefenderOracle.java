package teamBuildingSolvers.DefenderOracle;

import ilog.concert.IloException;

import java.util.List;

import lpWrapper.LPSolverException;
import model.teamBuildingModels.DefenderAllocation;
import model.teamBuildingModels.TeamBuildingProblem;

public class DefenderOracle {
	
	private TeamBuildingProblem team;
	
	private boolean prob=true;
	
	
	public BRDefenderP DOp;
	public BRDefender DO;
	
	public DefenderOracle(TeamBuildingProblem team){
		
		this.team=team;
		this.prob = this.team.isProbability;
		
		if(this.team.isProbability){
			DOp = new BRDefenderP(team);
		}else{
			DO = new BRDefender(team);
		}
		
	}
	
	public void loadProblem(){
		if(prob){
			DOp.loadProblem();
		}else{
			DO.loadProblem();
		}
	}
	public void solve() throws LPSolverException{
		if(prob){
			DOp.solve();
		}else{
			DO.solve();
		}
	}
	
	public void writeProb(String file){
		if(prob){
			DOp.writeProb(file);
		}else{
			DO.writeProb(file);
		}
		
	}
	
	public DefenderAllocation getDefenderAllocation(){
		if(prob){
			return DOp.getDefenderAllocation();
		}else{
			return DO.getDefenderAllocation();
		}
	}

	public void updateAdversaryPaths(){
		if(!prob){
			DO.updateAdversaryPaths();
		}
	}
	public void setAdversaryStrategy(List<Double> adversaryStrategy){
		if(prob){
			DOp.setAdversaryStrategy(adversaryStrategy);
		}else{
			DO.setAdversaryStrategy(adversaryStrategy);
		}
	}
	public double getDefenderPayoff(){
		if(prob){
			return DOp.getDefenderPayoff();
		}else{
			return DO.getDefenderPayoff();
		}
	}
	
	
	public long getRunTime(){
		if(prob){
			return (long)DOp.runTime;
		}else{
			return DO.getRunTime();
		}
	}
	public void end(){
		if(prob){
			DOp.end();
		}else{
			DO.end();
		}
	}

	public void writeSol(String file) {
		if(prob){
			DOp.writeSol(file);
		}else{
			DO.writeSol(file);
		}
		
	}
}
