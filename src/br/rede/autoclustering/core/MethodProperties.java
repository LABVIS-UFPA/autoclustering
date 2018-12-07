//package br.rede.autoclustering.core;
//
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.List;
//
//import br.rede.autoclustering.util.DistanceType;
//import br.rede.autoclustering.util.MethodPropertyValidator;
//
///**
// * Right now, there is no requirement to be a MethodProperties
// * @author sfelixjr
// *
// */
//public abstract class MethodProperties {
//	
//	@MethodProperty(parameter = Parameter.ALL_DISTANCE)
//	private DistanceType distance = DistanceType.EUCLIDEAN;
//
//	public DistanceType getDistance() {
//		return distance;
//	}
//	
//	public void setDistance(DistanceType distance) {
//		this.distance = distance;
//	}
//	
//	public List<Parameter> getParameters(){
//		List<Parameter> parameters = new ArrayList<Parameter>();
//		Field[] fields = getClass().getDeclaredFields();
//		for (Field field : fields) {
//			if (field.isAnnotationPresent(MethodProperty.class)) {
//				field.setAccessible(true);
//				MethodProperty annotation = (MethodProperty) field.getAnnotations()[0];
//				parameters.add(annotation.parameter());
//			}
//		}
//		return parameters;
//	}
//}
