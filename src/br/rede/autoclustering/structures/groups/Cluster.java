package br.rede.autoclustering.structures.groups;

import java.util.HashSet;
import java.util.Set;

import weka.core.Instance;

public class Cluster {

	private double volume = 0;
	private Set<Instance> instances = new HashSet<Instance>();

	public Set<Instance> getInstances() {
		return instances;
	}

	public void setInstances(Set<Instance> instances) {
		this.instances = instances;
	}

	public void addInstance(Instance p) {
		this.instances.add(p);
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}
	
}
