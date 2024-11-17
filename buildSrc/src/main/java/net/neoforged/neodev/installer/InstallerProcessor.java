package net.neoforged.neodev.installer;

import net.neoforged.neodev.Tools;

/**
 * Identifies the tools used by the {@link InstallerProfile} to install NeoForge.
 */
public enum InstallerProcessor {
    BINPATCHER(Tools.BINPATCHER),
    FART(Tools.AUTO_RENAMING_TOOL),
    INSTALLERTOOLS(Tools.INSTALLERTOOLS),
    JARSPLITTER(Tools.JARSPLITTER);

    public final Tools tool;

    InstallerProcessor(Tools tool) {
        this.tool = tool;
    }
}
