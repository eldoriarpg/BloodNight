package de.eldoria.bloodnight.bloodmob.serialization.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StringProperty {
    String name();
    String descr();
    String pattern() default "";
    int min() default 0;
    int max() default 32;
}
