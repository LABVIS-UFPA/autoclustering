package br.rede.autoclustering.structures.tree;

import weka.core.Instance;

public class NodoAtractor {

	NodoAtractor atrator;
		Instance instance;
		
	public NodoAtractor(Instance instance) {
		this.instance = instance;
	}
	
	public NodoAtractor() {
		
	}
	
	public NodoAtractor getAtrator() {
		return atrator;
	}
	public void setAtrator(NodoAtractor atrator) {
		this.atrator = atrator;
	}
	public Instance getInstance() {
		return instance;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
	}	
	
	public boolean isAtrator()
	{
		if(this.getAtrator()!=null)
			return true;
		return false;
	}	
	
}
