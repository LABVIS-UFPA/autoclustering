package br.rede.autoclustering.algorithms.dbscan;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.HyperCube;
import br.rede.autoclustering.structures.grid.HyperSpace;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;

public class ClustersByConnectiveness implements ClusteringMethod{
	
	/**
	 * Parametro que controla a tolerancia da reducao de densidade dos grupos
	 */

	
	/**
	 * O novo grupo e formado apartir de dois grupos que possuem uma distancia
	 * aceitavel, de acordo com o limiar estabelecido
	 * 
	 * @param groups
	 * @param group1
	 * @param group2
	 * @param distanceType 
	 * @return
	 */
	private void connectGroups(int group, double limiar, List<Group> groups, List<Boolean> visited,DistanceType distanceType) {
		visited.remove(group);
		visited.add(group, true);
		Group group1 = groups.get(group);
		for (int i = 0; i < visited.size(); i++) {
			Group group2 = groups.get(i);
			if ( group1 != group2 && !visited.get(i)) {
				if ( pathBetweenExists(group1, group2, limiar, distanceType)){
					connectGroups(i, limiar, groups, visited, distanceType);
					group1.getInstances().addAll(group2.getInstances());
					int secondPosition = groups.indexOf(group2);
					groups.remove(secondPosition);
					visited.remove(secondPosition);
					
					
				}
			}
		}

	}
	
	private boolean pathBetweenExists(Group group1, Group group2, double limiar, DistanceType distanceType) {
		List<Instance> list1 = new ArrayList<Instance>();
		List<Instance> list2 = new ArrayList<Instance>();
		list1.addAll(group1.getInstances());
		list2.addAll(group2.getInstances());
		for (int j = 0; j < list1.size(); j++) {
			Instance i = list1.get(j); 			
			for (int k = j+1; k < list2.size(); k++) {
				Instance m = list2.get(k);
				double distance = DistanceMeasures.getInstance().calculateDistance(i,m, distanceType);
				if(distance <= limiar){
					return true;
				}
			}
		}
		return false;
	}

//	private void merge(int a, Attractor[] att, SortedMap<Attractor, SortedList<Instance>> clusters, boolean[] visited, double sigma, DistanceType distance) {
//		visited[a] = true;
//		for (int i = 0; i < visited.length; i++) {
//			if ( att[i] != att[a] && !visited[i] ) {
//				if (DenclueFunctions.pathBetweenExists(att[a],att[i], sigma, clusters, distance)) {
//					merge(i, att, clusters, visited, sigma, distance);
//					att[a].getAttracted().add(att[i]);
//					att[a].getAttracted().addAll(att[i].getAttracted());
//					clusters.get(att[a]).addAll(clusters.get(att[i]));
//					clusters.remove(att[i]);
//				}
//			}
//		}
//	}
	
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			float limiar = ((Number) sharedObjects.get(Parameter.DBSCAN_MAX_DIST)).floatValue();
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			if ( distance == null )
				distance = DistanceType.EUCLIDEAN;
			
			List<Group> groups = (List<Group>) sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST);
			if ( groups == null )
				groups = (List<Group>) sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST);
			
			if (groups == null)
				groups = (List<Group>) sharedObjects.get(Parameter.SNN_GROUPS_FIRST);

			if ( groups != null )
				executeFromCandidatesByNPts(limiar, groups, distance, sharedObjects);
		
			HyperSpace spatialRegion = (HyperSpace) sharedObjects.get(Parameter.DENCLUE_HYPER_SPACE);
			if ( spatialRegion != null )
				executeFromASH(limiar, spatialRegion, distance, sharedObjects);
		}
	}

	private void executeFromASH(double limiar, HyperSpace spatialRegion, DistanceType distance,Map<Parameter, Object> sharedObjects) {
		List<Group> groups = new ArrayList<Group>();
		for (HyperCube hc : spatialRegion.getHypercubes().values()){
			Group group = new Group();
			group.getInstances().addAll(hc.getInstances());
			groups.add(group);
		}
		executeFromCandidatesByNPts(limiar, groups, distance,sharedObjects);
	}

	private void executeFromCandidatesByNPts(double limiar, List<Group> groups, DistanceType distance, Map<Parameter, Object> sharedObjects) {
		List<Boolean> visited = new ArrayList<Boolean>(groups.size());
		for (int i = 0; i < groups.size(); i++) 
			visited.add(false);

		for (int i = 0; i < groups.size(); i++) {
			if ( !groups.get(i).getInstances().isEmpty() ) {
				connectGroups(i , limiar, groups, visited, distance);
			}
		}
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
	}

	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( (sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST) != null || 
			  sharedObjects.get(Parameter.DENCLUE_HYPER_SPACE) != null ||
			  sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null ||
			  sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null )  &&
			  sharedObjects.get(Parameter.DBSCAN_MAX_DIST) != null )
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return "O algoritmo busca conectar todos os grupos possiveis com um limiar para a distancia entre os grupos";
	}

	@Override
	public String getName() {
		return "ClusterByConnectiveness";
	}
}
