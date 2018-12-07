package br.rede.autoclustering.util;

import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;

public class InstancesRandomizer {

	private Instances originalInstances;
	private Instances trainInstances;
	private Instances testInstances;
	private double trainPercent;

	public InstancesRandomizer(Instances instances, double trainPercentage) {
		this.originalInstances = instances;
		long train = Math.round(originalInstances.numInstances() * trainPercentage / 100.0);
		long test = originalInstances.numInstances() - train;
		trainInstances = new Instances(instances);
		trainInstances.setRelationName(originalInstances.relationName()+"-train");
		testInstances = new Instances(instances,0,0);
		testInstances.setRelationName(originalInstances.relationName()+"-test");
		Random random = new Random();
		while ( testInstances.numInstances() < test ) {
			int position = random.nextInt(trainInstances.numInstances());
			Instance instance = trainInstances.instance(position);
			instance.setDataset(testInstances);
			testInstances.add(instance);
			trainInstances.delete(position);
		}
	}

	public double getTrainPercent() {
		return trainPercent;
	}

	public void setTrainPercent(double trainPercent) {
		this.trainPercent = trainPercent;
	}

	public Instances getTrainInstances() {
		return trainInstances;
	}
	
	public Instances getOriginalInstances() {
		return originalInstances;
	}
	
	public Instances getTestInstances() {
		return testInstances;
	}
	
}
