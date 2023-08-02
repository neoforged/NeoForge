package net.minecraftforge.debug;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.attachment.RegisterAttachmentTypeEvent;
import net.minecraftforge.registries.attachment.AttachmentTypeKey;

import java.util.Optional;

@Mod("attachment_test")
public class RegistryAttachmentTest {
    public static final boolean ENABLED = true;

    private static final Codec<MobEffectInstance> MOB_EFFECT_CODEC = RecordCodecBuilder.create(in -> in.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(MobEffectInstance::getEffect),
            Codec.INT.fieldOf("duration").forGetter(MobEffectInstance::getDuration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier)
    ).apply(in, MobEffectInstance::new));
    private static final AttachmentTypeKey<MobEffectInstance> ATTACHMENT_KEY = AttachmentTypeKey.get(new ResourceLocation("attachment_test:right_click_effect"));

    public RegistryAttachmentTest()
    {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener((final PlayerInteractEvent.RightClickItem event) ->
        {
            if (event.getSide().isServer()) {
                Optional.ofNullable(event.getItemStack().getItem().builtInRegistryHolder().getAttachment(ATTACHMENT_KEY))
                        .ifPresent(effect -> event.getEntity().addEffect(new MobEffectInstance(
                                effect.getEffect(), effect.getDuration(), effect.getAmplifier()
                        )));
            }
        });
        bus.addListener((final RegisterAttachmentTypeEvent event) ->
                event.register(Registries.ITEM, ATTACHMENT_KEY, builder -> builder
                        .withNetworkCodec(MOB_EFFECT_CODEC).withAttachmentCodec(MOB_EFFECT_CODEC)));

        MinecraftForge.EVENT_BUS.addListener((final ItemTooltipEvent event) ->
                Optional.ofNullable(Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ITEM).wrapAsHolder(event.getItemStack().getItem()).getAttachment(ATTACHMENT_KEY))
                        .ifPresent(effect -> event.getToolTip().add(Component.literal("When clicked, adds effect: ")
                                .append(Component.translatable(effect.getDescriptionId())))));
    }
}
