package net.neoforged.neodev.installer;

record LibraryArtifact(
        String sha1,
        long size,
        String url,
        String path) {}
