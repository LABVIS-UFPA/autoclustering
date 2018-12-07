package br.rede.autoclustering.algorithms.dhc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.rede.autoclustering.core.ClusteringMethod;
import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.structures.groups.AreaDense;
import br.rede.autoclustering.structures.groups.Group;
import br.rede.autoclustering.structures.tree.NodoAtractor;
import br.rede.autoclustering.util.MathProcess;

public class DensityTree implements ClusteringMethod {

	private List<AreaDense> areaDenses;

	public List<AreaDense> calculateDensityTree(List<NodoAtractor> ns,int minPts,float threshold) {

		AreaDense areaDense;
		int i = 0;
		List<NodoAtractor> nodos = ns;
		MathProcess mathProcess = new MathProcess();

		while (!nodos.isEmpty()) {

			if (nodos.get(i) != null) {/*caso for nullo vai para outro*/ 
				List<NodoAtractor> nodes = mathProcess.getNeighbors(nodos.get(i),nodos, threshold);
				
				if ((nodes.size() >= minPts)) {// Minpts
					areaDense = new AreaDense();
 
					for (NodoAtractor objeto : nodes)
						areaDense.addInstance(objeto.getInstance());					
					nodos.removeAll(nodes);
					areaDenses.add(areaDense);
				
				}
				else 
				{
					areaDense = new AreaDense();
					
					for(NodoAtractor nodo:nodes)
						{
						 areaDense.addInstance(nodo.getInstance());
						 nodos.remove(nodo);
						}					
					areaDenses.add(areaDense);
				}
			}
						
		}

		return areaDenses;

	}

	public List<Group> getTree(List<NodoAtractor> objetos, Integer minPts,
			float threshold) {

		List<Group> groups = new ArrayList<Group>();
		

		areaDenses = new ArrayList<AreaDense>();

		for (AreaDense areaDense : calculateDensityTree(objetos,minPts,threshold)) {

			groups.add(areaDense);
		}

		return groups;
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void executeStep(Map<Parameter, Object> sharedObjects) {
		if (isReady(sharedObjects)) {
			
		
			Integer minPts = ((Number) sharedObjects.get(Parameter.DHC_MINPTS)).intValue();
			float threshold = ((Number) sharedObjects.get(Parameter.DHC_THRESHOLD)).floatValue();

			List<NodoAtractor> grupo = (List<NodoAtractor>) sharedObjects.get(Parameter.DHC_LIST_INSTANCE);
			List<NodoAtractor> nodos = (List<NodoAtractor>) sharedObjects.get(Parameter.DHC_ATTRACTION_TREE);

			if (grupo == null) {
				sharedObjects.put(Parameter.ALL_GROUPS, getTree(nodos, minPts,threshold));
			} else {
				 sharedObjects.put(Parameter.ALL_GROUPS, getTree(grupo, minPts,threshold));
			}

		}

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "DensityTree";
	}

	@Override
	public boolean isReady(Map<Parameter, Object> sharedObjects) {
		if ((sharedObjects.get(Parameter.DHC_ATTRACTION_TREE) != null || sharedObjects
				.get(Parameter.DHC_LIST_INSTANCE) != null)
				&& sharedObjects.get(Parameter.DHC_MINPTS) != null
				&& sharedObjects.get(Parameter.DHC_THRESHOLD) != null)
			return true;
		return false;
	}

	@Override
	public String technicalInformation() {
		// TODO Auto-generated method stub
		return "This method aims to create a Density Tree";
	}
}
