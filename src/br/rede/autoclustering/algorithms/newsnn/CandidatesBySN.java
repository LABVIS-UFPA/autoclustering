package br.rede.autoclustering.algorithms.newsnn;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.basic.BasicComboBoxUI.ListDataHandler;

import org.la4j.iterator.VectorIterator;
import org.la4j.matrix.sparse.CRSMatrix;

import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.InstanceComparator;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.IndividualNode;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.DistanceMeasures;
import br.rede.autoclustering.util.DistanceType;

public class CandidatesBySN  implements ClusteringMethod {

	@Override
	public void executeStep(Map<Parameter, Object> sharedObjects) {
		// TODO Auto-generated method stub
		if ( isReady(sharedObjects) ) {
			//Parameters
			Instances instances = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
			int k = ((Number) sharedObjects.get(Parameter.K_THRESHOLD)).intValue();
			//int MinPts = ((Number) sharedObjects.get(Parameter.SNN_NUM_PTS)).intValue();
			float Eps = ((Number) sharedObjects.get(Parameter.SNN_MAX_DIST)).floatValue();
//			DistanceType distance = (DistanceType) sharedObjects.get(Parameter.ALL_DISTANCE);
				
			Object[] SandGroups = findCandidatesBySN(instances, k);
			
			CRSMatrix S = (CRSMatrix) SandGroups[0];
			List<Group> groups = (List<Group>) SandGroups[1];
			
			double[] snnDen = findDensity(S, Eps, instances.numInstances());
			
//			int c = 0;
//			for (Group x : groups) {
//				System.out.println("Group n."+ c +": " + x.getInstances().size());
//				System.out.println("Instancias do group "+c+"\n"+x.getInstances());
//				c++;
//			}
			
			sharedObjects.put(Parameter.SNN_SN_Matrix, S);
			sharedObjects.put(Parameter.SNN_GROUPS_FIRST, groups);
			sharedObjects.put(Parameter.SNN_DENSITY, snnDen);
		}
		
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
	private double[] findDensity(CRSMatrix sharedNeighbors, float eps, int numInstances) {
		double[] snnDensity = new double[numInstances]; // should only contain ints though
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
		
		for (int i = 0; i < numInstances; i++) {
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

	private Object[] findCandidatesBySN(Instances instances, int k){
//		System.out.println("findCAndidatesBySN foi chamado.");
		int points = instances.numInstances(); // number of points
		int dim = instances.numAttributes(); // dimensionality
//		List<Group> groups = new ArrayList<Group>();
//		Group group;
		
		HashMap<Instance, Integer> mapIndex = new HashMap<Instance, Integer>();
		for (int i = 0; i < points; i++) {
			mapIndex.put(instances.instance(i), i);
		}

		/*if (MinPts >= k) {
			throw new RuntimeException(
					"MinPts has to be smaller than K. No sense in a point having more than K neighbors.");
		}*/

		int[] labels = new int[points];
		
		//step 1 - get a similarity matrix
		// construct the kd-tree for knn queries
		KDTree kdTree = new KDTree();
		try {
			kdTree.setInstances(instances);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Step 2
		//System.out.println("\n **** Invoking step 2 ****");
		HashMap<Integer, ArrayList<Integer>> kns = sparsifyMatrix(instances, kdTree, points, k);
//		System.out.println("Tamanho do KNS: "+kns.size());
//		for (int i = 0; i < kns.size(); i++) {
//			System.out.println("Número de K-vizinhos mais próximos de "+(i)+" : "+kns.get(i).size());
//		}
		
		
		//Step 3
		//System.out.println("\n **** Invoking step 3 ****");
		CRSMatrix S = contructSN(instances, kns, points, k);
		
		//Step 4
		//System.out.println("\n **** Invoking step 4 ****");
		// Para cada snnDensity[i] é armazenado o número de pontos (instâncias) que a instância i possui mais próxima
		// de acordo com a matrix de similaridade sharedNeighbors.

		List<Group> groups = formGroups(kns, instances);
		
		Object[] obj = new Object[2];
		//System.out.println("Rows: "+S.rows()+", Columns: "+S.columns()+" of S.");		
		
		obj[0] = S;
		obj[1] = groups;
		
		return obj;
//		return S;
		
	}

	private List<Group> formGroups(HashMap<Integer, ArrayList<Integer>> kns, Instances instances) {
		// TODO Auto-generated method stub
		List<Group> groups = new ArrayList<Group>();
		Group group  = new Group();
		
		HashMap<Integer, ArrayList<Integer>> lista = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> nearest = new ArrayList<Integer>();
		
		for (int i = 0; i < /*120*/ instances.numInstances(); i++) {
			//System.out.println("For i: "+i);
			if (i < 1) {
				//System.out.println("If i < 1: OK");
				group.setKey(instances.instance(0));
				//group.addInstance(instances.instance(0));
			}else
			{
				//System.out.println("If i > 0: OK");
				if (lista.get(0).contains(i)) {
					//System.out.println("Group contains that instance.");
				}else
				{
					//System.out.println("If true case instance doesn't exist in the group.");
					//System.out.println("It will create a new group.");
					group = new Group();
					group.setKey(instances.instance(i));
					nearest = new ArrayList<Integer>();
				}
				
			}
			

				for (int j = 0; j < instances.numInstances(); j++) {
					if (kns.get(i).contains(j) && kns.get(j).contains(i)) {
						group.addInstance(instances.instance(j));
						if (!nearest.contains(j)) {
//							group.addInstance(instances.instance(j));
							nearest.add((Integer) j);
						}
					}
				}
			
			lista.put(0, nearest);
			//System.out.println("Vizinhos de '"+i+"'"+kns.get(i).toString());
			//System.out.println("Group:"+nearest.toString()+"\n");
			if (!groups.contains(group)) {
				groups.add(group);
			}
		}// end FOR i
		
//		System.out.println("\nLista: "+lista.get(0).toString()+"\nLista size:"+lista.get(0).size());
//		int c = 0;
//		for (Group x : groups) {
//			System.out.println("Group n."+ c +": " + x.getInstances().size());
//			System.out.println("Instancias do group "+c+"\n"+x.getInstances());
//			c++;
//		}
		
		return groups;
	}

	/**STEP 3 - Method to construct the shared nearest neighbor graph from the
	 * sparsified matrix.
	 * The sparse matrix S holds in element (i,j) the SNN-similarity between
	 * points i and j.
	 * @param instances 
	 * @param k 
	*/
	public static CRSMatrix contructSN(Instances instances, HashMap<Integer, ArrayList<Integer>> kns, int points, int k) {
		CRSMatrix S = new CRSMatrix(points, points);
		double count;

		/*
		 * for (int i = 0; i < kns.size(); i++) { System.out.print(i + " - ");
		 * for (Integer j: kns.get(i)){ System.out.print(j + ","); }
		 * System.out.println(); }
		 */

		int c = 0;
		for (int i = 0; i < (points - 1); i++) {
			for (int j = i + 1; j < points; j++) {
				// create a link between i-j only if i is in j's kNN
				// neighborhood and j is in i's kNN neighborhood
				// System.out.println(i + "-" + kns.get(j));
				
				if (kns.get(i).contains(j) && kns.get(j).contains(i)) {
//					if (i == 0) {
//						System.out.print("Get 'i':"+i+" that contains 'j': "+j+" = "+kns.get(i).contains(j));
//						System.out.print(" AND Get 'j':"+j+" that contains 'i':"+i+" = "+kns.get(j).contains(i));
//						System.out.println("i("+i+"): "+kns.get(i).toString()+"\nj("+j+"): "+kns.get(j).toString());
//					}
//					
//					// count = countIntersect(kns.get(i), kns.get(j));
//					if (i < 2) {
//						count = calculateNewSimilarity(instances, i, j, kns, k);
//						System.out.println("Nova similaridade: "+count);
//						System.out.println("");
//					}else
//					{
						count = calculateNewSimilarity(instances, i, j, kns, k, c);
						c++;
						//System.out.println("Nova similaridade: "+count);
//					}

					S.set(i, j, count);
					S.set(j, i, count);
				}
			}
		}
		
		return S;
	}

	/** STEP 2 - Method to sparsify the matrix by keeping only the k most similar
	 *  neighbors.
	 *  Find the K-neighbors of each point.
	*/
	public static HashMap<Integer, ArrayList<Integer>> sparsifyMatrix(Instances instances, KDTree kdTree, int points, int k) {
		HashMap<Integer, ArrayList<Integer>> kNearestShared = new HashMap<Integer, ArrayList<Integer>>();
		ArrayList<Integer> hs;
//		System.out.println("Print Instances: "+instances.toString());
		
		for (int i = 0; i < points; i++) {
			// we will query for K + 1 nns because the
			// first nn is always the point itself
			Instances nns = null;
			try {
				nns = kdTree.kNearestNeighbours(instances.instance(i), k);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
//			if (i < 20) {
//				System.out.println("Instância em análise: "+i);
//				System.out.println("Número de K-vizinhos mais próximo: "+nns.numInstances());
//				System.out.println("Instâncias próximas a instância analisada '"+i+"'\n"+nns.toString());
//			}
			

			hs = new ArrayList<Integer>();

			for (int j = 0; j < nns.numInstances(); j++) { // start from the 2nd
															// nn
				// System.out.println(mapIndex.get(nns.instance(j)));
				hs.add((Integer) getIndex(instances, nns.instance(j)));
			}

			kNearestShared.put(i, hs);
//			if (i < 20) {
//				System.out.println(kNearestShared.get(i).toString());
//				System.out.println("");
//			}
			//System.out.println("Instância '"+i+"' com k-vizinhos:"+kNearestShared.get(i).size());
		}
		
		//System.out.println("Valor de K: "+k);
		int count = 0;
		int count2 = 0;
		double tmp = 0;
		double tmp2 = 0;
		EuclideanDistance distance = new EuclideanDistance();
		distance.setInstances(instances);
		
		for (int i = 0; i < kNearestShared.size(); i++) {
			Instance i1 = instances.instance(i);
			ArrayList<Double> distVec = new ArrayList<Double>();
			ArrayList<Double> distVec2 = new ArrayList<Double>();
			
			ArrayList<Double> distVec3 = new ArrayList<Double>();
			ArrayList<Double> distVec4 = new ArrayList<Double>();
			
			if (kNearestShared.get(i).size() <= k) {
				//debug to print normals K
				if (count2 < 3) {
					count2++;
					//System.out.println("(Normais)Instância '"+i+"' com size = k: "+kNearestShared.get(i).size());
					//System.out.println("K-vizinhos: "+kNearestShared.get(i).toString());
						for (Integer x : kNearestShared.get(i)) {
							Instance i2 = instances.instance(x);
							
							tmp2 = distance.distance(i1, i2);
//							DecimalFormat formato = new DecimalFormat(".##");
//							tmp2 = Double.valueOf(formato.format(tmp2));
//							String y = "("+x+"-"+tmp+")";
							distVec3.add(tmp);
							
							double i1x = i1.value(0);
							double i1y = i1.value(1);
							double i2x = i2.value(0);
							double i2y = i2.value(1);
							tmp = Math.sqrt(Math.pow((i2x-i1x),2) + Math.pow((i2y-i1y),2));
//							tmp = Double.valueOf(formato.format(tmp));
							distVec4.add(tmp);
						}
					//System.out.println("WEKA: "+distVec3.toString());
					//System.out.println("Hand: "+distVec4.toString());
					//System.out.println("");
				}//end IF count2
			}//end IF kNearestShared > K
			
//			ArrayList<String> distVec = new ArrayList<String>();
			//debug to print anormals K
			if (kNearestShared.get(i).size() > k) {
				if (count < 3) {
					count++;
					//System.out.println("(Anormais)Instância '"+i+"' com size > k: "+kNearestShared.get(i).size());
					//System.out.println("K-vizinhos: "+kNearestShared.get(i).toString());
						for (Integer x : kNearestShared.get(i)) {
							Instance i2 = instances.instance(x);
							
							tmp = distance.distance(i1, i2);
//							DecimalFormat formato = new DecimalFormat(".##");
//							tmp = Double.valueOf(formato.format(tmp));
//							String y = "("+x+"-"+tmp+")";
							distVec.add(tmp);
							
							double i1x = i1.value(0);
							double i1y = i1.value(1);
							double i2x = i2.value(0);
							double i2y = i2.value(1);
							tmp = Math.sqrt(Math.pow((i2x-i1x),2) + Math.pow((i2y-i1y),2));
//							tmp = Double.valueOf(formato.format(tmp));
							distVec2.add(tmp);
						}
					//ystem.out.println("WEKA: "+distVec.toString());
					//System.out.println("Hand: "+distVec2.toString());
					//System.out.println("");
				}//end IF count
			}//end IF kNearestShared > K
		}
		//System.out.println("");
		return kNearestShared;
	}

	private static double calculateNewSimilarity(Instances instances, int inst1, int inst2,
		HashMap<Integer, ArrayList<Integer>> knnHashmap, int k, int controlCount) {
		Instance i1 = instances.instance(inst1);
		Instance i2 = instances.instance(inst2);

		ArrayList<Integer> sharedObjects = getSharedObjects(knnHashmap.get(inst1), knnHashmap.get(inst2));
		//System.out.println("Shared Objects between '"+inst1+"' and '"+inst2+"' are "+ sharedObjects.toString());
		

		EuclideanDistance distance = new EuclideanDistance();
		distance.setInstances(instances);

		double sum1 = 0, sum2 = 0;
		
		//debug to print the first 3 new similarities
		if (controlCount < 3) {
			//System.out.println("Instância: "+inst1+" com Instância :"+inst2);
			//System.out.println("Lista de compartilhados: "+sharedObjects.toString());
			for (Integer inst : sharedObjects) {
	
				sum1 += (distance.distance(i1, instances.instance(inst)) + distance.distance(i2, instances.instance(inst)));
			}
	
			//System.out.println("Soma das distâncias entre os compartilhados 'sum1': "+sum1);
			
			for (int i = 0; i < k; i++) {
				sum2 += (distance.distance(i1, instances.instance(knnHashmap.get(inst1).get(i)))
						+ distance.distance(i2, instances.instance(knnHashmap.get(inst2).get(i))));
			}
			
			//System.out.println("Vizinhos da instância '"+inst1+"': "+knnHashmap.get(inst1).toString());
			//System.out.println("Vizinhos da instância '"+inst2+"': "+knnHashmap.get(inst2).toString());
			//System.out.println("Soma das distâncias com os seus vizinhos 'sum2': "+sum2);
			//System.out.println("Valor de K: "+k);
			//System.out.println("Similaridade: "+(sum1/sum2*k)+"\n");
			return sum1 / sum2 * k;
			
		}else
		{
			for (Integer inst : sharedObjects) {
				
				sum1 += (distance.distance(i1, instances.instance(inst)) + distance.distance(i2, instances.instance(inst)));
			}
	
			for (int i = 0; i < k; i++) {
				sum2 += (distance.distance(i1, instances.instance(knnHashmap.get(inst1).get(i)))
						+ distance.distance(i2, instances.instance(knnHashmap.get(inst2).get(i))));
			}
			return sum1 / sum2 * k;
		}
//		System.out.println("Soma das distâncias entre os compartilhados 'sum1': "+sum1);
//		System.out.println("Soma das distâncias dos seus vizinhos 'sum2': "+sum2);
//		System.out.println("Valor de K: "+k);
//		return sum1 / sum2 * k;
	}

	private static ArrayList<Integer> getSharedObjects(ArrayList<Integer> h1, ArrayList<Integer> h2) {
		// System.out.println(h1.size() + "," + h2.size());
		ArrayList<Integer> shared = new ArrayList<Integer>();
		for (Integer i : h1)
			if (h2.contains(i))
				shared.add(i);
		return shared;
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
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		// TODO Auto-generated method stub
		if ( sharedObjects.get(Parameter.ALL_INSTANCES) != null &&
			 sharedObjects.get(Parameter.K_THRESHOLD) != null &&
			 sharedObjects.get(Parameter.SNN_MAX_DIST) != null &&
			 sharedObjects.get(Parameter.SNN_NUM_PTS) != null/* &&
				 sharedObjects.get(Parameter.SNN_GROUPS_FIRST) != null &&
				 sharedObjects.get(Parameter.DBSCAN_MAX_DIST) != null &&
				 sharedObjects.get(Parameter.DBSCAN_NUM_PTS) != null && 
				 sharedObjects.get(Parameter.DBSCAN_GROUPS_FIRST) != null*/ )
		{
			return true;
		}else
		{
			return false;
		}
	}
	
	@Override
	public String getName() {
		return "CandidatesBySNN";
	}

	@Override
	public String technicalInformation() {
		// TODO Auto-generated method stub
		return "Este bloco de retornar os clusters de acordo com os steps até 3 do algoritmo de 2003 baseado em Jarvis-Patrick algorithm.";
	}

}
