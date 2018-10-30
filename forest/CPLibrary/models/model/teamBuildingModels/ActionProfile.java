package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.graphutils.ExtDWE;


public class ActionProfile {
	public Map<ExtDWE,Double> allocationProbability;
	private List<DefenderAllocation> listDefenderAllocations;
	private List<AdversaryPath> listAdversaryPaths;
	List<List<Boolean>> dbldimIntersect;
	List<List<Double>> probIntersect;
	private Set<DefenderAllocation> hashDefenderAllocations;
	private Set<AdversaryPath> hashAdversaryPaths;
	
	public ActionProfile() {
		super();
		listDefenderAllocations = new ArrayList<DefenderAllocation>();
		listAdversaryPaths = new ArrayList<AdversaryPath>();
		dbldimIntersect = new ArrayList<List<Boolean>>();
		probIntersect = new ArrayList<List<Double>>();
		hashDefenderAllocations = new HashSet<DefenderAllocation>();
		hashAdversaryPaths = new HashSet<AdversaryPath>();
		this.allocationProbability = new HashMap<ExtDWE,Double>();
	}
	
	public int getNumberOfDefenderAllocations() {
		return listDefenderAllocations.size();
	}
	public int getNumberOfAdversaryPaths() {
		return listAdversaryPaths.size();
	}
	
	public List<List<Boolean>> getDbldimIntersect() {
		return this.dbldimIntersect;
	}
	public List<List<Double>> getProbIntersect() {
		return this.probIntersect;
	}
	
	public List<DefenderAllocation> getDefenderAllocations() {
		return listDefenderAllocations;
	}
	/**
	 * index is from 0
	 * @param index
	 * @return
	 */
	public DefenderAllocation getDefenderAllocation(int index) {
		return listDefenderAllocations.get(index);
	}

	public List<AdversaryPath> getAdversaryPaths() {
		return listAdversaryPaths;
	}
	/**
	 * index is from 0
	 * @param index
	 * @return
	 */
	public AdversaryPath getAdversaryPath(int index) {
		return listAdversaryPaths.get(index);
	}
	
	public Boolean getIntersect(int defAllocIndex, int advPathIndex) {
		return dbldimIntersect.get(defAllocIndex).get(advPathIndex);
	}
	public double getProbability(int defAllocIndex, int advPathIndex) {
		return probIntersect.get(defAllocIndex).get(advPathIndex);
	}
	
	public void updateProbability(TeamBuildingProblem team){
		for(ExtDWE edge : team.getGraph().edgeSet()){
			double probability = 1.0;
			double p=0;
			for(int i=0;i<team.getNumResourceTypes();i++){
				for(DefenderAllocation da : listDefenderAllocations){
					if(da.edgeMap.containsKey(edge)){
						p = da.edgeMap.get(edge)[i];
					}else{
						p=0;
					}
					probability=probability*(Math.pow(1-da.ResourceProbability.get(i), p));
				}
//				if(newda!=null){
//					if(newda.edgeMap.containsKey(edge)){
//						p = newda.edgeMap.get(edge)[i];
//					}else{
//						p=0;
//					}
//					probability=probability*(Math.pow(1-newda.ResourceProbability.get(i), p));
//				}
			}
			probability=1-probability;
			allocationProbability.put(edge, probability);
		}
	}
	
	
	public double getEdgeProbability(ExtDWE e, int resourceType){
		//return allocationProbability.get(e);
		double probability=1;
		for(DefenderAllocation da : listDefenderAllocations){
			if(da.contains(e)){
				int dist;
				for(int i=0;i<da.numResourceTypes;i++){
					if(resourceType ==i){
						dist=1;
					}else{
						dist=0;
					}
					probability=probability*(Math.pow(1-da.ResourceProbability.get(i), da.edgeMap.get(e)[i]+dist));
				}
				probability=1-probability;
			}
		}
		return probability;
	}
	public double getEdgeProbability(ExtDWE e){
		return allocationProbability.get(e);
		
	}
	/**
	 * advPathIndex is from 0
	 * @param advPathIndex
	 * @return
	 */
	public double getPayoffOfPathOnTarget(int advPathIndex) {
		return listAdversaryPaths.get(advPathIndex).getTargetPayoff();
	}
	
	private Boolean computeIntersect(DefenderAllocation da, AdversaryPath ap) {
		
//		System.out.println("1" + (da == null));
//		System.out.println("2" + (ap == null));	
//		System.out.println("3" + (ap.getPath() == null));
//		System.out.flush();
		
		for ( ExtDWE edge: ap.getPath()) {
			
			// Manish
//			if ( da.getAllocation() == null ) {
//				System.out.println("DA's allocation is null.");
//				System.out.flush();
//			}
			
			if ( da.getAllocation().contains(edge)) {
				return true;
			}
		}
		return false;
	}
	
