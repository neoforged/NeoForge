package net.neoforged.neoforge.client.fluids;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NeoLiquidBlockRenderer implements FluidRenderer {
    private final TextureAtlasSprite still;
    private final TextureAtlasSprite flowing;
    @Nullable
    private final TextureAtlasSprite overlay;

    protected NeoLiquidBlockRenderer(
        TextureAtlasSprite still,
        TextureAtlasSprite flowing,
        @Nullable TextureAtlasSprite overlay) {
        this.still = still;
        this.flowing = flowing;
        this.overlay = overlay;
    }

    private static boolean shouldRenderOverlay(FluidState selfState, FluidState otherState, Direction neighborFace) {
        return !otherState.shouldHideAdjacentFluidFace(neighborFace, selfState);
    }

    private static boolean isFaceOccludedByState(Direction p_110980_, float p_110981_, BlockState p_110983_) {
        VoxelShape voxelshape = p_110983_.getFaceOcclusionShape(p_110980_.getOpposite());
        if (voxelshape == Shapes.empty()) {
            return false;
        } else if (voxelshape == Shapes.block()) {
            boolean flag = p_110981_ == 1.0F;
            return p_110980_ != Direction.UP || flag;
        } else {
            VoxelShape voxelshape1 = Shapes.box(0.0, 0.0, 0.0, 1.0, p_110981_, 1.0);
            return Shapes.blockOccludes(voxelshape1, voxelshape, p_110980_);
        }
    }

    private static boolean isFaceVisibleFromNeighbor(Direction p_203182_, float p_203183_, BlockState p_203184_) {
        return !isFaceOccludedByState(p_203182_, p_203183_, p_203184_);
    }

    private static boolean isFaceOccludedBySelf(BlockState p_110962_, Direction p_110963_) {
        return isFaceOccludedByState(p_110963_.getOpposite(), 1.0F, p_110962_);
    }

    public static boolean shouldRenderFace(FluidState p_203169_, BlockState p_203170_, Direction p_203171_, FluidState p_203172_) {
        return !isFaceOccludedBySelf(p_203170_, p_203171_) && shouldRenderOverlay(p_203169_, p_203172_, p_203171_.getOpposite());
    }

    @Override
    public void tesselate(BlockAndTintGetter level, BlockPos pos, VertexConsumer consumer, BlockState blockState, FluidState fluidState) {
        int tint = fluidState.getTintColor(level, pos);
        float alpha = ARGB.alphaFloat(tint);
        float red = ARGB.redFloat(tint);
        float green = ARGB.greenFloat(tint);
        float blue = ARGB.blueFloat(tint);

        BlockState blockstate = level.getBlockState(pos.relative(Direction.DOWN));
        FluidState fluidstate = blockstate.getFluidState();
        BlockState blockstate1 = level.getBlockState(pos.relative(Direction.UP));
        FluidState fluidstate1 = blockstate1.getFluidState();
        BlockState blockstate2 = level.getBlockState(pos.relative(Direction.NORTH));
        FluidState fluidstate2 = blockstate2.getFluidState();
        BlockState blockstate3 = level.getBlockState(pos.relative(Direction.SOUTH));
        FluidState fluidstate3 = blockstate3.getFluidState();
        BlockState blockstate4 = level.getBlockState(pos.relative(Direction.WEST));
        FluidState fluidstate4 = blockstate4.getFluidState();
        BlockState blockstate5 = level.getBlockState(pos.relative(Direction.EAST));
        FluidState fluidstate5 = blockstate5.getFluidState();
        boolean renderOverlay = shouldRenderOverlay(fluidState, fluidstate1, Direction.DOWN);
        boolean renderDown = shouldRenderFace(fluidState, blockState, Direction.DOWN, fluidstate) &&
            isFaceVisibleFromNeighbor(Direction.DOWN, 0.8888889F, blockstate);
        boolean renderNorth = shouldRenderFace(fluidState, blockState, Direction.NORTH, fluidstate2);
        boolean renderSouth = shouldRenderFace(fluidState, blockState, Direction.SOUTH, fluidstate3);
        boolean renderWest = shouldRenderFace(fluidState, blockState, Direction.WEST, fluidstate4);
        boolean renderEast = shouldRenderFace(fluidState, blockState, Direction.EAST, fluidstate5);
        if (renderOverlay || renderDown || renderEast || renderWest || renderNorth || renderSouth) {
            float f3 = level.getShade(Direction.DOWN, true);
            float f4 = level.getShade(Direction.UP, true);
            float f5 = level.getShade(Direction.NORTH, true);
            float f6 = level.getShade(Direction.WEST, true);
            Fluid fluid = fluidState.getType();
            float f11 = this.getHeight(level, fluid, pos, blockState, fluidState);
            float f7;
            float f8;
            float f9;
            float f10;
            if (f11 >= 1.0F) {
                f7 = 1.0F;
                f8 = 1.0F;
                f9 = 1.0F;
                f10 = 1.0F;
            } else {
                float f12 = this.getHeight(level, fluid, pos.north(), blockstate2, fluidstate2);
                float f13 = this.getHeight(level, fluid, pos.south(), blockstate3, fluidstate3);
                float f14 = this.getHeight(level, fluid, pos.east(), blockstate5, fluidstate5);
                float f15 = this.getHeight(level, fluid, pos.west(), blockstate4, fluidstate4);
                f7 = this.calculateAverageHeight(level, fluid, f11, f12, f14, pos.relative(Direction.NORTH).relative(Direction.EAST));
                f8 = this.calculateAverageHeight(level, fluid, f11, f12, f15, pos.relative(Direction.NORTH).relative(Direction.WEST));
                f9 = this.calculateAverageHeight(level, fluid, f11, f13, f14, pos.relative(Direction.SOUTH).relative(Direction.EAST));
                f10 = this.calculateAverageHeight(level, fluid, f11, f13, f15, pos.relative(Direction.SOUTH).relative(Direction.WEST));
            }

            float f36 = pos.getX() & 15;
            float f37 = pos.getY() & 15;
            float f38 = pos.getZ() & 15;
            float f16 = renderDown ? 0.001F : 0.0F;
            if (renderOverlay && isFaceVisibleFromNeighbor(Direction.UP, Math.min(Math.min(f8, f10), Math.min(f9, f7)), blockstate1)) {
                f8 -= 0.001F;
                f10 -= 0.001F;
                f9 -= 0.001F;
                f7 -= 0.001F;
                Vec3 vec3 = fluidState.getFlow(level, pos);
                float f17;
                float f18;
                float f19;
                float f20;
                float f21;
                float f22;
                float f23;
                float f24;
                if (vec3.x == 0.0 && vec3.z == 0.0) {
                    TextureAtlasSprite textureatlassprite1 = still;
                    f17 = textureatlassprite1.getU(0.0F);
                    f21 = textureatlassprite1.getV(0.0F);
                    f18 = f17;
                    f22 = textureatlassprite1.getV(1.0F);
                    f19 = textureatlassprite1.getU(1.0F);
                    f23 = f22;
                    f20 = f19;
                    f24 = f21;
                } else {
                    TextureAtlasSprite textureatlassprite = flowing;
                    float f25 = (float)Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
                    float f26 = Mth.sin(f25) * 0.25F;
                    float f27 = Mth.cos(f25) * 0.25F;
                    f17 = textureatlassprite.getU(0.5F + (-f27 - f26));
                    f21 = textureatlassprite.getV(0.5F + (-f27 + f26));
                    f18 = textureatlassprite.getU(0.5F + (-f27 + f26));
                    f22 = textureatlassprite.getV(0.5F + (f27 + f26));
                    f19 = textureatlassprite.getU(0.5F + (f27 + f26));
                    f23 = textureatlassprite.getV(0.5F + (f27 - f26));
                    f20 = textureatlassprite.getU(0.5F + (f27 - f26));
                    f24 = textureatlassprite.getV(0.5F + (-f27 - f26));
                }

                float f53 = (f17 + f18 + f19 + f20) / 4.0F;
                float f54 = (f21 + f22 + f23 + f24) / 4.0F;
                float f55 = still.uvShrinkRatio();
                f17 = Mth.lerp(f55, f17, f53);
                f18 = Mth.lerp(f55, f18, f53);
                f19 = Mth.lerp(f55, f19, f53);
                f20 = Mth.lerp(f55, f20, f53);
                f21 = Mth.lerp(f55, f21, f54);
                f22 = Mth.lerp(f55, f22, f54);
                f23 = Mth.lerp(f55, f23, f54);
                f24 = Mth.lerp(f55, f24, f54);
                int l = this.getLightColor(level, pos);
                float f57 = f4 * red;
                float f29 = f4 * green;
                float f30 = f4 * blue;
                this.vertex(consumer, f36 + 0.0F, f37 + f8, f38 + 0.0F, f57, f29, f30, alpha, f17, f21, l);
                this.vertex(consumer, f36 + 0.0F, f37 + f10, f38 + 1.0F, f57, f29, f30, alpha, f18, f22, l);
                this.vertex(consumer, f36 + 1.0F, f37 + f9, f38 + 1.0F, f57, f29, f30, alpha, f19, f23, l);
                this.vertex(consumer, f36 + 1.0F, f37 + f7, f38 + 0.0F, f57, f29, f30, alpha, f20, f24, l);
                if (fluidState.shouldRenderBackwardUpFace(level, pos.above())) {
                    this.vertex(consumer, f36 + 0.0F, f37 + f8, f38 + 0.0F, f57, f29, f30, alpha, f17, f21, l);
                    this.vertex(consumer, f36 + 1.0F, f37 + f7, f38 + 0.0F, f57, f29, f30, alpha, f20, f24, l);
                    this.vertex(consumer, f36 + 1.0F, f37 + f9, f38 + 1.0F, f57, f29, f30, alpha, f19, f23, l);
                    this.vertex(consumer, f36 + 0.0F, f37 + f10, f38 + 1.0F, f57, f29, f30, alpha, f18, f22, l);
                }
            }

            if (renderDown) {
                float f40 = still.getU0();
                float f41 = still.getU1();
                float f42 = still.getV0();
                float f43 = still.getV1();
                int k = this.getLightColor(level, pos.below());
                float f46 = f3 * red;
                float f48 = f3 * green;
                float f50 = f3 * blue;
                this.vertex(consumer, f36, f37 + f16, f38 + 1.0F, f46, f48, f50, alpha, f40, f43, k);
                this.vertex(consumer, f36, f37 + f16, f38, f46, f48, f50, alpha, f40, f42, k);
                this.vertex(consumer, f36 + 1.0F, f37 + f16, f38, f46, f48, f50, alpha, f41, f42, k);
                this.vertex(consumer, f36 + 1.0F, f37 + f16, f38 + 1.0F, f46, f48, f50, alpha, f41, f43, k);
            }

            int j = this.getLightColor(level, pos);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                float f44;
                float f45;
                float f47;
                float f49;
                float f51;
                float f52;
                boolean flag7;
                switch (direction) {
                    case NORTH:
                        f44 = f8;
                        f45 = f7;
                        f47 = f36;
                        f51 = f36 + 1.0F;
                        f49 = f38 + 0.001F;
                        f52 = f38 + 0.001F;
                        flag7 = renderNorth;
                        break;
                    case SOUTH:
                        f44 = f9;
                        f45 = f10;
                        f47 = f36 + 1.0F;
                        f51 = f36;
                        f49 = f38 + 1.0F - 0.001F;
                        f52 = f38 + 1.0F - 0.001F;
                        flag7 = renderSouth;
                        break;
                    case WEST:
                        f44 = f10;
                        f45 = f8;
                        f47 = f36 + 0.001F;
                        f51 = f36 + 0.001F;
                        f49 = f38 + 1.0F;
                        f52 = f38;
                        flag7 = renderWest;
                        break;
                    default:
                        f44 = f7;
                        f45 = f9;
                        f47 = f36 + 1.0F - 0.001F;
                        f51 = f36 + 1.0F - 0.001F;
                        f49 = f38;
                        f52 = f38 + 1.0F;
                        flag7 = renderEast;
                }

                if (flag7 &&
                    isFaceVisibleFromNeighbor(
                        direction,
                        Math.max(f44, f45),
                        level.getBlockState(pos.relative(direction)))) {
                    BlockPos blockpos = pos.relative(direction);
                    TextureAtlasSprite textureatlassprite2 = flowing;
                    if (overlay != null) {
                        if (level.getBlockState(blockpos).shouldDisplayFluidOverlay(level, blockpos, fluidState)) {
                            textureatlassprite2 = overlay;
                        }
                    }

                    float f56 = textureatlassprite2.getU(0.0F);
                    float f58 = textureatlassprite2.getU(0.5F);
                    float f59 = textureatlassprite2.getV((1.0F - f44) * 0.5F);
                    float f60 = textureatlassprite2.getV((1.0F - f45) * 0.5F);
                    float f31 = textureatlassprite2.getV(0.5F);
                    float f32 = direction.getAxis() == Direction.Axis.Z ? f5 : f6;
                    float f33 = f4 * f32 * red;
                    float f34 = f4 * f32 * green;
                    float f35 = f4 * f32 * blue;
                    this.vertex(consumer, f47, f37 + f44, f49, f33, f34, f35, alpha, f56, f59, j);
                    this.vertex(consumer, f51, f37 + f45, f52, f33, f34, f35, alpha, f58, f60, j);
                    this.vertex(consumer, f51, f37 + f16, f52, f33, f34, f35, alpha, f58, f31, j);
                    this.vertex(consumer, f47, f37 + f16, f49, f33, f34, f35, alpha, f56, f31, j);
                    if (textureatlassprite2 != overlay) {
                        this.vertex(consumer, f47, f37 + f16, f49, f33, f34, f35, alpha, f56, f31, j);
                        this.vertex(consumer, f51, f37 + f16, f52, f33, f34, f35, alpha, f58, f31, j);
                        this.vertex(consumer, f51, f37 + f45, f52, f33, f34, f35, alpha, f58, f60, j);
                        this.vertex(consumer, f47, f37 + f44, f49, f33, f34, f35, alpha, f56, f59, j);
                    }
                }
            }
        }
    }

    private float calculateAverageHeight(BlockAndTintGetter p_203150_, Fluid p_203151_, float p_203152_, float p_203153_, float p_203154_, BlockPos p_203155_) {
        if (!(p_203154_ >= 1.0F) && !(p_203153_ >= 1.0F)) {
            float[] afloat = new float[2];
            if (p_203154_ > 0.0F || p_203153_ > 0.0F) {
                float f = this.getHeight(p_203150_, p_203151_, p_203155_);
                if (f >= 1.0F) {
                    return 1.0F;
                }

                this.addWeightedHeight(afloat, f);
            }

            this.addWeightedHeight(afloat, p_203152_);
            this.addWeightedHeight(afloat, p_203154_);
            this.addWeightedHeight(afloat, p_203153_);
            return afloat[0] / afloat[1];
        } else {
            return 1.0F;
        }
    }

    private void addWeightedHeight(float[] p_203189_, float p_203190_) {
        if (p_203190_ >= 0.8F) {
            p_203189_[0] += p_203190_ * 10.0F;
            p_203189_[1] += 10.0F;
        } else if (p_203190_ >= 0.0F) {
            p_203189_[0] += p_203190_;
            p_203189_[1]++;
        }
    }

    private float getHeight(BlockAndTintGetter p_203157_, Fluid p_203158_, BlockPos p_203159_) {
        BlockState blockstate = p_203157_.getBlockState(p_203159_);
        return this.getHeight(p_203157_, p_203158_, p_203159_, blockstate, blockstate.getFluidState());
    }

    private void vertex(
        VertexConsumer p_110985_,
        float p_110989_,
        float p_110990_,
        float p_110991_,
        float p_110992_,
        float p_110993_,
        float p_350595_,
        float alpha,
        float p_350459_,
        float p_350437_,
        int p_110994_
    ) {
        p_110985_.addVertex(p_110989_, p_110990_, p_110991_)
            .setColor(p_110992_, p_110993_, p_350595_, alpha)
            .setUv(p_350459_, p_350437_)
            .setLight(p_110994_)
            .setNormal(0.0F, 1.0F, 0.0F);
    }

    private float getHeight(BlockAndTintGetter p_203161_, Fluid p_203162_, BlockPos p_203163_, BlockState p_203164_, FluidState p_203165_) {
        if (p_203162_.isSame(p_203165_.getType())) {
            BlockState blockstate = p_203161_.getBlockState(p_203163_.above());
            return p_203162_.isSame(blockstate.getFluidState().getType()) ? 1.0F : p_203165_.getOwnHeight();
        } else {
            return !p_203164_.isSolid() ? 0.0F : -1.0F;
        }
    }

    private int getLightColor(BlockAndTintGetter p_110946_, BlockPos p_110947_) {
        int i = LevelRenderer.getLightColor(p_110946_, p_110947_);
        int j = LevelRenderer.getLightColor(p_110946_, p_110947_.above());
        int k = i & 0xFF;
        int l = j & 0xFF;
        int i1 = i >> 16 & 0xFF;
        int j1 = j >> 16 & 0xFF;
        return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
    }
}