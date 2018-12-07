package br.rede.autoclustering.structures.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.rede.autoclustering.algorithms.denclue.DenclueFunctions;


import weka.core.Instance;

public class HyperCube {
	
	private double edge_length;

	// Instances in this hypercube
	private List<Instance> instances = new ArrayList<Instance>(); 

	// HyperCubes adjacent to this spatial region
	private List<String> neighbors = new ArrayList<String>(); 

	// Sum of each entity component. It speeds hypercube mean calculation
	private double[] entities_sum;

	private int dimension; 

    /* Influence of a point in its neighborhood */
    double sigma;

    /* Lower bound of density to consider */
    double xi;

    //density
    double density = 0;
    
    /* Bounds of the multi-dimensional space */
    double[] upper_bounds;
    double[] lower_bounds;

	public HyperCube(HyperSpace hyperspace, String lowBounds) {
		this.dimension = hyperspace.getDimension();
		this.edge_length = hyperspace.hypercubeEdgeLenght();
		this.upper_bounds = HyperCube.getArrayFromKey(lowBounds, dimension);
		this.lower_bounds = new double[dimension];
		this.entities_sum = new double[dimension];
	}

	public void setNeighbors(List<String> neighbors) {
		this.neighbors = neighbors;
	}
	
	public void addInstance( Instance instance ) {
	    /* Verify whether this object is inside the region represented by this
	     * hypercube */
	    boolean outside_hypercube = false;
	    for(int i=0 ; i < this.dimension ; i++){

	        // Calculate lower bound of current component
	    	lower_bounds[i] =  upper_bounds[i] - this.edge_length;
	        double curr_comp_value = instance.value(i); // Value of i-th component
	        if( ((int)curr_comp_value < (int)lower_bounds[i]) || ((int)curr_comp_value > (int)upper_bounds[i]) ){
	            // instance outside this spatial region
	            /*System.out.println( "Entity " + instance + " isn't inside this HyperCube" );
	            System.out.println( "Component " + i + " should be in [" + lower_bounds[i] );
	            System.out.println( "," + upper_bounds[i] + ")" );*/

	            outside_hypercube = true;
	            break;
	        }
	    }
	    if( !outside_hypercube ){
	    	this.instances.add(instance);  // Add object to hypercube
	        // Update sum of entities components
	        for(int i=0 ; i < dimension; i++){
	            this.entities_sum[i] = instance.value(i);
	        }
	    }
	}
	
