package br.rede.autoclustering.algorithms.denclue;

import java.util.List;
import java.util.SortedMap;

import br.rede.autoclustering.structures.groups.Attractor;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.SortedList;

import weka.core.Instance;

public class DenclueFunctions {
	
	/** Find density-attractor for an entity. The density-attractor is
	 * obtained executing a hill climbing algorithm.
	 *
	 *  @param entity The spatial point used to calculate the gradient.
	 *  @param iter Iterator over all dataset entities.
	 *  @param sigma Parameter that ponderates the influence of an entity into
	 *  another
	 *
	 * @return An entity that is the density-attractor for the given
	 *  entity.
	 * */
	public static Attractor hillClimbling( Instance entity, List<Instance> entities , double sigma, double epsilon, DistanceType distance){
	    double delta = 1;
	    // Set the initial density-attractor to the received entity
	    Instance curr_attractor  = (Instance) entity.copy();
	    Attractor found_attractor = null;

	    // Execute the hill climbing algorithm until it finds the local maxima of density function
	    int MAX_ITERATIONS = 1000;
	    boolean reachedTop = false;
	    do{
	        // Avoid infinite loops
	        if( --MAX_ITERATIONS <= 0 )  break;

	        // Store last calculated values for further comparison
	        Instance last_attractor = (Instance) curr_attractor.copy();

	        // Calculate the gradient of density function at current candidate to attractor
	        Instance curr_gradient = DenclueFunctions.calculateGradient(last_attractor, entities, sigma, distance);

	        // Calculate next candidate to attractor
	        double grad_entity_norm = getEuclideanNorm(curr_gradient);
	        for ( int i = 0 ; i < curr_attractor.numAttributes() ; i++ )
	        	curr_attractor.setValue( i, last_attractor.value(i)+((double) (delta/grad_entity_norm)) * curr_gradient.value(i) );

	        // Calculate density in current attractor
	        curr_attractor.setWeight(calculateDensity( curr_attractor, entities, sigma, distance ));
	        // Verify whether local maxima was found
	        reachedTop = ( curr_attractor.weight() < last_attractor.weight() );
	        if( reachedTop ) 
	        	found_attractor = new Attractor((Instance) last_attractor.copy());

	    }while( !reachedTop );

	    if( MAX_ITERATIONS <= 0 )  
	    	found_attractor = new Attractor((Instance)curr_attractor.copy());

	    return found_attractor;
	}

	
	public static double calculateInfluence(Instance first, Instance second, double sigma, DistanceType distanceType){
	    double distance = DistanceMeasures.getInstance().calculateDistance(first, second, distanceType);
	    // Verify whether the entities are the same (indirectly)
	    if( distance == 0 )
	        return 0;  // Influence is zero if entities are the same
	    double exponent = - Math.pow( distance , 2) / (2.0 * Math.pow(sigma,2) );
	    double influence = Math.exp(exponent);
	    return influence;
	}
	
	public static double distanceBetween( Attractor entity1, Attractor entity2 ){
	    Instance difference = new Instance(entity1.getPoints().length);
	    for (int i = 0 ; i < entity1.getPoints().length; i++) 
	    	difference.setValue( i , entity1.getPoints()[i] - entity2.getPoints()[i]);
	    return getEuclideanNorm(difference);
	}


	/** Calculate the Euclidean norm of a DatasetEntity.
	 *
	 * @return value of the Euclidean norm of the entity
	 * */
	public static double getEuclideanNorm(Instance instance) {
	    double component_squares_sum = 0;
	    for( int i=0 ; i < instance.numAttributes() ; i++)
	        component_squares_sum += Math.pow(instance.value(i), 2.0);
	    return Math.sqrt(component_squares_sum);
	}
	

	/** Calculate the density in an entity. It's defined as the sum of the
	 * influence of each another entity of dataset.
	 *
	 *  @param entity The entity to calculate density
	 *  @param iter Iterator over entities of dataset.
	 *  @param sigma Parameter that ponderates the influence of an entity into another
	 *
	 * @return The value of density in entity.
	 * */
	public static double calculateDensity( Instance entity, List<Instance> iter , double sigma, DistanceType distance){
	    double density = 0;
	    for( Instance i : iter )
	        density += calculateInfluence( entity, i, sigma, distance );
	    return density;
	}
	
	/** Calculate gradient of density functions in a given spatial point.
	 *
	 *  @param entity The spatial point used to calculate the gradient.
	 *  @param iter Iterator over all dataset entities.
	 *  @param sigma Parameter that ponderates the influence of an entity into another
	 *
	 * @return The vector that represents the gradient of the influence
	 *  function in a given spatial point.
	 * */
	public static Instance calculateGradient( Instance entity, List<Instance> iter, double sigma , DistanceType distance){
	    Instance gradient = new Instance(entity.numAttributes());
	    for( int i=0 ; i < entity.numAttributes(); i++)
	        gradient.setValue(i,0);
	    // Iterate over all entities and calculate the factors of gradient
	    for (Instance ins : iter){
	        double curr_influence = calculateInfluence(entity, ins, sigma, distance);
	        // Calculate the gradient function for each dimension of data
	        for(int i=0 ; i < entity.numAttributes() ; i++){
	            double curr_difference = (ins.value(i) - entity.value(i));
	            gradient.setValue(i, gradient.value(i) + curr_difference * curr_influence);
	        }
	    }
	    return gradient;
	}
	
	public static boolean pathBetweenExists( Attractor attractor1, Attractor attractor2, double sigma, SortedMap<Attractor, SortedList<Instance>> clusters, DistanceType distance ){
	    /* If the distance between entities is less or equal to sigma, a path can
	     * be established between them */
	    if( distanceBetween(attractor1, attractor2) <= sigma )
	        return true;
	    else {
	    	List<Instance> atrInstances1 = clusters.get(attractor1);
	    	List<Instance> atrInstances2 = clusters.get(attractor2);
	    	if ( atrInstances1 != null && atrInstances2 != null )
	    	for ( Instance atInstance : atrInstances1 )
	    		for ( Instance atInstance2 : atrInstances2 )
	    			if ( DistanceMeasures.getInstance().calculateDistance(atInstance, atInstance2, distance) <= sigma ) 
	    				return true;
	    }
	    return false;
	}
}
