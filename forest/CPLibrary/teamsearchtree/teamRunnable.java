package examples;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import lpWrapper.LPSolverException;
import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.graphutils.INode.NODE_TYPE;
import model.teamBuildingModels.TeamBuildingProblem;
import teamBuildingSolvers.RuggedCutoff;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;

public class teamRunnable implements Runnable {

	// set coverages
	private List<Double> coverage = new ArrayList<Double>();
		
	private teamNode n;
	private TeamBuildingProblem teamObj;
	//private AbstractBaseGraph<INode, ExtDWE> graph = genTestGraph();
	
	//set to -100 because we would never get close to this negative of a value
	private double bound = -100;
	private boolean cutoffUsed = true;
		
	public teamRunnable(teamNode n, List<Double> coverage, TeamBuildingProblem teamObj, int cutoff){
		this.n = n;
		this.coverage = coverage;
		this.teamObj = teamObj;
		n.setCutoff(cutoff);
	}
	
	public teamRunnable(teamNode n, List<Double> coverage, int cutoff){
		this.n = n;
		this.coverage = coverage;
		n.setCutoff(cutoff);
	}
	
	public teamRunnable(teamNode n, List<Double> coverage, double bound){
		this.n = n;
		this.coverage = coverage;
		this.bound = bound;
	}
	
