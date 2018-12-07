package br.rede.autoclustering.structures.grid;



/**
 * 
 * Interval is a class representing a range
 * @author samuel
 *
 */
public class Interval {
	private double min;
	private double max;
		
	/**
	 * @param min
	 * @param max
	 */
	public Interval(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	public double getMin() {
		return min;
	}
	public void setMin(double min) {
		this.min = min;
	}
	public double getMax() {
		return max;
	}
	public void setMax(double max) {
		this.max = max;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		sb.append(min).append(", ").append(max).append("(");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Interval)) {
			return false;
		}
		Interval other = (Interval) obj;
		if (Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max)) {
			return false;
		}
		if (Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min)) {
			return false;
		}
		return true;
	}
	
}
