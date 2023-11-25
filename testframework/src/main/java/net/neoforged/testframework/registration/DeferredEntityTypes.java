package net.neoforged.testframework.registration;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DeferredEntityTypes extends DeferredRegister<EntityType<?>> {
    private final RegistrationHelper helper;
    public DeferredEntityTypes(String namespace, RegistrationHelper helper) {
        super(Registries.ENTITY_TYPE, namespace);
        this.helper = helper;
    }

    @Override
    protected <I extends EntityType<?>> DeferredEntityTypeBuilder createHolder(ResourceKey<? extends Registry<EntityType<?>>> registryKey, ResourceLocation key) {
        return new DeferredEntityTypeBuilder(ResourceKey.create(registryKey, key), helper);
    }

    public <E extends Entity> DeferredEntityTypeBuilder<E, EntityType<E>> registerType(String name, Supplier<EntityType.Builder<E>> sup) {
        return (DeferredEntityTypeBuilder<E, EntityType<E>>) super.register(name, () -> sup.get().build(name));
    }
}