	@Override
	public void run() {
		
		//we are using incremented cutoff
		if(bound != -100){
			do {			
				long starttime = System.currentTimeMillis();
				teamObj = new TeamBuildingProblem(
						n.getResourceList(), coverage);
				//teamObj.setGraph(graph);
				genTestGraph2(teamObj);
				
				RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(teamObj, n.getCutoff() + 25);
				
				
					try {
						ruggedObj.run();
					} catch (MalformedGraphException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (LPSolverException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					cutoffUsed = ruggedObj.getcutoffUsed();
					System.out.println(cutoffUsed);
				n.setEvaluation(ruggedObj.getGameValue());
				if (cutoffUsed == true) {
					System.out.println("We done add more yo");
					if(n.getCutoff()<50)
						n.setCutoff(n.getCutoff() + 25);
					else
						n.setCutoff(n.getCutoff()+100);
				} else if (cutoffUsed == false) {
					n.setCutoff(ruggedObj.getNumberOfIterations());
					System.out.println("We done break yo from this");
					System.out.println(n.getName());
					break;
				}
				long endtime = System.currentTimeMillis();
				System.out.println(n.getName()+", "+n.getEvaluation()+", "+bound+", "+n.getCutoff()+" Runtime:"+(endtime-starttime));
			} while (n.getEvaluation() >= bound);
		}else{
			long starttime = System.currentTimeMillis();
			teamObj = new TeamBuildingProblem(
					n.getResourceList(), coverage);
			//teamObj.setGraph(graph);
			genTestGraph2(teamObj);
			
			RuggedCutoff ruggedObj = new RuggedCutoff(teamObj, 5);
			
			
				try {
					ruggedObj.run();
				} catch (MalformedGraphException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LPSolverException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			n.setEvaluation(ruggedObj.getGameValue());
			long endtime = System.currentTimeMillis();
			System.out.println("Team: "+n.getName()+" Eval: "+n.getEvaluation()+" Cutoff: "+n.getCutoff()+" Runtime:"+(endtime-starttime));
			n.setCutoff(n.getCutoff()+25);
			//System.out.println("Team: "+n.getName()+" Eval: "+n.getEvaluation()+" Cutoff: "+n.getCutoff());
		}
	}
	
	public teamNode getNode(){
		return n;
	}
	
	public boolean getCutoffUsed(){
		return cutoffUsed;
	}
	
	private static AbstractBaseGraph<INode, ExtDWE> genTestGraph(){
		
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		
		//Graph 5
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n8 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n9 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n10 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n11 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n12 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n13 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n14 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n15 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n16 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n17 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n18 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n19 = new Node(NODE_TYPE.TARGET, 5);
		Node n20 = new Node(NODE_TYPE.TARGET, 7);
		
		//add vertices
		graph.addVertex(n0);
		graph.addVertex(n1);
		graph.addVertex(n2);
		graph.addVertex(n3);
		graph.addVertex(n4);
		graph.addVertex(n5);
		graph.addVertex(n6);
		graph.addVertex(n7);
		graph.addVertex(n8);
		graph.addVertex(n9);
		graph.addVertex(n10);
		graph.addVertex(n11);
		graph.addVertex(n12);
		graph.addVertex(n13);
		graph.addVertex(n14);
		graph.addVertex(n15);
		graph.addVertex(n16);
		graph.addVertex(n17);
		graph.addVertex(n18);
		graph.addVertex(n19);
		graph.addVertex(n20);
		
		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		ExtDWE e2 = graph.addEdge(n0, n2);
		ExtDWE e3 = graph.addEdge(n0, n3);
		ExtDWE e4 = graph.addEdge(n0, n4);
		ExtDWE e5 = graph.addEdge(n1, n5);
		ExtDWE e6 = graph.addEdge(n1, n6);
		ExtDWE e7 = graph.addEdge(n2, n5);
		ExtDWE e8 = graph.addEdge(n2, n6);
		ExtDWE e9 = graph.addEdge(n2, n6);
		ExtDWE e10 = graph.addEdge(n3, n6);
		ExtDWE e11 = graph.addEdge(n3, n7);
		ExtDWE e12 = graph.addEdge(n3, n8);
		ExtDWE e13 = graph.addEdge(n4, n7);
		ExtDWE e14 = graph.addEdge(n4, n8);
		ExtDWE e15 = graph.addEdge(n5, n9);
		ExtDWE e16 = graph.addEdge(n6, n9);
		ExtDWE e17 = graph.addEdge(n6, n10);
		ExtDWE e18 = graph.addEdge(n7, n10);
		ExtDWE e19 = graph.addEdge(n8, n10);
		ExtDWE e20 = graph.addEdge(n8, n11);
		ExtDWE e21 = graph.addEdge(n9, n12);
		ExtDWE e22 = graph.addEdge(n9, n13);
		ExtDWE e23 = graph.addEdge(n9, n14);
		ExtDWE e24 = graph.addEdge(n10, n13);
		ExtDWE e25 = graph.addEdge(n10, n14);
		ExtDWE e26 = graph.addEdge(n11, n14);
		ExtDWE e27 = graph.addEdge(n11, n15);
		ExtDWE e28 = graph.addEdge(n12, n16);
		ExtDWE e29 = graph.addEdge(n12, n17);
		ExtDWE e30 = graph.addEdge(n13, n16);
		ExtDWE e31 = graph.addEdge(n13, n17);
		ExtDWE e32 = graph.addEdge(n14, n16);
		ExtDWE e33 = graph.addEdge(n14, n17);
		ExtDWE e34 = graph.addEdge(n14, n18);
		ExtDWE e35 = graph.addEdge(n15, n17);
		ExtDWE e36 = graph.addEdge(n15, n18);
		ExtDWE e37 = graph.addEdge(n16, n19);
		ExtDWE e38 = graph.addEdge(n16, n20);
		ExtDWE e39 = graph.addEdge(n17, n19);
		ExtDWE e40 = graph.addEdge(n17, n20);
		ExtDWE e41 = graph.addEdge(n18, n19);
		ExtDWE e42 = graph.addEdge(n18, n20);
		ExtDWE e43 = graph.addEdge(n1, n2);
		ExtDWE e44 = graph.addEdge(n3, n4);
		ExtDWE e45 = graph.addEdge(n6, n7);
		ExtDWE e46 = graph.addEdge(n7, n8);
		ExtDWE e47 = graph.addEdge(n9, n10);
		ExtDWE e48 = graph.addEdge(n10, n11);
		ExtDWE e49 = graph.addEdge(n12, n13);
		ExtDWE e50 = graph.addEdge(n14, n15);
		ExtDWE e51 = graph.addEdge(n16, n17);
		ExtDWE e52 = graph.addEdge(n17, n18);
		
		/*edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);
		edgeList.add(e6);
		edgeList.add(e7);
		edgeList.add(e8);
		edgeList.add(e9);
		edgeList.add(e10);
		edgeList.add(e11);
		edgeList.add(e12);
		edgeList.add(e13);
		edgeList.add(e14);
		edgeList.add(e15);
		edgeList.add(e16);
		edgeList.add(e17);
		edgeList.add(e18);
		edgeList.add(e19);
		edgeList.add(e20);
		edgeList.add(e21);
		edgeList.add(e22);
		edgeList.add(e23);
		edgeList.add(e24);
		edgeList.add(e25);
		edgeList.add(e26);
		edgeList.add(e27);
		edgeList.add(e28);
		edgeList.add(e29);
		edgeList.add(e30);
		edgeList.add(e31);
		edgeList.add(e32);
		edgeList.add(e33);
		edgeList.add(e34);
		edgeList.add(e35);
		edgeList.add(e36);
		edgeList.add(e37);
		edgeList.add(e38);
		edgeList.add(e39);
		edgeList.add(e40);
		edgeList.add(e41);
		edgeList.add(e42);
		edgeList.add(e43);
		edgeList.add(e44);
		edgeList.add(e45);
		edgeList.add(e46);
		edgeList.add(e47);
		edgeList.add(e48);
		edgeList.add(e49);
		edgeList.add(e50);
		edgeList.add(e51);
		edgeList.add(e52);
		
		return edgeList;*/
		
		return graph;
	}
	
	private static List<ExtDWE> genTestGraph2(TeamBuildingProblem uspObj) {

		/*
		 * //Graph 3_+1 Node n1 = new Node(NODE_TYPE.TARGET, 5); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n4 = new Node(NODE_TYPE.SOURCE,
		 * 0); Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0); Node n6 = new
		 * Node(NODE_TYPE.SOURCE, 0); Node n7 = new Node(NODE_TYPE.SOURCE, 0);
		 * uspObj.getGraph().addVertex(n1); uspObj.getGraph().addVertex(n2);
		 * uspObj.getGraph().addVertex(n3); uspObj.getGraph().addVertex(n4);
		 * uspObj.getGraph().addVertex(n5); uspObj.getGraph().addVertex(n6);
		 * uspObj.getGraph().addVertex(n7); // Generate edges List<ExtDWE>
		 * edgeList = new ArrayList<ExtDWE>(); ExtDWE e1 =
		 * uspObj.getGraph().addEdge(n4, n3); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n3, n2); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n2, n1); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n6, n5); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n5, n1); ExtDWE e6 =
		 * uspObj.getGraph().addEdge(n7, n1); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); edgeList.add(e4);
		 * edgeList.add(e5); edgeList.add(e6); return edgeList;
		 */

		/*
		 * //Graph 3 Node n0 = new Node(NODE_TYPE.SOURCE, 0); Node n1 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n4 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n5 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n6 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n7 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n8 = new Node(NODE_TYPE.TARGET,
		 * 5); Node n9 = new Node(NODE_TYPE.TARGET, 5);
		 * 
		 * uspObj.getGraph().addVertex(n0); uspObj.getGraph().addVertex(n1);
		 * uspObj.getGraph().addVertex(n2); uspObj.getGraph().addVertex(n3);
		 * uspObj.getGraph().addVertex(n4); uspObj.getGraph().addVertex(n5);
		 * uspObj.getGraph().addVertex(n6); uspObj.getGraph().addVertex(n7);
		 * uspObj.getGraph().addVertex(n8); uspObj.getGraph().addVertex(n9);
		 * 
		 * // Generate edges List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		 * ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n0, n2); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n0, n3); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n1, n4); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n2, n5); ExtDWE e6 =
		 * uspObj.getGraph().addEdge(n3, n6); ExtDWE e7 =
		 * uspObj.getGraph().addEdge(n1, n5); ExtDWE e8 =
		 * uspObj.getGraph().addEdge(n2, n4); ExtDWE e9 =
		 * uspObj.getGraph().addEdge(n2, n6); ExtDWE e10 =
		 * uspObj.getGraph().addEdge(n3, n5); ExtDWE e11 =
		 * uspObj.getGraph().addEdge(n4, n7); ExtDWE e12 =
		 * uspObj.getGraph().addEdge(n5, n8); ExtDWE e13 =
		 * uspObj.getGraph().addEdge(n6, n9);
		 * 
		 * edgeList.add(e1); edgeList.add(e2); edgeList.add(e3);
		 * edgeList.add(e4); edgeList.add(e5); edgeList.add(e6);
		 * edgeList.add(e7); edgeList.add(e8); edgeList.add(e9);
		 * edgeList.add(e10); edgeList.add(e11); edgeList.add(e12);
		 * edgeList.add(e13);
		 * 
		 * return edgeList;
		 */

		/*
		// Graph 4
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n8 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n9 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n10 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n11 = new Node(NODE_TYPE.TARGET, 5);
		Node n12 = new Node(NODE_TYPE.TARGET, 3);
		Node n13 = new Node(NODE_TYPE.TARGET, 7);

		uspObj.getGraph().addVertex(n0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);
		uspObj.getGraph().addVertex(n5);
		uspObj.getGraph().addVertex(n6);
		uspObj.getGraph().addVertex(n7);
		uspObj.getGraph().addVertex(n8);
		uspObj.getGraph().addVertex(n9);
		uspObj.getGraph().addVertex(n10);
		uspObj.getGraph().addVertex(n11);
		uspObj.getGraph().addVertex(n12);
		uspObj.getGraph().addVertex(n13);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1);
		ExtDWE e2 = uspObj.getGraph().addEdge(n0, n2);
		ExtDWE e3 = uspObj.getGraph().addEdge(n0, n3);
		ExtDWE e4 = uspObj.getGraph().addEdge(n0, n4);
		ExtDWE e5 = uspObj.getGraph().addEdge(n1, n2);
		ExtDWE e6 = uspObj.getGraph().addEdge(n3, n4);
		ExtDWE e7 = uspObj.getGraph().addEdge(n1, n5);
		ExtDWE e8 = uspObj.getGraph().addEdge(n1, n6);
		ExtDWE e9 = uspObj.getGraph().addEdge(n2, n5);
		ExtDWE e10 = uspObj.getGraph().addEdge(n2, n6);
		ExtDWE e11 = uspObj.getGraph().addEdge(n3, n6);
		ExtDWE e12 = uspObj.getGraph().addEdge(n4, n6);
		ExtDWE e13 = uspObj.getGraph().addEdge(n4, n7);
		ExtDWE e14 = uspObj.getGraph().addEdge(n5, n8);
		ExtDWE e15 = uspObj.getGraph().addEdge(n5, n9);
		ExtDWE e16 = uspObj.getGraph().addEdge(n6, n8);
		ExtDWE e17 = uspObj.getGraph().addEdge(n6, n9);
		ExtDWE e18 = uspObj.getGraph().addEdge(n6, n10);
		ExtDWE e19 = uspObj.getGraph().addEdge(n6, n7);
		ExtDWE e20 = uspObj.getGraph().addEdge(n7, n9);
		ExtDWE e21 = uspObj.getGraph().addEdge(n7, n10);
		ExtDWE e22 = uspObj.getGraph().addEdge(n8, n11);
		ExtDWE e23 = uspObj.getGraph().addEdge(n8, n12);
		ExtDWE e24 = uspObj.getGraph().addEdge(n8, n13);
		ExtDWE e25 = uspObj.getGraph().addEdge(n9, n11);
		ExtDWE e26 = uspObj.getGraph().addEdge(n9, n12);
		ExtDWE e27 = uspObj.getGraph().addEdge(n10, n12);
		ExtDWE e28 = uspObj.getGraph().addEdge(n10, n13);

		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);
		edgeList.add(e6);
		edgeList.add(e7);
		edgeList.add(e8);
		edgeList.add(e9);
		edgeList.add(e10);
		edgeList.add(e11);
		edgeList.add(e12);
		edgeList.add(e13);
		edgeList.add(e14);
		edgeList.add(e15);
		edgeList.add(e16);
		edgeList.add(e17);
		edgeList.add(e18);
		edgeList.add(e19);
		edgeList.add(e20);
		edgeList.add(e21);
		edgeList.add(e22);
		edgeList.add(e23);
		edgeList.add(e24);
		edgeList.add(e25);
		edgeList.add(e26);
		edgeList.add(e27);
		edgeList.add(e28);

		return edgeList;
		*/
		
		//Graph 5
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n8 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n9 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n10 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n11 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n12 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n13 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n14 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n15 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n16 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n17 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n18 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n19 = new Node(NODE_TYPE.TARGET, 5);
		Node n20 = new Node(NODE_TYPE.TARGET, 7);
		
		uspObj.getGraph().addVertex(n0);
		uspObj.getGraph().addVertex(n1);
		uspObj.getGraph().addVertex(n2);
		uspObj.getGraph().addVertex(n3);
		uspObj.getGraph().addVertex(n4);
		uspObj.getGraph().addVertex(n5);
		uspObj.getGraph().addVertex(n6);
		uspObj.getGraph().addVertex(n7);
		uspObj.getGraph().addVertex(n8);
		uspObj.getGraph().addVertex(n9);
		uspObj.getGraph().addVertex(n10);
		uspObj.getGraph().addVertex(n11);
		uspObj.getGraph().addVertex(n12);
		uspObj.getGraph().addVertex(n13);
		uspObj.getGraph().addVertex(n14);
		uspObj.getGraph().addVertex(n15);
		uspObj.getGraph().addVertex(n16);
		uspObj.getGraph().addVertex(n17);
		uspObj.getGraph().addVertex(n18);
		uspObj.getGraph().addVertex(n19);
		uspObj.getGraph().addVertex(n20);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = uspObj.getGraph().addEdge(n0, n1);
		ExtDWE e2 = uspObj.getGraph().addEdge(n0, n2);
		ExtDWE e3 = uspObj.getGraph().addEdge(n0, n3);
		ExtDWE e4 = uspObj.getGraph().addEdge(n0, n4);
		ExtDWE e5 = uspObj.getGraph().addEdge(n1, n5);
		ExtDWE e6 = uspObj.getGraph().addEdge(n1, n6);
		ExtDWE e7 = uspObj.getGraph().addEdge(n2, n5);
		ExtDWE e8 = uspObj.getGraph().addEdge(n2, n6);
		ExtDWE e9 = uspObj.getGraph().addEdge(n2, n6);
		ExtDWE e10 = uspObj.getGraph().addEdge(n3, n6);
		ExtDWE e11 = uspObj.getGraph().addEdge(n3, n7);
		ExtDWE e12 = uspObj.getGraph().addEdge(n3, n8);
		ExtDWE e13 = uspObj.getGraph().addEdge(n4, n7);
		ExtDWE e14 = uspObj.getGraph().addEdge(n4, n8);
		ExtDWE e15 = uspObj.getGraph().addEdge(n5, n9);
		ExtDWE e16 = uspObj.getGraph().addEdge(n6, n9);
		ExtDWE e17 = uspObj.getGraph().addEdge(n6, n10);
		ExtDWE e18 = uspObj.getGraph().addEdge(n7, n10);
		ExtDWE e19 = uspObj.getGraph().addEdge(n8, n10);
		ExtDWE e20 = uspObj.getGraph().addEdge(n8, n11);
		ExtDWE e21 = uspObj.getGraph().addEdge(n9, n12);
		ExtDWE e22 = uspObj.getGraph().addEdge(n9, n13);
		ExtDWE e23 = uspObj.getGraph().addEdge(n9, n14);
		ExtDWE e24 = uspObj.getGraph().addEdge(n10, n13);
		ExtDWE e25 = uspObj.getGraph().addEdge(n10, n14);
		ExtDWE e26 = uspObj.getGraph().addEdge(n11, n14);
		ExtDWE e27 = uspObj.getGraph().addEdge(n11, n15);
		ExtDWE e28 = uspObj.getGraph().addEdge(n12, n16);
		ExtDWE e29 = uspObj.getGraph().addEdge(n12, n17);
		ExtDWE e30 = uspObj.getGraph().addEdge(n13, n16);
		ExtDWE e31 = uspObj.getGraph().addEdge(n13, n17);
		ExtDWE e32 = uspObj.getGraph().addEdge(n14, n16);
		ExtDWE e33 = uspObj.getGraph().addEdge(n14, n17);
		ExtDWE e34 = uspObj.getGraph().addEdge(n14, n18);
		ExtDWE e35 = uspObj.getGraph().addEdge(n15, n17);
		ExtDWE e36 = uspObj.getGraph().addEdge(n15, n18);
		ExtDWE e37 = uspObj.getGraph().addEdge(n16, n19);
		ExtDWE e38 = uspObj.getGraph().addEdge(n16, n20);
		ExtDWE e39 = uspObj.getGraph().addEdge(n17, n19);
		ExtDWE e40 = uspObj.getGraph().addEdge(n17, n20);
		ExtDWE e41 = uspObj.getGraph().addEdge(n18, n19);
		ExtDWE e42 = uspObj.getGraph().addEdge(n18, n20);
		ExtDWE e43 = uspObj.getGraph().addEdge(n1, n2);
		ExtDWE e44 = uspObj.getGraph().addEdge(n3, n4);
		ExtDWE e45 = uspObj.getGraph().addEdge(n6, n7);
		ExtDWE e46 = uspObj.getGraph().addEdge(n7, n8);
		ExtDWE e47 = uspObj.getGraph().addEdge(n9, n10);
		ExtDWE e48 = uspObj.getGraph().addEdge(n10, n11);
		ExtDWE e49 = uspObj.getGraph().addEdge(n12, n13);
		ExtDWE e50 = uspObj.getGraph().addEdge(n14, n15);
		ExtDWE e51 = uspObj.getGraph().addEdge(n16, n17);
		ExtDWE e52 = uspObj.getGraph().addEdge(n17, n18);
		
		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);
		edgeList.add(e6);
		edgeList.add(e7);
		edgeList.add(e8);
		edgeList.add(e9);
		edgeList.add(e10);
		edgeList.add(e11);
		edgeList.add(e12);
		edgeList.add(e13);
		edgeList.add(e14);
		edgeList.add(e15);
		edgeList.add(e16);
		edgeList.add(e17);
		edgeList.add(e18);
		edgeList.add(e19);
		edgeList.add(e20);
		edgeList.add(e21);
		edgeList.add(e22);
		edgeList.add(e23);
		edgeList.add(e24);
		edgeList.add(e25);
		edgeList.add(e26);
		edgeList.add(e27);
		edgeList.add(e28);
		edgeList.add(e29);
		edgeList.add(e30);
		edgeList.add(e31);
		edgeList.add(e32);
		edgeList.add(e33);
		edgeList.add(e34);
		edgeList.add(e35);
		edgeList.add(e36);
		edgeList.add(e37);
		edgeList.add(e38);
		edgeList.add(e39);
		edgeList.add(e40);
		edgeList.add(e41);
		edgeList.add(e42);
		edgeList.add(e43);
		edgeList.add(e44);
		edgeList.add(e45);
		edgeList.add(e46);
		edgeList.add(e47);
		edgeList.add(e48);
		edgeList.add(e49);
		edgeList.add(e50);
		edgeList.add(e51);
		edgeList.add(e52);
		
		return edgeList;
		
		/*
		 * //Graph 2 Node n0 = new Node(NODE_TYPE.TARGET, 10); Node n1 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new Node(NODE_TYPE.SOURCE,
		 * 0); Node n4 = new Node(NODE_TYPE.INTERMEDIATE, 0); Node n5 = new
		 * Node(NODE_TYPE.SOURCE, 0); uspObj.getGraph().addVertex(n0);
		 * uspObj.getGraph().addVertex(n1); uspObj.getGraph().addVertex(n2);
		 * uspObj.getGraph().addVertex(n3); uspObj.getGraph().addVertex(n4);
		 * uspObj.getGraph().addVertex(n5); // Generate edges List<ExtDWE>
		 * edgeList = new ArrayList<ExtDWE>(); ExtDWE e1 =
		 * uspObj.getGraph().addEdge(n3, n2); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n2, n1); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n1, n0); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n5, n4); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n4, n0); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); edgeList.add(e4);
		 * edgeList.add(e5); return edgeList;
		 */

		/*
		 * //Graph 1 Node n0 = new Node(NODE_TYPE.TARGET, 10); Node n1 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n2 = new Node(NODE_TYPE.SOURCE,
		 * 0); Node n3 = new Node(NODE_TYPE.SOURCE, 0);
		 * uspObj.getGraph().addVertex(n0); uspObj.getGraph().addVertex(n1);
		 * uspObj.getGraph().addVertex(n2); uspObj.getGraph().addVertex(n3); //
		 * Generate edges List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		 * ExtDWE e1 = uspObj.getGraph().addEdge(n0, n3); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n2, n1); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n1, n0); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); return edgeList;
		 */

		/*
		 * Node n1 = new Node(NODE_TYPE.SOURCE, 0); Node n2 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n3 = new
		 * Node(NODE_TYPE.INTERMEDIATE, 0); Node n4 = new Node(NODE_TYPE.TARGET,
		 * 10); Node n5 = new Node(NODE_TYPE.TARGET, 100);
		 * uspObj.getGraph().addVertex(n1); uspObj.getGraph().addVertex(n2);
		 * uspObj.getGraph().addVertex(n3); uspObj.getGraph().addVertex(n4);
		 * uspObj.getGraph().addVertex(n5); // Generate edges List<ExtDWE>
		 * edgeList = new ArrayList<ExtDWE>(); ExtDWE e1 =
		 * uspObj.getGraph().addEdge(n1, n2); ExtDWE e2 =
		 * uspObj.getGraph().addEdge(n1, n3); ExtDWE e3 =
		 * uspObj.getGraph().addEdge(n2, n3); ExtDWE e4 =
		 * uspObj.getGraph().addEdge(n3, n2); ExtDWE e5 =
		 * uspObj.getGraph().addEdge(n2, n4); ExtDWE e6 =
		 * uspObj.getGraph().addEdge(n3, n5); // ExtDWE e7 =
		 * uspObj.getGraph().addEdge(n2, n5); edgeList.add(e1);
		 * edgeList.add(e2); edgeList.add(e3); edgeList.add(e4);
		 * edgeList.add(e5); edgeList.add(e6); // edgeList.add(e7); return
		 * edgeList;
		 */
	}

}
