package org.bukkit.craftbukkit.block.data;

import org.bukkit.block.data.FaceAttachable;

public abstract class CraftFaceAttachable extends CraftBlockData implements FaceAttachable {

    private static final CraftBlockStateEnum<?, AttachedFace> ATTACH_FACE = getEnum("face", AttachedFace.class);

    @Override
    public AttachedFace getAttachedFace() {
        return get(ATTACH_FACE);
    }

    @Override
    public void setAttachedFace(AttachedFace face) {
        set(ATTACH_FACE, face);
    }
}
