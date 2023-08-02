package net.minecraftforge.registries.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public interface AttachmentTypeBuilder<A, T> {
    AttachmentTypeBuilder<A, T> provideDefault(Function<T, @Nullable A> provider);
    AttachmentTypeBuilder<A, T> withTagMerger(TagMerger<A, T> merger);
    AttachmentTypeBuilder<A, T> withAttachmentCodec(Codec<A> attachmentCodec);
    AttachmentTypeBuilder<A, T> withNetworkCodec(@Nullable Codec<A> networkCodec);
    AttachmentTypeBuilder<A, T> withMerger(ValueMerger<A> merger);
    AttachmentTypeBuilder<A, T> setOptionallySynced();

    AttachmentType<A, T> build();

    static <A, T> AttachmentTypeBuilder<A, T> builder(AttachmentTypeKey<A> key, ResourceKey<Registry<T>> registryKey) {
        return new AttachmentType.Builder<>(key, registryKey);
    }

    interface TagMerger<A, T> {
        TagMerger<?, ?> DEFAULT = (object, objectValue, tag, tagValue, tagFirst) -> tagFirst ? tagValue : objectValue;
        A merge(T object, A objectValue, TagKey<T> tag, A tagValue, boolean tagFirst);

        record MergeResult<A>(A value, boolean fromTag) {}
    }

    interface ValueMerger<A> {
        ValueMerger<?> DEFAULT = (o, n) -> n;
        A merge(A oldValue, A newValue);
    }
}
