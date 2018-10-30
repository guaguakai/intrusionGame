package urbansecSolvers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import lpWrapper.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;
import model.urbansecModels.MeasureSet;
import model.urbansecModels.UrbanSecProblem;

import org.jgrapht.graph.AbstractBaseGraph;

public class ScaleUpGrid {
	

	private static final int STEP_SIZE = 5;
	private int repeats;
	private int minsize;
	private int maxsize;
	private int maxtargetval;
	private int resources;
	private int nrOfSources;
	private int nrOfTargets;

	
	
	public ScaleUpGrid(int repeats, int minsize, int maxsize, int maxtargetval,
			int resources, int nrOfSources, int nrOfTargets) {
		super();
		this.repeats = repeats;
		this.minsize = minsize;
		this.maxsize = maxsize;
		this.maxtargetval = maxtargetval;
		this.resources = resources;
		this.nrOfSources = nrOfSources;
		this.nrOfTargets = nrOfTargets;
	}

	public void run() {
		
		warmup();
		System.out.println("Warmup done.");
		MeasureSet set = new MeasureSet();
		for(int size=minsize;size<=maxsize;size+=STEP_SIZE){
			System.out.println("GRID size: "+ size);
			for(int rep=0;rep<repeats;rep++){
				AbstractBaseGraph<INode, ExtDWE> graph = GraphGenerator.getGridGraph(size, size, false, nrOfSources, nrOfTargets, maxtargetval, model.Configuration.randomGenerator);
				UrbanSecProblem uspObj = new UrbanSecProblem(resources, graph);
				Rugged ruggedObj = new Rugged(uspObj);
				set.add(ruggedObj.getMeasure());
				try {
					ruggedObj.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Measure done.");
		writeResults(set);
		System.out.println("END.");
	}

	private void writeResults(MeasureSet set) {
		String setout = set.toString("grid"+repeats);
		
		File cd = new File(".");
		String path = cd.getAbsolutePath().substring(0, cd.getAbsolutePath().length()-1);
		File f = new File(path+"output/grid"+"r"+resources+"-"+System.currentTimeMillis()+".m");
		
		StringBuilder sb= new StringBuilder();
		sb.append("clear all;close all;\n\n");
		sb.append("rep="+ repeats+";\n");
		sb.append("minsize="+ minsize+";\n");
		sb.append("maxsize="+ maxsize+";\n");
		sb.append("resources="+resources+";\n");
		sb.append("step="+ STEP_SIZE+";\n\n");
		
		FileWriter fstream;
 
		try {
			fstream = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(sb.toString()+setout);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Results written to: "+f.getAbsolutePath());
	}

	private void warmup() {
		for(int rep=0;rep<10;rep++){
			AbstractBaseGraph<INode, ExtDWE> graph = GraphGenerator.genFullyConnectedGraph(5,10);
			UrbanSecProblem uspObj = new UrbanSecProblem(resources, graph);
			Rugged ruggedObj = new Rugged(uspObj);
			try {
				ruggedObj.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
