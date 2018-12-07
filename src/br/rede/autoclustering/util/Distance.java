package br.rede.autoclustering.util;


public class Distance<I> implements Comparable<Distance<I>> {

	/**
	 * Distance among instances
	 */
	private double distanceToInstance;
	private I instance1;
	private I instance2;

	public double getDistanceToInstance() {
		return distanceToInstance;
	}

	public void setDistanceToInstance(double distanceToInstance) {
		this.distanceToInstance = distanceToInstance;
	}

	public I getInstance1() {
		return instance1;
	}

	public void setInstance1(I instance) {
		this.instance1 = instance;
	}

	public void setInstance2(I instance) {
		this.instance2 = instance;
	}

	public I getInstance2() {
		return instance2;
	}
	
	@Override
	public int compareTo(Distance<I> id) {
		if (this.distanceToInstance < id.getDistanceToInstance())
			return -1;
		if (this.distanceToInstance > id.getDistanceToInstance())
			return 1;
		return 0;
	}

	@Override
	public String toString() {
		return new StringBuilder(instance1.toString()).append("->").append(instance2.toString()).append("=").append(distanceToInstance).toString();
	}

	public I getTheOtherOne(I key) {
		return instance1 == key ? instance2 : instance1;
	}
	
	
}
