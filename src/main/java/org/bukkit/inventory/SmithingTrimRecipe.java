package org.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a smithing trim recipe.
 */
public class SmithingTrimRecipe extends SmithingRecipe implements ComplexRecipe {

    private final RecipeChoice template;
    private final TrimPattern trimPattern;

    @NotNull
    @Deprecated(since = "1.21.5")
    private static TrimPattern getTrimPattern(@Nullable RecipeChoice template) {
        if (template == null) {
            return TrimPattern.SENTRY;
        }

        return switch (template.getItemStack().getType()) {
            case SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.SENTRY;
            case DUNE_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.DUNE;
            case COAST_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.COAST;
            case WILD_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.WILD;
            case WARD_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.WARD;
            case EYE_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.EYE;
            case VEX_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.VEX;
            case TIDE_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.TIDE;
            case SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.SNOUT;
            case RIB_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.RIB;
            case SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.SPIRE;
            case WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.WAYFINDER;
            case SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.SHAPER;
            case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.SILENCE;
            case RAISER_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.RAISER;
            case HOST_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.HOST;
            case FLOW_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.FLOW;
            case BOLT_ARMOR_TRIM_SMITHING_TEMPLATE ->
                TrimPattern.BOLT;
            default ->
                TrimPattern.SENTRY;
        };
    }

    /**
     * Create a smithing recipe to produce the specified result ItemStack.
     *
     * @param key The unique recipe key
     * @param template The template item.
     * @param base The base ingredient
     * @param addition The addition ingredient
     * @see #SmithingTrimRecipe(NamespacedKey, RecipeChoice, RecipeChoice, RecipeChoice, TrimPattern)
     * @deprecated trimPattern must be specified
     */
    @Deprecated(since = "1.21.5")
    public SmithingTrimRecipe(@NotNull NamespacedKey key, @Nullable RecipeChoice template, @Nullable RecipeChoice base, @Nullable RecipeChoice addition) {
        this(key, template, base, addition, getTrimPattern(template));
    }

    public SmithingTrimRecipe(@NotNull NamespacedKey key, @Nullable RecipeChoice template, @Nullable RecipeChoice base, @Nullable RecipeChoice addition, @NotNull TrimPattern trimPattern) {
        super(key, new ItemStack(Material.AIR), base, addition);
        this.template = template;
        this.trimPattern = trimPattern;
    }

    /**
     * Get the template recipe item.
     *
     * @return template choice
     */
    @Nullable
    public RecipeChoice getTemplate() {
        return (template != null) ? template.clone() : null;
    }

    @NotNull
    public TrimPattern getTrimPattern() {
        return trimPattern;
    }
}
