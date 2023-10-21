/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.DualStackUtils;
import net.neoforged.neoforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class MinecraftForge
{
    /**
     * The core Forge EventBusses, all events for Forge will be fired on these,
     * you should use this to register all your listeners.
     * This replaces every register*Handler() function in the old version of Forge.
     * TERRAIN_GEN_BUS for terrain gen events
     * ORE_GEN_BUS for ore gen events
     * EVENT_BUS for everything else
     */
    public static final IEventBus EVENT_BUS = BusBuilder.builder().startShutdown().classChecker(eventType -> {
        if (eventType.isAssignableFrom(IModBusEvent.class)) {
            throw new IllegalArgumentException("IModBusEvent events are not allowed on the Forge bus! Use a mod bus instead.");
        }
    }).build();

    static final ForgeInternalHandler INTERNAL_HANDLER = new ForgeInternalHandler();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Marker FORGE = MarkerManager.getMarker("FORGE");

   /**
    * Method invoked by FML before any other mods are loaded.
    */
   public static void initialize()
   {
       LOGGER.info(FORGE,"NeoForge v{} Initialized", ForgeVersion.getVersion());

       UsernameCache.load();
       TierSortingRegistry.init();
       if (FMLEnvironment.dist == Dist.CLIENT) ClientCommandHandler.init();
       DualStackUtils.initialise();
   }

/*
   public static void preloadCrashClasses(ASMDataTable table, String modID, Set<String> classes)
   {
       //Find all ICrashReportDetail's handlers and preload them.
       List<String> all = Lists.newArrayList();
       for (ASMData asm : table.getAll(ICrashReportDetail.class.getName().replace('.', '/')))
           all.add(asm.getClassName());
       for (ASMData asm : table.getAll(ICrashCallable.class.getName().replace('.', '/')))
           all.add(asm.getClassName());

       all.retainAll(classes);

       if (all.size() == 0)
        return;

       ForgeMod.log.debug("Preloading CrashReport Classes");
       Collections.sort(all); //Sort it because I like pretty output ;)
       for (String name : all)
       {
           ForgeMod.log.debug("\t{}", name);
           try
           {
               Class.forName(name.replace('/', '.'), false, MinecraftForge.class.getClassLoader());
           }
           catch (Exception e)
           {
               LOGGER.error("Could not find class for name '{}'.", name, e);
           }
       }
   }
*/
}
