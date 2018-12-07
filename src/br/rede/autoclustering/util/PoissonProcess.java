package br.rede.autoclustering.util;

import org.apache.commons.math.util.MathUtils;

public class PoissonProcess {
	
	
	public static float calculatePoissonMassFunction(double lambda, int n){
		double um =Math.exp(-lambda);
		double dois =Math.pow(lambda, n);
		double tres = MathUtils.factorial(n);
		float quatro = (float) ((um*dois)/(tres)); 
		return quatro;
	}
	
	public static double calculatePoissonProbabilityDistribution(double lambda, double n){
		float sum = 0;
		for (int i = 0; i < n; i++) {
			sum += calculatePoissonMassFunction(lambda, i);
			if ( sum == 1 )
				return 1;
		}
		return sum;
	}
	
	public static double calculatePoisson(double lambdaA, double lambdaB, int n, int k){
		double aOverB = lambdaB*Math.exp(-lambdaB)*Math.exp(-lambdaB)/lambdaA*Math.exp(-lambdaA);
//		double combination = combination(n, k);
//		return combination*Math.pow(aOverB, k)*Math.pow(1-aOverB, n-k);
		return aOverB;
	}
	
	public static double combination(int n, int k){
		long result = 1;
		int i = 0, j = n;
		if ( k < n/2 )
			i = n - k;
		else
			i = k;
		while ( n > i ) 
			result *= n--;
		long under = MathUtils.factorial(j - i);
		return (double) result / under;
	}
	
}
