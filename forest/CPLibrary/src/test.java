
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lpWrapper.LPSolverException;
import model.Configuration;
import model.graphutils.ExtDWE;
import model.graphutils.GraphGenerator;
import model.graphutils.INode;
import model.graphutils.MalformedGraphException;
import model.graphutils.Node;
import model.graphutils.IEdge.EDGE_TYPE;
import model.graphutils.INode.NODE_TYPE;
import model.teamBuildingModels.TeamBuildingProblem;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;

import PQsearch.CompareParallel;
import PQsearch.TeamsPermuteCheck;
import search.BruteForce;
import search.Search;
import search.teamNode;
import teamBuildingSolvers.CompactProblem;
import teamBuildingSolvers.Rugged;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;
import teamBuildingSolvers.RuggedCutoff;


public class test {

	private static AbstractBaseGraph<INode, ExtDWE> mumbaiGraph() {
		return GraphGenerator.getMumbaiGraph();
	
	}
	public static AbstractBaseGraph<INode, ExtDWE> getMadagascarGraph() {
		// TODO Auto-generated method stub
		AbstractBaseGraph<INode, ExtDWE> graph = new DirectedWeightedMultigraph<INode, ExtDWE>(ExtDWE.class);

		//AbstractBaseGraph<INode, ExtDWE> graph = new WeightedMultigraph<INode, ExtDWE>(ExtDWE.class);
		
		List<INode> nodeList = new ArrayList<INode>();
		for (int x = 0; x < 383; x++) {
			INode v = new Node();
			v.setType(NODE_TYPE.INTERMEDIATE);
			nodeList.add(v);
			graph.addVertex(v);
		}

		int[] targetIndexes = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33};//,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90};
		
		int[] sourceIndexes = {91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114};//,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256};
		INode virtual = new Node();
		virtual.setType(NODE_TYPE.SOURCE);
		graph.addVertex(virtual);
		for (int i : sourceIndexes) {
			ExtDWE e = graph.addEdge(virtual, nodeList.get(i - 1));
			e.setType(EDGE_TYPE.VIRTUAL);
		}

		for (int i : targetIndexes) {
			nodeList.get(i - 1).setType(NODE_TYPE.TARGET);
			nodeList.get(i - 1).setPayoff(100);
		}
		//nodeList.get(25).setPayoff(100);
		//nodeList.get(104).setPayoff(200);

