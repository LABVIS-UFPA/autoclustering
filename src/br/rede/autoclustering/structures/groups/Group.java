package br.rede.autoclustering.structures.groups;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import weka.core.Instance;

public class Group {
	
    private Instance key;
    private double density;
    private Set<Instance> instances;

    public Group(){
            this.instances = new HashSet<Instance>();
    } 

    public Set<Instance> getInstances(){
            return this.instances;
    }
    
    public void setInstances(Set<Instance> instances) {
        this.instances = instances;
    }

    public Instance getKey(){
            return this.key;
    }

    public void addInstance(Instance data){
            this.instances.add(data);
    }

    public void addAllInstance(Group group){
            this.instances.addAll(group.getInstances());
    }
    public void addAllInstance(Collection<Instance> instances){
    	this.instances.addAll(instances);
    }
    public void removeInstance(Instance data){
            this.instances.remove(data);
    }

    public void setKey(Instance key) {
            this.key = key;
    }
    
    public void addInstances(Collection<Instance> instance){
    	instances.addAll(instance);
    }

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}
    
}
