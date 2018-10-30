package PQsearch;

import java.util.ArrayList;
import java.util.List;

public class TeamStats {
	public static int [] cost = {3, 4, 5};
	
	public static List<Double> getCoverage(){
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(2.0);
		coverage.add(3.0);
		coverage.add(4.0);
		
		return coverage;
	}
	
	public static List<Double> getProbability(){
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(0.5);
		coverage.add(0.6);
		coverage.add(0.7);
		
		return coverage;
	}
}
