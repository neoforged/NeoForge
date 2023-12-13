package net.neoforged.neoforge.network.event;

import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.LinkedList;
import java.util.Queue;

public class OnGameConfiguration extends Event implements IModBusEvent {

    private final ServerConfigurationPacketListener listener;
    
    private final Queue<ICustomConfigurationTask> configurationTasks = new LinkedList<>();
    
    public OnGameConfiguration(ServerConfigurationPacketListener listener) {
        this.listener = listener;
    }
    
    public void register(ICustomConfigurationTask task) {
        configurationTasks.add(task);
    }
    
    public Queue<ICustomConfigurationTask> getConfigurationTasks() {
        return new LinkedList<>(configurationTasks);
    }
    
    public ServerConfigurationPacketListener getListener() {
        return listener;
    }
}
