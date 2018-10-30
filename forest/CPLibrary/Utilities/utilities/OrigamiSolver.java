package utilities;

import java.util.*;
import java.math.RoundingMode;
import java.math.BigDecimal;

public class OrigamiSolver {
	static int precision = 4;
	public static int targetsComputed = 0;
	public static int constraintsComputed = 0;
	public static int iterationsComputed = 0;
	public static int minCoveragesComputed = 0;
	
	private static int checkConstraints(SSGSolution ssgs, Map<Integer, StructuredSecurityGame> ssgMap, Map<Integer, Double> lowerBoundMap){
		BigDecimal tolerance = new BigDecimal(1.0);
		tolerance = tolerance.movePointLeft(precision);
		
		for(Integer sgid : ssgMap.keySet()){
			ssgs.computeExpectedPayoffs(ssgMap.get(sgid));
			
			BigDecimal defenderPayoff = new BigDecimal(ssgs.getDefenderPayoff());
			
			if(defenderPayoff.scale() > precision){
				defenderPayoff = defenderPayoff.setScale(precision, RoundingMode.HALF_DOWN);
			}
			
			BigDecimal lowerBound = new BigDecimal(lowerBoundMap.get(sgid));
			
			if(lowerBound.scale() > precision){
				lowerBound = lowerBound.setScale(precision, RoundingMode.HALF_DOWN);
			}
			
			if(lowerBound.subtract(tolerance).compareTo(defenderPayoff) == 1){
				return sgid;
			}
		}

		return -1;
	}
	
	private static double maxDefenderPayoff(SSGSolution solution, StructuredSecurityGame violatedSSG, List<TargetData> targets, int nextTarget){
		SSGSolution newSolution = new SSGSolution(nextTarget + 1, 1);
		StructuredSecurityGame compactSSG = new StructuredSecurityGame(nextTarget + 1, 1);
		
		for (int t = 0; t <= nextTarget; t++) {
			TargetData td = targets.get(t);
			newSolution.setProb(t, 0, solution.getProb(td.originalIndex, 0));
			compactSSG.setPayoffs(t, violatedSSG.getPayoffs(td.originalIndex));
		}
		
		newSolution.computeExpectedPayoffs(compactSSG);
		
		return newSolution.getDefenderPayoff();
	}
	
	private static SSGSolution MinCov(StructuredSecurityGame violatedSSG, List<TargetData> targets, double lowerBound){
		minCoveragesComputed++;
		
		SSGSolution bestSolution = null;
		double lowestResourceTotal = Double.MAX_VALUE;
		
		BigDecimal tolerance = new BigDecimal(1.0);
		tolerance.movePointLeft(precision);
		
		for(int t1 = 0; t1 < violatedSSG.getNTargets(); t1++){
			SSGSolution newSolution = new SSGSolution(violatedSSG.getNTargets(), 1);
			
			for(int t = 0; t < violatedSSG.getNTargets(); t++){
				newSolution.setProb(t, 0, targets.get(t).currentCoverage);
			}
			
			newSolution.computeExpectedPayoffs(violatedSSG);
			
			SSGPayoffs ssgp = violatedSSG.getPayoffs(t1);

			double defUncov = ssgp.getDefenderUncoveredPayoff();
			double defCov = ssgp.getDefenderCoveredPayoff();
			double attUncov = ssgp.getAttackerUncoveredPayoff();
			double attCov = ssgp.getAttackerCoveredPayoff();

			double newCoverage = (lowerBound - defUncov) / (defCov - defUncov);
			
			boolean invalidSolution = false;
			
			newCoverage = Math.max(newCoverage, targets.get(t1).currentCoverage);

			if(newCoverage <= 1.0 && newCoverage >= targets.get(t1).currentCoverage){
				newSolution.setProb(t1, 0, newCoverage);
				
				double expectedAttackerPayoff = (newCoverage * attCov) + ((1 - newCoverage) * attUncov);

				for(int t2 = 0; t2 < violatedSSG.getNTargets(); t2++){
					if(t1 != t2){
						TargetData td = targets.get(t2);
						double eap = (td.currentCoverage * td.attackerCoveredPayoff) + ((1 - td.currentCoverage) * td.attackerUncoveredPayoff);
						
						if(eap > expectedAttackerPayoff){
							td.getAdditionalCoverage(expectedAttackerPayoff);
							newSolution.setProb(td.originalIndex, 0, td.newCoverage);
							
							if(td.newCoverage > 1.0 || td.currentCoverage > td.newCoverage || newSolution.getTotalDefenderProb(0) > violatedSSG.getNumDefenders(0)){
								invalidSolution = true;
								break;
							}
						}
					}
				}
				
				if(!invalidSolution){
					newSolution.computeExpectedPayoffs(violatedSSG);
					
					double resourcesUsed = newSolution.getTotalDefenderProb(0);
					
					if(violatedSSG.getNumDefenders(0) >= resourcesUsed && lowestResourceTotal > resourcesUsed && newSolution.getDefenderPayoff() >= lowerBound - tolerance.doubleValue()) {
						bestSolution = newSolution;
						lowestResourceTotal = newSolution.getTotalDefenderProb(0);
					}
				}
			}
		}
		
		return bestSolution;
	}
	
