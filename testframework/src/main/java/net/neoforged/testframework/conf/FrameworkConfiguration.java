/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.conf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.testframework.impl.DefaultMarkdownFileSummaryDumper;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.impl.TestFrameworkImpl;
import net.neoforged.testframework.summary.DefaultLogSummaryDumper;
import net.neoforged.testframework.summary.SummaryDumper;
import org.jetbrains.annotations.Nullable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FrameworkConfiguration(
        ResourceLocation id,
        Collection<Feature> enabledFeatures,
        int commandRequiredPermission,
        List<String> enabledTests,
        @Nullable Supplier<ClientConfiguration> clientConfiguration,
        List<SummaryDumper> dumpers,
        MissingDescriptionAction onMissingDescription) {

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public boolean isEnabled(Feature feature) {
        return enabledFeatures.contains(feature);
    }

    public MutableTestFramework create() {
        return new TestFrameworkImpl(this);
    }
    public static final class Builder {
        private final ResourceLocation id;
        private final Collection<Feature> features = EnumSet.noneOf(Feature.class);

        private int commandRequiredPermission = Commands.LEVEL_GAMEMASTERS;
        private final List<String> enabledTests = new ArrayList<>();
        private MissingDescriptionAction onMissingDescription = MissingDescriptionAction.WARNING;
        private final List<SummaryDumper> dumpers = new ArrayList<>();

        private @Nullable Supplier<ClientConfiguration> clientConfiguration;

        public Builder(ResourceLocation id) {
            this.id = id;

            for (final Feature value : Feature.values()) {
                if (value.isEnabledByDefault()) enable(value);
            }

            dumpers(new DefaultLogSummaryDumper(), new DefaultMarkdownFileSummaryDumper());
        }

        public Builder enable(Feature... features) {
            this.features.addAll(List.of(features));
            return this;
        }

        public Builder disable(Feature... features) {
            this.features.removeAll(List.of(features));
            return this;
        }

        public Builder commandRequiredPermission(int commandRequiredPermission) {
            this.commandRequiredPermission = commandRequiredPermission;
            return this;
        }

        public Builder enableTests(String... tests) {
            this.enabledTests.addAll(List.of(tests));
            return this;
        }

        public Builder clientConfiguration(Supplier<ClientConfiguration> clientConfiguration) {
            this.clientConfiguration = clientConfiguration;
            return this;
        }

        public Builder dumpers(SummaryDumper... dumpers) {
            this.dumpers.addAll(List.of(dumpers));
            return this;
        }

        public Builder setDumpers(SummaryDumper... dumpers) {
            this.dumpers.clear();
            return dumpers(dumpers);
        }

        public Builder onMissingDescription(MissingDescriptionAction onMissingDescription) {
            this.onMissingDescription = onMissingDescription;
            return this;
        }

        public FrameworkConfiguration build() {
            return new FrameworkConfiguration(id, features, commandRequiredPermission, enabledTests, clientConfiguration, dumpers, onMissingDescription);
        }
    }
}
