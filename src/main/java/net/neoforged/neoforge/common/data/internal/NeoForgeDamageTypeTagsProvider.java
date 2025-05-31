/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;

public final class NeoForgeDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public NeoForgeDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    private final Map<ResourceLocation, TagBuilder> vanillaBuilders = Maps.newLinkedHashMap();
    private boolean inVanilla;

    @Override
    protected TagAppender<ResourceKey<DamageType>, DamageType> tag(TagKey<DamageType> tag) {
        if (inVanilla) {
            return TagAppender.forBuilder(this.vanillaBuilders.computeIfAbsent(tag.location(), location -> TagBuilder.create()));
        }
        return super.tag(tag);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void addTags(HolderLookup.Provider lookupProvider) {
        // Collect vanilla tags
        inVanilla = true;
        super.addTags(lookupProvider);
        inVanilla = false;

        tag(Tags.DamageTypes.IS_POISON).addOptional(NeoForgeMod.POISON_DAMAGE);

        tag(DamageTypes.WITHER, Tags.DamageTypes.IS_WITHER);
        tag(DamageTypes.WITHER_SKULL, Tags.DamageTypes.IS_WITHER);

        tag(DamageTypes.MAGIC, Tags.DamageTypes.IS_MAGIC);
        tag(DamageTypes.INDIRECT_MAGIC, Tags.DamageTypes.IS_MAGIC);
        tag(DamageTypes.THORNS, Tags.DamageTypes.IS_MAGIC);
        tag(DamageTypes.DRAGON_BREATH, Tags.DamageTypes.IS_MAGIC);
        tag(Tags.DamageTypes.IS_MAGIC).addTags(Tags.DamageTypes.IS_POISON, Tags.DamageTypes.IS_WITHER);

        // Poisons should have the same behaviour as in vanilla
        addAsVanilla(DamageTypes.MAGIC).addTags(Tags.DamageTypes.IS_POISON);

        tag(DamageTypes.IN_FIRE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.ON_FIRE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.LAVA, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.HOT_FLOOR, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.DROWN, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.STARVE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.DRY_OUT, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FREEZE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.LIGHTNING_BOLT, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.CACTUS, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.STALAGMITE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FALLING_STALACTITE, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FALLING_BLOCK, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FALLING_ANVIL, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.CRAMMING, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.FLY_INTO_WALL, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.SWEET_BERRY_BUSH, Tags.DamageTypes.IS_ENVIRONMENT);
        tag(DamageTypes.IN_WALL, Tags.DamageTypes.IS_ENVIRONMENT);

        tag(DamageTypes.CACTUS, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.STALAGMITE, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALLING_STALACTITE, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALLING_BLOCK, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALLING_ANVIL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.CRAMMING, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FLY_INTO_WALL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.SWEET_BERRY_BUSH, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.FALL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.STING, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.MOB_ATTACK, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.PLAYER_ATTACK, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.MOB_ATTACK_NO_AGGRO, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.ARROW, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.THROWN, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.TRIDENT, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.MOB_PROJECTILE, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.SONIC_BOOM, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.IN_WALL, Tags.DamageTypes.IS_PHYSICAL);
        tag(DamageTypes.GENERIC, Tags.DamageTypes.IS_PHYSICAL);

        tag(DamageTypes.GENERIC_KILL, Tags.DamageTypes.IS_TECHNICAL);
        tag(DamageTypes.OUTSIDE_BORDER, Tags.DamageTypes.IS_TECHNICAL);
        tag(DamageTypes.FELL_OUT_OF_WORLD, Tags.DamageTypes.IS_TECHNICAL);
        tag(Tags.DamageTypes.NO_FLINCH);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tagWithOptionalLegacy(Tags.DamageTypes.IS_POISON);
        tagWithOptionalLegacy(Tags.DamageTypes.IS_WITHER);
        tagWithOptionalLegacy(Tags.DamageTypes.IS_MAGIC);
        tagWithOptionalLegacy(Tags.DamageTypes.IS_ENVIRONMENT);
        tagWithOptionalLegacy(Tags.DamageTypes.IS_PHYSICAL);
        tagWithOptionalLegacy(Tags.DamageTypes.IS_TECHNICAL);
        tagWithOptionalLegacy(Tags.DamageTypes.NO_FLINCH);
    }

    /** {@return an appender for vanilla tags that contain the given entry directly} */
    private TagAppender<ResourceKey<DamageType>, DamageType> addAsVanilla(ResourceKey<DamageType> entry) {
        final List<TagBuilder> builders = new ArrayList<>();
        vanillaBuilders.forEach((location, tagBuilder) -> {
            if (tagBuilder.build().stream().anyMatch(tagEntry -> tagEntry.verifyIfPresent(element -> element.equals(entry.location()), tag -> false))) {
                builders.add(getOrCreateRawBuilder(TagKey.create(registryKey, location)));
            }
        });
        return TagAppender.forBuilder(new TagBuilder() {
            @Override
            public TagBuilder add(TagEntry entry) {
                builders.forEach(builder -> builder.add(entry));
                return super.add(entry);
            }
        });
    }

    @SafeVarargs
    private void tag(ResourceKey<DamageType> type, TagKey<DamageType>... tags) {
        for (TagKey<DamageType> key : tags) {
            tag(key).add(type);
        }
    }

    private TagAppender<ResourceKey<DamageType>, DamageType> tagWithOptionalLegacy(TagKey<DamageType> tag) {
        TagAppender<ResourceKey<DamageType>, DamageType> tagAppender = tag(tag);
        tagAppender.addOptionalTag(TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("forge", tag.location().getPath())));
        return tagAppender;
    }

    @Override
    public String getName() {
        return "NeoForge Damage Type Tags";
    }
}
