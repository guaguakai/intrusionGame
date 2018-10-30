package model.urbansecModels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.graphutils.ExtDWE;


public class ActionProfile {
	private List<DefenderAllocation> listDefenderAllocations;
	private List<AdversaryPath> listAdversaryPaths;
	List<List<Boolean>> dbldimIntersect;
	private Set<DefenderAllocation> hashDefenderAllocations;
	private Set<AdversaryPath> hashAdversaryPaths;
	
	public ActionProfile() {
		super();
		listDefenderAllocations = new ArrayList<DefenderAllocation>();
		listAdversaryPaths = new ArrayList<AdversaryPath>();
		dbldimIntersect = new ArrayList<List<Boolean>>();
		hashDefenderAllocations = new HashSet<DefenderAllocation>();
		hashAdversaryPaths = new HashSet<AdversaryPath>();
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
	
	public boolean addDefenderAllocation(DefenderAllocation da, List<Boolean> zj) throws RuntimeException {
		if ( this.contains(da) ) {
			return false;
		}
		if ( zj.size() != listAdversaryPaths.size() ) {
			throw new RuntimeException();
		}
		listDefenderAllocations.add(da);
		hashDefenderAllocations.add(da);
		dbldimIntersect.add(zj);
		return true;
	}
	public boolean addDefenderAllocation(DefenderAllocation da) {
		if ( this.contains(da) ) {
			return false;
		}
		List<Boolean> zj = new ArrayList<Boolean>();
		for (int i=0; i<listAdversaryPaths.size(); i++) {
			zj.add(computeIntersect(da, listAdversaryPaths.get(i)));
		}
		return addDefenderAllocation(da, zj);		
	}
	
	public boolean addAdversaryPath(AdversaryPath ap, List<Boolean> zi) throws RuntimeException {
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
		for (int i=0; i<listDefenderAllocations.size(); i++) {
			zi.add(computeIntersect(listDefenderAllocations.get(i),ap));
		}
		return addAdversaryPath(ap, zi);		
	}

	/**
	 * deletes the defender Allocation in the index'th place of setDefenderAllocations
	 * Indices change after this.
	 * @param index
	 */
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
	}
	
	public boolean contains(AdversaryPath ap) {
		return hashAdversaryPaths.contains(ap);
	}
	public boolean contains(DefenderAllocation da) {
		return hashDefenderAllocations.contains(da);
	}
}