	public double computeProbability(DefenderAllocation da, AdversaryPath ap) {
		double probOfCapture = 0;
		double probOfEscape = 1;
		for ( ExtDWE edge: ap.getPath()) {
			if ( da.getAllocation().contains(edge)) {
				//probOfCapture += probOfEscape*da.getProbability(edge);
				//probOfEscape = 1-probOfCapture;
				probOfEscape = probOfEscape*(1-da.getProbability(edge));
			}
		}
		probOfCapture=1-probOfEscape;
		return probOfCapture;
	}
	
	public boolean addDefenderAllocation(DefenderAllocation da, List<Boolean> zj, List<Double> Pj) throws RuntimeException {
		if ( this.contains(da) ) {
			return false;
		}
		if ( zj.size() != listAdversaryPaths.size() ) {
			throw new RuntimeException();
		}
		listDefenderAllocations.add(da);
		hashDefenderAllocations.add(da);
		dbldimIntersect.add(zj);
		probIntersect.add(Pj);
		return true;
	}
	public boolean addDefenderAllocation(DefenderAllocation da) {
		if ( this.contains(da) ) {
			return false;
		}
		List<Boolean> zj = new ArrayList<Boolean>();
		List<Double> Pj = new ArrayList<Double>();
		for (int i=0; i<listAdversaryPaths.size(); i++) {
			zj.add(computeIntersect(da, listAdversaryPaths.get(i)));
			Pj.add(computeProbability(da, listAdversaryPaths.get(i)));
		}
		return addDefenderAllocation(da, zj, Pj);		
	}
	
	public boolean addAdversaryPath(AdversaryPath ap, List<Boolean> zi, List<Double> Pi) throws RuntimeException {
		if (this.contains(ap)) {
			return false;
		}
		if ( zi.size() != listDefenderAllocations.size() ) {
			throw new RuntimeException(); 
		}
		listAdversaryPaths.add(ap);
		hashAdversaryPaths.add(ap);
		
		for ( int i=0; i<zi.size(); i++){
			dbldimIntersect.get(i).add(zi.get(i));
			probIntersect.get(i).add(Pi.get(i));
		}
		return true;
	}
	public boolean addAdversaryPath(AdversaryPath ap) {
		// Manish
//		System.out.println("5: " + (ap == null));
		
		if ( this.contains(ap) ) {
			return false;
		}
		List<Boolean> zi = new ArrayList<Boolean>();
		List<Double> Pi = new ArrayList<Double>();
		
		for (int i=0; i<listDefenderAllocations.size(); i++) {
			zi.add(computeIntersect(listDefenderAllocations.get(i),ap));
			Pi.add(computeProbability(listDefenderAllocations.get(i),ap));
		}
		return addAdversaryPath(ap, zi, Pi);		
	}

	/**
	 * deletes the defender Allocation in the index'th place of setDefenderAllocations
	 * Indices change after this.
	 * @param index
	 */
	
	public void deleteAllDefenderAllocations(){
		for(int index=0;index<this.getNumberOfDefenderAllocations();index++){
			DefenderAllocation da = this.getDefenderAllocation(index);
			this.listDefenderAllocations.remove(index);
			this.hashDefenderAllocations.remove(da);
			// remove index'th row from dbldimIntersect
			this.dbldimIntersect.remove(index);	
		}
	}
	public void deleteDefenderAllocation(int index){
		// delete the defender allocation for the setDefenderAllocations
		// update intersect array
		// update hashSet
		DefenderAllocation da = this.getDefenderAllocation(index);
		this.listDefenderAllocations.remove(index);
		this.hashDefenderAllocations.remove(da);
		// remove index'th row from dbldimIntersect
		this.dbldimIntersect.remove(index);		
	}
	/**
	 * Finds the index and then calls the other function - deleteDefenderAllocation(index);
	 * @param da
	 */
	public void deleteDefenderAllocation(DefenderAllocation da) {
		this.deleteDefenderAllocation(this.listDefenderAllocations.indexOf(da));
	}

	public void deleteAdversaryPath(AdversaryPath ap) {
		this.deleteAdversaryPath(this.listAdversaryPaths.indexOf(ap));
	}
	public void deleteAdversaryPath(int index){
		// delete the defender allocation for the setAdversaryPaths
		// update intersect array
		// update hashSet
		AdversaryPath ap = this.getAdversaryPath(index);
		this.listAdversaryPaths.remove(index);
		this.hashAdversaryPaths.remove(ap);
		// remove index'th column from dbldimIntersect
		for ( List<Boolean> zi : dbldimIntersect) {
			zi.remove(index);
		}
		for ( List<Double> zi : probIntersect) {
			zi.remove(index);
		}
	}
	
	public boolean contains(AdversaryPath ap) {
		return hashAdversaryPaths.contains(ap);
	}
	public boolean contains(DefenderAllocation da) {
		return hashDefenderAllocations.contains(da);
	}
}
