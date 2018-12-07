package br.rede.autoclustering.core;

import java.util.HashMap;
import java.util.Map;

public class EdgeProbability {

	private double totalProb;
	private double optinalKProb;
	private double overlapProb;
	private double[] distanceProb;
	private Map<Parameter,Float[]> parametersProb = new HashMap<Parameter, Float[]>();
	
	public double getTotalProb() {
		return totalProb;
	}
	public void setTotalProb(double totalFitness) {
		this.totalProb = totalFitness;
	}
	public double getOptinalKProb() {
		return optinalKProb;
	}
	public void setOptinalKProb(double probOptinalK) {
		this.optinalKProb = probOptinalK;
	}
	public double getOverlapProb() {
		return overlapProb;
	}
	public void setOverlapProb(double OverlapProb) {
		this.overlapProb = OverlapProb;
	}
	public double[] getDistanceProb() {
		return distanceProb;
	}
	public void setDistanceProb(double[] distanceProb) {
		this.distanceProb = distanceProb;
	}
	
	public Map<Parameter, Float[]> getParametersProb() {
		return parametersProb;
	}
	
	public void setParametersProb(Map<Parameter, Float[]> parametersProb) {
		this.parametersProb = parametersProb;
	}
}
