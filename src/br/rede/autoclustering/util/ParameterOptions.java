package br.rede.autoclustering.util;

import java.util.HashMap;
import java.util.Map;

import br.rede.autoclustering.core.Parameter;
import br.rede.autoclustering.vo.ParameterVO;

public class ParameterOptions {
	
	private static Map<Parameter, Float[]> values = new HashMap<Parameter, Float[]>();

//	public static void putParameter(Parameter parameter, Object[] object){
//		if ( parameter == Parameter.ALL_DISTANCE || parameter == Parameter.ALL_INSTANCES) 
//				return;
//		float min = (Float) object[1];
//		float max = (Float) object[2];
//		Integer slices = (Integer) object[3];
//		Float[] values = new Float[slices];
//		values[0] = min;
//		values[slices-1] = max;
//		float length = (float) ( max - min ) / (slices-1);
//		for (int i = 1; i < slices-1; i++) 
//			values[i] = min + i*length;
//		ParameterOptions.values.put(parameter, values);
//	}
	public static void putParameter(ParameterVO parameterVO, int slices) {
		float max = parameterVO.getMax();
		float min = parameterVO.getMin();
		Float[] values = new Float[slices];
		values[0] = min;
		values[slices-1] = max;
		float length = (float) ( max - min ) / (slices-1);
		for (int i = 1; i < slices-1; i++) 
			values[i] = min + i*length;
		ParameterOptions.values.put(parameterVO.getType(), values);
	}
	public static float getParameterValue(Parameter p, int i){
		return ParameterOptions.values.get(p)[i];
	}
	
	public static Float[] getParameterValues(Parameter p){
		return ParameterOptions.values.get(p);
	}

	public static Integer getKeyFromValue(Parameter p, float value) {
		for (int i = 0; i < values.get(p).length; i++) {
			float iter = values.get(p)[i];
			if (iter == value)
				return i;
		}			
		return -1;
	}


	
}
