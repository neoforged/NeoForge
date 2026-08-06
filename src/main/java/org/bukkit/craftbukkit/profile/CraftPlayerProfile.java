package org.bukkit.craftbukkit.profile;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.configuration.ConfigSerializationUtil;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerSkinPatch;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SerializableAs("PlayerProfile")
public final class CraftPlayerProfile implements PlayerProfile {

    @Nullable
    public static Property getProperty(@NotNull GameProfile profile, String propertyName) {
        return Iterables.getFirst(profile.properties().get(propertyName), null);
    }

    private final UUID uniqueId;
    private final String name;

    private final Multimap<String, Property> properties = LinkedListMultimap.create();
    private final CraftPlayerTextures textures = new CraftPlayerTextures(this);
    private CraftPlayerSkinPatch skinPatch = new CraftPlayerSkinPatch();

    private CraftPlayerProfile(UUID uniqueId, String name, boolean applyPreconditions) {
        if (applyPreconditions) {
            Preconditions.checkArgument((uniqueId != null) || !StringUtils.isBlank(name), "uniqueId is null or name is blank");
        }
        this.uniqueId = uniqueId;
        this.name = name;
    }

    public CraftPlayerProfile(UUID uniqueId, String name) {
        this(uniqueId, name, true);
    }

    public CraftPlayerProfile(NameAndId nameandid) {
        this(nameandid.id(), nameandid.name());
    }

    // The ResolvableProfile used in Components can have just the properties then need ignore all checks internally
    @ApiStatus.Internal
    public CraftPlayerProfile(@NotNull ResolvableProfile resolvableProfile) {
        this(resolvableProfile.partialProfile(), false);
        this.skinPatch = new CraftPlayerSkinPatch(resolvableProfile.skinPatch());
    }

    public CraftPlayerProfile(@NotNull GameProfile gameProfile) {
        this(gameProfile, true);
    }

    // The Map of properties of the given GameProfile is not immutable. This captures a snapshot of the properties of
    // the given GameProfile at the time this CraftPlayerProfile is created.
    private CraftPlayerProfile(@NotNull GameProfile gameProfile, boolean applyPreconditions) {
        this(gameProfile.id(), gameProfile.name(), applyPreconditions);
        this.properties.putAll(gameProfile.properties());
    }

    private CraftPlayerProfile(@NotNull CraftPlayerProfile other) {
        this(other.uniqueId, other.name);
        this.properties.putAll(other.properties);
        this.textures.copyFrom(other.textures);
        this.skinPatch = new CraftPlayerSkinPatch(other.skinPatch);
    }

    @Override
    public UUID getUniqueId() {
        return (Objects.equals(uniqueId, Util.NIL_UUID)) ? null : uniqueId;
    }

    @Override
    public String getName() {
        return (StringUtils.isBlank(name)) ? null : name;
    }

    @Nullable
    Property getProperty(String propertyName) {
        return Iterables.getFirst(properties.get(propertyName), null);
    }

    void setProperty(String propertyName, @Nullable Property property) {
        // Assert: (property == null) || property.getName().equals(propertyName)
        removeProperty(propertyName);
        if (property != null) {
            properties.put(property.name(), property);
        }
    }

    void removeProperty(String propertyName) {
        properties.removeAll(propertyName);
    }

    void rebuildDirtyProperties() {
        textures.rebuildPropertyIfDirty();
    }

    @Override
    public CraftPlayerTextures getTextures() {
        return textures;
    }

    @Override
    public void setTextures(@Nullable PlayerTextures textures) {
        if (textures == null) {
            this.textures.clear();
        } else {
            this.textures.copyFrom(textures);
        }
    }

    @Override
    public PlayerSkinPatch getSkinPatch() {
        return skinPatch;
    }

    @Override
    public void setSkinPatch(PlayerSkinPatch patch) {
        if (patch == null) {
            this.skinPatch = new CraftPlayerSkinPatch();
        } else {
            this.skinPatch = new CraftPlayerSkinPatch(patch);
        }
    }

    @Override
    public boolean isComplete() {
        return (getUniqueId() != null) && (getName() != null) && !textures.isEmpty();
    }

