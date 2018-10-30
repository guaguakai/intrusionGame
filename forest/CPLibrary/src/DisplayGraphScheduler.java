import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jgrapht.graph.AbstractBaseGraph;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

import model.graphutils.ExtDWE;
import model.graphutils.INode;
import model.graphutils.INode.NODE_TYPE;
import model.teamBuildingModels.DefenderAllocation;
import teamBuildingSolvers.RuggedBetterResponsesCutoff;

public class DisplayGraphScheduler implements ActionListener{
	//private List <DisplayGraph> dg_list = new ArrayList();
	// JPanel
    JPanel pnlButton = new JPanel();
    // Buttons
    JButton btnNextGraph = new JButton("Next Graph");
    private JFrame frame = new JFrame();
    private int day;
    private int num_days;
	private DisplayGraph d;
	private List <DefenderAllocation> da_list = new ArrayList();
	private AbstractBaseGraph gr;
	private RuggedBetterResponsesCutoff sol;
	private int ds;
	
	//dynamically change graph
	//allow user to add edges
	
	//clean up graph, all in one window
	//get rid of numbers next to edges
	//not open a new graph every time
	//user can enter number of days to schedule
	
	public DisplayGraphScheduler(AbstractBaseGraph g, RuggedBetterResponsesCutoff solution, int days) {
		//for (int i = 0; i < days; i++) {
			sol = solution;
			ds = days;
			gr = g;
			d = new DisplayGraph(g);
			num_days = days;
			// NextGraph setbounds
	        btnNextGraph.setBounds(60, 400, 220, 30);
	        // JPanel bounds
	        pnlButton.setBounds(800, 800, 200, 100);
	        // Adding to JFrame
	        pnlButton.add(btnNextGraph);
	        this.frame.add(pnlButton);
	        btnNextGraph.addActionListener(this);
			//dg_list.add(dg_list.size(), d);
		//}
		//DisplayGraph sample = new DisplayGraph(g);
		da_list = d.schedule(solution, days);
		day = 0;
		d.highlight(da_list.get(day), day + 1, true);
		d.run();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(20, 70);
		frame.setVisible(true);
		//for (int i = 0; i < days; i++) {
			//dg_list.get(i).highlight(da_list.get(i));
			//dg_list.get(i).run();
		//}
	}
	public void actionPerformed (ActionEvent e) {
		day ++;
		if (day >= num_days) {
			day = 1;
		}
		System.out.println("now showing day " + day);
		//d.update_highlight(da_list.get(day), day + 1);
		//d.dispose();
		//d = new DisplayGraph(gr);
		
		//da_list = d.schedule(sol, ds);
		d.clear_highlight(da_list.get(day), day);
		//d.run();
	}
}
