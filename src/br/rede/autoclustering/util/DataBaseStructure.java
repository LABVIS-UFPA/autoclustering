package br.rede.autoclustering.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class DataBaseStructure {

	private Instances database;
	private Instances dataEvalBase;
	private Instances normalizedData;
	private Instances normalizedTrainingData;
	private Instances normalizedTestData;
	private Normalization normalized;
	
	public void loadDataBase(File file){
		try {
			database = new Instances(new BufferedReader(new FileReader(file)));
			dataEvalBase = new Instances(new BufferedReader(new FileReader(file)));
			normalized = new Normalization(database);
			normalizedData = normalized.normalizaBase();
			InstancesRandomizer randomizer = new InstancesRandomizer(normalizedData, 70.0);
			normalizedTrainingData = randomizer.getTrainInstances();
			normalizedTestData = randomizer.getTestInstances();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Erro ao carregar Base de Dados");
			System.exit(1);
		}
	}
	
	public void loadDataBaseCSV(File file){
		try {
			CSVLoader csv = new CSVLoader();
			csv.setFile(file);
			database = new Instances(csv.getDataSet());
			dataEvalBase = new Instances(database);
			normalized = new Normalization(database);
			normalizedData = normalized.normalizaBase();
			InstancesRandomizer randomizer = new InstancesRandomizer(normalizedData, 70.0);
			normalizedTrainingData = randomizer.getTrainInstances();
			normalizedTestData = randomizer.getTestInstances();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro ao carregar Base de Dados");
		}
	}
	
	public boolean getDataBaseUp(){
		if(database == null){
			return false;
		}
		return true;
	}
	
	public Instances getNormalizedData(){
		return normalizedData;
	}
	
	public Instances getNormalizedTrainingData(){
		return normalizedTrainingData;
	}
	
	public Instances getNormalizedTestData(){
		return normalizedTestData;
	}
	
	public void revertBase(List<Instance> group){
		normalized.reverteBase(group);
	}

	public Instances getDatabase() {
		return database;
	}

	public void setDatabase(Instances database) {
		this.database = database;
	}
	
	public int getCountAttribute(){
		return database.numAttributes();
	}
	
	public int getCountInstances(){
		return database.numInstances();
	}
	
	public Attribute getAttribute(int indice){
		return database.attribute(indice);
	}
	
	public Attribute getAttribute(String nome){
		return database.attribute(nome);
	}

	public Instances getDataEvalBase() {
		return dataEvalBase;
	}

	public void setDataEvalBase(Instances dataEvalBase) {
		this.dataEvalBase = dataEvalBase;
	}
}
