package net.neoforged.neoforge.eventtest.internal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.testframework.annotation.RegisterStructureTemplate;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.collector.CollectorType;
import net.neoforged.testframework.collector.Collectors;
import net.neoforged.testframework.conf.ClientConfiguration;
import net.neoforged.testframework.conf.Feature;
import net.neoforged.testframework.conf.FrameworkConfiguration;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.impl.TestFrameworkInternal;
import org.lwjgl.glfw.GLFW;

@Mod("neotests")
public class TestsMod {
    public static final String TEMPLATE_3x3 = "neotests:empty_3x3";
    public static final String TEMPLATE_9x9 = "neotests:empty_9x9";
    public static final String TEMPLATE_3x3_FLOOR = "neotests:empty_3x3_floor";

    @RegisterStructureTemplate(TEMPLATE_3x3)
    public static final StructureTemplate TEMPLATE3x3 = StructureTemplateBuilder.empty(3, 3, 3);
    @RegisterStructureTemplate(TEMPLATE_9x9)
    public static final StructureTemplate TEMPLATE9x9 = StructureTemplateBuilder.empty(9, 9, 9);

    @RegisterStructureTemplate(TEMPLATE_3x3_FLOOR)
    public static final StructureTemplate TEMPLATE3x3_FLOOR = StructureTemplateBuilder.withSize(3, 4, 3)
            .fill(0, 0, 0, 3, 1, 3, Blocks.IRON_BLOCK.defaultBlockState())
            .build();

    public TestsMod(IEventBus modBus, ModContainer container) {
        final TestFrameworkInternal framework = FrameworkConfiguration.builder(new ResourceLocation("neotests:tests"))
                .clientConfiguration(() -> ClientConfiguration.builder()
                        .toggleOverlayKey(GLFW.GLFW_KEY_J)
                        .openManagerKey(GLFW.GLFW_KEY_N)
                        .build())

                .enable(Feature.CLIENT_SYNC, Feature.CLIENT_MODIFICATIONS, Feature.TEST_STORE)

                .withCollector(CollectorType.TESTS, Collectors.Tests.forMethodsWithAnnotation(TestHolder.class))
                .withCollector(CollectorType.TESTS, Collectors.Tests.forClassesWithAnnotation(TestHolder.class))
                .withCollector(CollectorType.TESTS, Collectors.Tests.eventTestMethodsWithAnnotation(TestHolder.class))

                .withCollector(CollectorType.INIT_LISTENERS, Collectors.defaultOnInitCollector())
                .withCollector(CollectorType.STRUCTURE_TEMPLATES, Collectors.defaultTemplateCollector())
                .withCollector(CollectorType.GROUP_DATA, Collectors.defaultGroupCollector())

                .build().create();

        framework.init(modBus, container);

        NeoForge.EVENT_BUS.addListener((final RegisterCommandsEvent event) -> {
            final LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal("tests");
            framework.registerCommands(node);
            event.getDispatcher().register(node);
        });
    }
}
