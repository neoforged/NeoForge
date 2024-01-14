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
import net.neoforged.neoforge.client.ClientRegistrationEvents;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegistryManager;

public class NeoForgeStatesProvider implements IModStateProvider {
    final ModLoadingState CREATE_REGISTRIES = ModLoadingState.withInline("CREATE_REGISTRIES", "CONSTRUCT", ModLoadingPhase.GATHER, ml -> RegistryManager.postNewRegistryEvent());
    final ModLoadingState UNFREEZE = ModLoadingState.withInline("UNFREEZE_DATA", "CREATE_REGISTRIES", ModLoadingPhase.GATHER, ml -> GameData.unfreezeData());
    final ModLoadingState LOAD_REGISTRIES = ModLoadingState.withInline("LOAD_REGISTRIES", "UNFREEZE_DATA", ModLoadingPhase.GATHER, ml -> GameData.postRegisterEvents());
    final ModLoadingState FREEZE = ModLoadingState.withInline("FREEZE_DATA", "LOAD_REGISTRIES", ModLoadingPhase.GATHER, ml -> GameData.freezeData());
    final ModLoadingState REGISTRATION_EVENTS = ModLoadingState.withInline("REGISTRATION_EVENTS", "CLIENT_REGISTRATION_EVENTS", ModLoadingPhase.LOAD, ml -> RegistrationEvents.init());
    final ModLoadingState CLIENT_REGISTRATION_EVENTS = ModLoadingState.withInline("CLIENT_REGISTRATION_EVENTS", "SIDED_SETUP", ModLoadingPhase.LOAD, ml -> ClientRegistrationEvents.init());
    final ModLoadingState NETLOCK = ModLoadingState.withInline("NETWORK_LOCK", "COMPLETE", ModLoadingPhase.COMPLETE, ml -> NetworkRegistry.getInstance().setup());

    @Override
    public List<IModLoadingState> getAllStates() {
        return List.of(CREATE_REGISTRIES, UNFREEZE, LOAD_REGISTRIES, FREEZE, REGISTRATION_EVENTS, CLIENT_REGISTRATION_EVENTS, NETLOCK);
    }
}
