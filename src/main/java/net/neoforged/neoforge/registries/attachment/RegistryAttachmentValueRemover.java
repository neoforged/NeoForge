package net.neoforged.neoforge.registries.attachment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Optional;

public interface RegistryAttachmentValueRemover<T, R> {
    Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source);

    class Default<T, R> implements RegistryAttachmentValueRemover<T, R> {
        public static final Default<?, ?> INSTANCE = new Default<>();
        public static <T, R> Default<T, R> defaultRemover() {
            return new Default<>();
        }
        public static <T, R> Codec<Default<T, R>> codec() {
            return Codec.unit(defaultRemover());
        }

        private Default() {}

        @Override
        public Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source) {
            return Optional.empty();
        }
    }
}
