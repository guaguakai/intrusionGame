package model.teamBuildingModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import teamBuildingSolvers.CompactProblem;
import teamBuildingSolvers.CompactOracle.CompactDefender;
import teamBuildingSolvers.CompactOracle.MatrixCompactDefender;
import teamBuildingSolvers.DefenderOracle.BRDefender;
import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;

public class CompactAllocation {
	
	public List<Double> resources;
	
	public static MatrixCompactDefender brDef;
	public TeamBuildingProblem team;
	public static int[][] distances;
	public static List<INode> targets;
	
	public Map<String, List<NodePair>> DA;
	
	public Map<String,List<INode>> defenderAlloc = new HashMap<String,List<INode>>();
	public Map<INode,List<Double>> defenderAllocation;
	public Map<INode,List<INode>> overflowAllocation;
	public Map<INode,Double> defenderCoverage;
	
	public Map<NodePair, List<Double>> intersectionCoverage;
	public List<NodePair> coveredIntersections;
	public Map<NodePair,Map<Integer,List<NodePair>>> overflowMap;
	
	public CompactAllocation(){
		DA = new HashMap<String, List<NodePair>>();
		
		defenderAllocation = new HashMap<INode, List<Double>>(); //assigned resources
		overflowAllocation = new HashMap<INode, List<INode>>(); //overflow of each target
		defenderCoverage = new HashMap<INode, Double>();	//actual coverage of target
		
		coveredIntersections = new ArrayList<NodePair>();
		intersectionCoverage = new HashMap<NodePair, List<Double>>();
		overflowMap = new HashMap<NodePair, Map<Integer, List<NodePair>>>();
	}
	
	public void addAllocation(INode target, List<Double> resources){
		defenderAllocation.put(target, resources);
	}
	
	public void addCoverage(INode target, double coverage){
		defenderCoverage.put(target,coverage);
	}
	
	public boolean addResource(String resource, NodePair n){
		if(n==null){ return false;}
		if(DA.containsKey(resource)){
			if(!DA.get(resource).contains(n)){
				return DA.get(resource).add(n);
			}else{
				return false;
			}
		}else{
			List<NodePair> coverage = new ArrayList<NodePair>();
			coverage.add(n);
			DA.put(resource,coverage);
			return true;
		}
	}
	
	public void coverTarget(INode n, int k){
		if(defenderAllocation.containsKey(n)){
			defenderAllocation.get(n).set(k, defenderAllocation.get(n).get(k)+1);
		}else{
			List<Double> resources = new ArrayList<Double>();
			for(int i=0;i<team.getNumResourceTypes();i++){
				if(i==k){
					resources.add(1.0);
				}else{
					resources.add(0.0);
				}
			}
			defenderAllocation.put(n,resources);
			
		}
	}
	
	
	public void addIntersectionCoverage(NodePair n){
		coveredIntersections.add(n);
	}
	
	public void coverIntersection(NodePair n, int k){
		if(intersectionCoverage.containsKey(n)){
			intersectionCoverage.get(n).set(k, intersectionCoverage.get(n).get(k)+1);
		}else{
			List<Double> resources = new ArrayList<Double>();
			for(int i=0;i<team.getNumResourceTypes();i++){
				if(i==k){
					resources.add(1.0);
				}else{
					resources.add(0.0);
				}
			}
			intersectionCoverage.put(n,resources);
			
		}
	}
	
