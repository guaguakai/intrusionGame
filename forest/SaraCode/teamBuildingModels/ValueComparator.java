package model.teamBuildingModels;

import java.util.Comparator;
import java.util.Map;

public class ValueComparator implements Comparator {
	Map base;

	public ValueComparator(Map base) {
		this.base = base;
	}

	@Override
	public int compare(Object arg0, Object arg1) {
		if ((Double) base.get(arg0) < (Double) base.get(arg1)) {
			return 1;
		} else if ((Double) base.get(arg0) == (Double) base.get(arg1)) {
			return 0;
		} else {
			return -1;
		}
	}

}
