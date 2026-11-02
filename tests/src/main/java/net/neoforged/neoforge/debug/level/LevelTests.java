/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.level;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.event.RegisterGameRuleCategoryEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = LevelTests.GROUP)
public class LevelTests {
    public static final String GROUP = "level";

    /**
     * Simple test to ensure custom game rules can be registered correctly and used in game.
     * <p>
     * To test these game rules use the following commands.
     * <br>If the game rules are registered correctly, they should show up as auto-completion values and be able to be changed to valid values based on their types.
     * <br>These game rules should also show up and be editable under the {@code Edit Game Rules} screen, when creating a new world.
     * <br>{@code Create new world > More (tab) > Game Rules > Misc}
     * <ul>
     * <li>
     *
     * <pre>{@code /gamerule neotests_custom_game_rule:custom_boolean_game_rule <true|false>}</pre>
     *
     * </li>
     * Should be able to be set to either {@code true} or {@code false} (Defaulting to {@code true}).
     *
     * <li>
     *
     * <pre>{@code /gamerule neotests_custom_game_rule:custom_integer_game_rule <some integer>}</pre>
     *
     * </li>
     * Should be able to be set to any integer value (Defaulting to {@code 1337}).
     * </ul>
     */
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom game rules work")
    static void customGameRule(final DynamicTest test, final RegistrationHelper reg) {
        final GameRuleCategory category = new GameRuleCategory(Identifier.fromNamespaceAndPath(reg.modId(), "game_rules"));
        final DeferredHolder<GameRule<?>, GameRule<Boolean>> booleanGameRule = reg.register(Registries.GAME_RULE, "custom_boolean_game_rule", (r, n) -> GameRules.registerBoolean(n.toString(), category, true));
        final DeferredHolder<GameRule<?>, GameRule<Integer>> integerGameRule = reg.register(Registries.GAME_RULE, "custom_integer_game_rule", (r, n) -> GameRules.registerInteger(n.toString(), category, 1337, 1337));
        // '(visitor, gameRule) -> {}' is intentional here
        // `GameRules#visitGameRuleTypes` calls the default `GameRuleTypeVisitor#visit(GameRule<T>)` before invoking the game rule specific visitor
        // DO NOT call `GameRuleTypeVisitor#visit` from your custom visitor, this will double up 'visits' to your game rule
        final DeferredHolder<GameRule<?>, GameRule<Double>> doubleGameRule = reg.register(Registries.GAME_RULE, "custom_double_game_rule", (r, n) -> GameRules.register(n.toString(), category, GameRuleType.valueOf("NEOTESTS_DOUBLE"), DoubleArgumentType.doubleArg(), Codec.DOUBLE, 0D, FeatureFlagSet.of(), (visitor, gameRule) -> {}, value -> Command.SINGLE_SUCCESS));
        final DeferredHolder<GameRule<?>, GameRule<String>> stringGameRule = reg.register(Registries.GAME_RULE, "custom_string_game_rule", (r, n) -> GameRules.register(n.toString(), category, GameRuleType.valueOf("NEOTESTS_STRING"), StringArgumentType.string(), Codec.STRING, "", FeatureFlagSet.of(), (visitor, gameRule) -> {}, value -> Command.SINGLE_SUCCESS));

        test.eventListeners().forge().addListener((EntityTickEvent.Pre event) -> {
            if (event.getEntity() instanceof ServerPlayer player && player.getGameProfile().name().equals("test-mock-player")) {
                if (player.level().getGameRules().get(booleanGameRule.get())) {
                    player.setHealth(player.getHealth() - player.level().getGameRules().get(integerGameRule.get()));
                }
            }
        });

        test.eventListeners().mod().addListener((RegisterGameRuleCategoryEvent event) -> event.register(category));

        reg.clientProvider(LanguageProvider.class, provider -> {
            provider.add(category, "Custom GameRules");
            provider.addGameRule(booleanGameRule, "Custom Boolean", "A custom boolean game rule");
            provider.addGameRule(integerGameRule, "Custom Integer", "A custom integer game rule");
            provider.addGameRule(doubleGameRule, "Custom Double", "A custom double game rule");
            provider.addGameRule(stringGameRule, "Custom String", "A custom string game rule");
        });

        test.onGameTest(helper -> {
            final ServerPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);

            var gameRules = player.level().getGameRules();
            final var oldBool = gameRules.get(booleanGameRule.get());
            final var oldInt = gameRules.get(integerGameRule.get());
            final var oldDouble = gameRules.get(doubleGameRule.get());
            final var oldString = gameRules.get(stringGameRule.get());

            helper.startSequence()
                    .thenExecute(() -> gameRules.set(booleanGameRule.get(), true, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(integerGameRule.get(), 12, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(doubleGameRule.get(), 64D, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(stringGameRule.get(), "test", player.level().getServer()))

                    .thenIdle(1)
                    .thenExecute(() -> helper.assertEntityProperty(player, ServerPlayer::getHealth, "player health", 8f))

                    .thenExecute(() -> gameRules.set(booleanGameRule.get(), oldBool, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(integerGameRule.get(), oldInt, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(doubleGameRule.get(), oldDouble, player.level().getServer()))
                    .thenExecute(() -> gameRules.set(stringGameRule.get(), oldString, player.level().getServer()))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests the temperature modifier extensible enum")
    static void temperatureModifierExtensibleEnum(DynamicTest test, RegistrationHelper reg) {
        final Biome.TemperatureModifier modifier = Biome.TemperatureModifier.valueOf("NEOTESTS_TEMPERATURE_MODIFIER");

        // Create modifier to set temperature
        record SetTemperatureModifier(HolderSet<Biome> biomes, Biome.TemperatureModifier modifier) implements BiomeModifier {
            public static final MapCodec<SetTemperatureModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(SetTemperatureModifier::biomes),
                    Biome.TemperatureModifier.CODEC.fieldOf("modifier").forGetter(SetTemperatureModifier::modifier)).apply(instance, SetTemperatureModifier::new));

            @Override
            public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
                if (phase == Phase.MODIFY && this.biomes.contains(biome)) {
                    builder.getClimateSettings().setHasPrecipitation(true);
                    builder.getClimateSettings().setTemperatureModifier(this.modifier);
                }
            }

            @Override
            public MapCodec<? extends BiomeModifier> codec() {
                return CODEC;
            }
        }
        reg.register(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, "temperature_modifier", (registry, id) -> Registry.register(registry, id, SetTemperatureModifier.CODEC));
        reg.registriesForTest(gatherer -> gatherer.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, bootstrap -> {
            HolderGetter<Biome> biomeLookup = bootstrap.lookup(Registries.BIOME);

            bootstrap.register(
                    ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Identifier.fromNamespaceAndPath(reg.modId(), "temperature_modifier")),
                    new SetTemperatureModifier(biomeLookup.getOrThrow(Tags.Biomes.IS_OVERWORLD), modifier));
        }));

        // Check temperature at the desired locations
        test.onGameTest(helper -> {
            // Setup rain state
            float oRainLevel = helper.getLevel().getRainLevel(1f);
            helper.getLevel().setRainLevel(1f);

            // Check near the test position
            ChunkPos chunkOfTest = ChunkPos.containing(helper.testInfo.getTestBlockPos());
            if (helper.getLevel().precipitationAt(chunkOfTest.getBlockAt(0, helper.testInfo.getTestBlockPos().getY(), helper.testInfo.getTestBlockPos().getZ())) != Biome.Precipitation.RAIN) {
                helper.fail("Expected to see rain at X = 0 within the chunk, instead saw snow");
            }
            if (helper.getLevel().precipitationAt(chunkOfTest.getBlockAt(8, helper.testInfo.getTestBlockPos().getY(), helper.testInfo.getTestBlockPos().getZ())) != Biome.Precipitation.SNOW) {
                helper.fail("Expected to see snow at X = 8 within the chunk, instead saw rain");
            }

            // Restore rain state
            helper.getLevel().setRainLevel(oRainLevel);

            helper.succeed();
        });
    }

    public static Object getTemperatureModifierParameter(int idx, Class<?> type) {
        return type.cast(switch (idx) {
            case 0 -> "neotests:temperature_modifier";
            // For each chunk, if X offset is less than 8, then set temperature to 2, else -2
            case 1 -> (Biome.TemperatureModifier.TemperatureOverride) (pos, baseTemperature) -> (pos.getX() & 0xF) < 8 ? 2f : -2f;
            default -> throw new IllegalArgumentException("Unexpected parameter index: " + idx);
        });
    }
}
