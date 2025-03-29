/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attribute.Sentiment;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.config.NeoForgeCommonConfig;
import net.neoforged.neoforge.common.util.AttributeUtil;
import org.jetbrains.annotations.Nullable;

public interface IAttributeExtension {
    public static final DecimalFormat FORMAT = Util.make(new DecimalFormat("#.##"), fmt -> fmt.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));;

    /**
     * Converts the value of an attribute modifier to the value that will be displayed.
     * <p>
     * For multiplicative modifiers, this method is responsible for converting the value to percentage form.
     *
     * @param op    The operation of the modifier. Null if we are just displaying the raw value and not a modifier.
     * @param value The value to convert. Either the current attribute value (if null operation) or the attribute modifier's amount.
     * @param flag  The tooltip flag.
     * @return The component form of the formatted value.
     */
    default MutableComponent toValueComponent(@Nullable Operation op, double value, TooltipFlag flag) {
        if (isNullOrAddition(op)) {
            return Component.translatable("neoforge.value.flat", FORMAT.format(value));
        }

        return Component.translatable("neoforge.value.percent", FORMAT.format(value * 100));
    }

    /**
     * Converts an attribute modifier into its tooltip representation.
     * <p>
     * This method does not handle formatting of "base" modifiers, such as Attack Damage or Attack Speed.
     * <p>
     * The returned component may append additional debug information based on the tooltip flag.
     *
     * @param modif The attribute modifier being converted to a component.
     * @param flag  The tooltip flag.
     * @return The component representation of the passed attribute modifier, with debug info appended if enabled.
     */
    default MutableComponent toComponent(AttributeModifier modif, TooltipFlag flag) {
        Attribute attr = self();
        double value = modif.amount();
        String key = value > 0 ? "neoforge.modifier.plus" : "neoforge.modifier.take";
        ChatFormatting color = attr.getStyle(value > 0);

        Component attrDesc = Component.translatable(attr.getDescriptionId());
        Component valueComp = this.toValueComponent(modif.operation(), value, flag);
        MutableComponent comp = Component.translatable(key, valueComp, attrDesc).withStyle(color);

        return comp.append(this.getDebugInfo(modif, flag));
    }

    /**
     * Computes the additional debug information for a given attribute modifier, if the flag {@linkplain TooltipFlag#isAdvanced() is advanced}.
     * 
     * @param modif The attribute modifier being converted to a component.
     * @param flag  The tooltip flag.
     * @return The debug component, or {@link CommonComponents#EMPTY} if disabled.
     * @apiNote This information is automatically appended to {@link #toComponent(AttributeModifier, TooltipFlag)}.
     */
    default Component getDebugInfo(AttributeModifier modif, TooltipFlag flag) {
        Component debugInfo = CommonComponents.EMPTY;

        if (flag.isAdvanced() && NeoForgeCommonConfig.INSTANCE.attributeAdvancedTooltipDebugInfo.get()) {
            // Advanced Tooltips show the underlying operation and the "true" value. We offset MULTIPLY_TOTAL by 1 due to how the operation is calculated.
            double advValue = (modif.operation() == Operation.ADD_MULTIPLIED_TOTAL ? 1 : 0) + modif.amount();
            String valueStr = FORMAT.format(advValue);
            String txt = switch (modif.operation()) {
                case ADD_VALUE -> String.format(Locale.ROOT, advValue > 0 ? "[+%s]" : "[%s]", valueStr);
                case ADD_MULTIPLIED_BASE -> String.format(Locale.ROOT, advValue > 0 ? "[+%sx]" : "[%sx]", valueStr);
                case ADD_MULTIPLIED_TOTAL -> String.format(Locale.ROOT, "[x%s]", valueStr);
            };
            debugInfo = Component.literal(" ").append(Component.literal(txt).withStyle(ChatFormatting.GRAY));
        }
        return debugInfo;
    }

    /**
     * Gets the specific ID that represents a "base" (green) modifier for this attribute.
     *
     * @return The ID of the "base" modifier, or null, if no such modifier may exist.
     *
     * @apiNote Base modifiers always operate as if they were using {@link Operation#ADD_VALUE}.
     */
    @Nullable
    default ResourceLocation getBaseId() {
        if (this == Attributes.ATTACK_DAMAGE.value()) return AttributeUtil.BASE_ATTACK_DAMAGE_ID;
        else if (this == Attributes.ATTACK_SPEED.value()) return AttributeUtil.BASE_ATTACK_SPEED_ID;
        else if (this == Attributes.ENTITY_INTERACTION_RANGE.value()) return AttributeUtil.BASE_ENTITY_REACH_ID;
        return null;
    }

    /**
     * Converts a "base" attribute modifier (as dictated by {@link #getBaseId()}) into a text component.
     * <p>
     * Similar to {@link #toComponent}, this method is responsible for adding debug information when the tooltip flag {@linkplain TooltipFlag#isAdvanced() is advanced}.
     *
     * @param value      The value to be shown (after having been added to the entity's base value)
     * @param entityBase The entity's base value for this attribute from {@link LivingEntity#getAttributeBaseValue(Holder)}.
     * @param merged     If we are displaying a merged base component (which will have a non-merged base component as a child).
     * @param flag       The tooltip flag.
     * @return The component representation of the passed attribute modifier.
     * 
     * @apiNote Base modifiers always operate as if they were using {@link Operation#ADD_VALUE}.
     */
    default MutableComponent toBaseComponent(double value, double entityBase, boolean merged, TooltipFlag flag) {
        Attribute attr = self();
        MutableComponent comp = Component.translatable("attribute.modifier.equals.0", FORMAT.format(value), Component.translatable(attr.getDescriptionId()));

        // Emit both the value of the modifier, and the entity's base value as debug information, since both are flattened into the modifier.
        // Skip showing debug information here when displaying a merged modifier, since it will be shown if the user holds shift to display the un-merged modifier.
        if (flag.isAdvanced() && !merged && NeoForgeCommonConfig.INSTANCE.attributeAdvancedTooltipDebugInfo.get()) {
            double baseBonus = value - entityBase;
            String baseBonusText = String.format(Locale.ROOT, baseBonus > 0 ? " + %s" : " - %s", FORMAT.format(Math.abs(baseBonus)));
            Component debugInfo = Component.translatable("neoforge.attribute.debug.base", FORMAT.format(entityBase), baseBonusText).withStyle(ChatFormatting.GRAY);
            comp.append(CommonComponents.SPACE).append(debugInfo);
        }

        return comp;
    }

    /**
     * Returns the color used by merged attribute modifiers. Only used when {@link NeoForgeMod#enableMergedAttributeTooltips()} is active.
     * <p>
     * Similarly to {@link Attribute#getStyle(boolean)}, this method should return a color based on the attribute's {@link Sentiment}.
     * The returned color should be distinguishable from the color used by {@link Attribute#getStyle(boolean)}.
     * 
     * @param positive If the attribute modifier value is positive or not.
     */
    TextColor getMergedStyle(boolean isPositive);

    public static boolean isNullOrAddition(@Nullable Operation op) {
        return op == null || op == Operation.ADD_VALUE;
    }

    private Attribute self() {
        return (Attribute) this;
    }
}
