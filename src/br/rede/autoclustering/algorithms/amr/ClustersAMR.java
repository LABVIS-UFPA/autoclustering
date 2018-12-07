package br.rede.autoclustering.algorithms.amr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.tree.Cell;
import br.rede.autoclustering.structures.tree.Node;
import br.rede.autoclustering.structures.tree.NodeCluster;
import br.rede.autoclustering.structures.tree.Tree;
import br.rede.autoclustering.util.DistanceType;

/**
 * 
 * @author Samuel Félix
 *
 */
public class ClustersAMR implements ClusteringMethod{

	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			int density = ((Number) sharedObjects.get(Parameter.AMR_DENSITY)).intValue();
			int lambda  = ((Number) sharedObjects.get(Parameter.AMR_LAMBDA)).intValue();
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			Tree tree  = (Tree) (sharedObjects.get(Parameter.AMR_TREE));
			clustering(tree.getRoot(), distance, density, lambda);
			sharedObjects.put(Parameter.ALL_GROUPS, tree.getGroups());
		}
	}

	/**
	 * 
	 * @param node
	 * @param density
	 * @param lambda
	 */
	public void clustering(Node node, DistanceType distance, int density, int lambda){
		if ( node.getNodesLevel() < lambda)
			for ( Node child : node.getChildren() )
				clustering(child, distance, density, lambda);
		
		if ( node.getChildren().isEmpty() || node.getNodesLevel() == lambda ) {
			NodeCluster cluster = new NodeCluster();
			cluster.getCells().addAll(node.getGrid().getCells().values());
			cluster.setOutliners(identifyOutliners(cluster));
			node.getClusters().add(cluster);
			return;
		}
		
		//If there is only one cluster
		if ( node.getClusters().size() == 1 ){
			NodeCluster group = node.getClusters().iterator().next();
			// assign all unmarked mesh cell to this only cluster
			for ( Cell cell : node.getGrid().getCells().values() ) 
				if ( !cell.isRefined() ) 
					group.add(cell);
		} else {
			// for each unmarked mesh cell
			for ( Cell cell : node.getGrid().getCells().values() ) { 
				if ( !cell.isRefined() ) {
					double mininumdistance = Float.MIN_VALUE;
					NodeCluster closestCluster = null;
					// for each cluster in this grid
					for ( NodeCluster cluster : node.getClusters() ) {
						// 	find the distance to the clusters's outliners
						double value = calculateDistance(cell, cluster, distance);
						if ( value < mininumdistance ) { 
							mininumdistance = value;
							closestCluster = cluster;
						}
					}
					if ( closestCluster != null )
						closestCluster.add(cell);
				}
			}
		}
	}
	
	
	private double calculateDistance(Cell key, NodeCluster cluster, DistanceType distanceType) {
		double minimumDistance = Float.MAX_VALUE; 
		for ( Cell point : cluster.getOutliners() ) {
			double distance = calculateDistance(key, point, distanceType);
			if ( distance < minimumDistance  )
				minimumDistance = distance;
		}
		return minimumDistance;
	}
	
	private double calculateDistance(Cell key, Cell point, DistanceType distance) {
		String first[] = key.getKey().split(",");
		String second[] = point.getKey().split(",");
		
		double ed = 0.0;
		for (int i = 0; i < first.length; i++) 
			ed += Math.pow(Double.parseDouble(first[i]) - Double.parseDouble(second[i]), 2);
		ed = Math.sqrt(ed);
		return ed;
	}

	public List<Cell> identifyOutliners( NodeCluster node ){
		Map<String,Cell> points = new HashMap<String,Cell>();
		for ( Cell cell : node.getCells() )
			points.put(cell.getKey(), cell);
		
		List<Cell> outliners = new ArrayList<Cell>();
		for ( Cell key : points.values() ) {
			boolean isOutLiner = false;
			String[] values = key.getKey().split(",");
			int[] doubleValues = new int[values.length];
			for (int i = 0; i < doubleValues.length; i++) 
				doubleValues[i] = Integer.parseInt(values[i]);
			for (int i = 0; i < doubleValues.length; i++) {
				StringBuffer searchKeyPlus = new StringBuffer(doubleValues.length);
				StringBuffer searchKeyMinus = new StringBuffer(doubleValues.length);
				
				if ( i != 0 ) {
					searchKeyPlus.append(",");
					searchKeyMinus.append(",");
				}
				for (int j = 0; j < i; j++) {
					searchKeyPlus.append(doubleValues[j]).append(",");
					searchKeyMinus.append(doubleValues[j]).append(",");
				}
				searchKeyPlus.append(doubleValues[i]+1);
				searchKeyMinus.append(doubleValues[i]-1);

				for (int j = i+1; j < doubleValues.length; j++) {
					searchKeyPlus.append(",").append(doubleValues[j]);
					searchKeyMinus.append(",").append(doubleValues[j]);
				}
				
				if ( points.get(searchKeyMinus) == null || points.get(searchKeyPlus) == null ) {
					isOutLiner = true;
					break;
				}
			}
			if ( isOutLiner ) 
				outliners.add(key);
		}
		
		return outliners;
	}
	
	@Override
	public String getName() {
		return "ClustersAMR";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.AMR_TREE) != null &&
				 sharedObjects.get(Parameter.AMR_LAMBDA) != null &&
				 sharedObjects.get(Parameter.AMR_SLICES) != null &&
				 sharedObjects.get(Parameter.AMR_DENSITY) != null 
			)
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return "creates clusters from AMR Tree";
	}

}
