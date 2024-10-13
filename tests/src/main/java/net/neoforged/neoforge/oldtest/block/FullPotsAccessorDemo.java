/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.common.util.ConcatenatedListView;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@Mod(FullPotsAccessorDemo.MOD_ID)
public class FullPotsAccessorDemo {
    public static final String MOD_ID = "full_pots_accessor_demo";
    private static final boolean ENABLED = true;

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    private static final DeferredBlock<Block> DIORITE_POT = BLOCKS.registerBlock("diorite_pot", DioriteFlowerPotBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.NONE).instabreak().noOcclusion());
    private static final DeferredItem<BlockItem> DIORITE_POT_ITEM = ITEMS.registerSimpleBlockItem(DIORITE_POT);
    private static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DioriteFlowerPotBlockEntity>> DIORITE_POT_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "diorite_pot",
            () -> new BlockEntityType<>(DioriteFlowerPotBlockEntity::new, DIORITE_POT.get()));

    public FullPotsAccessorDemo(IEventBus bus) {
        if (ENABLED) {
            BLOCKS.register(bus);
            ITEMS.register(bus);
            BLOCK_ENTITIES.register(bus);
            bus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(DIORITE_POT_ITEM);
    }

    private static class DioriteFlowerPotBlock extends Block implements EntityBlock {
        private static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

        public DioriteFlowerPotBlock(Properties props) {
            super(props);
        }

        @Override
        public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            if (level.getBlockEntity(pos) instanceof DioriteFlowerPotBlockEntity be) {
                boolean isFlower = stack.getItem() instanceof BlockItem item && ((FlowerPotBlock) Blocks.FLOWER_POT).getFullPotsView().containsKey(BuiltInRegistries.ITEM.getKey(item));
                boolean hasFlower = be.plant != Blocks.AIR;

                if (isFlower != hasFlower) {
                    if (!level.isClientSide()) {
                        if (isFlower) {
                            be.setPlant(((BlockItem) stack.getItem()).getBlock());

                            player.awardStat(Stats.POT_FLOWER);
                            if (!player.getAbilities().instabuild) {
                                stack.shrink(1);
                            }
                        } else {
                            ItemStack flowerStack = new ItemStack(be.getPlant());
                            if (stack.isEmpty()) {
                                player.setItemInHand(hand, flowerStack);
                            } else if (!player.addItem(flowerStack)) {
                                player.drop(flowerStack, false);
                            }

                            be.setPlant(Blocks.AIR);
                        }
                    }

                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.CONSUME;
                }
            }
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }

        @Override
        @SuppressWarnings("deprecation")
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE;
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new DioriteFlowerPotBlockEntity(pos, state);
        }
    }

    private static class DioriteFlowerPotBlockEntity extends BlockEntity {
        public static final ModelProperty<Block> PLANT_PROPERTY = new ModelProperty<>();

        private ModelData modelData;
        private Block plant = Blocks.AIR;

        public DioriteFlowerPotBlockEntity(BlockPos pos, BlockState state) {
            super(DIORITE_POT_BLOCK_ENTITY.get(), pos, state);
            modelData = ModelData.of(PLANT_PROPERTY, plant);
        }

        public void setPlant(Block plant) {
            this.plant = plant;
            //noinspection ConstantConditions
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
            setChanged();
        }

        public Block getPlant() {
            return plant;
        }

        @Override
        public ModelData getModelData() {
            return modelData;
        }

        @Override
        public CompoundTag getUpdateTag(HolderLookup.Provider holderLookup) {
            return saveWithFullMetadata(holderLookup);
        }

        @Override
        public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider holderLookup) {
            super.handleUpdateTag(tag, holderLookup);
            modelData = modelData.derive().with(PLANT_PROPERTY, plant).build();
            requestModelDataUpdate();
        }

        @Override
        public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
        }

        @Override
        public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider holderLookup) {
            handleUpdateTag(pkt.getTag(), holderLookup);
            //noinspection ConstantConditions
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }

        @Override
        public void loadAdditional(CompoundTag tag, HolderLookup.Provider holderLookup) {
            super.loadAdditional(tag, holderLookup);
            plant = BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(tag.getString("plant")));
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider holderLookup) {
            //noinspection ConstantConditions
            tag.putString("plant", BuiltInRegistries.BLOCK.getKey(plant).toString());
            super.saveAdditional(tag, holderLookup);
        }
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    private static class ClientHandler {
        @SubscribeEvent
        public static void registerLoader(final ModelEvent.RegisterGeometryLoaders event) {
            event.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "diorite_pot"), new DioritePotGeometryLoader());
        }

        private static class DioritePotGeometryLoader implements IGeometryLoader<DioritePotModelGeometry> {
            @Override
            public DioritePotModelGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) {
                JsonObject wrappedModel = jsonObject.getAsJsonObject("model");
                return new DioritePotModelGeometry(deserializationContext.deserialize(wrappedModel, BlockModel.class));
            }
        }

        private record DioritePotModelGeometry(UnbakedModel wrappedModel) implements IUnbakedGeometry<DioritePotModelGeometry> {
            @Override
            public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, List<ItemOverride> overrides) {
                return new DioritePotModel(wrappedModel.bake(baker, spriteGetter, modelState));
            }

            @Override
            public void resolveDependencies(UnbakedModel.Resolver modelGetter, IGeometryBakingContext context) {
                wrappedModel.resolveDependencies(modelGetter);
            }
        }

        private static class DioritePotModel extends BakedModelWrapper<BakedModel> {
            private static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());
            private static final ResourceLocation POT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/flower_pot");
            private static final ResourceLocation DIRT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/dirt");

            public DioritePotModel(BakedModel wrappedModel) {
                super(wrappedModel);
            }

            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
                List<List<BakedQuad>> quads = new ArrayList<>();
                quads.add(originalModel.getQuads(state, side, rand, extraData, renderType));

                Block plant = extraData.get(DioriteFlowerPotBlockEntity.PLANT_PROPERTY);
                if (plant != null && plant != Blocks.AIR) {
                    quads.add(getPlantQuads(plant, side, rand, renderType));
                }

                return ConcatenatedListView.of(quads);
            }

            private List<BakedQuad> getPlantQuads(Block plant, @Nullable Direction face, RandomSource rand, @Nullable RenderType renderType) {
                BlockState potState = ((FlowerPotBlock) Blocks.FLOWER_POT).getFullPotsView().getOrDefault(BuiltInRegistries.BLOCK.getKey(plant), () -> Blocks.AIR).get().defaultBlockState();
                BakedModel potModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(potState);

                return potModel.getQuads(potState, face, rand, ModelData.EMPTY, renderType)
                        .stream()
                        .filter(q -> !q.getSprite().contents().name().equals(POT_TEXTURE))
                        .filter(q -> !q.getSprite().contents().name().equals(DIRT_TEXTURE))
                        .collect(Collectors.toList());
            }

            @Override
            public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
                return CUTOUT;
            }
        }
    }
}
