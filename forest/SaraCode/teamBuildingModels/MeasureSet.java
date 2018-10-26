package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MeasureSet {

	private List<Measure> measures = new ArrayList<Measure>();

	public void add(Measure m){
		measures.add(m);
	}
	
	public void add(List<Measure> ms){
		for (Iterator<Measure> iterator = ms.iterator(); iterator.hasNext();) {
			Measure measure = iterator.next();
			measures.add(measure);
		}
	}

	public String toString(){
		StringBuilder times = new StringBuilder();
		StringBuilder iterations = new StringBuilder();

		times.append("time=[");
		iterations.append("iters=[");
		for(Measure m :measures){
			times.append(m.totalTime+";");
			iterations.append(m.iterations+";");
		}
		times.append("];");
		iterations.append("];");

		return times+"\n"+iterations;
	}

	public String toString(String prefix) {
		StringBuilder times = new StringBuilder();
		StringBuilder iterations = new StringBuilder();

		times.append("time=[");
		iterations.append("iters=[");
		for(Measure m :measures){
			times.append(m.totalTime+";");
			iterations.append(m.iterations+";");
		}
		times.append("];");
		iterations.append("];");
		
		
		StringBuilder sb = new StringBuilder();
		List<List<? extends Number>> aOT = new ArrayList<List<? extends Number>>();
		List<List<? extends Number>> dOT = new ArrayList<List<? extends Number>>();
		List<List<? extends Number>> cOT = new ArrayList<List<? extends Number>>();
		List<List<? extends Number>> aSS = new ArrayList<List<? extends Number>>();
		List<List<? extends Number>> dSS = new ArrayList<List<? extends Number>>();
		List<List<? extends Number>> aSSS = new ArrayList<List<? extends Number>>();
		List<List<? extends Number>> dSSS = new ArrayList<List<? extends Number>>();
		for(Measure m: measures){
			aOT.add(m.adversaryOracleTimes);
			dOT.add(m.defenderOracleTimes);
			cOT.add(m.coreLPtimes);
			aSS.add(m.adversaryStrategiesSize);
			dSS.add(m.defenderStrategiesSize);
			aSSS.add(m.adversarySuppSetSize);
			dSSS.add(m.defenderSuppSetSize);
		}

		sb.append(writeMatrix(aOT, "advOTimes"));
		sb.append(writeMatrix(dOT, "defOTimes"));
		sb.append(writeMatrix(cOT, "coreOTimes"));
		sb.append(writeMatrix(aSS, "advStratSize"));
		sb.append(writeMatrix(dSS, "defStratSize"));
		sb.append(writeMatrix(aSSS, "advSupp"));
		sb.append(writeMatrix(dSSS, "defsupp"));
		
		
		
		return times.toString()+"\n"+iterations.toString()+"\n"+ sb.toString();
	}
	
	public static String writeMatrix(List<List<? extends Number>> matrix, String string){
		StringBuilder builder = new StringBuilder();
		builder.append(string);
		builder.append("=[");
		for(List<? extends Object> list: matrix){
		for(Object o: list ){
			builder.append(o.toString());
			builder.append(",");
		}
		builder.append(";");
		}
		builder.append("];\n");
		return builder.toString();
	}
	
	public static  String write(List<? extends Object> list, String string) {
		StringBuilder builder = new StringBuilder();
		builder.append(string);
		builder.append("=[");
		for(Object o: list ){
			builder.append(o.toString());
			builder.append(";");
		}
		builder.append("];\n");
		return builder.toString();
	}

	public List<Measure> getMeasures() {
		return measures;
	}

}
