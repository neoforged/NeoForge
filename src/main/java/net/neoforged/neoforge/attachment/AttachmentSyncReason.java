package net.neoforged.neoforge.attachment;

public enum AttachmentSyncReason {
    // Might not actually be new on the server, but it is now for the client
    NEW_ENTITY,
    ENTITY_SYNC_REQUESTED,
}
