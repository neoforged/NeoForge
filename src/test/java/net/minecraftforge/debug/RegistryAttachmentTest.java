package net.minecraftforge.debug;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.AttachmentProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.attachment.AttachmentValueMerger;
import net.minecraftforge.registries.attachment.RegisterAttachmentTypeEvent;
import net.minecraftforge.registries.attachment.AttachmentTypeKey;
import net.minecraftforge.registries.holdersets.AndHolderSet;
import net.minecraftforge.registries.holdersets.NotHolderSet;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mod(RegistryAttachmentTest.MODID)
public class RegistryAttachmentTest
{
    private static final boolean ENABLED = true;
    public static final String MODID = "attachment_test";

    private static final Codec<MobEffectInstance> MOB_EFFECT_CODEC = RecordCodecBuilder.create(in -> in.group(
            ForgeRegistries.MOB_EFFECTS.getCodec().fieldOf("effect").forGetter(MobEffectInstance::getEffect),
            Codec.INT.fieldOf("duration").forGetter(MobEffectInstance::getDuration),
            Codec.INT.optionalFieldOf("amplifier", 0).forGetter(MobEffectInstance::getAmplifier)
    ).apply(in, MobEffectInstance::new));
    private static final AttachmentTypeKey<MobEffectInstance> MOB_EFFECTS = AttachmentTypeKey.get(new ResourceLocation(MODID, "right_click_effect"));
    private static final AttachmentTypeKey<List<Integer>> INTS = AttachmentTypeKey.get(new ResourceLocation(MODID, "ints"));

    public RegistryAttachmentTest()
    {
        if (!ENABLED) return;

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.addListener((final PlayerInteractEvent.RightClickItem event) ->
        {
            if (event.getSide().isServer()) {
                Optional.ofNullable(event.getItemStack().getItem().builtInRegistryHolder().getAttachment(MOB_EFFECTS))
                        .ifPresent(effect -> event.getEntity().addEffect(new MobEffectInstance(
                                effect.getEffect(), effect.getDuration(), effect.getAmplifier()
                        )));
            }
        });
        bus.addListener((final AddPackFindersEvent event) -> {
            if (event.getPackType() == PackType.SERVER_DATA) {
                var resourcePath = ModList.get().getModFileById("attachment_test").getFile().findResource("test_attachments");
                var pack = Pack.readMetaAndCreate("builtin/registry_attachment_test", Component.literal("Registry Attachment Test"), false,
                        (path) -> new PathPackResources(path, resourcePath, false), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
                event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
            }
        });
        bus.addListener((final GatherDataEvent event) ->
        {
            event.getGenerator().addProvider(event.includeServer(), new AttachmentProvider<>(
                    event.getGenerator().getPackOutput(), Registries.ITEM, MOB_EFFECTS,
                    event.getLookupProvider(), false
            )
            {
                @Override
                protected void buildAttachments(HolderLookup.RegistryLookup<Item> registry, AttachmentBuilder<MobEffectInstance, Item> builder) {
                    builder.attach(BuiltInRegistries.ITEM.wrapAsHolder(Items.ANDESITE), new MobEffectInstance(MobEffects.BAD_OMEN, 25));
                    builder.attach(BuiltInRegistries.ITEM.getOrCreateTag(ItemTags.ANVIL), new MobEffectInstance(MobEffects.HARM, 1, 3));
                    builder.attach(new AndHolderSet<>(
                            List.of(HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.ALLAY_SPAWN_EGG), BuiltInRegistries.ITEM.wrapAsHolder(Items.BAMBOO_RAFT)), new NotHolderSet<>(registry, HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.BAMBOO_RAFT))))
                    ), new MobEffectInstance(MobEffects.INVISIBILITY, 1000));
                }
            });
        });
        bus.addListener((final RegisterAttachmentTypeEvent event) ->
        {
            event.register(Registries.ITEM, MOB_EFFECTS, builder -> builder
                    .withNetworkCodec(MOB_EFFECT_CODEC).withAttachmentCodec(MOB_EFFECT_CODEC));
            event.register(Registries.ITEM, INTS, builder -> builder.withAttachmentCodec(Codec.INT.listOf())
                    .withNetworkCodec(Codec.INT.listOf()).withMerger(AttachmentValueMerger.mergeLists()));
        });

        MinecraftForge.EVENT_BUS.addListener((final ItemTooltipEvent event) ->
        {
            Optional.ofNullable(Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.ITEM).wrapAsHolder(event.getItemStack().getItem()).getAttachment(MOB_EFFECTS))
                    .ifPresent(effect -> event.getToolTip().add(Component.literal("When clicked, adds effect: ")
                            .append(Component.translatable(effect.getDescriptionId()))));
            Optional.ofNullable(event.getItemStack().getItem().builtInRegistryHolder().getAttachment(INTS))
                    .ifPresent(ints -> event.getToolTip().add(Component.literal("Ints: " + Arrays.toString(ints.toArray()))));
        });
    }
}
