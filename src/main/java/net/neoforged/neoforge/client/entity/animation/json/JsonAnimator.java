package net.neoforged.neoforge.client.entity.animation.json;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import org.jetbrains.annotations.NotNull;

/**
 * Use this to animate your entity with animations loaded from JSON files. You can create these animations using the
 * "Animation to JSON Converter" plugin from the Blockbench plugins store.
 */
public final class JsonAnimator {
    private final HierarchicalModel<?> model;

    /**
     * Creates a {@code JsonAnimator} associated with an entity model.
     * @param model The associated entity model.
     */
    public JsonAnimator(@NotNull HierarchicalModel<?> model) {
        this.model = model;
    }

    public void animate(@NotNull AnimationState state, @NotNull ResourceLocation animationLocation, float ageInTicks) {
        model.animate(state, JsonAnimationLoader.INSTANCE.getAnimationOrThrow(animationLocation), ageInTicks);
    }

    public void animateWalk(
        @NotNull ResourceLocation animationLocation,
        float limbSwing, float limbSwingAmount, float maxAnimationSpeed, float animationScaleFactor
    ) {
        model.animateWalk(
            JsonAnimationLoader.INSTANCE.getAnimationOrThrow(animationLocation),
            limbSwing, limbSwingAmount, maxAnimationSpeed, animationScaleFactor
        );
    }

    public void animate(@NotNull AnimationState state, @NotNull ResourceLocation animationLocation, float ageInTicks, float speed) {
        model.animate(state, JsonAnimationLoader.INSTANCE.getAnimationOrThrow(animationLocation), ageInTicks, speed);
    }

    public void applyStatic(@NotNull ResourceLocation animationLocation) {
        model.applyStatic(JsonAnimationLoader.INSTANCE.getAnimationOrThrow(animationLocation));
    }
}
