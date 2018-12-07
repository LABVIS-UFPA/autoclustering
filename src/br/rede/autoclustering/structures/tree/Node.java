package br.rede.autoclustering.structures.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.structures.grid.Grid;

public class Node  {

	private int nodesLevel;
	private Node parent;
	private Set<NodeCluster> clusters = new HashSet<NodeCluster>();
	private List<Node> children = new ArrayList<Node>();
	
	public Node(int nodesLevel) {
		this.nodesLevel = nodesLevel;
	}

	//Data
	private Grid grid;
	private List<Instance> instances = new ArrayList<Instance>();

	public List<Instance> getInstances() {
		return instances;
	}
	
	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}
	
	public void setInstances(Instances instances) {
		this.instances.clear();
		for (int i = 0; i < instances.numInstances(); i++) 
			this.instances.add(instances.instance(i));
	}
	
	public void addChild(Node child){
		this.children.add(child);
		child.setParent(parent);
	}
	
	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}
	
	public boolean isLeaf() {
		return children.isEmpty();
	}

	public void setGrid(Grid grid) {
		this.grid = grid;
		for ( Cell instances : grid.getCells().values() ) {
			this.instances.addAll(instances.getInstances());
		}
	}
	
	public Grid getGrid() {
		return grid;
	}

	public int getNodesLevel() {
		return nodesLevel;
	}

	public void setNodesLevel(int nodesLevel) {
		this.nodesLevel = nodesLevel;
	}

	public Set<NodeCluster> getClusters() {
		for ( Node child : children ) 
			clusters.addAll(child.getClusters());
		return clusters;
	}
	
}
