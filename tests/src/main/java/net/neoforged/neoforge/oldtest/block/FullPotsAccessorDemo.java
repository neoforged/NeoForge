/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.block;

import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.QuadCollection;
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
import net.minecraft.world.level.BlockAndTintGetter;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

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
            bus.addListener(FullPotsAccessorDemo::addCreative);
        }
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
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
        public void handleUpdateTag(ValueInput input) {
            super.handleUpdateTag(input);
            modelData = modelData.derive().with(PLANT_PROPERTY, plant).build();
            requestModelDataUpdate();
        }

        @Override
        public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
        }

        @Override
        public void onDataPacket(Connection net, ValueInput input) {
            handleUpdateTag(input);
            //noinspection ConstantConditions
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }

        @Override
        public void loadAdditional(ValueInput input) {
            super.loadAdditional(input);
            input.getString("plant").ifPresent(id -> plant = BuiltInRegistries.BLOCK.getValue(ResourceLocation.parse(id)));
        }

        @Override
        protected void saveAdditional(ValueOutput output) {
            //noinspection ConstantConditions
            output.putString("plant", BuiltInRegistries.BLOCK.getKey(plant).toString());
            super.saveAdditional(output);
        }
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    private static class ClientHandler {
        @SubscribeEvent
        public static void registerBlockStateModelType(final RegisterBlockStateModels event) {
            event.registerModel(ResourceLocation.fromNamespaceAndPath(MOD_ID, "diorite_pot"), DioritePotUnbakedBlockStateModel.CODEC);
        }

        private static class DioritePotUnbakedBlockStateModel implements CustomUnbakedBlockStateModel {
            public static final MapCodec<DioritePotUnbakedBlockStateModel> CODEC = SingleVariant.Unbaked.MAP_CODEC
                    .xmap(DioritePotUnbakedBlockStateModel::new, model -> model.wrapped);

            private final SingleVariant.Unbaked wrapped;

            private DioritePotUnbakedBlockStateModel(SingleVariant.Unbaked wrapped) {
                this.wrapped = wrapped;
            }

            @Override
            public BlockStateModel bake(ModelBaker baker) {
                return new DioritePotModel(this.wrapped.bake(baker));
            }

            @Override
            public void resolveDependencies(Resolver resolver) {
                this.wrapped.resolveDependencies(resolver);
            }

            @Override
            public MapCodec<DioritePotUnbakedBlockStateModel> codec() {
                return CODEC;
            }
        }

        private static class DioritePotModel extends DelegateBlockStateModel {
            private static final ResourceLocation POT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/flower_pot");
            private static final ResourceLocation DIRT_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "block/dirt");
            private static final Direction[] DIRECTIONS = Arrays.copyOfRange(Direction.values(), 0, 7);

            public DioritePotModel(BlockStateModel wrappedModel) {
                super(wrappedModel);
            }

            @Override
            public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
                super.collectParts(level, pos, state, random, parts);

                Block plant = level.getModelData(pos).get(DioriteFlowerPotBlockEntity.PLANT_PROPERTY);
                if (plant != null && plant != Blocks.AIR) {
                    collectPlantParts(plant, level, pos, random, parts);
                }
            }

            private static void collectPlantParts(Block plant, BlockAndTintGetter level, BlockPos pos, RandomSource random, List<BlockModelPart> parts) {
                BlockState potState = ((FlowerPotBlock) Blocks.FLOWER_POT).getFullPotsView().getOrDefault(BuiltInRegistries.BLOCK.getKey(plant), () -> Blocks.AIR).get().defaultBlockState();
                BlockStateModel potModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(potState);

                for (BlockModelPart part : potModel.collectParts(level, pos, potState, random)) {
                    QuadCollection.Builder builder = new QuadCollection.Builder();
                    for (Direction side : DIRECTIONS) {
                        for (BakedQuad quad : part.getQuads(side)) {
                            ResourceLocation texture = quad.sprite().contents().name();
                            if (!texture.equals(POT_TEXTURE) && !texture.equals(DIRT_TEXTURE)) {
                                if (side == null) {
                                    builder.addUnculledFace(quad);
                                } else {
                                    builder.addCulledFace(side, quad);
                                }
                            }
                        }
                    }
                    parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleIcon(), part.getRenderType(potState)));
                }
            }
        }
    }
}
