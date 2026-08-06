package org.bukkit.craftbukkit.util;

import com.mojang.logging.LogQueues;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.craftbukkit.Main;
import org.jline.reader.LineReader;

public class TerminalConsoleWriterThread extends Thread {
    private final LineReader reader;
    private final OutputStream output;

    public TerminalConsoleWriterThread(OutputStream output, LineReader reader) {
        super("TerminalConsoleWriter");
        this.output = output;
        this.reader = reader;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        String message;

        // Using name from log4j config in vanilla jar
        while (true) {
            message = LogQueues.getNextLogEvent("TerminalConsole");
            if (message == null) {
                continue;
            }

            try {
                if (Main.useJline) {
                    reader.printAbove(message);
                } else {
                    output.write(message.getBytes());
                    output.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(TerminalConsoleWriterThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
