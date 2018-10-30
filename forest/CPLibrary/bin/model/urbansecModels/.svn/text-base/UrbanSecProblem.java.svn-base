package model.urbansecModels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class UrbanSecProblem {
	private ActionProfile actProfile;
	private int numDefenderResources;
	private AbstractBaseGraph<INode, ExtDWE> graph;
	private List<INode> listTargetNodes;
	
	private INode source = null;
	private INode virtualTarget = null;
	//Limits problem to one source --- make virtual source if necessary.
	//All id's from 1 to |N|, 1 to |E|		
	
	public UrbanSecProblem() {
		this(0);
	}
	public UrbanSecProblem(int numDefenderResources) {
		this(numDefenderResources,new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class));
	}
	
	public UrbanSecProblem(int numDefenderResources,
			AbstractBaseGraph<INode, ExtDWE> graph) {
		super();
		this.numDefenderResources = numDefenderResources;
		this.actProfile = new ActionProfile();
		this.graph = graph;
		this.listTargetNodes = new ArrayList<INode>();
	}
	
	public int getNumDefenderResources() {
		return numDefenderResources;
	}
	public void setNumDefenderResources(int numDefenderResources) {
		this.numDefenderResources = numDefenderResources;
	}
	public ActionProfile getActProfile() {
		return actProfile;
	}
	
	public void addNode(Node n) {
		this.graph.addVertex(n);
	}
	public void addEdge(INode n1, INode n2){
		this.graph.addEdge(n1, n2);
	}
	
	public INode getVirtualTarget() throws MalformedGraphException {
		if ( this.virtualTarget != null ) {
			return this.virtualTarget;
		}
		for ( Iterator<INode> nodeIter = this.graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.VIRTUAL_TARGET ) {
				this.virtualTarget = curNode;
				return this.virtualTarget;
			}
		}
		throw new MalformedGraphException();
	}
	
	public INode getSource() throws MalformedGraphException {
		if ( this.source != null ) {
			return this.source;
		}
		for ( Iterator<INode> nodeIter = this.graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.SOURCE ) {
				this.source = curNode;
				return this.source;
			}
		}
		throw new MalformedGraphException();
	}
	
	public List<INode> getTargetNodesSet() throws MalformedGraphException {
		if ( this.listTargetNodes.size() == 0 ) {
			for ( Iterator<INode> nodeIter = this.graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
				INode curNode = nodeIter.next();
				if ( curNode.getType() == NODE_TYPE.TARGET ) {
					this.listTargetNodes.add(curNode);
				}
			}
		}
		if ( this.listTargetNodes.size() == 0)
			throw new MalformedGraphException();
		return this.listTargetNodes;
	}
	
	public void setGraph(AbstractBaseGraph<INode, ExtDWE> graph) {
		this.graph = graph;
	}
	
	public AbstractBaseGraph<INode, ExtDWE> getGraph() {		
		return graph;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if(listTargetNodes.size()==0) getTargetNodesSet();
		return "UrbanSecProblem [numDefenderResources=" + numDefenderResources
				+ ", graph=" + graph + ", listTargetNodes=" + listTargetNodes
				+ "]";
	}
	
	
}

