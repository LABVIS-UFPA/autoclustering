package br.rede.autoclustering.core;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.util.ClusterViewerFrame;
import br.rede.autoclustering.util.DistanceType;
import br.rede.autoclustering.util.ParameterOptions;

/**
 * 
 * @author aruanda
 *
 */
public class Individual implements Comparable<Individual>, Runnable{
	
	private List<IndividualNode> nodes = new ArrayList<IndividualNode>();
	private double fitness;
	private List<Group> groups;
	//It's necessary to keep those instances because of the multithread processing. 
	//When the best algorithm is found. The original instances will not be the same 
	private Instances instances;
	private ProbDag probDag;
	
	public Individual(ProbDag probDag) {
		this.probDag = probDag;
	}
	
	public double fitness(boolean visualizate)	{
		
		StringBuffer output = new StringBuffer();
		IndividualNode initialNode = nodes.get(0);
		Map<Parameter, Object> sharedObjects = new HashMap<Parameter, Object>();
		sharedObjects.putAll(probDag.getSharedObjects());
		sharedObjects.putAll(initialNode.getProperties());
			
		// Initial Parameters
		for ( Parameter parameter : initialNode.getProperties().keySet()) 
			sharedObjects.put(parameter, initialNode.getProperties().get(parameter));
		sharedObjects.put(Parameter.ALL_DISTANCE, nodes.get(1).getDistanceType());
		
		Instances inst = (Instances) sharedObjects.get(Parameter.ALL_INSTANCES);
		instances = new Instances(inst);
		sharedObjects.put(Parameter.ALL_INSTANCES, instances);
		for (int i = 1; i < nodes.size(); i++) {
			IndividualNode node = nodes.get(i);
			sharedObjects.put(Parameter.ALL_DISTANCE,node.getDistanceType());
			for ( Parameter p : node.getProperties().keySet() ) 
				if ( sharedObjects.get(p) == null)
					sharedObjects.put(p, node.getProperties().get(p));
				else
					node.getProperties().put(p, ((Number) sharedObjects.get(p)).floatValue());

		}
		
		for (int i = 0; i < nodes.size(); i++) {
			IndividualNode node = nodes.get(i);
			ClusteringMethod method = node.getNode().getClusteringMethod();
			if ( Eda.isVerbose() ) {
				output.append("running ("+i+") "+method.getName()+"\r\n");
				for ( Parameter p : sharedObjects.keySet() ) 
					if ( p != Parameter.ALL_INSTANCES && p != Parameter.DHC_ATTRACTION_TREE )
						output.append("\t"+p + ": "+ sharedObjects.get(p)+"\r\n");
				System.out.println(output);
			}
			method.executeStep(sharedObjects);
		}
		
		//Pegar a msm instancia do Instances do usuário
		groups = (List<Group>) sharedObjects.get(Parameter.ALL_GROUPS);
		if ( groups != null && !groups.isEmpty()) {
			performCleaning(groups);
			if ( visualizate ) {
				ClusteringMethod lastMethod = nodes.get(nodes.size()-1).getNode().getClusteringMethod();
				ClusterViewerFrame f = new ClusterViewerFrame(instances, groups);
				f.setVisible(true);
			}
				double fitness = evaluate(instances, groups);
				if ( fitness != 0 ) {
					fitness = 100 * (1-fitness);
					if (Double.isNaN(fitness))
						fitness = 0;
					return fitness;
				}else 
					return 0;
				
			}
		return 0;
	}
	
