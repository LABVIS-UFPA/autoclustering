package br.rede.autoclustering.util;

import java.util.ArrayList;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.structures.groups.HyperShell;
import br.rede.autoclustering.structures.groups.HyperSphere;
import br.rede.autoclustering.structures.tree.NodoAtractor;

public class MathProcess {

	public double calculateInfluence(Instance first, Instance second,
			double sigma) {

		DistanceMeasures measures = new DistanceMeasures();
		double distance = measures.calculateDistance(first, second,
				DistanceType.EUCLIDEAN);

		
		if (distance == 0)
			return 0; 
		double exponent = -Math.pow(distance, 2) / (2.0 * Math.pow(sigma, 2));
		double influence = Math.exp(exponent);
		return influence;
	}

	public double getDensity(Instance objeto, Instances objetos,
			float threshold, double sigma) {

		HyperSphere hypersphere = new HyperSphere(objetos, objeto, threshold);

		ArrayList<HyperShell> hypershells = (ArrayList<HyperShell>) hypersphere
				.getHypershells();

		float density = 0;

		for (int i = 0; i < hypershells.size() - 1; i++) {

			double k = hypershells.get(i + 1).getRadius();

			density += (double) hypershells.get(i + 1).Normalization() * k;
		}

		objeto.setWeight(density);

		return (density);
	}

	public double getDensity(Instance objeto, Instances objetos, double sigma) {

		double density = 0;
		for (int i = 0; i < objetos.numInstances(); i++) {

			Instance Oj = objetos.instance(i);
			if (objeto != Oj)
				density += calculateInfluence(objeto, Oj, sigma);
		}

		return density;
	}

	public double getSimilarity(Instance object1, Instance object2) {

		DistanceMeasures measures = new DistanceMeasures();

		return (measures.calculateDistance(object1, object2,
				DistanceType.EUCLIDEAN));

	}

	public double getDistance(Instance object1, Instance object2) {

		double aux = getSimilarity(object1, object2);

		if (aux > 0) {
			return (double) aux;
		} else {
			return Double.POSITIVE_INFINITY;
		}
	}

	public List<Instance> getNeighbors(Instance center, List<Instance> objetos,
			float threshold) {
		ArrayList<Instance> neighbors = new ArrayList<Instance>();
		/* NEPS(P) = {q∈D| dist(p,q) <= Eps} */

		for (Instance node : objetos) {

			if (getDistance(center, node) <= threshold
					&& getDistance(center, node) >= 0.4) {

				neighbors.add(node);
			}
		}
		return neighbors;
	}

	public List<NodoAtractor> getNeighbors(NodoAtractor Oi, List<NodoAtractor> objetos, float threshold) {

		List<NodoAtractor> neighbors = new ArrayList<NodoAtractor>();
		List<NodoAtractor> atraidos = new ArrayList<NodoAtractor>();
			
		List<NodoAtractor> grupo = projetaGrupo(objetos, Oi, threshold);
		objetos.remove(Oi);

		
		for(int i =0;i < grupo.size();i++ )
		{			
			NodoAtractor Oj = grupo.get(i);
							
				if (Oi.getInstance() == Oj.getAtrator().getInstance()) {/* OI<-Oj */
						atraidos.add(Oj);					
				} else if ((1 / getDistance(Oi.getInstance(), Oj.getInstance())) >= 0.3) {
						atraidos.add(Oj);
			}			
		}
			
		if (atraidos.size() != 0 && !atraidos.isEmpty()) {

			if (objetos.removeAll(atraidos)) {
				for (NodoAtractor nodo : atraidos) {
					neighbors.addAll(getNeighbors(nodo, objetos, threshold));
				}
			}
		}

		neighbors.add(Oi);
		

		return neighbors;
	}
	
	

	private List<NodoAtractor> projetaGrupo(List<NodoAtractor> nodos, NodoAtractor center,
			float threshold) {
		
		List<NodoAtractor> neighbors = new ArrayList<NodoAtractor>();
		/* NEPS(P) = {q∈D| dist(p,q) <= Eps} */

		for (int i=0;i<nodos.size();i++) {
			NodoAtractor node = nodos.get(i);
			if(node.getInstance() != center.getInstance())
			if (getDistance(center.getInstance(), node.getInstance()) <= threshold) {
				neighbors.add(node);
			}
		}
		return neighbors;
	}

	private List<Instance> setGrupo(Instance Oi, Instances instances) {
		List<Instance> Set = new ArrayList<Instance>();

		for (int i = 0; i < instances.numInstances(); i++) {
			Instance Oj = instances.instance(i);

			if (Oj != Oi) {
				if (Oj.weight() > Oi.weight())
					Set.add(Oj);
			}

		}

		return Set;
	}
	
	private List<Instance> setGrupo(Instance Oi,List<Instance> instances) {
		List<Instance> Set = new ArrayList<Instance>();

		for (int i = 0; i < instances.size(); i++) {
			
			Instance Oj = instances.get(i);
			if (Oj != Oi) {
				if (Oj.weight() > Oi.weight())
					Set.add(Oj);
			}

		}

		return Set;
	}

	public Instance getAttrator(Instance Oi, Instances instances, double sigma) {
		List<Instance> Set = setGrupo(Oi, instances);
		double maior = 0;
		Instance attrator = Oi;

		if (Set.isEmpty()) {
		
			return attrator;
		}
		for (int i = 0; i < Set.size(); i++) {
			double influencia = calculateInfluence(Oi, Set.get(i), sigma);

			if (influencia > maior) {
				attrator = Set.get(i);
				maior = influencia;
			}
		}

		return attrator;

	}
	
	public Instance getAttrator(Instance Oi, List<Instance> instances, double sigma) {
		List<Instance> Set = setGrupo(Oi, instances);
		double maior = 0;
		Instance attrator = Oi;

		if (Set.isEmpty()) {
			return attrator;
		}
		for (int i = 0; i < Set.size(); i++) {
			double influencia = calculateInfluence(Oi, Set.get(i), sigma);

			if (influencia > maior) {
				attrator = Set.get(i);
				maior = influencia;
			}
		}

		return attrator;

	}
	
	

}
