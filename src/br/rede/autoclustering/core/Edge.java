/*
 * Created on 01-Aug-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.rede.autoclustering.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.ParameterOptions;


/**
 * @author ag227
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Edge {

	private Node nodeIn;
	private Node nodeOut;
	private float probability;
	private float probOverlap;
	private float probOptionalK;
	private float probDistance[];
	private Map<Parameter, Float[]> probParameters = new HashMap<Parameter, Float[]>();
	
	public static final int NUM_DISTANCES = DistanceType.values().length;
	

	public Edge(Node in, Node out) {
		nodeIn = in;
		nodeOut = out;
		probOverlap = 0.5F;
		probOptionalK = 0.5F;
		List<Parameter> parameters = out.getParameters();
		this.probDistance = new float[Edge.NUM_DISTANCES];
		for (int i = 0; i < Edge.NUM_DISTANCES; i++)
			this.probDistance[i]= (1f/Edge.NUM_DISTANCES);
		for (Parameter p : parameters) {
			int parametersLength = ParameterOptions.getParameterValues(p).length;
			Float[] probs =  new Float[parametersLength];
			for (int j = 0; j < parametersLength; j++) 
				probs[j] = 1f/parametersLength;
			probParameters.put(p,probs);
		}
		in.getEdgesOut().add(this);
		out.getEdgesIn().add(this);
	}
	
	/**
	 * @return Returns the probability.
	 */
	public float getProbability() {
		return probability;
	}
	/**
	 * @param probability The probability to set.
	 */
	public void setProbability(float probability) {
		this.probability = probability;
	}
	/**
	 * @return Returns the nodeIn.
	 */
	public Node getNodeIn() {
		return nodeIn;
	}
	/**
	 * @param nodeIn The nodeIn to set.
	 */
	public void setNodeIn(Node nodeIn) {
		this.nodeIn = nodeIn;
	}
	/**
	 * @return Returns the nodeOut.
	 */
	public Node getNodeOut() {
		return nodeOut;
	}
	/**
	 * @param nodeOut The nodeOut to set.
	 */
	public void setNodeOut(Node nodeOut) {
		this.nodeOut = nodeOut;
	}
	public float getProbOverlap() {
		return probOverlap;
	}
	public void setProbOverlap(float probOverlap) {
		this.probOverlap = probOverlap;
	}
	public float getProbOptionalK() {
		return probOptionalK;
	}
	public void setProbOptionalK(float probOptionalK) {
		this.probOptionalK = probOptionalK;
	}
	public float[] getProbDistance() {
		return probDistance;
	}
	public void setProbDistance(float[] probDistance) {
		this.probDistance = probDistance;
	}
	public Map<Parameter, Float[]> getProbParameters() {
		return probParameters;
	}
	
	@Override
	public String toString() {
		return nodeIn + " -> " + nodeOut;
	}
}
