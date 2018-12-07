package br.rede.autoclustering.vo;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "edge")
public class EdgeVO {

	@Attribute(name="in")
	private int in;
	@Attribute(name="out")
	private int out;

//	public EdgeVO(int in, int out) {
//		this.in = in;
//		this.out = out;
//	}

	public int getIn() {
		return in;
	}
	
	public int getOut() {
		return out;
	}
	
	public void setIn(int in) {
		this.in = in;
	}
	
	public void setOut(int out) {
		this.out = out;
	}
}
