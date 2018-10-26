
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import lpWrapper.LPSolverException;
import model.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;
import model.teamBuildingModels.AdversaryPath;
import model.teamBuildingModels.CompactAllocation;
import model.teamBuildingModels.CompactPath;
import model.teamBuildingModels.MinCut;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;

import PQsearch.CompareParallel;
import PQsearch.PQ;
import PQsearch.TeamsPermuteCheck;
import search.BruteForce;
import search.Search;
import search.teamNode;
import teamBuildingSolvers.CompactProblem;
import teamBuildingSolvers.Rugged;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;
import teamBuildingSolvers.RuggedCutoff;

public class OptRandom3 {

	private static AbstractBaseGraph<INode, ExtDWE> mumbaiGraph() {
		return GraphGenerator.getMumbaiGraph();

	}

	public static void parseGraph(AbstractBaseGraph g){

		String nodelist = "";

		double minlat=0;
		double minlon=0;
		for(Iterator<INode> node = g.vertexSet().iterator(); node.hasNext();){
			INode n = node.next();

			double d = Math.abs(n.getLat());
			if(Math.abs(minlat) < Math.abs(n.getLat())){
				minlat = n.getLat();
			}
			if(Math.abs(minlon) < Math.abs(n.getLon())){
				minlon = n.getLon();
			}

		}

		for(Iterator<INode> node = g.vertexSet().iterator(); node.hasNext();){
			INode n = node.next();

			String nodetype = "mid node";

			if(n.getType().equals(NODE_TYPE.TARGET)){
				nodetype = "target node";
			}
			if(n.getType().equals(NODE_TYPE.OFFICE)){
				nodetype = "source node";
			}
			if(!n.getType().equals(NODE_TYPE.SOURCE)){
				nodelist += "\\node ("+ n.getId() +") at (" + (n.getLat()-minlat)*3  + "," + (n.getLon()-minlon)*3 +") ["+nodetype+"] {};\n";
			}
		}

		String edgelist = "\\path[draw,thick, above]\n";
		for(Iterator<ExtDWE> edge = g.edgeSet().iterator(); edge.hasNext();){
			ExtDWE e = edge.next();

			if(!e.getsource().getType().equals(NODE_TYPE.SOURCE)){
				edgelist +=  "("+e.getsource().getId()+") edge node {} ("+e.gettarget().getId()+")\n";}
		}

		edgelist+=";";
		System.out.println(nodelist);
		System.out.println(edgelist);
	}


	public static AbstractBaseGraph<INode, ExtDWE> getMadagascarGraph() {
		// TODO Auto-generated method stub
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);

		//AbstractBaseGraph<INode, ExtDWE> graph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);

		double[] latlon ={49.6784529935848,-15.9909249968463,
				49.5792294691834,-15.9623714644671,
				49.6134937083235,-16.0427496574297,
				49.5463929072497,-16.0256175381717,
				49.5256915969072,-15.9713658270755,
				49.6870190529776,-16.1069951045754,
				49.4614461494153,-16.0398943044958,
				49.3743578764859,-16.0348974360629,
				49.330813740247,-16.0127684485713,
				49.5720910862404,-16.0727308659956,
				49.5007072563352,-16.0862937942076,
				49.4086221150046,-16.0920045001987,
				49.3657918169992,-16.1648160074669,
				49.7205694531407,-16.152966291365,
				49.6606070355579,-16.1729537636486,
				49.6113521931515,-16.2029349729483,
				49.6384780486992,-16.2650389044665,
				49.7048650107857,-16.2086456788874,
				49.7441261175377,-16.2955911837736,
				49.6962989512499,-16.3158641917482,
				49.6713146108332,-16.3686882263134,
				49.5049902860652,-16.3439894209198,
				49.4535939285393,-16.2483350887218,
				49.3843516126814,-16.2569011478484,
				49.6113521929499,-16.5044602715138,
				49.6306258273945,-16.5951177359013,
				49.711289555599,-16.6065391480366,
				49.5449652314622,-16.4707671029739,
				49.4850028132377,-16.475763971105,
				49.46144614953,-16.5385817420747,
				49.3860172358562,-16.4000971115116,
				49.381734206023,-16.5014621504651,
				49.3046396696217,-16.4257952900179,
				49.6744079101887,-16.7084752577054,
				49.6501374072815,-16.74131181935,
				49.6230115515841,-16.6763525346645,
				49.5280710580165,-16.7871402383665,
				49.6244392283012,-16.8292566986984,
				49.4673948026423,-16.7271778215654,
				49.196969059269,-16.5372254488427,
				49.2959546363671,-16.5981396514003,
				49.1075013253639,-16.4106381241063,
				49.3749527423208,-16.6305003205993,
				49.0475389074528,-16.5916675169028,
				49.1646083894291,-16.6268835395803,
				49.4777454577124,-16.9194620652497,
				49.4330115906665,-16.8395121752319,
				49.3187974624882,-16.8128622122889,
				49.2883403612143,-16.8718728455872,
				49.4796490263488,-16.987800185224,
				49.534852522168,-17.0096912264504,
				49.2664493202705,-16.957343084232,
				49.208390472082,-17.0163537175588,
				49.0856102835366,-17.0001733828904,
				49.0399246325657,-16.9126092171639,
				49.2702564575717,-17.0909736149457,
				49.4063616271603,-17.0700343581516,
				49.1122602471572,-16.9077551173708,
				49.015892076756,-16.9815184084883,
				48.9623542037204,-16.3697589838726,
				49.8344266622382,-16.2324640836171,
				49.8320472011577,-16.5465529363357,
				49.3803124920001,-16.9097020339999,
				49.5845870330001,-16.9217115409999,
				49.5669320330001,-16.8235440939999,
				49.7466466420001,-16.6883480829999,
				49.5816657560001,-16.425854259,
				49.6114958250001,-16.3409831989999,
				49.6677420290001,-16.2625175589999,
				49.7672838250001,-16.1695644449999,
				49.6726940920001,-16.0426572449999,
				49.6164158550001,-16.1036585189999,
				49.5002879160001,-16.7663788699999,
				49.6178765070001,-16.8725692489999,
				49.4756125670001,-17.0920193819999,
				49.3660976390001,-17.0331249239999,
				49.5244758906642,-15.9275066110944,
				49.2874815750686,-15.9874690283939,
				49.5216205376355,-16.6563831052505,
				49.691038161513,-16.7999121947828,
				48.9550338416434,-16.7742413076889,
				49.2337398824392,-16.7477112382078,
				48.8820236386907,-17.0468316178809,
				49.7356143811995,-16.4319546430777,
				49.2363112267414,-16.3528532374691,
				48.8806145941657,-16.339628796699,
				49.558938253500054,-16.906284624499947,
				49.657738694907096,-16.8145844467406,
				49.26104012182675,-16.8097920418975,
				49.7838307911786,-16.4892537897067,
				49.7159566662247,-16.37390941741295,
				49.78502052171885,-16.3322093633474,
				49.73987024936859,-16.36377291342565,
				49.42498820750652,-16.993098409641373,
				49.67058778439725,-16.770612007066397
		};


		List<INode> nodeList = new ArrayList<INode>();
		for (int x = 0; x < 95; x++) {
			INode v = new Node();
			v.setType(NODE_TYPE.INTERMEDIATE);
			//v.setLatLon((latlon[2*x+1]+17.5)*2,(latlon[2*x]-48.7)*2);
			nodeList.add(v);
			graph.addVertex(v);
		}
		//80->88
		int[] targetIndexes = {77,78,79,80,81,82,83,84,85,86};
		//1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};//,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90};

		int[] sourceIndexes = {63,65,66,67,69,70,71,72,74,75,76};


		INode virtual = new Node();
		virtual.setType(NODE_TYPE.SOURCE);
		graph.addVertex(virtual);

		for (int i : sourceIndexes) {
			ExtDWE e = graph.addEdge(virtual, nodeList.get(i - 1));

			e.setType(EDGE_TYPE.VIRTUAL);
			nodeList.get(i - 1).setType(NODE_TYPE.OFFICE);
		}


		//		Low Priority (1) = 83,86
		//		Medium Priority (2) = 78,81,85,
		//		High Priority (3) = 77,79,80,82,84

		for (int i : targetIndexes) {
			nodeList.get(i - 1).setType(NODE_TYPE.TARGET);
			nodeList.get(i - 1).setPayoff(300);
		}

		nodeList.get(82).setPayoff(200);
		nodeList.get(85).setPayoff(200);

		nodeList.get(77).setPayoff(100);
		nodeList.get(80).setPayoff(100);
		nodeList.get(84).setPayoff(100);


		int[][] edgeSet = {{1,2},{2,77},{1,71},{71,3},{3,4},{4,5},{5,77},{71,6},{6,72},{72,3},{72,10},{3,10},{10,4},
				{10,11},{4,7},{11,7},{7,8},{8,9},{9,78},{11,12},{12,8},{6,14},{14,70},{70,61},{61,92},
				{92,84},{14,15},{15,16},{16,17},{18,14},{70,18},{18,69},{17,69},{69,19},{19,93},{93,84},
				{69,20},{20,91},{91,84},{17,68},{69,68},{68,21},{21,84},{68,22},{22,23},{23,24},{24,85},
				{23,13},{13,12},{68,67},{67,25},{25,27},{27,84},{62,90},{90,84},{25,26},{26,79},{67,28},
				{28,29},{29,30},{30,79},{30,32},{32,33},{33,85},{32,31},{31,22},{62,66},{66,34},{34,35},
				{35,95},{95,80},{35,36},{36,79},{30,26},{35,65},{65,38},{38,88},{88,80},{74,87},{87,94},{94,80},{74,64},
				{65,37},{37,73},{73,39},{39,79},{43,79},{43,41},{41,40},{40,85},{85,42},{42,60},{60,86},
				{64,46},{46,63},{63,47},{47,37},{47,73},{47,48},{48,82},{45,82},{45,40},{40,44},{63,49},{49,89},
				{89,82},{63,52},{52,53},{53,54},{54,83},{54,59},{59,83},{59,55},{55,81},{44,81},{56,53},{56,76},
				{57,56},{76,50},{57,76},{57,75},{51,64},{64,50},{80,88}};


		for (int[] edge : edgeSet) {
			if(nodeList.get(edge[1] - 1).getType().equals(NODE_TYPE.OFFICE)){
				graph.addEdge(nodeList.get(edge[1] - 1), nodeList.get(edge[0] - 1));
				nodeList.get(edge[1] - 1).setType(NODE_TYPE.INTERMEDIATE);
			}else{
				graph.addEdge(nodeList.get(edge[0] - 1), nodeList.get(edge[1] - 1));
			}
			//ExtDWE e = graph.addEdge(nodeList.get(edge[1] - 1), nodeList.get(edge[0] - 1));
			//e.setType(EDGE_TYPE.DUAL);
		}

		for (int x = 0; x < 95; x++) {
			INode v = nodeList.get(x);
			//			if(x>=86){
			//				double x1 = graph.incomingEdgesOf(v).iterator().next().getsource().getLat();
			//				double y1 = graph.incomingEdgesOf(v).iterator().next().getsource().getLon();
			//				
			//				double x2 = graph.outgoingEdgesOf(v).iterator().next().gettarget().getLat();
			//				double y2 = graph.outgoingEdgesOf(v).iterator().next().gettarget().getLon();
			//				
			//				v.setLatLon((x1+x2)/2,(y1+y2)/2);
			//				System.out.println((v.getLon()/2+48.7)+","+(v.getLat()/2-17.5));
			//				
			//			}else{
			//v.setLatLon((latlon[2*x+1]+17.5)*2,(latlon[2*x]-48.7)*2);
			v.setLatLon((latlon[2*x+1]),(latlon[2*x]));
			//}
		}
		System.out.println("No. of edges: " + graph.edgeSet().size());
		System.out.println("No. of nodes: " + graph.vertexSet().size());