	private static SSGSolution computeMinCoverage(StructuredSecurityGame violatedSSG, List<TargetData> targets, int nextTarget, double lowerBound){
		minCoveragesComputed++;
		
		SSGSolution bestSolution = null;
		double lowestResourceTotal = Double.MAX_VALUE;
		
		BigDecimal tolerance = new BigDecimal(1.0);
		tolerance.movePointLeft(precision);
		
		for (int t1 = 0; t1 < nextTarget; t1++) {
			SSGSolution newSolution = new SSGSolution(violatedSSG.getNTargets(), 1);
			
			for (int t = 0; t < violatedSSG.getNTargets(); t++) {
				TargetData td = targets.get(t);
				newSolution.setProb(td.originalIndex, 0, td.currentCoverage);
			}
			
			newSolution.computeExpectedPayoffs(violatedSSG);
			
			SSGPayoffs ssgp = violatedSSG.getPayoffs(targets.get(t1).originalIndex);

			double defUncov = ssgp.getDefenderUncoveredPayoff();
			double defCov = ssgp.getDefenderCoveredPayoff();
			double attUncov = ssgp.getAttackerUncoveredPayoff();
			double attCov = ssgp.getAttackerCoveredPayoff();

			double newCoverage = (lowerBound - defUncov) / (defCov - defUncov);

			if (newCoverage > 0.0){
				double expectedAttackerPayoff = (newCoverage * attCov) + ((1 - newCoverage) * attUncov);

				for (int t2 = 0; t2 < violatedSSG.getNTargets(); t2++){
					TargetData td = targets.get(t2);
					double eap = (td.currentCoverage * td.attackerCoveredPayoff) + ((1 - td.currentCoverage) * td.attackerUncoveredPayoff);
					
					if(eap > expectedAttackerPayoff){
						double reduction = td.getAdditionalCoverage(expectedAttackerPayoff);
						newSolution.setProb(td.originalIndex, 0, td.currentCoverage + reduction);
					}
				}

				newSolution.computeExpectedPayoffs(violatedSSG);
				
				double resourcesUsed = newSolution.getTotalDefenderProb(0);

				if (violatedSSG.getNumDefenders(0)> resourcesUsed && lowestResourceTotal > resourcesUsed && newSolution.getDefenderPayoff() >= lowerBound - tolerance.doubleValue()) {
					bestSolution = newSolution;
					lowestResourceTotal = newSolution.getTotalDefenderProb(0);
				}
			}
		}
		
		return bestSolution;
	}
	
