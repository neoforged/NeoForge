package net.neoforged.neodev;

// TODO: reconsider organization of constants
public class Constants {
    public static final String BINPATCHER_VERSION =  "1.1.1";
    public static final String BINPATCHER_VERSION_INTERPOLATION = "net.minecraftforge:binarypatcher:%s:fatjar";
    public static final String BINPATCHER = String.format(BINPATCHER_VERSION_INTERPOLATION, BINPATCHER_VERSION);
    public static final String FART_VERSION = "2.0.3";
    public static final String FART_ARTIFACT_INTERPOLATION = "net.neoforged:AutoRenamingTool:%s:all";
    public static final String FART = String.format(FART_ARTIFACT_INTERPOLATION, FART_VERSION);
    public static final String INSTALLERTOOLS_VERSION = "2.1.2";
    public static final String INSTALLERTOOLS = "net.neoforged.installertools:installertools:" + INSTALLERTOOLS_VERSION;
    public static final String JARSPLITTER = "net.neoforged.installertools:jarsplitter:" + INSTALLERTOOLS_VERSION;
    public static final String BINARYPATCHER = "net.neoforged.installertools:binarypatcher:" + INSTALLERTOOLS_VERSION;
}
