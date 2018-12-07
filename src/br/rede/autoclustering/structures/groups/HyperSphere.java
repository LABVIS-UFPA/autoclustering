package br.rede.autoclustering.structures.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Instance;
import weka.core.Instances;
import br.rede.autoclustering.util.MathProcess;


public class HyperSphere {

    private List<Instance> sphereObjects;
    private List<HyperShell> hypershells;
    private Map<Instance, Double> MapDistance;
    private Instance sphereCenter;
    private float threshold;
  
    
    public HyperSphere(Instances dataObjects, Instance sphereCenter,float threshold) {

    	this.threshold = threshold;
    	MathProcess process = new MathProcess();
        hypershells = new ArrayList<HyperShell>();
        MapDistance = new HashMap<Instance, Double>();
        this.sphereCenter = sphereCenter;

        sphereObjects = new ArrayList<Instance>();
        for (int i = 0; i < dataObjects.numInstances(); i++) {
        	Instance dataObject = dataObjects.instance(i);
           
        	if (dataObject != sphereCenter) {

                double distance = process.getDistance(sphereCenter, dataObject);
                               
                if (distance <= threshold) {
                  
                    MapDistance.put(dataObject, distance);
                    sphereObjects.add(dataObject);
                }

            }
        }
        setHypershells();
    }


	public int getNumberOfSphereObjects() {
        return sphereObjects.size();
    }

    private void setHypershells() {

        double radiusOfKHypershell;
       

        int k = 1;
        //   radiusOfKHypershell = (Math.pow(k, (double) 1 / numberOfDimensions));
        radiusOfKHypershell = k;
        hypershells.add(new HyperShell(0.5f, 0));
        hypershells.get(0).addObject(sphereCenter);

        while (radiusOfKHypershell <= threshold) {

            hypershells.add(new HyperShell(radiusOfKHypershell, k));
            k++;
            radiusOfKHypershell = k;

        }
      
        for (Instance dataObject : sphereObjects) {

            double distance = MapDistance.get(dataObject);

            for (int i = 0; i < hypershells.size() - 1; i++) {
               
                if ((hypershells.get(i).getRadius() <= distance) && (distance <= hypershells.get(i + 1).getRadius())) {
                    
                    hypershells.get(i + 1).addObject(dataObject);
                    break;
                }


            }
        }
        
    }

    public List<HyperShell> getHypershells() {
        return hypershells;
    }
}
