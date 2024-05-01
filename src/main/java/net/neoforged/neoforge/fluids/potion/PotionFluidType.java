/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.potion;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class PotionFluidType extends FluidType {
    public PotionFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId(FluidStack stack) {
        var potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        return Potion.getName(potion, "%s.effect.".formatted(Items.POTION.getDescriptionId()));
    }

    @Override
    public ItemStack getBucket(FluidStack stack) {
        // if theres at least 1 bucket, we use the new potion bucket item
        // otherwise return a potion bottle
        var item = new ItemStack(stack.getAmount() % FluidType.BUCKET_VOLUME == 0 ? NeoForgeMod.POTION_BUCKET : Items.POTION);
        item.copyFrom(stack, DataComponents.POTION_CONTENTS);
        return item;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new PotionFluidTypeClientExtensions());
    }
}