	public double getCoverage(INode target){
		return defenderCoverage.get(target);
	}
	public int strToType(String resource){
		return Integer.parseInt(resource.split(",")[1]);
	}
	public boolean feasibleAllocation(CompactDefender def) throws LPSolverException{
		boolean infeasible = false;
		List<CompactPath> paths = new ArrayList<CompactPath>();
		List<INode> nodes = new ArrayList<INode>();
		
		for( String resource : defenderAlloc.keySet()){
			int type = strToType(resource);
			paths.clear();
			nodes.clear();
			for( CompactPath ap : def.adversaryPaths){
				INode t = ap.target;
				if(defenderAlloc.get(resource).contains(t)){
					paths.add(ap);
					nodes.add(t);
				}
			}
			if(paths.size()>0){
			PathCoverage f  = new PathCoverage(team, paths, type);
			f.loadProblem();
			f.writeProb("test");
			f.solve();
			f.writeSol("testsol.lp");
			
			
			double val = f.getLPObjective();
			if(f.getLPObjective()<paths.size()){
				def.addContraints(nodes,  (int) f.getLPObjective(), type);
				infeasible = true;
			}
			f.end();
			}
		}
		
		
		
		return infeasible;
	}
	public boolean checkAllocation(PathConstraint constraints) throws LPSolverException{
		boolean infeasible = false;
		for(String s : DA.keySet()){
			
				List<NodePair> lst = DA.get(s);
				List<CompactPath> paths = new ArrayList<CompactPath>();
				
				for(NodePair np : lst){
					for(CompactPath p : np.pathset){
						if(p!=null && !paths.contains(p)){
							paths.add(p);
						}
					}
				}
				
		
				PathCoverage f  = new PathCoverage(team, paths);
				f.loadProblem();
				f.writeProb("test");
				f.solve();
				f.writeSol("testsol.lp");
				//System.out.println(f.getLPObjective());
				double val = f.getLPObjective();
				if(f.getLPObjective()<paths.size()){
					constraints.add(s, lst, f.getLPObjective());
					infeasible = true;
				}
				//System.out.println(f.getLPObjective());
				
		}
		return infeasible;
		
	}
	public String toString(){
		String result ="";
		
		for(INode n : defenderCoverage.keySet()){
			result += "[" + n.getId() + ":" + defenderCoverage.get(n) + "]";
		}
		
		return result;
	}
	
	public DefenderAllocation toSimpleDefenderAllocation(){
		DefenderAllocation da = new DefenderAllocation();
		
		Set<INode> overflow = new HashSet<INode>();
		
		for(int k=0;k<team.getNumResourceTypes();k++){
			
			for(INode target : defenderAllocation.keySet()){
				double resources = defenderAllocation.get(target).get(k); //number of resources assigned to target
				int t = targets.indexOf(target);
				
				while(resources>0){ //for each resource of type k assigned to this intersection
					boolean added = false;
					
					for(INode n : overflowAllocation.get(target)){
						if(!defenderAllocation.keySet().contains(n) && !overflow.contains(n)){	
						//for every intersection implicitly covered through another and not already covered
							
							int i = targets.indexOf(n);
							if( distances[t][i]<team.ResourceCoverage.get(k)){
								overflow.add(n);
								
								for(ExtDWE edge : n.intersectionDistances.get(nc)){ //add the intersection edges
									if(edge.equals(null)){
										System.out.println("sad");
									}
									da.addEdgeToAllocation(edge);
									da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
									da.addResourcetoEdge(edge, k);
								}
								added=true;
							}
						}
					}
					if(!added){
						double d = team.ResourceCoverage.get(k);
						List<ExtDWE> edges2 = n.getEdges(d);
						for(ExtDWE edge :edges2){
							if(edge.equals(null)){
								System.out.println("sad");
							}
							da.addEdgeToAllocation(edge);
							da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
							da.addResourcetoEdge(edge, k);
						}
						
					}
					i++;
					resources--;
				}
			}
		}
		return da;
		
	}
	
