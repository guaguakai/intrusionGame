package model.graphutils;

public interface IEdge {
	public enum EDGE_TYPE {NORMAL, VIRTUAL, DUAL, WATER};
	
	public int getId();
	
	public EDGE_TYPE getType();

}
