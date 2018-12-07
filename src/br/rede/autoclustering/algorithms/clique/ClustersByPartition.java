package br.rede.autoclustering.algorithms.clique;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.Unit;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.structures.groups.Subspace;
import br.rede.autoclustering.util.DistanceType;

/**
 * 
 * @author Samuel FÃ©lix
 *
 */
public class ClustersByPartition implements ClusteringMethod {

	/**
     * Perform join among the subspaces
     * @param subspaces		Subspaces to be used in the join
     * @param population	number of instances
     * @param threshold		the threshold
     * @return an array of Subspaces
     */
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			Instances instances = ((Instances)sharedObjects.get(Parameter.ALL_INSTANCES));
			int population = instances.numInstances();
			int atts = instances.numAttributes();
			Float threshold = (Float) sharedObjects.get(Parameter.CLIQUE_THRESHOLD);
			Subspace[] subspaces = (Subspace[]) sharedObjects.get(Parameter.CLIQUE_SUBSPACES_BEGIN);
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			
			//System.out.println("INICIANDO o bloco ´ClustersByPartition´");
			if ( distance == null )
				distance = DistanceType.EUCLIDEAN;
			List<Group> groups = clusterByPartition(subspaces, population, threshold, atts);
			sharedObjects.put(Parameter.ALL_GROUPS, groups);
			//System.out.println("TERMINANDO o bloco ´ClustersByPartition´\\n");
		}
	}
	
	public List<Group> clusterByPartition(Subspace[] subspaces, int population, float threshold, int dimensions){
		//System.out.println("\nENTREI no clusterByPartition()");
		subspaces = joinSubspaces(subspaces, population, threshold);
		List<Group> groups = new ArrayList<Group>();
		int k = 0;
 		while ( subspaces != null && subspaces.length != 0 ){
 			//remove previous (k-1)-clusters and add k-clusters until k <= d (d-dimensional space)
 			//System.out.println("ANTES do getClusters()");
 			groups.clear();
 			
 			//debug
 			//long started = System.currentTimeMillis();
 			groups.addAll(getClusters(subspaces));
 			//long elapsed = System.currentTimeMillis() - started;
 			//System.out.println("Tempo de execução do .addAll(getClusters()): "+(elapsed/1000));

 			//System.out.println("Clusters atÃ© o momento (k="+(++k)+") = "+groups.size());
 			
 			Arrays.sort(subspaces);
 			//debug
 			//started = 0;
 			//started = System.currentTimeMillis();
 			subspaces = joinSubspaces(subspaces, population, threshold);
 			//elapsed = System.currentTimeMillis() - started;//debug
 			//System.out.println("Tempo de execução do joinSubspaces(): "+(elapsed/1000));//debug
 		}
 		//System.out.println("NÂº de clusters finais (k="+(++k)+") = "+groups.size());
 		//System.out.println("SAI do clusterByPartition()"); //debug
 		return groups;
	}
	
    private List<Group> getClusters(Subspace[] subspacesOfADimension){
    	List<Group> groups = new ArrayList<Group>();
		for (Subspace subspace : subspacesOfADimension) 
			groups.addAll(getClusters(subspace));
    	return groups;
    }
    
    
    private List<Group> getClusters(Subspace subspace){
    	List<Group> clusters = new ArrayList<Group>();
		for (Unit aUnit : subspace.getUnits()) {
			if (aUnit.getCluster()==null){
				Group cluster = new Group();
				clusters.add(cluster);
				dfs(aUnit, cluster, subspace);
			}
		}
		return clusters;
	}
    private Subspace[] joinSubspaces(Subspace[] subspaces, int population, float threshold){
		List<Subspace> candidates = new ArrayList<Subspace>();
		//System.out.println("Tamanho do SUBSPACE: "+subspaces.length); // debug
		if ( subspaces.length > 1 ){
			for (int i = 0 ; i < subspaces.length - 1; i++ ){
				for (int j = i+1 ; j < subspaces.length ; j++ ){
					Subspace candidate = subspaces[i].subspaceJoin(subspaces[j], population, threshold);
					if ( candidate != null ) 
						candidates.add(candidate);
				}
			}
			return candidates.toArray(new Subspace[candidates.size()]);
		}
		return null;
    }
	
    private void dfs(Unit u, Group cluster, Subspace subspace){
		cluster.getInstances().addAll(u.getInstances());
		u.setCluster(cluster);
		
		for (Attribute a : u.getAttributes().keySet()){
			Unit left = getLeftNeighbor(u, a, subspace);
			if (left!=null && left.getCluster()==null)
				dfs(left,cluster,subspace);
			
			Unit right = getRightNeighbor(u, a, subspace);
			if (right!=null && right.getCluster()==null)
				dfs(right,cluster,subspace);
			
		}
	}
	
	private Unit getLeftNeighbor(Unit u, Attribute attribute, Subspace subspace){
		for (Unit aUnit : subspace.getUnits()) {
			if (u.checkLeftNeighbor(aUnit, attribute))
				return aUnit; 
		}
		return null;
	}
	
	private Unit getRightNeighbor(Unit u, Attribute attribute, Subspace subspace){
		for (Unit aUnit : subspace.getUnits()) {
			if (u.checkRightNeighbor(aUnit, attribute))
				return aUnit; 
		}
		return null;
	}

	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.CLIQUE_SUBSPACES_BEGIN) != null &&
			 sharedObjects.get(Parameter.CLIQUE_THRESHOLD) != null )
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return "perform a subspace join in order to get the final cluster";
	}

	@Override
	public String getName() {
		return "ClusterByPartition";
	}
}