	public List<Instance> getInstances() {
		return instances;
	}
	
	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}
	

    public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	/** Retrieve the length of a partition (an edge of a hypercube).
     *
     * @return the length of a hypercube edge
     * */
    public double hypercubeEdgeLenght() {
    	return (2 * this.sigma);  
    }

    public double minimumObjectsInHypercubes() {
    	return (this.xi / (2 *this.dimension) );  
    }
    

    /** Get the mean element of the hypercube.
     *
     * @return a DatasetEntity representing the mean of the hypercube.
     * */
    public Instance getMeanElement() {
        Instance mean = new Instance(this.dimension);
        int num_entities = this.instances.size();
        // Calculate the mean values of each component, storing them in a string
        for( int i = 0 ; i < entities_sum.length ; i++ ){
        	double it = entities_sum[i];
            double curr_component_mean = it / (num_entities*1.0);
            mean.setValue((int)i, curr_component_mean);
        }
        return mean;
    }
    

	/** Verify whether this hypercube is neighbor of any of a list of hypercubes.
	 *
	 *  @param hypercube_keys Keys of hypercubes supposed to be neighbors.
	 *  @param cubes Container of hypercubes.
	 *
	 * @return True, if any of the received list is a neighbor. False, otherwise.
	 *
	 * */
	boolean isNeighbor( List<String> hypercube_keys , Map<String,HyperCube> cubes) {

	    boolean is_neighbor = false;
	    for( String it : hypercube_keys ){
	        // Search for current received key in the list of neighbors
	    	int position = this.neighbors.indexOf(it);
	    	String wanted_key = null;
	    	if ( position != -1)
	    		wanted_key = this.neighbors.get(position);
	        boolean found_it = (position != this.neighbors.size() - 1 ) && position != -1;

	        // More restrict neighborhood criterion: distance between means MUST be
	        // less or equal to 2*edge_length
	        if( found_it ){
	        	// Calculate distance between means of hypercubes
	            Instance this_mean = getMeanElement();
	            HyperCube neighbor_cube = cubes.get(wanted_key);
	            Instance neighbor_mean = neighbor_cube.getMeanElement();

	            Instance difference = new Instance(this_mean.numAttributes());
	            for (int i = 0; i < difference.numAttributes(); i++) 
	            	difference.setValue(i, this_mean.value(i) - neighbor_mean.value(i));
	            
	            double distance = DenclueFunctions.getEuclideanNorm(difference);

	            // Verify whether distance satisfies minimum bound
	            is_neighbor = ( distance <= (2*this.edge_length) );

	            if( is_neighbor ) break;
	        }
	    }
	    return is_neighbor;
	}
	
	/** Remove keys of neighbors that are empty neighbors.
	 *
	 *  @param empty_neighbors: Vector with keys of empty neighbors
	 *
	 * */
	void removeEmptyNeighbors( List<String> empty_neighbors ){
	    // Search for any of this hypercube's neighbors in the empty neighbors' list
	    for( String it : empty_neighbors ){

	        // Search for current neighbor in empties' list
	    	int wanted_key = empty_neighbors.indexOf(it) ;
	        if( wanted_key != empty_neighbors.size()-1 ){  // Curr neighbor is and empty neighbor

	            this.neighbors.remove(it);  // delete empty neihgbor
	        }
	    }
	}
	/** Create a string representation of a hypercube identifier from an
	 * array.
	 *
	 *  @param array_key Array containing the upper bounds of a hypercube.
	 *  @param dimension The number of dimensions in the hypercube.
	 *  @param edge_length size of each uni-dimensional edge
	 *
	 * @return the string representation of the key
	 *
	 * */
	public static String getKeyFromArray( double[] upper_bounds, int dimension ) {

	    StringBuffer str_buf = new StringBuffer();

	    for(int i=0 ; i < dimension ; i++){
	        double curr_index =  upper_bounds[i] ;
	        if( i != 0 )  
	        	str_buf.append(",");
	        str_buf.append(curr_index);
	    }

	    return str_buf.toString();

	}

	/** Create an array representation of a hypercube identifier from a
	 * string.
	 *
	 *  @param str_key String containing the key of a hypercube.
	 *  @param dimension The number of dimensions in the hypercube.
	 *
	 * @return the upper bounds of the hypercube represented by the key.
	 *  This array have 'dimension' elements
	 *
	 * */
	public static double[] getArrayFromKey( String str_key, int dimension ){
		
		String[] v = str_key.split(",");
	    double[] upp_bounds = new double[dimension];
	    for (int i = 0; i < upp_bounds.length; i++) {
	    	upp_bounds[i] = Double.parseDouble(v[i]);
		}

	    return upp_bounds;

	}
	
	public static List<Instance> getArrayFromKey( String[] str_key, int dimension ){

		List<Instance> attractorsList = new ArrayList<Instance>();
		for ( String m : str_key ) {
			String[] v = m.split(",");
			Instance upp_bounds = new Instance(dimension);
			for (int i = 0; i < dimension; i++) 
				upp_bounds.setValue(i,Double.parseDouble(v[i]));
			attractorsList.add(upp_bounds);
		}
	    return attractorsList;

	}
	
	
	
	public List<String> getNeighbors() {
		return neighbors;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for ( int i = 0 ; i < instances.size() ; i++ )
			sb.append(instances.get(i).toString()).append("\n");
		return sb.toString();
	}
}
