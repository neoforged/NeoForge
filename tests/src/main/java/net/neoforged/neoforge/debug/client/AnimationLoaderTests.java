/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.entity.animation.json.AnimationLoader;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = "client.animation")
public class AnimationLoaderTests {
    @TestHolder(description = "Tests that data-driven animations are loaded at the correct time", enabledByDefault = true)
    static void animLoaderTest(final DynamicTest test) {
        var entity = test.registrationHelper().entityTypes().registerEntityType("test", TestEntity::new, MobCategory.AMBIENT);
        ResourceLocation itemModelId = ResourceLocation.fromNamespaceAndPath(test.createModId(), "test_item_model");
        ResourceLocation itemModelFile = ResourceLocation.fromNamespaceAndPath(test.createModId(), "test_item");
        test.framework().modEventBus().addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(TestEntityModel.LAYER_LOC, TestEntityModel::createLayer);
        });
        test.framework().modEventBus().addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(entity.get(), context -> new TestEntityRenderer(context, test));
        });
        test.framework().modEventBus().addListener((RegisterItemModelsEvent event) -> {
            event.register(itemModelId, new TestItemModel(test).codec);
        });
    }

    private static final class TestEntity extends Entity {
        private TestEntity(EntityType<? extends Entity> entityType, Level level) {
            super(entityType, level);
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {}

        @Override
        protected void readAdditionalSaveData(ValueInput tag) {}

        @Override
        protected void addAdditionalSaveData(ValueOutput tag) {}

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            return false;
        }
    }

    private static final class TestEntityModel extends EntityModel<EntityRenderState> {
        private static final ModelLayerLocation LAYER_LOC = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("neotests_anim_loader_test", "test"), "main");
        private static final ResourceLocation ANIM_LOC = ResourceLocation.fromNamespaceAndPath("neotests_anim_loader_test", "empty_animation");

        private TestEntityModel(ModelPart modelPart, DynamicTest test) {
            super(modelPart);
            if (AnimationLoader.INSTANCE.getAnimation(ANIM_LOC) != null) {
                test.pass();
            } else {
                test.fail("Test animation not loaded in time");
            }
        }

        private static LayerDefinition createLayer() {
            return LayerDefinition.create(new MeshDefinition(), 0, 0);
        }
    }

    private static final class TestEntityRenderer extends EntityRenderer<TestEntity, EntityRenderState> {
        private TestEntityRenderer(EntityRendererProvider.Context context, DynamicTest test) {
            super(context);
            new TestEntityModel(context.bakeLayer(TestEntityModel.LAYER_LOC), test);
        }

        @Override
        public EntityRenderState createRenderState() {
            return new EntityRenderState();
        }
    }

    private static final class TestItemModel implements ItemModel.Unbaked {
        private final DynamicTest test;
        private final MapCodec<TestItemModel> codec;

        private TestItemModel(DynamicTest test) {
            this.test = test;
            this.codec = MapCodec.unit(this);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            AnimationLoader.PendingAnimations pendingAnimations = context.pendingAnimations();
            if (pendingAnimations.get(TestEntityModel.ANIM_LOC) == null) {
                this.test.fail("Test animation not present in PendingAnimations");
            } else {
                this.test.pass();
            }
            return context.missingItemModel();
        }

        @Override
        public void resolveDependencies(Resolver resolver) {}

        @Override
        public MapCodec<TestItemModel> type() {
            return codec;
        }
    }
}
