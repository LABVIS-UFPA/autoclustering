package br.rede.autoclustering.util;


import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;

import javax.swing.JFrame;
import javax.swing.JLabel;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.structures.groups.Group;

public class ClusterViewerFrame extends JFrame {
	private static final long serialVersionUID = -2017094467363261159L;
	private Map<Instance, InstanceLabel> instances = new HashMap<Instance, InstanceLabel>();
	
	public ClusterViewerFrame(Instances all, List<Group> cluster) {
		setTitle("EDACluster rocks!");
		setLayout(null);
		setBackground(Color.white);
		
		if ( all != null ) {
			boolean isOneDim = all.instance(0).numAttributes() < 2 ? true : false;
		
		for (int i = 0; i < all.numInstances(); i++) {
			InstanceLabel il =  new InstanceLabel();
			il.setToolTipText(all.instance(i).toString());
			if ( isOneDim )
				il.setLocation(50+(int)Math.round(all.instance(i).value(0)*6), 50);
			else
				il.setLocation(50+(int)Math.round(all.instance(i).value(0)*6), 50+(int)Math.round(all.instance(i).value(1)*4));
			il.repaint();
			instances.put(all.instance(i),il);
			add(il);
		}
		}
		Random d = new Random();
		if ( cluster != null ) {
			for (Group ia : cluster){
				Color c = new Color(d.nextInt(175)+80,d.nextInt(175)+80,d.nextInt(175)+80);
				for ( Instance i : ia.getInstances()) {
					InstanceLabel il =  instances.get(i);
					if ( il == null ) {
						il =  new InstanceLabel();
						il.setLocation((int)Math.round(i.value(0)*7), (int)Math.round(i.value(1)*5));
						il.setVisible(true);
						super.add(il);
					}
					il.setColor(c);
				}
			}
		}
		setSize(1000,700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public ClusterViewerFrame(int i, int j, Instances all,List<List<Instance>> cluster) {
		int size = 700;
		setTitle(all.attribute(i).name()+" - "+all.attribute(j).name());
		setLayout(null);
		setBackground(Color.white);

		Random d = new Random();
		if ( cluster != null ) {
			for (List<Instance> ia : cluster){
				Color c = new Color(d.nextInt(175)+80,d.nextInt(175)+80,d.nextInt(175)+80);
				for ( Instance k : ia) {
					InstanceLabel il =  instances.get(k);
					if ( il == null ) {
						il =  new InstanceLabel();
						il.setLocation((int)Math.round(k.value(i)*7), size - (int)Math.round(k.value(j)*5));
						il.setToolTipText(k.toString());
						il.setVisible(true);
						super.add(il);
					}
					il.setColor(c);
				}
			}
		}
		for (int k = 0; k < all.numInstances(); k++) {
			InstanceLabel il =  new InstanceLabel();
			il.setColor(Color.black);
			il.setToolTipText(all.instance(k).toString());
			il.setLocation((int)Math.round(all.instance(k).value(i)*7), size - (int)Math.round(all.instance(k).value(j)*5));
			il.setVisible(true);
			super.add(il);
			instances.put(all.instance(k),il);
			
		}
		setSize(1000,size);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public ClusterViewerFrame(Instances database,
			SortedMap<String, List<Instance>> clusters) {
		new ClusterViewerFrame(database,getGroup(clusters));
	}
	public ClusterViewerFrame(List<Group> group) {
		setLayout(null);
		setBackground(Color.white);
		
		Random d = new Random();
		for (Group ia : group){
			Color c = new Color(d.nextInt(175)+80,d.nextInt(175)+80,d.nextInt(175)+80);
			for ( Instance i : ia.getInstances()) {
				InstanceLabel il =  instances.get(i);
				if ( il == null ) {
					il =  new InstanceLabel();
					il.setLocation((int)Math.round(i.value(0)*7), (int)Math.round(i.value(1)*5));
					il.setVisible(true);
					super.add(il);
				}
				il.setColor(c);
			}
		}
		setSize(1000,700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public ClusterViewerFrame(Instance[] group) {
		setLayout(null);
		setBackground(Color.white);
		
		
		Random d = new Random();
		Color c = new Color(d.nextInt(175)+80,d.nextInt(175)+80,d.nextInt(175)+80);
		for ( Instance i : group) {
				InstanceLabel il =  new InstanceLabel();
				il.setLocation((int)Math.round(i.value(0)*7), (int)Math.round(i.value(1)*5));
				il.setVisible(true);
				super.add(il);
				il.setColor(c);
		}
		setSize(1000,700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	public ClusterViewerFrame(Instances all) {
		setLayout(null);
		setBackground(Color.white);
		for (int i = 0; i < all.numInstances(); i++) {
			InstanceLabel il =  new InstanceLabel();
			il.setToolTipText(all.instance(i).toString());
			il.setLocation(50+(int)Math.round(all.instance(i).value(0)*6), 50+(int)Math.round(all.instance(i).value(1)*4));
			il.repaint();
			instances.put(all.instance(i),il);
			add(il);
		}
		setSize(1000,800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}

	private List<Group> getGroup(SortedMap<String, List<Instance>> clusters){
		
		List<Group> listOfGroups = new ArrayList<Group>();
		for ( String key : clusters.keySet() ){
			Group g = new Group();
			g.getInstances().addAll(clusters.get(key));
			listOfGroups.add(g);
		}
		return listOfGroups;
	}
}

class InstanceLabel extends JLabel{
	private static final long serialVersionUID = -1107300368923561832L;
	private Color color = Color.black;
	public InstanceLabel() {
		setSize(10,10);
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(color);
		g.fillOval(0,0, 10,10);
	}
}