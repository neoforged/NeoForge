package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;

public class CraftRabbit extends CraftAnimals implements Rabbit {

    public CraftRabbit(CraftServer server, net.minecraft.world.entity.animal.rabbit.Rabbit entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.animal.rabbit.Rabbit getHandle() {
        return (net.minecraft.world.entity.animal.rabbit.Rabbit) entity;
    }

    @Override
    public String toString() {
        return "CraftRabbit{RabbitType=" + getRabbitType() + "}";
    }

    @Override
    public Type getRabbitType() {
        return Type.values()[getHandle().getVariant().ordinal()];
    }

    @Override
    public void setRabbitType(Type type) {
        getHandle().setVariant(net.minecraft.world.entity.animal.rabbit.Rabbit.Variant.values()[type.ordinal()]);
    }
}
