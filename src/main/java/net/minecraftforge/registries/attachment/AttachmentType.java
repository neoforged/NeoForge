package net.minecraftforge.registries.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record AttachmentType<A, T>(
        AttachmentTypeKey<A> key, ResourceKey<Registry<T>> registryKey,
        Function<T, @Nullable A> defaultProvider,
        AttachmentTypeBuilder.TagMerger<A, T> tagMerger,
        AttachmentTypeBuilder.ValueMerger<A> valueMerger,
        Codec<A> attachmentCodec,
        @Nullable Codec<A> networkCodec,
        boolean forciblySynced
) {
    @ApiStatus.Internal
    public AttachmentType {}

    static final class Builder<A, T> implements AttachmentTypeBuilder<A, T> {
        private final AttachmentTypeKey<A> key;
        private final ResourceKey<Registry<T>> registryKey;
        private Function<T, @Nullable A> defaultProvider = a -> null;
        private Codec<A> attachmentCodec, networkCodec;
        private TagMerger<A, T> tagMerger = (TagMerger) TagMerger.DEFAULT;
        private ValueMerger<A> valueMerger = (ValueMerger) ValueMerger.DEFAULT;
        private boolean forciblySynced = true;

        Builder(AttachmentTypeKey<A> key, ResourceKey<Registry<T>> registryKey) {
            this.key = key;
            this.registryKey = registryKey;
        }

        @Override
        public AttachmentTypeBuilder<A, T> provideDefault(Function<T, @Nullable A> provider) {
            this.defaultProvider = provider;
            return this;
        }

        @Override
        public AttachmentTypeBuilder<A, T> withTagMerger(TagMerger<A, T> merger) {
            this.tagMerger = merger;
            return this;
        }

        public AttachmentTypeBuilder<A, T> withValueMerger(ValueMerger<A> merger) {
            this.valueMerger = merger;
            return this;
        }

        @Override
        public AttachmentTypeBuilder<A, T> withAttachmentCodec(Codec<A> attachmentCodec) {
            this.attachmentCodec = attachmentCodec;
            return this;
        }

        @Override
        public AttachmentTypeBuilder<A, T> withNetworkCodec(@Nullable Codec<A> networkCodec) {
            this.networkCodec = networkCodec;
            return this;
        }

        @Override
        public AttachmentTypeBuilder<A, T> withMerger(ValueMerger<A> merger) {
            this.valueMerger = merger;
            return this;
        }

        @Override
        public AttachmentTypeBuilder<A, T> setOptionallySynced() {
            this.forciblySynced = false;
            return this;
        }

        @Override
        public AttachmentType<A, T> build() {
            if (networkCodec == null) forciblySynced = false;
            return new AttachmentType<>(
                    key, registryKey, defaultProvider,
                    tagMerger,
                    valueMerger,
                    attachmentCodec, networkCodec,
                    forciblySynced
            );
        }
    }
}