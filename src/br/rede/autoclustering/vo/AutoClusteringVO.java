package br.rede.autoclustering.vo;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import br.rede.autoclustering.util.SortedList;

@Root(name = "AutoClustering")
public class AutoClusteringVO {

	@Attribute(name = "version", required = true)
	private String version;
	
	@Attribute(name = "slices", required = true)
	private int slices;
	
	@ElementList(inline = true)
	private List<NodeVO> nodes = new ArrayList<NodeVO>();
	
	@ElementList(inline = true)
	private List<EdgeVO> edges = new ArrayList<EdgeVO>();


	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getSlices() {
		return slices;
	}

	public void setSlices(int slices) {
		this.slices = slices;
	}

	public List<NodeVO> getNodes() {
		return nodes;
	}

	public void setNodes(List<NodeVO> nodes) {
		this.nodes = nodes;
	}

	public List<EdgeVO> getEdges() {
		return edges;
	}

	public void setEdges(List<EdgeVO> edges) {
		this.edges = edges;
	}

}
