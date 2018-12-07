/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.rede.autoclustering.algorithms.clique;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.Interval;
import br.rede.autoclustering.structures.grid.Unit;
import br.rede.autoclustering.structures.groups.Subspace;
import br.rede.autoclustering.util.DistanceType;

/**
 *
 * @author Samuel FÃ©lix
 */
public class DenseAreas implements ClusteringMethod{
	
	/**
	 * Extract Subspace of dense units from an array of instances.
	 * 
	 * @param instances		the object instances extracted by weka
	 * @param att			the attribute chosen
	 * @param partitions	the number of partitions
	 * @param threshold		the considered threshold
	 * @return the subspace of dense units.
	 */
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			Float threshold = (Float) sharedObjects.get(Parameter.CLIQUE_THRESHOLD);
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int slices = ((Number) sharedObjects.get(Parameter.CLIQUE_SLICES)).intValue();
			Subspace[] subspaces = new Subspace[instances.numAttributes()];
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			
			//System.out.println("INICIANDO o bloco ´DenseAreas´");
			if ( distance == null )
				distance = DistanceType.EUCLIDEAN;
			int count = 0;
			for (int i = 0; i < instances.numAttributes(); i++) 
				subspaces[count++] = identifyDenseUnits(instances,instances.attribute(i), slices, threshold);
			Arrays.sort(subspaces);
			sharedObjects.put(Parameter.CLIQUE_SUBSPACES_BEGIN, subspaces);
			//System.out.println("TERMINANDO o bloco ´DenseAreas´");
		}
	}
	
	private Subspace identifyDenseUnits(Instances inst, Attribute att, int partitions, float threshold){

		Instance[] instances = new Instance[inst.numInstances()];
    	for (int i = 0; i < inst.numInstances(); i++) 
			instances[i] = inst.instance(i);
    	SortedSet<Attribute> attributes = new TreeSet<Attribute>(getAttributeComparator());
    	attributes.add(att);
    	Subspace subspace = new Subspace(attributes);
        Arrays.sort(instances,getInstanceComparator(att));
        int numberOfInstances = instances.length;
        // The first value of the interval 
        double begin = instances[0].value(att);
        // The last value of the vector
        double lastEnd = instances[numberOfInstances - 1].value(att);
        // The amount of range increasing
        double value = (lastEnd - begin ) / partitions ;
        // The end of the interval
        double end = value + begin;
        int position = 0;
        float percentage = 0;
        List<Instance> total = new ArrayList<Instance>();
        Instance v = instances[0];
        
        //This while will organize the values according to the slices
        while ( position <= numberOfInstances ){
        	// If the value lies on the current interval like if 2 is between 1 and 3
        	// If true, it's added to the List
            if ( v.value(att) < end ) {
                total.add(v);
                if ( position == numberOfInstances )
                	break;
                v = instances[position++];
            }
            //If It's not, It means there are no more members of the current interval, when can "close" it
            else {
            	// If there is at least one member of the interval, let's create an unit
            	if ( total.size() != 0 ) {
            		percentage = (float) total.size() / numberOfInstances ;
            		// If this unit has a dense population according to the threshold informed by the user
            		//It is a dense unit
            		if ( percentage >= threshold ) 
            			subspace.addUnit(new Unit(new Interval(begin, end), total, att));
           			total.clear();
            	}
                //Reorganize the bottom and up range values
                if ( v.value(att) == lastEnd ){
                	begin = end;
                	end = lastEnd + 0.0001f ;
                }
                else {
                	begin = end;
                	end = value + begin;
                }
            }
        }
        percentage = (float) total.size() / numberOfInstances ;
        if ( percentage >= threshold )
        	subspace.addUnit(new Unit(new Interval(begin, end - 0.0001f ), total, att));
        System.gc();
        return subspace;
	}

	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
				 sharedObjects.get(Parameter.CLIQUE_SLICES) != null &&
				 sharedObjects.get(Parameter.CLIQUE_THRESHOLD) != null )
				return true;
			return false;
	}

	@Override
	public String technicalInformation() {
		return "This method aims to find dense areas";
	}

	@Override
	public String getName() {
		return "DenseAreas";
	}
	
    /**
     * 
     * Return the attribute comparator
     * @return comparator
     */
    private static Comparator<Attribute> getAttributeComparator(){
    	Comparator<Attribute> i = new Comparator<Attribute>() {
			@Override
			public int compare(Attribute o1, Attribute o2) {
				return o1.name().compareTo(o2.name());
			}
		};
		return i;
    }
    
    /**
     * Get the Instance Comparator
     * @param att
     * @return comparator
     */
    private static Comparator<Instance> getInstanceComparator(final Attribute att){
    	Comparator<Instance> i = new Comparator<Instance>() {
			@Override
			public int compare(Instance o1, Instance o2) {
				double value1 = o1.value(att);
				double value2 = o2.value(att);
				return value1 < value2 ? -1 : value1 > value2 ? 1 : 0;
			}
		};
		return i;
    }
    
}
