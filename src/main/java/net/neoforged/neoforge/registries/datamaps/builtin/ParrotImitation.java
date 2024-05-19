/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/**
 * Data map value for {@link NeoForgeDataMaps#PARROT_IMITATIONS parrot imitations}.
 * 
 * @param sound the sound that the parrot will emit when imitating the mob
 */
public record ParrotImitation(SoundEvent sound) {
    public static final Codec<ParrotImitation> SOUND_CODEC = BuiltInRegistries.SOUND_EVENT.byNameCodec()
            .xmap(ParrotImitation::new, ParrotImitation::sound);
    public static final Codec<ParrotImitation> CODEC = Codec.withAlternative(RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound").forGetter(ParrotImitation::sound)).apply(in, ParrotImitation::new)), SOUND_CODEC);
}
