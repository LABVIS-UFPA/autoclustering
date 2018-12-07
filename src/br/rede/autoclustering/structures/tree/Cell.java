package br.rede.autoclustering.structures.tree;

import java.util.ArrayList;
import java.util.List;

import weka.core.Instance;
import br.rede.autoclustering.structures.groups.Group;

public class Cell implements Comparable<Cell>{
	
	private boolean refined = false;
	private float density;
	private String key;
	private List<Group> groups = new ArrayList<Group>();
	private List<Instance> instances = new ArrayList<Instance>();
	private List<NodeCluster> clusters = new ArrayList<NodeCluster>();
	
	public Cell(String key) {
		this.key = key;
	}

	public List<NodeCluster> getClusters() {
		return clusters;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	@Override
	public String toString() {
		return key;
	}

	public boolean isRefined() {
		return refined;
	}

	public void setRefined(boolean refined) {
		this.refined = refined;
	}

	@Override
	public int compareTo(Cell o) {
		if ( density > o.getDensity() )
			return -1;
		else if ( density < o.getDensity() )
			return 1;
		return 0;
	}
	
	public float getDensity() {
		return density;
	}
	public void setDensity(float density) {
		this.density = density;
	}
	
	public List<Group> getGroups() {
		return groups;
	}
	
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}
	
}
