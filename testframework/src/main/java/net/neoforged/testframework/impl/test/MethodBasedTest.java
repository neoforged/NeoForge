package net.neoforged.testframework.impl.test;

import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.impl.HackyReflection;
import net.minecraft.gametest.framework.GameTest;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

public class MethodBasedTest extends AbstractTest.Dynamic {
    protected MethodHandle handle;
    private final Method method;

    public MethodBasedTest(Method method) {
        this.method = method;

        configureFrom(AnnotationHolder.method(method));
        configureGameTest(method.getAnnotation(GameTest.class));

        this.handle = HackyReflection.handle(method);
    }

    public MethodBasedTest bindTo(Object target) {
        handle = handle.bindTo(target);
        return this;
    }

    @Override
    public void init(@Nonnull TestFramework framework) {
        super.init(framework);
        try {
            this.handle.invoke(this);
        } catch (Throwable e) {
            throw new RuntimeException("Encountered exception initiating method-based test: " + method, e);
        }
    }

}
