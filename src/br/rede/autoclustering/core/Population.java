/*
 * Created on 01-Aug-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.rede.autoclustering.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import br.rede.autoclustering.util.ParameterOptions;
import br.rede.autoclustering.util.SortedList;
import br.rede.autoclustering.util.ThreadPool;

/**
 * @author ag227
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Population {
    
    private SortedList<Individual> individual;
    private ThreadPool threadPool;
    public Population(ProbDag probdag, int size) {
    	threadPool = new ThreadPool(20);
        individual = new SortedList<Individual>();
        for (int i=0; i< size;i++){
        	Individual ind = new Individual(probdag);
            individual.add(ind);
            threadPool.runTask(ind);
        }
        threadPool.join();
    }

	public synchronized void cheat(){
    	notify();
    }
    
    public int getPopSize() {
        return individual.size();
    }

    public Individual bestIndividual() {
    	Collections.sort(individual);
        return individual.get(0);
    }

    public void addIndividual(Individual r) {
        individual.add(r);
        individual.remove(individual.size()-1);
    }

	/**
	 * @return Returns the individual.
	 */
	public SortedList<Individual> getIndividual() {
		return individual;
	}
	/**
	 * @param individual The individual to set.
	 */
	public void setIndividual(SortedList<Individual> individual) {
		this.individual = individual;
	}
    public double getMediumFitness(){
    	Iterator<Individual> it = individual.iterator();
        int num = 0;
        double fitMedia = 0.0F;
        while (it.hasNext()) {
        	Individual r = (Individual) it.next();
            num++;
            fitMedia += r.fitness(false);
        }
        return fitMedia / num;
    }
    
	/**
	 * 
	 * Samuel Refactoring : This method was wrong allowing tot = 0. It will throw an exception when Something / tot !
	 * Fixing
	 * @param pop
	 * @param e
	 * @param totFitness
	 * @return
	 */
	public EdgeProbability edgeProbability(Edge e) {
		Iterator<Individual> itInd = individual.iterator();
		EdgeProbability fit = new EdgeProbability();
		int tot = 0, over = 0, opt = 0;
		int dist[] = new int[Edge.NUM_DISTANCES];
		Map<Parameter, Integer[]> parametersValues = new HashMap<Parameter, Integer[]>();
		for (Parameter p : e.getProbParameters().keySet()){
			parametersValues.put(p, new Integer[ParameterOptions.getParameterValues(p).length]);
			Arrays.fill(parametersValues.get(p), 0);
		}
		for (int i = 0; i < individual.size(); i++) {
			Individual individual = (Individual) itInd.next(); 
			if (individual.contains(e.getNodeIn()) && individual.contains( e.getNodeOut()) ) {
				tot++;
				IndividualNode nodeOut = individual.getIndividualNode(e.getNodeOut());
				if (nodeOut.isOptionalK()) 
					opt++;
				if (nodeOut.isOverlap()) 
					over++;
				dist[nodeOut.getDistanceType().getType()]++;
				for ( Parameter p : nodeOut.getProperties().keySet() ) {
					int position = ParameterOptions.getKeyFromValue(p,nodeOut.getProperties().get(p));
					parametersValues.get(p)[position]++;
				}
			}
		}
		double[] distProb = new double[Edge.NUM_DISTANCES];
		if ( tot != 0 ) {
			fit.setTotalProb(tot/individual.size());
			fit.setOptinalKProb(opt/tot);
			fit.setOverlapProb(over/tot);
			for( int j = 0 ;j < Edge.NUM_DISTANCES ; j++ )
				distProb[j] = dist[j] / tot;
		}
//		else
//			for( int j = 0 ;j < Edge.NUM_DISTANCES ; j++ )
//				distProb[j] = 0;
		fit.setDistanceProb(distProb);
		return fit;
	}
	

/*    
    public String toString()
    {
        String populacao = new String();
        Iterator it = regra.iterator();
        int num = 0;
        float fitMedia = 0.0F;
        while (it.hasNext())
        {
            Regra r = (Regra) it.next();
            populacao = populacao + num++ + ":" + r + "\n";
            fitMedia = fitMedia + r.getMatriz().fitness();
        }
        return "#Fitness Media#" + fitMedia / num + "\n" + populacao;
    }

*/
	

}
