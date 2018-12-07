package br.rede.autoclustering.structures.groups;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Elton
 */
public class AreaDense extends Group {

    private List<Instance> instances;
    private Map<Instance,List<Instance>> map;

    public AreaDense() {
        instances = new ArrayList<Instance>();
        map = new HashMap<Instance, List<Instance>>();
    }

    public void setGroup(List<Instance> agrupamento) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Instance getKey() {
        return map.keySet().iterator().next();
    }

    public void addInstance(Instance data) {
        instances.add(data);
    }

    public void addAllInstance(Group group) {
        instances.addAll(group.getInstances());
    }

    public void addAllInstance(Instances data) {
      instances.addAll(instances);
    }

    public void removeInstance(Instance data) {
        instances.remove(data);
    }

    public void setKey(Instance key) {
        map.put(key, instances);
    }

    public int compareTo(Group o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public java.util.Set<Instance> getInstances() {
    	super.getInstances().addAll(this.instances);
    	return super.getInstances();
    }
}
