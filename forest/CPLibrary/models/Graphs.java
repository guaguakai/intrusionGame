import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;
import model.graphutils.Node;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;


public class Graphs {
	
	public static AbstractBaseGraph<INode, ExtDWE> GridGraph(int width, int height, int nrSources,int nrTargets, int payoff) {
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);

		List<ArrayList<INode>> rows = new ArrayList<ArrayList<INode>>(height);
		
		for(int j=0;j<height;j++){
			
		
		ArrayList<INode> nodeList = new ArrayList<INode>();

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
					ExtDWE e = graph.addEdge(rows.get(j-1).get(i), nodeList.get(i));
					ExtDWE e1 = graph.addEdge(nodeList.get(i), rows.get(j-1).get(i));
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
	
	public static AbstractBaseGraph<INode, ExtDWE> RandomGraph(int numNodes, int nrOfSources, int nrOfTargets, double r){
		Configuration.initialize(108374);
		Random random = new Random();
		return GraphGenerator.getRandomGeometricGraph(numNodes, nrOfSources, nrOfTargets, r,
			50, random);
		
		//System.out.println("Number of Edges: " + team.getGraph().edgeSet().size());
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
	
	

}
