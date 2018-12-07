package br.rede.autoclustering.algorithms.denclue;

import java.util.Map;

import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.HyperSpace;
import br.rede.autoclustering.util.DistanceType;

public class ASH implements ClusteringMethod{
	
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			float sigma = ((Number) sharedObjects.get(Parameter.DENCLUE_SIGMA)).floatValue();
			float epsilon = ((Number) sharedObjects.get(Parameter.DENCLUE_EPSILON)).floatValue();
			
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			if ( distance == null )
				distance = DistanceType.EUCLIDEAN;
			
			double[] lower_bounds = (double[]) sharedObjects.get(Parameter.ALL_LOWER_BOUNDS);
			double[] upper_bounds = (double[]) sharedObjects.get(Parameter.ALL_UPPER_BOUNDS);
			
			HyperSpace spatialRegion = new HyperSpace(instances, upper_bounds, lower_bounds, sigma, epsilon, distance);
			sharedObjects.put(Parameter.DENCLUE_HYPER_SPACE, spatialRegion);
		}
	}

	@Override
	public String getName() {
		return "ASH";
	}
	
	@Override
	public String technicalInformation() {
		return "This method aims to create a HyperSpace";
	}

	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
			 sharedObjects.get(Parameter.DENCLUE_SIGMA) != null &&
			 sharedObjects.get(Parameter.ALL_LOWER_BOUNDS) != null &&
			 sharedObjects.get(Parameter.ALL_UPPER_BOUNDS) != null &&
			 sharedObjects.get(Parameter.DENCLUE_EPSILON) != null  )
			return true;
		return false;
	}


}
