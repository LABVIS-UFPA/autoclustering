package br.rede.autoclustering.structures.groups;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import br.rede.autoclustering.algorithms.denclue.DenclueFunctions;
import br.rede.autoclustering.structures.grid.Unit;


import weka.core.Attribute;
import weka.core.Instance;


/**
 * 
 * @author samuel
 *
 */
public class Subspace implements Comparable<Subspace>{
	
	private Comparator<Attribute> attComparator = new Comparator<Attribute>() {
		@Override
		public int compare(Attribute o1, Attribute o2) {
			return o1.name().compareTo(o2.name());
		}
	}; 
	private List<Unit> units = new ArrayList<Unit>();
	private SortedSet<Attribute> attributes = new TreeSet<Attribute>(attComparator);
	private int dimensionality = 0;
	
	public Subspace(SortedSet<Attribute> dimensions) {
		this.attributes.addAll(dimensions);
		this.dimensionality = dimensions.size();
	}
	
	public SortedSet<Attribute> joinDimensions(Subspace other) {
	    SortedSet<Attribute> otherDimensions = other.getAttributes();
	    //Collections.sort(otherDimensions,attComparator);
	    
	    if (this.attributes.size() != otherDimensions.size())
	      throw new IllegalArgumentException("Different Dimensions Sizes!");
	    
	    if (attComparator.compare(this.attributes.last(), otherDimensions.last()) >= 0)
	      return null;

	    SortedSet<Attribute> result = new TreeSet<Attribute>(attComparator);
	    Iterator<Attribute> it1 = this.attributes.iterator();
	    Iterator<Attribute> it2 = otherDimensions.iterator();
	    for (int i = 0; i < this.attributes.size() - 1; i++) {
	      Attribute dim1 = it1.next();
	      Attribute dim2 = it2.next();
	      if (!dim1.equals(dim2)) return null;
	      result.add(dim1);
	    }

	    result.add(it1.next());
	    result.add(it2.next());
	    return result;

	}
	
	/**
	 * 
	 * @param units
	 * @param population
	 * @param threshold
	 */
	public Subspace subspaceJoin(Subspace subspace, long population, float threshold){
		
	    if (this.attributes.size() != subspace.getAttributes().size())
		      throw new IllegalArgumentException("Different Dimensions Sizes!");
	    
	    SortedSet<Attribute> candidateDimension = joinDimensions(subspace);
	    if ( candidateDimension != null ) {
	    	Subspace candidateSubspace = new Subspace(candidateDimension);
	    	for (Unit u : units) { 
	    		for ( Unit u1 : subspace.getUnits() )
	    			if ( u != u1 ){
	    				Unit candidate = compareUnits(u, u1, population, threshold);
	    				if (candidate != null)
	    					candidateSubspace.addUnit(candidate);
	    			}
	    	}
	    	if ( candidateSubspace.getUnits().size() == 0 )
	    		return null;
	    	return candidateSubspace;	
	    }
	    else
	    	return null;
		
	}
	
    
	
	/**
	 * It only works if u != u1
	 * @param u
	 * @param u1
	 */
	private Unit compareUnits(Unit u, Unit u1,  long total, float threshold) {
		SortedSet<Attribute> ss1 = new TreeSet<Attribute>(attComparator);
		ss1.addAll(u.getAttributes().keySet());
		SortedSet<Attribute> ss2 = new TreeSet<Attribute>(attComparator);
		ss2.addAll(u1.getAttributes().keySet());
		Iterator<Attribute> i = ss1.iterator();
		Iterator<Attribute> j = ss2.iterator();
		
		while ( i.hasNext() ) {
			Attribute a = i.next();
			Attribute b = j.next();
			if ( i.hasNext() && a == b ) {
				continue;
			}
			//Check for the threshold
			else if ( attComparator.compare(a, b) < 0 ){
				Unit intersection = new Unit(u);
				intersection.retainEntities(u1);
				if ( intersection.getSelectivity(total) >= threshold ){
					return intersection;
				}
				else
					return null;
			}else
				return null;
		}
		return null;
	}
	
	public void addUnit(Unit unit){
		this.units.add(unit);
	}
	
	public List<Unit> getUnits(){
		return units;
	}
	
	public SortedSet<Attribute> getAttributes() {
		return attributes;
	}
	
	public int getDimensionality() {
		return dimensionality;
	}

	@Override
	public int compareTo(Subspace arg0) {
		return attComparator.compare(attributes.iterator().next(),arg0.getAttributes().iterator().next());
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Attribute d: attributes)
			sb.append(d.name()).append(" ");
		return sb.toString();
	}
	
	public List<Instance> getEntities(){
		List<Instance> entities = new ArrayList<Instance>();
		for ( Unit hc : units )
			for ( Instance i : hc.getInstances()) 
				entities.add(i);
		return entities;
	}
}
