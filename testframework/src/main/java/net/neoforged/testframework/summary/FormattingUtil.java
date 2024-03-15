package net.neoforged.testframework.summary;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public final class FormattingUtil {

    public static String componentToString(Component component, Style baseStyle, BiFunction<Style, String, String> formatter) {
        StringBuilder builder = new StringBuilder();
        component.visit((style, text) -> {
            if (style.isEmpty()) {
                builder.append(text);
            } else {
                builder.append(formatter.apply(style, text));
            }
            return Optional.empty();
        }, baseStyle);
        return builder.toString();
    }

    public static String componentToPlainString(Component component) {
        return componentToString(component, Style.EMPTY, (style, text) -> text);
    }

    public static String componentToAnsiFormattedText(Component component) {
        return componentToString(component, Style.EMPTY, FormattingUtil::toAnsi);
    }

    public static String componentsToAnsiFormattedText(List<Component> components) {
        return components.stream().map(FormattingUtil::componentToAnsiFormattedText).collect(Collectors.joining("\n"));
    }

    public static String componentToMarkdownFormattedText(Component component) {
        return componentToString(component, Style.EMPTY, FormattingUtil::toMarkdown);
    }

    public static String componentsToPlainString(List<Component> components) {
        return components.stream().map(FormattingUtil::componentToPlainString).collect(Collectors.joining("\n"));
    }

    public static String componentsToMarkdownFormattedText(List<Component> components) {
        return components.stream().map(FormattingUtil::componentToMarkdownFormattedText).collect(Collectors.joining("</br>"));
    }

    private static String toAnsi(Style style, String text) {
        StringBuilder builder = new StringBuilder();
        if (style.isBold()) builder.append(Ansi.BOLD);
        if (style.isItalic()) builder.append(Ansi.ITALIC);
        if (style.isUnderlined()) builder.append(Ansi.UNDERLINE);
        if (style.isStrikethrough()) builder.append(Ansi.STRIKE);
        if (style.getColor() != null) builder.append(Ansi.convertRGBToAnsiCode(style.getColor().getValue()));
        boolean empty = builder.isEmpty();
        builder.append(text);
        if (!empty) builder.append(Ansi.RESET);
        return builder.toString();
    }

    private static String toMarkdown(Style style, String text) {
        if (style.isObfuscated()) {
            text = text.replaceAll("\\w", "*");
        }
        if (style.isBold()) {
            text = "**" + text + "**";
        }
        if (style.isItalic()) {
            text = "*" + text + "*";
        }
        if (style.isStrikethrough()) {
            text = "~~" + text + "~~";
        }
        if (style.isUnderlined()) {
            text = "<u>" + text + "</u>";
        }
        if (style.getColor() != null) {
            text = "<font color=\"" + style.getColor().toString() + "\">" + text + "</font>";
        }
        return text;
    }

    public static final class Ansi {
        public static final String RESET = "\u001B[0m";
        public static final String BOLD = "\u001B[1m";
        public static final String ITALIC = "\u001B[3m";
        public static final String UNDERLINE = "\u001B[4m";
        public static final String STRIKE = "\u001B[9m";

        // ANSI 16-color palette
        private static final int[][] COLORS_RGB = {
                { 0, 0, 0 },       // 0 - Black
                { 128, 0, 0 },     // 1 - Dark Red
                { 0, 128, 0 },     // 2 - Dark Green
                { 128, 128, 0 },   // 3 - Dark Yellow
                { 0, 0, 128 },     // 4 - Dark Blue
                { 128, 0, 128 },   // 5 - Dark Magenta
                { 0, 128, 128 },   // 6 - Dark Cyan
                { 192, 192, 192 }, // 7 - Light Gray
                { 128, 128, 128 }, // 8 - Dark Gray
                { 255, 0, 0 },     // 9 - Red
                { 0, 255, 0 },     // 10 - Green
                { 255, 255, 0 },   // 11 - Yellow
                { 0, 0, 255 },     // 12 - Blue
                { 255, 0, 255 },   // 13 - Magenta
                { 0, 255, 255 },   // 14 - Cyan
                { 255, 255, 255 }  // 15 - White
        };

        public static String convertRGBToAnsiCode(int rgb) {
            // Extract RGB components
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;

            // Find the closest color in the ANSI palette
            double minDistance = Double.MAX_VALUE;
            int closestColorIndex1 = 0;

            // Iterate over the ANSI colors and find the one with the minimum distance
            for (int i = 0; i < COLORS_RGB.length; i++) {
                int[] ansiColor = COLORS_RGB[i];
                double distance = calculateDistance(red, green, blue, ansiColor[0], ansiColor[1], ansiColor[2]);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestColorIndex1 = i;
                }
            }
            int closestColorIndex = closestColorIndex1;

            if (closestColorIndex > 7) {
                return "\u001B[" + (90 + (closestColorIndex - 8)) + "m";
            }
            return "\u001B[" + (30 + closestColorIndex) + "m";
        }

        private static double calculateDistance(int red1, int green1, int blue1, int red2, int green2, int blue2) {
            // Convert CIE XYZ to CIE LAB
            double[] lab1 = xyzToLab(red1, green1, blue1);
            double[] lab2 = xyzToLab(red2, green2, blue2);

            // Compute Euclidean distance in CIE LAB space
            double deltaL = lab2[0] - lab1[0];
            double deltaA = lab2[1] - lab1[1];
            double deltaB = lab2[2] - lab1[2];
            return Math.sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB);
        }

        private static double[] xyzToLab(double r, double g, double b) {
            r /= 255.0;  //R from 0 to 255
            g /= 255.0;  //G from 0 to 255
            b /= 255.0;  //B from 0 to 255

            if (r > 0.04045) r = Math.pow(((r + 0.055) / 1.055), 2.4);
            else r = r / 12.92;
            if (g > 0.04045) g = Math.pow(((g + 0.055) / 1.055), 2.4);
            else g = g / 12.92;
            if (b > 0.04045) b = Math.pow(((b + 0.055) / 1.055), 2.4);
            else b = b / 12.92;

            r *= 100;
            g *= 100;
            b *= 100;

            // Observer. = 2°, Illuminant = D65
            double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
            double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
            double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

            x /= 95.047;         //ref_X =  95.047     Observer= 2°, Illuminant= D65
            y /= 100.000;        //ref_Y = 100.000
            z /= 108.883;        //ref_Z = 108.883

            if (x > 0.008856) x = Math.pow(x, 1.0 / 3);
            else x = (7.787 * x) + (16.0 / 116);
            if (y > 0.008856) y = Math.pow(y, 1.0 / 3);
            else y = (7.787 * y) + (16.0 / 116);
            if (z > 0.008856) z = Math.pow(z, 1.0 / 3);
            else z = (7.787 * z) + (16.0 / 116);

            return new double[] {
                    (116 * y) - 16, // CIE-L
                    500 * (x - y),  // CIE-a
                    200 * (y - z),  // CIE-b
            };
        }
    }
}
