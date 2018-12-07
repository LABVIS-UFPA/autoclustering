package br.rede.autoclustering.algorithms.newsnn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.sparse.CRSMatrix;

import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;

public class SNN implements ClusteringMethod {
//object
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "SNN";
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if (isReady(sharedObjects)) {
			//Parameters
			Instances X = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int MinPts = ((Number) sharedObjects.get(Parameter.DBSCAN_NUM_PTS)).intValue();
			float Eps = ((Number) sharedObjects.get(Parameter.DBSCAN_MAX_DIST)).floatValue();
			int K = ((Number) sharedObjects.get(Parameter.K_THRESHOLD)).intValue();
			
			//algorithm
			List<Group> groups = (List<Group>) sharedObjects.get(Parameter.DBCLASD_GROUPS_FIRST);
//			List<Group> groups = (List<Group>) sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST);
			if ( groups == null )
				groups = (List<Group>) sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST);
			if ( groups != null )
				try {
					snn(X, K ,  Eps, MinPts);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			sharedObjects.put(Parameter.DBSCAN_GROUPS_FIRST, groups);
		
		}
	}
		

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
				 sharedObjects.get(Parameter.K_THRESHOLD) != null &&
				 sharedObjects.get(Parameter.DBSCAN_MAX_DIST) != null &&
				 sharedObjects.get(Parameter.DBSCAN_NUM_PTS) != null && 
				 sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null )
		{
			return true;
		}else
		{
			return false;
		}
	}

	@Override
	public String technicalInformation() {
		
		return "Shared Nearest Neighbor";
	}
	public static List<Group> formGroups(int[] labels, ArrayList<Integer> corePts, int C, Instances instances) {
		System.out.println("Passei pelo formGroups()");
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

	public static int getIndex(Instances instances, Instance instance) {
		System.out.println("Passei pelo getIndex()");
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

	public static List<Group> snn(Instances X, int K, double Eps, int MinPts) throws Exception {
		System.out.println("Passei pelo snn()");
		int N = X.numInstances(); // number of points
		int d = X.numAttributes(); // dimensionality

		HashMap<Instance, Integer> mapIndex = new HashMap<Instance, Integer>();
		for (int i = 0; i < N; i++) {
			mapIndex.put(X.instance(i), i);
		}

		if (MinPts >= K) {
			throw new RuntimeException(
					"MinPts has to be smaller than K. No sense in a point having more than K neighbors.");
		}

		int[] labels = new int[N];

		// STEP 1 - get a similarity matrix

		// construct the kd-tree for knn queries
		KDTree kdtree = new KDTree();
		kdtree.setInstances(X);

		// STEP 2 - sparsify the matrix by keeping only the k most similar
		// neighbors

		// find the K-neighbors of each point
		HashMap<Integer, ArrayList<Integer>> kns = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> hs;

		for (int i = 0; i < N; i++) {
			// we will query for K + 1 nns because the
			// first nn is always the point itself
			Instances nns = kdtree.kNearestNeighbours(X.instance(i), K);
			// System.out.println(nns.numInstances());

			hs = new ArrayList<Integer>();

			for (int j = 0; j < nns.numInstances(); j++) { // start from the 2nd
															// nn
				// System.out.println(mapIndex.get(nns.instance(j)));
				hs.add((Integer) getIndex(X, nns.instance(j)));
			}

			kns.put(i, hs);
		}

		// STEP 3 - construct the shared nearest neighbor graph from the
		// sparsified matrix

		// The sparse matrix S holds in element (i,j) the SNN-similarity between
		// points i and j.
		CRSMatrix S = new CRSMatrix(N, N);
		double count;

		/*
		 * for (int i = 0; i < kns.size(); i++) { System.out.print(i + " - ");
		 * for (Integer j: kns.get(i)){ System.out.print(j + ","); }
		 * System.out.println(); }
		 */

		for (int i = 0; i < (N - 1); i++) {
			for (int j = i + 1; j < N; j++) {
				// create a link between i-j only if i is in j's kNN
				// neighborhood
				// and j is in i's kNN neighborhood
				// System.out.println(i + "-" + kns.get(j));
				if (kns.get(i).contains(j) && kns.get(j).contains(i)) {
					// System.out.println("entrou");
					// count = countIntersect(kns.get(i), kns.get(j));

					count = calculateNewSimilarity(X, i, j, kns, K);

					S.set(i, j, count);
					S.set(j, i, count);
				}
			}
		}

		// System.out.println(S.toCSV());

		// STEP 4 - find the SNN density of each point
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

		// STEP 5 - find the core points
		// using MinPts, find all points that have SNN density greater than
		// MinPts
		ArrayList<Integer> corePts = new ArrayList<Integer>();
		boolean[] cores = new boolean[N]; // initialized to false by default

		for (int i = 0; i < N; i++) {
			if (snnDens[i] >= MinPts) {
				corePts.add(i);
				cores[i] = true;
			}
		}

		int truen = 0;
		for (int i = 0; i < cores.length; i++) {
			if (cores[i] == true) {
				truen++;
			}
		}
		//System.out.println("true: " + truen);

		/*
		 * for (int i = 0; i < corePts.size(); i++) {
		 * System.out.print(corePts.get(i) + ","); }
		 */

		//System.out.println("Core pts list:");
		//System.out.println(corePts.toString());

		// System.out.println("similarities for point 0:");
		// vi = S.nonZeroIteratorOfRow(0);
		// while(vi.hasNext()) {
		// System.out.println("sim to 0: " + vi.next());
		// }

		// STEP 6 - form clusters from the core points. If two core pts are
		// within
		// Eps of each other, then place them in the same cluster
		int C = 0;
		ArrayList<Integer> visited = new ArrayList<Integer>(corePts.size());
		//System.out.println("core: " + corePts.size());
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

		//System.out.println("labels after corepts merges:");
		//System.out.println(Arrays.toString(labels));

		// STEP 7 & STEP 8
		//
		// All points that are not within a radius of Eps of a core point are
		// discarded (noise);
		//
		// Assign all non-noise, non-core points to their nearest
		// core point

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
		List<Group> groups = formGroups(labels, corePts, C, X);

		return groups;
	}

	private static void expandCluster(int[] labels, ArrayDeque<Integer> neighbors, ArrayList<Integer> corePts, int C,
			CRSMatrix S, double Eps, ArrayList<Integer> visited) {
		System.out.println("Passei pelo expandCluster()");

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

	private static ArrayDeque<Integer> findCoreNeighbors(final int p, ArrayList<Integer> corePts, CRSMatrix S,
			final double Eps) {
		System.out.println("Passei pelo arrayDeque()");
		ArrayDeque<Integer> neighbors = new ArrayDeque<Integer>(corePts.size() / 2);
		int p2;
		for (int i = 0; i < corePts.size(); i++) {
			p2 = corePts.get(i);
			if (p != p2 && S.get(p, p2) >= Eps)
				neighbors.add(p2);
		}
		return neighbors;
	}

	public static int countIntersect(HashSet<Integer> h1, HashSet<Integer> h2) {
		System.out.println("Passei pelo countIntersect()");
		int count = 0;
		for (Integer i : h1)
			if (h2.contains(i))
				count++;
		return count;
	}

	public static double calculateNewSimilarity(Instances instances, int inst1, int inst2,
			HashMap<Integer, ArrayList<Integer>> knnHashmap, int k) {
		System.out.println("Passei pelo calculateNewSimilarity()");
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

	public static ArrayList<Integer> getSharedObjects(ArrayList<Integer> h1, ArrayList<Integer> h2) {
		System.out.println("Passei pelo getSharedObjects()");
		// System.out.println(h1.size() + "," + h2.size());
		ArrayList<Integer> shared = new ArrayList<Integer>();
		for (Integer i : h1)
			if (h2.contains(i))
				shared.add(i);
		return shared;
	}

	// public static void main(String[] args) throws IOException {
	//
	// // test data set -> two normals: (0,0.5) and (10,0.5); 30 points each
	// CSVLoader csvLoader = new CSVLoader();
	// csvLoader.setSource(new File("C:/Users/Cássio/Dropbox/snn/simple.csv"));
	// csvLoader.setNoHeaderRowPresent(true);
	// Instances inst = csvLoader.getDataSet();
	//
	// double[][] X = new double[inst.numInstances()][inst.numAttributes()];
	// for (int i = 0; i < inst.numInstances(); i++)
	// X[i] = inst.instance(i).toDoubleArray();
	//
	// int[] labels = snn(X, 20, 10, 5);
	// plotScatter(X, labels);
	// }
	//
	// private static void plotScatter(double[][] X, int[] labels) {
	// List<Paint> colors = Arrays.asList(ChartColor.createDefaultPaintArray());
	// Collections.reverse(colors);
	//
	// XYSeriesCollection dataset = new XYSeriesCollection();
	//
	// HashSet<Integer> clusters = MyUtils.getUniqueElements(labels);
	//
	// Object[] uclusters = clusters.toArray();
	//
	// for (int i = 0; i < uclusters.length; i++) {
	// int mi = (Integer) uclusters[i];
	//
	// XYSeries p = new XYSeries("Cluster " + mi);
	// for (int j = 0; j < labels.length; j++)
	// if (labels[j] == mi)
	// p.add(X[j][0], X[j][1]);
	// dataset.addSeries(p);
	// }
	//
	// // create chart:
	// JFreeChart chart = ChartFactory.createScatterPlot("Scatter", "X0", "X1",
	// dataset);
	// XYPlot xypl = chart.getXYPlot();
	//
	// for (int i = 0; i < dataset.getSeriesCount(); i++) {
	// int mi = (Integer) uclusters[i];
	// xypl.getRenderer().setSeriesPaint(i, colors.get(i));
	// }
	//
	// for(int i = 0; i < dataset.getSeriesCount(); i++)
	// xypl.getRenderer().setSeriesShape(i, ShapeUtilities.createDiamond(3));
	//
	// ChartFrame frame = new ChartFrame("PROStream",chart);
	// frame.pack();
	// frame.setVisible(true);
	//
	// }
	//

}
