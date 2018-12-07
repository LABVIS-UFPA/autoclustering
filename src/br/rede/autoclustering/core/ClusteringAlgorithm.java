/*
 * Created on 04-Aug-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.rede.autoclustering.core;

import java.util.Arrays;

import br.rede.autoclustering.algorithms.clique.ClustersByPartition;
import br.rede.autoclustering.algorithms.clique.DenseAreas;
import br.rede.autoclustering.algorithms.dbclasd.ClustersByDistribution;
import br.rede.autoclustering.algorithms.denclue.ASH;
import br.rede.autoclustering.structures.grid.HyperSpace;
import br.rede.autoclustering.structures.groups.Cluster;
import br.rede.autoclustering.structures.groups.Subspace;
import br.rede.autoclustering.util.DataBaseStructure;

import weka.core.Instances;

/**
 * @author ag227
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class ClusteringAlgorithm {

	// TODO Adicionar CandidatesByNpts e ClustersAMR

	/**
	 * PARAMETROS
	 */

	public static void main(String[] args) {
		System.out.println("Executando main em ClusteringAlgorithm");
		// ***CODE
	}

	public void criaClusterDistancia() {
		System.out.println("criaClusterDistancia - Candidates");
	}

	public void criaArvoreKDAdap() {
		System.out.println("criaArvoreKDAdap");
	}

	public void criaGradeTamFixo() {
		System.out.println("criaGradeTamFixo");
	}

	public void identifyDenseAreas() {
		System.out.println("identifyDenseAreas");
//		float threshold = 0.001f;
//		int partitions = 30;
//		try {
//			Instances instances = DataBaseStructure.getInstance().getDatabase();
//			Subspace[] subspaces = new Subspace[instances.numAttributes()];
//			int count = 0;
//	 		for (int i = 0; i < subspaces.length; i++) 
//				subspaces[count++] = DenseAreas.identifyDenseUnits(instances, instances.attribute(i), partitions, threshold);
//	 		Arrays.sort(subspaces);
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.out.println("Erro ao identificar areas densas");
//		}
	}

	public void criaClusterAttractor() {
		System.out.println("criaClusterAtrator");
//		IdentifyAttractors.determineAttractorsGroup("");
	}

	public void criaASH() {
		System.out.println("criaASH");
	}

	public void criaArvoreAtracao() {
		System.out.println("criaArvoreAtracao");
	}

	public void criaArvoreAMR() {
		System.out.println("criaArvoreAMR");
	}

	public void createPartitions() {
		System.out.println("criaParticoes");
//		Subspace[] subspaces = null;
//		float threshold = 0.01f;
//		Instances instances = DataBaseStructure.getInstance().getDatabase();
//		subspaces = ClusterByPartition.joinSubspaces(subspaces, instances.numInstances(), threshold);
// 		while ( subspaces != null ){
// 			Arrays.sort(subspaces);
// 			subspaces = ClusterByPartition.joinSubspaces(subspaces, instances.numInstances(), threshold);
// 		}
	}

	public void criaArvoreDensidade() {
		System.out.println("criaArvoreDensidade");
	}

	public void validateDistribution(Cluster c, double volume) {
		System.out.println("validaDistrib");
//		return ClustersByDistribution.validateProbabilityDistribution(c, new double[]{volume});
	}

	public void validaAlcance() {
		System.out.println("validaAlcance");
	}

	public void aglomeraSobrepos() {
		System.out.println("aglomeraSobrepos");
	}

	public void aglomeraDistancia() {
		System.out.println("aglomeraDistancia");
	}
}
