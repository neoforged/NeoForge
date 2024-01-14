package net.neoforged.neoforge.registries.attachment;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Map;
import java.util.Optional;

/**
 * An interface used to remove values from registry attachments. This allows "decomposing" the attachment
 * and removing only a specific part of it (like a specific key in the case of maps).
 *
 * @param <T> the attachment type
 * @param <R> the type of the registry this remover is for
 */
@FunctionalInterface
public interface RegistryAttachmentValueRemover<T, R> {

    /**
     * Remove the entry specified in this remover from the {@code value}.
     *
     * @param value    the attachment to remove. Do <b>NOT</b> mutate this object. You should return copies instead,
     *                 if you need to
     * @param registry the registry
     * @param source   the source of the attachment
     * @param object   the object to remove the attachment from
     * @return the removed attachment. If an {@link Optional#empty() empty optional}, the attachment will be removed
     * completely. Otherwise, this method returns the new value of the attachment.
     */
    Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, R object);

    /**
     * A remover that completely removes the attachment.
     *
     * @param <T> the type of the attachment
     * @param <R> the registry type
     */
    class Default<T, R> implements RegistryAttachmentValueRemover<T, R> {
        public static final Default<?, ?> INSTANCE = new Default<>();

        public static <T, R> Default<T, R> defaultRemover() {
            return new Default<>();
        }

        public static <T, R> Codec<Default<T, R>> codec() {
            return Codec.unit(defaultRemover());
        }

        private Default() {
        }

        @Override
        public Optional<T> remove(T value, Registry<R> registry, Either<TagKey<R>, ResourceKey<R>> source, R object) {
            return Optional.empty();
        }
    }
}
