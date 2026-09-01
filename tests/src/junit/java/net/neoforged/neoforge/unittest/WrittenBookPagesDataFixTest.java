/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests that {@code ItemStackComponentizationFix} keeps the pages of a pre-1.20.5 {@code written_book}. The component
 * stores pages as stringified text components, so a legacy page that is not valid JSON fails to decode, and because
 * {@code pages} is a strict {@code optionalFieldOf} that error silently drops every page of the book.
 */
@ExtendWith(EphemeralTestServerProvider.class)
public class WrittenBookPagesDataFixTest {
    /** Data version of 1.20.1, i.e. the last release that stored book pages as free-form strings. */
    private static final int LEGACY_DATA_VERSION = 3465;

    private static ItemStack upgrade(MinecraftServer server, String legacySnbt) throws CommandSyntaxException {
        CompoundTag legacy = TagParser.parseTag(legacySnbt);
        Dynamic<Tag> fixed = DataFixers.getDataFixer()
                .update(
                        References.ITEM_STACK,
                        new Dynamic<>(NbtOps.INSTANCE, legacy),
                        LEGACY_DATA_VERSION,
                        SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        return ItemStack.parse(server.registryAccess(), fixed.getValue())
                .orElseThrow(() -> new AssertionError("Data-fixed item stack failed to parse at all: " + fixed.getValue()));
    }

    /** Plain text and empty pages are both invalid JSON, and either one used to drop the whole book. */
    @Test
    void plainTextPagesSurviveComponentization(MinecraftServer server) throws CommandSyntaxException {
        ItemStack stack = upgrade(server, """
                {id:"minecraft:written_book",Count:1b,tag:{pages:["Page 1","","Page 2"],title:"My Book",author:"Author"}}""");

        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        Assertions.assertThat(content).isNotNull();
        Assertions.assertThat(content.getPages(false))
                .withFailMessage("written_book pages were dropped by the componentization fixer, book was: %s", content)
                .map(Component::getString)
                .containsExactly("Page 1", "", "Page 2");
    }

    /** {@code filtered_pages} rides the same path, and pages without one must not gain a filtered variant. */
    @Test
    void filteredPagesSurviveComponentization(MinecraftServer server) throws CommandSyntaxException {
        ItemStack stack = upgrade(server, """
                {id:"minecraft:written_book",Count:1b,tag:{pages:["Page 1","Page 2"],filtered_pages:{"0":"Redacted"},title:"My Book",author:"Author"}}""");

        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        Assertions.assertThat(content).isNotNull();
        Assertions.assertThat(content.getPages(true)).map(Component::getString).containsExactly("Redacted", "Page 2");
        Assertions.assertThat(content.getPages(false)).map(Component::getString).containsExactly("Page 1", "Page 2");
    }

    /** Control: a page that already was a valid JSON component must not be wrapped into literal text. */
    @Test
    void jsonPagesAreNotDoubleWrapped(MinecraftServer server) throws CommandSyntaxException {
        ItemStack stack = upgrade(server, """
                {id:"minecraft:written_book",Count:1b,tag:{pages:['{"text":"Styled","bold":true}'],title:"My Book",author:"Author"}}""");

        WrittenBookContent content = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        Assertions.assertThat(content).isNotNull();
        Component page = content.getPages(false).get(0);
        Assertions.assertThat(page.getString()).isEqualTo("Styled");
        Assertions.assertThat(page.getStyle().isBold())
                .withFailMessage("An already-JSON page lost its styling, so it was wrapped as literal text")
                .isTrue();
    }

    /** Control: {@code writable_book} shares the {@code fixBookPages} helper but stores genuinely raw strings. */
    @Test
    void writableBookPagesStayRawText(MinecraftServer server) throws CommandSyntaxException {
        ItemStack stack = upgrade(server, """
                {id:"minecraft:writable_book",Count:1b,tag:{pages:["Page 1",'{"text":"not a component here"}']}}""");

        WritableBookContent content = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
        Assertions.assertThat(content).isNotNull();
        Assertions.assertThat(content.getPages(false)).containsExactly("Page 1", "{\"text\":\"not a component here\"}");
    }
}
