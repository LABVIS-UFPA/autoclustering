package br.rede.autoclustering.algorithms.newsnn;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.apps.svgbrowser.TransformHistory;
import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.sparse.CRSMatrix;

import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.DataBaseStructure;
import br.rede.autoclustering.util.DistanceType;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;

public class NewSNN2 {
	private static int k;

	public static void main(String[] args) throws Exception {
		setK(2);
		// long first = System.currentTimeMillis();
		DataBaseStructure db = new DataBaseStructure();
		db.loadDataBaseCSV(new File("databases/dbclasd.in"));
		// List<ClusteringMethod> methods = new ArrayList<ClusteringMethod>();
		Instances instances = db.getNormalizedData();

		// KDTree myKDTree;
		// System.out.println(instances.numInstances());
		// System.out.println(instances.numAttributes());

		// myKDTree = new KDTree();
		// System.out.println("build kd-tree");
		// myKDTree.setInstances(instances);

		// System.out.println(myKDTree.distanceFunctionTipText());

		// Instances knn = myKDTree.kNearestNeighbours(instances.instance(500),
		// 5);
		// System.out.println(instances.instance(500).value(0) + " " +
		// instances.instance(500).value(1));
		// System.out.println("==========");
		// for (int i = 0; i < knn.numInstances(); i++) {
		// for (int j = 0; j < knn.numAttributes(); j++) {
		// System.out.print(knn.instance(i).value(j) + " ");
		// }
		// System.out.println();
		// }

		List<Group> clusters = new NewSNN2().snn(instances, 2, 4, 15);

	}

	public List<Group> snn(Instances instances, int min_pts, double Eps,
			int K/* , DistanceType distance */) throws Exception {
		List<Group> groups = new ArrayList<Group>();
		int N = instances.numInstances();
		CRSMatrix S = new CRSMatrix(N, N);
		double[] snnDens = new double[N];
		ArrayList<Integer> corePts = new ArrayList<Integer>(N);
		boolean[] cores = new boolean[N];

		HashMap<Instance, ArrayList<Instance>> knnHashmap = new HashMap<Instance, ArrayList<Instance>>();

		if (min_pts >= K) {
			throw new RuntimeException("min_pts has to be smaller than K");
		}

		knnHashmap = getKnnOfEachPoints(instances, K);
		S = getSNNMatrix(instances, knnHashmap);
		System.out.println("SNN: " + snnDens.length);
		snnDens = getSNNDensity(S, N, Eps);
		System.out.println("SNN: " + snnDens.length);
		corePts = getCorePts(snnDens, instances, min_pts, cores);
		formClusterFromCorePts(instances, groups, corePts, S, Eps);
		refineGroups(instances, groups, corePts, cores, S, Eps);

		return groups;
	}

