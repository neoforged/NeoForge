package net.neoforged.neodev.installer;

import net.neoforged.neodev.Constants;

public enum InstallerProcessor {
    BINPATCHER(Constants.BINPATCHER),
    FART(Constants.FART),
    INSTALLERTOOLS(Constants.INSTALLERTOOLS),
    JARSPLITTER(Constants.JARSPLITTER);

    public final String gav;

    InstallerProcessor(String gav) {
        this.gav = gav;
    }
}
