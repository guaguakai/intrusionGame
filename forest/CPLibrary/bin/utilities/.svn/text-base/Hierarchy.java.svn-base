package utilities;

import java.util.HashSet;
import java.util.Set;

public class Hierarchy {
	public enum HIERARCHY {
		NONE, DEPTH_ONE, FULL_BINARY, CUSTOM
	};
	
	public static RecursiveList makeDepthOneHierarchy(Set<Integer> setGameIds) {
		RecursiveList partition = new RecursiveList();
		for (Integer gameId : setGameIds) {	
//			RecursiveList pt1 = new RecursiveList();
//			pt1.add(new Singleton(gameId));
			partition.add(new Singleton(gameId));
		}
		return partition;
	}
	
	public static RecursiveList makeFullBinaryHierarchy(Set<Integer> setGameIds) {
		if (setGameIds.size() <= 2) {
			return Hierarchy.makeDepthOneHierarchy(setGameIds);
		}
		
		int numTypes = setGameIds.size();
		int firstSubGameNumTypes = numTypes / 2;

		Set<Integer> setGG1 = new HashSet<Integer>();
		Set<Integer> setGG2 = new HashSet<Integer>();

		RecursiveList partition = new RecursiveList();
		int count = 0;
		for (Integer gameIds : setGameIds) {
			if (count < firstSubGameNumTypes) {
				setGG1.add(gameIds);
			} else {
				setGG2.add(gameIds);
			}
			count++;
		}

		partition.add(makeFullBinaryHierarchy(setGG1));
		partition.add(makeFullBinaryHierarchy(setGG2));

		return partition;
	}
	
	public static RecursiveList makeHierarchy(HIERARCHY h, Set<Integer> setGameIds) {
		switch (h) {
		case DEPTH_ONE:
			return Hierarchy.makeDepthOneHierarchy(setGameIds);
		case FULL_BINARY:
			return Hierarchy.makeFullBinaryHierarchy(setGameIds);
		case NONE:
		default:
			throw new RuntimeException("Unknown hierarchy.");
		}
	}
}
