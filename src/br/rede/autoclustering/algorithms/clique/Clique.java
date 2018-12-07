package br.rede.autoclustering.algorithms.clique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.structures.grid.Interval;
import br.rede.autoclustering.structures.grid.Unit;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.structures.groups.Subspace;


/**
 * 
 * Clique is an algorithm to find automatically high-density clusters
 * 
 * @author Samuel Félix and Rafael Veras
 *
 */
public class Clique {
	
	/**
	 * Identify all possible subspaces available in the database considering the number of slices and the threshold 
	 * @param instances	The database extracted by weka
	 * @param slices	The number of slices
	 * @param threshold	The threshold
	 * @return a list of a subspaces's array. Each value in the list represents a subspace of a specific dimension.
	 */
	public List<Group> identifySubspaces(Instances instances, int slices, float threshold ){
		Subspace[] subspaces = identifyDenseUnits(instances, slices, threshold);
		List<Group> groups = clusterByPartition(subspaces, instances.numInstances(), threshold);
		return groups;
	}
	
	public List<Group> clusterByPartition(Subspace[] subspaces, int population, float threshold){
		subspaces = joinSubspaces(subspaces, population, threshold);
 		while ( true ){
 			Arrays.sort(subspaces);
 			Subspace[] subspaces2 = joinSubspaces(subspaces, population, threshold);
 			if ( subspaces2 == null )
 				break;
 			else 
 				subspaces = subspaces2;
 		}
 		return getClusters(subspaces);
	}
	
    public Subspace[] joinSubspaces(Subspace[] subspaces, int population, float threshold){
		List<Subspace> candidates = new ArrayList<Subspace>();
		if ( subspaces.length > 1 ){
			for (int i = 0 ; i < subspaces.length - 1; i++ ){
				for (int j = i+1 ; j < subspaces.length ; j++ ){
					Subspace candidate = subspaces[i].subspaceJoin(subspaces[j], population, threshold);
					if ( candidate != null ) 
						candidates.add(candidate);
				}
			}
			return candidates.toArray(new Subspace[candidates.size()]);
		}
		return null;
    }
    
    private Subspace[] identifyDenseUnits(Instances instances, int slices, float threshold) {
    	Subspace[] subspaces = new Subspace[instances.numAttributes()];
		int count = 0;
 		for (int i = 0; i < instances.numAttributes(); i++) 
			subspaces[count++] = identifyDenseUnits(instances,instances.attribute(i), slices, threshold);
 		Arrays.sort(subspaces);
 		return subspaces;
	}



	public List<Group> getClusters(List<Subspace[]> subspacesByDimension){
    	ArrayList<Group> groups = new ArrayList<Group>();
    	for (Subspace[] subspaces : subspacesByDimension) {
			for (Subspace subspace : subspaces) {
				groups.addAll(getClusters(subspace));
			}
		}
    	return groups;
    }
    
    public List<Group> getClusters(Subspace[] subspacesOfADimension){
    	ArrayList<Group> groups = new ArrayList<Group>();
		for (Subspace subspace : subspacesOfADimension) {
			groups.addAll(getClusters(subspace));
		}
		
    	return groups;
    }
    
    public List<Group> getClusters(Subspace subspace){
    	List<Group> clusters = new ArrayList<Group>();

		for (Unit aUnit : subspace.getUnits()) {
			if (aUnit.getCluster()==null){
				Group cluster = new Group();
				clusters.add(cluster);
				dfs(aUnit, cluster, subspace);
			}
		}
		return clusters;
	}
	
	private void dfs(Unit u, Group cluster, Subspace subspace){
		cluster.addInstances(u.getInstances());
		u.setCluster(cluster);
		
		for (Attribute a : u.getAttributes().keySet()){
			Unit left = getLeftNeighbor(u, a, subspace);
			if (left!=null && left.getCluster()==null)
				dfs(left,cluster,subspace);
			
			Unit right = getRightNeighbor(u, a, subspace);
			if (right!=null && right.getCluster()==null)
				dfs(right,cluster,subspace);
			
		}
	}
	
	private Unit getLeftNeighbor(Unit u, Attribute attribute, Subspace subspace){
		for (Unit aUnit : subspace.getUnits()) {
			if (u.checkLeftNeighbor(aUnit, attribute))
				return aUnit; 
		}
		return null;
	}
	
	private Unit getRightNeighbor(Unit u, Attribute attribute, Subspace subspace){
		for (Unit aUnit : subspace.getUnits()) {
			if (u.checkRightNeighbor(aUnit, attribute))
				return aUnit; 
		}
		return null;
	}
	
	/**
	 * Extract Subspace of dense units from an array of instances.
	 * 
	 * @param instances		the object instances extracted by weka
	 * @param att			the attribute chosen
	 * @param partitions	the number of partitions
	 * @param threshold		the considered threshold
	 * @return the subspace of dense units.
	 */
    public Subspace identifyDenseUnits(Instances inst, Attribute att, int partitions, float threshold) {
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

