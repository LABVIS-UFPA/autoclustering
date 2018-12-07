package br.rede.autoclustering.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;

public class StudentsT {
	
	/**
	 * It requires the csv file
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static double pairedTTest(String file) throws Exception{
		Scanner s = new Scanner(new File(file)).useDelimiter("[,]|\n");
		List<Double> edaDistr = new ArrayList<Double>();
		List<Double> dbscanDistr = new ArrayList<Double>();
		s.next();s.next();
		while(s.hasNext()) {
			double eda = Double.parseDouble(s.next());
			double dbscan = Double.parseDouble(s.next());
			edaDistr.add(eda);
			dbscanDistr.add(dbscan);
		}
		double[] edaVet = new double[edaDistr.size()];
		double[] dbscanVet = new double[dbscanDistr.size()];
		for (int i = 0; i < dbscanVet.length; i++) {
			edaVet[i] = edaDistr.get(i);
			dbscanVet[i] = dbscanDistr.get(i);
		}
		TTest test = new TTestImpl();
		return test.pairedTTest(edaVet, dbscanVet);
	}
}