	public DefenderAllocation toDefenderAllocation(){
		DefenderAllocation da = new DefenderAllocation();
		
		Set<NodePair> overflow = new HashSet<NodePair>();
		Set<NodePair> allocations = intersectionCoverage.keySet();
		
		
		for(int k=0;k<team.getNumResourceTypes();k++){
			int i=0;
			
			for(NodePair n : allocations){
				double resources = intersectionCoverage.get(n).get(k);
				
				while(resources>0){ //for each resource of type k assigned to this intersection
					boolean added = false;
					if(!n.self){
						double coverage = team.ResourceCoverage.get(k);
							for(NodePair nc : coveredIntersections){
								if(!allocations.contains(nc) && !overflow.contains(nc)){	//for every intersection implicitly covered through another
									double d = Math.abs(n.intersectionSize.get(nc));
									if(d<coverage && d>=0){
										coverage=coverage-n.intersectionSize.get(nc);
										overflow.add(nc);
										List<ExtDWE> edges = n.intersectionDistances.get(nc);
//										if(edges.isEmpty()){
//											
//										}else{
											for(ExtDWE edge : edges){ //add the intersection edges
												if(edge.equals(null)){
													System.out.println("sad");
												}
												if(!da.contains(edge)){
													da.addEdgeToAllocation(edge);
												}
												if(!da.ResourceCoversEdge(edge, "Resource"+(i+1)+","+(k+1))){
													da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
													da.addResourcetoEdge(edge, k);
												}
												
											}
											
											added=true;
										}
//									}
								}
							}
						
					}
					if(!added){
						double d = team.ResourceCoverage.get(k);
						List<ExtDWE> edges2 = n.getEdges(d);
						for(ExtDWE edge :edges2){
							if(edge.equals(null)){
								System.out.println("sad");
							}
							da.addEdgeToAllocation(edge);
							da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
							da.addResourcetoEdge(edge, k);
						}
						
					}
					i++;
					resources--;
				}
			}
		}
		return da;
		
	}
	public DefenderAllocation getDefenderAllocation(){
		DefenderAllocation da = new DefenderAllocation();
		
		Set<NodePair> overflow = new HashSet<NodePair>();
		Set<NodePair> allocations = intersectionCoverage.keySet();
		
		
		for(int k=0;k<team.getNumResourceTypes();k++){
			int i=0;
			
			for(NodePair n : allocations){
				double resources = intersectionCoverage.get(n).get(k);
				double coverage = team.ResourceCoverage.get(k);
				
				while(resources>0){ //for each resource of type k assigned to this intersection
					boolean added = false;
					List<NodePair> covered = new ArrayList<NodePair>();
					
					if(!n.self){
					for(NodePair possiblePairs : overflowMap.get(n).get(k)){
						if(coveredIntersections.contains(possiblePairs)){
							covered.add(possiblePairs);
						}
					}
							for(NodePair nc : covered){
								if(!allocations.contains(nc) && !overflow.contains(nc)){	//for every intersection implicitly covered through another
									double d = Math.abs(n.intersectionSize.get(nc));
									
									if(d<coverage && d>=0){
										coverage=coverage-n.intersectionSize.get(nc);
										overflow.add(nc);
										List<ExtDWE> edges = n.intersectionDistances.get(nc);

											for(ExtDWE edge : edges){ //add the intersection edges
												if(edge.equals(null)){
													System.out.println("sad");
												}
												if(!da.contains(edge)){
													da.addEdgeToAllocation(edge);
												}
												if(!da.ResourceCoversEdge(edge, "Resource"+(i+1)+","+(k+1))){
													da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
													da.addResourcetoEdge(edge, k);
												}
												
											}
											
											added=true;
										}else{
											brDef.addConstraints(n, k);
										}

								}
							}
						
					}
					if(!added){
						double d = team.ResourceCoverage.get(k);
						List<ExtDWE> edges2 = n.getEdges(d);
						for(ExtDWE edge :edges2){
							if(edge.equals(null)){
								System.out.println("sad");
							}
							da.addEdgeToAllocation(edge);
							da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
							da.addResourcetoEdge(edge, k);
						}
						
					}
					i++;
					resources--;
				}
			}
		}
		return da;
		
	}
	public DefenderAllocation TODefenderAllocation(NodePair constrain, int resource){
		DefenderAllocation da = new DefenderAllocation();
		
		Set<NodePair> overflow = new HashSet<NodePair>();
		Set<NodePair> allocations = intersectionCoverage.keySet();
		
		
		for(int k=0;k<team.getNumResourceTypes();k++){
			int i=0;
			
			for(NodePair n : allocations){
				double resources = intersectionCoverage.get(n).get(k);
				boolean added = false;
				
				if(!n.self && !overflow.contains(n) ){
				while(resources>0){ //for each resource of type k assigned to this intersection
					added = false;
					List<NodePair> covered = new ArrayList<NodePair>();
					
					for(NodePair possiblePairs : overflowMap.get(n).get(k)){
						if(coveredIntersections.contains(possiblePairs)){
							covered.add(possiblePairs);
						}
						List<ExtDWE> edges = new ArrayList<ExtDWE>();
						if(n.getTreeCoverage(covered,edges)+2>team.ResourceCoverage.get(k)){
							//we have a proper solution
							for(ExtDWE edge : edges){ //add the intersection edges
								da.addEdgeToAllocation(edge);
								da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
								da.addResourcetoEdge(edge, k);
								added=true;
							}
						}else{
							constrain = n;
							resource = k;
							return null;
						}
					}}

					if(!added){
						double d = team.ResourceCoverage.get(k);
						List<ExtDWE> edges2 = n.getEdges(d);
						for(ExtDWE edge :edges2){
							if(edge.equals(null)){
								System.out.println("sad");
							}
							da.addEdgeToAllocation(edge);
							da.addEdgetoResource(edge, "Resource"+(i+1)+","+(k+1));
							da.addResourcetoEdge(edge, k);
						}
						
					}
					i++;
					resources--;
				}
			}
		}
		return da;
		
	}

}
