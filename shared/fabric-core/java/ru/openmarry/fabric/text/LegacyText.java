package ru.openmarry.fabric.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class LegacyText {

    private static final char CODE_MARKER = '&';

    private LegacyText() {
    }

    public static MutableComponent parse(String text) {
        MutableComponent result = Component.empty();
        StringBuilder segment = new StringBuilder();
        Style style = Style.EMPTY;

        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            ChatFormatting format = formatAt(text, i);
            if (format == null) {
                segment.append(current);
                continue;
            }
            append(result, segment, style);
            style = nextStyle(style, format);
            i++;
        }
        append(result, segment, style);
        return result;
    }

    private static ChatFormatting formatAt(String text, int index) {
        if (text.charAt(index) != CODE_MARKER || index + 1 >= text.length()) {
            return null;
        }
        return ChatFormatting.getByCode(text.charAt(index + 1));
    }

    private static Style nextStyle(Style current, ChatFormatting format) {
        if (format == ChatFormatting.RESET) {
            return Style.EMPTY;
        }
        if (format.isColor()) {
            return Style.EMPTY.applyFormat(format);
        }
        return current.applyFormat(format);
    }

    private static void append(MutableComponent target, StringBuilder segment, Style style) {
        if (segment.isEmpty()) {
            return;
        }
        target.append(Component.literal(segment.toString()).setStyle(style));
        segment.setLength(0);
    }
}
