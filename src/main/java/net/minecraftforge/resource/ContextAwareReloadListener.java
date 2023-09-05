package net.minecraftforge.resource;

import net.minecraft.core.RegistryAccess;
import net.minecraftforge.common.conditions.ICondition;

public abstract class ContextAwareReloadListener {
    protected ICondition.IContext conditionContext = ICondition.IContext.EMPTY;
    protected RegistryAccess registryAccess = RegistryAccess.EMPTY;

    public void injectContext(ICondition.IContext conditionContext, RegistryAccess registryAccess) {
        this.conditionContext = conditionContext;
        this.registryAccess = registryAccess;
    }
}
