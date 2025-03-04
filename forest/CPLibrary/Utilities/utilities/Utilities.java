package utilities;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Utilities {
	public static List<Double> getAverages(List<List<Double>> sampleData) {
		int numSamples = sampleData.size();
		if (sampleData == null || numSamples == 0) {
			throw new RuntimeException("No data.");
		}
		int sampleSize = sampleData.get(0).size();
		List<Double> averages = new ArrayList<Double>(sampleSize);
		for (int i = 0; i < sampleSize; i++) {
			averages.add(0.0);
		}

		for (int i = 0; i < numSamples; i++) {
			if (sampleData.get(i).size() != sampleSize) {
				throw new RuntimeException("Sample size incorrect. Expected: "
						+ sampleSize + ", got: " + sampleData.get(i).size());
			}
			for (int j = 0; j < sampleSize; j++) {
				averages.set(j, averages.get(j) + sampleData.get(i).get(j));
			}
		}
		for (int i = 0; i < sampleSize; i++) {
			averages.set(i, averages.get(i) / numSamples);
		}
		return averages;
	}

	public static String listAsString(List<Double> listData) {
		String s = new String();
		for (Double d : listData) {
			s += ("" + d.toString() + ",\t");
		}
		return s;
	}

	public static Double getAverageDouble(List<Double> sampleData) {
		double sum = 0.0;
		if (sampleData.size() == 0.0) {
			return 0.0;
		}
		for (Double d : sampleData) {
			sum += ((double) 1.0 * d);
		}
		return (sum * 1.0 / sampleData.size());
	}

	public static Double getAverageInteger(List<Integer> sampleData) {
		double sum = 0.0;
		if (sampleData.size() == 0.0) {
			return 0.0;
		}
		for (Integer d : sampleData) {
			sum += ((double) 1.0 * d);
		}
		return (sum * 1.0 / sampleData.size());
	}

	public static Double getAverageLong(List<Long> sampleData) {
		double sum = 0.0;
		if (sampleData.size() == 0.0) {
			return 0.0;
		}
		for (Long d : sampleData) {
			sum += ((double) 1.0 * d);
		}
		return (sum * 1.0 / sampleData.size());
	}

	public static Integer max(Integer a, Integer b) {
		return (a > b ? a : b);
	}	
}
