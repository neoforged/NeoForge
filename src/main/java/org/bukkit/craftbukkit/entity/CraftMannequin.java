package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.profile.CraftPlayerProfile;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Pose;
import org.bukkit.entity.model.PlayerModelPart;
import org.bukkit.inventory.MainHand;
import org.bukkit.profile.PlayerProfile;

public class CraftMannequin extends CraftLivingEntity implements Mannequin {

    public CraftMannequin(CraftServer server, net.minecraft.world.entity.decoration.Mannequin entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftMannequin";
    }

    @Override
    public net.minecraft.world.entity.decoration.Mannequin getHandle() {
        return (net.minecraft.world.entity.decoration.Mannequin) super.getHandle();
    }

    @Override
    public MainHand getMainHand() {
        return getHandle().getMainArm() == HumanoidArm.LEFT ? MainHand.LEFT : MainHand.RIGHT;
    }

    @Override
    public void setMainHand(MainHand hand) {
        Preconditions.checkArgument(hand != null, "hand cannot be null");

        getHandle().setMainArm((hand == MainHand.LEFT) ? HumanoidArm.LEFT : HumanoidArm.RIGHT);
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        Preconditions.checkArgument(part != null, "part cannot be null");
        net.minecraft.world.entity.player.PlayerModelPart nms = net.minecraft.world.entity.player.PlayerModelPart.valueOf(part.name());

        return getHandle().isModelPartShown(nms);
    }

    @Override
    public void setModelPartShown(PlayerModelPart part, boolean shown) {
        Preconditions.checkArgument(part != null, "part cannot be null");
        net.minecraft.world.entity.player.PlayerModelPart nms = net.minecraft.world.entity.player.PlayerModelPart.valueOf(part.name());

        byte flags = getHandle().getEntityData().get(Avatar.DATA_PLAYER_MODE_CUSTOMISATION);
        if (shown) {
            flags |= nms.getMask();
        } else {
            flags &= ~nms.getMask();
        }

        getHandle().getEntityData().set(Avatar.DATA_PLAYER_MODE_CUSTOMISATION, flags);
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        return (getHandle().getProfile().equals(net.minecraft.world.entity.decoration.Mannequin.DEFAULT_PROFILE)) ? null : new CraftPlayerProfile(getHandle().getProfile());
    }

    @Override
    public void setPlayerProfile(PlayerProfile profile) {
        if (profile instanceof CraftPlayerProfile craftPlayerProfile) {
            getHandle().setProfile(craftPlayerProfile.buildResolvableProfile());
        } else {
            getHandle().setProfile(net.minecraft.world.entity.decoration.Mannequin.DEFAULT_PROFILE);
        }
    }

    @Override
    public void setPose(Pose pose) {
        Preconditions.checkArgument(pose != null, "pose cannot be null");

        net.minecraft.world.entity.Pose nmsPose = net.minecraft.world.entity.Pose.values()[pose.ordinal()];
        Preconditions.checkArgument(net.minecraft.world.entity.decoration.Mannequin.VALID_POSES.contains(nmsPose));

        getHandle().setPose(nmsPose);
    }

    @Override
    public boolean isImmovable() {
        return getHandle().getImmovable();
    }

    @Override
    public void setImmovable(boolean immovable) {
        getHandle().setImmovable(immovable);
    }

    @Override
    public String getDescripion() {
        return getDescription();
    }

    @Override
    public String getDescription() {
        Component description = getHandle().description;

        return (description != null) ? CraftChatMessage.fromComponent(description) : null;
    }

    @Override
    public void setDescription(String description) {
        if (description != null) {
            getHandle().setDescription(CraftChatMessage.fromStringOrNull(description));
        } else {
            getHandle().setDescription(net.minecraft.world.entity.decoration.Mannequin.DEFAULT_DESCRIPTION);

        }
    }

    @Override
    public boolean isHideDescription() {
        return getHandle().hideDescription;
    }

    @Override
    public void setHideDescription(boolean hide) {
        getHandle().setHideDescription(hide);
    }
}
