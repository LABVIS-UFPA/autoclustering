package br.rede.autoclustering.algorithms.newsnn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.la4j.Vector;
import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.sparse.CRSMatrix;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;

public class SNNByConnectiveness implements ClusteringMethod {

	@Override
	public void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			//Parameters
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int k = ((Number) sharedObjects.get(Parameter.K_THRESHOLD)).intValue();
			CRSMatrix sharedNeighbors =((CRSMatrix) sharedObjects.get(Parameter.SNN_SN_Matrix));
			double[] density = (double[]) sharedObjects.get(Parameter.SNN_DENSITY);
			int MinPts = ((Number) sharedObjects.get(Parameter.SNN_NUM_PTS)).intValue();
			float Eps = ((Number) sharedObjects.get(Parameter.SNN_MAX_DIST)).floatValue();
			/*DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);*/
			//long first = System.currentTimeMillis();
			
			//algorithm
			//List<Group> groups = findCandidatesByDistance(instances, minPts, maxDist, distance);
			//Object[] result = findDensityBySNN(instances, sharedNeighbors, density, k, Eps, MinPts);
			List<Group> groups = findDensityBySNN(instances, sharedNeighbors, density, k, Eps, MinPts);
			//double[] snnDensity = (double[]) result[1];
			
//			int i = 0;
//			for (Group group : groups) {
//				System.out.println("group " + i + ":" + group.getInstances().size());
//				i++;
//			}
//			System.out.println("#################");
//			
//			new ClusterViewerFrame(instances, groups);
//			System.out.println("Tempo total: " + (System.currentTimeMillis() - first) / 1000);
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println("Number of instances:" + instances.numInstances());
			
