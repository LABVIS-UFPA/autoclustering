package br.rede.autoclustering.core;

import java.util.HashMap;
import java.util.Map;

import br.rede.autoclustering.util.DistanceType;

public class IndividualNode {

	private Node node;
	private boolean overlap;
	private boolean optionalK;
	private DistanceType distanceType;
	private Map<Parameter, Float> properties = new HashMap<Parameter,Float>();
	
	public IndividualNode(Node node, boolean overlap, boolean optionalK, DistanceType distanceType) {
		this.node = node;
		this.overlap = overlap;
		this.optionalK = optionalK;
		this.distanceType = distanceType;
	}

	public IndividualNode(Node node) {
		this.node = node;
		this.overlap = false;
		this.optionalK = false;
		this.distanceType = DistanceType.EUCLIDEAN;
	}
	
	public synchronized void putProperties(Parameter p, Float o){
		this.properties.put(p, o);
	}
	
	public synchronized Map<Parameter, Float> getProperties() {
		return properties;
	}
	
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public boolean isOverlap() {
		return overlap;
	}
	public void setOverlap(boolean overlap) {
		this.overlap = overlap;
	}
	public boolean isOptionalK() {
		return optionalK;
	}
	public void setOptionalK(boolean optionalK) {
		this.optionalK = optionalK;
	}
	public DistanceType getDistanceType() {
		return distanceType;
	}
	public void setDistanceType(DistanceType distanceType) {
		this.distanceType = distanceType;
	}
	
}
