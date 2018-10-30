package osmutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DataContainer {
	
	protected Map<String, ExtNode> nodesById;
	protected Map<String, ExtPath> pathsById;
	protected Map<String, List<ExtPath>> pathsByNodeId;
	
	public DataContainer() {
		this.nodesById = new HashMap<String, ExtNode>();
		this.pathsById = new HashMap<String, ExtPath>();
		this.pathsByNodeId = new HashMap<String, List<ExtPath>>();
	}
	
	public Collection<ExtPath> getPaths() {
		return pathsById.values();
	}
	
	public Collection<ExtNode> getNodes() {
		return nodesById.values();
	}

	public ExtPath getPathById(String id) {
		return pathsById.get(id);
	}
	
	public ExtNode getNodeById(String id) {
		return nodesById.get(id);
	}
	
	public void add(ExtPath path) {
		
		if(!pathsById.containsKey(path.getId())) {
			
			// Add path
			pathsById.put(path.getId(), path);
			
			// Get terminal nodes
			ExtNode from = path.getFrom();
			ExtNode to = path.getTo();
			
			// Add terminal nodes
			add(from);
			add(to);
			
			// Add link
			getPathsByNodeId(from.getId()).add(path);
			getPathsByNodeId(to.getId()).add(path);
			
		}
		
	}
	
	public List<ExtPath> getPathsByNodeId(String id) {
		
		List<ExtPath> paths = pathsByNodeId.get(id);
		
		// Check and possibly create new list
		if (paths == null) {
			paths = new LinkedList<ExtPath>();
			pathsByNodeId.put(id, paths);
		}
		
		return paths;
	}
	
	public void add(ExtNode node) {
		
		if(!nodesById.containsKey(node.getId())) {
			nodesById.put(node.getId(), node);
		}
		
	}
	
	public void remove(ExtPath path) {
		
		if (pathsById.containsKey(path.getId())) {
			
			// Remove path
			pathsById.remove(path.getId());
			
			// Remove link from nodes 
			getPathsByNodeId(path.getFrom().getId()).remove(path);
			getPathsByNodeId(path.getTo().getId()).remove(path);
		}
	}
	
	public void remove(ExtNode node) {
		
		if (nodesById.containsKey(node.getId())) {
			
			// Remove node
			nodesById.remove(node.getId());
			
			// Remove all paths linked with this node
			List<ExtPath> paths = getPathsByNodeId(node.getId());
			
			for (ExtPath path : paths) {
				pathsById.remove(path.getId());
				
				if(path.getFrom().equals(node)) {
					getPathsByNodeId(path.getTo().getId()).remove(path);
				} else {
					getPathsByNodeId(path.getFrom().getId()).remove(path);
				}
			}
			
			pathsByNodeId.remove(node.getId());
			
		}
		
	}
	
	public DataContainer merge(DataContainer other) {
		if (other.getElementCount() > getElementCount()) {
			
			for (ExtNode node : getNodes()) {
				other.add(node);
			}
			
			for (ExtPath path : getPaths()) {
				other.add(path);
			}
			
			return other;
		} else {
			
			for (ExtNode node : other.getNodes()) {
				add(node);
			}
			
			for (ExtPath path : other.getPaths()) {
				add(path);
			}
			
			return this;
		}
	}
	
	public int getElementCount() {
		return nodesById.size() + pathsById.size();
	}
	
	public boolean contains(ExtNode node) {
		return nodesById.containsKey(node.getId());
	}
	
	public boolean contains(ExtPath path) {
		return pathsById.containsKey(path.getId());
	}
	
	public void replace(DataContainer container) {
		this.nodesById = container.nodesById;
		this.pathsById = container.pathsById;
		this.pathsByNodeId = container.pathsByNodeId;
	}
}
