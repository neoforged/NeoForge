package net.neoforged.testframework.annotation;

import net.neoforged.testframework.TestListener;
import net.neoforged.testframework.impl.test.AbstractTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate the class of an {@link AbstractTest} or a method-based test with this annotation in order
 * to add test listeners to the test. <br>
 * The classes provided as listeners must have a no-arg constructor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface WithListener {
    /**
     * {@return the listeners of the test}
     */
    Class<? extends TestListener>[] value();
}
