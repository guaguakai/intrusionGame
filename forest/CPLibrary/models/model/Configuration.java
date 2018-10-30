package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Configuration {
	public final static int LOW = 1;
	public final static int HIGH = 10;
	
	public final static int DEF_COV_MIN = LOW;
	public final static int DEF_UNCOV_MIN = -HIGH;
	public final static int ATT_COV_MIN = -HIGH;
	public final static int ATT_UNCOV_MIN = LOW;
	
	public final static int DEF_COV_MAX = HIGH;
	public final static int DEF_UNCOV_MAX = -LOW;
	public final static int ATT_COV_MAX = -LOW;
	public final static int ATT_UNCOV_MAX = HIGH;
	
	public final static int GENERIC_GAME_MIN_LEADER_REWARD = LOW;
	public final static int GENERIC_GAME_MAX_LEADER_REWARD = HIGH;
	public final static int GENERIC_GAME_MIN_FOLLOWER_REWARD = LOW;
	public final static int GENERIC_GAME_MAX_FOLLOWER_REWARD = HIGH;
	
	public final static double ATTACKER_TOLERANCE = 1e-3;
	
	public static final int MM = lpWrapper.Configuration.MM; 
	
	public static final long RANGER_BUCKETS = 10000; // resolution for sampling probabilities from Ranger
	
	public static Random randomGenerator; 
	
	public static void initialize(long seed) {
		randomGenerator = new Random(seed);		
	}
	
	public static void setSeed(long seed) {
		randomGenerator.setSeed(seed);
	}
	
	public static int generateWeightedBucketSample(List<Integer> weights) {
		List<Integer> buckets = new ArrayList<Integer>();
		for ( int i=0; i < weights.size(); i++) {
			for ( int w=0; w < weights.get(i); w++) {
				buckets.add(i);
			}
		}
		int randomNumber = randomGenerator.nextInt(buckets.size());
		return buckets.get(randomNumber);
	}
	
	public static int generateWeightedBucketSample(Map<Integer, Integer> weights) {
		int count = 0;
		List<Integer> buckets = new ArrayList<Integer>();
		Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
		for ( Integer targetID : weights.keySet() ) {
			count ++;
			mapping.put(count, targetID);
			for ( int w=0; w < weights.get(targetID); w++) {
				buckets.add(count);
			}
		}
		
		// Manish Debug Print
		// System.out.println("BUCKETS SIZE: " + buckets.size());
		
		int randomNumber = randomGenerator.nextInt(buckets.size());
		return mapping.get(buckets.get(randomNumber));
	}

}