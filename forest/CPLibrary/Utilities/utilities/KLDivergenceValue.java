package utilities;

public class KLDivergenceValue implements Comparable<KLDivergenceValue> {
	public int gameId;
	public double kldValue;
	
	public KLDivergenceValue(int gameId, double kldValue) {
		super();
		this.gameId = gameId;
		this.kldValue = kldValue;
	}

	@Override
	public int compareTo(KLDivergenceValue o) {
		// TODO Auto-generated method stub
		if (this.equals(o)) {
			return 0;
		} else if (this.kldValue < o.kldValue) {
			return -1;
		} else
			return 1;
	}		
}
