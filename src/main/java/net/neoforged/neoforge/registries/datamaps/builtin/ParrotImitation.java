package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record ParrotImitation(SoundEvent sound) {
    public static final Codec<ParrotImitation> SOUND_CODEC = BuiltInRegistries.SOUND_EVENT.byNameCodec()
            .xmap(ParrotImitation::new, ParrotImitation::sound);
    public static final Codec<ParrotImitation> CODEC = ExtraCodecs.withAlternative(RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound").forGetter(ParrotImitation::sound)
    ).apply(in, ParrotImitation::new)), SOUND_CODEC);
}
