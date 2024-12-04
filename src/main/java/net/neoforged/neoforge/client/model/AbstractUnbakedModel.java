package net.neoforged.neoforge.client.model;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Nullable;

/**
 * Base unbaked model for custom models which support the standard top-level model parameters
 * added by vanilla and NeoForge except elements but create the quads from something other
 * than the vanilla elements spec.
 */
public abstract class AbstractUnbakedModel implements ExtendedUnbakedModel {
    protected final StandardModelParameters parameters;
    private UnbakedModel parent;

    protected AbstractUnbakedModel(StandardModelParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        if (this.parameters.parent() != null) {
            this.parent = resolver.resolve(this.parameters.parent());
        }
    }

    @Nullable
    @Override
    public Boolean getAmbientOcclusion() {
        return this.parameters.ambientOcclusion();
    }

    @Nullable
    @Override
    public GuiLight getGuiLight() {
        return this.parameters.guiLight();
    }

    @Nullable
    @Override
    public ItemTransforms getTransforms() {
        return this.parameters.itemTransforms();
    }

    @Override
    public TextureSlots.Data getTextureSlots() {
        return this.parameters.textures();
    }

    @Nullable
    @Override
    public UnbakedModel getParent() {
        return this.parent;
    }

    @Override
    public void fillAdditionalProperties(ContextMap.Builder propertiesBuilder) {
        if (this.parameters.rootTransform() != null) {
            propertiesBuilder.withParameter(NeoForgeModelProperties.TRANSFORM, this.parameters.rootTransform());
        }
        if (!this.parameters.renderTypeGroup().isEmpty()) {
            propertiesBuilder.withParameter(NeoForgeModelProperties.RENDER_TYPE, this.parameters.renderTypeGroup());
        }
        if (!this.parameters.partVisibility().isEmpty()) {
            propertiesBuilder.withParameter(NeoForgeModelProperties.PART_VISIBILITY, this.parameters.partVisibility());
        }
    }
}
