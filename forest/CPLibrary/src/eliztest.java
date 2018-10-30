import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.AbstractBaseGraph;

import teamBuildingSolvers.DefenderOracle.TestLP;
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

public class eliztest {

	
	public static void main(String[] args ){
		
		List<Double> resources = new ArrayList<Double>();
		//resources.add(2.0);
		resources.add(1.0);

		List<Double> coverage = new ArrayList<Double>();
		//coverage.add(1.0);
		coverage.add(1.0);

		AbstractBaseGraph graph = SaraCompareSolvers.GridGraph(4,4,4,4,50);;
		
		TeamBuildingProblem team = new TeamBuildingProblem(resources, coverage);
		team.setGraph(graph);
		
		double tick = System.currentTimeMillis();
		TestLP test = new TestLP(team, 1000000);
		test.loadProblem();
		//test.writeProb("testprob.lp");
		double tock = System.currentTimeMillis();
		
		System.out.println(tock-tick);

	}
}
