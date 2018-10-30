package osmutils;

import java.util.LinkedList;
import java.util.List;


/**
 * Very naive implementation of disjoint set.
 * It allows adding new sets dynamically by union.
 * //TODO RFCT neni tohle nahodou uz implementovany v jave ? jestli ne, tak by se to ale snad dalo jednoduse stahnout, pokud se tahle metoda vola casto, tak by to chtelo rychlejsi implementaci
 * @author Libor Wagner
 *
 * @param <E> 
 */
public class DisjointSets<E> {

	List<List<E>> lists;
	
	/** Create new instance of DisjointSet. */
	public DisjointSets() {
		this.lists = new LinkedList<List<E>>();
	}
	
	/** Make new set containing given element. */
	public List<E> makeSet(E x) {
		List<E> list = new LinkedList<E>();
		list.add(x);
		lists.add(list);
		return list;
	}
	
	/** Union two elements, connect two components. */
	public void union(E a, E b) {
		List<E> aList = find(a);
		List<E> bList = find(b);
		
		if (!aList.equals(bList)) {
			lists.remove(bList);
			aList.addAll(bList);
		}
		
	}
	
	/** Find components containing this elements. */
	public List<E> find(E x) {
		
		for (List<E> list : lists) {
			if (list.contains(x)) {
				return list;
			}
		}
		
		return makeSet(x);
	}
	
	/** @return All disjoint sets. */
	public List<List<E>> getDisjointSets() {
		return lists;
	}
}
