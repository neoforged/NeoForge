package net.neoforged.neodev.installer;

public record LibraryArtifact(
        String sha1,
        long size,
        String url,
        String path) {}
