package org.bukkit.craftbukkit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

public final class CraftChatMessage {

    private static final Pattern LINK_PATTERN = Pattern.compile("((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(ChatColor.COLOR_CHAR) + " \\n]|$))))");
    private static final Map<Character, ChatFormatting> formatMap;

    static {
        Builder<Character, ChatFormatting> builder = ImmutableMap.builder();
        for (ChatFormatting format : ChatFormatting.values()) {
            builder.put(Character.toLowerCase(format.toString().charAt(1)), format);
        }
        formatMap = builder.build();
    }

    public static ChatFormatting getColor(ChatColor color) {
        return formatMap.get(color.getChar());
    }

    public static ChatColor getColor(ChatFormatting format) {
        return ChatColor.getByChar(format.code);
    }

    private static final class StringMessage {
        private static final Pattern INCREMENTAL_PATTERN = Pattern.compile("(" + String.valueOf(ChatColor.COLOR_CHAR) + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(ChatColor.COLOR_CHAR) + " \\n]|$))))|(\\n)", Pattern.CASE_INSENSITIVE);
        // Separate pattern with no group 3, new lines are part of previous string
        private static final Pattern INCREMENTAL_PATTERN_KEEP_NEWLINES = Pattern.compile("(" + String.valueOf(ChatColor.COLOR_CHAR) + "[0-9a-fk-orx])|((?:(?:https?):\\/\\/)?(?:[-\\w_\\.]{2,}\\.[a-z]{2,4}.*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(ChatColor.COLOR_CHAR) + " ]|$))))", Pattern.CASE_INSENSITIVE);
        // ChatColor.b does not explicitly reset, its more of empty
        private static final Style RESET = Style.EMPTY.withBold(false).withItalic(false).withUnderlined(false).withStrikethrough(false).withObfuscated(false);

        private final List<Component> list = new ArrayList<Component>();
        private MutableComponent currentChatComponent = Component.empty();
        private Style modifier = Style.EMPTY;
        private final Component[] output;
        private int currentIndex;
        private StringBuilder hex;
        private final String message;

        private StringMessage(String message, boolean keepNewlines, boolean plain) {
            this.message = message;
            if (message == null) {
                output = new Component[]{currentChatComponent};
                return;
            }
            list.add(currentChatComponent);

            Matcher matcher = (keepNewlines ? INCREMENTAL_PATTERN_KEEP_NEWLINES : INCREMENTAL_PATTERN).matcher(message);
            String match = null;
            boolean needsAdd = false;
            while (matcher.find()) {
                int groupId = 0;
                while ((match = matcher.group(++groupId)) == null) {
                    // NOOP
                }
                int index = matcher.start(groupId);
                if (index > currentIndex) {
                    needsAdd = false;
                    appendNewComponent(index);
                }
                switch (groupId) {
                case 1:
                    char c = match.toLowerCase(Locale.ROOT).charAt(1);
                    ChatFormatting format = formatMap.get(c);

                    if (c == 'x') {
                        hex = new StringBuilder("#");
                    } else if (hex != null) {
                        hex.append(c);

                        if (hex.length() == 7) {
                            modifier = RESET.withColor(TextColor.parseColor(hex.toString()).result().get());
                            hex = null;
                        }
                    } else if (format.compareTo(ChatFormatting.WHITE) > 0 && format != ChatFormatting.RESET) {
                        switch (format) {
                        case BOLD:
                            modifier = modifier.withBold(Boolean.TRUE);
                            break;
                        case ITALIC:
                            modifier = modifier.withItalic(Boolean.TRUE);
                            break;
                        case STRIKETHROUGH:
                            modifier = modifier.withStrikethrough(Boolean.TRUE);
                            break;
                        case UNDERLINE:
                            modifier = modifier.withUnderlined(Boolean.TRUE);
                            break;
                        case OBFUSCATED:
                            modifier = modifier.withObfuscated(Boolean.TRUE);
                            break;
                        default:
                            throw new AssertionError("Unexpected message format");
                        }
                    } else { // Color resets formatting
                        modifier = RESET.withColor(format);
                    }
                    needsAdd = true;
                    break;
                case 2:
                    if (plain) {
                        appendNewComponent(matcher.end(groupId));
                    } else {
                        if (!(match.startsWith("http://") || match.startsWith("https://"))) {
                            match = "http://" + match;
                        }
                        try {
                            modifier = modifier.withClickEvent(new ClickEvent.OpenUrl(Util.parseAndValidateUntrustedUri(match)));
                        } catch (URISyntaxException ex) {
                        }
                        appendNewComponent(matcher.end(groupId));
                        modifier = modifier.withClickEvent((ClickEvent) null);
                    }
                    break;
                case 3:
                    if (needsAdd) {
                        appendNewComponent(index);
                    }
                    currentChatComponent = null;
                    break;
                }
                currentIndex = matcher.end(groupId);
            }

            if (currentIndex < message.length() || needsAdd) {
                appendNewComponent(message.length());
            }

            output = list.toArray(new Component[list.size()]);
        }

        private void appendNewComponent(int index) {
            Component addition = Component.literal(message.substring(currentIndex, index)).setStyle(modifier);
            currentIndex = index;
            if (currentChatComponent == null) {
                currentChatComponent = Component.empty();
                list.add(currentChatComponent);
            }
            currentChatComponent.append(addition);
        }

        private Component[] getOutput() {
            return output;
        }
    }

    public static Optional<Component> fromStringOrOptional(String message) {
        return Optional.ofNullable(fromStringOrNull(message));
    }

    public static Optional<Component> fromStringOrOptional(String message, boolean keepNewlines) {
        return Optional.ofNullable(fromStringOrNull(message, keepNewlines));
    }

    public static Component fromStringOrNull(String message) {
        return fromStringOrNull(message, false);
    }

    public static Component fromStringOrNull(String message, boolean keepNewlines) {
        return (message == null || message.isEmpty()) ? null : fromString(message, keepNewlines)[0];
    }

    public static Component fromStringOrEmpty(String message) {
        return fromStringOrEmpty(message, false);
    }

    public static Component fromStringOrEmpty(String message, boolean keepNewlines) {
        return fromString(message, keepNewlines)[0];
    }

    public static Component[] fromString(String message) {
        return fromString(message, false);
    }

    public static Component[] fromString(String message, boolean keepNewlines) {
        return fromString(message, keepNewlines, false);
    }

    public static Component[] fromString(String message, boolean keepNewlines, boolean plain) {
        return new StringMessage(message, keepNewlines, plain).getOutput();
    }

    public static String toJSON(Component component) {
        return ChatSerializer.toJson(component, MinecraftServer.getDefaultRegistryAccess());
    }

    public static String toJSONOrNull(Component component) {
        if (component == null) return null;
        return toJSON(component);
    }

    public static Component fromJSON(String jsonMessage) throws JsonParseException {
        // Note: This also parses plain Strings to text components.
        // Note: An empty message (empty, or only consisting of whitespace) results in null rather than a parse exception.
        return ChatSerializer.fromJson(jsonMessage, MinecraftServer.getDefaultRegistryAccess());
    }

    public static Component fromJSONOrNull(String jsonMessage) {
        if (jsonMessage == null) return null;
        try {
            return fromJSON(jsonMessage); // Can return null
        } catch (JsonParseException ex) {
            return null;
        }
    }

    public static Component fromJSONOrString(String message) {
        return fromJSONOrString(message, false);
    }

    public static Component fromJSONOrString(String message, boolean keepNewlines) {
        return fromJSONOrString(message, false, keepNewlines);
    }

    public static Component fromJSONOrString(String message, boolean nullable, boolean keepNewlines) {
        return fromJSONOrString(message, nullable, keepNewlines, Integer.MAX_VALUE, false);
    }

    public static Component fromJSONOrString(String message, boolean nullable, boolean keepNewlines, int maxLength, boolean checkJsonContentLength) {
        if (message == null) message = "";
        if (nullable && message.isEmpty()) return null;
        Component component = fromJSONOrNull(message);
        if (component != null) {
            if (checkJsonContentLength) {
                String content = fromComponent(component);
                String trimmedContent = trimMessage(content, maxLength);
                if (content != trimmedContent) { // Identity comparison is fine here
                    // Note: The resulting text has all non-plain text features stripped.
                    return fromString(trimmedContent, keepNewlines)[0];
                }
            }
            return component;
        } else {
            message = trimMessage(message, maxLength);
            return fromString(message, keepNewlines)[0];
        }
    }

    public static String trimMessage(String message, int maxLength) {
        if (message != null && message.length() > maxLength) {
            return message.substring(0, maxLength);
        } else {
            return message;
        }
    }

    public static String fromComponent(Component component) {
        if (component == null) return "";
        StringBuilder out = new StringBuilder();

        boolean hadFormat = false;
        for (Component c : component) {
            Style modi = c.getStyle();
            TextColor color = modi.getColor();
            if (c.getContents() != PlainTextContents.EMPTY || color != null) {
                if (color != null) {
                    if (color.format != null) {
                        out.append(color.format);
                    } else {
                        out.append(ChatColor.COLOR_CHAR).append("x");
                        for (char magic : color.serialize().substring(1).toCharArray()) {
                            out.append(ChatColor.COLOR_CHAR).append(magic);
                        }
                    }
                    hadFormat = true;
                } else if (hadFormat) {
                    out.append(ChatColor.RESET);
                    hadFormat = false;
                }
            }
            if (modi.isBold()) {
                out.append(ChatFormatting.BOLD);
                hadFormat = true;
            }
            if (modi.isItalic()) {
                out.append(ChatFormatting.ITALIC);
                hadFormat = true;
            }
            if (modi.isUnderlined()) {
                out.append(ChatFormatting.UNDERLINE);
                hadFormat = true;
            }
            if (modi.isStrikethrough()) {
                out.append(ChatFormatting.STRIKETHROUGH);
                hadFormat = true;
            }
            if (modi.isObfuscated()) {
                out.append(ChatFormatting.OBFUSCATED);
                hadFormat = true;
            }
            c.getContents().visit((x) -> {
                out.append(x);
                return Optional.empty();
            });
        }
        return out.toString();
    }

    public static Component fixComponent(MutableComponent component) {
        Matcher matcher = LINK_PATTERN.matcher("");
        return fixComponent(component, matcher);
    }

    private static Component fixComponent(MutableComponent component, Matcher matcher) {
        if (component.getContents() instanceof PlainTextContents) {
            PlainTextContents text = ((PlainTextContents) component.getContents());
            String msg = text.text();
            if (matcher.reset(msg).find()) {
                matcher.reset();

                Style modifier = component.getStyle();
                List<Component> extras = new ArrayList<Component>();
                List<Component> extrasOld = new ArrayList<Component>(component.getSiblings());
                component = Component.empty();

                int pos = 0;
                while (matcher.find()) {
                    String match = matcher.group();

                    if (!(match.startsWith("http://") || match.startsWith("https://"))) {
                        match = "http://" + match;
                    }

                    MutableComponent prev = Component.literal(msg.substring(pos, matcher.start()));
                    prev.setStyle(modifier);
                    extras.add(prev);

                    MutableComponent link = Component.literal(matcher.group());
                    try {
                        Style linkModi = modifier.withClickEvent(new ClickEvent.OpenUrl(Util.parseAndValidateUntrustedUri(match)));
                        link.setStyle(linkModi);
                    } catch (URISyntaxException ex) {
                    }
                    extras.add(link);

                    pos = matcher.end();
                }

                MutableComponent prev = Component.literal(msg.substring(pos));
                prev.setStyle(modifier);
                extras.add(prev);
                extras.addAll(extrasOld);

                for (Component c : extras) {
                    component.append(c);
                }
            }
        }

        List<Component> extras = component.getSiblings();
        for (int i = 0; i < extras.size(); i++) {
            Component comp = extras.get(i);
            if (comp.getStyle() != null && comp.getStyle().getClickEvent() == null) {
                extras.set(i, fixComponent(comp.copy(), matcher));
            }
        }

        if (component.getContents() instanceof TranslatableContents) {
            Object[] subs = ((TranslatableContents) component.getContents()).getArgs();
            for (int i = 0; i < subs.length; i++) {
                Object comp = subs[i];
                if (comp instanceof Component) {
                    Component c = (Component) comp;
                    if (c.getStyle() != null && c.getStyle().getClickEvent() == null) {
                        subs[i] = fixComponent(c.copy(), matcher);
                    }
                } else if (comp instanceof String && matcher.reset((String) comp).find()) {
                    subs[i] = fixComponent(Component.literal((String) comp), matcher);
                }
            }
        }

        return component;
    }

    private CraftChatMessage() {
    }

    public static class ChatSerializer {

        private static final Gson GSON = (new GsonBuilder()).disableHtmlEscaping().create();

        private ChatSerializer() {
        }

        private static MutableComponent deserialize(JsonElement jsonelement, HolderLookup.Provider holderlookup_a) {
            return (MutableComponent) ComponentSerialization.CODEC.parse(holderlookup_a.createSerializationContext(JsonOps.INSTANCE), jsonelement).getOrThrow(JsonParseException::new);
        }

        private static JsonElement serialize(Component component, HolderLookup.Provider holderlookup_a) {
            return ComponentSerialization.CODEC.encodeStart(holderlookup_a.createSerializationContext(JsonOps.INSTANCE), component).getOrThrow(JsonParseException::new);
        }

        public static String toJson(Component component, HolderLookup.Provider holderlookup_a) {
            return GSON.toJson(serialize(component, holderlookup_a));
        }

        @Nullable
        public static MutableComponent fromJson(String s, HolderLookup.Provider holderlookup_a) {
            JsonElement jsonelement = JsonParser.parseString(s);

            return jsonelement == null ? null : deserialize(jsonelement, holderlookup_a);
        }
    }
}