		return graph;
	}


	public static AbstractBaseGraph<INode, ExtDWE> GridGraph(int width, int height, int nrSources,int nrTargets, int payoff) {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		//AbstractBaseGraph<INode, ExtDWE> graph = new WeightedMultigraph<INode, ExtDWE>(
		//		ExtDWE.class);

		List<ArrayList<INode>> rows = new ArrayList<ArrayList<INode>>(height);

		for(int j=0;j<height;j++){


			ArrayList<INode> nodeList = new ArrayList<INode>();
			//rows.add(nodeList);

			for(int i=0;i<width-1;i++){
				if(nodeList.isEmpty()){
					Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
					n1.setLatLon(i, j);
					Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
					n1.setLatLon(i, j+1);

					graph.addVertex(n1);
					graph.addVertex(n2);

					nodeList.add(n1);
					nodeList.add(n2);

					ExtDWE e = graph.addEdge(n1, n2);
					ExtDWE e1 = graph.addEdge(n2, n1);

				}else{
					Node n = new Node(NODE_TYPE.INTERMEDIATE, 0);
					n.setLatLon(i, j+1);

					nodeList.add(n);
					graph.addVertex(n);
					ExtDWE e = graph.addEdge(nodeList.get(nodeList.size()-2), n);
					ExtDWE e2 = graph.addEdge(n,nodeList.get(nodeList.size()-2));
				}
			}
			if(j>0){
				for(int i=1;i<width-1;i++){
					//if(i%2 == 0){
					ExtDWE e = graph.addEdge(rows.get(j-1).get(i), nodeList.get(i));
					//}else{
					ExtDWE e1 = graph.addEdge(nodeList.get(i), rows.get(j-1).get(i));
					//}
				}
			}
			rows.add(nodeList);

		}

		double stepSRC = height/nrSources;

		Node n = new Node(NODE_TYPE.SOURCE, 0);
		graph.addVertex(n);
		double loc=0;

		for(int i=0;i<nrSources;i++){
			ExtDWE e = graph.addEdge(n, rows.get((int)loc).get(0));
			e.setType(EDGE_TYPE.VIRTUAL);
			loc= loc+stepSRC;
		}

		double stepTGT = height/nrTargets;
		loc=0;
		for(int i=0;i<nrTargets;i++){
			rows.get((int)loc).get(width-1).setType(NODE_TYPE.TARGET);
			rows.get((int)loc).get(width-1).setPayoff(payoff);
			loc= loc+stepTGT;
		}

		return graph;

	}

	private static AbstractBaseGraph<INode, ExtDWE> oldGridGraph(int width, int height, int nrSources,int nrTargets, int payoff) {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		List<ArrayList<INode>> rows = new ArrayList<ArrayList<INode>>(height);

		for(int j=0;j<height;j++){


			ArrayList<INode> nodeList = new ArrayList<INode>();
			//rows.add(nodeList);

			for(int i=0;i<width-1;i++){
				if(nodeList.isEmpty()){
					Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
					n1.setLatLon(i, j);
					Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
					n1.setLatLon(i, j+1);

					graph.addVertex(n1);
					graph.addVertex(n2);

					nodeList.add(n1);
					nodeList.add(n2);

					ExtDWE e = graph.addEdge(n1, n2);
					ExtDWE e1 = graph.addEdge(n2, n1);

				}else{
					Node n = new Node(NODE_TYPE.INTERMEDIATE, 0);
					n.setLatLon(i, j+1);

					nodeList.add(n);
					graph.addVertex(n);
					ExtDWE e = graph.addEdge(nodeList.get(nodeList.size()-2), n);
					ExtDWE e2 = graph.addEdge(n,nodeList.get(nodeList.size()-2));
				}
			}
			if(j>0){
				for(int i=1;i<width-1;i++){
					//if(i%2 == 0){
					ExtDWE e = graph.addEdge(rows.get(j-1).get(i), nodeList.get(i));
					//}else{
					ExtDWE e1 = graph.addEdge(nodeList.get(i), rows.get(j-1).get(i));
					//}
				}
			}
			rows.add(nodeList);

		}

		Node n = new Node(NODE_TYPE.SOURCE, 0);
		graph.addVertex(n);
		for(int i=0;i<nrSources;i++){
			ExtDWE e = graph.addEdge(n, rows.get(i).get(0));
			e.setType(EDGE_TYPE.VIRTUAL);
		}

		for(int i=0;i<nrTargets;i++){
			rows.get(i).get(width-1).setType(NODE_TYPE.TARGET);
			rows.get(i).get(width-1).setPayoff(payoff);

		}

		return graph;

	}

	private static AbstractBaseGraph<INode, ExtDWE> genlargeTestGraph() {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);

		//Graph 4
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		//Node n14 = new Node(NODE_TYPE.SOURCE, 0);
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
		//graph.addVertex(n14);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		ExtDWE e2 = graph.addEdge(n0, n2);
		ExtDWE e3 = graph.addEdge(n0, n3);
		ExtDWE e4 = graph.addEdge(n0, n4);

		ExtDWE e5 = graph.addEdge(n1, n2);
		ExtDWE e6 = graph.addEdge(n3, n4);
		ExtDWE e7 = graph.addEdge(n1, n5);
		ExtDWE e8 = graph.addEdge(n1, n6);
		ExtDWE e9 = graph.addEdge(n2, n5);
		ExtDWE e10 = graph.addEdge(n2, n6);
		ExtDWE e11 = graph.addEdge(n3, n6);
		ExtDWE e12 = graph.addEdge(n4, n6);
		ExtDWE e13 = graph.addEdge(n4, n7);

		ExtDWE e14 = graph.addEdge(n5, n8);
		ExtDWE e15 = graph.addEdge(n5, n9);
		ExtDWE e16 = graph.addEdge(n6, n8);
		ExtDWE e17 = graph.addEdge(n6, n9);
		ExtDWE e18 = graph.addEdge(n6, n10);
		ExtDWE e19 = graph.addEdge(n6, n7);
		ExtDWE e20 = graph.addEdge(n7, n9);
		ExtDWE e21 = graph.addEdge(n7, n10);

		ExtDWE e22 = graph.addEdge(n8, n11);
		ExtDWE e23 = graph.addEdge(n8, n12);
		ExtDWE e24 = graph.addEdge(n8, n13);
		ExtDWE e25 = graph.addEdge(n9, n11);
		ExtDWE e26 = graph.addEdge(n9, n12);
		ExtDWE e27 = graph.addEdge(n10, n12);


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

		return graph;
	}


	private static AbstractBaseGraph<INode, ExtDWE> genTestGraph8() {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		//Graph 3
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.TARGET, 5);

		graph.addVertex(n0);
		graph.addVertex(n1);
		graph.addVertex(n2);
		graph.addVertex(n3);
		graph.addVertex(n4);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		ExtDWE e2 = graph.addEdge(n0, n2);
		ExtDWE e3 = graph.addEdge(n0, n3);
		ExtDWE e4 = graph.addEdge(n1, n4);
		ExtDWE e5 = graph.addEdge(n2, n4);
		ExtDWE e6 = graph.addEdge(n3, n4);

		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		edgeList.add(e5);
		edgeList.add(e6);


		return graph;



	}
	private static AbstractBaseGraph<INode, ExtDWE> genTestGraph() {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		//Graph 3
		Node n0 = new Node(NODE_TYPE.SOURCE, 0);
		Node n1 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n2 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n3 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		Node n4 = new Node(NODE_TYPE.TARGET, 5);

		//			Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		//			Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		//			Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		//			Node n8 = new Node(NODE_TYPE.TARGET, 5);

		graph.addVertex(n0);
		graph.addVertex(n1);
		graph.addVertex(n2);
		graph.addVertex(n3);
		graph.addVertex(n4);

		//			graph.addVertex(n5);
		//			graph.addVertex(n6);
		//			graph.addVertex(n7);
		//			graph.addVertex(n8);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e1 = graph.addEdge(n0, n1);
		e1.setType(EDGE_TYPE.VIRTUAL);
		ExtDWE e2 = graph.addEdge(n1, n2);
		ExtDWE e3 = graph.addEdge(n2, n3);
		ExtDWE e4 = graph.addEdge(n3, n4);

		//			ExtDWE e5 = graph.addEdge(n0, n5);
		//			e5.setType(EDGE_TYPE.VIRTUAL);
		//			ExtDWE e6 = graph.addEdge(n5, n6);
		//			ExtDWE e7 = graph.addEdge(n6, n7);
		//			ExtDWE e8 = graph.addEdge(n7, n8);
		//			

		edgeList.add(e1);
		edgeList.add(e2);
		edgeList.add(e3);
		edgeList.add(e4);
		//			edgeList.add(e5);
		//			edgeList.add(e6);
		//			edgeList.add(e7);
		//			edgeList.add(e8);


		return graph;



	}
	private static AbstractBaseGraph<INode, ExtDWE> genLargeTestGraph() {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(
				ExtDWE.class);
		//Graph 4
		Node n = new Node(NODE_TYPE.SOURCE, 0);
		Node n0 = new Node(NODE_TYPE.INTERMEDIATE, 0);
		//Node n14 = new Node(NODE_TYPE.SOURCE, 0);
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


		graph.addVertex(n);
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
		//graph.addVertex(n14);

		// Generate edges
		List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
		ExtDWE e = graph.addEdge(n, n0);
		e.setType(EDGE_TYPE.VIRTUAL);
		ExtDWE e1 = graph.addEdge(n0, n1);
		ExtDWE e2 = graph.addEdge(n0, n2);
		ExtDWE e3 = graph.addEdge(n0, n3);
		ExtDWE e4 = graph.addEdge(n0, n4);

		ExtDWE e5 = graph.addEdge(n1, n2);
		ExtDWE e6 = graph.addEdge(n3, n4);
		ExtDWE e7 = graph.addEdge(n1, n5);
		ExtDWE e8 = graph.addEdge(n1, n6);
		ExtDWE e9 = graph.addEdge(n2, n5);
		ExtDWE e10 = graph.addEdge(n2, n6);
		ExtDWE e11 = graph.addEdge(n3, n6);
		ExtDWE e12 = graph.addEdge(n4, n6);
		ExtDWE e13 = graph.addEdge(n4, n7);

		ExtDWE e14 = graph.addEdge(n5, n8);
		ExtDWE e15 = graph.addEdge(n5, n9);
		ExtDWE e16 = graph.addEdge(n6, n8);
		ExtDWE e17 = graph.addEdge(n6, n9);
		ExtDWE e18 = graph.addEdge(n6, n10);
		ExtDWE e19 = graph.addEdge(n6, n7);
		ExtDWE e20 = graph.addEdge(n7, n9);
		ExtDWE e21 = graph.addEdge(n7, n10);

		ExtDWE e22 = graph.addEdge(n8, n11);
		ExtDWE e23 = graph.addEdge(n8, n12);
		ExtDWE e24 = graph.addEdge(n8, n13);
		ExtDWE e25 = graph.addEdge(n9, n11);
		ExtDWE e26 = graph.addEdge(n9, n12);
		ExtDWE e27 = graph.addEdge(n10, n12);


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

		return graph;
	}
	private static AbstractBaseGraph<INode, ExtDWE> RandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r){
		Configuration.initialize(108374);
		Random random = new Random();
		return GraphGenerator.getRandomGeometricGraph(numNodes, nrOfSources, nrOfTargets, r,
				50, random);

		//System.out.println("Number of Edges: " + team.getGraph().edgeSet().size());
	}
	private static AbstractBaseGraph<INode, ExtDWE> DenseRandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r, int min, int max){
		Configuration.initialize(108374);
		Random random = new Random();
		return GraphGenerator.getDenseRandomGraph(numNodes, nrOfSources, nrOfTargets, r, min, max);

		//System.out.println("Number of Edges: " + team.getGraph().edgeSet().size());
	}
	private static Set<ExtDWE> computeSingleMinCut(INode targetNode, AbstractBaseGraph graph) {
		MinCut minCutObj = new MinCut(graph);
		for ( Iterator<INode> nodeIter = graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.SOURCE ) {
				minCutObj.setSource(curNode);
				break;
			}
		}
		minCutObj.setTarget(targetNode);

		minCutObj.resetLP();

		Set<ExtDWE> minCutSet = minCutObj.getMinCut();
		return minCutSet;


	}
	public static Set<ExtDWE> getMinCutPaths(AbstractBaseGraph graph){

		Set<ExtDWE> minCut = new HashSet<ExtDWE>();
		List<INode> targets = new ArrayList<INode>();

		for ( Iterator<INode> nodeIter = graph.vertexSet().iterator(); nodeIter.hasNext(); ) {
			INode curNode = nodeIter.next();
			if ( curNode.getType() == NODE_TYPE.TARGET ) {
				targets.add(curNode);
			}
		}
		//get mincut for each individual target
		for (INode targetNode : targets) {
			minCut.addAll( computeSingleMinCut(targetNode, graph));

		}
		return minCut;

	}
	public static void runRugged(AbstractBaseGraph graph, String[] args) throws LPSolverException,
	MalformedGraphException {

		boolean print = true;

		List<Double> resources = new ArrayList<Double>();
		resources.add(2.0);
		//resources.add(1.0);

		List<Double> coverage = new ArrayList<Double>();
		coverage.add(4.0);
		//coverage.add(3.0);

		List<Double> Prob = new ArrayList<Double>();
		//Prob.add(0.9);
		Prob.add(0.5);

		TeamBuildingProblem uspObj3 = new TeamBuildingProblem(resources, coverage);

		uspObj3.setGraph(graph);

		System.out.println(uspObj3.getSources());

		Rugged ruggedObj3 = new Rugged(uspObj3);
		//EnumerateAll ruggedObj3 = new EnumerateAll(uspObj3);

		ruggedObj3.run();

		System.out.println("Game Value: " + ruggedObj3.getGameValue());


		int j=0;
		for(int i=0;i<ruggedObj3.getDefenderStrategy().size();i++){
			if(!ruggedObj3.getDefenderStrategy().get(i).equals(0.0)){
				j++;
				System.out.println("Defender Strategy "+j+" : "
						+ ruggedObj3.getDefenderStrategy().get(i));
				model.teamBuildingModels.DefenderAllocation da = ruggedObj3.getDefenderAllocations().get(i);
				for(String r :da.getResourceMap().keySet()){
					System.out.println(r+ ":" + da.getResourceMap().get(r));
				}
				System.out.println();
			}

		}

		System.out.println("Adversary Strategy: "
				+ ruggedObj3.getAdversaryStrategy());
		System.out
		.println("Adversary Paths: " + ruggedObj3.getAdversaryPaths());



		System.out
		.println("RunTime: " + ruggedObj3.getRunTime());
		System.out
		.println("TotalTime: " + ruggedObj3.getTotalTime()/1000);

		System.out
		.println("TotalIterations: " + ruggedObj3.getNumberOfIterations());


	}


	public static void testBetterResp(TeamBuildingProblem uspObj, String[] args){

		boolean ENABLE_FILE_WRITE = true;

		//String OUTPUTFOLDER = args[6];
		String CONFIG = "a zS"; 
		/**
		 * CONFIG: s: skeleton f: skeleton - better defender response t:
		 * skeleton - better attacker response d: better defender response a:
		 * better attacker response o: converge only with better responses w:
		 * converge with better responses first, then use best responses
		 */

		double r = 0.1;
		if ( args.length > 8) {
			System.out.println("Setting r: " + args[8]);
			r = new Double(args[8]);
		}

		double betterResponseEpsilon = 0.001;
		if ( args.length > 9) {
			System.out.println("Setting brE: " + args[9]);
			betterResponseEpsilon = new Double(args[9]);
		}

		double finalConvergenceEpsilon = 0.002;
		if ( args.length > 10) {
			System.out.println("Setting fcE: " + args[10]);
			finalConvergenceEpsilon = new Double(args[10]);
		}	


		int targetVal = 100; // Random integer between 1 and this number

		System.out.flush();
		finalConvergenceEpsilon = 0.002;

		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(uspObj,5);
		ruggedObj.setNumRoundsBeforeDiscard(100);
		ruggedObj.setEnableDiscardUselessActions(false);

		// ruggedObj.setkShortestPaths(2);

		ruggedObj.setEnableSkeleton(CONFIG.contains("s"), CONFIG.contains("f"),
				CONFIG.contains("t"));
		ruggedObj.setEnableBetterDefenderOracle(CONFIG.contains("d"));
		ruggedObj.setEnableBetterAttackerOracle(CONFIG.contains("a"));
		ruggedObj.setOnlyWarmStartConverge(CONFIG.contains("o"));
		ruggedObj.setEnableWarmStartConverge(CONFIG.contains("w"));

		ruggedObj.setFinalConvergenceEpsilon(finalConvergenceEpsilon);
		ruggedObj.setBetterResponseEpsilon(betterResponseEpsilon);

		int warmStartCount = 5;
		if ( args.length > 11) {
			System.out.println("Setting wSC: " + args[11]);
			warmStartCount = new Integer(args[11]);
		}

		List<model.teamBuildingModels.DefenderAllocation> daList = null;
		if ( CONFIG.contains("qM") ) {
			daList = ruggedObj.getWarmStartDefenderMinCutEdges(warmStartCount);
		} else if ( CONFIG.contains("qE") ) {
			daList = ruggedObj.getWarmStartDefenderRandomEdges(warmStartCount);
		} else if ( CONFIG.contains("qR") ) {
			//daList = ruggedObj.getWarmStartDefenderRanger();	
		}

		List<model.teamBuildingModels.AdversaryPath> apList = null;
		if ( CONFIG.contains("zR")) {
			apList = ruggedObj.getWarmStartAttackerRanger();	
		} else if ( CONFIG.contains("zS")) {
			apList = ruggedObj.getWarmStartAttackerShortestPath();
		}				 

		ruggedObj.warmStart(daList, apList);


		System.out.println("Warm Start Strategy Count: " + ruggedObj.getDefenderAllocations().size() + ", " + ruggedObj.getAdversaryPaths().size() );
		System.out.flush();
		System.err.flush();


		try {
			ruggedObj.run();
		} catch (MalformedGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
		}

		System.out.flush();
		System.err.flush();


		System.out.println("Final Size of matrix: "
				+ ruggedObj.getDefenderAllocations().size() + " x "
				+ ruggedObj.getAdversaryPaths().size());
		System.out.println("Final Load Time for problems: "
				+ ruggedObj.getLoadTime() / 1000.0);
		ruggedObj.printRunTimeBreakup();
		System.out.println("Final Average Path Length: "
				+ ruggedObj.getAveragePathLength());


		System.out.println("GameValue: "
				+ (ruggedObj.getGameValue()));
		System.out.println("Final Run Time: "
				+ (ruggedObj.getRunTime() * 1.0 / 1000));
		System.out.println("Final Number of Iterations: "
				+ ruggedObj.getNumberOfIterations());
		System.out.println("Final Total Time: "
				+ (ruggedObj.getTotalTime() / 1000.00));

		System.out.println("Strategy: ");
		List<model.teamBuildingModels.DefenderAllocation> dalist = ruggedObj.getDefenderAllocations();
		List<Double> strategy = ruggedObj.getDefenderStrategy();
		assert(dalist.size() == strategy.size());
		for ( int i = 0; i < strategy.size(); i++ ) {
			if ( strategy.get(i) > lpWrapper.Configuration.EPSILON) {
				System.out.println(dalist.get(i) + ": " + strategy.get(i));
			}
		}


		int j=0;
		for(int i=0;i<ruggedObj.getDefenderStrategy().size();i++){
			if(!ruggedObj.getDefenderStrategy().get(i).equals(0.0)){
				j++;
				System.out.println("Defender Strategy "+j+" : "
						+ ruggedObj.getDefenderStrategy().get(i));
				model.teamBuildingModels.DefenderAllocation da = ruggedObj.getDefenderAllocations().get(i);
				for(String r1 :da.getResourceMap().keySet()){
					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
				}
				System.out.println();
			}

		}

		System.out.println("Adversary Strategy: "
				+ ruggedObj.getAdversaryStrategy());
		System.out
		.println("Adversary Paths: " + ruggedObj.getAdversaryPaths());



		//ruggedObj.writeMeasure();
		System.out.flush();
		System.err.flush();
		System.out.print(ruggedObj.getcutoffUsed());
		ruggedObj.end();



	}	public static List<AdversaryPath> getBestResponsePaths(AbstractBaseGraph graph, List<Double> resources, List<Double> coverage, List<Double> Prob){


		TeamBuildingProblem uspObj = new TeamBuildingProblem(resources, coverage);

		int numNodes = graph.vertexSet().size();

		boolean ENABLE_FILE_WRITE = true;

		//String OUTPUTFOLDER = args[6];
		String CONFIG = "d a zS"; 
		/**
		 * CONFIG: s: skeleton f: skeleton - better defender response t:
		 * skeleton - better attacker response d: better defender response a:
		 * better attacker response o: converge only with better responses w:
		 * converge with better responses first, then use best responses
		 */

		double r = 0.1;

		double betterResponseEpsilon = 0.001;	
		double finalConvergenceEpsilon = 0.002;
		int targetVal = 100; // Random integer between 1 and this number

		System.out.flush();
		finalConvergenceEpsilon = 0.002;

		uspObj.setGraph(graph);
		RuggedBetterResponsesCutoff ruggedObj;
		ruggedObj = new RuggedBetterResponsesCutoff(uspObj);


		ruggedObj.setNumRoundsBeforeDiscard(100);
		ruggedObj.setEnableDiscardUselessActions(false);

		ruggedObj.setEnableSkeleton(CONFIG.contains("s"), CONFIG.contains("f"),
				CONFIG.contains("t"));
		ruggedObj.setEnableBetterDefenderOracle(CONFIG.contains("d"));
		ruggedObj.setEnableBetterAttackerOracle(CONFIG.contains("a"));
		ruggedObj.setOnlyWarmStartConverge(CONFIG.contains("o"));
		ruggedObj.setEnableWarmStartConverge(CONFIG.contains("w"));

		ruggedObj.setFinalConvergenceEpsilon(finalConvergenceEpsilon);
		ruggedObj.setBetterResponseEpsilon(betterResponseEpsilon);

		int warmStartCount = 15;

		List<model.teamBuildingModels.DefenderAllocation> daList = null;
		if ( CONFIG.contains("qM") ) {
			daList = ruggedObj.getWarmStartDefenderMinCutEdges(1);
		} else if ( CONFIG.contains("qE") ) {
			daList = ruggedObj.getWarmStartDefenderRandomEdges(warmStartCount);
		} else if ( CONFIG.contains("qR") ) {
			//daList = ruggedObj.getWarmStartDefenderRanger();	
		}	
		List<model.teamBuildingModels.AdversaryPath> apList = null;
		if ( CONFIG.contains("zR")) {
			apList = ruggedObj.getWarmStartAttackerRanger();	
		} else if ( CONFIG.contains("zS")) {
			apList = ruggedObj.getWarmStartAttackerShortestPath();
		}				 	
		ruggedObj.warmStart(daList, apList);

		try {
			ruggedObj.run();
		} catch (MalformedGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
		}

		System.out.flush();
		System.err.flush();

		System.out.println("GameValue: "
				+ (ruggedObj.getGameValue()));

		System.out.println("Strategy: ");
		List<model.teamBuildingModels.DefenderAllocation> dalist = ruggedObj.getDefenderAllocations();
		List<Double> strategy = ruggedObj.getDefenderStrategy();
		assert(dalist.size() == strategy.size());
		for ( int i = 0; i < strategy.size(); i++ ) {
			if ( strategy.get(i) > lpWrapper.Configuration.EPSILON) {
				System.out.println(dalist.get(i) + ": " + strategy.get(i));
			}
		}


		int j=0;
		for(int i=0;i<ruggedObj.getDefenderStrategy().size();i++){
			if(!ruggedObj.getDefenderStrategy().get(i).equals(0.0)){
				j++;
				System.out.println("Defender Strategy "+j+" : "
						+ ruggedObj.getDefenderStrategy().get(i));
				model.teamBuildingModels.DefenderAllocation da = ruggedObj.getDefenderAllocations().get(i);
				for(String r1 :da.getResourceMap().keySet()){
					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
				}
				System.out.println();
			}

		}

		List<AdversaryPath> usedAP = new ArrayList<AdversaryPath>();
		List<AdversaryPath> ap = ruggedObj.getAdversaryPaths();
		int m =0;
		for(Double d : ruggedObj.getAdversaryStrategy()){
			if(d>lpWrapper.Configuration.EPSILON){
				System.out.println(d+": "+ap.get(m));
				usedAP.add(ap.get(m));
			}
			m++;
		}
		System.out.println("Adversary Strategy: "
				+ ruggedObj.getAdversaryStrategy());
		System.out
		.println("Adversary Paths: " + ruggedObj.getAdversaryPaths());

		return usedAP;

	}
	//public static TeamBuildingProblem testBetterRespt(AbstractBaseGraph graph, String[] args, int cutoff, List<Double> res, boolean o){
	public static TeamBuildingProblem testBetterRespt(AbstractBaseGraph graph, String[] args, int cutoff){
		List<Double> resources = new ArrayList<Double>();
		resources.add(1.0);
		resources.add(1.0);
		resources.add(1.0);

		List<Double> coverage = new ArrayList<Double>();
		coverage.add(3.0);
		coverage.add(2.0);
		coverage.add(3.0);

		List<Double> Prob = new ArrayList<Double>();
		Prob.add(0.5);
		Prob.add(0.7);
		Prob.add(0.5);

		TeamBuildingProblem uspObj = new TeamBuildingProblem(resources, coverage, Prob);

		int numNodes = graph.vertexSet().size();

		boolean ENABLE_FILE_WRITE = true;

		//String OUTPUTFOLDER = args[6];
		String CONFIG = "d a zS"; 
		/**
		 * CONFIG: s: skeleton f: skeleton - better defender response t:
		 * skeleton - better attacker response d: better defender response a:
		 * better attacker response o: converge only with better responses w:
		 * converge with better responses first, then use best responses
		 */

		double r = 0.1;
		if ( args.length > 8) {
			System.out.println("Setting r: " + args[8]);
			r = new Double(args[8]);
		}

		double betterResponseEpsilon = 0.001;
		if ( args.length > 9) {
			System.out.println("Setting brE: " + args[9]);
			betterResponseEpsilon = new Double(args[9]);
		}

		double finalConvergenceEpsilon = 0.002;
		if ( args.length > 10) {
			System.out.println("Setting fcE: " + args[10]);
			finalConvergenceEpsilon = new Double(args[10]);
		}	


		//int targetVal = 100; // Random integer between 1 and this number

		System.out.flush();
		finalConvergenceEpsilon = 0.002;

		uspObj.setGraph(graph);
		RuggedBetterResponsesCutoff ruggedObj;
		if(cutoff!=0){
			ruggedObj = new RuggedBetterResponsesCutoff(uspObj, cutoff);
		}else{
			ruggedObj = new RuggedBetterResponsesCutoff(uspObj);
		}

		ruggedObj.setNumRoundsBeforeDiscard(100);
		ruggedObj.setEnableDiscardUselessActions(false);

		// ruggedObj.setkShortestPaths(2);

		ruggedObj.setEnableSkeleton(CONFIG.contains("s"), CONFIG.contains("f"),
				CONFIG.contains("t"));
		ruggedObj.setEnableBetterDefenderOracle(CONFIG.contains("d"));
		ruggedObj.setEnableBetterAttackerOracle(CONFIG.contains("a"));
		ruggedObj.setOnlyWarmStartConverge(CONFIG.contains("o"));
		ruggedObj.setEnableWarmStartConverge(CONFIG.contains("w"));

		ruggedObj.setFinalConvergenceEpsilon(finalConvergenceEpsilon);
		ruggedObj.setBetterResponseEpsilon(betterResponseEpsilon);

		int warmStartCount = 15;
		if ( args.length > 11) {
			System.out.println("Setting wSC: " + args[11]);
			warmStartCount = new Integer(args[11]);
		}

		List<model.teamBuildingModels.DefenderAllocation> daList = null;
		if ( CONFIG.contains("qM") ) {
			daList = ruggedObj.getWarmStartDefenderMinCutEdges(1);
		} else if ( CONFIG.contains("qE") ) {
			daList = ruggedObj.getWarmStartDefenderRandomEdges(warmStartCount);
		} else if ( CONFIG.contains("qR") ) {
			//daList = ruggedObj.getWarmStartDefenderRanger();	
		}

		List<model.teamBuildingModels.AdversaryPath> apList = null;
		if ( CONFIG.contains("zR")) {
			apList = ruggedObj.getWarmStartAttackerRanger();	
		} else if ( CONFIG.contains("zS")) {
			apList = ruggedObj.getWarmStartAttackerShortestPath();
		}				 

		ruggedObj.warmStart(daList, apList);
		//ruggedObj.optimal=o;
		/*
		System.out.println("Warm Start Strategy Count: " + ruggedObj.getDefenderAllocations().size() + ", " + ruggedObj.getAdversaryPaths().size() );
		System.out.flush();
		System.err.flush();

		 */
		try {
			ruggedObj.run();
		} catch (MalformedGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
		}

		System.out.flush();
		System.err.flush();

		/*
		System.out.println("Final Size of matrix: "
				+ ruggedObj.getDefenderAllocations().size() + " x "
				+ ruggedObj.getAdversaryPaths().size());
		System.out.println("Final Load Time for problems: "
				+ ruggedObj.getLoadTime() / 1000.0);
		ruggedObj.printRunTimeBreakup();
		System.out.println("Final Average Path Length: "
				+ ruggedObj.getAveragePathLength());
		 */

		System.out.println("GameValue: "
				+ (ruggedObj.getGameValue()));
		System.out.println("Final Run Time: "
				+ (ruggedObj.getRunTime() * 1.0 / 1000));
		System.out.println("Final Number of Iterations: "
				+ ruggedObj.getNumberOfIterations());
		System.out.println("Def oracle iter: "
				+ ruggedObj.deforaclecount);
		System.out.println("Final Total Time: "
				+ (ruggedObj.getTotalTime() / 1000.00));

		System.out.println("Strategy: ");
		List<model.teamBuildingModels.DefenderAllocation> dalist = ruggedObj.getDefenderAllocations();
		List<Double> strategy = ruggedObj.getDefenderStrategy();
		assert(dalist.size() == strategy.size());
		for ( int i = 0; i < strategy.size(); i++ ) {
			if ( strategy.get(i) > lpWrapper.Configuration.EPSILON) {
				System.out.println(dalist.get(i) + ": " + strategy.get(i));
			}
		}


		int j=0;
		for(int i=0;i<ruggedObj.getDefenderStrategy().size();i++){
			if(!ruggedObj.getDefenderStrategy().get(i).equals(0.0)){
				j++;
				System.out.println("Defender Strategy "+j+" : "
						+ ruggedObj.getDefenderStrategy().get(i));
				model.teamBuildingModels.DefenderAllocation da = ruggedObj.getDefenderAllocations().get(i);
				for(String r1 :da.getResourceMap().keySet()){
					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
				}
				System.out.println();
			}

		}

		List<AdversaryPath> usedAP = new ArrayList<AdversaryPath>();
		List<AdversaryPath> ap = ruggedObj.getAdversaryPaths();
		int m =0;
		for(Double d : ruggedObj.getAdversaryStrategy()){
			if(d>lpWrapper.Configuration.EPSILON){
				System.out.println(d+": "+ap.get(m));
				usedAP.add(ap.get(m));
			}
			m++;
		}
		System.out.println("Adversary Strategy: "
				+ ruggedObj.getAdversaryStrategy());
		System.out
		.println("Adversary Paths: " + ruggedObj.getAdversaryPaths());

		//ruggedObj.writeMeasure();
		//System.out.flush();
		//System.err.flush();
		//System.out.print(ruggedObj.getcutoffUsed());
		//ruggedObj.end();

		//		try {
		//			testQuickSolve(graph,usedAP);
		//		} catch (LPSolverException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		return uspObj;

	}

	public static void testBetterRespPROBS(AbstractBaseGraph graph, String[] args){

		List<Double> resources = new ArrayList<Double>();
		resources.add(1.0);
		resources.add(1.0);
		//resources.add(0.0);

		List<Double> coverage = new ArrayList<Double>();
		coverage.add(1.0);
		coverage.add(1.0);
		//coverage.add(5.0);

		List<Double> Probability = new ArrayList<Double>();
		//Probability.add(0.5);
		//Probability.add(0.9);
		Probability.add(0.5);
		Probability.add(0.5);

		TeamBuildingProblem uspObj = new TeamBuildingProblem(resources, coverage,  Probability);

		int numNodes = graph.vertexSet().size();

		boolean ENABLE_FILE_WRITE = true;

		//String OUTPUTFOLDER = args[6];
		String CONFIG = "d a qE zS"; 
		/**
		 * CONFIG: s: skeleton f: skeleton - better defender response t:
		 * skeleton - better attacker response d: better defender response a:
		 * better attacker response o: converge only with better responses w:
		 * converge with better responses first, then use best responses
		 */

		double r = 0.1;
		if ( args.length > 8) {
			System.out.println("Setting r: " + args[8]);
			r = new Double(args[8]);
		}

		double betterResponseEpsilon = 0.001;
		if ( args.length > 9) {
			System.out.println("Setting brE: " + args[9]);
			betterResponseEpsilon = new Double(args[9]);
		}

		double finalConvergenceEpsilon = 0.002;
		if ( args.length > 10) {
			System.out.println("Setting fcE: " + args[10]);
			finalConvergenceEpsilon = new Double(args[10]);
		}	


		int targetVal = 100; // Random integer between 1 and this number

		System.out.flush();
		finalConvergenceEpsilon = 0.002;

		uspObj.setGraph(graph);

		RuggedBetterResponsesCutoff ruggedObj = new RuggedBetterResponsesCutoff(uspObj);

		ruggedObj.setNumRoundsBeforeDiscard(100);
		ruggedObj.setEnableDiscardUselessActions(false);

		// ruggedObj.setkShortestPaths(2);

		ruggedObj.setEnableSkeleton(CONFIG.contains("s"), CONFIG.contains("f"),
				CONFIG.contains("t"));
		ruggedObj.setEnableBetterDefenderOracle(CONFIG.contains("d"));
		ruggedObj.setEnableBetterAttackerOracle(CONFIG.contains("a"));
		ruggedObj.setOnlyWarmStartConverge(CONFIG.contains("o"));
		ruggedObj.setEnableWarmStartConverge(CONFIG.contains("w"));

		ruggedObj.setFinalConvergenceEpsilon(finalConvergenceEpsilon);
		ruggedObj.setBetterResponseEpsilon(betterResponseEpsilon);

		int warmStartCount = 5;
		if ( args.length > 11) {
			System.out.println("Setting wSC: " + args[11]);
			warmStartCount = new Integer(args[11]);
		}

		List<model.teamBuildingModels.DefenderAllocation> daList = null;
		if ( CONFIG.contains("qM") ) {
			daList = ruggedObj.getWarmStartDefenderMinCutEdges(warmStartCount);
		} else if ( CONFIG.contains("qE") ) {
			daList = ruggedObj.getWarmStartDefenderRandomEdges(warmStartCount);
		} else if ( CONFIG.contains("qR") ) {
			//daList = ruggedObj.getWarmStartDefenderRanger();	
		}

		List<model.teamBuildingModels.AdversaryPath> apList = null;
		if ( CONFIG.contains("zR")) {
			apList = ruggedObj.getWarmStartAttackerRanger();	
		} else if ( CONFIG.contains("zS")) {
			apList = ruggedObj.getWarmStartAttackerShortestPath();
		}				 

		ruggedObj.warmStart(daList, apList);

		try {
			ruggedObj.run();
		} catch (MalformedGraphException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LPSolverException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
		}

		System.out.flush();
		System.err.flush();


		System.out.println("GameValue: "
				+ (ruggedObj.getGameValue()));
		System.out.println("Final Run Time: "
				+ (ruggedObj.getRunTime() * 1.0 / 1000));
		System.out.println("Final Number of Iterations: "
				+ ruggedObj.getNumberOfIterations());
		System.out.println("Final Total Time: "
				+ (ruggedObj.getTotalTime() / 1000.00));
		System.out.println("BDO Iterations: " + ruggedObj.betterdeforaclecount);
		System.out.println("DO Iterations: " + ruggedObj.deforaclecount);



		System.out.println("Strategy: ");
		List<model.teamBuildingModels.DefenderAllocation> dalist = ruggedObj.getDefenderAllocations();
		List<Double> strategy = ruggedObj.getDefenderStrategy();
		assert(dalist.size() == strategy.size());
		for ( int i = 0; i < strategy.size(); i++ ) {
			if ( strategy.get(i) > lpWrapper.Configuration.EPSILON) {
				System.out.println(dalist.get(i) + ": " + strategy.get(i));
			}
		}


		int j=0;
		for(int i=0;i<ruggedObj.getDefenderStrategy().size();i++){
			if(!ruggedObj.getDefenderStrategy().get(i).equals(0.0)){
				j++;
				System.out.println("Defender Strategy "+j+" : "
						+ ruggedObj.getDefenderStrategy().get(i));
				model.teamBuildingModels.DefenderAllocation da = ruggedObj.getDefenderAllocations().get(i);
				for(String r1 :da.getResourceMap().keySet()){
					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
				}
				System.out.println();
			}

		}

		System.out.println("Adversary Strategy: "
				+ ruggedObj.getAdversaryStrategy());
		System.out
		.println("Adversary Paths: " + ruggedObj.getAdversaryPaths());




		//ruggedObj.writeMeasure();
		System.out.flush();
		System.err.flush();

		ruggedObj.end();

	}

	public static List<Double> testQuickSolve(AbstractBaseGraph graph, List<AdversaryPath> ap) throws LPSolverException{


		List<Double> costs = new ArrayList<Double>();
		costs.add(2.0);
		//costs.add(6.0);
		//costs.add(5.0);

		List<Double> resources = new ArrayList<Double>();
		resources.add(1.0);
		//resources.add(1.0);

		List<Double> coverage = new ArrayList<Double>();
		coverage.add(4.0);
		//coverage.add(3.0);
		//coverage.add(2.0);

		List<Double> prob = new ArrayList<Double>();
		prob.add(0.5);
		prob.add(0.5);

		TeamBuildingProblem team = new TeamBuildingProblem(resources, coverage);
		//TeamBuildingProblem team = new TeamBuildingProblem(20, costs, coverage);
		team.setGraph(graph);


		CompactProblem test = new CompactProblem(team);
		test.lstAP=ap;

		//test.getCompactGraph();
		//System.out.println(test.solveQuick()+"\n");
		//System.out.println(test.runtime+"\n");
		System.out.println("GV: "+test.solve(0)+"\n");
		System.out.println("RT: "+test.runtime+"\n");

		if(test.ruggedObj!=null){
			for(int i=0;i<test.compactGraph.attackerPaths.size();i++){
				System.out.println(test.compactGraph.attackerPaths.get(i)+": "+test.ruggedObj.getAdversaryStrategy().get(i));
			}

			int j=0;
			for(int i=0;i<test.ruggedObj.getDefenderStrategy().size();i++){
				if(!test.ruggedObj.getDefenderStrategy().get(i).equals(0.0)){
					j++;
					System.out.println("Defender Strategy "+j+" : "
							+ test.ruggedObj.getDefenderStrategy().get(i));
					model.teamBuildingModels.DefenderAllocation da = test.ruggedObj.getDefenderAllocations().get(i);
					for(String r1 :da.getResourceMap().keySet()){
						System.out.println(r1+ ":" + da.getResourceMap().get(r1));
					}
					System.out.println();
				}

			}
		}else{


			for(int i=0;i<test.compactGraph.attackerPaths.size();i++){
				System.out.println(test.compactGraph.attackerPaths.get(i)+": "+test.strategy.getAttackerStrategy().get(i));
			}
		}


		//		int j=0;
		//		for(int i=0;i<test.strategy.defenderStrategy.size();i++){
		//			if(!test.strategy.defenderStrategy.get(i).equals(0.0)){
		//				j++;
		//				System.out.println("Defender Strategy "+j+" : "
		//						+ test.teamProb.getActProfile().getDefenderAllocations().get(i));
		//				model.teamBuildingModels.DefenderAllocation da = test.teamProb.getActProfile().getDefenderAllocations().get(i);
		//				for(String r1 :da.getResourceMap().keySet()){
		//					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
		//				}
		//				System.out.println();
		//			}
		//			
		//		}
		//		
		//		int j=0;
		//		for(int i=0;i<test.strategy.defenderStrategy.size();i++){
		//			if(!test.strategy.defenderStrategy.get(i).equals(0.0)){
		//				j++;
		//				System.out.println("Defender Strategy "+j+" : "
		//						+ test.strategy.defenderStrategy.get(i));
		//				model.teamBuildingModels.DefenderAllocation da = test.strategy.defenderAllocation.get(i).toDefenderAllocation();
		//				for(String r1 :da.getResourceMap().keySet()){
		//					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
		//				}
		//				System.out.println();
		//			}
		//			
		//		}

		List<Double> r=null;


		for(int i=0;i<test.strategy.getDefenderStrategy().size();i++){
			double p = test.strategy.getDefenderStrategy().get(i);
			if(p>0){
				System.out.println(p+": "+test.strategy.defenderAllocation.get(i).defenderCoverage);
			}
		}

		return r;
	}

	public static List<Double> testCompactProblem(AbstractBaseGraph graph,List<Double> resources, List<Double> coverage, List<Double> Prob, List<AdversaryPath> ap) throws LPSolverException{


		TeamBuildingProblem team = new TeamBuildingProblem(resources, coverage);
		//TeamBuildingProblem team = new TeamBuildingProblem(20, costs, coverage);
		team.setGraph(graph);


		CompactProblem test = new CompactProblem(team);

		if(ap!=null){
			//test.inputPaths(ap);
		}
		//test.getCompactGraph();
		//System.out.println(test.solveQuick()+"\n");
		//System.out.println(test.runtime+"\n");
		System.out.println("GV: "+test.solve(0)+"\n");
		System.out.println("RT: "+test.runtime+"\n");

		if(test.ruggedObj!=null){
			for(int i=0;i<test.attackerPaths.size();i++){
				System.out.println(test.attackerPaths.get(i)+": "+test.ruggedObj.getAdversaryStrategy().get(i));
			}

			int j=0;
			for(int i=0;i<test.ruggedObj.getDefenderStrategy().size();i++){
				if(!test.ruggedObj.getDefenderStrategy().get(i).equals(0.0)){
					j++;
					System.out.println("Defender Strategy "+j+" : "
							+ test.ruggedObj.getDefenderStrategy().get(i));
					model.teamBuildingModels.DefenderAllocation da = test.ruggedObj.getDefenderAllocations().get(i);
					for(String r1 :da.getResourceMap().keySet()){
						System.out.println(r1+ ":" + da.getResourceMap().get(r1));
					}
					for(String r1 :da.getResourceMap().keySet()){
						System.out.println(r1+ ":" + da.getResourceMap().get(r1));
					}
					System.out.println();
				}

			}
		}else{


			for(int i=0;i<test.attackerPaths.size();i++){
				System.out.println(test.attackerPaths.get(i)+": "+test.strategy.getAttackerStrategy().get(i));
			}
		}


		//		int j=0;
		//		for(int i=0;i<test.strategy.defenderStrategy.size();i++){
		//			if(!test.strategy.defenderStrategy.get(i).equals(0.0)){
		//				j++;
		//				System.out.println("Defender Strategy "+j+" : "
		//						+ test.teamProb.getActProfile().getDefenderAllocations().get(i));
		//				model.teamBuildingModels.DefenderAllocation da = test.teamProb.getActProfile().getDefenderAllocations().get(i);
		//				for(String r1 :da.getResourceMap().keySet()){
		//					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
		//				}
		//				System.out.println();
		//			}
		//			
		//		}
		//		
		//		int j=0;
		//		for(int i=0;i<test.strategy.defenderStrategy.size();i++){
		//			if(!test.strategy.defenderStrategy.get(i).equals(0.0)){
		//				j++;
		//				System.out.println("Defender Strategy "+j+" : "
		//						+ test.strategy.defenderStrategy.get(i));
		//				model.teamBuildingModels.DefenderAllocation da = test.strategy.defenderAllocation.get(i).toDefenderAllocation();
		//				for(String r1 :da.getResourceMap().keySet()){
		//					System.out.println(r1+ ":" + da.getResourceMap().get(r1));
		//				}
		//				System.out.println();
		//			}
		//			
		//		}

		List<Double> r=null;


		for(int i=0;i<test.strategy.getDefenderStrategy().size();i++){
			double p = test.strategy.getDefenderStrategy().get(i);
			if(p>0){
				System.out.println(p+": "+test.strategy.defenderAllocation.get(i).defenderCoverage);
			}
			CompactAllocation ca=test.strategy.defenderAllocation.get(i);
			for(String r2 : ca.DA.keySet()){
				System.out.println(r2 + ca.DA.get(r2));
			}


		}

		return r;
	}	
	public static String BruteForce(AbstractBaseGraph graph, double budget) throws MalformedGraphException, LPSolverException{

		teamNode root = new teamNode("root", null, 0, budget, false);
		root.teamObj.setGraph(graph);

		double tick = System.currentTimeMillis();
		BruteForce tree = new BruteForce(root.teamObj, budget);	

		try {
			teamNode result = tree.Search(root); 
			double tock =  System.currentTimeMillis();


			String results = 	"Budget: " + budget +", verticies: "
					+graph.vertexSet().size() + ", edges: "
					+result.teamObj.getGraph().edgeSet().size() + ", GV: "
					+result.getEvaluation() + ", runtime(S): " 
					+(tock-tick)/1000 + ", resources: " 
					+result.resourcesList;

			System.out.println("\n"+results);
			return results;

		} catch (OutOfMemoryError E) {
			double tock =  System.currentTimeMillis();
			String results=""+ (tock-tick)/1000;
			System.out.println(results);
			return results;

		}

	}
	public static String testAttacker(AbstractBaseGraph graph, double budget) throws MalformedGraphException, LPSolverException{

		teamNode root = new teamNode("root", null, 0, budget, false);
		root.teamObj.setGraph(graph);

		double tick = System.currentTimeMillis();
		BruteForce tree = new BruteForce(root.teamObj, budget);	

		try {
			List<teamNode> result = tree.testAttacker(root); 
			double tock =  System.currentTimeMillis();


			String results ="Budget: " + budget +", verticies: "
					+graph.vertexSet().size() + ", edges: "
					+graph.edgeSet().size()+", maxval: "+root.teamObj.maxTarget;
			for(teamNode n : result){
				results += n.heuristicEval+", "+n.Hrt/1000+", "+n.getEvaluation()+", "+n.evalTime/1000+"\n";
			}

			System.out.println("\n"+results);
			return results;

		} catch (OutOfMemoryError E) {
			double tock =  System.currentTimeMillis();
			String results=""+ (tock-tick)/1000;
			System.out.println(results);
			return results;

		}

	}
	public static String smartBruteForce(AbstractBaseGraph graph, double budget) throws MalformedGraphException, LPSolverException{

		teamNode root = new teamNode("root", null, 0, budget, false);
		root.teamObj.setGraph(graph);

		double tick = System.currentTimeMillis();
		BruteForce tree = new BruteForce(root.teamObj, budget);	

		try {
			teamNode result = tree.smartSearch(root); 
			double tock =  System.currentTimeMillis();
			System.out.println("done");	 

			String results = 	"Budget: " + budget +", verticies: "
					+graph.vertexSet().size() + ", edges: "
					+result.teamObj.getGraph().edgeSet().size() + ", GV: "
					+result.getEvaluation() + ", runtime(S): " 
					+(tock-tick)/1000 + ", resources: " 
					+result.resourcesList;

			System.out.println("\n"+results);
			return results;

		} catch (OutOfMemoryError E) {
			double tock =  System.currentTimeMillis();
			String results=""+ (tock-tick)/1000;
			System.out.println(results);
			return results;

		}

	}
	public static void oldPQSearch(AbstractBaseGraph graph, double budget,int bound, double s, int t) throws MalformedGraphException, LPSolverException, FileNotFoundException{
		String output = TeamsPermuteCheck.create(budget); 


		double tick = System.currentTimeMillis();
		CompareParallel tree = new CompareParallel();	
		PQsearch.teamNode result=  tree.runSequentialPQ(graph, bound, 10, output);
		//PQsearch.teamNode result = tree.Search(graph, budget);
		double tock =  System.currentTimeMillis();


		String results = 	"Budget: "+ budget +", verticies: "
				+graph.vertexSet().size() + ", edges: "
				+graph.edgeSet().size() + ", GV: "
				+result.getEvaluation() + ", runtime(s): " 
				+(tock-tick)/1000 + ", resources: " 
				+result.getName();

		System.out.println(results);

	}
	public static void warmStartPaths(){

	}


	public static String SearchCutoff(AbstractBaseGraph graph, double budget, int t, boolean pad) throws MalformedGraphException, LPSolverException{

		teamNode root = new teamNode("root", null, 0, budget, pad);
		root.teamObj.setGraph(graph);

		//root.getBestResponsePaths(graph,root.teamObj.ResourceCoverage,root.teamObj.CoverageProbability);
		Search tree = new Search(root.teamObj, budget);
		tree.t=t;
		double tick = System.currentTimeMillis();
		teamNode result = tree.cutoffSearch(root);
		double tock =  System.currentTimeMillis();


		//		 String results = 	"Budget: " + budget +", verticies: "
		//				 			+graph.vertexSet().size() + ", edges: "
		//				 			+result.teamObj.getGraph().edgeSet().size() + ", GV: "
		//				 			+result.getEvaluation() + ", runtime(S): " 
		//				 			+(tock-tick)/1000 + ", resources: " 
		//				 			+result.resourcesList;
		String results = budget +","+graph.vertexSet().size() + ","
				+result.teamObj.getGraph().edgeSet().size() + ", value "
				+result.getEvaluation() + ", time " 
				+(tock-tick)/1000 + "," 
				+result.resourcesList.get(0)+", "+result.resourcesList.get(1)+", "+result.resourcesList.get(2)+", "+result.resourcesList.get(3)+","+result.resourcesList.get(4)
				+result.resourcesList.get(5)+","+result.resourcesList.get(6)+","+result.resourcesList.get(7)+","+result.resourcesList.get(8)+","+result.resourcesList.get(9);
		System.out.println("\n"+results);
		return results;
	}




	public static String subSearch(AbstractBaseGraph graph, double budget, int t) throws MalformedGraphException, LPSolverException{
		double tick = System.currentTimeMillis();
		teamNode root = new teamNode("root", null, 0, budget, false);
		root.teamObj.setGraph(graph);



		Search tree = new Search(root.teamObj, budget);
		tree.t=t;

		teamNode result = tree.submodularSearch(root);
		double tock =  System.currentTimeMillis();


		String results = 	"Budget: " + budget +", verticies: "
				+graph.vertexSet().size() + ", edges: "
				+result.teamObj.getGraph().edgeSet().size() + ", GV: "
				+result.getEvaluation() + ", runtime(S): " 
				+(tock-tick)/1000 + ", resources: " 
				+result.resourcesList;

		System.out.println("\n"+results);
		return results;

	}
	public static void setSearchSpace(int size){
		int [] resourcecost = new int[size];
		int [] resourcecoverage = new int[size];
		double [] resourceprob = new double[size];

		for(int i=0;i<size;i++){
			resourcecost[i]=1;
			resourcecoverage[i]=1+i;
			resourceprob[i]=1.0;
		}

		teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, false);


	}



	/* ========================================================
    M_dp(W) = max value achieved by knapsack with capacity W
    ======================================================== */
	static double M_dp(double[] v, int[] w, int W)
	{
		double[] sol, mySol;
		int i;
		double myFinalSol;

		double[] M;                 // Data structure to store results
		int   C;                 // Index to run through M[]

		sol   = new double[v.length];
		mySol = new double[v.length];

		M = new double[W + 1];      // Create array

		/* ---------------------------
       Base cases
       --------------------------- */
		M[0] = 0;

		/* ==============================================
       The other values M[C]
       ============================================== */
		for ( C = 1; C <= W; C++ )
		{
			/* ---------------------------------------
          Solve the appropriate smaller problems
          --------------------------------------- */
			for ( i = 0; i < v.length; i++ )
			{
				if ( C >= w[i] )
					sol[i] = M[ C-w[i] ]; // Knapsack capacity reduced by w[i]
				// because it has item i packed in
				// it already
				else
					sol[i] = 0;        // Not enough space to  pack item i
			}

			/* ---------------------------------------------
          Use the solutions to the smaller problems
          to solve original problem
          --------------------------------------------- */
			for ( i = 0; i < v.length; i++ )
			{
				if ( C >= w[i] ){
					mySol[i] = sol[i] + v[i];   // Value is increased by v[i]
					// because it has item i packed in
					// it already
				}else
					mySol[i] = 0;        // Not enough space to  pack item i
			}

			/* *************************
          Find the best (maximum)
			 ************************* */
			M[C] = mySol[0];
			for ( i = 1; i < v.length; i++ )
				if ( mySol[i] > M[C] )
					M[C] = mySol[i];
		}

		return M[ W ];     // Return best value for knapsack of cap = W
	}

	static double[] recoverValues(int[] resourcecoverage, double[] resourceprob, int[] resourcecost, int budget, AbstractBaseGraph graph) throws MalformedGraphException, LPSolverException{
		double [] valuesarray = new double[resourcecost.length];
		//		int[] rescov = new int[resourcecost.length];
		//		int[] rescost = new int[resourcecost.length];
		//		double[] resprob = new double[resourcecost.length];

		for(int ii=0;ii<resourcecoverage.length;ii++){
			System.out.println("Here "+ii);
			int[] rescov = new int[resourcecost.length];
			int[] rescost = new int[resourcecost.length];
			double[] resprob = new double[resourcecost.length];
			rescov[ii] = resourcecoverage[ii];
			rescost[ii] = resourcecost[ii];
			resprob[ii] = resourceprob[ii];
			boolean useProbabilities = false;
			teamNode.setTeams(rescost, rescov, resprob, useProbabilities);
			String result = PQSearch(graph, budget, 1, false);
			String[] splitted_result = result.split(",");
			valuesarray[ii] = Double.parseDouble(splitted_result[3]);
			System.out.println("Done "+ii);
		}
		return valuesarray;
	}


	public static String PQSearch(AbstractBaseGraph graph, double budget, int t, boolean pad) throws MalformedGraphException, LPSolverException{
		double tick_pq = System.currentTimeMillis();
		double tock_pq = 0;
		teamNode root = new teamNode("root", null, 0, budget, pad);
		root.teamObj.setGraph(graph);

		//root.getBestResponsePaths(graph,root.teamObj.ResourceCoverage,root.teamObj.CoverageProbability);
		Search tree = new Search(root.teamObj, budget);
		tree.t=t;

		teamNode result = tree.PQSearch(root);
		tock_pq =  System.currentTimeMillis();

		String results = budget +", "+result.getEvaluation() + ", "+(tock_pq-tick_pq)/1000 + ", " 
				+result.resourcesList.get(0)+", "+result.resourcesList.get(1)+", "+result.resourcesList.get(2)+", "
				+result.resourcesList.get(3)+", "+result.resourcesList.get(4)+", "+result.resourcesList.get(5);
		return results;
	}

	public static double[] ExactValuesForSingletons(AbstractBaseGraph graph, int budget,
			int[] resourcecoverage,	double[] resourceprob,int[] resourcecost, 
			boolean useProbabilities,int max_target_value){
		int len = resourcecost.length;
		int[] resources = {1};
		double[] computed_values = new double[len];
		Arrays.fill(computed_values, -99999.99);

		for(int ii=0;ii<len;ii++){
			int[] curr_cov = {resourcecoverage[ii]};
			double[] curr_prob = {resourceprob[ii]};
			int[] curr_cost = {resourcecost[ii]};

			teamNode.setTeams(curr_cost,curr_cov,curr_prob,useProbabilities);
			teamNode root = new teamNode("root", null, 0, budget, false);
			root.teamObj.setGraph(graph);			
			root.setResources(resources);
			try {
				root.setFullBetterRuggedEvaluation();
				computed_values[ii] = root.getEvaluation()+max_target_value;
			} catch (MalformedGraphException e) {
				e.printStackTrace();
			} catch (LPSolverException e) {
				e.printStackTrace();
			}
		}
		return computed_values;
	}

	public static LinkedList<teamNode> ListAllSaturatingTeams(AbstractBaseGraph graph, int budget, int t,boolean pad,
			int[] resourcecoverage,	double[] resourceprob,int[] resourcecost, boolean useProbabilities)
					throws MalformedGraphException, LPSolverException{

		teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, useProbabilities);
		teamNode root = new teamNode("root", null, 0, budget, pad);
		root.teamObj.setGraph(graph);
		Search tree = new Search(root.teamObj, budget);
		tree.t=t;
		LinkedList<teamNode> teamList= tree.ListTeam(root);
		return teamList;

	}

	public static boolean[][] ComputeMatrixOfDifferences(LinkedList<teamNode> teamList){
		int nTeams = teamList.size();
		boolean[][] matrixOfDifferences = new boolean[nTeams][nTeams];
		for(int rows=0;rows<nTeams;rows++){
			int[] firstTeam = teamList.get(rows).resources;
			for(int columns=0;columns<nTeams;columns++){
				int[] secondTeam = teamList.get(columns).resources;
				int[] differenceArray = new int[teamList.get(0).resources.length];

				for(int ii=0;ii<differenceArray.length;ii++){
					differenceArray[ii] = differenceArray[ii] + Math.abs(firstTeam[ii] - secondTeam[ii]);
				}
				
				int sumOfDifferentElements = 0;
				for(int ii=0;ii<differenceArray.length;ii++){
					sumOfDifferentElements = sumOfDifferentElements + differenceArray[ii];
				}

				if(sumOfDifferentElements == 2)
					matrixOfDifferences[rows][columns] = true;
			}
		}
		return matrixOfDifferences;
	}

	public static int[] ComputeConnectedComponents(boolean[][] adjacencyMatrix, int nTeams){
		int indexOfComponents = 0;
		int[] components = new int[nTeams];

		for(int ii=0;ii<nTeams;ii++){
			if(components[ii] == 0){
				indexOfComponents++;
				Queue<Integer> queue = new LinkedList<Integer>();
				queue.add(ii);
				components[ii] = indexOfComponents;
				while (!queue.isEmpty()){
					Integer current = queue.remove();
					for (int i = 0; i < nTeams; ++i)
						if (adjacencyMatrix[current][i] && components[i]==0){
							components[i] = indexOfComponents;
							queue.add(i);
						}
				}
			}
		}
		return components;
	}


	public static int ComputeTeamDegree(int index, boolean[][] matrixOfDifferences){
		int teamDegree = 0;
		for(int ii=0;ii<matrixOfDifferences[0].length;ii++)
			if(matrixOfDifferences[index][ii])
				teamDegree = teamDegree + 1;
		return teamDegree;
	}

	public static double EvaluateTeam(AbstractBaseGraph graph,int budget,int[] teamResources, int[] resourceCoverage, double[] resourceProb,
			int[] resourceCost, boolean useProbabilities, int maxTargetValue){
		teamNode.setTeams(resourceCost, resourceCoverage, resourceProb, useProbabilities);
		teamNode root = new teamNode("root", null, 0, budget, false);
		root.teamObj.setGraph(graph);
		root.setResources(teamResources);
		try {
			root.setFullBetterRuggedEvaluation();
		} catch (MalformedGraphException e) {
			e.printStackTrace();
		} catch (LPSolverException e) {
			e.printStackTrace();
		}
		return root.getEvaluation()+maxTargetValue;
	}

	public static int ComputeDifferentResourceOfFirstTeam(int[] firstTeam, int[] secondTeam){
		int differentResource = -1;
		for(int ii=0;ii<firstTeam.length;ii++)
			if((firstTeam[ii]-secondTeam[ii]) > 0)
				differentResource = ii;
		return differentResource;
	}

	public static int ComputeDifferentResourceOfSecondTeam(int[] firstTeam, int[] secondTeam){
		int differentResource = -1;
		for(int ii=0;ii<firstTeam.length;ii++)
			if((firstTeam[ii]-secondTeam[ii]) < 0)
				differentResource = ii;
		return differentResource;
	}

	
	/**
	 * Goal: given a set of resources and a graph, compute the best team and the best allocation for that team on the graph.
	 * 
	 * Diffusion works as follows:
	 *  1 - solve the problem for all the singletons, i.e., the single resources;
	 *  2 - list all the teams that saturate the budget;
	 *  3 - compute the differences in terms of resources among the various teams;
	 *  4 - create clusters according to differences: if two resources differ for just one resource, they are put in the same cluster;
	 *  5 - for each cluster, evaluate exactly the team that has the higher degree;
	 *  6 - update the upper bounds UBs and the lower bounds LBs for all the other teams in the cluster according to the bounding rules; 
	 *  7 - call T the team with the highest value among the ones evaluated. If the value of T is higher than all the other UBs, return T, else continue;
	 *  8 - for each cluster, evaluate exactly the team with the highest upper bound;
	 *  9 - update UBs and LBs for all the other teams in the cluster;
	 * 10 - call T the team with the highest value among the ones evaluated. If the value of T is higher than all the other UBs, return T, else go to 8.
	 * 
	 * Inputs and outputs:
	 * @param graph: the input graph representing the environment. The graph has some source nodes, target nodes and intermediate targets;
	 * @param budget: the budget we can spend to build the team;
	 * @param t: this parameter tells which type of responses should be computed. For Diffusion, it is always set to 1;
	 * @param pad: a boolean variable employed in a search. For Diffusion, it is always set to false (actually we do not care);
	 * @param resourceCoverage: an array containing the number of edges each type of resource can cover;
	 * @param resourceProb: an array containing the detection probability each type of resource has;
	 * @param resourceCost: an array containing the cost of each type of resource;
	 * 
	 * @return results: a single string obtained concatenating the following information:
	 * - budget;
	 * - value of the best allocation of the best team, i.e., the value of the game for the Defender;
	 * - the time required for an entire run of Diffusion;
	 * - an integer for each type of resource. Such values tell how many resources of that type are in the team. 
	 * All the information is saved in a .csv file, one data per column. Different rows correspond to a different run.
	 */
	
	public static String Diffusion(AbstractBaseGraph graph, int budget, int t, boolean pad,
			int[] resourceCoverage, double[] resourceProb, int[] resourceCost)
					throws MalformedGraphException, LPSolverException{
		double tickDiff = System.currentTimeMillis();
		boolean useProbabilities = true;

		//Compute the value of the most valuable target.
		int maxTargetValue = 0;
		Set<INode> targets = getTargetSet(graph);
		for (INode node: targets)
			if(node.getPayoff() > maxTargetValue)
				maxTargetValue = node.getPayoff();

		//Solve the problem for all the singletons computing f, not U. f = U+max_target_value.
		//We compute f since we know it is submodular.
		double[] computedValues = ExactValuesForSingletons(graph, budget, 
				resourceCoverage, resourceProb, resourceCost, useProbabilities, maxTargetValue);

		//Compute all the teams that saturates the budget.
		LinkedList<teamNode> teamList = ListAllSaturatingTeams(graph, budget, t,pad,
				resourceCoverage, resourceProb, resourceCost, useProbabilities);
		int numberOfTeams = teamList.size();

		//Build an adjacency matrix with dimensions given by the number of teams.
		//M(T_i,T_j) = true if teams T_i and T_j differ for one single resource.
		boolean[][] matrixOfDifferences = ComputeMatrixOfDifferences(teamList);

		//Create the connected components and compute the team of each component with the highest degree;
		//Set LB for all the teams to zero;
		//Set UB for all the teams to the maximum value of the targets.
		int[] componentsIndices = ComputeConnectedComponents(matrixOfDifferences, numberOfTeams);
		int numberOfComponents = 0;
		for(int ii=0;ii<componentsIndices.length;ii++)
			if(componentsIndices[ii] > numberOfComponents)
				numberOfComponents = componentsIndices[ii];

		ArrayList<ArrayList<int[]>> components = new ArrayList<ArrayList<int[]>>();
		ArrayList<int[]> bestTeamOfEachComponent = new ArrayList<int[]>();
		ArrayList<Integer> indicesOfBestTeamOfEachComponent = new ArrayList<Integer>();
		ArrayList<double[]> lowerBounds = new ArrayList<double[]>();
		ArrayList<double[]> upperBounds = new ArrayList<double[]>();

		for(int ii=1;ii<=numberOfComponents;ii++){
			indicesOfBestTeamOfEachComponent.add(0);
			ArrayList<int[]> currentComponent = new ArrayList<int[]>();
			int[] bestTeamOfComponent = new int[resourceCoverage.length];
			int  maxDegree = 0;
			int count = -1;
			for(int jj=0;jj<numberOfTeams;jj++){
				if(componentsIndices[jj] == ii){
					count++;
					currentComponent.add(teamList.get(jj).resources);
					int currentTeamDegree = ComputeTeamDegree(jj, matrixOfDifferences);
					if(currentTeamDegree > maxDegree){
						bestTeamOfComponent = teamList.get(jj).resources;
						maxDegree = currentTeamDegree;
						indicesOfBestTeamOfEachComponent.set(ii-1, count);			
					}		
				}
			}
			components.add(ii-1, currentComponent);
			bestTeamOfEachComponent.add(bestTeamOfComponent);
			double[] lowerBoundsOfTeam = new double[components.get(ii-1).size()];
			lowerBounds.add(lowerBoundsOfTeam);
			double[] upperBoundsOfTeam = new double[components.get(ii-1).size()];
			Arrays.fill(upperBoundsOfTeam, maxTargetValue);
			upperBounds.add(upperBoundsOfTeam);
		}

		//For each component, evaluate the team with the highest degree and 
		//set its LB and UB equal to its real value		
		double[] bestTeamsEvaluations = new double[numberOfTeams];
		for(int ii=0;ii<bestTeamOfEachComponent.size();ii++){
			bestTeamsEvaluations[ii] = EvaluateTeam(graph, budget, bestTeamOfEachComponent.get(ii), 
					resourceCoverage, resourceProb, resourceCost, useProbabilities, maxTargetValue);
		}
		for(int ii=0;ii<upperBounds.size();ii++){
			double[] oldUpperBounds = upperBounds.get(ii);
			double[] updatedUpperBounds = oldUpperBounds;
			updatedUpperBounds[indicesOfBestTeamOfEachComponent.get(ii)] = bestTeamsEvaluations[ii];
			upperBounds.set(ii, updatedUpperBounds);
		}
		for(int ii=0;ii<lowerBounds.size();ii++){
			double[] oldLowerBounds = lowerBounds.get(ii);
			double[] updatedLowerBounds = oldLowerBounds;
			updatedLowerBounds[indicesOfBestTeamOfEachComponent.get(ii)] = bestTeamsEvaluations[ii];
			lowerBounds.set(ii, updatedLowerBounds);
		}

		//Update LB and UB of every team of each component
		for(int ii=0;ii<components.size();ii++){
			int[] bestCurrentTeam = bestTeamOfEachComponent.get(ii);
			for(int jj=0;jj<components.get(ii).size();jj++){
				int firstTeamDifferentResource = ComputeDifferentResourceOfFirstTeam(bestCurrentTeam,components.get(ii).get(jj));
				int secondTeamDifferentResource = ComputeDifferentResourceOfSecondTeam(bestCurrentTeam,components.get(ii).get(jj));
				if(firstTeamDifferentResource >= 0 && secondTeamDifferentResource >= 0){
					if(computedValues[firstTeamDifferentResource] > computedValues[secondTeamDifferentResource]){
						double possibleLowerBound = bestTeamsEvaluations[ii] - computedValues[firstTeamDifferentResource];
						if(possibleLowerBound > lowerBounds.get(ii)[jj])
							lowerBounds.get(ii)[jj] = possibleLowerBound;
						double firstPossibleUpperBound = bestTeamsEvaluations[ii] + computedValues[secondTeamDifferentResource];
						double secondPossibleUpperBound = 2*bestTeamsEvaluations[ii];
						if(firstPossibleUpperBound < secondPossibleUpperBound && firstPossibleUpperBound < upperBounds.get(ii)[jj])
							upperBounds.get(ii)[jj] = firstPossibleUpperBound;
						if(secondPossibleUpperBound < firstPossibleUpperBound && secondPossibleUpperBound < upperBounds.get(ii)[jj])
							upperBounds.get(ii)[jj] = secondPossibleUpperBound;
					}else{
						double firstPossibleLowerBound = bestTeamsEvaluations[ii] - computedValues[firstTeamDifferentResource];
						double secondPossibleLowerBound = (0.5)*bestTeamsEvaluations[ii];
						if(firstPossibleLowerBound > secondPossibleLowerBound && firstPossibleLowerBound > lowerBounds.get(ii)[jj])
							lowerBounds.get(ii)[jj] = firstPossibleLowerBound;
						if(secondPossibleLowerBound > firstPossibleLowerBound && secondPossibleLowerBound > lowerBounds.get(ii)[jj])
							lowerBounds.get(ii)[jj] = secondPossibleLowerBound;
						double possibleUpperBound = bestTeamsEvaluations[ii] + computedValues[secondTeamDifferentResource];
						if(possibleUpperBound < upperBounds.get(ii)[jj])
							upperBounds.get(ii)[jj] = possibleUpperBound;
					}
				}
			}
		}

		//Confront the value of the team evaluated exactly with the highest value w.r.t. UB of all the other teams
		//If it is higher, return such team, otherwise continue
		double highestValueOfFirstIteration = 0;
		int teamWithTheHighestValue = -1;
		for(int ii=0;ii<bestTeamsEvaluations.length;ii++){
			if(bestTeamsEvaluations[ii] > highestValueOfFirstIteration){
				highestValueOfFirstIteration = bestTeamsEvaluations[ii];
				teamWithTheHighestValue = ii;
			}
		}
		boolean best = true;
		for(int ii=0;ii<upperBounds.size();ii++){
			double[] upperBoundsOfComponent = upperBounds.get(ii);
			for(int jj=0;jj<upperBoundsOfComponent.length;jj++)
				if(upperBoundsOfComponent[jj] > highestValueOfFirstIteration)
					best = false;
		}

		if(best){
			double tockDiff = System.currentTimeMillis();
			String results = budget +", "+highestValueOfFirstIteration+ ", "+(tockDiff-tickDiff)/1000 + ", " 
					+bestTeamOfEachComponent.get(teamWithTheHighestValue)[0]+", "
					+bestTeamOfEachComponent.get(teamWithTheHighestValue)[1]+", "
					+bestTeamOfEachComponent.get(teamWithTheHighestValue)[2]+", "
					+bestTeamOfEachComponent.get(teamWithTheHighestValue)[3]+", "
					+bestTeamOfEachComponent.get(teamWithTheHighestValue)[4]+", "
					+bestTeamOfEachComponent.get(teamWithTheHighestValue)[5];
			return results;
		}

		//Now all the teams that have not been evaluated have their LB and UB updated
		while(true){
			ArrayList<int[]> bestTeamsWithHighestUB = new ArrayList<int[]>();
			//Select the team with the highest UB per each component, evaluate it and set its LB and UB equal to its real value
			double[] allExactEvaluationOfThisIteration = new double[components.size()];
			for(int ii=0;ii<components.size();ii++){
				double highestUB = 0;
				int indexOfhighestUBTeam = -1;
				int[] highestUBTeam = new int[resourceCost.length];
				for(int jj=0;jj<components.get(ii).size();jj++){
					if(upperBounds.get(ii)[jj] > highestUB){
						highestUB = upperBounds.get(ii)[jj];
						indexOfhighestUBTeam = jj;
						highestUBTeam = components.get(ii).get(jj);
					}
				}
				bestTeamsWithHighestUB.add(highestUBTeam);
				double valueOfHighestUBTeam = EvaluateTeam(graph, budget, highestUBTeam, resourceCoverage, resourceProb, resourceCost,
						useProbabilities, maxTargetValue);
				upperBounds.get(ii)[indexOfhighestUBTeam] = valueOfHighestUBTeam;
				lowerBounds.get(ii)[indexOfhighestUBTeam] = valueOfHighestUBTeam;
				allExactEvaluationOfThisIteration[ii] = valueOfHighestUBTeam;

				//Update LB and UB of the teams in the components according to the new exact values
				for(int jj=0;jj<components.get(ii).size();jj++){
					int firstTeamDifferentResource = ComputeDifferentResourceOfFirstTeam(highestUBTeam,components.get(ii).get(jj));
					int secondTeamDifferentResource = ComputeDifferentResourceOfSecondTeam(highestUBTeam,components.get(ii).get(jj));
					if(firstTeamDifferentResource >= 0 && secondTeamDifferentResource >= 0){
						if(computedValues[firstTeamDifferentResource] > computedValues[secondTeamDifferentResource]){
							double possibleLowerBound = valueOfHighestUBTeam - computedValues[firstTeamDifferentResource];
							if(possibleLowerBound > lowerBounds.get(ii)[jj])
								lowerBounds.get(ii)[jj] = possibleLowerBound;
							double firstPossibleUpperBound = valueOfHighestUBTeam + computedValues[secondTeamDifferentResource];
							double secondPossibleUpperBound = 2*valueOfHighestUBTeam;
							if(firstPossibleUpperBound < secondPossibleUpperBound && firstPossibleUpperBound < upperBounds.get(ii)[jj])
								upperBounds.get(ii)[jj] = firstPossibleUpperBound;
							if(secondPossibleUpperBound < firstPossibleUpperBound && secondPossibleUpperBound < upperBounds.get(ii)[jj])
								upperBounds.get(ii)[jj] = secondPossibleUpperBound;
						}else{
							double firstPossibleLowerBound = valueOfHighestUBTeam - computedValues[firstTeamDifferentResource];
							double secondPossibleLowerBound = (0.5)*valueOfHighestUBTeam;
							if(firstPossibleLowerBound > secondPossibleLowerBound && firstPossibleLowerBound > lowerBounds.get(ii)[jj])
								lowerBounds.get(ii)[jj] = firstPossibleLowerBound;
							if(secondPossibleLowerBound > firstPossibleLowerBound && secondPossibleLowerBound > lowerBounds.get(ii)[jj])
								lowerBounds.get(ii)[jj] = secondPossibleLowerBound;
							double possibleUpperBound = valueOfHighestUBTeam + computedValues[secondTeamDifferentResource];
							if(possibleUpperBound < upperBounds.get(ii)[jj])
								upperBounds.get(ii)[jj] = possibleUpperBound;
						}
					}		
				}				
			}
			
			//Confront the value of the team evaluated exactly with the highest value w.r.t. UB of all the other teams
			double maxValueOfThisIteration = 0;
			int[] bestTeamOfThisIteration = new int[resourceCost.length];
			for(int ii=0;ii<components.size();ii++){
				for(int jj=0;jj<components.get(ii).size();jj++){
					if(Math.abs(upperBounds.get(ii)[jj]-lowerBounds.get(ii)[jj]) < 0.001){
						if(upperBounds.get(ii)[jj] > maxValueOfThisIteration){
							maxValueOfThisIteration = upperBounds.get(ii)[jj];
							bestTeamOfThisIteration = components.get(ii).get(jj);
						}
					}
				}
			}
			
			boolean bestAgain = true;
			for(int ii=0;ii<components.size();ii++){
				for(int jj=0;jj<components.get(ii).size();jj++){
					if(upperBounds.get(ii)[jj] > maxValueOfThisIteration){
						bestAgain = false;
					}
				}
			}
			
			if(bestAgain){
				double tockDiff = System.currentTimeMillis();
				String results = budget +", "+maxValueOfThisIteration+ ", "+(tockDiff-tickDiff)/1000 + ", " 
						+bestTeamOfThisIteration[0]+", "
						+bestTeamOfThisIteration[1]+", "
						+bestTeamOfThisIteration[2]+", "
						+bestTeamOfThisIteration[3]+", "
						+bestTeamOfThisIteration[4]+", "
						+bestTeamOfThisIteration[5];
				return results;
			}
		}
	}


	public static String Knapsack(AbstractBaseGraph graph, double budget, int t, boolean pad,
			int[] resourcecoverage, double[] resourceprob, int[] resourcecost){




		return "";
	}

	public static String HeuristicFast(AbstractBaseGraph graph, int budget, 
			int[] resourcecoverage, double[] resourceprob, int[] resourcecost){
		double tick_fast = System.currentTimeMillis();
		double tock_tot_fast = 0;
		int len = resourcecost.length;
		boolean useProbabilities = true;
		double[] values = new double[len];
		Arrays.fill(values, -99999.99);

		for(int ii=0;ii<len;ii++){	
			values[ii] = (resourcecoverage[ii]*resourceprob[ii])/resourcecost[ii];
		}

		int[] tot_cov = new int[len];
		double[] tot_prob = new double[len];
		int[] tot_cost = new int[len];
		double max = values[0];
		int max_index = 0;

		for(int ii=0;ii<len;ii++){
			for (int jj = 0; jj < len; jj++) {
				if (values[jj] > max) {
					max = values[jj];
					max_index = jj;
				}else if(values[jj] == max){
					if(resourcecoverage[jj] < resourcecoverage[max_index] 
							&& resourceprob[jj] > resourceprob[max_index]){
						max = values[jj];
						max_index = jj;
					}
				}
			}
			tot_cov[ii] = resourcecoverage[max_index];
			tot_prob[ii] = resourceprob[max_index];
			tot_cost[ii] = resourcecost[max_index];

			values[max_index] = -99999.99;
			max = -99999;
		}

		int decremented_budget = budget;	
		int ii = 0;

		int[] num_resources = new int[len];
		while(ii < len){
			if(decremented_budget-tot_cost[ii] >= 0){
				num_resources[ii] = num_resources[ii] + 1;
				decremented_budget = decremented_budget - tot_cost[ii];
			} else {
				ii++;
			}
		}

		teamNode.setTeams(tot_cost, tot_cov, tot_prob, useProbabilities);
		teamNode root = new teamNode("root", null, 0, budget, false);
		root.teamObj.setGraph(graph);
		root.setResources(num_resources);

		try {
			root.setFullBetterRuggedEvaluation();
			tock_tot_fast =  System.currentTimeMillis();
		} catch (MalformedGraphException e) {
			e.printStackTrace();
		} catch (LPSolverException e) {
			e.printStackTrace();
		}
		String results = budget +", "+root.getEvaluation() + ", "+(tock_tot_fast-tick_fast)/1000 + ", "
				+root.resourcesList.get(0)+", "+root.resourcesList.get(1)+", "+root.resourcesList.get(2)+", "
				+root.resourcesList.get(3)+", "+root.resourcesList.get(4)+", "+root.resourcesList.get(5)+", "
				+Integer.toString(tot_cost[0])+", "+Integer.toString(tot_cost[1])+", "+Integer.toString(tot_cost[2])+", "
				+Integer.toString(tot_cost[3])+", "+Integer.toString(tot_cost[4])+", "+Integer.toString(tot_cost[5])+", "
				+Integer.toString(tot_cov[0])+", "+Integer.toString(tot_cov[1])+", "+Integer.toString(tot_cov[2])+", "
				+Integer.toString(tot_cov[3])+", "+Integer.toString(tot_cov[4])+", "+Integer.toString(tot_cov[5])+", "
				+Double.toString(tot_prob[0])+", "+Double.toString(tot_prob[1])+", "+Double.toString(tot_prob[2])+", "
				+Double.toString(tot_prob[3])+", "+Double.toString(tot_prob[4])+", "+Double.toString(tot_prob[5]);

		return results;
	}

	public static String EvalTeamFast(AbstractBaseGraph graph, double budget, int t, boolean pad,
			int[] resourcecoverage, double[] resourceprob, int[] resourcecost, boolean div)
					throws MalformedGraphException, LPSolverException{
		double tick_evfast = System.currentTimeMillis();
		double tock_tot_evfast = 0;
		int len = resourcecost.length;
		boolean useProbabilities = true;
		double[] resource_values = new double[len];
		Arrays.fill(resource_values, -99999.99);

		for(int ii=0;ii<len;ii++){	
			if(div)
				resource_values[ii] = (resourcecoverage[ii]*resourceprob[ii])/resourcecost[ii];
			else
				resource_values[ii] = (resourcecoverage[ii]*resourceprob[ii]);
		}

		teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, useProbabilities);
		teamNode root = new teamNode("root", null, 0, budget, pad);
		root.teamObj.setGraph(graph);
		Search tree = new Search(root.teamObj, budget);
		tree.t=t;

		LinkedList<teamNode> teamList= tree.ListTeam(root);
		int n_teams = teamList.size();
		double[] team_values = new double[n_teams];
		for(int ii=0;ii<n_teams;ii++){
			int[] curr_team = teamList.get(ii).resources;
			for(int jj=0;jj<len;jj++){
				team_values[ii] = team_values[ii] + resource_values[jj]*curr_team[jj];
			}
		}

		double max_value = team_values[0];
		int max_index = 0;
		for(int ii=0;ii<n_teams;ii++){
			if (team_values[ii] > max_value) {
				max_value = team_values[ii];
				max_index = ii;
			}
		}
		int[] bestTeam = teamList.get(max_index).resources;

		teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, useProbabilities);
		teamNode root_sol = new teamNode("root", null, 0, budget, false);
		root_sol.teamObj.setGraph(graph);
		root_sol.setResources(bestTeam);

		try {
			root_sol.setFullBetterRuggedEvaluation();
			tock_tot_evfast =  System.currentTimeMillis();
		} catch (MalformedGraphException e) {
			e.printStackTrace();
		} catch (LPSolverException e) {
			e.printStackTrace();
		}

		String results = budget+", "+ root_sol.getEvaluation()+", "+(tock_tot_evfast-tick_evfast)/1000 +", " 
				+bestTeam[0]+", "+bestTeam[1]+", "+bestTeam[2]+", "
				+bestTeam[3]+", "+bestTeam[4]+", "+bestTeam[5];
		return results;
	}


	public static String HeuristicGuarantee(AbstractBaseGraph graph, int budget, 
			int[] resourcecoverage, double[] resourceprob, int[] resourcecost){
		//	public static String HeuristicGuarantee(AbstractBaseGraph graph, int budget, 
		//			int[] resourcecoverage, double[] resourceprob, int[] resourcecost, boolean div){
		double tick_guar = System.currentTimeMillis();
		double tock_tot_guar = 0;
		int len = resourcecost.length;
		int[] tot_cov = new int[len];
		double[] tot_prob = new double[len];
		int[] tot_cost = new int[len];
		boolean useProbabilities = true;
		double[] computed_values = new double[len];
		int[] resources = {1};
		Arrays.fill(computed_values, -99999.99);

		Set<INode> targets = getTargetSet(graph);
		int max_target_value = 0;
		for (INode node: targets)
			if(node.getPayoff() > max_target_value)
				max_target_value = node.getPayoff();

		for(int ii=0;ii<len;ii++){
			int[] curr_cov = {resourcecoverage[ii]};
			double[] curr_prob = {resourceprob[ii]};
			int[] curr_cost = {resourcecost[ii]};
			teamNode.setTeams(curr_cost,curr_cov,curr_prob,useProbabilities);
			teamNode root = new teamNode("root", null, 0, budget, false);
			root.teamObj.setGraph(graph);			
			root.setResources(resources);
			try {
				root.setFullBetterRuggedEvaluation();
				computed_values[ii] = root.getEvaluation()+max_target_value;
			} catch (MalformedGraphException e) {
				e.printStackTrace();
			} catch (LPSolverException e) {
				e.printStackTrace();
			}
		}

		double[] values = new double[len];
		//		if(div)		
		for(int ii=0;ii<computed_values.length;ii++)
			values[ii] = computed_values[ii]/resourcecost[ii];
		//		else
		//			for(int ii=0;ii<computed_values.length;ii++)
		//				values[ii] = computed_values[ii];

		double max = values[0];
		int max_index = 0;

		for(int ii=0;ii<len;ii++){
			for (int jj = 0; jj < len; jj++) {
				if (values[jj] > max) {
					max = values[jj];
					max_index = jj;
				}
			}
			tot_cov[ii] = resourcecoverage[max_index];
			tot_prob[ii] = resourceprob[max_index];
			tot_cost[ii] = resourcecost[max_index];

			values[max_index] = -99999.99;
			max = -99999;
		}

		len = tot_cost.length;
		int decremented_budget = budget;	
		int ii = 0;

		int[] num_resources = new int[len];
		while(ii < len){
			if(decremented_budget-tot_cost[ii] >= 0){
				num_resources[ii] = num_resources[ii] + 1;
				decremented_budget = decremented_budget - tot_cost[ii];
			} else {
				ii++;
			}
		}

		teamNode.setTeams(tot_cost, tot_cov, tot_prob, useProbabilities);
		teamNode root = new teamNode("root", null, 0, budget, false);
		root.teamObj.setGraph(graph);
		root.setResources(num_resources);

		try {
			root.setFullBetterRuggedEvaluation();
			tock_tot_guar = System.currentTimeMillis();		
		} catch (MalformedGraphException e) {
			e.printStackTrace();
		} catch (LPSolverException e) {
			e.printStackTrace();
		}

		double max_comp_value = computed_values[0];
		int max_comp_index = 0;
		for(int zz=0;zz<computed_values.length;zz++){
			if(computed_values[zz] >= max_comp_value){
				max_comp_value = computed_values[zz];
				max_comp_index = zz;
			}
		}

		String results = "";
		if(root.getEvaluation()+max_target_value >= max_comp_value)
			results = budget +", "+root.getEvaluation()+ ", "+(tock_tot_guar-tick_guar)/1000 + ", "
					+root.resourcesList.get(0)+", "+root.resourcesList.get(1)+", "+root.resourcesList.get(2)+", "
					+root.resourcesList.get(3)+", "+root.resourcesList.get(4)+", "+root.resourcesList.get(5)+", "
					+Integer.toString(tot_cost[0])+", "+Integer.toString(tot_cost[1])+", "+Integer.toString(tot_cost[2])+", "
					+Integer.toString(tot_cost[3])+", "+Integer.toString(tot_cost[4])+", "+Integer.toString(tot_cost[5])+", "
					+Integer.toString(tot_cov[0])+", "+Integer.toString(tot_cov[1])+", "+Integer.toString(tot_cov[2])+", "
					+Integer.toString(tot_cov[3])+", "+Integer.toString(tot_cov[4])+", "+Integer.toString(tot_cov[5])+", "
					+Double.toString(tot_prob[0])+", "+Double.toString(tot_prob[1])+", "+Double.toString(tot_prob[2])+", "
					+Double.toString(tot_prob[3])+", "+Double.toString(tot_prob[4])+", "+Double.toString(tot_prob[5]);
		else
			results = budget +", "+max_comp_value+", "+(tock_tot_guar-tick_guar)/1000 + ", "
					+root.resourcesList.get(max_comp_index)+", "+Integer.toString(resourcecost[max_comp_index])+", "
					+Integer.toString(resourcecoverage[max_comp_index])+", "+Double.toString(resourceprob[max_comp_index]);
		return results;
	}

	public static String EvalTeamGuarantee(AbstractBaseGraph graph, double budget, int t, boolean pad,
			int[] resourcecoverage, double[] resourceprob, int[] resourcecost, boolean div) 
					throws MalformedGraphException, LPSolverException{
		double tick_evguar = System.currentTimeMillis();
		double tock_tot_evguar = 0;
		int len = resourcecost.length;
		int[] resources = {1};
		boolean useProbabilities = true;
		double[] computed_values = new double[len];
		Arrays.fill(computed_values, -99999.99);

		Set<INode> targets = getTargetSet(graph);
		int max_target_value = 0;
		for (INode node: targets)
			if(node.getPayoff() > max_target_value)
				max_target_value = node.getPayoff();

		for(int ii=0;ii<len;ii++){
			int[] curr_cov = {resourcecoverage[ii]};
			double[] curr_prob = {resourceprob[ii]};
			int[] curr_cost = {resourcecost[ii]};
			teamNode.setTeams(curr_cost,curr_cov,curr_prob,useProbabilities);
			teamNode root = new teamNode("root", null, 0, budget, false);
			root.teamObj.setGraph(graph);			
			root.setResources(resources);
			try {
				root.setFullBetterRuggedEvaluation();
				computed_values[ii] = root.getEvaluation()+max_target_value;
			} catch (MalformedGraphException e) {
				e.printStackTrace();
			} catch (LPSolverException e) {
				e.printStackTrace();
			}
		}

		double[] resource_values = new double[len];
		if(div)		
			for(int ii=0;ii<computed_values.length;ii++)
				resource_values[ii] = computed_values[ii]/resourcecost[ii];
		else
			for(int ii=0;ii<computed_values.length;ii++)
				resource_values[ii] = computed_values[ii];

		teamNode.setTeams(resourcecost,resourcecoverage,resourceprob,true);
		teamNode root = new teamNode("root", null, 0, budget, pad);
		root.teamObj.setGraph(graph);
		Search tree = new Search(root.teamObj, budget);
		tree.t=t;
		LinkedList<teamNode> teamList= tree.ListTeam(root);

		int n_teams = teamList.size();
		double[] team_values = new double[n_teams];
		for(int ii=0;ii<n_teams;ii++){
			int[] curr_team = teamList.get(ii).resources;
			for(int jj=0;jj<len;jj++){
				team_values[ii] = team_values[ii] + resource_values[jj]*curr_team[jj];
			}
		}

		double max_value = team_values[0];
		int max_index = 0;
		for(int ii=0;ii<n_teams;ii++){
			if (team_values[ii] > max_value) {
				max_value = team_values[ii];
				max_index = ii;
			}
		}

		// FOR COMPUTATIONAL ERORRS APPROXIMATIONS
		//		int[] temp_max_indices = new int[n_teams];
		//		Arrays.fill(temp_max_indices, -1);
		//		int jj=0;
		//		for(int ii=0;ii<n_teams;ii++){
		//			if(Math.abs(team_values[ii]-max_value) <= 1){
		//				temp_max_indices[jj] = ii;
		//				jj++;
		//			}
		//		}
		//		
		//		int total_teams = jj--;
		//		int[] max_indices = new int[total_teams];
		//		for(int ii=0;ii<n_teams;ii++){
		//			if(temp_max_indices[ii] > 0){
		//				max_indices[ii] = temp_max_indices[ii];
		//			}
		//		}
		//		
		//		int[] currentTeam = teamList.get(max_indices[0]).resources;
		//		int[] bestTeam = teamList.get(max_indices[0]).resources;
		//		int best_index = max_indices[0];
		//		double bestEval = -99999;
		//		for(int ii=0;ii<max_indices.length;ii++){
		//			currentTeam = teamList.get(max_indices[ii]).resources;
		//			teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, useProbabilities);
		//			teamNode root_sol = new teamNode("root", null, 0, budget, false);
		//			root_sol.teamObj.setGraph(graph);
		//			root_sol.setResources(currentTeam);
		//
		//			try {
		//				root_sol.setFullBetterRuggedEvaluation();
		//				tock_tot_evguar =  System.currentTimeMillis();
		//			} catch (MalformedGraphException e) {
		//				e.printStackTrace();
		//			} catch (LPSolverException e) {
		//				e.printStackTrace();
		//			}
		//
		//			if(root_sol.getEvaluation() > bestEval)
		//				//best_index = max_indices[ii];
		//				bestEval = root_sol.getEvaluation();
		//			bestTeam = currentTeam;
		//		}

		int[] bestTeam = teamList.get(max_index).resources;
		double bestEval = -99999.99;
		teamNode.setTeams(resourcecost, resourcecoverage, resourceprob, useProbabilities);
		teamNode root_sol = new teamNode("root", null, 0, budget, false);
		root_sol.teamObj.setGraph(graph);
		root_sol.setResources(bestTeam);

		try {
			root_sol.setFullBetterRuggedEvaluation();
			tock_tot_evguar =  System.currentTimeMillis();
			bestEval = root_sol.getEvaluation();
		} catch (MalformedGraphException e) {
			e.printStackTrace();
		} catch (LPSolverException e) {
			e.printStackTrace();
		}

		String results = budget+", "+bestEval+ ", "+(tock_tot_evguar-tick_evguar)/1000 + ", "
				+bestTeam[0]+", "+bestTeam[1]+", "+bestTeam[2]+", "
				+bestTeam[3]+", "+bestTeam[4]+", "+bestTeam[5];	

		return results;
	}


	public static Set<INode> getTargetSet(
			AbstractBaseGraph<INode, ExtDWE> graph) {
		Set<INode> targets = new HashSet<INode>();
		for (INode u : graph.vertexSet()) {
			if (u.getType() == NODE_TYPE.TARGET) {
				targets.add(u);
			}
		}
		return targets;
	}

	public static void EvaluateAndPrint(AbstractBaseGraph graph, double budget, int t, boolean pad,
			int[] resourcecoverage, double[] resourceprob, int[] resourcecost, boolean div) 
					throws MalformedGraphException, LPSolverException{
		int len = resourcecost.length;
		int[] resources = {1};
		boolean useProbabilities = true;
		double[] computed_values = new double[len];
		Arrays.fill(computed_values, -99999.99);

		Set<INode> targets = getTargetSet(graph);
		int max_target_value = 0;
		for (INode node: targets)
			if(node.getPayoff() > max_target_value)
				max_target_value = node.getPayoff();

		for(int ii=0;ii<len;ii++){
			int[] curr_cov = {resourcecoverage[ii]};
			double[] curr_prob = {resourceprob[ii]};
			int[] curr_cost = {resourcecost[ii]};
			teamNode.setTeams(curr_cost,curr_cov,curr_prob,useProbabilities);
			teamNode root = new teamNode("root", null, 0, budget, false);
			root.teamObj.setGraph(graph);			
			root.setResources(resources);
			try {
				root.setFullBetterRuggedEvaluation();
				computed_values[ii] = root.getEvaluation()+max_target_value;
				System.out.println("Resource "+ii+", Value "+computed_values[ii]);
			} catch (MalformedGraphException e) {
				e.printStackTrace();
			} catch (LPSolverException e) {
				e.printStackTrace();
			}
		}

		teamNode.setTeams(resourcecost,resourcecoverage,resourceprob,true);
		teamNode root = new teamNode("root", null, 0, budget, pad);
		root.teamObj.setGraph(graph);
		Search tree = new Search(root.teamObj, budget);
		tree.t=t;
		LinkedList<teamNode> teamList= tree.ListTeam(root);

		double[] team_computed_values = new double[teamList.size()];
		for(int ii=0;ii<teamList.size();ii++){
			teamNode.setTeams(resourcecost,resourcecoverage,resourceprob,useProbabilities);
			teamNode root_team = new teamNode("root", null, 0, budget, false);
			root_team.teamObj.setGraph(graph);
			root_team.setResources(teamList.get(ii).resources);
			try {
				root_team.setFullBetterRuggedEvaluation();
				team_computed_values[ii] = root_team.getEvaluation()+max_target_value;
				System.out.println("Team "+Arrays.toString(teamList.get(ii).resources)+", Value "+team_computed_values[ii]);
			} catch (MalformedGraphException e) {
				e.printStackTrace();
			} catch (LPSolverException e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) throws MalformedGraphException, LPSolverException ,FileNotFoundException{
		//Configuration.initialize(108374);
		//Set budget, graph and available resources
		int budget = 40;
		int [] resourceCoverage = {2,2,5,3,3,6};
		double [] resourceProb = {0.7,0.9,0.7,0.6,0.6,0.6};
		int [] resourceCost = {5,8,10,5,8,10};

		teamNode.setTeams(resourceCost,resourceCoverage,resourceProb,true);
		for(;budget<41;budget+=10){
			for(int ii=0;ii<9;ii++){
				AbstractBaseGraph graph = RandomGraph(20, 4, 4, 0.2);
				teamNode.setTeams(resourceCost,resourceCoverage,resourceProb,true);
				String resultsDiffusion = Diffusion(graph, budget, 1, false, 
						resourceCoverage, resourceProb, resourceCost);	
				try {
					PrintWriter outDiffusion = new PrintWriter(new BufferedWriter(new FileWriter(
							"results_with_prob_random_diffusion_budget_"+budget+".csv", true)));
					outDiffusion.println(resultsDiffusion);
					outDiffusion.close();
				} catch (IOException e) {
				}

				teamNode.setTeams(resourceCost,resourceCoverage,resourceProb,true);
				String resultsLayers = PQSearch(graph, budget, 1, false);	
				try {
					PrintWriter outLayers = new PrintWriter(new BufferedWriter(new FileWriter(
							"results_with_prob_random_layers_budget_"+budget+".csv", true)));
					outLayers.println(resultsLayers);
					outLayers.close();
				} catch (IOException e) {
				}
			}
		}
	}
}