package com.github.baibeicha.test.annotation;

import com.github.baibeicha.reflection.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@TestTarget
public @interface TestConfiguration {
    @AliasFor(annotation = TestTarget.class)
    String value() default "";
    Class<?>[] classes() default {};
    String[] packages() default {};
}
