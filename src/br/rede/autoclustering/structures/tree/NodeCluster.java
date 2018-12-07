package br.rede.autoclustering.structures.tree;

import java.util.ArrayList;
import java.util.List;

public class NodeCluster {

	private List< Cell> cells = new ArrayList< Cell>();
	private List<Cell> outliners = new ArrayList<Cell>();
	
	public List< Cell> getCells() {
		return cells;
	}
	
	public void add(Cell list) {
		cells.add(list);
	}
	
	public List<Cell> getOutliners() {
		return outliners;
	}
	
	public void setOutliners(List<Cell> outliners) {
		this.outliners = outliners;
	}
}
