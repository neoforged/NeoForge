package org.bukkit.craftbukkit.inventory;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import org.bukkit.DyeColor;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.entity.CraftTropicalFish;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;

@DelegateDeserialization(SerializableMeta.class)
class CraftMetaTropicalFishBucket extends CraftMetaItem implements TropicalFishBucketMeta {

    static final ItemMetaKey VARIANT = new ItemMetaKey("BucketVariantTag", "fish-variant");
    static final ItemMetaKeyType<TypedEntityData<EntityType<?>>> ENTITY_TAG = new ItemMetaKeyType<>(DataComponents.ENTITY_DATA, "entity-tag");
    static final ItemMetaKeyType<CustomData> BUCKET_ENTITY_TAG = new ItemMetaKeyType<>(DataComponents.BUCKET_ENTITY_DATA, "bucket-entity-tag");
    static final Codec<TypedEntityData<EntityType<?>>> ENTITY_TAG_CODEC = TypedEntityData.codec(EntityType.CODEC);

    private Integer variant;
    private TypedEntityData<EntityType<?>> entityTag;
    private CompoundTag bucketEntityTag;

    CraftMetaTropicalFishBucket(CraftMetaItem meta) {
        super(meta);

        if (!(meta instanceof CraftMetaTropicalFishBucket)) {
            return;
        }

        CraftMetaTropicalFishBucket bucket = (CraftMetaTropicalFishBucket) meta;
        this.variant = bucket.variant;
        this.entityTag = bucket.entityTag;
        this.bucketEntityTag = bucket.bucketEntityTag;
    }

    CraftMetaTropicalFishBucket(DataComponentPatch tag) {
        super(tag);

        getOrEmpty(tag, ENTITY_TAG).ifPresent((nbt) -> {
            entityTag = nbt;

            entityTag.copyTagWithoutId().getInt(VARIANT.NBT).ifPresent((variant) -> {
                this.variant = variant;
            });
        });
        getOrEmpty(tag, BUCKET_ENTITY_TAG).ifPresent((nbt) -> {
            bucketEntityTag = nbt.copyTag();

            bucketEntityTag.getInt(VARIANT.NBT).ifPresent((variant) -> {
                this.variant = variant;
            });
        });
    }

    CraftMetaTropicalFishBucket(Map<String, Object> map) {
        super(map);

        Integer variant = SerializableMeta.getObject(Integer.class, map, VARIANT.BUKKIT, true);
        if (variant != null) {
            this.variant = variant;
        }
    }

    @Override
    void deserializeInternal(CompoundTag tag, Object context) {
        super.deserializeInternal(tag, context);

        ENTITY_TAG_CODEC.decode(NbtOps.INSTANCE, tag).ifSuccess((result) -> {
            entityTag = result.getFirst();
        });
        bucketEntityTag = tag.getCompound(BUCKET_ENTITY_TAG.NBT).orElse(bucketEntityTag);
    }

    @Override
    void serializeInternal(Map<String, Tag> internalTags) {
        if (entityTag != null) {
            internalTags.put(ENTITY_TAG.NBT, ENTITY_TAG_CODEC.encodeStart(NbtOps.INSTANCE, entityTag).getOrThrow());
        }
        if (bucketEntityTag != null && !bucketEntityTag.isEmpty()) {
            internalTags.put(BUCKET_ENTITY_TAG.NBT, bucketEntityTag);
        }
    }

    @Override
    void applyToItem(Applicator tag) {
        super.applyToItem(tag);

        if (entityTag != null) {
            tag.put(ENTITY_TAG, entityTag);
        }

        CompoundTag bucketEntityTag = (this.bucketEntityTag != null) ? this.bucketEntityTag.copy() : null;
        if (hasVariant()) {
            if (bucketEntityTag == null) {
                bucketEntityTag = new CompoundTag();
            }
            bucketEntityTag.putInt(VARIANT.NBT, variant);
        }

        if (bucketEntityTag != null) {
            tag.put(BUCKET_ENTITY_TAG, CustomData.of(bucketEntityTag));
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isBucketEmpty();
    }

    boolean isBucketEmpty() {
        return !(hasVariant() || entityTag != null || bucketEntityTag != null);
    }

    @Override
    public DyeColor getPatternColor() {
        return CraftTropicalFish.getPatternColor(variant);
    }

    @Override
    public void setPatternColor(DyeColor color) {
        if (variant == null) {
            variant = 0;
        }
        variant = CraftTropicalFish.getData(color, getPatternColor(), getPattern());
    }

    @Override
    public DyeColor getBodyColor() {
        return CraftTropicalFish.getBodyColor(variant);
    }

    @Override
    public void setBodyColor(DyeColor color) {
        if (variant == null) {
            variant = 0;
        }
        variant = CraftTropicalFish.getData(getPatternColor(), color, getPattern());
    }

    @Override
    public TropicalFish.Pattern getPattern() {
        return CraftTropicalFish.getPattern(variant);
    }

    @Override
    public void setPattern(TropicalFish.Pattern pattern) {
        if (variant == null) {
            variant = 0;
        }
        variant = CraftTropicalFish.getData(getPatternColor(), getBodyColor(), pattern);
    }

    @Override
    public boolean hasVariant() {
        return variant != null;
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaTropicalFishBucket) {
            CraftMetaTropicalFishBucket that = (CraftMetaTropicalFishBucket) meta;

            return (hasVariant() ? that.hasVariant() && this.variant.equals(that.variant) : !that.hasVariant())
                    && (entityTag != null ? that.entityTag != null && this.entityTag.equals(that.entityTag) : that.entityTag == null)
                    && (bucketEntityTag != null ? that.bucketEntityTag != null && this.bucketEntityTag.equals(that.bucketEntityTag) : that.bucketEntityTag == null);
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaTropicalFishBucket || isBucketEmpty());
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();

        if (hasVariant()) {
            hash = 61 * hash + variant;
        }
        if (entityTag != null) {
            hash = 61 * hash + entityTag.hashCode();
        }
        if (bucketEntityTag != null) {
            hash = 61 * hash + bucketEntityTag.hashCode();
        }

        return original != hash ? CraftMetaTropicalFishBucket.class.hashCode() ^ hash : hash;
    }

    @Override
    public CraftMetaTropicalFishBucket clone() {
        CraftMetaTropicalFishBucket clone = (CraftMetaTropicalFishBucket) super.clone();

        if (entityTag != null) {
            clone.entityTag = TypedEntityData.of(entityTag.type(), entityTag.copyTagWithoutId());
        }
        if (bucketEntityTag != null) {
            clone.bucketEntityTag = bucketEntityTag.copy();
        }

        return clone;
    }

    @Override
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        super.serialize(builder);

        if (hasVariant()) {
            builder.put(VARIANT.BUKKIT, variant);
        }

        return builder;
    }
}
