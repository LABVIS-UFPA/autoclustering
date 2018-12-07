package br.rede.autoclustering.algorithms.denclue;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import weka.core.Instance;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.HyperSpace;
import br.rede.autoclustering.structures.groups.Attractor;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.structures.groups.Subspace;
import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.SortedList;

public class ClustersByAttractors implements ClusteringMethod{

	private List<Group> findConnections(SortedMap<Attractor, SortedList<Instance>> clusters, double sigma, DistanceType distance){
		int k = 0;
		Attractor att[] = new Attractor[clusters.keySet().size()];
		for ( Attractor a : clusters.keySet() )
			att[k++] = a;
		boolean[] visited = new boolean[att.length];
		Arrays.fill(visited, false);
		for (int i = 0; i < visited.length; i++) 
			merge(i, att, clusters, visited, sigma, distance);
		return getGroup(clusters);
	}
	
	private synchronized List<Group> getGroup(SortedMap<Attractor, SortedList<Instance>> clusters){
		List<Group> listOfGroups = new ArrayList<Group>();
		for ( Attractor key : clusters.keySet() ){
			Group g = new Group();
			g.getInstances().addAll(clusters.get(key));
			listOfGroups.add(g);
		}
		return listOfGroups;
	}
	
	private void merge(int a, Attractor[] att, SortedMap<Attractor, SortedList<Instance>> clusters, boolean[] visited, double sigma, DistanceType distance) {
		visited[a] = true;
		for (int i = 0; i < visited.length; i++) {
			if ( att[i] != att[a] && !visited[i] ) {
				if (DenclueFunctions.pathBetweenExists(att[a],att[i], sigma, clusters, distance)) {
					merge(i, att, clusters, visited, sigma, distance);
					att[a].getAttracted().add(att[i]);
					att[a].getAttracted().addAll(att[i].getAttracted());
					clusters.get(att[a]).addAll(clusters.get(att[i]));
					clusters.remove(att[i]);
				}
			}
		}
	}

	@Override
	public void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			float sigma = ((Number) sharedObjects.get(Parameter.DENCLUE_SIGMA)).floatValue();
			float epsilon = ((Number) sharedObjects.get(Parameter.DENCLUE_EPSILON)).floatValue();
			Subspace[] subspaceRegion = (Subspace[]) sharedObjects.get(Parameter.CLIQUE_SUBSPACES_BEGIN);
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			
			if ( distance == null )
				distance = DistanceType.EUCLIDEAN;
			if ( subspaceRegion != null ) 
				executeFromDenseAreas(subspaceRegion[subspaceRegion.length-1].getEntities(), sigma, epsilon, distance, sharedObjects);
			HyperSpace spatialRegion = (HyperSpace) sharedObjects.get(Parameter.DENCLUE_HYPER_SPACE);
			if ( spatialRegion != null ) {
				executeFromASH(spatialRegion.getEntities(), sigma, epsilon, distance, sharedObjects);
			}
		}
	}

	private void executeFromDenseAreas(List<Instance> list, double sigma, double epsilon, DistanceType distance, Map<Parameter, Object> sharedObjects) {
		//Only for integration with denclue
		List<Instance> entities = new ArrayList<Instance>(list);
		for (Instance i : entities)
			i.setWeight(DenclueFunctions.calculateDensity(i, entities, sigma, distance));
		executeFromASH(entities, sigma, epsilon, distance, sharedObjects);
	}

	private synchronized void executeFromASH(List<Instance> spatialInstances, double sigma, double epsilon, DistanceType distance, Map<Parameter, Object> sharedObjects) {
		Comparator<Instance> comparator = new  Comparator<Instance>() {
			@Override
			public int compare(Instance o1, Instance o2) {
				if (o1.weight() < o2.weight())
					return -1;
				else if (o1.weight() > o2.weight())
					return 1;
				return 0;
			}
		};
		/* Determine density attractors and entities attracted by each of them */
		SortedMap<Attractor, SortedList<Instance>> clusters = new TreeMap<Attractor, SortedList<Instance>>(); // Map
		for (int i = 0; i < spatialInstances.size(); i++) {
			Instance iter_entities = spatialInstances.get(i);
			if (iter_entities.weight() < epsilon) {
				spatialInstances.remove(iter_entities);
				continue;
			}
			Attractor curr_attractor = DenclueFunctions.hillClimbling( iter_entities, spatialInstances, sigma, epsilon, distance);
			// Ignores density-attractors that don't satisfy minimum density restriction
			if (curr_attractor.getDensity() < epsilon )
				continue;
			// Create a new cluster if necessary
			if (clusters.get(curr_attractor) == null)
				clusters.put(curr_attractor, new SortedList<Instance>(comparator));
			// Assign current entity to the cluster represented by its density-attractor
			clusters.get(curr_attractor).add(iter_entities);
		}
		
		List<Group> groups = findConnections(clusters, sigma, distance);
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
		
	}

	/** Determine density attractors and entities attracted by each of them */
	@Override
	public String technicalInformation() {
		return "Determine density attractors and entities attracted by each of them";
	}

	@Override
	public String getName() {
		return "ClustersByAttractors";
	}
	
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( ( sharedObjects.get(Parameter.DENCLUE_HYPER_SPACE) != null || 
			(sharedObjects.get(Parameter.CLIQUE_SUBSPACES_BEGIN) != null)) &&
			(sharedObjects.get(Parameter.DENCLUE_SIGMA) != null) &&
			(sharedObjects.get(Parameter.DENCLUE_EPSILON) != null)
		)
			return true;
		return false;
	}
}
