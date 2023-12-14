package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.Connection;
import net.minecraft.server.network.ConfigurationTask;

public interface IServerConfigurationPacketListenerExtension {
    void finishCurrentTask(ConfigurationTask.Type p_294853_);
    
    boolean isVanillaConnection();
    
    Connection getConnection();
}
