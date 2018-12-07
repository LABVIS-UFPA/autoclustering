package br.rede.autoclustering.algorithms.dhc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.grid.HyperSpace;
import br.rede.autoclustering.structures.tree.NodoAtractor;
import br.rede.autoclustering.util.MathProcess;

public class AttractionTree implements ClusteringMethod, Comparator<NodoAtractor> {

	private Instances getAttractionTree(Instances instances,double sigma) {

		MathProcess process = new MathProcess();
		
		for (int i = 0; i < instances.numInstances(); i++) {
			Instance objeto = instances.instance(i);
			objeto.setWeight(process.getDensity(objeto, instances, sigma));
		}
		
		return instances;
	}

	private List<NodoAtractor> creatTree(Instances instances,double sigma) {
				
		MathProcess process = new MathProcess();
		List<NodoAtractor> nodos = new ArrayList<NodoAtractor>();
			
		for (int i = 0; i < instances.numInstances(); i++) {
	
			Instance Oi = instances.instance(i);
			NodoAtractor nodo = new NodoAtractor(Oi);
			Instance atrator = process.getAttrator(Oi, instances, sigma);
			nodo.setAtrator(new NodoAtractor(atrator));   
			nodos.add(nodo);		 
		
		}
		
		Collections.sort(nodos, this);
		return nodos;
	}
	
	private List<NodoAtractor> creatTree(List<Instance> instances,double sigma) {
		
		MathProcess process = new MathProcess();
		List<NodoAtractor> nodos = new ArrayList<NodoAtractor>();
			
		for (int i = 0; i < instances.size(); i++) {	
			Instance Oi = instances.get(i);
			NodoAtractor nodo = new NodoAtractor(Oi);
			Instance atrator = process.getAttrator(Oi, instances, sigma);
			nodo.setAtrator(new NodoAtractor(atrator));   
			nodos.add(nodo);		 		
		}
		
		Collections.sort(nodos, this);
		return nodos;
	}
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		
		if (isReady(sharedObjects)) {
			
			HyperSpace spatialRegion = (HyperSpace) sharedObjects.get(Parameter.DENCLUE_HYPER_SPACE);
			double sigma = ((Number) sharedObjects.get(Parameter.DHC_SIGMA)).doubleValue();
			
			if (spatialRegion != null) {				
				  List<Instance> list = spatialRegion.getEntities();
				  sharedObjects.put(Parameter.DHC_LIST_INSTANCE, creatTree(list,sigma));				 

			} else {
				Instances instances = getAttractionTree((Instances) sharedObjects.get(Parameter.ALL_INSTANCES), sigma);
				sharedObjects.put(Parameter.DHC_ATTRACTION_TREE,creatTree(instances,sigma));
			}

		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "AttractionTree";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ((sharedObjects.get(Parameter.ALL_INSTANCES) != null || sharedObjects
				.get(Parameter.DENCLUE_HYPER_SPACE) != null)
				&& sharedObjects.get(Parameter.DHC_SIGMA) != null)
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		// TODO Auto-generated method stub
		return "This method aims to create a Attraction Tree";
	}

	@Override
	public int compare(NodoAtractor o1, NodoAtractor o2) {
		 if (o1.getInstance().weight() < o2.getInstance().weight()) {
	            return 1;
	        } else if (o1.getInstance().weight() == o2.getInstance().weight()) {
	            return 0;
	        } else {
	            return -1;
	        }
	}

}
