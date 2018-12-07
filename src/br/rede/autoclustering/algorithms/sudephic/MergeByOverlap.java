package br.rede.autoclustering.algorithms.sudephic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.Grid;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.structures.tree.Cell;
import br.rede.autoclustering.structures.tree.Node;
import br.rede.autoclustering.structures.tree.Tree;
import br.rede.autoclustering.util.Distance;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.SortedList;

/**
 * 
 * @author Samuel Félix
 * 
 */
public class MergeByOverlap implements ClusteringMethod {

	private void createBaseClusters(Grid grid, float radius, Map<Instance, SortedList<Distance<Instance>>> distances, Instances instances){
		SortedList<Cell> cells = grid.getSortedCells();
		for ( Cell cell : cells ) {
			for ( Instance i : cell.getInstances() ) {
				Group group = new Group();
				group.setKey(i);
				group.addInstance(i);
				for ( Distance<Instance> colleague : distances.get(i) ) {
					if ( colleague.getDistanceToInstance() > radius )
						break;
					group.addInstance(colleague.getTheOtherOne(i));
				}
				group.setDensity(group.getInstances().size());
				cell.getGroups().add(group);
			}
		}
	}
	
	
	private List<Group> deh(Grid grid/*, int k*/ ){

		List<Group> result = new ArrayList<Group>();
		SortedList<Cell> sortedCell = grid.getSortedCells();
		for (Cell cell : sortedCell)
			result.addAll(cell.getGroups());
		for (int i = 0; i < result.size(); i++) {
			if ( result.get(i).getInstances().size() == 1 )
				result.remove(i);
		}
//		while ( result.size() > 20 ) 
			merge( result );
		return result;
	}

	public void merge(List<Group> result){
		boolean change;
		do{
			change = false;
			for (int i = 0; i <result.size(); i++) {
				for (int j = 0; j < result.size(); j++) {
					if ( i == result.size() )
						i--;
					if ( i == j )
						continue;
					if ( canMerge(result.get(i), result.get(j), result) ) {
						change = true;
						j--;
					}
				}
			}
		}
		while(change);
	}
	
	private boolean canMerge(Group group1, Group group2, List<Group> cell) {
		Set<Instance> s = findSimilarInstancesInGroups(group1, group2);
		if ( s.isEmpty() )
			return false;
		group1.setDensity((group1.getDensity() + group2.getDensity())/2);
		for ( Instance j : group2.getInstances() ) {
			if ( !s.contains(j) )
				group1.addInstance(j);
		}
		cell.remove(group2);
		return true;
  	}

	private List<Group> startFromEquallySizedGrid(Instances instances, float radius, Grid grid){
		Map<Instance, SortedList<Distance<Instance>>> distances = calculateDistance(instances);
		createBaseClusters(grid, radius, distances, instances);
		List<Group> result = deh(grid/*, k*/);  
		return result;
	}
	