	public double evaluate(Instances instances,List<Group> groups) {
		if ( groups.size() == 1 || groups.isEmpty() )
			return 0;
		FastVector fastVector = new FastVector(groups.size());
		for (int i = 0; i < groups.size(); i++) 
			fastVector.addElement("C"+(i<10?"0"+i:i));//String.valueOf(((char)(i+65)))
		Attribute cluster = new Attribute("cluster", fastVector);
		instances.insertAttributeAt(cluster, instances.numAttributes());
		
		for (int i = 0; i < groups.size(); i++) {
			for ( Instance instance : groups.get(i).getInstances() ) {
				instance.setDataset(instances);
				instance.setValue( instance.numAttributes()-1, cluster.value(i));
			}
		}
		instances.setClassIndex(instances.numAttributes()-1);
		
		Evaluation evaluation = null;
		try {
			evaluation = new Evaluation(instances);
			probDag.getClassifier().buildClassifier(instances);
			evaluation.crossValidateModel(probDag.getClassifier(), instances, 10, instances.getRandomNumberGenerator(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double fitness = evaluation.rootMeanSquaredError();//errorRate();//meanAbsoluteError();
		
		instances.setClassIndex(-1);
		instances.deleteAttributeAt(instances.numAttributes()-1);
		return fitness;
	}
	
	
	/**
	 * Remove all groups with only one instance 
	 * @param groups
	 */
	public void performCleaning(List<Group> groups){
		for (int i = 0; i < groups.size(); i++) {
			if ( groups.get(i).getInstances().size() == 1 )
				groups.remove(i);
		}
	}	
	
    public int compareTo(Individual r) {
        if (this.getFitness() > r.getFitness())
            return -1;
        if (this.getFitness() < r.getFitness())
            return 1;
    	return 0;
    }
    
    public IndividualNode getIndividualNode(Node n) {
    	Iterator<IndividualNode> it = nodes.iterator();
    	for(int i=0;i<nodes.size();i++){
    		IndividualNode iNode = it.next();
    		if(iNode.getNode().equals(n)) return iNode;
    	}
    	return null;
    }

    public boolean contains(Node n) {
    	Iterator<IndividualNode> it = nodes.iterator();
    	for(int i=0;i<nodes.size();i++){
    		IndividualNode iNode = it.next();
    		if(iNode.getNode().equals(n)) return true;
    	}
    	return false;
    }

    public List<IndividualNode> getNodes() {
		return nodes;
	}
    
	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public void execute(){
		
		Random r = new Random();
//		r.setSeed(1);
		nodes.add(new IndividualNode(probDag.getInitialNode()));
		for (int i=0; nodes.get(i).getNode().getEdgesOut().size()>0; i++) {
			float probEdge = r.nextFloat();
			float totProb = 0f;
			Iterator<Edge> it = nodes.get(i).getNode().getEdgesOut().iterator();
			Edge selEdge = null; 
			while (it.hasNext() && totProb < probEdge){
				 selEdge = (Edge) it.next();
				 totProb += selEdge.getProbability();
			}
			IndividualNode newNode = new IndividualNode(selEdge.getNodeOut());
			newNode.setOptionalK(selEdge.getProbOptionalK() > r.nextFloat() ? true : false);
			newNode.setOverlap(selEdge.getProbOverlap() > r.nextFloat() ? true : false);
			float probDistance = r.nextFloat();
			float totProbDistance = 0f;
			int distanceType;
			for( distanceType=0; distanceType<Edge.NUM_DISTANCES && totProbDistance < probDistance; distanceType++) 
				totProbDistance += selEdge.getProbDistance()[distanceType];
			
			float[] totProbParameters = new float[selEdge.getNodeOut().getParameters().size()];
			int[] parameterType = new int[selEdge.getNodeOut().getParameters().size()];
			for( int j = 0 ; j < totProbParameters.length ; j++){
				float probParameter = r.nextFloat();
				totProbParameters[j] = 0f;
				Parameter p = selEdge.getNodeOut().getParameters().get(j);
				int NUM_PARAMETER = ParameterOptions.getParameterValues(p).length-1;
				for( parameterType[j] = 0; parameterType[j] < NUM_PARAMETER && 
						totProbParameters[j] < probParameter; parameterType[j]++){
					totProbParameters[j] += selEdge.getProbParameters().get(p)[parameterType[j]];
				}
				newNode.putProperties(p, ParameterOptions.getParameterValue(p, parameterType[j]));
			}
			newNode.setDistanceType(DistanceType.getDistanceType(distanceType));
			nodes.add(newNode); 
		}
		this.setFitness(this.fitness(false));
	}
	
	public List<Group> getGroups() {
		return groups;
	}

	@Override
	public void run() {
		execute();
	}
	
	public Instances getInstances() {
		//some algorithms use this weight value, so we have to clean it
		for (int i = 0; i < instances.numInstances(); i++) {
			instances.instance(i).setWeight(1);
		}
		return instances;
	}
}