	public HashMap<Instance, ArrayList<Instance>> getKnnOfEachPoints(Instances instances, int k) throws Exception {
		HashMap<Instance, ArrayList<Instance>> knnHashmap = new HashMap<Instance, ArrayList<Instance>>();
		KDTree instanceKdtree = new KDTree();
		instanceKdtree.setInstances(instances);

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			Instances kns = instanceKdtree.kNearestNeighbours(instance, k);
			ArrayList<Instance> neighbors = new ArrayList<Instance>();
			for (int j = 0; j < kns.numInstances(); j++) {
				neighbors.add(kns.instance(j));
			}
			knnHashmap.put(instance, neighbors);
		}
		return knnHashmap;
	}

	public CRSMatrix getSNNMatrix(Instances instances, HashMap<Instance, ArrayList<Instance>> knnHashmap) {
		int N = instances.numInstances();
		CRSMatrix S = new CRSMatrix(N, N);

		double count;

		for (int i = 0; i < (N - 1); i++) {
			for (int j = i + 1; j < N; j++) {
				// create a link between i-j only if i is in j's kNN
				// neighborhood
				// and j is in i's kNN neighborhood
				System.out.println("Size for instance " + i + ": " + knnHashmap.get(instances.instance(i)).size());
				if (knnHashmap.get(instances.instance(i)).contains(instances.instance(j))
						&& knnHashmap.get(instances.instance(j)).contains(instances.instance(i))) {
					System.out.println("ENTROU");

					// snn normal
					// count = countIntersect(knnHashmap.get(i),
					// knnHashmap.get(j));

					count = calculateNewSimilarity(instances.instance(i), instances.instance(j), knnHashmap, getK());

					S.set(i, j, count);
					S.set(j, i, count);
				}
			}
		}
		//System.out.println(S.toCSV());

		return S;
	}

	public static double calculateNewSimilarity(Instance i1, Instance i2,
			HashMap<Instance, ArrayList<Instance>> knnHashmap, int k) {
		ArrayList<Instance> sharedObjects = getSharedObjects(knnHashmap.get(i1), knnHashmap.get(i2));
		EuclideanDistance distance = new EuclideanDistance();
		double sum1 = 0, sum2 = 0;

		for (Instance instance : sharedObjects) {
			sum1 += (distance.distance(i1, instance) + distance.distance(i2, instance));
		}

		for (int i = 0; i < k; i++) {
			sum2 += (distance.distance(i1, knnHashmap.get(i1).get(i))
					+ distance.distance(i2, knnHashmap.get(i2).get(i)));
		}

		return sum1 / sum2 * k;
	}

	public static double[] getSNNDensity(CRSMatrix S, int N, double Eps) {
		double[] snnDens = new double[N]; // should only contain ints though
		VectorIterator vi;
		double snnSim;

		for (int i = 0; i < N; i++) {
			vi = S.nonZeroIteratorOfRow(i);
			while (vi.hasNext()) {
				snnSim = vi.next();
				if (snnSim >= Eps)
					snnDens[i]++;
			}
		}
		return snnDens;
	}

	public static ArrayList<Integer> getCorePts(double[] snnDens, Instances instances, int MinPts, boolean[] cores) {
		int N = instances.numInstances();
		ArrayList<Integer> corePts = new ArrayList<Integer>(N);
		// boolean[] cores = new boolean[N]; // initialized to false by default

		System.out.println("snnDens: " + snnDens.length);
		for (int i = 0; i < N; i++) {
			if (snnDens[i] >= MinPts) {
				corePts.add(i);
				cores[i] = true;
			}
		}

		System.out.println("Core pts list:" + corePts.size());
		System.out.println(corePts.toString());

		return corePts;
	}

	public static List<Group> formClusterFromCorePts(Instances instances, List<Group> groups,
			ArrayList<Integer> corePts, CRSMatrix S,
			double Eps/* , int[] labels */) {
		Group group;

		int C = 0;
		ArrayList<Integer> visited = new ArrayList<Integer>(corePts.size());

		for (int i = 0; i < corePts.size(); i++) {
			int p = corePts.get(i);
			if (visited.contains(p))
				continue;
			visited.add(p);
			C++;

			group = new Group();
			group.setKey(instances.instance(p));
			group.addInstance(instances.instance(p));

			// labels[p] = C;
			ArrayDeque<Integer> neighCore = findCoreNeighbors(p, corePts, S, Eps);
			expandCluster(/* labels, C */instances, neighCore, corePts, group, S, Eps, visited);

			groups.add(group);
		}

		// System.out.println("labels after corepts merges:");
		// System.out.println(Arrays.toString(labels));

		System.out.println("Número de grupos: " + groups.size());

		return groups;
	}

	public static void refineGroups(Instances instances, List<Group> groups, ArrayList<Integer> corePts,
			boolean[] cores, CRSMatrix S, double Eps) {
		int N = instances.numInstances();

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

			if (notNoise) {
				int groupID = groups.indexOf(instances.instance(bestCore));
				groups.get(groupID).addInstance(instances.instance(i));

				// labels[i] = labels[bestCore];
			}
		}

		// return labels;
	}

	public static ArrayList<Instance> getSharedObjects(ArrayList<Instance> h1, ArrayList<Instance> h2) {
		ArrayList<Instance> shared = new ArrayList<Instance>();
		for (Instance i : h1)
			if (h2.contains(i))
				shared.add(i);
		return shared;
	}

	public static int countIntersect(ArrayList<Instance> h1, ArrayList<Instance> h2) {
		int count = 0;
		for (Instance i : h1)
			if (h2.contains(i))
				count++;
		return count;
	}

	public static double sumKnnDistances(Instance i, ArrayList<Instance> set) {
		EuclideanDistance distance = new EuclideanDistance();
		// distance.setInstances(test);
		double sum = 0;

		for (Instance iSet : set) {
			sum += distance.distance(i, iSet);
		}

		return sum;
	}

	private static void expandCluster(/* int[] labels, C */Instances instances, ArrayDeque<Integer> neighbors,
			ArrayList<Integer> corePts, Group group, CRSMatrix S, double Eps, ArrayList<Integer> visited) {

		while (neighbors.size() > 0) {
			int p = neighbors.poll();

			if (visited.contains(p))
				continue;

			// labels[p] = C;
			group.addInstance(instances.instance(p));

			visited.add(p);
			ArrayDeque<Integer> neigh = findCoreNeighbors(p, corePts, S, Eps);
			neighbors.addAll(neigh);
		}

	}

	private static ArrayDeque<Integer> findCoreNeighbors(final int p, ArrayList<Integer> corePts, CRSMatrix S,
			final double Eps) {
		ArrayDeque<Integer> neighbors = new ArrayDeque<Integer>(corePts.size() / 2);
		int cPoint;
		for (int i = 0; i < corePts.size(); i++) {
			cPoint = corePts.get(i);
			if (p != cPoint && S.get(p, cPoint) >= Eps)
				neighbors.add(cPoint);
		}
		return neighbors;
	}

	public int getK() {
		return k;
	}

	public static void setK(int K) {
		k = K;
	}

}
