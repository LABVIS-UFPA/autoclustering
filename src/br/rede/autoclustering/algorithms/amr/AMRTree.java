package br.rede.autoclustering.algorithms.amr;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.Grid;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.structures.tree.Node;
import br.rede.autoclustering.structures.tree.Tree;

/**
 * 
 * @author Samuel Félix
 *
 */
public class AMRTree implements ClusteringMethod{
	
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			Grid grid = (Grid) sharedObjects.get(Parameter.PHC_GRID2);
			int slices = ((Number) sharedObjects.get(Parameter.AMR_SLICES)).intValue();
			float density = ((Number) sharedObjects.get(Parameter.AMR_DENSITY)).floatValue();
			
			Node root = new Node(0);
			root.setGrid(grid);
			Tree tree = new Tree(root);
			start(instances, root, slices, density);
			sharedObjects.put(Parameter.AMR_TREE, tree);
		}
	}
	@Override
	public String getName() {
		return "AMRTree";
	}
	
	//-2.147483648E9
	//40.23350877 23.684211
	public void start(Instances instances,Node node, int slices, float density) {

		//obter grid
		Grid grid = node.getGrid();
		//Achar regioes densas
		List<String> denseRegions = findDenseCells(grid, density);
		if ( denseRegions.isEmpty() )
			return;
		//achar regioes conectadas e criar um novo grid
		List<Grid> grids = findConnectedRegions(grid, denseRegions);
		if ( grids.isEmpty() || grids.size() == 1 )
			return;
		List<Group> clusters = new ArrayList<Group>();
		for ( Grid ka : grids ) {
			Group a = new Group();
			a.getInstances().addAll(ka.getInstances());
			clusters.add(a);
		}
//		new ClusterViewerFrame("a",null, clusters);
		//criar um novo nó com o grid encontrado
		for ( Grid key : grids ) {
			Grid recalculatedGrid = retrieveChildGrid(key.getInstances(), instances.numAttributes(), slices);
			Node child = new Node(node.getNodesLevel()+1);
			child.setGrid(recalculatedGrid);
			node.addChild(child);
			start(instances,child, slices, density);
		}
	}

	/**
	 * Find those regions which density is over threshold
	 * @param grid
	 * @param density
	 * @return
	 */
	private List<String> findDenseCells(Grid grid, float density) {
		List<String> keys = new ArrayList<String>();
		for ( String key : grid.getCells().keySet() ){
			if ( grid.getCells().get(key).getInstances().size() >= density ) {
				grid.getCells().get(key).setRefined(true);
				keys.add(key);
			}
		}
		return keys;
	}

	/**
	 * Find dense neighbors
	 * @param grid
	 * @param denseRegions
	 * @return
	 */
	private List<Grid> findConnectedRegions(Grid grid, List<String> denseRegions) {
		List<Grid> result = new ArrayList<Grid>();
		List<Boolean> visited = new ArrayList<Boolean>();
		for (int j = 0; j < denseRegions.size(); j++) 
			visited.add(false);

		for (int i = 0; i < denseRegions.size(); i++) {
			Grid newGrid = new Grid();
			newGrid.getCells().put(denseRegions.get(i), grid.getCells().get(denseRegions.get(i)));
			discoverNeighbors(i, grid, newGrid, denseRegions, visited);
			result.add(newGrid);
		}
		return result;
	}
	

	private void discoverNeighbors(int i, Grid grid, Grid newGrid, List<String> denseRegions, List<Boolean> visited) {
		visited.set(i, true);
		String key1 = denseRegions.get(i);
		for (int j = 0; j < denseRegions.size(); j++) {
			if ( i != j && !visited.get(j)) {
				String key2 = denseRegions.get(j);
				if ( canJoin(key1, key2) ) {
					if ( !visited.get(j) ) 
						discoverNeighbors(j, grid, newGrid, denseRegions, visited);
					newGrid.getCells().put(key2, grid.getCells().get(key2));
					int index2 = denseRegions.indexOf(key2);
					denseRegions.remove(index2);
					visited.remove(index2);
				}
			}
		}
	}


	
	private boolean canJoin(String key1, String key2) {
		int maxDiffAtt = 0;
		String first[] = key1.split(","); 
		String second[] = key2.split(",");
		for (int i = 0; i < first.length; i++) { 
			int firstValue = Integer.parseInt(first[i]);
			int secondValue = Integer.parseInt(second[i]);
			int difference = Math.abs(firstValue - secondValue);
			if ( difference == 0 )
				continue;
			else if ( difference == 1 )
				maxDiffAtt++;
			else
				return false;
			if ( maxDiffAtt > 1 )
				return false;
		}
		return true;
	}
	
	private Grid retrieveChildGrid(List<Instance> instances, int attSize, int slices){
		double[] lowerBounds = new double[attSize],upperBounds = new double[attSize];
		Arrays.fill(lowerBounds, Double.MAX_VALUE);
		Arrays.fill(upperBounds, Double.MIN_VALUE);
		for (Instance i : instances) {
			for (int j = 0; j < attSize; j++) { 
				if ( i.value(j) < lowerBounds[j] )
					lowerBounds[j] = i.value(j);
				if ( i.value(j) > upperBounds[j] )
					upperBounds[j] = i.value(j);
			}
		}
		return buildGrid(instances, lowerBounds, upperBounds, slices);
	}
	
	public Grid buildGrid(List<Instance> instances, double[] lowerBounds, double[] upperBounds, int slices){
		Grid grid2 = new Grid();
		grid2.createGrid(slices, lowerBounds, upperBounds, instances);
		return grid2;
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
			 sharedObjects.get(Parameter.PHC_GRID2) != null &&
			 sharedObjects.get(Parameter.AMR_SLICES) != null &&
			 sharedObjects.get(Parameter.AMR_DENSITY) != null 
		)
			return true;
		return false;
	}
	@Override
	public String technicalInformation() {
		return "Create an AMRTree";
	}	
}