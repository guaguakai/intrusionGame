package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import model.graphutils.INode;

public class CompactStrategy {
	
	
	
	List<CompactPath> adversaryPaths;
	List<Double> adversaryStrategy;
	
	List<INode> targetSet;
	
	public List<CompactAllocation> defenderAllocation;
	public List<Double> defenderStrategy;
	
	public CompactStrategy(){
		adversaryPaths = new ArrayList<CompactPath>();
		adversaryStrategy = new ArrayList<Double>();
		defenderAllocation = new ArrayList<CompactAllocation>();
		defenderStrategy = new ArrayList<Double>();	
	}
	
	public List<INode> getTargetSet(){
		if(targetSet == null){
			targetSet = new ArrayList<INode>();
			for(CompactPath cp : adversaryPaths){
				targetSet.add(cp.target);
			}
		}
		return targetSet;
	}
	
	public void addDefenderAllocation(CompactAllocation da){
		defenderAllocation.add(da);
	}
	public void setDefenderStrategy(List<Double> strat){
		defenderStrategy=strat;
	}
	public int getNumDefenderAllocations(){
		return defenderAllocation.size();
	}
	
	public List<Double> getDefenderStrategy(){
		return defenderStrategy;
	}
	public List<Double> getAttackerStrategy(){
		return adversaryStrategy;
	}
	public void addAdversaryPath(CompactPath ap){
		adversaryPaths.add(ap);
		targetSet.add(ap.target);
	}
	
	public void addAllAdversaryPath(List<CompactPath> ap){
		adversaryPaths.addAll(ap);
		getTargetSet();
	}
	public void addAttackerStrategy(List<Double> astrat){
		adversaryStrategy=astrat;
	}
	public double PathPayoff(int ap){
		return adversaryPaths.get(ap).target.getPayoff();
	}
	
	public double ProbabilityIntersect(int da, int ap){
		CompactPath adversaryPath = adversaryPaths.get(ap);
		INode target = adversaryPath.target;
		return defenderAllocation.get(da).getCoverage(target);
	}
	
	

}
