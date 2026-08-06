package org.bukkit.inventory;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A ItemFlag can hide some Attributes from ItemStacks
 */
public enum ItemFlag {

    /**
     * Setting to show/hide enchants
     */
    HIDE_ENCHANTS,
    /**
     * Setting to show/hide Attributes like Damage
     */
    HIDE_ATTRIBUTES,
    /**
     * Setting to show/hide the unbreakable State
     */
    HIDE_UNBREAKABLE,
    /**
     * Setting to show/hide what the ItemStack can break/destroy
     */
    HIDE_DESTROYS,
    /**
     * Setting to show/hide where this ItemStack can be build/placed on
     */
    HIDE_PLACED_ON,
    /**
     * Setting to show/hide potion effects, book and firework information, map
     * tooltips, patterns of banners, and enchantments of enchanted books.
     */
    HIDE_ADDITIONAL_TOOLTIP,
    /**
     * Setting to show/hide dyes from colored leather armor.
     */
    HIDE_DYE,
    /**
     * Setting to show/hide armor trim from leather armor.
     */
    HIDE_ARMOR_TRIM,
    // Component flags
    @ApiStatus.Experimental
    HIDE_CUSTOM_DATA("custom_data"),
    @ApiStatus.Experimental
    HIDE_MAX_STACK_SIZE("max_stack_size"),
    @ApiStatus.Experimental
    HIDE_MAX_DAMAGE("max_damage"),
    @ApiStatus.Experimental
    HIDE_DAMAGE("damage"),
    // @ApiStatus.Experimental
    // HIDE_UNBREAKABLE("unbreakable"),
    @ApiStatus.Experimental
    HIDE_CUSTOM_NAME("custom_name"),
    @ApiStatus.Experimental
    HIDE_ITEM_NAME("item_name"),
    @ApiStatus.Experimental
    HIDE_ITEM_MODEL("item_model"),
    @ApiStatus.Experimental
    HIDE_LORE("lore"),
    @ApiStatus.Experimental
    HIDE_RARITY("rarity"),
    @ApiStatus.Experimental
    HIDE_ENCHANTMENTS("enchantments"),
    @ApiStatus.Experimental
    HIDE_CAN_PLACE_ON("can_place_on"),
    @ApiStatus.Experimental
    HIDE_CAN_BREAK("can_break"),
    @ApiStatus.Experimental
    HIDE_ATTRIBUTE_MODIFIERS("attribute_modifiers"),
    @ApiStatus.Experimental
    HIDE_CUSTOM_MODEL_DATA("custom_model_data"),
    @ApiStatus.Experimental
    HIDE_TOOLTIP_DISPLAY("tooltip_display"),
    @ApiStatus.Experimental
    HIDE_REPAIR_COST("repair_cost"),
    @ApiStatus.Experimental
    HIDE_CREATIVE_SLOT_LOCK("creative_slot_lock"),
    @ApiStatus.Experimental
    HIDE_ENCHANTMENT_GLINT_OVERRIDE("enchantment_glint_override"),
    @ApiStatus.Experimental
    HIDE_INTANGIBLE_PROJECTILE("intangible_projectile"),
    @ApiStatus.Experimental
    HIDE_FOOD("food"),
    @ApiStatus.Experimental
    HIDE_CONSUMABLE("consumable"),
    @ApiStatus.Experimental
    HIDE_USE_REMAINDER("use_remainder"),
    @ApiStatus.Experimental
    HIDE_USE_COOLDOWN("use_cooldown"),
    @ApiStatus.Experimental
    HIDE_DAMAGE_RESISTANT("damage_resistant"),
    @ApiStatus.Experimental
    HIDE_TOOL("tool"),
    @ApiStatus.Experimental
    HIDE_WEAPON("weapon"),
    @ApiStatus.Experimental
    HIDE_ENCHANTABLE("enchantable"),
    @ApiStatus.Experimental
    HIDE_EQUIPPABLE("equippable"),
    @ApiStatus.Experimental
    HIDE_REPAIRABLE("repairable"),
    @ApiStatus.Experimental
    HIDE_GLIDER("glider"),
    @ApiStatus.Experimental
    HIDE_TOOLTIP_STYLE("tooltip_style"),
    @ApiStatus.Experimental
    HIDE_DEATH_PROTECTION("death_protection"),
    @ApiStatus.Experimental
    HIDE_BLOCKS_ATTACKS("blocks_attacks"),
    @ApiStatus.Experimental
    HIDE_STORED_ENCHANTMENTS("stored_enchantments"),
    @ApiStatus.Experimental
    HIDE_DYED_COLOR("dyed_color"),
    @ApiStatus.Experimental
    HIDE_MAP_COLOR("map_color"),
    @ApiStatus.Experimental
    HIDE_MAP_ID("map_id"),
    @ApiStatus.Experimental
    HIDE_MAP_DECORATIONS("map_decorations"),
    @ApiStatus.Experimental
    HIDE_MAP_POST_PROCESSING("map_post_processing"),
    @ApiStatus.Experimental
    HIDE_CHARGED_PROJECTILES("charged_projectiles"),
    @ApiStatus.Experimental
    HIDE_BUNDLE_CONTENTS("bundle_contents"),
    @ApiStatus.Experimental
    HIDE_POTION_CONTENTS("potion_contents"),
    @ApiStatus.Experimental
    HIDE_POTION_DURATION_SCALE("potion_duration_scale"),
    @ApiStatus.Experimental
    HIDE_SUSPICIOUS_STEW_EFFECTS("suspicious_stew_effects"),
    @ApiStatus.Experimental
    HIDE_WRITABLE_BOOK_CONTENT("writable_book_content"),
    @ApiStatus.Experimental
    HIDE_WRITTEN_BOOK_CONTENT("written_book_content"),
    @ApiStatus.Experimental
    HIDE_TRIM("trim"),
    @ApiStatus.Experimental
    HIDE_DEBUG_STICK_STATE("debug_stick_state"),
    @ApiStatus.Experimental
    HIDE_ENTITY_DATA("entity_data"),
    @ApiStatus.Experimental
    HIDE_BUCKET_ENTITY_DATA("bucket_entity_data"),
    @ApiStatus.Experimental
    HIDE_BLOCK_ENTITY_DATA("block_entity_data"),
    @ApiStatus.Experimental
    HIDE_INSTRUMENT("instrument"),
    @ApiStatus.Experimental
    HIDE_PROVIDES_TRIM_MATERIAL("provides_trim_material"),
    @ApiStatus.Experimental
    HIDE_OMINOUS_BOTTLE_AMPLIFIER("ominous_bottle_amplifier"),
    @ApiStatus.Experimental
    HIDE_JUKEBOX_PLAYABLE("jukebox_playable"),
    @ApiStatus.Experimental
    HIDE_PROVIDES_BANNER_PATTERNS("provides_banner_patterns"),
    @ApiStatus.Experimental
    HIDE_RECIPES("recipes"),
    @ApiStatus.Experimental
    HIDE_LODESTONE_TRACKER("lodestone_tracker"),
    @ApiStatus.Experimental
    HIDE_FIREWORK_EXPLOSION("firework_explosion"),
    @ApiStatus.Experimental
    HIDE_FIREWORKS("fireworks"),
    @ApiStatus.Experimental
    HIDE_PROFILE("profile"),
    @ApiStatus.Experimental
    HIDE_NOTE_BLOCK_SOUND("note_block_sound"),
    @ApiStatus.Experimental
    HIDE_BANNER_PATTERNS("banner_patterns"),
    @ApiStatus.Experimental
    HIDE_BASE_COLOR("base_color"),
    @ApiStatus.Experimental
    HIDE_POT_DECORATIONS("pot_decorations"),
    @ApiStatus.Experimental
    HIDE_CONTAINER("container"),
    @ApiStatus.Experimental
    HIDE_BLOCK_STATE("block_state"),
    @ApiStatus.Experimental
    HIDE_BEES("bees"),
    @ApiStatus.Experimental
    HIDE_LOCK("lock"),
    @ApiStatus.Experimental
    HIDE_CONTAINER_LOOT("container_loot"),
    @ApiStatus.Experimental
    HIDE_BREAK_SOUND("break_sound"),
    @ApiStatus.Experimental
    HIDE_VILLAGER_VARIANT("villager/variant"),
    @ApiStatus.Experimental
    HIDE_WOLF_VARIANT("wolf/variant"),
    @ApiStatus.Experimental
    HIDE_WOLF_SOUND_VARIANT("wolf/sound_variant"),
    @ApiStatus.Experimental
    HIDE_WOLF_COLLAR("wolf/collar"),
    @ApiStatus.Experimental
    HIDE_FOX_VARIANT("fox/variant"),
    @ApiStatus.Experimental
    HIDE_SALMON_SIZE("salmon/size"),
    @ApiStatus.Experimental
    HIDE_PARROT_VARIANT("parrot/variant"),
    @ApiStatus.Experimental
    HIDE_TROPICAL_FISH_PATTERN("tropical_fish/pattern"),
    @ApiStatus.Experimental
    HIDE_TROPICAL_FISH_BASE_COLOR("tropical_fish/base_color"),
    @ApiStatus.Experimental
    HIDE_TROPICAL_FISH_PATTERN_COLOR("tropical_fish/pattern_color"),
    @ApiStatus.Experimental
    HIDE_MOOSHROOM_VARIANT("mooshroom/variant"),
    @ApiStatus.Experimental
    HIDE_RABBIT_VARIANT("rabbit/variant"),
    @ApiStatus.Experimental
    HIDE_PIG_VARIANT("pig/variant"),
    @ApiStatus.Experimental
    HIDE_COW_VARIANT("cow/variant"),
    @ApiStatus.Experimental
    HIDE_CHICKEN_VARIANT("chicken/variant"),
    @ApiStatus.Experimental
    HIDE_FROG_VARIANT("frog/variant"),
    @ApiStatus.Experimental
    HIDE_HORSE_VARIANT("horse/variant"),
    @ApiStatus.Experimental
    HIDE_PAINTING_VARIANT("painting/variant"),
    @ApiStatus.Experimental
    HIDE_LLAMA_VARIANT("llama/variant"),
    @ApiStatus.Experimental
    HIDE_AXOLOTL_VARIANT("axolotl/variant"),
    @ApiStatus.Experimental
    HIDE_CAT_VARIANT("cat/variant"),
    @ApiStatus.Experimental
    HIDE_CAT_COLLAR("cat/collar"),
    @ApiStatus.Experimental
    HIDE_SHEEP_COLOR("sheep/color"),
    @ApiStatus.Experimental
    HIDE_SHULKER_COLOR("shulker/color");
    //
    private final NamespacedKey component;

    private ItemFlag() {
        this.component = null;
    }

    private ItemFlag(String component) {
        this.component = NamespacedKey.minecraft(component);
    }

    @ApiStatus.Internal
    @Nullable
    public NamespacedKey getComponent() {
        return this.component;
    }
}
