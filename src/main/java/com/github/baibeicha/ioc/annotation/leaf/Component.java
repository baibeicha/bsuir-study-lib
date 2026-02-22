package com.github.baibeicha.ioc.annotation.leaf;

import com.github.baibeicha.reflection.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TeaLeaf
public @interface Component {
    @AliasFor(annotation = TeaLeaf.class)
    String name() default "";
    String initMethod() default "";
    String destroyMethod() default "";
}
