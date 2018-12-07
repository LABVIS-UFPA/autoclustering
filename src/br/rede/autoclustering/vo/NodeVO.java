package br.rede.autoclustering.vo;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="node")
public class NodeVO implements Comparable<NodeVO>{
	
	@Attribute(name="id", required = true)
	private int number;
	
	@Attribute(name="optK", required = true)
	private boolean optk;
	
	@Attribute(name="optOver", required = true)
	private boolean optOver;
	
	@Element(name="method", required = true)
	private String clazz;
	
	@ElementList(inline = true, required = false)
	private List<ParameterVO> properties = new ArrayList<ParameterVO>();
	
	public void setProperties(List<ParameterVO> properties) {
		this.properties = properties;
	}
	
	public List<ParameterVO> getProperties() {
		return properties;
	}

	public String getClazz() {
		return clazz;
	}


	public void setClazz(String clazz) {
		this.clazz = clazz;
	}


	public boolean isOptk() {
		return optk;
	}


	public void setOptk(boolean optk) {
		this.optk = optk;
	}


	public boolean isOptOver() {
		return optOver;
	}


	public void setOptOver(boolean optOver) {
		this.optOver = optOver;
	}


	public int getNumber() {
		return number;
	}


	public void setNumber(int number) {
		this.number = number;
	}


	@Override
	public int compareTo(NodeVO o) {
		if ( this.number < o.getNumber() )
			return -1;
		else if ( this.number > o.getNumber() )
			return 1;
		return 0;
	}
}