		int[][] edgeSet = {{1,2},{2,263},{263,217},{217,100},{100,273},{100,275},{275,274},{274,92},{92,229},{229,3},{92,272},{272,91},{91,271},{271,247},{91,279},{279,277},{279,241},{279,218},{218,5},{5,264},{218,280},{280,6},{280,89},{89,270},{270,269},{269,82},{82,265},{265,4},{82,266},{266,257},{266,216},{216,258},{258,262},{262,259},{259,260},{259,239},{216,215},{215,261},{215,267},{215,240},{280,278},{278,281},{278,282},{282,108},{108,283},{108,285},{285,284},{285,96},{96,95},{96,94},{94,107},{107,276},{107,91},{94,95},{94,93},{95,97},{93,92},{93,97},{97,290},{290,251},{97,98},{97,274},{98,286},{286,289},{289,293},{289,83},{98,99},{99,274},{99,219},{219,275},{219,288},{99,101},{101,287},{287,102},{102,103},{103,294},{294,115},{115,114},{114,17},{115,242},{115,116},{116,17},{103,105},{105,252},{252,17},{105,104},{104,296},{104,106},{106,195},{106,300},{300,17},{104,112},{112,111},{111,253},{253,113},{112,109},{109,298},{109,110},{110,299},{299,297},{299,111},{110,70},{70,14},{14,113},{113,17},{14,306},{306,15},{306,121},{121,122},{122,134},{134,19},{122,123},{123,124},{124,18},{124,16},{17,16},{16,125},{125,305},{305,84},{125,126},{126,116},{126,220},{220,216},{220,120},{126,127},{220,127},{127,303},{127,304},{304,130},{120,119},{119,302},{119,301},{301,225},{119,118},{120,118},{118,117},{117,228},{228,8},{117,129},{117,128},{129,128},{282,90},{90,128},{128,292},{292,7},{7,234},{234,235},{235,236},{7,291},{291,238},{238,237},{291,227},{227,9},{9,383},{9,268},{134,254},{254,135},{135,136},{136,310},{136,312},{135,311},{311,312},{312,313},{313,21},{21,20},{135,255},{255,137},{137,138},{138,139},{139,24},{139,140},{140,22},{22,314},{140,141},{139,23},{141,23},{23,73},{73,32},{23,143},{143,142},{142,309},{309,73},{309,256},{256,130},{143,25},{143,26},{142,146},{143,144},{144,27},{144,146},{145,146},{145,132},{146,132},{146,315},{315,13},{13,226},{226,130},{13,133},{132,133},{133,308},{308,131},{131,307},{307,316},{131,225},{131,12},{12,71},{12,11},{11,10},{137,321},{321,322},{322,338},{338,34},{338,147},{34,336},{336,69},{335,69},{335,323},{323,322},{147,36},{36,339},{339,151},{151,336},{69,26},{147,35},{35,148},{148,149},{148,81},{81,334},{81,150},{150,149},{81,159},{159,151},{159,158},{158,152},{152,340},{340,26},{152,153},{153,156},{156,157},{157,154},{154,153},{154,337},{337,28},{154,155},{155,29},{158,230},{158,160},{155,85},{155,31},{31,30},{30,33},{33,204},{157,341},{341,224},{224,324},{324,320},{320,165},{165,39},{165,204},{165,317},{317,204},{156,160},{160,202},{202,221},{221,163},{163,37},{37,342},{342,163},{221,66},{66,38},{38,203},{203,47},{203,325},{325,319},{319,46},{319,68},{68,317},{68,318},{318,45},{318,207},{207,206},{206,41},{206,40},{40,317},{207,205},{205,42},{205,208},{208,43},{208,223},{223,72},{205,44},{44,209},{209,222},{222,67},{209,210},{210,243},{210,211},{211,214},{211,212},{214,212},{212,213},{210,233},{213,233},{80,246},{149,76},{76,333},{76,332},{332,344},{344,77},{77,162},{162,331},{162,343},{343,230},{343,164},{164,330},{330,161},{161,160},{343,358},{161,201},{201,168},{201,346},{346,355},{201,168},{168,169},{169,74},{74,167},{168,167},{168,54},{54,167},{167,328},{167,166},{166,328},{328,329},{329,249},{54,249},{166,55},{328,87},{87,88},{249,52},{52,88},{249,86},{55,173},{173,53},{53,326},{326,88},{88,48},{88,327},{327,250},{250,49},{250,51},{51,50},{346,347},{346,355},{347,170},{344,357},{357,187},{187,369},{187,368},{368,367},{367,361},{187,78},{77,345},{345,356},{356,363},{363,366},{78,186},{186,362},{362,361},{361,366},{186,185},{78,370},{370,185},{185,371},{371,372},{372,379},{379,381},{370,184},{184,380},{184,183},{183,79},{183,188},{188,381},{188,189},{189,382},{382,190},{366,181},{366,182},{181,182},{182,360},{360,381},{182,365},{365,180},{180,201},{180,179},{179,364},{179,170},{170,171},{360,178},{178,359},{178,248},{248,171},{171,172},{172,64},{172,174},{172,231},{231,173},{172,174},{174,232},{232,177},{177,173},{177,62},{62,354},{354,352},{352,353},{353,63},{63,196},{196,197},{197,58},{197,351},{196,350},{350,349},{349,348},{349,194},{194,195},{195,57},{57,374},{195,193},{191,193},{194,192},{192,191},{192,56},{56,245},{354,198},{198,176},{176,376},{176,377},{377,61},{61,375},{198,60},{60,199},{198,59},{59,175},{175,373},{175,200},{200,75},{60,199},{199,75},{75,244},{244,190},{244,65},{65,174}};
		for (int[] edge : edgeSet) {
			graph.addEdge(nodeList.get(edge[0] - 1), nodeList.get(edge[1] - 1));
			graph.addEdge(nodeList.get(edge[1] - 1), nodeList.get(edge[0] - 1));
		}

