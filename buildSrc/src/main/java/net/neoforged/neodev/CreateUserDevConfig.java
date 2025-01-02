package net.neoforged.neodev;

import com.google.gson.GsonBuilder;
import net.neoforged.neodev.utils.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the userdev configuration file used by the various Gradle plugins used to develop
 * mods for NeoForge, such as <a href="https://github.com/architectury/architectury-loom">Architectury Loom</a>,
 * <a href="https://github.com/neoforged/ModDevGradle/">ModDevGradle
 * or <a href="https://github.com/neoforged/NeoGradle">NeoGradle</a>.
 */
abstract class CreateUserDevConfig extends DefaultTask {
    @Inject
    public CreateUserDevConfig() {}

    /**
     * Toggles the launch type written to the userdev configuration between *dev and *userdev.
     */
    @Input
    abstract Property<Boolean> getForNeoDev();

    @Input
    abstract Property<String> getFmlVersion();

    @Input
    abstract Property<String> getMinecraftVersion();

    @Input
    abstract Property<String> getNeoForgeVersion();

    @Input
    abstract Property<String> getRawNeoFormVersion();

    @Input
    abstract ListProperty<String> getLibraries();

    @Input
    abstract ListProperty<String> getModules();

    @Input
    abstract ListProperty<String> getJavaAgents();

    @Input
    abstract ListProperty<String> getTestLibraries();

    @Input
    abstract ListProperty<String> getIgnoreList();

    @Input
    abstract Property<String> getBinpatcherGav();

    @OutputFile
    abstract RegularFileProperty getUserDevConfig();

    @TaskAction
    public void writeUserDevConfig() throws IOException {
        var config = new UserDevConfig(
                // TODO: Technically new field... backwards compatible?
                2,
                "net.neoforged:neoform:%s-%s@zip".formatted(getMinecraftVersion().get(), getRawNeoFormVersion().get()),
                "ats/",
                "joined.lzma",
                new BinpatcherConfig(
                        getBinpatcherGav().get(),
                        List.of("--clean", "{clean}", "--output", "{output}", "--apply", "{patch}")),
                "patches/",
                "net.neoforged:neoforge:%s:sources".formatted(getNeoForgeVersion().get()),
                "net.neoforged:neoforge:%s:universal".formatted(getNeoForgeVersion().get()),
                getLibraries().get(),
                getTestLibraries().get(),
                new LinkedHashMap<>(),
                getModules().get(),
                getJavaAgents().get()
        );

        for (var runType : RunType.values()) {
            var mainClass = switch (runType) {
                case CLIENT -> "net.neoforged.fml.startup.Client";
                case DATA -> "net.neoforged.fml.startup.Data";
                case GAME_TEST_SERVER, SERVER -> "net.neoforged.fml.startup.Server";
                case JUNIT -> null;
            };

            List<String> args = new ArrayList<>();

            if (runType == RunType.CLIENT) {
                // TODO: this is copied from NG but shouldn't it be the MC version?
                Collections.addAll(args, "--version", getNeoForgeVersion().get());
            }

            if (runType == RunType.CLIENT || runType == RunType.DATA) {
                Collections.addAll(args, "--assetIndex", "{asset_index}", "--assetsDir", "{assets_root}");
            }

            Collections.addAll(args,
                    "--gameDir", ".",
                    "--fml.fmlVersion", getFmlVersion().get(),
                    "--fml.mcVersion", getMinecraftVersion().get(),
                    "--fml.neoForgeVersion", getNeoForgeVersion().get(),
                    "--fml.neoFormVersion", getRawNeoFormVersion().get());

            Map<String, String> systemProperties = new LinkedHashMap<>();
            systemProperties.put("java.net.preferIPv6Addresses", "system");

            if (runType == RunType.CLIENT || runType == RunType.GAME_TEST_SERVER) {
                systemProperties.put("neoforge.enableGameTest", "true");

                if (runType == RunType.GAME_TEST_SERVER) {
                    systemProperties.put("neoforge.gameTestServer", "true");
                }
            }

            config.runs().put(runType.jsonName, new UserDevRunType(
                    runType != RunType.JUNIT,
                    mainClass,
                    args,
                    List.of(
                            "--add-opens", "java.base/java.util.jar=ALL-UNNAMED",
                            "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
                            "--add-exports", "java.base/sun.security.util=ALL-UNNAMED",
                            "--add-exports", "jdk.naming.dns/com.sun.jndi.dns=java.naming"),
                    runType == RunType.CLIENT || runType == RunType.JUNIT,
                    runType == RunType.GAME_TEST_SERVER || runType == RunType.SERVER,
                    runType == RunType.DATA,
                    runType == RunType.CLIENT || runType == RunType.GAME_TEST_SERVER,
                    runType == RunType.JUNIT,
                    Map.of("MOD_CLASSES", "{source_roots}"),
                    systemProperties
            ));
        }

        FileUtils.writeStringSafe(
                getUserDevConfig().getAsFile().get().toPath(),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(config),
                // TODO: Not sure what this should be? Most likely the file is ASCII.
                StandardCharsets.UTF_8);
    }

    private enum RunType {
        CLIENT("client"),
        DATA("data"),
        GAME_TEST_SERVER("gameTestServer"),
        SERVER("server"),
        JUNIT("junit");

        private final String jsonName;

        RunType(String jsonName) {
            this.jsonName = jsonName;
        }
    }
}

record UserDevConfig(
        int spec,
        String mcp,
        String ats,
        String binpatches,
        BinpatcherConfig binpatcher,
        String patches,
        String sources,
        String universal,
        List<String> libraries,
        List<String> testLibraries,
        Map<String, UserDevRunType> runs,
        List<String> modules,
        List<String> javaAgents
) {
}

record BinpatcherConfig(
        String version,
        List<String> args) {}

record UserDevRunType(
        boolean singleInstance,
        String main,
        List<String> args,
        List<String> jvmArgs,
        boolean client,
        boolean server,
        boolean dataGenerator,
        boolean gameTest,
        boolean unitTest,
        Map<String, String> env,
        Map<String, String> props) {}
