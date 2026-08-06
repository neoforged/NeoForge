package org.bukkit.craftbukkit.block.data.type;

import org.bukkit.block.data.type.Bell;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

public abstract class CraftBell extends CraftBlockData implements Bell {

    private static final org.bukkit.craftbukkit.block.data.CraftBlockStateEnum<?, Attachment> ATTACHMENT = getEnum("attachment", Attachment.class);

    @Override
    public Attachment getAttachment() {
        return get(ATTACHMENT);
    }

    @Override
    public void setAttachment(Attachment leaves) {
        set(ATTACHMENT, leaves);
    }
}
