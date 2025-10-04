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
import java.util.Objects;

/**
 * Creates the userdev configuration file used by the various Gradle plugins used to develop
 * mods for NeoForge, such as <a href="https://github.com/architectury/architectury-loom">Architectury Loom</a>,
 * <a href="https://github.com/neoforged/ModDevGradle/">ModDevGradle
 * or <a href="https://github.com/neoforged/NeoGradle">NeoGradle</a>.
 */
abstract class CreateUserDevConfig extends DefaultTask {
    @Inject
    public CreateUserDevConfig() {}

    @Input
    abstract Property<String> getMinecraftVersion();

    @Input
    abstract Property<String> getNeoForgeVersion();

    @Input
    abstract Property<String> getRawNeoFormVersion();

    @Input
    abstract ListProperty<String> getLibraries();

    @Input
    abstract ListProperty<String> getTestLibraries();

    @Input
    abstract Property<String> getBinpatcherGav();

    @OutputFile
    abstract RegularFileProperty getUserDevConfig();

    @TaskAction
    public void writeUserDevConfig() throws IOException {
        var features = new UserDevFeatures(
            true //Since 21.9 we use a more advanced version of FML which discovers dependencies and their libraries directly from the CP, no need for additional classpath elements.
        );

        var config = new UserDevConfig(
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
                List.of() /* deprecated: modules */,
                features);

        for (var runType : RunType.values()) {
            List<String> args = new ArrayList<>();

            if (runType == RunType.CLIENT || runType == RunType.JUNIT) {
                // TODO: this is copied from NG but shouldn't it be the MC version?
                Collections.addAll(args,
                        "--version", getNeoForgeVersion().get());
            }

            if (runType == RunType.CLIENT || runType == RunType.CLIENT_DATA || runType == RunType.JUNIT) {
                Collections.addAll(args,
                        "--assetIndex", "{asset_index}",
                        "--assetsDir", "{assets_root}");
            }

            Collections.addAll(args,
                    "--fml.mcVersion", getMinecraftVersion().get(),
                    "--fml.neoForgeVersion", getNeoForgeVersion().get(),
                    "--fml.neoFormVersion", getRawNeoFormVersion().get());

            Map<String, String> systemProperties = new LinkedHashMap<>();
            systemProperties.put("java.net.preferIPv6Addresses", "system");

            if (runType == RunType.CLIENT || runType == RunType.GAME_TEST_SERVER) {
                systemProperties.put("neoforge.enableGameTest", "true");
            }

            config.runs().put(runType.jsonName, new UserDevRunType(
                    runType != RunType.JUNIT,
                    // Archloom crashes when reading a userconfig without a main class
                    Objects.requireNonNullElse(runType.mainClass, "NONE"),
                    args,
                    List.of(
                            "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
                            "--add-exports", "jdk.naming.dns/com.sun.jndi.dns=java.naming"),
                    runType == RunType.CLIENT || runType == RunType.JUNIT || runType == RunType.CLIENT_DATA,
                    runType == RunType.GAME_TEST_SERVER || runType == RunType.SERVER || runType == RunType.SERVER_DATA,
                    runType == RunType.CLIENT_DATA || runType == RunType.SERVER_DATA,
                    runType == RunType.CLIENT || runType == RunType.GAME_TEST_SERVER,
                    runType == RunType.JUNIT,
                    Map.of(
                            "MOD_CLASSES", "{source_roots}"),
                    systemProperties
            ));
        }

        FileUtils.writeStringSafe(
                getUserDevConfig().getAsFile().get().toPath(),
                new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(config),
                StandardCharsets.UTF_8);
    }

    private enum RunType {
        CLIENT("client", "net.neoforged.fml.startup.Client"),
        CLIENT_DATA("clientData", "net.neoforged.fml.startup.DataClient"),
        SERVER_DATA("serverData", "net.neoforged.fml.startup.DataServer"),
        GAME_TEST_SERVER("gameTestServer", "net.neoforged.fml.startup.GameTestServer"),
        SERVER("server", "net.neoforged.fml.startup.Server"),
        JUNIT("junit", null);
        private final String jsonName;
        private final String mainClass;

        RunType(String jsonName, String mainClass) {
            this.jsonName = jsonName;
            this.mainClass = mainClass;
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
        UserDevFeatures features) {}

record UserDevFeatures(
    boolean noLegacyClasspath) {}

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
