package net.feltmc.neoforge.patches.interfaces;

import io.netty.channel.Channel;

public interface ConnectionInterface {
    default public Channel channel() {
        return null;
    }
}
