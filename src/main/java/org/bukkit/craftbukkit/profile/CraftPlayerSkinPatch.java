package org.bukkit.craftbukkit.profile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.configuration.ConfigSerializationUtil;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.profile.PlayerSkinPatch;
import org.bukkit.profile.PlayerTextures;

@SerializableAs("PlayerSkinPatch")
public final class CraftPlayerSkinPatch implements PlayerSkinPatch {

    private NamespacedKey texturePatch;
    private NamespacedKey capeTexturePatch;
    private NamespacedKey elytraTexturePatch;
    private PlayerTextures.SkinModel modelPatch;

    public CraftPlayerSkinPatch(NamespacedKey texturePatch, NamespacedKey capeTexturePatch, NamespacedKey elytraTexturePatch, PlayerTextures.SkinModel modelPatch) {
        this.texturePatch = texturePatch;
        this.capeTexturePatch = capeTexturePatch;
        this.elytraTexturePatch = elytraTexturePatch;
        this.modelPatch = modelPatch;
    }

    public CraftPlayerSkinPatch() {
        this(null, null, null, null);
    }

    public CraftPlayerSkinPatch(PlayerSkinPatch patch) {
        this(patch.getTexturePatch(), patch.getCapeTexturePatch(), patch.getElytraTexturePatch(), patch.getModelPatch());
    }

    public CraftPlayerSkinPatch(PlayerSkin.Patch patch) {
        this(
                patch.body().map(ClientAsset.ResourceTexture::id).map(CraftNamespacedKey::fromMinecraft).orElse(null),
                patch.cape().map(ClientAsset.ResourceTexture::id).map(CraftNamespacedKey::fromMinecraft).orElse(null),
                patch.elytra().map(ClientAsset.ResourceTexture::id).map(CraftNamespacedKey::fromMinecraft).orElse(null),
                patch.model().map((model) -> model == PlayerModelType.WIDE ? PlayerTextures.SkinModel.CLASSIC : PlayerTextures.SkinModel.valueOf(model.name())).orElse(null)
        );
    }

    @Override
    public NamespacedKey getTexturePatch() {
        return this.texturePatch;
    }

    @Override
    public void setTexturePatch(NamespacedKey texturePatch) {
        this.texturePatch = texturePatch;
    }

    @Override
    public NamespacedKey getCapeTexturePatch() {
        return this.capeTexturePatch;
    }

    @Override
    public void setCapeTexturePatch(NamespacedKey capeTexturePatch) {
        this.capeTexturePatch = capeTexturePatch;
    }

    @Override
    public NamespacedKey getElytraTexturePatch() {
        return this.elytraTexturePatch;
    }

    @Override
    public void setElytraTexturePatch(NamespacedKey elytraTexturePatch) {
        this.elytraTexturePatch = elytraTexturePatch;
    }

    @Override
    public PlayerTextures.SkinModel getModelPatch() {
        return this.modelPatch;
    }

    @Override
    public void setModelPatch(PlayerTextures.SkinModel modelPatch) {
        this.modelPatch = modelPatch;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.texturePatch);
        hash = 47 * hash + Objects.hashCode(this.capeTexturePatch);
        hash = 47 * hash + Objects.hashCode(this.elytraTexturePatch);
        hash = 47 * hash + Objects.hashCode(this.modelPatch);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CraftPlayerSkinPatch other = (CraftPlayerSkinPatch) obj;
        if (!Objects.equals(this.texturePatch, other.texturePatch)) {
            return false;
        }
        if (!Objects.equals(this.capeTexturePatch, other.capeTexturePatch)) {
            return false;
        }
        if (!Objects.equals(this.elytraTexturePatch, other.elytraTexturePatch)) {
            return false;
        }
        return this.modelPatch == other.modelPatch;
    }

    @Override
    public String toString() {
        return "CraftPlayerSkinPatch{" + "texturePatch=" + texturePatch + ", capeTexturePatch=" + capeTexturePatch + ", elytraTexturePatch=" + elytraTexturePatch + ", modelPatch=" + modelPatch + '}';
    }

    @Override
    public boolean isEmpty() {
        return this.texturePatch == null
                && this.capeTexturePatch == null
                && this.elytraTexturePatch == null
                && this.modelPatch == null;
    }

    public PlayerSkin.Patch toMinecraft() {
        return PlayerSkin.Patch.create(
                Optional.ofNullable(this.texturePatch).map(CraftNamespacedKey::toMinecraft).map(ClientAsset.ResourceTexture::new),
                Optional.ofNullable(this.capeTexturePatch).map(CraftNamespacedKey::toMinecraft).map(ClientAsset.ResourceTexture::new),
                Optional.ofNullable(this.elytraTexturePatch).map(CraftNamespacedKey::toMinecraft).map(ClientAsset.ResourceTexture::new),
                Optional.ofNullable(this.modelPatch).map((model) -> model == PlayerTextures.SkinModel.CLASSIC ? PlayerModelType.WIDE : PlayerModelType.valueOf(model.name()))
        );
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();

        if (this.texturePatch != null) {
            map.put("texture", this.texturePatch);
        }
        if (this.capeTexturePatch != null) {
            map.put("cape", this.capeTexturePatch);
        }
        if (this.elytraTexturePatch != null) {
            map.put("elytra", this.elytraTexturePatch);
        }
        if (this.modelPatch != null) {
            map.put("model", this.modelPatch.name());
        }

        return map;
    }

    public static CraftPlayerSkinPatch deserialize(Map<String, Object> map) {
        String texture = ConfigSerializationUtil.getString(map, "texture", true);
        String cape = ConfigSerializationUtil.getString(map, "cape", true);
        String elytra = ConfigSerializationUtil.getString(map, "elytra", true);
        String model = ConfigSerializationUtil.getString(map, "model", true);

        return new CraftPlayerSkinPatch(
                (texture != null) ? NamespacedKey.fromString(texture) : null,
                (cape != null) ? NamespacedKey.fromString(cape) : null,
                (elytra != null) ? NamespacedKey.fromString(elytra) : null,
                (model != null) ? PlayerTextures.SkinModel.valueOf(model) : null
        );
    }
}
