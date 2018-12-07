package br.rede.autoclustering.algorithms.newsnn;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.sparse.CRSMatrix;

import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.ClusterViewerFrame;
import br.rede.autoclustering.util.DataBaseStructure;
import br.rede.autoclustering.util.DistanceType;
import weka.classifiers.bayes.net.search.fixed.FromFile;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;

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
public class BlockTest1SNN implements ClusteringMethod{
	
	/**
	 * Método inicial para chamada das etapas do algorimo (Step 1-3).
	 * Deve retornar uma lista de grupos.
	 * @param intances : As instâncias do dataset
	 * @param k : Número de k vizinhos mais próximos
	 * @throws Exception 
	*/
	private static List<Group> findCandidatesBySNN(Instances instances,int k){
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
		KDTree kdt = new KDTree();
		try {
			kdt.setInstances(instances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Step 2
		HashMap<Integer, ArrayList<Integer>> kns = sparsifyMatrix(instances,kdt,N,k);
		
		//debugger
		/*System.out.println("Tamanho: "+kns.size());
		for (int i = 0; i < 20; i++) {
			System.out.println("Linha: "+i+" do kns");
			for (Integer integer : kns.get(i)) {
				System.out.print(integer+" | ");
			}
			System.out.println("");
		}
		System.out.println("");*/
		
		//Step 3
		List<Group> groups = contructSNN(instances,kns, N);
//		int c = 0;
//		for (Group group : groups) {
//			for (Instance x : group.getInstances()) {
//				System.out.println("Instances:"+x);
//			}
//			c++;
//		}
//		System.out.println("Tamanho do laço:"+c);
		//System.out.println("Tamanho do list groups:"+groups.size());
		
		///////////
		return null;
	}
	
	/**STEP 3 - construct the shared nearest neighbor graph from the
	 * sparsified matrix.
	 * The sparse matrix S holds in element (i,j) the SNN-similarity between
	 * points i and j.
	 * @param instances 
	*/
	private static List<Group> contructSNN(Instances instances, HashMap<Integer, ArrayList<Integer>> kns, int n) {
		
		List<Group> groups = new ArrayList<Group>();
		Group group;
		
		CRSMatrix S = new CRSMatrix(n, n);
		double count;


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

					//count = calculateNewSimilarity(X, i, j, kns, K);

					//S.set(i, j, count);
					//S.set(j, i, count);
				}else
				{
//					C++;
				}
				
			}
		}
		
		return groups;
	}

	/** STEP 2 - sparsify the matrix by keeping only the k most similar
	 *  neighbors.
	 *  Find the K-neighbors of each point.
	*/
	private static HashMap<Integer, ArrayList<Integer>> sparsifyMatrix(Instances instances, KDTree kdt, int n, int k) {
				
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


	private static int getIndex(Instances instances, Instance instance) {
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
	public synchronized  void executeStep(Map<Parameter, Object> sharedObjects) {
		if ( isReady(sharedObjects) ) {
			//Parameters
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int k = ((Number) sharedObjects.get(Parameter.K_THRESHOLD)).intValue();
			/*int minPts = ((Number) sharedObjects.get(Parameter.DBSCAN_NUM_PTS)).intValue();
			float maxDist = ((Number) sharedObjects.get(Parameter.DBSCAN_MAX_DIST)).floatValue();
			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);*/
			
			//algorithm
			//List<Group> groups = findCandidatesByDistance(instances, minPts, maxDist, distance);
			List<Group> groups = findCandidatesBySNN(instances, k);
			
//			
			sharedObjects.put(Parameter.SNN_GROUPS_FIRST, groups);
		}
	}


	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return "BlockTest1SNN";
	}

	@Override
	public String technicalInformation() {
		// TODO Auto-generated method stub
		return "Teste de construção do primeiro bloco para o algoritmo do SNN.";
	}

}
