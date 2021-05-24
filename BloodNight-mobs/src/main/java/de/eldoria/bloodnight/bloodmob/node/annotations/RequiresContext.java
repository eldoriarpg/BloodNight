package de.eldoria.bloodnight.bloodmob.node.annotations;

import de.eldoria.bloodnight.bloodmob.node.context.IContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiresContext {
    Class<? extends IContext>[] value() default {};
}
