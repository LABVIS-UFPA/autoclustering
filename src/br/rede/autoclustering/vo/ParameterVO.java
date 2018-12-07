package br.rede.autoclustering.vo;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import br.rede.autoclustering.core.Parameter;

@Root(name="parameter")
public class ParameterVO {
	
	@Attribute(name="min", required=true)
	private float min;
	
	@Attribute(name="max", required=true)
	private float max;
	
	@Attribute(name="type", required=true)
	private Parameter type;
	
	public float getMin() {
		return min;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public float getMax() {
		return max;
	}

	public void setMax(float max) {
		this.max = max;
	}

	public Parameter getType() {
		return type;
	}

	public void setType(Parameter type) {
		this.type = type;
	}
	
	
	
}