			sharedObjects.put(Parameter.ALL_GROUPS, groups);
			//sharedObjects.put(Parameter.SNN_DENSITY, snnDensity);
		}
	}

	private List<Group> findDensityBySNN(Instances instances, CRSMatrix sharedNeighbors, double[] density, int k, float eps, int minPts) {
		int points = instances.numInstances();
		
		//Step 4
		//System.out.println("\n **** Invoking step 4 ****");
		// Para cada snnDensity[i] é armazenado o número de pontos (instâncias) que a instância i possui mais próxima
		// de acordo com a matrix de similaridade sharedNeighbors.
		double[] snnDen = findDensity(sharedNeighbors, eps, points);
//		System.out.print("\nsnnDen output: ");
//		for (int i = 0; i < snnDen.length; i++) {
//			System.out.print(snnDen[i]+" | ");
//		}
		

		//Step 5
		//System.out.println("\n **** Invoking step 5 ****");
		Object[] objCore = findCorePts(density, minPts, points);
		boolean[] cores = (boolean[]) objCore[0];
		ArrayList<Integer> corePts = (ArrayList<Integer>) objCore[1];
//		System.out.println("CorePts list: "+corePts.toString());
		
		//Step 6
		//System.out.println("\n **** Invoking step 6 ****");
		int C = 0;
		int[] labels = new int[points];
		C = joinCorePts(labels, corePts, sharedNeighbors, eps, C);
		//System.out.println("Number of cluster identified: "+C);
		
		//Step 7 and 8
		List<Group> groups = joinPtsGroups(cores, corePts, labels, instances, sharedNeighbors, eps, points, C);

		///////////
		/*Object[] obj = new Object[2];
		obj[0] = groups;
		obj[1] = snnDen;*/
		
		return groups;
	}

	/**
	 *  STEP 7 and 8
	 *  All points that are not within a radius of Eps of a core point are.
	 *  discarded (noise);
	 *  Assign all non-noise, non-core points to their nearest core point.
	 * @param cores : An identification list of corePts.
	 * @param corePts : An corePts list.
	 * @param labels : An list of labels assigned to each point.
	 * @param instances : The dataset.
	 * @param eps 
	 * @param s 
	 * @param N : Number of instances.
	 * @param C : Number of clusters identified.
	 * @return groups : An list of group, each point is assigned to one group.
	 */
	private List<Group> joinPtsGroups(boolean[] cores, ArrayList<Integer> corePts, int[] labels, Instances instances,
			CRSMatrix sharedNeighbors, float eps, int points, int C) {
		
		for (int i = 0; i < points; i++) {
			boolean notNoise = false;
			double maxSim = Double.MIN_VALUE;
			int bestCore = -1;
			double sim;

			if (cores[i]) // this is a core point
				continue;

			for (int j = 0; j < corePts.size(); j++) {
				int p = corePts.get(j);
				sim = sharedNeighbors.get(i, p);
				if (sim >= eps)
					notNoise = true;
				if (sim > maxSim) {
					maxSim = sim;
					bestCore = p;
				}
			}
			
			if (notNoise)
				labels[i] = labels[bestCore];
		}

		//System.out.println(C);
		List<Group> groups = formGroups(labels, C, instances);
		
		return groups;
	}

	/**
	 * Method to insert an instance to its group according to its label. Create group.
	 * @param labels : An list of labels assigned to each point.
	 * @param C : Number of clusters identified.
	 * @param instances : The dataset.
	 * @return groups : An list of group, each point is assigned to one group.
	 */
	private List<Group> formGroups(int[] labels, int C, Instances instances) {
		List<Group> groups = new ArrayList<Group>();
		Group group;

		for (int i = 0; i <= C; i++) {
			group = new Group();
			groups.add(group);
		}

		//System.out.println("labels: " + labels.length);

		for (int i = 0; i < labels.length; i++) {
			Instance instance = instances.instance(i);
			groups.get(labels[i]).addInstance(instance);
		}

		return groups;
	}

	/**
	 * Step 6 - Method to form clusters from the core points. If two core Pts are
	 * within Eps of each other, then place them in the same cluster.
	 * @param labels : Each instance is assign to a related cluster label.
	 * @param corePts ; List of core Pts.
	 * @param Eps : Radius from a point.
	 * @param S : Shared nearest neighbor from the Sparsified similarity matrix.
	 * @param c : Number of clusters formed.
	 */
	private int joinCorePts(int[] labels, ArrayList<Integer> corePts, CRSMatrix sharedNeighbors, float eps, int C) {
		ArrayList<Integer> visited = new ArrayList<Integer>(corePts.size());
//		System.out.println("Number of corePts: " + corePts.size());
//		System.out.print("Labels before corePts merges:");
//		System.out.println(Arrays.toString(labels));
		for (int i = 0; i < corePts.size(); i++) {
			int p = corePts.get(i);
			if (visited.contains(p))
				continue;
			visited.add(p);
			C++;
			labels[p] = C;
			ArrayDeque<Integer> neighCore = findCoreNeighbors(p, corePts, sharedNeighbors, eps);
			expandCluster(labels, neighCore, corePts, C, sharedNeighbors, eps, visited);
		}
		
//		System.out.print("Labels after corePts merges:");
//		System.out.println(Arrays.toString(labels));
//		System.out.println("Value of C into method: "+C);
		
		return C;
	}

	/**
	 * Method to continue find new neighbors from a neighbor's list of a point.
	 * @param labels : An list of labels assigned to each point(instance).
	 * @param neighbors : An neighbors list.
	 * @param corePts : An corePts list.
	 * @param C : Current cluster.
	 * @param S : Sparsified similarity matrix
	 * @param Eps : An radius from a point
	 * @param visited : An list of visited points
	 */
	private void expandCluster(int[] labels, ArrayDeque<Integer> neighCore,	ArrayList<Integer> corePts, int C, CRSMatrix sharedNeighbors,
			float eps, ArrayList<Integer> visited) {
		while (neighCore.size() > 0) {
			int p = neighCore.poll();

			if (visited.contains(p))
				continue;

			labels[p] = C;
			visited.add(p);

			ArrayDeque<Integer> neigh = findCoreNeighbors(p, corePts, sharedNeighbors, eps);
			neighCore.addAll(neigh);
		}
	}

	/**
	 * Method to find neighbors of a corePts.
	 * @param p : An corePts.
	 * @param corePts : An corePts list.
	 * @param S : Sparsified similarity matrix.
	 * @param Eps : An radius from a point
	 * @return neighbors : List of neighbors from a point.
	 */
	private ArrayDeque<Integer> findCoreNeighbors(int p, ArrayList<Integer> corePts, CRSMatrix sharedNeighbors, float eps) {
		ArrayDeque<Integer> neighbors = new ArrayDeque<Integer>(corePts.size() / 2);
		int p2;
		for (int i = 0; i < corePts.size(); i++) {
			p2 = corePts.get(i);
			if (p != p2 && sharedNeighbors.get(p, p2) >= eps)
				neighbors.add(p2);
		}
		return neighbors;
	}

	/**STEP 5 
	 * Method to find the core points using MinPts, find all points
	 * that have SNN density greater than MinPts.
	 * @param snnDen : A density vector to each point
	 * @param minPts : Number of min points to form a core point
	 * @param n : Number of instances
	 * @return obj : An array of object with cores and corePts
	*/
	private Object[] findCorePts(double[] snnDen, int minPts, int points) {
		ArrayList<Integer> corePts = new ArrayList<Integer>();
		boolean[] cores = new boolean[points];
		Object[] obj = new Object[2];
		
		int trueN = 0;
		for (int i = 0; i < points; i++) {
			if (snnDen[i] >= minPts) {
				corePts.add(i);
				cores[i] = true;
				trueN++;
			}
		}
//		System.out.println("\nNumber of Cores: " + trueN);
//		System.out.println("CorePts list:");
//		System.out.print(corePts.toString());
		
		obj[0] = cores;
		obj[1] = corePts;
		
		return obj;
	}

	/**
	 * Step 4 - Method to find the number of points that have an
	 * SNN similarity of Eps or greater to each point. This
	 * is the SNN density of the point.
	 * @param s 
	 * @param eps : min distance to each point
	 * @param n : number of instances
	 * @return snnDens : Return an array with the points that have the min requirement of density
	 */
	private double[] findDensity(CRSMatrix sharedNeighbors, float eps, int points) {
		double[] snnDensity = new double[points]; // should only contain ints though
		VectorIterator vi;
		double snnSim;
		
		//debug
		/*for (int i = 0; i < 3; i++) {
			int count = 0;
			Vector vec = sharedNeighbors.getRow(0);
			for (int j = 0; j < vec.length(); j++) {
				if (vec.get(j) != 0) {
					count++;
				}
			}
			//System.out.println("Print counter of line '"+i+"' in CSRm: "+count);
		}*/
		
		
		for (int i = 0; i < points; i++) {
			int count2 = 0;
			vi = sharedNeighbors.nonZeroIteratorOfRow(i);
			while (vi.hasNext()) {
				snnSim = vi.next();
				if (snnSim >= eps){
//					System.out.print("\nsnnSim: "+snnSim+" - ");
					snnDensity[i]++;
					if (i == 0 || i == 1 || i == 2) {
						count2++;
					}
					if (i == 0 || i == 1 || i == 2) {
						//System.out.print("snnDensity: "+snnDensity[i]+" | ");	
					}
				}
//					System.out.print(snnDensity[i]+": snnDensity |");
			}
			if (i == 0 || i == 1 || i == 2) {
				//System.out.println("\nPrint counter2 of line '"+i+"' in CSRm: "+count2);
			}
			
//			System.out.println("\nOUT= "+snnDensity[i]);
		}
		//System.out.println("Print snnDensity[0]: "+snnDensity[0]+"\nPrint snnDensity[1]: "+snnDensity[1]+"\nPrint snnDensity[2]: "+snnDensity[2]);
		
		return snnDensity;
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
			 sharedObjects.get(Parameter.K_THRESHOLD) != null &&
			 sharedObjects.get(Parameter.SNN_SN_Matrix) != null &&
			 sharedObjects.get(Parameter.SNN_DENSITY) != null &&
			 sharedObjects.get(Parameter.SNN_MAX_DIST) != null &&
			 sharedObjects.get(Parameter.SNN_NUM_PTS) != null)
			{
					return true;
			}else
			{
					return false;
			}
	}
	
	@Override
	public String getName() {
		return "SNNByConnectiveness";
	}

	@Override
	public String technicalInformation() {
		return "Este bloco é responsável por refinar o algoritmo SNN por densidade similar ao dbscan.";
	}

}
