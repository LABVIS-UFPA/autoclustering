package br.rede.autoclustering.structures.groups;

import br.rede.autoclustering.util.SortedList;
import weka.core.Instance;


public class Attractor implements Comparable<Attractor>{
	
	private double[] points; 
	private double density;
	private SortedList<Attractor> attracted = new SortedList<Attractor>();
	private Instance instance;
	
	public Attractor(Instance i) {
		this.density = i.weight();
		this.points = new double[i.numAttributes()];
		for (int j = 0; j < i.numAttributes(); j++) {
			points[j] = (int)i.value(j);
		}
		this.instance = i;
	}
	
	public double getDensity() {
		return density;
	}

	public Instance getInstance() {
		return instance;
	}
	
	public SortedList<Attractor> getAttracted() {
		return attracted;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (int i = 0; i < points.length; i++) 
			result += points[i];
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attractor other = (Attractor) obj;
		for (int i = 0; i < points.length; i++) {
			if (points[i] != other.getPoints()[i])
				return false;
		}
		return true;
	}

	
	public double[] getPoints() {
		return points;
	}

	@Override
	public int compareTo(Attractor o) {
		for (int i = 0; i < points.length; i++) {
			if (points[i] == o.getPoints()[i])
				continue;
			else if (points[i] < o.getPoints()[i])
				return -1;
			else if (points[i] > o.getPoints()[i])
				return 1;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < points.length; i++) {
			sb.append(points[i]).append(",");
		}
		return sb.toString();
	}
	
	
}
