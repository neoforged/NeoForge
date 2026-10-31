/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.attachment;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(side = Dist.CLIENT, groups = "attachment.session")
public class SessionAttachmentClientTests {
    @TestHolder(description = {
            "Tests that client-local session attachments are persisted per world or server",
            "Join any world or server, leave, and join it again: the test passes when the persisted join counter reaches 2" })
    static void clientSessionAttachmentPersistence(DynamicTest test, RegistrationHelper reg) {
        var joinCount = reg.attachments()
                .register("client_join_count", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("value")).build());

        test.eventListeners().forge().addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            var player = event.getPlayer();
            var session = player.connection.getSessionData();
            if (session != player.level().getSessionData()) {
                test.fail("Expected the client level to forward to the connection's session data");
                return;
            }

            int count = session.getData(joinCount) + 1;
            session.setData(joinCount, count);
            Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(Component.literal("Client session join count for this world or server: " + count));
            if (count >= 2) {
                test.pass();
            }
        });
    }
}