		System.out.println("No. of edges: " + graph.edgeSet().size());
		System.out.println("No. of nodes: " + graph.vertexSet().size());

		return graph;
	}
	private static AbstractBaseGraph<INode, ExtDWE> GridGraph(int width, int height, int nrSources,int nrTargets, int payoff) {
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
			
			Node n5 = new Node(NODE_TYPE.INTERMEDIATE, 0);
			Node n6 = new Node(NODE_TYPE.INTERMEDIATE, 0);
			Node n7 = new Node(NODE_TYPE.INTERMEDIATE, 0);
			Node n8 = new Node(NODE_TYPE.TARGET, 5);
			
			graph.addVertex(n0);
			graph.addVertex(n1);
			graph.addVertex(n2);
			graph.addVertex(n3);
			graph.addVertex(n4);
			
			graph.addVertex(n5);
			graph.addVertex(n6);
			graph.addVertex(n7);
			graph.addVertex(n8);
			
			// Generate edges
			List<ExtDWE> edgeList = new ArrayList<ExtDWE>();
			ExtDWE e1 = graph.addEdge(n0, n1);
			e1.setType(EDGE_TYPE.VIRTUAL);
			ExtDWE e2 = graph.addEdge(n1, n2);
			ExtDWE e3 = graph.addEdge(n2, n3);
			ExtDWE e4 = graph.addEdge(n3, n4);
			
			ExtDWE e5 = graph.addEdge(n0, n5);
			e5.setType(EDGE_TYPE.VIRTUAL);
			ExtDWE e6 = graph.addEdge(n5, n6);
			ExtDWE e7 = graph.addEdge(n6, n7);
			ExtDWE e8 = graph.addEdge(n7, n8);
			
			
			edgeList.add(e1);
			edgeList.add(e2);
			edgeList.add(e3);
			edgeList.add(e4);
			edgeList.add(e5);
			edgeList.add(e6);
			edgeList.add(e7);
			edgeList.add(e8);
			
			
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
	
	public static void runRugged(AbstractBaseGraph graph, String[] args) throws LPSolverException,
	MalformedGraphException {

		boolean print = true;
		
		List<Double> resources = new ArrayList<Double>();
		resources.add(1.0);
		//resources.add(1.0);
	
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(3.0);
		//coverage.add(3.0);
		
		List<Double> Prob = new ArrayList<Double>();
		//Prob.add(0.9);
		Prob.add(0.5);
		
		TeamBuildingProblem uspObj3 = new TeamBuildingProblem(resources, coverage, Prob);

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
	
			

	}
	public static TeamBuildingProblem testBetterRespt(AbstractBaseGraph graph, String[] args, int cutoff, List<Double> res){
		
		List<Double> resources = new ArrayList<Double>();
		resources.add(8.0);
		resources.add(2.0);
		resources.add(6.0);
	
		List<Double> coverage = new ArrayList<Double>();
		coverage.add(4.0);
		coverage.add(3.0);
		coverage.add(2.0);
		
		List<Double> Prob = new ArrayList<Double>();
		Prob.add(0.5);
		Prob.add(0.5);
		
		TeamBuildingProblem uspObj = new TeamBuildingProblem(res, coverage);

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
		
		
		int targetVal = 100; // Random integer between 1 and this number

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

		System.out.println("Adversary Strategy: "
				+ ruggedObj.getAdversaryStrategy());
		System.out
				.println("Adversary Paths: " + ruggedObj.getAdversaryPaths());
		
		//ruggedObj.writeMeasure();
		//System.out.flush();
		//System.err.flush();
		//System.out.print(ruggedObj.getcutoffUsed());
		//ruggedObj.end();
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

	public static List<Double> testQuickSolve(AbstractBaseGraph graph) throws LPSolverException{
		
		List<Double> costs = new ArrayList<Double>();
		costs.add(5.0);
		costs.add(4.0);
		costs.add(3.0);
		
		List<Double> resources = new ArrayList<Double>();
		resources.add(4.0);
		//resources.add(1.0);

		List<Double> coverage = new ArrayList<Double>();
		coverage.add(2.0);
		//coverage.add(3.0);
		//coverage.add(2.0);
		
		List<Double> prob = new ArrayList<Double>();
		prob.add(0.5);
		prob.add(0.5);
		
		TeamBuildingProblem team = new TeamBuildingProblem(resources, coverage);
		//TeamBuildingProblem team = new TeamBuildingProblem(40, costs, coverage);
		team.setGraph(graph);
		
	
		CompactProblem test = new CompactProblem(team);
		//test.getCompactGraph();
		//System.out.println(test.solveQuick()+"\n");
		//System.out.println(test.runtime+"\n");
		System.out.println("GV: "+test.solve(1)+"\n");
		System.out.println("RT: "+test.runtime+"\n");
		
		for(int i=0;i<test.attackerPaths.size();i++){
			System.out.println(test.attackerPaths.get(i)+": "+test.strategy.getAttackerStrategy().get(i));
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
		
		List<Double> r = test.CompactDef_E.getAllocation();
		
		if(team.buildteam){
			System.out.println(r);
		}
//		for(int i=0;i<test.strategy.getDefenderStrategy().size();i++){
//			double p = test.strategy.getDefenderStrategy().get(i);
//			if(p>0){
//				System.out.println(p+": "+test.strategy.defenderAllocation.get(i).defenderCoverage);
//			}
//		}
		
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
	public static void PQSearch(AbstractBaseGraph graph, double budget, int t) throws MalformedGraphException, LPSolverException{
		 
		 teamNode root = new teamNode("root", null, 0, budget, false);
		 root.teamObj.setGraph(graph);
		 
		 
		 
		 Search tree = new Search(root.teamObj, budget);
		 tree.t=t;
		 double tick = System.currentTimeMillis();
		 teamNode result = tree.PQSearch(root);
		 double tock =  System.currentTimeMillis();
		 
		 
		 String results = 	"Budget: " + budget +", verticies: "
				 			+graph.vertexSet().size() + ", edges: "
				 			+result.teamObj.getGraph().edgeSet().size() + ", GV: "
				 			+result.getEvaluation() + ", runtime(S): " 
				 			+(tock-tick)/1000 + ", resources: " 
				 			+result.resourcesList;
		 	
		 System.out.println("\n"+results);
		 
	 }
	
	public static void main(String[] args) throws MalformedGraphException, LPSolverException ,FileNotFoundException{
		// TODO Auto-generated method stub


		
		

		System.load("/home/kai/Dropbox/USC/publication/intrusion/forest/CPLibrary/cplex/bin/x86-64_linux/libcplex1280.so");
		// System.load("/home/kai/Dropbox/USC/publication/intrusion/forest/CPLibrary/cpoptimizer/bin/x86-64_linux/libcp_wrap_cpp_java1280.so");
		Configuration.initialize(108374);
		
		
		//AbstractBaseGraph graph = RandomGraph( 60, 8, 5, 0.13);
		
		
		
		
		
		
		//AbstractBaseGraph graph = genLargeTestGraph();
		//AbstractBaseGraph graph = genTestGraph();
		//AbstractBaseGraph graph = getMadagascarGraph();
		//AbstractBaseGraph graph = mumbaiGraph();
		int i = 10;
		AbstractBaseGraph graph = GridGraph(5,20,5,5,50);
		//System.out.println(graph.edgeSet().size());
		//System.out.println(graph.vertexSet().size());
		
		
//		GraphVisual g = new GraphVisual(graph);
//		g.run();
		
		//testUrbanSec(graph,5,args);
		//testUrbanSec(graph,2,args);
		//runRugged(graph, args);
		//testBetterRespt(graph,args, 0);
		
		//System.out.println("---------------------");
		//testBetterResp(team,args);
		//System.out.println("---------------------");
		
		//testQuickSolve(graph);
		//testBetterRespt(graph,args, 0, testQuickSolve(graph));
		//testBetterRespPROBS(graph,args);
		//testBetterRespPROBS2(graph,args);
		
		if(true){
		int budget  = 22;
		PQSearch(graph, budget, 0);
		System.out.println("-----------");
		PQSearch(graph, budget, 1);
		System.out.println("-----------");
		BruteForce(graph, budget);
		}
		//PQSearch(graph, budget, 2);
//		oldPQSearch(graph,budget,(int)budget-5,0.0,3);
		
	}


}
