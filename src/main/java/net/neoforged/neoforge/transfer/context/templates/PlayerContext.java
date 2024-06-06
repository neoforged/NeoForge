package net.neoforged.neoforge.transfer.context.templates;

import net.minecraft.world.entity.player.Player;

public class PlayerContext extends SimpleItemContext {
    protected final Player player;

    public PlayerContext(Player player) {
        super();
        this.player = player;
    }
}
