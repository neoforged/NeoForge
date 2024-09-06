/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

// TODO: Get this working during initial server spin up
// Does not work due to there being no server instance and potentially level data loaded
// need to hook into the WorldDataConfiguration system and load similarly to how vanilla FeatureFlags are loaded
/**
 * {@link ICondition Condition} used to conditionally load data files based on state of given set of {@link Flag Flags}.
 * <p>
 * All provided {@link #requiredFlags flags} must be enabled in order for data to pass this {@link ICondition condition}.
 *
 * @apiNote A {@code /reload} is be required to correctly reload data after toggling the state of a given {@link Flag}.
 */
public final class RequiredFlagsCondition implements ICondition {
    public static final MapCodec<RequiredFlagsCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            NeoForgeExtraCodecs.setOf(Flag.CODEC).fieldOf("flags").forGetter(condition -> condition.requiredFlags)).apply(builder, RequiredFlagsCondition::new));

    private final Set<Flag> requiredFlags;

    public RequiredFlagsCondition(Flag... requiredFlags) {
        this.requiredFlags = new ReferenceOpenHashSet<>(requiredFlags);
    }

    private RequiredFlagsCondition(Collection<Flag> requiredFlags) {
        this.requiredFlags = new ReferenceOpenHashSet<>(requiredFlags);
    }

    @Override
    public boolean test(IContext context) {
        return FlagManager.lookup().map(manager -> manager.isEnabled(requiredFlags)).orElseGet(FlagManager::shouldBeEnabledDefault);
    }

    @Override
    public MapCodec<RequiredFlagsCondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "required_flags(" + requiredFlags.stream().map(Flag::toStringShort).collect(Collectors.joining(", ", "\"", "\"")) + ')';
    }
}
