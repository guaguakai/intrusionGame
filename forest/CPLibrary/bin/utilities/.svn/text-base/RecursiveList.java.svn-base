package utilities;

import java.util.ArrayList;
import java.util.List;

public class RecursiveList extends Element {
	List<Element> elemList;

	public RecursiveList(Integer i) {
		super();
		this.elemList = new ArrayList<Element>();
		elemList.add(new Singleton(i));
	}
	
	public RecursiveList(Element elem) {
		super();
		this.elemList = new ArrayList<Element>();
		if (elem.type == TYPE.LIST) {
			for (Element e : elem.getElemList()) {
				this.elemList.add(e);
			}
		} else if (elem.type == TYPE.SINGLETON) {
			elemList.add(elem);
		} else {
			throw new RuntimeException("Unknown Type for Element: " + elem);
		}
	}

	public RecursiveList(List<Element> elemList) {
		super();
		this.type = TYPE.LIST;
		this.elemList = elemList;
	}

	public RecursiveList() {
		super();
		this.type = TYPE.LIST;
		this.elemList = new ArrayList<Element>();
	}

	public List<Element> getElemList() {
		return elemList;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return elemList.size();
	}

	public void add(Element elem) {
		this.elemList.add(elem);
	}

	public void append(ArrayList<Integer> listElemIds) {
		for ( Integer gameId : listElemIds ) {
			this.elemList.add(new Singleton(gameId));
		}
	}
	
	public void append(int[] listElemIds) {
		for ( Integer gameId : listElemIds ) {
			this.elemList.add(new Singleton(gameId));
		}
	}
	
	@Override
	public int sum() {
		// TODO Auto-generated method stub
		int sum = 0;
		for (Element e : elemList)
			sum += e.sum();
		return sum;
	}

	@Override
	public String toString() {
		return "RecursiveList [elemList=" + elemList + "]";
	}

	@Override
	public List<Integer> getListGameIds() {
		// TODO Auto-generated method stub
		List<Integer> lstGameIds = new ArrayList<Integer>();
		for (Element e : this.elemList) {
			lstGameIds.addAll(e.getListGameIds());
		}
		return lstGameIds;
	}
}
