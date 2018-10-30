package model.graphutils;


/**
 *
 * @author inducer
 */
public class MyLink extends ExtDWE {

	private static final long serialVersionUID = -9006345511664819264L;
	private static int EDGE_COUNT = 1;
	
	double capacity; // should be private
    double weight; // should be private for good practice
   // int id;
    public MyNode source;
    public MyNode target;
    int targetIndex;
    /**
     * When solved, set the probability of edge coverage.
     */
    private double coverageProb;

    public MyLink(int id, double weight, double capacity, MyNode leftV, MyNode rightV) {
    	setId(id);
        this.weight = weight;
        this.capacity = capacity;
        this.source = leftV;
        this.target = rightV;
        //System.out.println("Current edge no: " + id);
        EDGE_COUNT ++;
    }

    public MyLink(double weight, double capacity, MyNode leftV, MyNode rightV) {
    	int id = EDGE_COUNT++;
		setId(id);
        this.weight = weight;
        this.capacity = capacity;
        this.source = leftV;
        this.target = rightV;
        //System.out.println("Current edge no: " + id);
    }

    public String toString() { // Always good for debugging
        return "E("+getId()+")[" + source.id+", "+target.id+"]";
    }

	/**
	 * @return the coverageProb
	 */
	public double getCoverageProb() {
		return coverageProb;
	}

	/**
	 * @param coverageProb the coverageProb to set
	 */
	public void setCoverageProb(double coverageProb) {
		this.coverageProb = coverageProb;
	}
  
    
}
