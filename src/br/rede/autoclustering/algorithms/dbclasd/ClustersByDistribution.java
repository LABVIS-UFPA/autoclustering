package br.rede.autoclustering.algorithms.dbclasd;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.HyperCube;
import br.rede.autoclustering.structures.grid.HyperSpace;
import br.rede.autoclustering.structures.groups.Cluster;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.ChiSquare;
import br.rede.autoclustering.util.Distance;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.PoissonProcess;
import br.rede.autoclustering.util.SortedList;

	
//@author Samuel Félix

public class ClustersByDistribution implements ClusteringMethod {
	
	@Override
	public void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			
			double[] lowerBounds = (double[]) sharedObjects.get(Parameter.ALL_LOWER_BOUNDS);
			List<Group> groups = null;
			if ( sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST) != null )
				groups = (List<Group>) sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST);
			else if ( sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null )
				groups = (List<Group>) sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST);
			else if ( sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null )
				groups = (List<Group>) sharedObjects.get(Parameter.SNN_GROUPS_FIRST);
			else
				groups = extractGroupsFromDenclue((HyperSpace) sharedObjects.get(Parameter.DENCLUE_HYPER_SPACE));
			List<Group> result = start(groups, lowerBounds, instances, distance);
			sharedObjects.put(Parameter.ALL_GROUPS, result);
		}
	}
	
	private List<Group> extractGroupsFromDenclue(HyperSpace hyperSpace) {
		List<Group> groups = new ArrayList<Group>();
		Collection<HyperCube> hyperCubes = hyperSpace.getHypercubes().values();
		for ( HyperCube hyperCube : hyperCubes ){
			Group group = new Group();
			group.addAllInstance(hyperCube.getInstances());
		}
		return groups;
	}

	public List<Group> start(List<Group> groups, double[] lowerBounds, Instances instances, DistanceType distance){
		Map<Instance, Group> instToClusters = new HashMap<Instance,Group>();
		Map<Group, Map<String, Integer>> clusters = new HashMap<Group, Map<String,Integer>>();
		Map<Instance,SortedList<Distance<Instance>>> distances = calculateDistance(instances, distance);
		List<Instance> candidates = new ArrayList<Instance>();
		Set<Instance> processedPoints = new HashSet<Instance>();
		
		for (int i = 0; i < groups.size(); i++) {
			Group c = groups.get(i);
			for ( Instance key : c.getInstances()) {
				if ( instToClusters.get(key) == null )
					instToClusters.put(key, c);
			}
			clusters.put(c, new HashMap<String,Integer>());
			double volume = calculateClusterVolume(c, lowerBounds,clusters.get(c), distances);
			double radius = calculateQueryRadius(c, volume);
			c.setDensity(volume);
			for (Instance p1 : c.getInstances()) 
				updateCandidades(c, candidates, retrieveNeighbors(radius, p1, distances.get(p1)), processedPoints);
			//	If there are no candidates, this is not a cluster, but a noise, remove it!
			//	This has not been said in the paper, however, I assume this value based on the results
			if ( candidates.isEmpty() ) {
				for ( Instance a : c.getInstances())
					processedPoints.remove(a);
				clusters.remove(c);
				break;
			}
			expandCluster(c, lowerBounds, candidates, clusters, processedPoints, instToClusters, distances);
		}
		
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance p = instances.instance(i);
			if (instToClusters.get(p) == null) {
				Group c = expandByNPoints(p, lowerBounds, clusters, 3, candidates, processedPoints, instToClusters, distances);
				if ( c != null )
					expandCluster(c, lowerBounds, candidates, clusters, processedPoints, instToClusters, distances);
			}
		}
		
		List<Group> result = new ArrayList<Group>();
		result.addAll(clusters.keySet());
		return result;
	}
	
	public static Group expandByNPoints(Instance key, double[] lowerBounds, Map<Group, Map<String, Integer>> clusters, int pts, 
			List<Instance> candidates, Set<Instance> processedPoints, Map<Instance, Group> instToClusters, Map<Instance, SortedList<Distance<Instance>>> distances) {
		//create a new cluster C and insert p into C;
		Group c = new Group();
		c.addInstance(key);
		instToClusters.put(key, c);
		clusters.put(c, new HashMap<String,Integer>());
		//Retrieve position of the 
		int count = 0;
		for (Distance<Instance> dai : distances.get(key)){
			if ( dai.getDistanceToInstance() != 0 && instToClusters.get(dai.getTheOtherOne(key)) == null) {
				Instance theOtherOne = dai.getTheOtherOne(key) ;
				instToClusters.put(theOtherOne, c);
				c.addInstance(theOtherOne);
				if ( count == pts )
					break;
				else
					count++;
			}
		}	
		double volume = calculateClusterVolume(c, lowerBounds,clusters.get(c), distances);
		double radius = calculateQueryRadius(c, volume);
		c.setDensity(volume);
		for (Instance p1 : c.getInstances()) 
			updateCandidades(c, candidates, retrieveNeighbors(radius, p1, distances.get(p1)), processedPoints);
		//If there are no candidates, this is not a cluster, but a noise, remove it!
		//This has not been said in the paper, however, I assume this value based on the results
		if ( candidates.isEmpty() ) {
			for ( Instance a : c.getInstances())
				processedPoints.remove(a);
			clusters.remove(c);
			return null;
		}
		return c;
	}
	
	/**
	 * @param distances 
	 * @param db 
	 * 
	 */
	private Map<Instance, SortedList<Distance<Instance>>> calculateDistance(Instances db, DistanceType distance) {
		Map<Instance, SortedList<Distance<Instance>>> distances = new HashMap<Instance, SortedList<Distance<Instance>>>();
		for (int i = 0; i < db.numInstances(); i++) {
			for (int j = i + 1; j < db.numInstances(); j++) {
				double dist = DistanceMeasures.getInstance().calculateDistance(db.instance(j), db.instance(i), distance);
				if ( dist != 0 ) {
					Distance<Instance> amongInstances = new Distance<Instance>();
					amongInstances.setInstance1(db.instance(i));
					amongInstances.setInstance2(db.instance(j));
					amongInstances.setDistanceToInstance(dist);
					SortedList<Distance<Instance>> dis1 = distances.get(db.instance(i));
					SortedList<Distance<Instance>> dis2 = distances.get(db.instance(j));
					if ( dis1 == null ) {
						dis1 = new SortedList<Distance<Instance>>();
						distances.put(db.instance(i), dis1);
					}
					if ( dis2 == null ) {
						dis2 = new SortedList<Distance<Instance>>();
						distances.put(db.instance(j), dis2);
					}	
					dis1.add(amongInstances);
					dis2.add(amongInstances);
				}
			}
		}
		return distances;
	}
	
	private void findCandidate(Group c, double[] lowerBounds, Map<Cluster, 
			Map<String, Integer>> clusters, Map<Instance,SortedList<Distance<Instance>>> distances, List<Instance> candidates, Set<Instance> processedPoints){
		double volume = calculateClusterVolume(c, lowerBounds,clusters.get(c), distances);
		double radius = calculateQueryRadius(c, volume);
		c.setDensity(volume);
		for (Instance p1 : c.getInstances()) 
			updateCandidades(c, candidates, retrieveNeighbors(radius, p1, distances.get(p1)), processedPoints);
		//If there are no candidates, this is not a cluster, but a noise, remove it!
		//This has not been said in the paper, however, I assume this value based on the results
		if ( candidates.isEmpty() ) {
			for ( Instance a : c.getInstances())
				processedPoints.remove(a);
			clusters.remove(c);
//			return null;
		}
	}
	
	public static Group expandCluster(Group c, double[] lowerBounds, List<Instance> candidates, 
			Map<Group,Map<String, Integer>> clusters, Set<Instance> processedPoints,
			Map<Instance, Group> instToClusters, Map<Instance, SortedList<Distance<Instance>>> distances) {
		//We store the size of the Cluster before the expansion
		int sizeBefore = c.getInstances().size();
		boolean change = true;
		while(change) {
			change = false;
			while (!candidates.isEmpty()) {
				Instance instance = candidates.get(0);
				candidates.remove(instance);
				Group previous = instToClusters.get(instance);
				instToClusters.put(instance,c);
				c.addInstance(instance);
				if (validateProbabilityDistribution(c, distances)){
					double clusterVolume = calculateClusterVolume(c, lowerBounds,clusters.get(c), distances);
					double radius = calculateQueryRadius(c, clusterVolume);
					c.setDensity(clusterVolume);
					List<Instance> answer = retrieveNeighbors(radius, instance, distances.get(instance));
					updateCandidades(c, candidates, answer, processedPoints);
					change = true;
				}else{
					c.getInstances().remove(instance);
					instToClusters.put(instance,previous);
				}
			}
		}
		//We store the size of the Cluster after the expansion
		int sizeAfter = c.getInstances().size();
		if ( sizeAfter == sizeBefore ) {
			for ( Instance a : c.getInstances())
				processedPoints.remove(a);
			clusters.remove(c);
			return null;
		}
		Group g = new Group();
		g.getInstances().addAll(c.getInstances());
		return g;
	}
	/**
	 * Determine Radius for circle query
	 * @param c
	 * @return
	 */
	private static double calculateQueryRadius(Group c, double volumeCluster){
		int n = c.getInstances().size();
		double result = Math.sqrt(volumeCluster/Math.PI * (1 - Math.pow(1.0/n, 1.0/n) ));
		return result;
	}
	
	/**
	 * @param c
	 * @param answer
	 */
	private static void updateCandidades(Group c, List<Instance> candidates, List<Instance> answer, Set<Instance> processedPoints) {
		for ( Instance i : answer ) {
			if ( !c.getInstances().contains(i) && !processedPoints.contains(i) ){
				processedPoints.add( i);
				candidates.add( i);
			}
		}
	}
	
	/**
	 * We set the grid-lenght as the maximum element of the NNDistSet(S)
	 * This method aims to retrieve the grid length
	 * @param c
	 * @param distances 
	 * @return
	 */
	private static double retrieveGridLength(Group c, double[] lower_bounds, Map<String, Integer> cube, Map<Instance, SortedList<Distance<Instance>>> distances) {
		double gridLength = 0;
		for ( Instance i : c.getInstances() ){
			Distance<Instance> d = distances.get(i).first();
			if ( d.getDistanceToInstance() > gridLength ) 
				gridLength = d.getDistanceToInstance();
		}
		for ( Instance i : c.getInstances() ){
			String name = findInterval(i, lower_bounds, gridLength);
			Integer value = cube.get(name);
			cube.put(name, value == null ? 1 : value + 1 );
		}
		return gridLength;
	}


	/**
	 * Find the grid interval of the instance
	 *  
	 * @param i
	 * @return
	 */
	private static String findInterval(Instance i, double[] lower_bounds, double gridLength){
		StringBuffer interval = new StringBuffer(i.numAttributes());
		for (int j = 0; j < i.numAttributes(); j++) {
			if ( j != 0 )
				interval.append(",");
			double d = (((int) ( (i.value(j) - 0.0001 - lower_bounds[j] ) / gridLength ) ) * 
					gridLength + lower_bounds[j]);
			interval.append(d);
		}
		return interval.toString();
	}

	/**
	 * 
	 * The volume of the cluster is the Σ gridLength^numOfDimension
	 * @param c
	 * @param distances 
	 * @return Volume of the cluster
	 */
	public static double calculateClusterVolume(Group c, double[] lower_bounds,Map<String, Integer> clusters, Map<Instance, SortedList<Distance<Instance>>> distances){
//		Retrieve max(NNDist) for grid length
		double gridLength = retrieveGridLength(c, lower_bounds, clusters, distances);
		return Math.pow(gridLength, lower_bounds.length)*clusters.size();
	}
	
	
	/**
	 * TODO corrigir distancias, ta pegando o primeiro de novo :P
	 * @param c
	 * @param distances 
	 */
	public static boolean validateProbabilityDistribution(Group c, Map<Instance, SortedList<Distance<Instance>>> distances){
		double volumeCluster = c.getDensity();
		Map<Instance,Distance<Instance>> nearestNeighborsDistances = retrieveNearestNeighborsDistances(c, distances);
		int i = 0, dim = c.getInstances().iterator().next().numAttributes();
		SortedSet<Double> si = new TreeSet<Double>(); 
		for ( Distance<Instance> d : nearestNeighborsDistances.values() )
			si.add(d.getDistanceToInstance());
		double[] probabilityDistribution = new double[si.size()];
		double[] poissomDistribution = new double[si.size()];
		for ( Double d : si ){
			double hyperSphereVolume = calculateNSphereVolume(d, dim);
			probabilityDistribution[i] = 1 - Math.pow( (1 - hyperSphereVolume / volumeCluster ), c.getInstances().size());
			poissomDistribution[i++] = PoissonProcess.calculatePoissonProbabilityDistribution(hyperSphereVolume/volumeCluster, d == 0 ? 0 : 2);
		}
		return ChiSquare.chiSquareTest(probabilityDistribution,poissomDistribution, dim-1, 0.9);
	}
	

	/**
	 * @param c
	 * @param distances 
	 * @return
	 */
	public static Map<Instance,Distance<Instance>> retrieveNearestNeighborsDistances(Group c, Map<Instance, SortedList<Distance<Instance>>> distances){
		Map<Instance,Distance<Instance>> nnDistSet =  new HashMap<Instance,Distance<Instance>>();
		for ( Instance dbI : c.getInstances() ) {
			Distance<Instance> d = distances.get(dbI).first();
//			distances.put(dbI, d);
		}
		return nnDistSet;
	}
	
	/**
	 * Recursive method for calculating a n sphere volume
	 * @param radius
	 * @param n
	 * @return
	 */
	public static double calculateNSphereVolume(double radius, int n){
		//If it is a point
		if ( n == 0 )
			return 1;
		//If it is a line
		else if ( n == 1 )
			return 2*radius;
		return ((2*Math.PI*Math.pow(radius, 2))/n) * calculateNSphereVolume(radius, n-2);
	}
	/**
	 * 
	 * 
	 * @param c
	 * @param instance
	 * @return
	 */
	private static List<Instance> retrieveNeighbors(double radius, Instance instance, SortedList<Distance<Instance>> distances) {
		
		List<Instance> neighbors = new ArrayList<Instance>();
		for ( Distance<Instance> dbi : distances) {
			//lembrar do problema que quando tem um cara igual a ele
			if (dbi.getDistanceToInstance() <= radius)
				neighbors.add(dbi.getTheOtherOne(instance));
			else 
				break;
		}
		return neighbors;
	}

	@Override
	public String getName() {
		return "ClustersByDistribution";
	}
	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( 
				( sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST) != null ||
				  sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null ||
				  sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null ||
				  sharedObjects.get(Parameter.DENCLUE_HYPER_SPACE) != null	) &&
				  sharedObjects.get(Parameter.ALL_DISTANCE) != null &&
				  sharedObjects.get(Parameter.ALL_INSTANCES) != null && 
				  sharedObjects.get(Parameter.ALL_LOWER_BOUNDS) != null)
			return true;
		return false;
	}
	@Override
	public String technicalInformation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
