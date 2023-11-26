package net.neoforged.testframework.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a test method with this annotation in order to configure an empty template for the test.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EmptyTemplate {
    /**
     * {@return the size of the template}
     */
    String value() default "3x3x3";

    /**
     * {@return whether the template should have a floor}
     * If this is {@code true}, the structure's height with be increased by one.
     */
    boolean floor() default false;

    record Size(int length, int height, int width) {
        public static Size parse(String str) {
            final var split = str.split("x");
            if (split.length == 0) {
                throw new IllegalArgumentException("Empty size not allowed!");
            }
            if (split.length == 1) {
                final var asInt = Integer.parseInt(split[0]);
                return new Size(asInt, asInt, asInt);
            } else if (split.length == 2) {
                final var v1 = Integer.parseInt(split[0]);
                if (v1 != Integer.parseInt(split[1])) {
                    throw new IllegalArgumentException("Two dimension sizes must have equal width and length!");
                }
                return new Size(v1, v1, v1);
            } else if (split.length == 3) {
                return new Size(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
            }
            throw new IllegalArgumentException("Size has too many dimensions: " + split.length);
        }

        @Override
        public String toString() {
            return length + "x" + height + "x" + width;
        }
    }
}
