/**
 * 
 */
package model.graphutils;

/**
 * Stores Latitude and Longitude.
 * @author Ondrej Vanek
 *
 */
public class LatLon {
	
	private double lat;
	private double lon;
	
	public LatLon(double lat, double lon) {
		super();
		this.lat = lat;
		this.lon = lon;
	}

	/**
	 * @return the lat
	 */
	public double getLat() {
		return lat;
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LatLon other = (LatLon) obj;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
			return false;
		return true;
	}
	
	
	public double getDistance(LatLon obj) {
		double latDiff = this.lat - obj.lat;
		double lonDiff = this.lon - obj.lon;
		
		return Math.sqrt (latDiff*latDiff + lonDiff*lonDiff);
	}
	
	
	
	
	

}
