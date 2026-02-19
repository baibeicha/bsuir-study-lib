package com.github.baibeicha.reflection.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AliasFor {

    String value() default "";

    Class<? extends Annotation> annotation() default Annotation.class;

    String attribute() default "";
}
