package utilities;

import java.util.List;

public abstract class Element {
	
	public enum TYPE {
		LIST, SINGLETON
	};
	
	public TYPE type;
	
	public abstract int size();
	public abstract int sum();
	public abstract String toString();
	public abstract List<Element> getElemList();
	public abstract List<Integer> getListGameIds();
}
