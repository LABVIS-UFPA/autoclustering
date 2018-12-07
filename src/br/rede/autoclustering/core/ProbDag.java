/*
 * Created on 01-Aug-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.rede.autoclustering.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.rede.autoclustering.algorithms.InitialNode;

import weka.classifiers.Classifier;
import weka.core.Instances;




/**
 * @author ag227
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProbDag {

	private Node initialNode;
	private Classifier classifier;
	private Map<Parameter, Object> sharedObjects = new HashMap<Parameter, Object>();
	
	public ProbDag(Collection<Node> nodes, Instances instances, Classifier classifier, int slices) throws Exception {
		this.classifier = classifier;
		for ( Node node : nodes) {
			if ( node.getClusteringMethod() instanceof InitialNode ) {
				initialNode = node;
				break;
			}
		}
		if ( initialNode == null )
			throw new Exception("InitialNode not found! Check your config.xml file!");
		sharedObjects.put(Parameter.ALL_INSTANCES, instances);
		initialNode.getClusteringMethod().executeStep(sharedObjects);
		initProbabilities(initialNode);
	}
	
	
	private void initProbabilities(Node n) {
		Iterator<Edge> it = n.getEdgesOut().iterator();
		int noEdges = n.getEdgesOut().size();
		for (int i=0; i< noEdges ;i++){
			Edge e = (Edge) it.next();
			e.setProbability(1f/noEdges);
			initProbabilities(e.getNodeOut());
		}
	}

	
	/**
	 * @return Returns the initialNode.
	 */
	public Node getInitialNode() {
		return initialNode;
	}
	/**
	 * @param initialNode The initialNode to set.
	 */
	public void setInitialNode(Node initialNode) {
		this.initialNode = initialNode;
	}

	public float totalProbability(Node n) {
	    float prob = 0.0F;
		Iterator<Edge> it = n.getEdgesOut().iterator();
		int noEdges = n.getEdgesOut().size();
		for (int i=0; i< noEdges ;i++){
			Edge e = (Edge) it.next();
			prob = prob + e.getProbability();
		}
	    return prob;
	}

	public void normalizeProbabilities() {
		this.normalizeProbabilities(initialNode);
	}
	
	public void normalizeProbabilities(Node n) {
	    float totprob = this.totalProbability(n);
		Iterator<Edge> it = n.getEdgesOut().iterator();
		int noEdges = n.getEdgesOut().size();
		for (int i=0; i< noEdges ;i++){
			Edge e = (Edge) it.next();
			e.setProbability(e.getProbability() / totprob);
			normalizeProbabilities(e.getNodeOut());
	    }
		//*** normalizar prob detalhadas
	}
	
	public void estimateProbability(Population pop) {
		estimateProbability(initialNode, pop);
    }
	
	public void estimateProbability(Node n, Population pop) {
		//For each edge from initialNode, get Edge probability
		//Verify influence from past generation using alpha
		//update probabilities
		List<Edge> edges = n.getEdgesOut();
        for (Edge edge : edges){
            EdgeProbability eProb = pop.edgeProbability(edge);
            edge.setProbability(Eda.pbil(eProb.getTotalProb(), edge.getProbability()));
			edge.setProbOptionalK(Eda.pbil(eProb.getOptinalKProb(), edge.getProbOptionalK()));
			edge.setProbOverlap(Eda.pbil(eProb.getOverlapProb(), edge.getProbOverlap()));
			double[] dist = new double[Edge.NUM_DISTANCES];
			for(int i=0;i<Edge.NUM_DISTANCES;i++)
				dist[i]=Eda.pbil(eProb.getDistanceProb()[i], edge.getProbDistance()[i]);
			estimateProbability(edge.getNodeOut(),pop);
        }
        this.normalizeProbabilities(); 
    }


	private static int percorreDag(Node n, int part)	{
		Set<Parameter> parameters = new HashSet<Parameter>();
		parameters.addAll(n.getParameters());
		int tot = 1;
		for (int i=0;i<n.getEdgesOut().size();i++)
			tot += percorreDag( n.getEdgesOut().get(i).getNodeOut() , part);
		tot *= part * parameters.size();
//		tot *= part;
//		if(n.isSupportOptionalK()) 
//			tot*=2;
//		if(n.isSupportOptionalOverlap()) 
//			tot*=2;
		return tot*Edge.NUM_DISTANCES;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public Map<Parameter, Object> getSharedObjects() {
		return sharedObjects;
	}
	
//	public static void main(String[] args) {
//		// conta opcoes
//		
//		ProbDag prob = new ProbDag();
//		System.out.println("Possibilidades: " + percorreDag(prob.getInitialNode()));
//	}
	
}
