/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.world;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

@Mod("raid_enum_test")
public class RaidEnumTest {
    private static final boolean ENABLE = false;
    @SuppressWarnings("unused") // referenced by enumextender.json
    public static final EnumProxy<Raid.RaiderType> RAIDER_ENUM_PARAMS = new EnumProxy<>(
            Raid.RaiderType.class,
            (Supplier<EntityType<?>>) () -> EntityType.ILLUSIONER,
            new int[] { 0, 5, 0, 1, 0, 1, 0, 2 });

    public RaidEnumTest() {
        if (ENABLE) {
            Raid.RaiderType type = RAIDER_ENUM_PARAMS.getValue();
        }
    }
}
