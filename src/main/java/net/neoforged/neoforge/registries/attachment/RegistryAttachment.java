package net.neoforged.neoforge.registries.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record RegistryAttachment<T, R, VR extends RegistryAttachmentValueRemover<T, R>>(
        ResourceLocation id,
        Codec<T> codec, @Nullable Codec<T> networkCodec,
        boolean mandatorySync,
        Function<R, T> defaultValue,
        Codec<VR> remover,
        RegistryAttachmentValueMerger<T, R> merger
) {
}
