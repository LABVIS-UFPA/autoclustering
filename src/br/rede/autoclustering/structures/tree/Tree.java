package br.rede.autoclustering.structures.tree;

import java.util.ArrayList;
import java.util.List;

import br.rede.autoclustering.structures.groups.Group;

public class Tree {

	private Node root;

	public Tree(Node root) {
		this.root = root;
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public List<Group> getGroups(){
		List<Group> groups = new ArrayList<Group>();
		for ( NodeCluster a : root.getClusters()){
			Group group = new Group();
			for ( Cell il : a.getCells())
				group.getInstances().addAll(il.getInstances());
			groups.add(group);
		}
		return groups;
	}
}
