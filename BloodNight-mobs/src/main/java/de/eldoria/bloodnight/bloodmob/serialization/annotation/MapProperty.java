package de.eldoria.bloodnight.bloodmob.serialization.annotation;


import de.eldoria.bloodnight.bloodmob.serialization.value.SimpleValue;
import de.eldoria.bloodnight.bloodmob.serialization.value.Value;
import de.eldoria.bloodnight.bloodmob.serialization.value.ValueType;

import javax.management.ValueExp;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MapProperty {
    String name();

    String descr();

    ValueType key();

    ValueType value();
}
