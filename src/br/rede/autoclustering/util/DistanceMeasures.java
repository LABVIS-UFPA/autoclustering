package br.rede.autoclustering.util;

import org.apache.commons.math.MathRuntimeException;

import weka.core.Instance;

public class DistanceMeasures {

	private Instance instance1;
	private Instance instance2;

	public static DistanceMeasures instance = null;

	public static DistanceMeasures getInstance() {
		if (instance == null)
			instance = new DistanceMeasures();
		return instance;
	}

	public synchronized double calculateDistance(Instance inst1, Instance inst2, DistanceType tipo) {
		
		this.instance1 = inst1;
		this.instance2 = inst2;
		
		switch (tipo) {
		case EUCLIDEAN:
			return euclideanDistance();
		case MANHATTAN:
			return manhattanDistance();
		case CHEBYCHEV:
			return chebychevDistance();
//		case MINKOWSKI:
//			return minkowskiDistance();
//		case CANBERRA:
//			return canberraDistance();
//		case BRAYCURTIS:
//			return brayCurtisDistance();
//		case ANGULAR_SEPARATION:
//			return angularSeparation();

		default:
			return euclideanDistance();
		}
	}

	private double euclideanDistance() {
		double ed = 0.0;
		for (int i = 0; i < instance1.numAttributes(); i++) {
			ed += Math.pow(instance1.value(i) - instance2.value(i), 2);
		}
		ed = Math.sqrt(ed);
		// System.out.println("Euclidean Distance : " + ed);

		return ed;
	}

	private double manhattanDistance() {
		double md = 0.0;
		for (int i = 0; i < instance1.numAttributes(); i++) {
			md += Math.abs(instance1.value(i) - instance2.value(i));
		}
		// md = Math.sqrt(md);
		// System.out.println("Manhattan Distance : " + md);
		// System.out.println("Manhattan Distance 2: " + Math.sqrt(md));
		return md;
	}

	private double chebychevDistance() {
		double cd = 0.0;
		double maxCD = -1111;
		for (int i = 0; i < instance1.numAttributes(); i++) {
			if (maxCD < Math.abs(instance1.value(i) - instance2.value(i))) {
				maxCD = Math.abs(instance1.value(i) - instance2.value(i));
			}
		}
		cd = maxCD;
		// System.out.println("Chebychev Distance : " + cd);
		return cd;
	}

	private double minkowskiDistance() {
		double md = 0.0;
		for (int i = 0; i < instance1.numAttributes(); i++) {
			md += Math.pow(instance1.value(i) - instance2.value(i),
					instance1.numAttributes());
		}
		md = Math.pow(md, 1 / (double) instance1.numAttributes());
		// System.out.println("Minkowski Distance : " + md);
		return md;
	}

	private double canberraDistance() {
		double cd = 0.0;
		for (int i = 0; i < instance1.numAttributes(); i++) {
			cd += (Math.abs(instance1.value(i) - instance2.value(i)))
					/ Math.abs((instance1.value(i) + instance2.value(i)));
		}
		// System.out.println("Camberra Distance : " + cd);
		return cd;
	}

	private double brayCurtisDistance() {
		double bcd = 0.0;
		double denom = 0.0;
		for (int i = 0; i < instance1.numAttributes(); i++) {
			bcd += (Math.abs(instance1.value(i) - instance2.value(i)));
			denom += (instance1.value(i) + instance2.value(i));
		}
		bcd = bcd / denom;
		// System.out.println("Bray Curtis Distance : " + bcd);
		return bcd;
	}

