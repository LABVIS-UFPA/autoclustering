package br.rede.autoclustering.algorithms.sudephic;

import java.util.Map;

import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.Grid;

public class EquallySizedGrid implements ClusteringMethod{

	private float findRadiusD(int dimensions, int n){
		return ((Double) (dimensions / Math.pow(n, 1.0/dimensions))).floatValue(); 
	}
	
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			double[] lowerBounds = (double[]) sharedObjects.get(Parameter.ALL_LOWER_BOUNDS);
			double[] upperBounds = (double[]) sharedObjects.get(Parameter.ALL_UPPER_BOUNDS);			
			Number slices = (Number) sharedObjects.get(Parameter.AMR_SLICES);
			int d = instances.numAttributes(), n = instances.numInstances();
			float radius = 50*findRadiusD(d, n);
			
			Grid grid = null;
			if ( sharedObjects.get(Parameter.AMR_DENSITY)  != null )
				grid = startFromAMR(instances, lowerBounds, upperBounds, slices.intValue());
			else
				grid = startFromInstances(instances, lowerBounds, upperBounds, radius);
			sharedObjects.put(Parameter.PHC_RADIUS, radius);
			sharedObjects.put(Parameter.PHC_GRID, grid);
			
		}
	}

	private Grid startFromInstances(Instances instances, double[] lowerBounds,
			double[] upperBounds, float radius) {
		return buildGrid(instances, lowerBounds, upperBounds, radius);
	}

	private Grid startFromAMR(Instances instances, double[] lowerBounds, double[] upperBounds, int slices){
		return buildGrid(instances, lowerBounds, upperBounds, slices);
	}
	
	private Grid buildGrid(Instances instances, double[] lowerBounds, double[] upperBounds, int slices){
		Grid grid2 = new Grid();
		grid2.createGrid(slices, lowerBounds, upperBounds, instances);
		return grid2;
	}
	
	private Grid buildGrid(Instances instances, double[] lowerBounds, double[] upperBounds, float diameter){
		Grid grid2 = new Grid();
		grid2.createGrid(diameter, lowerBounds, upperBounds, instances);
		return grid2;
	}
	
	@Override
	public String getName() {
		return "Equally Sized Grid";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
				 sharedObjects.get(Parameter.ALL_LOWER_BOUNDS) != null &&
				 sharedObjects.get(Parameter.ALL_UPPER_BOUNDS) != null )
				return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return "Create and populate a grid";
	}
	
}
