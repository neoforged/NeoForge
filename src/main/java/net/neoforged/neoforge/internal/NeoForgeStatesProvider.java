/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import java.util.List;
import net.neoforged.fml.IModLoadingState;
import net.neoforged.fml.IModStateProvider;
import net.neoforged.fml.ModLoadingPhase;
import net.neoforged.fml.ModLoadingState;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegistryManager;

public class NeoForgeStatesProvider implements IModStateProvider {
    final ModLoadingState CREATE_REGISTRIES = ModLoadingState.withInline("CREATE_REGISTRIES", "CONSTRUCT", ModLoadingPhase.GATHER, ml -> RegistryManager.postNewRegistryEvent());
    final ModLoadingState INJECT_CAPABILITIES = ModLoadingState.withInline("INJECT_CAPABILITIES", "CREATE_REGISTRIES", ModLoadingPhase.GATHER, ml -> CapabilityManager.INSTANCE.injectCapabilities(ml.getAllScanData()));
    final ModLoadingState UNFREEZE = ModLoadingState.withInline("UNFREEZE_DATA", "INJECT_CAPABILITIES", ModLoadingPhase.GATHER, ml -> GameData.unfreezeData());
    final ModLoadingState LOAD_REGISTRIES = ModLoadingState.withInline("LOAD_REGISTRIES", "UNFREEZE_DATA", ModLoadingPhase.GATHER, ml -> GameData.postRegisterEvents());
    final ModLoadingState FREEZE = ModLoadingState.withInline("FREEZE_DATA", "LOAD_REGISTRIES", ModLoadingPhase.GATHER, ml -> GameData.freezeData());
    final ModLoadingState REGISTER_TICKET_CONTROLLERS = ModLoadingState.withInline("REGISTER_TICKET_CONTROLLERS", "COMPLETE", ModLoadingPhase.COMPLETE, ml -> ForcedChunkManager.init());
    final ModLoadingState NETLOCK = ModLoadingState.withInline("NETWORK_LOCK", "COMPLETE", ModLoadingPhase.COMPLETE, ml -> NetworkRegistry.lock());

    @Override
    public List<IModLoadingState> getAllStates() {
        return List.of(CREATE_REGISTRIES, INJECT_CAPABILITIES, UNFREEZE, LOAD_REGISTRIES, FREEZE, REGISTER_TICKET_CONTROLLERS, NETLOCK);
    }
}
