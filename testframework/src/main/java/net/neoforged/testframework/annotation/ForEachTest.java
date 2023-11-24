package net.neoforged.testframework.annotation;

import net.neoforged.testframework.TestListener;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Apply this annotation to a class in order to add a common configuration to all child tests. <br>
 * This is primarily useful for method-based tests. <br>
 * Note: the common configuration goes <strong>only one</strong> level deep.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForEachTest {
    ForEachTest DEFAULT = new ForEachTest() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return ForEachTest.class;
        }

        @Override
        public String idPrefix() {
            return "";
        }

        @Override
        public String[] groups() {
            return new String[0];
        }

        @Override
        public Class<? extends TestListener>[] listeners() {
            return new Class[0];
        }
    };

    /**
     * {@return a prefix to apply to the child tests}
     */
    String idPrefix() default "";

    /**
     * {@return the groups in which child tests will be, by default}
     */
    String[] groups() default {};

    /**
     * {@return the listeners to add to all child tests}
     */
    Class<? extends TestListener>[] listeners() default {};
}
