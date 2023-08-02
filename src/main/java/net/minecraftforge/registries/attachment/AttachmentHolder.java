package net.minecraftforge.registries.attachment;

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AttachmentHolder<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private Map<AttachmentTypeKey<?>, AttachmentType<?, T>> attachmentTypes;
    private Map<AttachmentTypeKey<?>, IdentityHashMap<Object, ?>> attachments;
    private Map<AttachmentTypeKey<?>, Map<Object, ?>> attachmentView;

    public AttachmentHolder(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;

        if (attachmentsRegistered) {
            populateMaps();
        }
    }

    public Map<AttachmentTypeKey<?>, AttachmentType<?, T>> getAttachmentTypes() {
        if (attachmentTypes == null) {
            attemptPopulate();
        }
        return attachmentTypes;
    }

    public Map<AttachmentTypeKey<?>, Map<Object, ?>> getAttachments() {
        if (attachmentView == null) {
            populateMaps();
        }
        return attachmentView;
    }

    @ApiStatus.Internal
    public void bindAttachments(Map<AttachmentTypeKey<?>, Map<Object, ?>> attachments) {
        if (this.attachments == null) {
            populateMaps();
        }
        this.attachments.values().forEach(Map::clear);
        attachments.forEach((key, value) -> {
            final var toReplaceIn = this.attachments.get(key);
            if (toReplaceIn == null) {
                throw new IllegalArgumentException("Attempted to bind attachments of type " + key.getId() + " to registry " + this.registryKey + " which does not support such attachments!");
            }
            toReplaceIn.putAll((Map)value);
        });
    }

    private void populateMaps() {
        this.attachmentTypes = (Map) Map.copyOf(registeredAttachmentTypes.getOrDefault(registryKey, Map.of()));
        this.attachments = attachmentTypes.keySet().stream().collect(Collectors.toMap(Function.identity(), k -> new IdentityHashMap<>(), (a, b) -> b, IdentityHashMap::new));
        this.attachmentView = attachments.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, m -> Collections.unmodifiableMap(m.getValue())));
    }

    private void attemptPopulate() {
        if (attachmentsRegistered) {
            populateMaps();
        } else {
            throw new IllegalStateException("Attachment holders are not yet initialised!");
        }
    }

    private static boolean attachmentsRegistered;
    private static Map<ResourceKey<Registry<?>>, Map<AttachmentTypeKey<?>, AttachmentType<?, ?>>> registeredAttachmentTypes;

    public static void init() {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != GameData.class) {
            throw new IllegalCallerException();
        }
        attachmentsRegistered = true;
        registeredAttachmentTypes = Map.copyOf(GameData.gatherAttachmentTypes());
    }

    public static Map<ResourceKey<? extends Registry<?>>, List<AttachmentTypeKey<?>>> getForciblySyncedAttachments() {
        final Map<ResourceKey<? extends Registry<?>>, List<AttachmentTypeKey<?>>> types = Maps.newHashMapWithExpectedSize(registeredAttachmentTypes.size());
        registeredAttachmentTypes.forEach((key, attachmentTypes) ->
                types.put(key, (List) attachmentTypes.values().stream()
                        .filter(t -> t.forciblySynced() && t.networkCodec() != null)
                        .map(AttachmentType::key)
                        .toList()));
        return types;
    }
}
