package ru.spbau.mit.commands;

import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.Shell;
import ru.spbau.mit.parsing.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>echo</code> command
 */
@CommandAnnotation(commandName = "echo")
public final class EchoCommand extends Command {

    @NotNull private static final Logger LOGGER = Logger.getLogger(EchoCommand.class.getName());

    /**
     * {@inheritDoc}
     */
    public EchoCommand(@NotNull Shell shell, @NotNull OutputStream out) {
        super(shell, out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int execute(@NotNull InputStream in, @NotNull Token[]args) {
        /*
          Echo command in bash ignores it's input stream
         */
        try {
            int i = 0;
            for (Token arg : args) {
                i++;
                out.write(arg.getContent().getBytes());
                if (i != args.length) {
                    out.write(" ".getBytes());
                }
            }
            out.write("\n".getBytes()); // that's what bash does
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "could not write to output stream", e);
            return 1;
        }
        return 0;
    }


}
