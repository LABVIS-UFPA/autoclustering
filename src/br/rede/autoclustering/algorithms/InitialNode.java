package br.rede.autoclustering.algorithms;

import java.util.Arrays;
import java.util.Map;

import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.util.DistanceType;

public class InitialNode implements ClusteringMethod{

	@Override
	public void executeStep(Map<Parameter, Object> sharedObjects) {
		Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
		double[] lowerBounds = new double[instances.numAttributes()],upperBounds = new double[instances.numAttributes()];
		Arrays.fill(lowerBounds, Double.MAX_VALUE);
		Arrays.fill(upperBounds, Double.MIN_VALUE);
		for (int i = 0; i < instances.numInstances(); i++) {
			for (int j = 0; j < instances.numAttributes(); j++) { 
				if ( instances.instance(i).value(j) < lowerBounds[j] )
					lowerBounds[j] = instances.instance(i).value(j);
				if ( instances.instance(i).value(j) > upperBounds[j] )
					upperBounds[j] = instances.instance(i).value(j);
			}
		}
		sharedObjects.put(Parameter.ALL_INSTANCES, instances);
		sharedObjects.put(Parameter.ALL_LOWER_BOUNDS, lowerBounds);
		sharedObjects.put(Parameter.ALL_UPPER_BOUNDS, upperBounds);
		sharedObjects.put(Parameter.ALL_DISTANCE, DistanceType.EUCLIDEAN);
	}

	@Override
	public String getName() {
		return "Reading Instances";
	}

	@Override
	public String technicalInformation() {
		return "This initial node is responsible for reading weka Instances";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		return true;
	}
	
}
