package model.urbansecModels;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.Node;

public class AdversaryPath implements Iterable<ExtDWE>{
	private INode target;
	private Set<ExtDWE> path;
	
	public AdversaryPath() {
		super();
		this.path = new HashSet<ExtDWE>();
	}
	public AdversaryPath(INode target) {
		this();
		this.target = target;
	}
	
	public AdversaryPath(INode target, List<ExtDWE> path) {
		this();
		this.target = target;		
		for ( ExtDWE edge : path) {
			this.path.add(edge);
		}
	}
	
	private ExtDWE edgeWithTarget(Set<ExtDWE> setEdges, INode t) {
		for (ExtDWE e : setEdges ) {
			if ( e.gettarget().equals(t) ) {
				return e;
			}
		}
		return null;
	}
	
	public void removeCycleFromPath(INode source) {		
		INode pathTarget = this.target;
		Set<ExtDWE> validEdgesInPath = new HashSet<ExtDWE>();
		ExtDWE retEdge = this.edgeWithTarget(this.path, pathTarget);
		validEdgesInPath.add(retEdge);
		
		while ( !retEdge.getsource().equals(source) ) {
			pathTarget = retEdge.getsource();
			retEdge = this.edgeWithTarget(this.path, pathTarget);
			validEdgesInPath.add(retEdge);
		}
		
		// validEdgesInPath is the valid path now
		this.path = validEdgesInPath;		
	}
	
	public AdversaryPath(INode target, Set<ExtDWE> path) {
		super();
		this.target = target;
		this.path = path;
	}
	public String toString() {
		return path.toString();
	}
	public boolean isInPath(ExtDWE e) {
		if ( e == null ) {
			return false;
		}
		for(Iterator<ExtDWE> itExt = this.path.iterator(); itExt.hasNext(); ) {
			if ( itExt.next().getId() == e.getId() ) {
				return true;
			}
		}
		return false;
	}	
	public INode getTarget() {
		return target;
	}
	public void setTarget(INode target) {
		this.target = target;
	}
	public int getTargetPayoff() {
		return target.getPayoff();
	}
	
	public Set<ExtDWE> getPath() {
		return path;
	}
	public void setPath(Set<ExtDWE> path) {
		this.path = path;
	}
	public boolean addEdgeToPath(ExtDWE edge) {
		return this.path.add(edge);
	}
	public int size() {
		return path.size();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		AdversaryPath other = (AdversaryPath) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}
	@Override
	public Iterator<ExtDWE> iterator() {
		// TODO Auto-generated method stub
		return path.iterator();
	}
}
