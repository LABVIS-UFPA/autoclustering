package br.rede.autoclustering.structures.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.structures.tree.Cell;
import br.rede.autoclustering.util.SortedList;

public class Grid implements Cloneable {
	
    private HashMap<String, Cell> cells = new HashMap<String, Cell>();
    private SortedList<Cell> sortedCells = new SortedList<Cell>();
    
    public void createGrid(int slices, double[] lowerBounds, double[] upperBounds, List<Instance> instances) {
    	for (Instance i : instances) {
    		String key = findInterval(i, slices, lowerBounds, upperBounds);
    		if ( cells.get(key) == null ) {
    			Cell cell = new Cell(key);
    			cell.getInstances().add(i);
    			cells.put(key, cell);
    			
    		}else
    			cells.get(key).getInstances().add(i);
		}
    	sortedCells.clear();
    	for ( Cell cell : cells.values()){
    		cell.setDensity(cell.getInstances().size());
    		sortedCells.add(cell);
    	}
    }
    
    /**
     * 
     * @param slices
     * @param lowerBounds
     * @param upperBounds
     * @param instances
     */
    public void createGrid(int slices, double[] lowerBounds, double[] upperBounds, Instances instances) {
    	for (int i = 0; i < instances.numInstances(); i++) {
    		String key = findInterval(instances.instance(i), slices, lowerBounds, upperBounds);
    		if ( cells.get(key) == null ) {
    			Cell cell = new Cell(key);
    			cell.getInstances().add(instances.instance(i));
    			cells.put(key, cell);
    		}else
    			cells.get(key).getInstances().add(instances.instance(i));
		}
    	sortedCells.clear();
    	for ( Cell cell : cells.values()){
    		cell.setDensity(cell.getInstances().size());
    		sortedCells.add(cell);
    	}
    }
	
	private String findInterval(Instance i, int slices, double[] lowerBounds, double[] upperBounds){
		StringBuffer interval = new StringBuffer(i.numAttributes());
		for (int j = 0; j < i.numAttributes(); j++) {
			if ( j != 0 )
				interval.append(",");
			float gridLength = (float) ((upperBounds[j]-lowerBounds[j]) / slices);
			int d;
			if ( gridLength == 0 ) 
				d = 0;
			else
				d = (((int) ( (i.value(j) - lowerBounds[j] -0.0001) / (gridLength) ) ) );
			interval.append(d);
		}
		return interval.toString();
	}
	
	public HashMap<String, Cell> getCells() {
		return cells;
	}

	public List<Instance> getInstances(){
		List<Instance> instances = new ArrayList<Instance>();
		for(Cell values : cells.values() ) 
			instances.addAll(values.getInstances());
		return instances;
	}
		
	
	public Grid clone() {
		try {
			return (Grid) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void createGrid(float diameter, double[] lowerBounds,
			double[] upperBounds, Instances instances) {
    	for (int i = 0; i < instances.numInstances(); i++) {
    		String key = findInterval(instances.instance(i), diameter, lowerBounds, upperBounds);
    		if ( cells.get(key) == null ) {
    			Cell cell = new Cell(key);
    			cell.getInstances().add(instances.instance(i));
    			cells.put(key, cell);
    		}else
    			cells.get(key).getInstances().add(instances.instance(i));
		}
    	sortedCells.clear();
    	for ( Cell cell : cells.values()){
    		cell.setDensity(cell.getInstances().size());
    		sortedCells.add(cell);
    	}
	}

	private String findInterval(Instance i, float diameter,
			double[] lowerBounds, double[] upperBounds) {
		StringBuffer interval = new StringBuffer(i.numAttributes());
		for (int j = 0; j < i.numAttributes(); j++) {
			if ( j != 0 )
				interval.append(",");
			int d = (((int) ( (i.value(j) - lowerBounds[j] -0.0001) / (diameter) ) ) );
			interval.append(d);
		}
		return interval.toString();
	}
	
	public SortedList<Cell> getSortedCells() {
		return sortedCells;
	}
}

