package net.neoforged.neoforge.internal;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.CLASS)
public @interface MethodDesc {
    /**
     * The class containing the method
     */
    Class<?> owner();

    /**
     * The name of the method. Can be omitted if the name matches the extension method's name.
     */
    String name() default "";

    /**
     * The descriptor of the method consisting of the return type followed by the parameter types. Can be omitted
     * if the owning class contains only one method with the specified name.
     */
    Class<?>[] descriptor() default { Void.class };
}