	private void startFromDBClasd(Map<Parameter, Object> sharedObjects) {
//		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();
		List<Group> groups = (List<Group>) sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST);
		merge(groups);
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
	}


	private void startFromDBScan(Map<Parameter, Object> sharedObjects) {
		List<Group> groups = (List<Group>) sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST);
		merge(groups);
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
	}
	
	private void startFromSNN(Map<Parameter, Object> sharedObjects) {
		List<Group> groups = (List<Group>) sharedObjects.get(Parameter.SNN_GROUPS_FIRST);
		merge(groups);
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
	}

	private void startFromAMR(Map<Parameter, Object> sharedObjects) {
		Tree tree = (Tree) sharedObjects.get(Parameter.AMR_TREE);
		int lambda = ((Number) sharedObjects.get(Parameter.AMR_LAMBDA)).intValue();
//		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();c
		DistanceType type = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
		List<Group> groups = createGroupsFromAMR(tree.getRoot(), lambda, 0);
		merge(groups);
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
	}
	
	private void startFromDescry(Map<Parameter, Object> sharedObjects) {
		Tree tree = (Tree) sharedObjects.get(Parameter.DESCRY_TREE);
		
		List<Group> groups = createGroupsFromDescry(tree.getRoot());
		merge(groups);
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
	}
	
	private void startFromEquallySizedGrid(Map<Parameter, Object> sharedObjects) {
		Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
		Grid grid = (Grid) sharedObjects.get(Parameter.PHC_GRID);
//		int k = ((Number) sharedObjects.get(Parameter.DESCRY_K)).intValue();
		float radius = ((Number) sharedObjects.get(Parameter.PHC_RADIUS)).floatValue(); 
		List<Group> groups  = startFromEquallySizedGrid(instances, radius, grid/*, k*/);
		sharedObjects.put(Parameter.ALL_GROUPS, groups);
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

	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if (isReady(sharedObjects)) {
			if (	sharedObjects.get(Parameter.AMR_TREE) != null && 
					sharedObjects.get(Parameter.AMR_LAMBDA) != null && 
					sharedObjects.get(Parameter.ALL_DISTANCE) != null)
//					&& 
//					sharedObjects.get(Parameter.DESCRY_K) != null)
				startFromAMR(sharedObjects);
			else if (
					sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null && 
					sharedObjects.get(Parameter.ALL_DISTANCE) != null/* && 
					sharedObjects.get(Parameter.DESCRY_K) != null*/)
				startFromDBScan(sharedObjects);
			else if (
					sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null && 
					sharedObjects.get(Parameter.ALL_DISTANCE) != null/* && 
					sharedObjects.get(Parameter.DESCRY_K) != null*/)
				startFromSNN(sharedObjects);
			else if (
					sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
					sharedObjects.get(Parameter.DESCRY_TREE) != null && 
					sharedObjects.get(Parameter.DESCRY_K) != null)
				startFromDescry(sharedObjects);
			else if (
					sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
					sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST) != null 
//					&& 
//					sharedObjects.get(Parameter.DESCRY_K) != null
			)
				startFromDBClasd(sharedObjects);
			else {
				startFromEquallySizedGrid(sharedObjects);
			}
		}
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if (	(sharedObjects.get(Parameter.AMR_TREE) != null && 
						sharedObjects.get(Parameter.AMR_LAMBDA) != null && 
						sharedObjects.get(Parameter.ALL_DISTANCE) != null 
//						&& 
//						sharedObjects.get(Parameter.DESCRY_K) != null
						)
				||
						(sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null && 
						sharedObjects.get(Parameter.ALL_DISTANCE) != null 
//						&& 
//						sharedObjects.get(Parameter.DESCRY_K) != null)
						)
				||
						(sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null && 
						sharedObjects.get(Parameter.ALL_DISTANCE) != null 
//						&& 
//						sharedObjects.get(Parameter.DESCRY_K) != null)
						)
				||
						(sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
						sharedObjects.get(Parameter.DESCRY_TREE) != null && 
						sharedObjects.get(Parameter.DESCRY_K) != null)
				||
						(sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
						sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST) != null )
//						&& 
//						sharedObjects.get(Parameter.DESCRY_K) != null
				||		
						(sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
						sharedObjects.get(Parameter.PHC_GRID) != null && 
						sharedObjects.get(Parameter.PHC_RADIUS) != null && 
						sharedObjects.get(Parameter.DESCRY_K) != null)
			)
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return "This method aims to join clusters using users' preferences";
	}

	@Override
	public String getName() {
		return "MergeByOverlap";
	}
	
	/**
	 * O procedimento analisa os itens que pertencem aos dois grupos
	 * adicionando-os a collection que retorna os itens comuns
	 * 
	 * @param grupo1
	 * @param grupo2
	 * @return
	 */
	private Set<Instance> findSimilarInstancesInGroups(Group grupo1, Group grupo2) {
		Set<Instance> a = new HashSet<Instance>();
		a.addAll(grupo1.getInstances());
		a.retainAll(grupo2.getInstances());
		return a;
	}
	private Map<Instance, SortedList<Distance<Instance>>> calculateDistance(Instances db) {
		Map<Instance, SortedList<Distance<Instance>>> distances = new HashMap<Instance, SortedList<Distance<Instance>>>();
		for (int i = 0; i < db.numInstances(); i++) {
			for (int j = i + 1; j < db.numInstances(); j++) {
				double dist = DistanceMeasures.getInstance().calculateDistance(db.instance(j), db.instance(i), DistanceType.EUCLIDEAN);
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
}
