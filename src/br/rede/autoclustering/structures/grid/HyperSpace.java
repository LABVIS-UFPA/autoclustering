package br.rede.autoclustering.structures.grid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import br.rede.autoclustering.algorithms.denclue.DenclueFunctions;
import br.rede.autoclustering.util.DistanceType;

import weka.core.Instance;
import weka.core.Instances;

public class HyperSpace {

	private int dimension;

	/* Influence of a point in its neighborhood */
	private double sigma;

	/* Lower bound of density to consider */
	private double epsilon;

	/* Bounds of the multi-dimensional space */
	private double[] upper_bounds, lower_bounds;

	private SortedMap<String, HyperCube> hypercubes = new TreeMap<String, HyperCube>();
	private List<String> high_populated_keys  = new ArrayList<String>();  // Regions in the space that satisfy entities minimum bound
	
	// Constructor
	public HyperSpace(Instances instances, double[] upper_bounds, double[] lower_bounds, double sigma, double xi, DistanceType distance) {
		this.sigma = sigma;
		this.epsilon = xi;
		this.dimension = instances.numAttributes();
		this.upper_bounds = upper_bounds;
		this.lower_bounds = lower_bounds;
		
	    for (int i = 0; i < instances.numInstances(); i++){
	    	String interval = findInterval(instances.instance(i));
	    	if ( hypercubes.get(interval) == null ) {
	    		HyperCube newOne = new HyperCube(this,interval);
	    		hypercubes.put(interval, newOne);
	    		verifyIfThereIsANeighborAlready(newOne);
	    	}
	    	hypercubes.get(interval).addInstance(instances.instance(i));
	    }
		removeLowPopulatedHypercubes();
		calculateDensity(distance);
		sortEntities();
	}
	
	public void sortEntities(){
		Collections.sort(getEntities(), new Comparator<Instance>() {
			@Override
			public int compare(Instance o1, Instance o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
	}
	
	public void calculateDensity(DistanceType distance){
		List<Instance> entities = getEntities();
		for (Instance i : entities)
			i.setWeight(DenclueFunctions.calculateDensity(i, entities, sigma, distance));
	}
	
	/*
	 * TODO Descobrir se o ultimo deve ser incluido no ultimo intervalo
	 * 5 pertence ao
	 * 
	 * 3-5 ou 5-7 se ele o 5 for o ultimo elemento
	 *  
	 */
	private String findInterval(Instance i){
		StringBuffer interval = new StringBuffer(i.numAttributes());
		for (int j = 0; j < i.numAttributes(); j++) {
			if ( j != 0 )
				interval.append(",");
			double d = (((int) ( (i.value(j) - lower_bounds[j] -0.0001) / hypercubeEdgeLenght()) ) * 
					hypercubeEdgeLenght() + lower_bounds[j]) + hypercubeEdgeLenght();
			interval.append(d);
		}
		return interval.toString();
	}
	
	public int getDimension() {
		return dimension;
	}
	

	/**
	 * Retrieve the length of a partition (an edge of a hypercube).
	 * 
	 * @return the length of a hypercube edge
	 * */
	double hypercubeEdgeLenght() {
		return (2 * sigma);
	}

	/**
	 * Retrieve the minimum number of entities of a high populated hypercube.
	 * 
	 * @return the minimum number of entities of a high populated hypercube.
	 * */
	double minimumObjectsInHypercubes() {
		return (epsilon / (2 * dimension));
	}

	public double[] getUpper_bounds() {
		return upper_bounds;
	}


	public void verifyIfThereIsANeighborAlready(HyperCube hc){
		int notEqualDimensions = 0;
		for ( HyperCube hyper : hypercubes.values() ){
			boolean isNeighbor = true;
			for ( int dim = 0 ; dim < dimension ; dim++ ){
				if ( hc.upper_bounds[dim] == hyper.upper_bounds[dim]){
					continue;
				}else if ( hc.upper_bounds[dim] == hyper.lower_bounds[dim] || hyper.upper_bounds[dim] == hc.lower_bounds[dim]){
					if ( notEqualDimensions > 1 ) {
						isNeighbor = false;
						break;
					}
					notEqualDimensions++;
				}else {
					isNeighbor = false;
					break;
				}
			}
			if ( isNeighbor ) {
				hyper.getNeighbors().add(HyperCube.getKeyFromArray(hc.upper_bounds, dimension));
				hc.getNeighbors().add(HyperCube.getKeyFromArray(hyper.upper_bounds, dimension));
			}
		}
	}
	

	/**
	 * Remove low populated hypercubes, except those who are connected to a high
	 * populated hypercube.
	 * 
	 * */
	public void removeLowPopulatedHypercubes() {

	    List<String> deleted_keys = new ArrayList<String>();
	    this.high_populated_keys.clear();
	    Iterator<String> it = hypercubes.keySet().iterator();
	    while ( it.hasNext() ){
	    	String i = it.next();
	    	HyperCube hc = hypercubes.get(i);
	        // Mark high populated cube and store it internally
	        if( hc.getInstances().size() >= this.minimumObjectsInHypercubes() )
	            this.high_populated_keys.add( i ); // Store cube key
	        // Mark empty cubes
	        if( hc.getInstances().isEmpty() )
	            deleted_keys.add(i); 		 // Mark key as deleted
	    }

	    for (String deleted : deleted_keys)
	    	this.hypercubes.remove(deleted);

	    /** Remove keys of empty hypercubes from other hypercubes **/
	    for(HyperCube hc : this.hypercubes.values())
	        hc.removeEmptyNeighbors( deleted_keys );
	   
	    deleted_keys.clear();

	    /** Remove hypercubes that aren't connected to high populated hypercubes
	     * */
	    for ( String hc : hypercubes.keySet() )
	        if( !(hypercubes.get(hc).isNeighbor( high_populated_keys, this.hypercubes )) )  // Cube isn't connected to high populated cube
	        	deleted_keys.add(hc);
	    
	    for (String deleted : deleted_keys)
	    	this.hypercubes.remove(deleted);

	}

	/**
	 * Retrieve the number of entities in the spatial region.
	 * 
	 * @return The number of entities associated with the HyperSpace object.
	 * */
	public int getNumEntities() {
		int total = 0;
		for ( HyperCube hc : hypercubes.values() )
			total += hc.getInstances().size();
		return total;
	}
	
	public List<Instance> getEntities(){
		List<Instance> entities = new ArrayList<Instance>();
		for ( HyperCube hc : hypercubes.values() )
			for ( Instance i : hc.getInstances()) 
				entities.add(i);
		return entities;
	}
	
	public Map<String,HyperCube> getHypercubes() {
		return hypercubes;
	}
	
	public double getSigma() {
		return sigma;
	}
	
	public double getEpsilon() {
		return epsilon;
	}
}
