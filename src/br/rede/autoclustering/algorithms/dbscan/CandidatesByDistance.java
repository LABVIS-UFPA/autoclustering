package br.rede.autoclustering.algorithms.dbscan;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.IndividualNode;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;

public class CandidatesByDistance implements ClusteringMethod{

	/**
	 * Para cada instancia candidata a centro sao incluidos as instancias que
	 * atendem os parametros estabelecidos de distancia e numero de pontos minimos
	 * para formar um grupo
	 * O algoritmo testa se o grupo recem formado, ja nao existe na collection, caso
	 * o grupo ja exista ele nao e incluido na collection 
	 * @param distance 
	 */
	
	private List<Group> findCandidatesByDistance(Instances instances, int min_pts, double maxDist, DistanceType distance){
//		System.out.println("//");
		//System.out.println("\nMin Pts: "+min_pts+"\nMax Dist: "+maxDist+"\nDistance: "+distance);
		//System.out.println("//");
		List<Group> groups = new ArrayList<Group>();
		Group group;
		/**
		 * Para cada instancia e criado um grupo e adicionado as instancias proximas
		 * determinadas pelo parametro de distancia
		 */
		for (int i = 0; i < instances.numInstances(); i++) {
			group = new Group();
			group.setKey(instances.instance(i));
			group.addInstance(instances.instance(i));
			/**
			 * Cada instancia proxima e incluida no novo grupo
			 */
			for (int j = 0; j < instances.numInstances(); j++) 
				if (i != j) 
					createGroupByDistance(group.getKey(), instances.instance(j), maxDist, group, distance);
			
			/**
			 * Caso o grupo contenha o numero minimo de pontos e nao exista ainda na collection
			 * o novo grupo e adicionado
			 */
			if(group.getInstances().size() >= min_pts && !groups.contains(group))
				groups.add(group);
			
		}
		
		return groups;
	}
	
	
	/**
	 * Verifica se a instancia possui uma distancia menor que o parametro estabelecido
	 * e adiciona ao grupo se a condicao for atendida
	 * @param inst2
	 * @param group
	 * @param distance 
	 */
	private void createGroupByDistance(Instance inst1, Instance inst2, double maxDist, Group group, DistanceType distance){
		double distanceValue = DistanceMeasures.getInstance().calculateDistance(inst1, inst2, distance);
		if(distanceValue <= maxDist)
			group.addInstance(inst2);
	}

	@Override
	public synchronized  void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			//Parameters
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int minPts = ((Number) sharedObjects.get(Parameter.DBSCAN_NUM_PTS)).intValue();
			float maxDist = ((Number) sharedObjects.get(Parameter.DBSCAN_MAX_DIST)).floatValue();
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
			
			//algorithm
			List<Group> groups = findCandidatesByDistance(instances, minPts, maxDist, distance);
			
//			
			sharedObjects.put(Parameter.DBSCAN_GROUPS_FIRST, groups);
		}
	}

	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
			 sharedObjects.get(Parameter.DBSCAN_MAX_DIST) != null &&
			 sharedObjects.get(Parameter.DBSCAN_NUM_PTS) != null )
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return "CandidatesByDistance";
	}

	@Override
	public String getName() {
		return "CandidatesByDistance";
	}
}

