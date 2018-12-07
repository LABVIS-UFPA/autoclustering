package br.rede.autoclustering.structures.groups;

import java.util.ArrayList;

import weka.core.Instance;

public class HyperShell {

    private ArrayList<Instance> shellObjects;
    private int position;
    private double radius;

    public HyperShell(double radius, int shellPosition) {
        this.position = shellPosition;
        this.radius = radius;
        this.shellObjects = new ArrayList<Instance>();
    }


   
    public void addObject(Instance objectToShell) {
        shellObjects.add(objectToShell);
    }

    public double getRadius() {
        return radius;
    }

    public double Normalization()
    {
        return shellObjects.size();
    }

    public int getPosition() {
        return position;
    }

    public ArrayList<Instance> getShellObjects() {
        return shellObjects;
    }
}
