package net.neoforged.neoforge.client;

import net.minecraft.resources.ResourceLocation;

/**
 * A group identifies a set of post-shader which is mutually exclusive.
 * The priority field controls in which order the various groups are applied
 */
public record PostChainGroup(ResourceLocation resourceLocation, int priority) {
    /**
     * Default post-effect group. Unused
     */
    public static final PostChainGroup DEFAULT = new PostChainGroup(ResourceLocation.withDefaultNamespace("default"), 0);
    /**
     * Group used by spectator entity effects
     */
    public static final PostChainGroup ENTITY_SHADERS = new PostChainGroup(ResourceLocation.withDefaultNamespace("entity_shaders"), 1);
}
