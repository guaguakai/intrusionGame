package model.urbansecModels;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.graphutils.ExtDWE;

public class DefenderAllocation implements Iterable<ExtDWE> {
	private Set<ExtDWE> allocation;
	
	public String toString() {
		return allocation.toString();
	}
	
	public DefenderAllocation() {
		super();
		this.allocation = new HashSet<ExtDWE>();
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

	public boolean contains(ExtDWE e) {
		return (this.allocation.contains(e));
	}
	
	public Set<ExtDWE> getAllocation() {
		return allocation;
	}

	public void setAllocation(Set<ExtDWE> allocation) {
		this.allocation = allocation;
	}
	
	public boolean addEdgeToAllocation(ExtDWE e){
		return allocation.add(e);
	}
	
	public int size() {
		return allocation.size();
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
