/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An {@code EntityCapability} gives flexible access to objects of type {@code T} from entities.
 *
 * <h3>Querying an entity capability</h3>
 * <p>To get an object of type {@code T}, use {@link Entity#getCapability(EntityCapability)}.
 * For example, to query an item handler from an entity:
 * 
 * <pre>{@code
 * Entity entity = ...;
 *
 * IItemHandler maybeHandler = entity.getCapability(Capabilities.ItemHandler.ENTITY);
 * if (maybeHandler != null) {
 *     // Use maybeHandler
 * }
 * }</pre>
 *
 * <h3>Providing an entity capability</h3>
 * <p>To provide objects of type {@code T}, register providers to {@link RegisterCapabilitiesEvent}. For example:
 * 
 * <pre>{@code
 * modBus.addListener((RegisterCapabilitiesEvent event) -> {
 *     event.registerEntity(
 *         Capabilities.ItemHandler.ENTITY, // capability to register for
 *         MY_ENTITY_TYPE,
 *         (myEntity, context) -> <return the IItemHandler for myEntity>);
 * });
 * }</pre>
 *
 * @param <T> type of queried objects
 * @param <C> type of the additional context
 */
public final class EntityCapability<T, C> extends BaseCapability<T, C> {
    /**
     * Creates a new entity capability, or gets it if it already exists.
     *
     * @param name         name of the capability
     * @param typeClass    type of the queried API
     * @param contextClass type of the additional context
     */
    public static <T, C> EntityCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (EntityCapability<T, C>) registry.create(name, typeClass, contextClass);
    }

    /**
     * Creates a new entity capability with {@code Void} context, or gets it if it already exists.
     * This should be used for capabilities that do not require any additional context.
     *
     * @see #create(ResourceLocation, Class, Class)
     */
    public static <T> EntityCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    /**
     * Creates a new entity capability with nullable {@code Direction} context, or gets it if it already exists.
     * The side is generally the side from which the entity is being accessed, or {@code null} if it is not known or not a specific side.
     */
    public static <T> EntityCapability<T, @Nullable Direction> createSided(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    /**
     * {@return a new immutable copy of all the currently known entity capabilities}
     */
    public static synchronized List<EntityCapability<?, ?>> getAll() {
        return registry.getAll();
    }

    // INTERNAL
    private static final CapabilityRegistry<EntityCapability<?, ?>> registry = new CapabilityRegistry(EntityCapability::new);

    private EntityCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    final Map<EntityType<?>, List<ICapabilityProvider<Entity, C, T>>> providers = new IdentityHashMap<>();

    @ApiStatus.Internal
    @Nullable
    public T getCapability(Entity entity, C context) {
        for (var provider : providers.getOrDefault(entity.getType(), List.of())) {
            var ret = provider.getCapability(entity, context);
            if (ret != null)
                return ret;
        }
        return null;
    }
}
