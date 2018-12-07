package br.rede.autoclustering.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class SortedList<T> extends ArrayList<T> {
	
	private Comparator<T> comparator;
	
	public SortedList(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	public SortedList() {
		
	}
	
	public boolean add(T x) {
		if ( size() == 0 ){
			super.add(x);
			return true;
		}
		int count = 0;
		for ( T t : this ) {
			int result = 0;
			if ( comparator != null ) {
				result = comparator.compare(t, x);
			} else if (t instanceof Comparable<?>) {
				try {
					result = ((Comparable<T>) t).compareTo(x);
				}catch (Exception e) {
					new Exception("The type "+t.getClass().toString()+" does not implement the Comparable interface. Try to inform a Comparator");
				}
			}
			if (result>0) {
				super.add(count,x);
				return true;
			}
			count++;
		}
		super.add(size(), x);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for ( T t : c )
			add( t );
		return true;
	}
	
	public T first() {
		if ( size() == 0 )
			return null;
		else
			return get(0);
	}
}
