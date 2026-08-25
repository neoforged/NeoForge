package net.neoforged.neoforge.common.loot;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemInstance;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class NeoForgeLootContextParams {
    /// Holds the itemstack whose data is being queried with the loot context (i.e. the fuel item in a furnace).
    public static final ContextKey<ItemInstance> QUERIED_STACK = new ContextKey<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "queried_stack"));

    private NeoForgeLootContextParams() { }
}
