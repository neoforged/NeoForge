package net.neoforged.testframework.registration;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Function;
import java.util.function.Supplier;

public class DeferredEntityTypeBuilder<E extends Entity, T extends EntityType<E>> extends DeferredHolder<EntityType<?>, T> {
    private final RegistrationHelper helper;
    protected DeferredEntityTypeBuilder(ResourceKey<EntityType<?>> key, RegistrationHelper helper) {
        super(key);
        this.helper = helper;
    }

    public DeferredEntityTypeBuilder<E, T> withRenderer(Supplier<Function<EntityRendererProvider.Context, EntityRenderer<E>>> renderer) {
        if (FMLLoader.getDist().isClient()) {
            helper.framework().modEventBus().addListener((final EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(value(), renderer.get()::apply));
        }
        return this;
    }

    public DeferredEntityTypeBuilder<E, T> withAttributes(Supplier<AttributeSupplier.Builder> attributes) {
        helper.framework().modEventBus().addListener((final EntityAttributeCreationEvent event) -> event.put((EntityType)get(), attributes.get().build()));
        return this;
    }

    public DeferredEntityTypeBuilder<E, T> withLang(String name) {
        helper.provider(LanguageProvider.class, prov -> prov.add(value(), name));
        return this;
    }
}
