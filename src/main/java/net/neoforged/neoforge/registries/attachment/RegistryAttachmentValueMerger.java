package net.neoforged.neoforge.registries.attachment;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

@FunctionalInterface
public interface RegistryAttachmentValueMerger<T, R> {
    T merge(Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> first, T firstValue, Either<TagKey<R>, ResourceKey<R>> second, T secondValue);

    static <T, R> RegistryAttachmentValueMerger<T, R> defaultMerger() {
        return (registry, first, firstValue, second, secondValue) -> secondValue;
    }
}