	public static SSGSolution OrigamiM(Map<Integer, StructuredSecurityGame> ssgMap, Map<Integer, Double> coverageMap, Map<Integer, Double> lowerBoundMap, int defenderResources) {
		SSGSolution ssgs = new SSGSolution(coverageMap.size(),1);
		
		for(Integer target : coverageMap.keySet()){
			ssgs.setProb(target - 1, 0, coverageMap.get(target));
		}
		
		if(ssgs.getTotalDefenderProb(0) > defenderResources){
			return null;
		}
		
		int violatedConstraint = checkConstraints(ssgs, ssgMap, lowerBoundMap);
		
		while(violatedConstraint > 0){
			constraintsComputed++;
			
			StructuredSecurityGame violatedSSG = ssgMap.get(violatedConstraint);
			SSGSolution solution = new SSGSolution(violatedSSG.getNTargets(), 1);
			
			int nextTarget = 1;
			double remainingCoverage = violatedSSG.getNumDefenders(0) - ssgs.getTotalDefenderProb(0);
			
			List<TargetData> targets = new ArrayList<TargetData>();

			for (int t = 0; t < violatedSSG.getNTargets(); t++) {
				TargetData td = new TargetData();
				td.attackerCoveredPayoff = violatedSSG.getPayoffs(t).getAttackerCoveredPayoff();
				td.attackerUncoveredPayoff = violatedSSG.getPayoffs(t).getAttackerUncoveredPayoff();
				td.currentCoverage = ssgs.getProb(t, 0);
				td.newCoverage = ssgs.getProb(t, 0);
				td.originalIndex = t;
				targets.add(td);
				
				solution.setProb(t, 0, ssgs.getProb(t, 0));
			}
			
			Collections.sort(targets, new TargetComparator());
			
			solution.computeExpectedPayoffs(violatedSSG);
			
			while(nextTarget < violatedSSG.getNTargets()){
				targetsComputed++;
				
				double maxCovered = Double.NEGATIVE_INFINITY;
				
				for(int t = 0; t < nextTarget; t++){
					if(targets.get(t).attackerCoveredPayoff > maxCovered){
						maxCovered = targets.get(t).attackerCoveredPayoff;
					}
				}
				
				if(maxCovered > targets.get(nextTarget).attackerUncoveredPayoff){
					double coverageNeeded = 0.0;
					double nextExpectedValue = maxCovered;
					
					for (int t = 0; t < nextTarget; t++) {
						double additional = targets.get(t).getAdditionalCoverage(nextExpectedValue);
						coverageNeeded += additional;
					}
					
					if(coverageNeeded > remainingCoverage) {
						double normalizationFactor = 0.0;
					    
						for (int t = 0; t < nextTarget; t++) {
					    	normalizationFactor += targets.get(t).getRatio();
					    }
	
						for(int t = 0; t < violatedSSG.getNTargets(); t++) {
							TargetData td = targets.get(t);
							
							if(t < nextTarget){
								double temp = (td.getRatio() / normalizationFactor) * remainingCoverage;
						    	
								solution.setProb(td.originalIndex, 0, td.currentCoverage + temp);
							}
							else{
								solution.setProb(td.originalIndex, 0, td.currentCoverage);
							}
						}
						
						if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget) >= lowerBoundMap.get(violatedConstraint)){
							if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget) > lowerBoundMap.get(violatedConstraint)){
								SSGSolution tempSolution = computeMinCoverage(violatedSSG, targets, nextTarget, lowerBoundMap.get(violatedConstraint));
								
								if(tempSolution != null){
									solution = tempSolution;
								}
							}
							
							break;
						}
						else{
							return null;
						}
					}
					else{
						for(int t = 0; t < violatedSSG.getNTargets(); t++) {
							TargetData td = targets.get(t);
							solution.setProb(td.originalIndex, 0, td.newCoverage);
						}
						
						if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget) >= lowerBoundMap.get(violatedConstraint)){
							if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget - 1) > lowerBoundMap.get(violatedConstraint)){
								SSGSolution tempSolution = computeMinCoverage(violatedSSG, targets, nextTarget, lowerBoundMap.get(violatedConstraint));
								
								if(tempSolution != null){
									solution = tempSolution;
								}
							}
							
							break;
						}
						else{
							return null;
						}
					}
				}
				else{
					double coverageNeeded = 0.0;
					
					TargetData nt = targets.get(nextTarget);
					double coverage = nt.currentCoverage;
					double nextExpectedValue = (coverage * nt.attackerCoveredPayoff) + ((1 - coverage) * nt.attackerUncoveredPayoff);
					
					for (int t = 0; t < nextTarget; t++) {
						double additional = targets.get(t).getAdditionalCoverage(nextExpectedValue);
						coverageNeeded += additional;
					}
					
					if(coverageNeeded > remainingCoverage) {
						double normalizationFactor = 0.0;
					    
						for (int t = 0; t < nextTarget; t++) {
					    	normalizationFactor += targets.get(t).getRatio();
					    }
	
						for(int t = 0; t < violatedSSG.getNTargets(); t++) {
							TargetData td = targets.get(t);
							
							if(t < nextTarget){
								double temp = (td.getRatio() / normalizationFactor) * remainingCoverage;
						    	
								solution.setProb(td.originalIndex, 0, td.currentCoverage + temp);
							}
							else{
								solution.setProb(td.originalIndex, 0, td.currentCoverage);
							}
						}
						
						if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget) >= lowerBoundMap.get(violatedConstraint)){
							if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget - 1) > lowerBoundMap.get(violatedConstraint)){
								SSGSolution tempSolution = computeMinCoverage(violatedSSG, targets, nextTarget, lowerBoundMap.get(violatedConstraint));
								
								if(tempSolution != null){
									solution = tempSolution;
								}
							}
							
							break;
						}
						else{
							return null;
						}
					}
					else{
						for(int t = 0; t < nextTarget; t++) {
							TargetData td = targets.get(t);
							solution.setProb(td.originalIndex, 0, td.newCoverage);
						}
						
						solution.computeExpectedPayoffs(violatedSSG);
						
						if(solution.getDefenderPayoff() >= lowerBoundMap.get(violatedConstraint)){
						//if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget) >= lowerBoundMap.get(violatedConstraint)){
							if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget - 1) > lowerBoundMap.get(violatedConstraint)){
								SSGSolution tempSolution = computeMinCoverage(violatedSSG, targets, nextTarget, lowerBoundMap.get(violatedConstraint));
								
								if(tempSolution != null){
									solution = tempSolution;
								}
							}
							
							break;
						}
						else{
							for(int t = 0; t < nextTarget; t++){
								TargetData td = targets.get(t);
								td.currentCoverage = td.newCoverage;
							}
							
							nextTarget++;
							
							if(nextTarget == violatedSSG.getNTargets()){
								return null;
							}
							
							TargetData nnt = targets.get(nextTarget);
							double nextCoverage = nnt.currentCoverage;
							double expectedValue = (nextCoverage * nnt.attackerCoveredPayoff) + ((1 - nextCoverage) * nnt.attackerUncoveredPayoff);
							
							while (nextTarget < violatedSSG.getNTargets() - 1 && expectedValue == nextExpectedValue) {
								nextTarget++;
								
								nnt = targets.get(nextTarget);
								nextCoverage = nnt.currentCoverage;
								expectedValue = (nextCoverage * nnt.attackerCoveredPayoff) + ((1 - nextCoverage) * nnt.attackerUncoveredPayoff);
							}
							
							remainingCoverage -= coverageNeeded;
						}
					}
				}
			}
			
			///////////////////////////////
			if(nextTarget == violatedSSG.getNTargets() && remainingCoverage > 0){
				double normalizationFactor = 0.0;
			    
				for (int t = 0; t < nextTarget; t++) {
			    	normalizationFactor += targets.get(t).getRatio();
			    }

				for(int t = 0; t < violatedSSG.getNTargets(); t++) {
					TargetData td = targets.get(t);
				
					double temp = (td.getRatio() / normalizationFactor) * remainingCoverage;
				    	
					solution.setProb(td.originalIndex, 0, td.currentCoverage + temp);
				}
				
				solution.computeExpectedPayoffs(violatedSSG);
				
				if(solution.getDefenderPayoff() >= lowerBoundMap.get(violatedConstraint)){
					SSGSolution tempSolution = computeMinCoverage(violatedSSG, targets, nextTarget, lowerBoundMap.get(violatedConstraint));
					
					if(tempSolution != null){
						solution = tempSolution;
					}
				}	
			}
			//////////////////////////////
			
			ssgs = solution;
				
			int previousViolatedConstraint = violatedConstraint;
			violatedConstraint = checkConstraints(ssgs, ssgMap, lowerBoundMap);
				
			if(violatedConstraint == previousViolatedConstraint){
				System.out.println("Duplicate Violated Constraint");
				return null;
			}
		}
		
		return ssgs;
	}
	
	public static SSGSolution directMinCov(Map<Integer, StructuredSecurityGame> ssgMap, Map<Integer, Double> coverageMap, Map<Integer, Double> lowerBoundMap, int defenderResources) {
		SSGSolution ssgs = new SSGSolution(coverageMap.size(),1);
		
		for(Integer target : coverageMap.keySet()){
			ssgs.setProb(target - 1, 0, coverageMap.get(target));
		}
		
		if(ssgs.getTotalDefenderProb(0) > defenderResources){
			return null;
		}
		
		int violatedConstraint = checkConstraints(ssgs, ssgMap, lowerBoundMap);
		
		Map<Integer, List<TargetData>> targetsMap = new HashMap<Integer, List<TargetData>>();
		
		for(Integer ssgID : ssgMap.keySet()){
			List<TargetData> targetList = new ArrayList<TargetData>();
			for (int t = 0; t < ssgMap.get(ssgID).getNTargets(); t++) {
				TargetData td = new TargetData();
				td.attackerCoveredPayoff = ssgMap.get(ssgID).getPayoffs(t).getAttackerCoveredPayoff();
				td.attackerUncoveredPayoff = ssgMap.get(ssgID).getPayoffs(t).getAttackerUncoveredPayoff();
				td.currentCoverage = ssgs.getProb(t, 0);
				td.newCoverage = 0.0;
				td.originalIndex = t;
				targetList.add(td);
			}
			
			targetsMap.put(ssgID, targetList);
		}
		
		while(violatedConstraint > 0){
			constraintsComputed++;
			
			StructuredSecurityGame violatedSSG = ssgMap.get(violatedConstraint);
			List<TargetData> targetsList = targetsMap.get(violatedConstraint);
			double bound = lowerBoundMap.get(violatedConstraint);
			
			for(int t = 0; t < violatedSSG.getNTargets(); t++){
				TargetData td = targetsList.get(t);
				td.currentCoverage = ssgs.getProb(t, 0);
				td.newCoverage = ssgs.getProb(t, 0);
			}
			
			SSGSolution currentSolution = MinCov(violatedSSG, targetsList, bound);

			if(currentSolution != null){
				ssgs = currentSolution;
				
				int previousViolatedConstraint = violatedConstraint;
				violatedConstraint = checkConstraints(ssgs, ssgMap, lowerBoundMap);
				
				if(violatedConstraint == previousViolatedConstraint){
					return null;
				}
			}
			else{
				return null;
			}
		}
		
		return ssgs;
	}

	public static SSGSolution binaryOrigamiM(Map<Integer, StructuredSecurityGame> ssgMap, Map<Integer, Double> coverageMap, Map<Integer, Double> lowerBoundMap, int defenderResources) {
		SSGSolution ssgs = new SSGSolution(coverageMap.size(),1);
		
		for(Integer target : coverageMap.keySet()){
			ssgs.setProb(target - 1, 0, coverageMap.get(target));
		}
		
		if(ssgs.getTotalDefenderProb(0) > defenderResources){
			return null;
		}
		
		int violatedConstraint = checkConstraints(ssgs, ssgMap, lowerBoundMap);
		
		Map<Integer, List<TargetData>> targetsMap = new HashMap<Integer, List<TargetData>>();
		
		for(Integer ssgID : ssgMap.keySet()){
			List<TargetData> targetList = new ArrayList<TargetData>();
			for (int t = 0; t < ssgMap.get(ssgID).getNTargets(); t++) {
				TargetData td = new TargetData();
				td.attackerCoveredPayoff = ssgMap.get(ssgID).getPayoffs(t).getAttackerCoveredPayoff();
				td.attackerUncoveredPayoff = ssgMap.get(ssgID).getPayoffs(t).getAttackerUncoveredPayoff();
				td.currentCoverage = ssgs.getProb(t, 0);
				td.newCoverage = ssgs.getProb(t, 0);
				td.originalIndex = t;
				targetList.add(td);
			}
			
			targetsMap.put(ssgID, targetList);
		}
		
		while(violatedConstraint > 0){
			constraintsComputed++;
			
			StructuredSecurityGame violatedSSG = ssgMap.get(violatedConstraint);
			
			int lowerIndex = -1;
			int upperIndex = coverageMap.size();
			
			int lowestSatisfyingIndex = Integer.MAX_VALUE;
			int highestNonsatisfyingIndex = -Integer.MAX_VALUE;
			
			List<TargetData> targets = targetsMap.get(violatedConstraint);
			
			SSGSolution currentSolution = null;
			
			while(upperIndex - lowerIndex > 1){
				targetsComputed++;
				
				SSGSolution solution = new SSGSolution(violatedSSG.getNTargets(), 1);
				
				for(int t = 0; t < violatedSSG.getNTargets(); t++){
					TargetData td = targets.get(t);
					td.currentCoverage = ssgs.getProb(td.originalIndex, 0);
					td.newCoverage = ssgs.getProb(td.originalIndex, 0);
					
					solution.setProb(td.originalIndex, 0, ssgs.getProb(td.originalIndex, 0));
				}
				
				Collections.sort(targets, new TargetComparator());
				
				solution.computeExpectedPayoffs(violatedSSG);
				
				//System.out.println(solution.toString());
				//System.out.println(targets);
				
				int nextTarget = (upperIndex + lowerIndex) / 2;
				
				//System.out.print(nextTarget + " ");
				
				double maxCovered = Double.NEGATIVE_INFINITY;
				
				for(int t = 0; t < nextTarget; t++){
					if(targets.get(t).attackerCoveredPayoff > maxCovered){
						maxCovered = targets.get(t).attackerCoveredPayoff;
					}
				}
				
				double remainingCoverage = violatedSSG.getNumDefenders(0) - solution.getTotalDefenderProb(0);
				
				double coverageNeeded = 0.0;
				double nextExpectedValue;
				
				if(maxCovered > targets.get(nextTarget).attackerUncoveredPayoff){
					nextExpectedValue = maxCovered;
				}
				else{
					TargetData nt = targets.get(nextTarget);
					double coverage = nt.currentCoverage;
					
					nextExpectedValue = (coverage * nt.attackerCoveredPayoff) + ((1 - coverage) * nt.attackerUncoveredPayoff);
				}
				
				for (int t = 0; t < nextTarget; t++) {
					double additional = targets.get(t).getAdditionalCoverage(nextExpectedValue);
					coverageNeeded += additional;
				}
				
				if(coverageNeeded > remainingCoverage) {
					upperIndex = nextTarget;
				}
				else{
					for(int t = 0; t < nextTarget; t++) {
						TargetData td = targets.get(t);
						solution.setProb(td.originalIndex, 0, td.newCoverage);
					}
					
					if(maxDefenderPayoff(solution, violatedSSG, targets, nextTarget) >= lowerBoundMap.get(violatedConstraint)){
						upperIndex = nextTarget;
						
						if(nextTarget < lowestSatisfyingIndex){
							lowestSatisfyingIndex = nextTarget;
							currentSolution = solution;
						}
					}
					else{
						lowerIndex = nextTarget;
						                
						if(nextTarget > highestNonsatisfyingIndex){
							highestNonsatisfyingIndex = nextTarget;
						}
					}
				}
			}
			
			if(lowestSatisfyingIndex < violatedSSG.getNTargets() && highestNonsatisfyingIndex >= 0){
				if(maxDefenderPayoff(currentSolution, violatedSSG, targets, highestNonsatisfyingIndex) >= lowerBoundMap.get(violatedConstraint)){
					currentSolution = computeMinCoverage(violatedSSG, targets, lowestSatisfyingIndex, lowerBoundMap.get(violatedConstraint));
				}
			}
			else if(lowestSatisfyingIndex > violatedSSG.getNTargets() && highestNonsatisfyingIndex >= 0 && highestNonsatisfyingIndex < violatedSSG.getNTargets() - 1){
				double normalizationFactor = 0.0;
		    
				for (int t = 0; t <= highestNonsatisfyingIndex; t++) {
			    	normalizationFactor += targets.get(t).getRatio();
			    }
				
				double newExpectedValue = 0;
				
				TargetData nt = targets.get(highestNonsatisfyingIndex);
				double c = nt.currentCoverage;
				
				newExpectedValue = (c * nt.attackerCoveredPayoff) + ((1 - c) * nt.attackerUncoveredPayoff);
							
				double usedCoverage = 0.0;
				
				for (int t = 0; t < violatedSSG.getNTargets(); t++) {
					if(t <= highestNonsatisfyingIndex){
						targets.get(t).getAdditionalCoverage(newExpectedValue);
					}
					
					if(targets.get(t).newCoverage < 0){
						System.out.println();
					}
					
					usedCoverage += targets.get(t).newCoverage;
				}
				
				if(usedCoverage <= defenderResources){
					SSGSolution solution = new SSGSolution(violatedSSG.getNTargets(), 1);
					
					for(int t = 0; t < violatedSSG.getNTargets(); t++) {
						TargetData td = targets.get(t);
						
						if(t <= highestNonsatisfyingIndex){
							double temp = (td.getRatio() / normalizationFactor) * (defenderResources - usedCoverage);
							
							if(td.newCoverage + temp < 0){
								break;
							}
					    	
							solution.setProb(td.originalIndex, 0, td.newCoverage + temp);
						}
						else{
							solution.setProb(td.originalIndex, 0, td.newCoverage);
						}
					}
					
					if(maxDefenderPayoff(solution, violatedSSG, targets, highestNonsatisfyingIndex + 1) >= lowerBoundMap.get(violatedConstraint)){
						currentSolution = computeMinCoverage(violatedSSG, targets, highestNonsatisfyingIndex + 1, lowerBoundMap.get(violatedConstraint));
						
						if(currentSolution == null){
							currentSolution = solution;
						}
					}
				}
			}

			if(currentSolution != null){
				ssgs = currentSolution;
				
				int previousViolatedConstraint = violatedConstraint;
				violatedConstraint = checkConstraints(ssgs, ssgMap, lowerBoundMap);
				
				if(violatedConstraint == previousViolatedConstraint){
					//System.out.println("Duplicate Violated Constraint");
					return null;
				}
			}
			else{
				return null;
			}
		}
		
		return ssgs;
	}
	
	public static SSGSolution OrigamiA(Map<Integer, StructuredSecurityGame> ssgMap, Map<Integer, Double> lowerBoundMap, int primaryObjective, int defenderResources, double alpha, int formulation){
		SSGSolution finalSolution = null;
		Map<Integer, Double> modifiedLowerBoundMap = new HashMap<Integer, Double>(lowerBoundMap);
		Map<Integer, Double> coverageMap = new HashMap<Integer, Double>();
		
		for(int t = 1; t <= ssgMap.get(primaryObjective).getNTargets(); t++){
			coverageMap.put(t, 0.0);
		}
		
		List<Integer> sgidList = new ArrayList<Integer>(ssgMap.keySet());
		Collections.sort(sgidList);
		
		for(Integer sgid : sgidList){
			Double lowerBound = Double.POSITIVE_INFINITY;
			Double upperBound = Double.NEGATIVE_INFINITY;
			
			StructuredSecurityGame ssg = ssgMap.get(sgid);
			
			SSGPayoffs[] ssgpList = ssg.getPayoffs(); 
					
			for(int t = 0; t < ssgpList.length; t++){
				if(lowerBound > ssgpList[t].getDefenderUncoveredPayoff()){
					lowerBound = ssgpList[t].getDefenderUncoveredPayoff();
				}
						
				if(ssgpList[t].getDefenderCoveredPayoff() > upperBound){
					upperBound = ssgpList[t].getDefenderCoveredPayoff();
				}
			}
			
			if(sgid != primaryObjective){
				finalSolution.computeExpectedPayoffs(ssg);
				lowerBound = finalSolution.getDefenderPayoff();
			}
			
			while(upperBound - lowerBound > alpha){
				iterationsComputed++;
				
				double bound = (upperBound + lowerBound) / 2.0;
				
				modifiedLowerBoundMap.put(sgid, bound);
				
				//System.out.println(iterationsComputed + ":" + modifiedLowerBoundMap.toString());
				
				SSGSolution solution = null;
				
				if(formulation == 5){
					solution = OrigamiM(ssgMap, coverageMap, modifiedLowerBoundMap, defenderResources);
				}
				else if(formulation == 6){
					solution = directMinCov(ssgMap, coverageMap, modifiedLowerBoundMap, defenderResources);
				}
				else if(formulation == 7){
					solution = binaryOrigamiM(ssgMap, coverageMap, modifiedLowerBoundMap, defenderResources);
				}
			
				if(solution == null){
					upperBound = bound;
				}
				else{
					lowerBound = bound;
					finalSolution = solution;
				}
			}
			
			if(finalSolution == null){
				return null;
			}
			else{
				finalSolution.computeExpectedPayoffs(ssg);
				modifiedLowerBoundMap.put(sgid, finalSolution.getDefenderPayoff());
			}
		}
		
		return finalSolution;
	}

	private static class TargetData {
		int originalIndex;
		double attackerCoveredPayoff;
		double attackerUncoveredPayoff;
		double currentCoverage;
		double newCoverage;

		public double getAdditionalCoverage(double newExpectedPayoff) {
			newCoverage = (newExpectedPayoff - attackerUncoveredPayoff)	/ (attackerCoveredPayoff - attackerUncoveredPayoff);
			return newCoverage - currentCoverage;
		}

		public double getRatio() {
			return 1 / (attackerUncoveredPayoff - attackerCoveredPayoff);
		}

		public String toString() {
			return "[" + attackerCoveredPayoff + "," + attackerUncoveredPayoff + "," + currentCoverage + "," + newCoverage + "]";
		}
	}

	private static class TargetComparator implements Comparator<TargetData> {
		public int compare(TargetData td1, TargetData td2) {
			double td1ExpectedValue = (td1.currentCoverage * td1.attackerCoveredPayoff) + ((1 - td1.currentCoverage) * td1.attackerUncoveredPayoff);
			double td2ExpectedValue = (td2.currentCoverage * td2.attackerCoveredPayoff) + ((1 - td2.currentCoverage) * td2.attackerUncoveredPayoff);
			
			if (td1ExpectedValue < td2ExpectedValue)
				return 1;
			else if (td1ExpectedValue > td2ExpectedValue)
				return -1;
			else
				return 0;
		}
	}
}
