package br.rede.autoclustering.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Normalization {
	
	private HashMap<String, ArrayList<Double>> minMax;
	private Instances data;
	
	public Normalization(Instances data) {
		this.data = data;
		minMax = encontraLimitesAtributos(data);
	}
	
	
	private HashMap<String, ArrayList<Double>> encontraLimitesAtributos(Instances data){
		HashMap<String, ArrayList<Double>> minMax;
		ArrayList<Double> menorMaior;
		minMax = new HashMap<String, ArrayList<Double>>();
		for (int i = 0; i < data.numAttributes(); i++) {
			menorMaior = new ArrayList<Double>();
			double maior = -1111111;
			double menor = 99999999;
			for (int j = 0; j < data.numInstances(); j++) {
				if (data.instance(j).value(i) > maior) {
					maior = data.instance(j).value(i);
				}
				if (data.instance(j).value(i) < menor) {
					menor = data.instance(j).value(i);
				}
			}
			menorMaior.add(menor);
			menorMaior.add(maior);
			minMax.put("atributo"+i, menorMaior);
		}
		return minMax;
	}
	
	public void reverteBase(List<Instance> group){
		double originalValue;
		double min;
		double max;
		for(Instance i : group){
			for (int j = 0; j < i.numAttributes(); j++) {
				min = minMax.get("atributo"+j).get(0);
				max = minMax.get("atributo"+j).get(1);
				originalValue = (i.value(j) - min)/(max - min) * 100;
				i.setValue(j, originalValue);
			}
		}
	}
	
	public Instances normalizaBase(){
		Instances baseNormalizada; 
		FastVector fv = new FastVector();

		double normInstance;
		double min;
		double max;
		
		for (int i = 0; i < data.numAttributes(); i++) {
			fv.addElement(data.attribute(i));
		}
		baseNormalizada = new Instances(data.relationName(), fv, data.numInstances());
		for (int i = 0; i < data.numInstances(); i++) {
			Instance instance = new Instance(data.numAttributes());
			for (int j = 0; j < data.numAttributes(); j++) {
				min = minMax.get("atributo"+j).get(0);
				max = minMax.get("atributo"+j).get(1);
				normInstance = (data.instance(i).value(j) - min)/(max - min) * 100;
				instance.setValue(j, normInstance);
			}
			baseNormalizada.add(instance);
		}
		return baseNormalizada;
	}
}
