package br.rede.autoclustering.algorithms.dbclasd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.Distance;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.SortedList;

/**
 * 
 * @author Samuel Félix
 *
 */
public class CandidatesByNpts implements ClusteringMethod{
	
	@Override
	public void executeStep(Map<Parameter, Object> sharedObjects) {
		if (isReady(sharedObjects)) {
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int pts = ((Number) sharedObjects.get(Parameter.DBCLASD_PTS)).intValue();
			double[][] distanceMatrix = (double[][]) sharedObjects.get(Parameter.DISTANCE_MATRIX);
			Map<Instance, SortedList<Distance<Instance>>> distances = null;
			
			if (distanceMatrix != null) {
				distances = calculateDistance(distanceMatrix,instances);
			} else {
				distances = calculateDistance(instances);
			}
			
			//Map<Instance, SortedList<Distance<Instance>>> distances = calculateDistance(instances);
			boolean overlap = true;
		
			List<Group> groups = overlap ? startOverlapping(distances, pts) : startNotOverlapping(distances, pts);
			sharedObjects.put(Parameter.DBCLASD_GROUPS_FIRST, groups);
		}
	}

	private List<Group> startNotOverlapping(Map<Instance, SortedList<Distance<Instance>>> distances, int pts) {
		List<Group> groups = new ArrayList<Group>();
		for(Instance i : distances.keySet()){
			Group g = new Group();
			int j = 0;
			for (Distance<Instance> di : distances.get(i)){
				if (di.getDistanceToInstance() != 0)
					j++;
				Instance theOther = di.getTheOtherOne(i);
				if ( distances.get(theOther) == null)
					theOther = di.getTheOtherOne(i);
				g.addInstance(theOther);
				if ( j == pts )
					break;
			}
			groups.add(g);
		}
		return groups;
	}

	private List<Group> startOverlapping(Map<Instance, SortedList<Distance<Instance>>> distances, int pts) {
		List<Group> groups = new ArrayList<Group>();
		for(Instance i : distances.keySet()){
			Group g = new Group();
			int j = 0;
			for (Distance<Instance> di : distances.get(i)){
				if (di.getDistanceToInstance() != 0)
					j++;
				g.addInstance(di.getTheOtherOne(i));
				if ( j == pts )
					break;
			}
			groups.add(g);
		}
		return groups;
	}

	@Override
	public String getName() {
		return "CandidatesByNpts";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_DISTANCE) != null && 
			 sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
			 sharedObjects.get(Parameter.DBCLASD_PTS) != null ||
			 sharedObjects.get(Parameter.DISTANCE_MATRIX) != null
		)
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		return null;
	}
	
	/**
	 * @param distances 
	 * @param db 
	 * 
	 */
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
	
	/**
	 * This method will use the distance Matrix computed on DistanceInformation building block
	 * @param distanceMatrix 
	 * @param db 
	 * 
	 */
	private Map<Instance, SortedList<Distance<Instance>>> calculateDistance(double[][] distanceMatrix, Instances db) {
		Map<Instance, SortedList<Distance<Instance>>> distances = new HashMap<Instance, SortedList<Distance<Instance>>>();
		for (int i = 0; i < distanceMatrix.length; i++) {
			for (int j = i + 1; j < distanceMatrix[i].length; j++) {
				double dist = distanceMatrix[i][j];
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
