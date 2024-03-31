package net.neoforged.testframework.annotation;

import net.neoforged.testframework.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to generate {@link Test}. Annotated methods must be static, take no parameters, and return an {@link Iterable} of {@link Test}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface TestHolderGenerator {
}
