package br.rede.autoclustering.core;

import java.util.Map;



public interface ClusteringMethod {

	public abstract String getName();
	public abstract void executeStep(Map<Parameter, Object> sharedObjects);
	public abstract boolean isReady(Map<Parameter, Object> sharedObjects);
	public abstract String technicalInformation();
	
}
