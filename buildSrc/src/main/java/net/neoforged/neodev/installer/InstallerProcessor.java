package net.neoforged.neodev.installer;

import net.neoforged.neodev.Tools;

/**
 * Identifies the tools used by the {@link InstallerProfile} to install NeoForge.
 */
public enum InstallerProcessor {
    INSTALLERTOOLS(Tools.INSTALLERTOOLS);

    public final Tools tool;

    InstallerProcessor(Tools tool) {
        this.tool = tool;
    }
}