    @Override
    public CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.supplyAsync(this::getUpdatedProfile, Util.backgroundExecutor());
    }

    private CraftPlayerProfile getUpdatedProfile() {
        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile profile = this.buildGameProfile();

        // If missing, look up the uuid by name:
        if (profile.id().equals(Util.NIL_UUID)) {
            profile = server.services().nameToIdCache().get(profile.name()).map((resolved) -> new GameProfile(resolved.id(), resolved.name())).orElse(profile);
        }

        // Look up properties such as the textures:
        if (!profile.id().equals(Util.NIL_UUID)) {
            ProfileResult newProfile = server.services().sessionService().fetchProfile(profile.id(), true);
            if (newProfile != null) {
                profile = newProfile.profile();
            }
        }

        return new CraftPlayerProfile(profile);
    }

    // This always returns a new GameProfile instance to ensure that property changes to the original or previously
    // built ResolvableProfile don't affect the use of this profile in other contexts.
    @NotNull
    public ResolvableProfile buildResolvableProfile() {
        rebuildDirtyProperties();
        if (!textures.isEmpty()) {
            return new ResolvableProfile.Static(Either.left(buildGameProfile()), skinPatch.toMinecraft());
        } else if (getUniqueId() != null) {
            return new ResolvableProfile.Dynamic(Either.right(getUniqueId()), skinPatch.toMinecraft());
        } else if (getName() != null) {
            return new ResolvableProfile.Dynamic(Either.left(getName()), skinPatch.toMinecraft());
        } else {
            throw new IllegalArgumentException("The skull profile is missing a name, UUID and textures!");
        }
    }

    // This always returns a new GameProfile instance to ensure that property changes to the original or previously
    // built GameProfiles don't affect the use of this profile in other contexts.
    @NotNull
    public GameProfile buildGameProfile() {
        rebuildDirtyProperties();
        GameProfile profile = new GameProfile((uniqueId != null) ? uniqueId : Util.NIL_UUID, (name != null) ? name : "", new PropertyMap(properties));
        return profile;
    }

    @NotNull
    public NameAndId buildNameAndId() {
        return new NameAndId(uniqueId, name);
    }

    @Override
    public String toString() {
        rebuildDirtyProperties();
        StringBuilder builder = new StringBuilder();
        builder.append("CraftPlayerProfile [uniqueId=");
        builder.append(uniqueId);
        builder.append(", name=");
        builder.append(name);
        builder.append(", properties=");
        builder.append(properties);
        builder.append(", skinPatch=");
        builder.append(skinPatch);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CraftPlayerProfile other)) return false;
        if (!Objects.equals(uniqueId, other.uniqueId)) return false;
        if (!Objects.equals(name, other.name)) return false;

        rebuildDirtyProperties();
        other.rebuildDirtyProperties();
        if (!Objects.equals(properties, other.properties)) return false;
        if (!Objects.equals(skinPatch, other.skinPatch)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        rebuildDirtyProperties();
        int result = 1;
        result = 31 * result + Objects.hashCode(uniqueId);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(properties);
        result = 31 * result + Objects.hashCode(skinPatch);
        return result;
    }

    @Override
    public CraftPlayerProfile clone() {
        return new CraftPlayerProfile(this);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (this.uniqueId != null) {
            map.put("uniqueId", this.uniqueId.toString());
        }
        if (this.name != null) {
            map.put("name", this.name);
        }
        rebuildDirtyProperties();
        if (!this.properties.isEmpty()) {
            List<Object> propertiesData = new ArrayList<>();
            this.properties.forEach((propertyName, property) -> propertiesData.add(CraftProfileProperty.serialize(property)));
            map.put("properties", propertiesData);
        }
        if (!this.skinPatch.isEmpty()) {
            map.put("patch", this.skinPatch);
        }
        return map;
    }

    public static CraftPlayerProfile deserialize(Map<String, Object> map) {
        UUID uniqueId = ConfigSerializationUtil.getUuid(map, "uniqueId", true);
        String name = ConfigSerializationUtil.getString(map, "name", true);

        // This also validates the deserialized unique id and name (ensures that not both are null):
        CraftPlayerProfile profile = new CraftPlayerProfile(uniqueId, name);

        if (map.containsKey("properties")) {
            for (Object propertyData : (List<?>) map.get("properties")) {
                Preconditions.checkArgument(propertyData instanceof Map, "Property data (%s) is not a valid Map", propertyData);
                Property property = CraftProfileProperty.deserialize((Map<?, ?>) propertyData);
                profile.properties.put(property.name(), property);
            }
        }

        PlayerSkinPatch patch = ConfigSerializationUtil.getObject(PlayerSkinPatch.class, map, "patch", true);
        if (patch != null) {
            profile.skinPatch = new CraftPlayerSkinPatch(patch);
        }

        return profile;
    }
}
