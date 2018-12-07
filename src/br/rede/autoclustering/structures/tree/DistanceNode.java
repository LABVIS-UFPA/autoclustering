package br.rede.autoclustering.structures.tree;

import java.util.List;

import weka.core.Instance;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.Distance;
import br.rede.autoclustering.util.SortedList;

public class DistanceNode implements Comparable<DistanceNode>{

	//centroid
	private Instance centroid;
	private int neighborsOfCluster;
	private Group group;
	private double closestNeighborDistance = Double.MAX_VALUE;
	
	private SortedList<Distance<DistanceNode>> distances = new SortedList<Distance<DistanceNode>>();
	
	public void addNode(Distance<DistanceNode> node){
		distances.add(node);
		updateClosest();
	}
	
	public SortedList<Distance<DistanceNode>> getDistances() {
		return distances;
	}
	
	public Instance getCentroid() {
		return centroid;
	}
	
	public void setCentroid(Instance centroid) {
		this.centroid = centroid;
	}
	
	private void updateClosest(){
		if ( !distances.isEmpty() )
			closestNeighborDistance = distances.get(0).getDistanceToInstance();
	}
	
	public double getClosestNeighborDistance() {
		updateClosest();
		return closestNeighborDistance;
	}
	
	public DistanceNode getClosestNeighbor(){
		if ( !distances.isEmpty() )
			return getDistances().get(0).getTheOtherOne(this);
		return null;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public void pop(){
		if ( !distances.isEmpty())
			distances.remove(0);
		updateClosest();
	}
	
	public boolean pop(DistanceNode toBePopped){
		boolean popped = false;
		for ( Distance<DistanceNode> di : distances )
			if ( di.getTheOtherOne(this) == toBePopped) {
				distances.remove(di);
				popped = true;
				break;
			}
		updateClosest();
		return popped;
	}
	
	public Group getGroup() {
		return group;
	}

	@Override
	public int compareTo(DistanceNode o) {
		double value = this.getClosestNeighborDistance();
		if ( value < o.getClosestNeighborDistance() )
			return -1;
		else if ( value > o.getClosestNeighborDistance() )
			return 1;
		return 0;
	}
	@Override
	public String toString() {
		return "Group: "+ neighborsOfCluster + " closest neighbor -> "+closestNeighborDistance ;
	}
	
	public void setNeighborsOfCluster(int neighborsOfCluster) {
		this.neighborsOfCluster = neighborsOfCluster;
	}
	
	public int getNeighborsOfCluster() {
		return neighborsOfCluster;
	}
}

