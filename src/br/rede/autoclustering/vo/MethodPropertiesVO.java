package br.rede.autoclustering.vo;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "method")
public class MethodPropertiesVO {

	@Element
	private int id;
	@ElementList(inline = true)
	private List<ParameterVO> properties = new ArrayList<ParameterVO>();

	public MethodPropertiesVO(int id, List<ParameterVO> properties) {
		this.id = id;
		this.properties = properties;
	}

	public List<ParameterVO> getProperties() {
		return properties;
	}

	public void setProperties(List<ParameterVO> properties) {
		this.properties = properties;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