	private double angularSeparation() {
	    double normProduct = getEuclideanNorm(instance1) * getEuclideanNorm(instance2);
	    if (normProduct == 0) 
	      throw MathRuntimeException.createArithmeticException("zero norm");
	    
	    double dot = dotProduct(instance1, instance2);
	    double threshold = normProduct * 0.9999;
	    if ((dot < -threshold) || (dot > threshold)) {
		     // the vectors are almost aligned, compute using the sine
			try {
				Instance v3 = crossProduct(instance1, instance2);
			    if (dot >= 0) {
				     return Math.asin(getEuclideanNorm(v3) / normProduct);
				}
				return Math.PI - Math.asin(getEuclideanNorm(v3) / normProduct);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		 		// the vectors are sufficiently separated to use the cosine
		return Math.acos(dot / normProduct);

//		double as = 0;
//		double den1 = 0.0;
//		double den2 = 0.0;
//		for (int i = 0; i < instance1.numAttributes(); i++) {
//			as += instance1.value(i) * instance2.value(i);
//			den1 += Math.pow(instance1.value(i), 2);
//			den2 += Math.pow(instance2.value(i), 2);
//		}
//		as = (as / (den1 * den2));
//		// System.out.println("Angular Separation : " + as);
//		return as;
	}

	private Instance crossProduct(Instance instance1, Instance instance2) throws Exception {
		if ( instance1.numAttributes() != 3 )
			throw new Exception("Angular Separation is not ready for more than 3 dimensions");
		
		Instance i = new Instance(instance1.numAttributes());
		i.setValue(0, instance1.value(1) * instance2.value(2) - instance1.value(2) * instance2.value(1));
        i.setValue(1, instance1.value(2) * instance2.value(0) - instance1.value(0) * instance2.value(2));
        i.setValue(2, instance1.value(0) * instance2.value(1) - instance1.value(1) * instance2.value(0));
        
		return i;
	}

	/** Compute the dot-product of two instances.
	   * @param v1 first Instance
	   * @param v2 second Instance
	   * @return the dot product v1.v2
	   */
	  public static double dotProduct(Instance v1, Instance v2) {
		  double sum = 0;
		  for (int i = 0; i < v1.numAttributes(); i++) 
			  sum += v1.value(i) * v2.value(i);
		  return sum;
	  }
	
	  public double correlationCoefficient() {
		
		    int attributes = instance1.numAttributes();
	        double result = 0;
	        double sum_sq_x = 0;
	        double sum_sq_y = 0;
	        double sum_coproduct = 0;
	        double mean_x = 0;
	        double mean_y = 0;
	        
			for (int i = 0; i < instance1.numAttributes(); i++) {
				mean_x += instance1.value(i);
				mean_y += instance2.value(i);
			}
			mean_x /= attributes;
			mean_y /= attributes;
	        
	        for(int i=0;i<attributes;i++){
	            double sweep =Double.valueOf(i-1)/i;
	            double delta_x = instance1.value(i)-mean_x;
	            double delta_y = instance2.value(i)-mean_y;
	            sum_sq_x += delta_x * delta_x * sweep;
	            sum_sq_y += delta_y * delta_y * sweep;
	            sum_coproduct += delta_x * delta_y * sweep;
	            mean_x += delta_x / i;
	            mean_y += delta_y / i;
	        }
	        double pop_sd_x = (double) Math.sqrt(sum_sq_x/attributes);
	        double pop_sd_y = (double) Math.sqrt(sum_sq_y/attributes);
	        double cov_x_y = sum_coproduct / attributes;
	        result = cov_x_y / (pop_sd_x*pop_sd_y);
	        return result;
	    }

		
//		double cc = 0.0;
//		double mean1 = 0.0;
//		double mean2 = 0.0;
//		double den1 = 0.0;
//		double den2 = 0.0;
//
//		for (int i = 0; i < instance1.numAttributes(); i++) {
//			mean1 += instance1.value(i);
//			mean2 += instance2.value(i);
//		}
//		mean1 = mean1 / instance1.numAttributes();
//		mean2 = mean2 / instance1.numAttributes();
//
//		for (int i = 0; i < instance1.numAttributes(); i++) {
//			cc += (instance1.value(i) - mean1) * (instance2.value(i) - mean2);
//			den1 += Math.pow(instance1.value(i) - mean1, 2);
//			den2 += Math.pow(instance2.value(i) - mean2, 2);
//		}
//		cc = cc / Math.sqrt(den1 * den2);
		// System.out.println("Correlation Coefficient : " + cc);
//		return cc;
//	}
	
	
	public static double getEuclideanNorm(Instance instance) {
	    double component_squares_sum = 0;
	    for( int i=0 ; i < instance.numAttributes() ; i++)
	        component_squares_sum += Math.pow(instance.value(i), 2.0);
	    return Math.sqrt(component_squares_sum);
	}
	
}
