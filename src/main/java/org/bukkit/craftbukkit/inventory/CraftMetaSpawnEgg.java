package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.TypedEntityData;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.meta.SpawnEggMeta;

@DelegateDeserialization(SerializableMeta.class)
public class CraftMetaSpawnEgg extends CraftMetaItem implements SpawnEggMeta {

    static final ItemMetaKeyType<TypedEntityData<net.minecraft.world.entity.EntityType<?>>> ENTITY_TAG = new ItemMetaKeyType<>(DataComponents.ENTITY_DATA, "entity-tag");
    static final Codec<TypedEntityData<net.minecraft.world.entity.EntityType<?>>> ENTITY_TAG_CODEC = TypedEntityData.codec(net.minecraft.world.entity.EntityType.CODEC);

    private TypedEntityData<net.minecraft.world.entity.EntityType<?>> entityTag;

    CraftMetaSpawnEgg(CraftMetaItem meta) {
        super(meta);

        if (!(meta instanceof CraftMetaSpawnEgg egg)) {
            return;
        }

        this.entityTag = egg.entityTag;
    }

    CraftMetaSpawnEgg(DataComponentPatch tag) {
        super(tag);

        getOrEmpty(tag, ENTITY_TAG).ifPresent((nbt) -> {
            entityTag = nbt;
        });
    }

    CraftMetaSpawnEgg(Map<String, Object> map) {
        super(map);
    }

    @Override
    void deserializeInternal(CompoundTag tag, Object context) {
        super.deserializeInternal(tag, context);

        tag.getCompound(ENTITY_TAG.NBT).ifPresent((entityTag) -> {
            ENTITY_TAG_CODEC.decode(NbtOps.INSTANCE, tag).ifSuccess((result) -> {
                this.entityTag = result.getFirst();
            });

            // Tag still has some other data, lets try our luck with a conversion
            if (!entityTag.isEmpty()) {
                // SPIGOT-4128: This is hopeless until we start versioning stacks. RIP data.
                // entityTag = (NBTTagCompound) MinecraftServer.getServer().dataConverterManager.update(DataConverterTypes.ENTITY, new Dynamic(DynamicOpsNBT.a, entityTag), -1, CraftMagicNumbers.DATA_VERSION).getValue();
            }
        });
    }

    @Override
    void serializeInternal(Map<String, Tag> internalTags) {
        if (entityTag != null) {
            internalTags.put(ENTITY_TAG.NBT, ENTITY_TAG_CODEC.encodeStart(NbtOps.INSTANCE, entityTag).getOrThrow());
        }
    }

    @Override
    void applyToItem(Applicator tag) {
        super.applyToItem(tag);

        if (entityTag != null) {
            tag.put(ENTITY_TAG, entityTag);
        }
    }

    @Override
    boolean isEmpty() {
        return super.isEmpty() && isSpawnEggEmpty();
    }

    boolean isSpawnEggEmpty() {
        return !(entityTag != null);
    }

    @Override
    public EntityType getSpawnedType() {
        throw new UnsupportedOperationException("Must check item type to get spawned type");
    }

    @Override
    public void setSpawnedType(EntityType type) {
        throw new UnsupportedOperationException("Must change item type to set spawned type");
    }

    @Override
    public EntitySnapshot getSpawnedEntity() {
        return CraftEntitySnapshot.create(this.entityTag);
    }

    @Override
    public void setSpawnedEntity(EntitySnapshot snapshot) {
        Preconditions.checkArgument(snapshot.getEntityType().isSpawnable(), "Entity is not spawnable");
        this.entityTag = ((CraftEntitySnapshot) snapshot).getEntityTag();
    }

    @Override
    boolean equalsCommon(CraftMetaItem meta) {
        if (!super.equalsCommon(meta)) {
            return false;
        }
        if (meta instanceof CraftMetaSpawnEgg) {
            CraftMetaSpawnEgg that = (CraftMetaSpawnEgg) meta;

            return entityTag != null ? that.entityTag != null && this.entityTag.equals(that.entityTag) : entityTag == null;
        }
        return true;
    }

    @Override
    boolean notUncommon(CraftMetaItem meta) {
        return super.notUncommon(meta) && (meta instanceof CraftMetaSpawnEgg || isSpawnEggEmpty());
    }

    @Override
    int applyHash() {
        final int original;
        int hash = original = super.applyHash();

        if (entityTag != null) {
            hash = 73 * hash + entityTag.hashCode();
        }

        return original != hash ? CraftMetaSpawnEgg.class.hashCode() ^ hash : hash;
    }

    @Override
    Builder<String, Object> serialize(Builder<String, Object> builder) {
        super.serialize(builder);

        return builder;
    }

    @Override
    public CraftMetaSpawnEgg clone() {
        CraftMetaSpawnEgg clone = (CraftMetaSpawnEgg) super.clone();

        if (entityTag != null) {
            clone.entityTag = TypedEntityData.of(entityTag.type(), entityTag.copyTagWithoutId());
        }

        return clone;
    }
}
