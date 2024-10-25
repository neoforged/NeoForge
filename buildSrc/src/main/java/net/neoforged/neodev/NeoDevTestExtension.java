package net.neoforged.neodev;

import net.neoforged.moddevgradle.dsl.ModModel;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

import javax.inject.Inject;

public abstract class NeoDevTestExtension {
    public static final String NAME = "neoDevTest";

    @Inject
    public NeoDevTestExtension() {
    }

    /**
     * The mod that will be loaded in JUnit tests.
     * The compiled classes from {@code src/test/java} and the resources from {@code src/test/resources}
     * will be added to that mod at runtime.
     */
    public abstract Property<ModModel> getTestedMod();

    /**
     * The mods to load when running unit tests. Defaults to all mods registered in the project.
     * This must contain {@link #getTestedMod()}.
     *
     * @see ModModel
     */
    public abstract SetProperty<ModModel> getLoadedMods();
}
