package br.rede.autoclustering.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MethodProperty {
	abstract float min() default 0;
	abstract float max() default 10;
	Parameter parameter();
}
