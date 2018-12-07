package br.rede.autoclustering.algorithms.descry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Instance;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.structures.tree.DistanceNode;
import br.rede.autoclustering.structures.tree.Node;
import br.rede.autoclustering.structures.tree.Tree;
import br.rede.autoclustering.util.Distance;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.SortedList;

/**
 * 
 * @author Samuel FÃ©lix
 * 
 */
public class MergeByDistance implements ClusteringMethod {

	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if (isReady(sharedObjects)) {
			if (	sharedObjects.get(Parameter.AMR_TREE) != null && 
					sharedObjects.get(Parameter.AMR_LAMBDA) != null && 
					sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
					sharedObjects.get(Parameter.DESCRY_K) != null)
				startFromAMR(sharedObjects);
			else if (
					sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null && 
					sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
					sharedObjects.get(Parameter.DESCRY_K) != null)
				startFromDBScan(sharedObjects);
			else if (
					sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null && 
					sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
					sharedObjects.get(Parameter.DESCRY_K) != null)
				//System.out.println("Tamanho do grupo: "+sharedObjects.get(Parameter.SNN_GROUPS_FIRST));
				startFromSNN(sharedObjects);
			else if (
					sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
					sharedObjects.get(Parameter.DESCRY_TREE) != null && 
					sharedObjects.get(Parameter.DESCRY_K) != null)
				startFromDescry(sharedObjects);
			else if (
					sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
					sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST) != null && 
					sharedObjects.get(Parameter.DESCRY_K) != null
			)
				startFromDBClasd(sharedObjects);
		}
	}

	private void start(Map<Parameter, Object> sharedObjects, List<Group> groups, DistanceType type, int k){
		List<DistanceNode> nodes = createNodes(groups, type);
		Collections.sort(nodes);
		
		Map<Integer, List<DistanceNode>> result = new HashMap<Integer,List<DistanceNode>>();
		for (int i = 0; i < nodes.size(); i++) {
			DistanceNode node = nodes.get(i);
			List<DistanceNode> newList = new ArrayList<DistanceNode>();
			newList.add(node);
			node.setNeighborsOfCluster(i);
			result.put(i, newList );
		}
		int a = 0;
		while ( result.size() > k ) {
			merge( nodes, result ,a++);
		}
		
		List<Group> finalGroups = new ArrayList<Group>();
		for ( List<DistanceNode> dns : result.values()) {
			Group group = new Group();
			for ( DistanceNode dn : dns )
				group.getInstances().addAll(dn.getGroup().getInstances());
			finalGroups.add(group);
		}
		
		sharedObjects.put(Parameter.ALL_GROUPS, finalGroups);
//		new ClusterViewerFrame(finalGroups);
	}
	
	private void startFromDBScan(Map<Parameter, Object> sharedObjects) {
		List<Group> groups = (List<Group>) sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST);
		DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();
		start(sharedObjects, groups, distance, k);
	}
	
	private void startFromSNN(Map<Parameter, Object> sharedObjects) {
		List<Group> groups = (List<Group>) sharedObjects.get(Parameter.SNN_GROUPS_FIRST);
		//System.out.println("Tamanho do grupo: "+groups.size());
		DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();
		start(sharedObjects, groups, distance, k);
	}
	
	private void startFromDBClasd(Map<Parameter, Object> sharedObjects) {
		List<Group> groups = (List<Group>) sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST);
		DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();
		start(sharedObjects, groups, distance, k);
	}


	private void startFromAMR(Map<Parameter, Object> sharedObjects) {
		Tree tree = (Tree) sharedObjects.get(Parameter.AMR_TREE);
		int lambda = ((Number) sharedObjects.get(Parameter.AMR_LAMBDA)).intValue();
		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();
		DistanceType type = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
		List<Group> groups = createGroupsFromAMR(tree.getRoot(), lambda, 0);
		start(sharedObjects, groups, type, k);
	}
	
	private List<Group> createGroupsFromAMR(Node node, int lambda, int level) {
		if ( level == lambda || node.getChildren().isEmpty()){
			List<Group> groups = new ArrayList<Group>();
			Group group = new Group();
			group.getInstances().addAll(node.getInstances());
			groups.add(group);
			return groups;
		}else {
			List<Group> groups = new ArrayList<Group>();
			for (Node child : node.getChildren())
				groups.addAll(createGroupsFromAMR(child, lambda, level+1));
			return groups;
		}
	}

	private void startFromDescry(Map<Parameter, Object> sharedObjects) {
		Tree tree = (Tree) sharedObjects.get(Parameter.DESCRY_TREE);
		DistanceType type = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();
		
		List<Group> groups = createGroupsFromDescry(tree.getRoot());
		start(sharedObjects, groups, type, k);
	}

	/**
	 * 
	 * @param nodes
	 * @param result
	 */
	private void merge(List<DistanceNode> nodes, Map<Integer,List<DistanceNode>> result, int a) {
		DistanceNode first = nodes.get(0);
		DistanceNode closestToFirst = first.getClosestNeighbor();
		int targetCluster = first.getNeighborsOfCluster();
		int previousCluster = closestToFirst.getNeighborsOfCluster();
		List<DistanceNode> target = result.get(targetCluster);
		List<DistanceNode> previous = result.get(previousCluster);
		//First cluster removes all members of the second cluster from its list
		for (DistanceNode targetMember : target) {
			for (DistanceNode previousMember : previous) {
				targetMember.pop(previousMember);
				previousMember.pop(targetMember);
				previousMember.setNeighborsOfCluster(targetCluster);
			}
		}
		result.get(targetCluster).addAll(previous);
		result.remove(previousCluster);
		Collections.sort(nodes);
	}

	
	/**
	 * Create {@link DistanceNode}s. First, It finds the {@link Group}s of the k-level from the {@link Tree}. 
	 * Then, It finds the centroids of each one of those groups.
	 * After that, It creates a {@link SortedList} with the closest neighbors of the groups. (Ascendent)
	 * 
	 * @param tree	The free from {@link AdaptableKDTree}
	 * @param type	The type of {@link DistanceType}
	 * @return {@link List}&lt;{@link DistanceNode}&gt;	The sorted groups (From the first groups to be sorted to last ones).
	 */
	private List<DistanceNode> createNodes(List<Group> groups, DistanceType type) {
		List<DistanceNode> nodes = findCentroids(groups);
		for (int i = 0; i < nodes.size(); i++) {
			for (int j = i+1; j < nodes.size(); j++) {
				double value = DistanceMeasures.getInstance().calculateDistance(nodes.get(i).getCentroid(), nodes.get(j).getCentroid(), type);
				Distance<DistanceNode> distance = new Distance<DistanceNode>();
				distance.setDistanceToInstance(value);
				distance.setInstance1(nodes.get(i));
				distance.setInstance2(nodes.get(j));
				nodes.get(i).addNode(distance);
				nodes.get(j).addNode(distance);
			}
		}
		return nodes;
	}

	/**
	 * It finds the {@link Group}s of the k-level from the {@link Tree}.
	 * @param node
	 * @return
	 */
	private List<Group> createGroupsFromDescry(Node node) {
		if ( node == null )
			return null;
		List<Group> groups = new ArrayList<Group>();
		for ( Node child : node.getChildren() ) {
			List<Group> childsGroup = createGroupsFromDescry(child);
			if ( childsGroup != null )
				groups.addAll(childsGroup);
			else {
				Group group = new Group();
				group.getInstances().addAll(node.getInstances());
				groups.add(group);
				break;
			}
		}
		return groups;
	}

	private List<DistanceNode> findCentroids(List<Group> groups) {
		List<DistanceNode> nodes = new ArrayList<DistanceNode>(groups.size());
		for (int i = 0; i < groups.size(); i++) {
			Group group = groups.get(i);
			Instance centroid = findCentroid(group.getInstances());
			if (centroid == null) {
				centroid = group.getKey();
			}
			DistanceNode node = new DistanceNode();
			node.setCentroid(centroid);
			node.setGroup(group);
			nodes.add(node);
		}
		return nodes;
	}

	private Instance findCentroid(Set<Instance> instances) {
		/*Este método estava dando erro ao buscar a próxima linha(java.util.NoSuchElementException:).
		Assim, foi colocado uma condição de teste para não dar crash e o retorno é tratado para que
		pegue a instância relacionada ao grupo.*/
		if (instances.iterator().hasNext()) {
			int numAttributes = instances.iterator().next().numAttributes();
			Instance centroid = new Instance(numAttributes);

			for (int j = 0; j < numAttributes; j++) {
				float value = 0;
				for (Instance i : instances)
					value += i.value(j);
				centroid.setValue(j, value / instances.size());
			}

			return centroid;
		}else {
			return null;
		}
		
		//int numAttributes = instances.iterator().next().numAttributes();
		/*System.out.println("Número de atributos: "+numAttributes);
		Instance centroid = new Instance(numAttributes);

		for (int j = 0; j < numAttributes; j++) {
			float value = 0;
			for (Instance i : instances)
				value += i.value(j);
			centroid.setValue(j, value / instances.size());
		}

		return centroid;*/
	}

	@Override
	public String getName() {
		return "MergeByDistance";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if (	(sharedObjects.get(Parameter.AMR_TREE) != null && 
				sharedObjects.get(Parameter.AMR_LAMBDA) != null && 
				sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
				sharedObjects.get(Parameter.DESCRY_K) != null)
				||
				
				(sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null && 
				sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
				sharedObjects.get(Parameter.DESCRY_K) != null)			
				
				||
				(sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST) != null && 
				sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
				sharedObjects.get(Parameter.DESCRY_K) != null)			
						
				||
				
				(sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null && 
				sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
				sharedObjects.get(Parameter.DESCRY_K) != null)			
				
				||
				(sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
				sharedObjects.get(Parameter.DESCRY_TREE) != null && 
				sharedObjects.get(Parameter.DESCRY_K) != null)
			)
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return "This method aims to join clusters using users' preferences";
	}

}
