package br.rede.autoclustering.structures.grid;


import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Instance;
import br.rede.autoclustering.structures.groups.Group;

/**
 * 
 * It is a set of entities whose dimension's ranges are the same
 * 
 * @author samuel
 *
 */
public class Unit {
	
	private Map<Attribute,Interval> attributes = new HashMap<Attribute,Interval>();
	private Set<Instance> entities = new HashSet<Instance>();
	private Group cluster;
	
	/**
	 * Create an unit using another unit's attributes such attributes and entities
	 * @param unit
	 */
	public Unit(Unit unit) {
		this.attributes.putAll(unit.getAttributes());
		this.entities.addAll(unit.getInstances());
	}
	
	/**
	 * Create an unit using the following parameters
	 * @param range
	 * @param entities
	 * @param attribute
	 */
	public Unit(Interval range, List<Instance> entities, Attribute attribute) {
		this.entities.addAll(entities);
		this.attributes.put(attribute, range);
	}
	
	/**
	 * Retain the entities of the {@link Unit}
	 * @param unit
	 */
	public void retainEntities(Unit unit){
		this.entities.retainAll(unit.getInstances());
		for ( Attribute e : unit.getAttributes().keySet() )
			attributes.put(e, unit.getAttributes().get(e));
	}

	public boolean checkLeftNeighbor(Unit unit, Attribute a){
		if ( !checkK1(unit, a) )
			return false;
		if ( attributes.get(a).getMin() == unit.getAttributes().get(a).getMax() ) 
			return true;
		return false;
	}
	
	public boolean checkRightNeighbor(Unit unit, Attribute a){
		if ( !checkK1(unit, a) )
			return false;
		if ( attributes.get(a).getMax() == unit.getAttributes().get(a).getMin() ) 
			return true;
		return false;
	}
	
	public boolean checkK1(Unit unit, Attribute a){
		for ( Attribute chosen : attributes.keySet() ){
			if (chosen == a)
				continue;
			else if (attributes.get(chosen).equals(unit.getAttributes().get(chosen) ))
				continue;
			else
				return false;
		}
		return true;
	}
	
	public Map<Attribute,Interval> getAttributes() {
		return attributes;
	}
	
	public int getDimensionality() {
		return attributes.size();
	}
	
	public float getSelectivity(long totalPopulation) {
		return (float) this.entities.size() / totalPopulation;
	}
	
	public Set<Instance> getInstances() {
		return entities;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for ( Attribute d : attributes.keySet() )
			sb.append(d.name()).append(" ");
		return sb.toString();
	}

	public Group getCluster() {
		return cluster;
	}

	public void setCluster(Group cluster) {
		this.cluster = cluster;
	}
	
	public Comparator<Attribute> getAttributeComparator() {
		Comparator<Attribute> comp = new Comparator<Attribute>() {

			@Override
			public int compare(Attribute o1, Attribute o2) {
				return o1.name().compareTo(o2.name());
			}
			
		};
		return comp;
	}

	
}
