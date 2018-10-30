package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.graphutils.ExtDWE;

public class DefenderAllocation implements Iterable<ExtDWE> {
	public static int numResourceTypes;
	public static List<Double> ResourceProbability;
	private Set<ExtDWE> allocation;
	private Map<ExtDWE,Double> allocationProbability;
	private Map<String, List<ExtDWE>> resourceMap;
	public Map<ExtDWE,int[]> edgeMap;
	
	public String toString() {
		return allocation.toString();
	}
	
	public DefenderAllocation() {
		super();
		this.allocation = new HashSet<ExtDWE>();
		this.resourceMap = new HashMap<String, List<ExtDWE>>();
		this.edgeMap = new HashMap<ExtDWE,int[]>();
		this.allocationProbability = new HashMap<ExtDWE,Double>();
	}
	
	public DefenderAllocation(Set<ExtDWE> allocation) {
		super();
		this.allocation = allocation;
	}
	
	public DefenderAllocation(List<ExtDWE> allocation) {
		super();
		this.allocation = new HashSet<ExtDWE>();
		for ( ExtDWE e : allocation) {
			this.allocation.add(e);
		}
	}
	public static void setResources(int resources){
		numResourceTypes=resources;
	}
	public boolean contains(ExtDWE e) {
		return (this.allocation.contains(e));
	}
	public int allocationSize(){
		return this.resourceMap.size();
	}
	public int numberEdgeCoverage(){
		return this.allocation.size();
	}
	public Set<ExtDWE> getAllocation() {
		return allocation;
	}
	public Map<String, List<ExtDWE>> getResourceMap(){
		return resourceMap;
	}
	
	public List<ExtDWE> getAllocationResource(int type, int resource){
		return resourceMap.get("Resource"+(type)+","+(resource));
	}

	public void setAllocation(Set<ExtDWE> allocation) {
		this.allocation = allocation;
	}
	
	public boolean addEdgeToAllocation(ExtDWE e){
		return allocation.add(e);
	}
	public boolean removeEdgeFromAllocation(ExtDWE e){
		return allocation.remove(e);
	}
	public boolean addEdgetoResource(ExtDWE e, String resource){
		if(resourceMap.containsKey(resource)){
			return resourceMap.get(resource).add(e);
		}else{
			List<ExtDWE> edges = new ArrayList<ExtDWE>();
			edges.add(e);
			resourceMap.put(resource,edges);
			return true;
		}
	}
	
	public boolean ResourceCoversEdge(ExtDWE e, String resource){
		if(resourceMap.containsKey(resource)){
			return resourceMap.get(resource).contains(e);
		}
		return false;
	}
	public void updateEdgeProbability(ExtDWE edge){
		double probability = 1.0;
		for(int i=0;i<numResourceTypes;i++){
			probability=probability*(Math.pow(1-ResourceProbability.get(i), edgeMap.get(edge)[i]));
		}
		probability=1-probability;
		allocationProbability.put(edge, probability);
	}
	
	public void addResourcetoEdge(ExtDWE e, int index){
		if(edgeMap.containsKey(e)){
			edgeMap.get(e)[index]++;
			//edgeMap.put(e,)
		}else{
			int[] resources = new int[numResourceTypes];
			for(int i=0;i<resources.length;i++){
				resources[i]=0;
			}
			resources[index]++;
			edgeMap.put(e,resources);
		}
		updateEdgeProbability(e);
	}
	
	public void updateProbability(){
		for(ExtDWE edge : edgeMap.keySet()){
			double probability = 1.0;
			for(int i=0;i<numResourceTypes;i++){
				probability=probability*(Math.pow(1-ResourceProbability.get(i), edgeMap.get(edge)[i]));
			}
			probability=1-probability;
			allocationProbability.put(edge, probability);
		}
	}
	
//	public boolean addProbability(ExtDWE e, Double probability){
//		if( allocationProbability.containsKey(e)){
//			double prob = allocationProbability.get(e)+probability;
//			if(prob > 1){ prob = 1; }
//			
//			allocationProbability.put(e, prob);
//		}else{
//			allocationProbability.put(e, probability);
//		}
//		return true;
//	}
//	public int size() {
//		return allocation.size();
//	}
	public double getProbability(ExtDWE e){
		if(allocationProbability.containsKey(e)){
			return allocationProbability.get(e);
		}else{
			return 0.0;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allocation == null) ? 0 : allocation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefenderAllocation other = (DefenderAllocation) obj;
		if (allocation == null) {
			if (other.allocation != null)
				return false;
		} else if (!allocation.equals(other.allocation))
			return false;
		return true;
	}

	@Override
	public Iterator<ExtDWE> iterator() {
		// TODO Auto-generated method stub
		return allocation.iterator();		
	}
	
}
