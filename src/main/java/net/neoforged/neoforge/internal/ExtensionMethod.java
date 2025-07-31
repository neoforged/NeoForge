package net.neoforged.neoforge.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface ExtensionMethod {
    /**
     * Declares the owner and method descriptor of the original vanilla method.
     * <p>
     * Can be omitted if the class holding the extension method is an extension interface which is injected onto the class
     * containing the target method, the target method's name matches the extension method's name and said class only
     * contains one method with this name
     */
    MethodDesc original() default @MethodDesc(owner = Void.class);

    /**
     * Declares a list of methods which are allowed to call the original method
     */
    MethodDesc[] exclusions() default {};
}
