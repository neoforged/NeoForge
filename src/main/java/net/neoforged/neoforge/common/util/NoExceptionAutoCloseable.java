package net.neoforged.neoforge.common.util;

/// An [AutoCloseable] that does not throw an [Exception] on [#close()].
public interface NoExceptionAutoCloseable extends AutoCloseable {

    @Override
    void close();
}
