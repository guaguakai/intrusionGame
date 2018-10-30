package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.List;

import lpWrapper.Configuration;

/**
 * Package protected variables for easy access (data storage class, getters and setters
 * are unnecessary). Use only for storing data, not manipulation!
 * @author Ondrej Vanek
 *
 */
public class Measure {

	public int iterations;
	public long totalTime;
	public long loadTime;
	
	public long warmStartTime = 0;
	public long warmStartIterations = 0;

	public List<Integer> defenderSuppSetSize = new ArrayList<Integer>();
	public List<Integer> adversarySuppSetSize = new ArrayList<Integer>();

	public List<Integer> defenderStrategiesSize = new ArrayList<Integer>();
	public List<Integer> adversaryStrategiesSize = new ArrayList<Integer>();

	public List<Long> defenderOracleTimes =new ArrayList<Long>();
	public List<Long> adversaryOracleTimes = new ArrayList<Long>();
	public List<Long> coreLPtimes = new ArrayList<Long>();

	public List<Long> betterDefenderOracleTimes =new ArrayList<Long>();
	public List<Long> betterAdversaryOracleTimes = new ArrayList<Long>();

	public List<Double> defenderBRVal = new ArrayList<Double>();
	public List<Double> adversaryBRVal =  new  ArrayList<Double>();
	
	public List<Double> defenderBetterRVal = new ArrayList<Double>();
	public List<Double> adversaryBetterRVal =  new  ArrayList<Double>();

	public List<Double> coreLPGV = new ArrayList<Double>();

	public void write() {
		System.out.println("%=== MEASURE ===");
		System.out.println("%Iterations: "+ iterations);
		System.out.println("%TotalTime: " + totalTime);
		System.out.println("%WarmStartIterations: "+ warmStartIterations);
		System.out.println("%WarmStartTime: " + warmStartTime);
		if(iterations>0) System.out.println("%Time per iteration :"+ totalTime/iterations);
		write(betterDefenderOracleTimes, "bdTime");
		write(defenderOracleTimes,"dTime");
		write(betterAdversaryOracleTimes, "baTime");	
		write(adversaryOracleTimes,"aTime");
		write(coreLPtimes,"ctime");
		write(defenderBetterRVal,"bdVal");
		write(defenderBRVal,"dVal");
		write(adversaryBetterRVal,"baVal");
		write(adversaryBRVal,"aVal");
		write(coreLPGV,"cVal");
		write(adversarySuppSetSize,"aSSS");
		write(defenderSuppSetSize,"dSSS");
		write(adversaryStrategiesSize,"aSize");
		write(defenderStrategiesSize,"dSize");
		System.out.println("%=== END OF MEASURE ===");

	}

	public static  void write(List<? extends Object> list, String string) {
		StringBuilder builder = new StringBuilder();
		builder.append(string);
		builder.append("=[");
		for(Object o: list ){
			builder.append(o.toString());
			builder.append(";");
		}
		builder.append("];");
		System.out.println(builder.toString());
	}

	public boolean isOK(){
		double advMax = -getMin(adversaryBRVal);
		double defMin = getMin(defenderBRVal);
		//System.out.println("ADVERSARY MAX: "+ advMax);
		//System.out.println("DEFENDER MIN: "+ defMin);
		//if(advMax>defMin){
			if(advMax-defMin> Configuration.EPSILON){
				System.out.println("ADVERSARY MAX: "+ advMax);
				System.out.println("DEFENDER MIN: "+ defMin);
				return false;
			}

		return true;
	}

	private double getMin(List<Double> defenderBRVal) {
		double min = Double.MAX_VALUE;
		for(double d: defenderBRVal){
			if(min>d) min = d;
		}
		return min;
	}

	@SuppressWarnings("unused")
	private double getMax(List<Double> adversaryBRVal) {
		double max = -Double.MAX_VALUE;

		for(double d: adversaryBRVal){
			if(max<d) max = d;
		}
		return max;
	}

}
