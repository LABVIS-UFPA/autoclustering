package br.rede.autoclustering.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;

import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * 
 * A chi-square test (also chi-squared or χ2  test) is any statistical hypothesis test 
 * in which the sampling distribution of the test statistic is a chi-square distribution 
 * when the null hypothesis is true, or any in which this is asymptotically true, meaning 
 * that the sampling distribution (if the null hypothesis is true) can be made to approximate 
 * a chi-square distribution as closely as desired by making the sample size large enough.
 * Source: http://en.wikipedia.org/wiki/Chi-square_test
 * 
 * Chi-SquareTable from http://www.statsoft.com/textbook/sttable.html
 * 
 * @author Samuel Félix 2009
 *
 */
public class ChiSquare {

	private static Instances chiSquareTable = null;

	/**
	 * Use weka for reading the chi square table available at resources package
	 * 
	 * @return chi square table
	 */
	public static Instances readChiSquareTable() {
		if (chiSquareTable == null) {
			try {
				CSVLoader csv = new CSVLoader();
				csv.setFile(new File("resources/chisquaretable.txt"));
				chiSquareTable = new Instances(csv.getDataSet());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return chiSquareTable;
	}

	
	/**
	 * Calculates the X²-test for two probability distributions. The observed one and the theoretical one.
	 * It requires the degree of freedom and alpha (probability level) as input parameters
	 * 
	 * @param observed
	 * @param theoretical
	 * @param degreesOfFreedom
	 * @param alpha
	 * @return true if the observed distribution fits the theoretical one, false otherwise
	 */
	public static boolean chiSquareTest(double[] observed, double[] theoretical, int degreesOfFreedom,  double alpha) {
		readChiSquareTable();
		if ( observed.length != theoretical.length )
			throw new InvalidParameterException("The number of attributes are not the same!");
//		if ( observed.length < 2 )
//			throw new InvalidParameterException("The number of attributes need to be at least 2!");
		
		double chiSquare = 0f;
		double foundbound = -1;
		for (int i = 0; i < observed.length; i++) {
			if ( theoretical[i] != 0 )
				chiSquare += (Math.pow(theoretical[i] - observed[i], 2)) / theoretical[i];
		}
		for ( int i = 0 ; i < chiSquareTable.numAttributes(); i++ ) 
			if ( chiSquareTable.instance(degreesOfFreedom-1).value(i) < chiSquare )
				continue;
			else {
				foundbound = Float.parseFloat(chiSquareTable.attribute(i).name());
				break;
			}
		if ( foundbound > alpha )
			return true;
		else
			return false;
	}
}
