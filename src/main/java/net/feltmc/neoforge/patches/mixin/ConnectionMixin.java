package net.feltmc.neoforge.patches.mixin;

import io.netty.channel.Channel;
import net.feltmc.neoforge.patches.interfaces.ConnectionInterface;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Connection.class)
public class ConnectionMixin implements ConnectionInterface {
    @Shadow private Channel channel;

    @Override
    public Channel channel() {
        return this.channel;
    }
}
