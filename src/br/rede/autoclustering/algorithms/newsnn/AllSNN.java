package br.rede.autoclustering.algorithms.newsnn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.sparse.CRSMatrix;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.ClusterViewerFrame;

/**
 * Este bloco de construção é responsável por três etapas do algoritmo SNN
 * baseado no artigo 'Finding Clusters of diferent Sizes, Shapes and Densities in Noise,
 * High Dimensional Data (Levent Ertoz, Michael Steinbach and Vipin Kumar., 2003)'. As
 * etapas correspondem às 3 primeiras citadas no artigo. As etapas são:
 * 	1) Compute the similarity matrix;
 * 	2) Sparsify the similarity matrix by keeping only the 'k' most similar neighbors;
 * 	3)Construct the shared nearest neighbor graph from the sparsified similarity matrix.
 * Dessa forma, deseja obter os grupos em que os pontos estejam agrupados com os seus
 * vizinhos mais próximos. 
 * @param k
 */
public class AllSNN implements ClusteringMethod{

	/**
	 * Método inicial para chamada das etapas do algorimo (Step 1-3).
	 * Deve retornar uma lista de grupos.
	 * @param intances : As instâncias do dataset
	 * @param k : Número de k vizinhos mais próximos
	 * @param MinPts 
	 * @param Eps 
	 * @throws Exception 
	*/
	private List<Group> findCandidatesBySNN(Instances instances,int k, float Eps, int MinPts){
		int N = instances.numInstances(); // number of points
		int Dim = instances.numAttributes(); // dimensionality
		//System.out.println("Número de instâncias: "+N+"\nNúmero de atributos: "+Dim);
		
		//Mapear cada instância para um inteiro. 
		HashMap<Instance, Integer> mapIndex = new HashMap<Instance, Integer>();
		for (int i = 0; i < N; i++) {
			mapIndex.put(instances.instance(i), i);
		}
		
		int[] labels = new int[N];
		
		// STEP 1 - get a similarity matrix
		// construct the kd-tree for knn queries
		//System.out.println("\n * Invoking step 1 *");
		KDTree kdt = new KDTree();
		try {
			kdt.setInstances(instances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Step 2
		//System.out.println("\n **** Invoking step 2 ****");
		HashMap<Integer, ArrayList<Integer>> kns = sparsifyMatrix(instances, kdt, N, k);
		//System.out.println("Size of kns: "+kns.size());
		
		
		//Step 3
		//System.out.println("\n **** Invoking step 3 ****");
		CRSMatrix S = contructSNN(instances, kns, N, k);
//		System.out.println("Rows: "+S.rows()+", Columns: "+S.columns()+" of S.");

		//Step 4
		//System.out.println("\n **** Invoking step 4 ****");
		double[] snnDen = findDensity(S, Eps, N);
//		System.out.print("\nsnnDen output: ");
//		for (int i = 0; i < snnDen.length; i++) {
//			System.out.print(snnDen[i]+" | ");
//		}
		
		//Step 5
		//System.out.println("\n **** Invoking step 5 ****");
		Object[] objCore = findCorePts(snnDen, MinPts, N);
		boolean[] cores = (boolean[]) objCore[0];
		ArrayList<Integer> corePts = (ArrayList<Integer>) objCore[1];
		
		//Step 6
		//System.out.println("\n **** Invoking step 6 ****");
		int C = 0;
		C = joinCorePts(labels, corePts, S, Eps, C);
		//System.out.println("Number of cluster identified: "+C);
		
		//Step 7 and 8
		List<Group> groups = joinPtsGroups(cores, corePts, labels, instances, S, Eps, N, C);
		
		///////////
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
			CRSMatrix S, float Eps, int N, int C) {
		
		for (int i = 0; i < N; i++) {
			boolean notNoise = false;
			double maxSim = Double.MIN_VALUE;
			int bestCore = -1;
			double sim;

			if (cores[i]) // this is a core point
				continue;

			for (int j = 0; j < corePts.size(); j++) {
				int p = corePts.get(j);
				sim = S.get(i, p);
				if (sim >= Eps)
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
	public List<Group> formGroups(int[] labels, int C, Instances instances) {
		
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
	public static int joinCorePts(int[] labels, ArrayList<Integer> corePts, CRSMatrix S, float Eps, int C) {
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
			ArrayDeque<Integer> neighCore = findCoreNeighbors(p, corePts, S, Eps);
			expandCluster(labels, neighCore, corePts, C, S, Eps, visited);
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
	private static void expandCluster(int[] labels, ArrayDeque<Integer> neighbors, ArrayList<Integer> corePts, int C, CRSMatrix S, float Eps,
			ArrayList<Integer> visited) {
		
		while (neighbors.size() > 0) {
			int p = neighbors.poll();

			if (visited.contains(p))
				continue;

			labels[p] = C;
			visited.add(p);

			ArrayDeque<Integer> neigh = findCoreNeighbors(p, corePts, S, Eps);
			neighbors.addAll(neigh);
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
	private static ArrayDeque<Integer> findCoreNeighbors(int p, ArrayList<Integer> corePts, CRSMatrix S, float Eps) {
		ArrayDeque<Integer> neighbors = new ArrayDeque<Integer>(corePts.size() / 2);
		int p2;
		for (int i = 0; i < corePts.size(); i++) {
			p2 = corePts.get(i);
			if (p != p2 && S.get(p, p2) >= Eps)
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
	public static Object[] findCorePts(double[] snnDen, int minPts, int n) {
		ArrayList<Integer> corePts = new ArrayList<Integer>();
		boolean[] cores = new boolean[n];
		Object[] obj = new Object[2];
		
		int trueN = 0;
		for (int i = 0; i < n; i++) {
			if (snnDen[i] >= minPts) {
				corePts.add(i);
				cores[i] = true;
				trueN++;
			}
		}
//		System.out.println("Number of Cores: " + trueN);
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
	public static double[] findDensity(CRSMatrix ssm, float eps, int n) {
		double[] snnDensity = new double[n]; // should only contain ints though
		VectorIterator vi;
		double snnSim;

		for (int i = 0; i < n; i++) {
			vi = ssm.nonZeroIteratorOfRow(i);
			while (vi.hasNext()) {
				snnSim = vi.next();
				if (snnSim >= eps)
					snnDensity[i]++;
			}
		}
		return snnDensity;
	}

	/**STEP 3 - Method to construct the shared nearest neighbor graph from the
	 * sparsified matrix.
	 * The sparse matrix S holds in element (i,j) the SNN-similarity between
	 * points i and j.
	 * @param instances 
	 * @param k 
	*/
	public static CRSMatrix contructSNN(Instances instances, HashMap<Integer, ArrayList<Integer>> kns, int n, int k) {

		CRSMatrix s = new CRSMatrix(n, n);
		double count;

		/*
		 * for (int i = 0; i < kns.size(); i++) { System.out.print(i + " - ");
		 * for (Integer j: kns.get(i)){ System.out.print(j + ","); }
		 * System.out.println(); }
		 */

		for (int i = 0; i < (n - 1); i++) {
			for (int j = i + 1; j < n; j++) {
				// create a link between i-j only if i is in j's kNN
				// neighborhood and j is in i's kNN neighborhood
				// System.out.println(i + "-" + kns.get(j));
				
				if (kns.get(i).contains(j) && kns.get(j).contains(i)) {
//					System.out.print("Get: "+i+" Contains: "+j+" = "+kns.get(i).contains(j));
//					System.out.print(" AND Get: "+j+" Contains: "+i+" = "+kns.get(j).contains(i));
//					System.out.println("");
					// count = countIntersect(kns.get(i), kns.get(j));

					count = calculateNewSimilarity(instances, i, j, kns, k);

					s.set(i, j, count);
					s.set(j, i, count);
				}
			}
		}
		
		return s;
	}

	/** STEP 2 - Method to sparsify the matrix by keeping only the k most similar
	 *  neighbors.
	 *  Find the K-neighbors of each point.
	*/
	public static HashMap<Integer, ArrayList<Integer>> sparsifyMatrix(Instances instances, KDTree kdt, int n, int k) {
				
		HashMap<Integer, ArrayList<Integer>> temp = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> hs;
		
		for (int i = 0; i < n; i++) {
			// we will query for K + 1 nns because the
			// first nn is always the point itself
			Instances nns = null;
			try {
				nns = kdt.kNearestNeighbours(instances.instance(i), k);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// System.out.println(nns.numInstances());

			hs = new ArrayList<Integer>();

			for (int j = 0; j < nns.numInstances(); j++) { // start from the 2nd
															// nn
				// System.out.println(mapIndex.get(nns.instance(j)));
				hs.add((Integer) getIndex(instances, nns.instance(j)));
			}

			temp.put(i, hs);
		}
		
		return temp;
	}
	
	/**Method to calculate the similarity between two instances
	 * if they are neighbor.
	 * @param instances : Dataset
	 * @param inst1 : Instance(i) in the Dataset
	 * @param inst2 : Instance(j) in the Dataset
	 * @param knnHashmap : HashMap with the shared neighbor
	 * @param k : The k most similar neighbor(threshold)
	*/
	public static double calculateNewSimilarity(Instances instances, int inst1, int inst2, HashMap<Integer, ArrayList<Integer>> knnHashmap, int k) {
			Instance i1 = instances.instance(inst1);
			Instance i2 = instances.instance(inst2);

			ArrayList<Integer> sharedObjects = getSharedObjects(knnHashmap.get(inst1), knnHashmap.get(inst2));
			// System.out.println("shared.size: " + sharedObjects.size());

			EuclideanDistance distance = new EuclideanDistance();
			distance.setInstances(instances);

			double sum1 = 0, sum2 = 0;

			for (Integer inst : sharedObjects) {

				sum1 += (distance.distance(i1, instances.instance(inst)) + distance.distance(i2, instances.instance(inst)));
			}

			for (int i = 0; i < k; i++) {
				sum2 += (distance.distance(i1, instances.instance(knnHashmap.get(inst1).get(i)))
						+ distance.distance(i2, instances.instance(knnHashmap.get(inst2).get(i))));
			}

			return sum1 / sum2 * k;
	}

	/**
	 * Method to obtain the shared objects(instances) between a pair of instances
	 * @param h1 : Instance h1 with k nearest neighbors
	 * @param h2 : Instance h2 with k nearest neighbors
	 * @return shared : ArrayList with the shared points between h1 and h2
	 */
	public static ArrayList<Integer> getSharedObjects(ArrayList<Integer> h1, ArrayList<Integer> h2) {
		ArrayList<Integer> shared = new ArrayList<Integer>();
		for (Integer i : h1)
			if (h2.contains(i))
				shared.add(i);
		return shared;
	}

	public static int getIndex(Instances instances, Instance instance) {
		int index = 0;
		InstanceComparator comp = new InstanceComparator();

		for (int i = 0; i < instances.numInstances(); i++) {
			if (comp.compare(instances.instance(i), instance) == 0) {
				index = i;
				break;
			}
		}

		return index;
	}
	
	@Override
	public void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			//Parameters
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int k = ((Number) sharedObjects.get(Parameter.K_THRESHOLD)).intValue();
			int MinPts = ((Number) sharedObjects.get(Parameter.DBSCAN_NUM_PTS)).intValue();
			float Eps = ((Number) sharedObjects.get(Parameter.DBSCAN_MAX_DIST)).floatValue();
			/*DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);*/
			//long first = System.currentTimeMillis();
			
			//algorithm
			//List<Group> groups = findCandidatesByDistance(instances, minPts, maxDist, distance);
			List<Group> groups = findCandidatesBySNN(instances, k, Eps, MinPts);
			
			int i = 0;
			for (Group group : groups) {
				System.out.println("group " + i + ":" + group.getInstances().size());
				i++;
			}
			System.out.println("#################");
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
			
//			sharedObjects.put(Parameter.SNN_GROUPS_FIRST, groups);
			sharedObjects.put(Parameter.ALL_GROUPS, groups);
		}
		
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
			 sharedObjects.get(Parameter.K_THRESHOLD) != null &&
			 sharedObjects.get(Parameter.DBSCAN_MAX_DIST) != null &&
			 sharedObjects.get(Parameter.DBSCAN_NUM_PTS) != null)
		{
				return true;
		}else
		{
				return false;
		}
	}

	@Override
	public String getName() {
		return "FullSNN";
	}
	
	@Override
	public String technicalInformation() {
		return "Este bloco é responsável por implementar todo o algoritmo do SNN.";
	}

}
