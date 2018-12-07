/*package br.rede.autoclustering.core;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.core.Instance;
import weka.core.Instances;

public class Controller {



	private static String[] METHODS = {
		"procedimentos.denclue.ASH",
		"procedimentos.denclue.ClustersByAttractors",
		"procedimentos.clique.ClustersByPartition",
		"procedimentos.clique.DenseAreas",
		"procedimentos.dbscan.CandidatesByDistance",
		"procedimentos.dbscan.ClusterByConnectiveness"
	};
	
	private Map<Parameter, Object> shared_objects = new HashMap<Parameter, Object>();
	private Map<Parameter, List<ClusteringMethod>> listeners = new HashMap<Parameter, List<ClusteringMethod>>(); 
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	private Map<String, ClusteringMethod> algorithms = new HashMap<String, ClusteringMethod>();
	
	public Controller() {
		for (Parameter key : Parameter.values()) {
			shared_objects.put(key, null);
			listeners.put(key, new ArrayList<ClusteringMethod>());
		}
		for ( String method : METHODS ) {
			try {
				ClusteringMethod clazz = (ClusteringMethod) Class.forName(method).newInstance();
//				clazz.init(this);
				algorithms.put(method, clazz);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setKey(Parameter key, Object object) {
		if ( key == Parameter.INSTANCES_SHARED ) 
			calculateLowerAndUpperBounds((Instances) object);
		shared_objects.put(key, object);
		for ( ClusteringMethod ca : listeners.get(key) ){
			//checar todos os parâmetros antes de acordar o ouvinte
			if ( ca.isReady() ) {
				threadPool.execute(ca);
			}
		}
	}

	public void addObserver(Parameter key, ClusteringMethod algorithm){
		listeners.get(key).add(algorithm);
	}

	public Object getKey(Parameter key) {
		return shared_objects.get(key);
	}
	
	private void calculateLowerAndUpperBounds(Instances instances){
		double[] upper_bounds = new double[instances.numAttributes()];
		double[] lower_bounds = new double[instances.numAttributes()];
		Arrays.fill(upper_bounds, -Double.MAX_VALUE);
		Arrays.fill(lower_bounds, Double.MAX_VALUE);
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance instance = instances.instance(i);
			for( int j = 0 ; j < instances.numAttributes() ; j++){
		        upper_bounds[j] = instance.value(j) > upper_bounds[j] ? instance.value(j) : upper_bounds[j] ;
		        lower_bounds[j] = instance.value(j) < lower_bounds[j] ? instance.value(j) : lower_bounds[j] ;
		    }
		}
		setKey(Parameter.LOWER_BOUNDS, lower_bounds);
		setKey(Parameter.UPPER_BOUNDS, upper_bounds);
	}
}
*/