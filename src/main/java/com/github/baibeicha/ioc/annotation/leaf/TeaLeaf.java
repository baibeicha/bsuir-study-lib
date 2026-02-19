package com.github.baibeicha.ioc.annotation.leaf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TeaLeaf {
    String name() default "";
    String initMethod() default "";
    String destroyMethod() default "";
}
