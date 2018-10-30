package model.cyber;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.AbstractBaseGraph;

import model.graphutils.ExtDWE;
import model.graphutils.HNode;
import model.graphutils.HNode.NODE_TYPE;

public class host {
	
	public int time;
	
	public List<HNode> IDS;
	public List<HNode> Malware;
	public List<HNode> Data;
	

	
	List<host> neighbors;
	
	public host(){
		IDS = new ArrayList<HNode>();
		Malware = new ArrayList<HNode>();
		Data = new ArrayList<HNode>();
		
	}
	
	public void createNodes(int time){
		this.time = time;
		for(int i=0;i<time;i++){
			IDS.add(new HNode(NODE_TYPE.IDS));
			Malware.add(new HNode(NODE_TYPE.MALWARE));
			Data.add(new HNode(NODE_TYPE.DATA));
		}
	}
	public void addNodes(AbstractBaseGraph graph){
		for(int i=0;i<time;i++){
			graph.addVertex(IDS.get(i));
			graph.addVertex(Malware.get(i));
			graph.addVertex(Data.get(i));
		}
		
		for(int i=0;i<time-1;i++){
			ExtDWE e1 = (ExtDWE) graph.addEdge(IDS.get(i), IDS.get(i+1));
			ExtDWE e2 = (ExtDWE) graph.addEdge(IDS.get(i), Malware.get(i+1));
			ExtDWE e3 = (ExtDWE) graph.addEdge(Malware.get(i), Malware.get(i+1));
			ExtDWE e4 = (ExtDWE) graph.addEdge(Malware.get(i), Data.get(i+1));
			ExtDWE e5 = (ExtDWE) graph.addEdge(Data.get(i), Data.get(i+1));
		}
		
		
	}
	public void buildNeighborhood(AbstractBaseGraph graph){
		for(host n : neighbors){
			for(int i=0;i<time-1;i++){
				ExtDWE e1 = (ExtDWE) graph.addEdge(IDS.get(i), n.IDS.get(i+1));
				ExtDWE e2 = (ExtDWE) graph.addEdge(IDS.get(i), n.Malware.get(i+1));
				ExtDWE e3 = (ExtDWE) graph.addEdge(Malware.get(i), n.Malware.get(i+1));
				ExtDWE e4 = (ExtDWE) graph.addEdge(Malware.get(i), n.Data.get(i+1));
				ExtDWE e5 = (ExtDWE) graph.addEdge(Data.get(i), n.Data.get(i+1));
		
			}
		}
	}

}
