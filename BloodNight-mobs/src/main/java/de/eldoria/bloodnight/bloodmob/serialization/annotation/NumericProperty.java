package de.eldoria.bloodnight.bloodmob.serialization.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NumericProperty {
    String name();

    String descr();

    float min() default 0;

    float max() default 1024;
}
