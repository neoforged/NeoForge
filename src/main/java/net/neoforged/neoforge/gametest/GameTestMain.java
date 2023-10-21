/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.gametest;

import net.minecraft.server.Main;

public class GameTestMain
{
    public static void main(String[] args) throws Exception
    {
        System.setProperty("neoforge.enableGameTest", "true");
        System.setProperty("neoforge.gameTestServer", "true");
        Main.main(args);
    }
}
