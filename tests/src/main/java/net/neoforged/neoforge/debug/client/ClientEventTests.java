package net.neoforged.neoforge.debug.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = {"client.event", "event"})
public class ClientEventTests {
    @TestHolder(description = {"Tests if the client chat event allows message modifications", "Will delete 'Cancel' and replace 'Replace this text'"})
    static void playerClientChatEvent(final ClientChatEvent event, final DynamicTest test) {
        if (event.getMessage().equals("Cancel")) {
            event.setCanceled(true);
            Minecraft.getInstance().tell(() ->
                    test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Was your message deleted?")));
        }
        else if (event.getMessage().equals("Replace this text")) {
            event.setMessage("Text replaced.");
            Minecraft.getInstance().tell(() ->
                    test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Was your message modified?")));
        }
    }
}
