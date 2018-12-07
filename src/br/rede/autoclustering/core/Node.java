package br.rede.autoclustering.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author sfelixjr
 *
 */
public class Node {
	
	private List<Parameter> parameters = new ArrayList<Parameter>();
	private List<Edge> edgesIn;
	private List<Edge> edgesOut;
	private String code;
	private boolean supportOptionalK;
	private boolean supportOptionalOverlap;
	private ClusteringMethod clusteringMethod;

	public Node(ClusteringMethod clusteringMethod, boolean supOptK, boolean supOptOver) {
		this.clusteringMethod = clusteringMethod;
		this.edgesIn = new ArrayList<Edge>();
		this.edgesOut = new ArrayList<Edge>();
		this.supportOptionalK = supOptK;
		this.supportOptionalOverlap = supOptOver;
	}
	
	public ClusteringMethod getClusteringMethod() {
		return clusteringMethod;
	}
	
	public void setClusteringMethod(ClusteringMethod clusteringMethod) {
		this.clusteringMethod = clusteringMethod;
	}
	
	/**
	 * @return Returns the methodName.
	 */
	public String getMethodName() {
		return clusteringMethod.getName();
	}

	/**
	 * @return Returns the edgesIn.
	 */
	public List<Edge> getEdgesIn() {
		return edgesIn;
	}
	/**
	 * @param edgesIn The edgesIn to set.
	 */
	public void setEdgesIn(List<Edge> edgesIn) {
		this.edgesIn = edgesIn;
	}
	/**
	 * @return Returns the edgesOut.
	 */
	public List<Edge> getEdgesOut() {
		return edgesOut;
	}
	/**
	 * @param edgesOut The edgesOut to set.
	 */
	public void setEdgesOut(List<Edge> edgesOut) {
		this.edgesOut = edgesOut;
	}
	
	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		if (code == null) 
			code = this.getMethodName() +"();\n";
		return code;
	}
	/**
	 * @param code The code to set.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	public boolean isSupportOptionalK() {
		return supportOptionalK;
	}

	public void setSupportOptionalK(boolean supportOptionalK) {
		this.supportOptionalK = supportOptionalK;
	}

	public boolean isSupportOptionalOverlap() {
		return supportOptionalOverlap;
	}

	public void setSupportOptionalOverlap(boolean supportOptionalOverlap) {
		this.supportOptionalOverlap = supportOptionalOverlap;
	}
	
	public List<Parameter> getParameters(){
		return parameters;
	}
	
	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public String toString() {
		return clusteringMethod.getName();
	}
}
