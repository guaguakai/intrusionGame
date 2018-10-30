package utilities;

import java.util.ArrayList;
import java.util.List;

public class Singleton extends Element {
	public int gameId;	
	
	public Singleton(int gameId) {
		super();
		this.type = TYPE.SINGLETON;
		this.gameId = gameId;
	}
	
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public int sum() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ("" + this.gameId);
	}

	@Override
	public List<Integer> getListGameIds() {
		// TODO Auto-generated method stub
		List<Integer> lstGameIds = new ArrayList<Integer>();
		lstGameIds.add(this.gameId);
		return lstGameIds;
	}

	@Override
	public List<Element> getElemList() {
		// TODO Auto-generated method stub
		List<Element> lstElem = new ArrayList<Element>();
		lstElem.add(this);
		return lstElem;
	}

}
