package de.eldoria.bloodnight.bloodmob.serialization.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NumberProperty {
    String name();

    String descr();

    int min() default 0;

    int max() default 1024;
}
