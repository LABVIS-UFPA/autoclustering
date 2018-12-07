//package br.rede.autoclustering.util;
//
//import java.lang.reflect.Field;
//
//import br.rede.autoclustering.core.MethodProperty;
//import br.rede.autoclustering.exceptions.InvalidValueException;
//
//public class MethodPropertyValidator {
//
//	/**
//	 * Returns a Parameter mapping an array of Objects
//	 * The first position is the value, the second one is the min value, the third one is max value and the last one is the increment value.
//	 * @param obj
//	 * @param slices 
//	 * @return
//	 * @throws Exception
//	 */
//	public static void validate(Object obj, int slices) throws Exception {
//		Field[] fields = obj.getClass().getDeclaredFields();
//		for (Field field : fields) {
//			if (field.isAnnotationPresent(MethodProperty.class)) {
//				field.setAccessible(true);
//				MethodProperty annotation = (MethodProperty) field
//						.getAnnotations()[0];
//				Class<? extends Object> type = field.getType();
//				Number value = null;
//				if (type.getName().equals("float"))
//					value = (Number) field.get(obj);
//				else if (type.getName().equals("double"))
//					value = (Number) field.get(obj);
//				else if (type.getName().equals("int"))
//					value = (Number) field.get(obj);
//				else {
//					continue;
//				}
//				if (value == null)
//					throw new NullPointerException("Field \"" + field.getName()	+ "\" not informed or not supported!");
//				if (value.doubleValue() < annotation.min())
//					throw new InvalidValueException("Field \""+ field.getName()	+ "\" is under minimum's value! Mininum: "+ annotation.min());
//				if (value.doubleValue() > annotation.max())
//					throw new InvalidValueException("Field \""+ field.getName() + "\" is over maximum's value! Maximum: " + annotation.max());
//				ParameterOptions.putParameter(annotation.parameter(),  new Object[]{value.floatValue(), annotation.min(), annotation.max(), slices});
//			}
//		}
//	}
//}
