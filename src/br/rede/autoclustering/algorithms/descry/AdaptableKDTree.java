package br.rede.autoclustering.algorithms.descry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.tree.Node;
import br.rede.autoclustering.structures.tree.Tree;


/**
 * 
 * @author Samuel Félix
 *
 */
public class AdaptableKDTree implements ClusteringMethod{
	
	
	public Node kdtree(List<Instance> instances, int depth, int density){
	    if (instances.size() <= density)
	        return null;
	 
	    // Select axis based on depth so that axis cycles through all valid values
	    int k = instances.get(0).numAttributes(); // assumes all points have the same dimension
	    int axis = depth % k;
	 
	    // Sort point list and choose median as pivot element
	    sort(instances, axis);
	    int median = (instances.size())/2; // choose median
	 
	    // Create node and construct subtrees
	    Node node = new Node(depth);
	    node.setInstances(instances);
	    List<Node> leftAndRight = new ArrayList<Node>();
	    leftAndRight.add(kdtree( instances.subList(0, median), depth + 1 , density));
	    leftAndRight.add(kdtree( instances.subList(median, instances.size()), depth + 1 , density));
	    node.setChildren(leftAndRight);
	    return node;
	}

	private void sort(List<Instance> instances, final int axis) {
		Comparator<Instance> comparator = new Comparator<Instance>() {

			@Override
			public int compare(Instance o1, Instance o2) {
				if ( o1.value(axis) < o2.value(axis))
					return -1;
				if ( o1.value(axis) > o2.value(axis))
					return 1;
				return 0;
			}
			
		};
		Collections.sort(instances, comparator);
	}
	

	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int density = ((Number) sharedObjects.get(Parameter.DESCRY_DENSITY)).intValue();
			List<Instance> instancesList = new ArrayList<Instance>();
			for (int i = 0; i < instances.numInstances(); i++) 
				instancesList.add(instances.instance(i));
			Node root = kdtree(instancesList, 0, density);
			Tree tree = new Tree(root);
			sharedObjects.put(Parameter.DESCRY_TREE, tree);
		}
	}

	@Override
	public String getName() {
		return "AdaptableKDTree";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_DISTANCE) != null &&
			sharedObjects.get(Parameter.DESCRY_DENSITY) != null ){
			return true;
		}
		return false;
	}

	@Override
	public String technicalInformation() {
		return "creates a KDTree";
	}
	
}
