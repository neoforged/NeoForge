/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber
@Mod("entity_renderer_events_test")
public class EntityRendererEventsTest {
    private static final ResourceLocation MY_ENTITY = ResourceLocation.fromNamespaceAndPath("entity_renderer_events_test", "test_entity");

    public static final DeferredHolder<EntityType<?>, EntityType<MyEntity>> MY_ENTITY_TYPE = DeferredHolder.create(Registries.ENTITY_TYPE, MY_ENTITY);

    @SubscribeEvent
    public static void entityRegistry(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ENTITY_TYPE)) {
            event.register(Registries.ENTITY_TYPE, MY_ENTITY, () -> EntityType.Builder.of(MyEntity::new, MobCategory.MONSTER).build(ResourceKey.create(Registries.ENTITY_TYPE, MY_ENTITY)));
        }
    }

    @SubscribeEvent
    public static void entityRegistry(EntityAttributeCreationEvent event) {
        event.put(MY_ENTITY_TYPE.get(), Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0D).build());
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = "entity_renderer_events_test")
    private static class EntityRenderEventsTestClientModStuff {
        private static final ModelLayerLocation MAIN_LAYER = new ModelLayerLocation(MY_ENTITY, "main");
        private static final ModelLayerLocation OUTER_LAYER = new ModelLayerLocation(MY_ENTITY, "main");
        private static final ModelLayerLocation ADDED_LAYER = new ModelLayerLocation(MY_ENTITY, "added");

        @SubscribeEvent
        public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(MAIN_LAYER, MyEntityModel::createMainLayer);
            event.registerLayerDefinition(OUTER_LAYER, () -> MyEntityModel.createLayer(new CubeDeformation(1.0f)));
            event.registerLayerDefinition(ADDED_LAYER, () -> MyEntityModel.createLayer(new CubeDeformation(2.0f)));
        }

        @SubscribeEvent
        public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(MY_ENTITY_TYPE.get(), MyEntityRenderer::new);
        }

        @SubscribeEvent
        public static void entityLayers(EntityRenderersEvent.AddLayers event) {
            MyEntityRenderer renderer = event.getRenderer(MY_ENTITY_TYPE.get());
            renderer.addLayer(new MyEntityLayer(renderer, new MyEntityModel(event.getEntityModels().bakeLayer(ADDED_LAYER)), 0.5f));
        }

        private static class MyEntityModel extends EntityModel<LivingEntityRenderState> {
            public static final String BODY = "body";
            public static final String HEAD = "head";

            public static LayerDefinition createMainLayer() {
                return createLayer(CubeDeformation.NONE);
            }

            public static LayerDefinition createLayer(CubeDeformation deformation) {
                MeshDefinition definition = new MeshDefinition();
                PartDefinition root = definition.getRoot();
                root.addOrReplaceChild(BODY, CubeListBuilder.create().addBox(-4, 0, -4, 4, 10, 4, deformation), PartPose.ZERO);
                root.addOrReplaceChild(HEAD, CubeListBuilder.create().addBox(-2, 10, -2, 2, 4, 2, deformation), PartPose.ZERO);
                return LayerDefinition.create(definition, 64, 32);
            }

            public MyEntityModel(ModelPart modelPart) {
                super(modelPart);
            }
        }

        private static class MyEntityRenderer extends LivingEntityRenderer<MyEntity, LivingEntityRenderState, MyEntityModel> {
            private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("entity_renderer_events_test", "textures/entity/test_entity.png");

            public MyEntityRenderer(EntityRendererProvider.Context context) {
                super(context, new MyEntityModel(context.bakeLayer(MAIN_LAYER)), 1.0f);
                addLayer(new MyEntityLayer(this, new MyEntityModel(context.bakeLayer(OUTER_LAYER)), 0.1f));
            }

            @Override
            public ResourceLocation getTextureLocation(LivingEntityRenderState p_114482_) {
                return TEXTURE;
            }

            @Override
            public LivingEntityRenderState createRenderState() {
                return new LivingEntityRenderState();
            }
        }

        private static class MyEntityLayer extends RenderLayer<LivingEntityRenderState, MyEntityModel> {
            private final MyEntityModel model;
            private final int color;

            public MyEntityLayer(MyEntityRenderer renderer, MyEntityModel model, float r) {
                super(renderer);
                this.model = model;
                this.color = ARGB.colorFromFloat(1F, r, 1F, 1F);
            }

            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int lightness, LivingEntityRenderState renderState, float netHeadYaw, float headPitch) {
                nodeCollector.submitModel(model, renderState, poseStack, this.getParentModel().renderType(MyEntityRenderer.TEXTURE), lightness, OverlayTexture.NO_OVERLAY, color, null);
            }
        }
    }

    private static class MyEntity extends LivingEntity {
        protected MyEntity(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
            super(p_20966_, p_20967_);
        }

        @Override
        public ItemStack getItemBySlot(EquipmentSlot p_21127_) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) {}

        @Override
        public HumanoidArm getMainArm() {
            return HumanoidArm.LEFT;
        }
    }
}
