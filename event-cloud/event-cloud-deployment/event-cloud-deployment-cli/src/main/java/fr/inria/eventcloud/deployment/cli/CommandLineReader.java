/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.deployment.cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterDescription;
import com.beust.jcommander.ParameterException;

import fr.inria.eventcloud.deployment.cli.commands.Command;

/**
 * A CommandLineReader is used to read from an interactive command line some
 * commands that have to be executed.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the object type to give to a {@link Command} when it is executed.
 */
public class CommandLineReader<T> {

    private final T context;

    private final List<Command<T>> commands;

    private final Map<String, Command<T>> shortcutsMapping;

    // indicates whether the reader is reading commands
    private boolean isReading;

    private ConsoleReader reader;

    /**
     * Creates a command line reader with the specified commands to parse and
     * the given {@code object} to give to the command when it is executed. The
     * {@code object} parameter acts as the context that is used to execute the
     * operation.
     * 
     * @param commands
     *            the commands to parse.
     * 
     * @param context
     *            the context value to give to the command which is executed.
     */
    public CommandLineReader(List<Command<T>> commands, T context) {
        this.context = context;
        this.commands = new ArrayList<Command<T>>(commands);

        // adds default quit and help commands
        this.commands.add(new QuitCommand<T>());
        this.commands.add(new HelpCommand<T>());

        this.shortcutsMapping = new HashMap<String, Command<T>>();
        for (Command<T> command : this.commands) {
            this.checkIfAlreadyExists(command.getName(), command);
            this.shortcutsMapping.put(command.getName(), command);
            for (String shortcut : command.getShortcuts()) {
                this.checkIfAlreadyExists(shortcut, command);
                this.shortcutsMapping.put(shortcut, command);
            }
        }

        try {
            this.reader = new ConsoleReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkIfAlreadyExists(String shortcutName, Command<T> command) {
        if (this.shortcutsMapping.containsKey(shortcutName)) {
            throw new IllegalArgumentException("Command "
                    + this.shortcutsMapping.get(shortcutName)
                            .getClass()
                            .getCanonicalName() + " and "
                    + command.getClass().getCanonicalName()
                    + " use the same name or shortcut name '" + shortcutName
                    + "'");
        }
    }

    /**
     * Starts the execution of the interactive command-line reader.
     */
    public void run() {
        String[] commandNames = new String[this.commands.size()];
        for (int i = 0; i < this.commands.size(); i++) {
            commandNames[i] = this.commands.get(i).getName();
        }

        String inputLine;

        this.reader.addCompleter(new ArgumentCompleter(new StringsCompleter(
                commandNames)));

        this.isReading = true;
        try {
            this.reader.beep();
            while (this.isReading
                    && (inputLine = this.reader.readLine("> ")) != null) {
                String[] lineArgs = inputLine.split(" ");

                if (lineArgs.length > 0) {
                    Command<T> command = this.shortcutsMapping.get(lineArgs[0]);

                    if (command != null) {
                        try {
                            JCommander jc = new JCommander(command);
                            jc.parse(Arrays.copyOfRange(
                                    lineArgs, 1, lineArgs.length));

                            command.execute(this, this.context);
                        } catch (ParameterException e) {
                            System.out.println("\\/!\\ " + e.getMessage());
                        }
                    } else {
                        System.err.println("No command found for: "
                                + lineArgs[0]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ConsoleReader getReader() {
        return this.reader;
    }

    /**
     * Stops the execution
     */
    public void stopExecution() {
        this.isReading = false;
    }

    private static final class QuitCommand<T> extends Command<T> {

        public QuitCommand() {
            super("quit", "Quit the application", "exit", "q");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(CommandLineReader<T> reader, T context) {
            reader.stopExecution();
            System.exit(0);
        }

    }

    private static final class HelpCommand<T> extends Command<T> {

        public HelpCommand() {
            super("help", "Help", "h");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void execute(CommandLineReader<T> reader, T context) {
            StringBuilder out = new StringBuilder("usage:\n");
            for (Command<T> command : reader.commands) {
                out.append(String.format(
                        "    * %-25.25s    %s\n", command.getName(),
                        command.getDescription()));

                for (ParameterDescription param : command.getParameters()) {
                    Parameter ap =
                            ((Parameter) param.getField().getAnnotations()[0]);
                    out.append(String.format(
                            "        %-23s    %s %s\n", ap.names()[0],
                            ap.description(), ap.required()
                                    ? "(required)" : "(optional)"));
                }
            }
            System.out.println(out.toString());
        }
    }

}
