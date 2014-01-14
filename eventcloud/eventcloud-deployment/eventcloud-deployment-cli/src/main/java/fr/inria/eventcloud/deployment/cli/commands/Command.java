/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.deployment.cli.commands;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

import fr.inria.eventcloud.deployment.cli.CommandLineReader;

/**
 * A Command is an operation that have to be parsed from a command-line to be
 * executed. The parsing of the arguments associated to a command is delegated
 * to <a href="http://jcommander.org/">JCommander</a>.
 * <p>
 * To add a parameter into your application, you have to add a field and to
 * annotate it with the JCommander annotations. For more information you can
 * look at the <a href="http://jcommander.org/#Overview">JCommander
 * tutorial</a>.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the type associated to the context object that is given to the
 *            command it is executed by using
 *            {@link Command#execute(CommandLineReader, Object)}.
 */
public abstract class Command<T> {

    private final String name;

    private final Set<String> shortcuts;

    private final String description;

    private final List<ParameterDescription> parameters;

    /**
     * Creates a new command with the specified name, description and list of
     * shortcuts.
     * 
     * @param name
     *            the command name.
     * @param description
     *            a short command description.
     * @param shortcuts
     *            the shortcuts associated to the command. This parameter is
     *            optional.
     */
    public Command(String name, String description, String... shortcuts) {
        this.name = name;
        this.description = description;
        this.shortcuts = new HashSet<String>();

        this.parameters = new JCommander(this).getParameters();

        Collections.addAll(this.shortcuts, shortcuts);
    }

    /**
     * Executes the command with the specified reader and context object.
     * 
     * @param reader
     *            the {@link CommandLineReader} that has been used to parse the
     *            command that is executed.
     * 
     * @param context
     *            the context object.
     */
    public abstract void execute(CommandLineReader<T> reader, T context);

    /**
     * Returns the command name.
     * 
     * @return the command name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a short command description.
     * 
     * @return a short command description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the shortcuts associated to the command name. A command may have
     * no shortcut.
     * 
     * @return the shortcuts associated to the command name.
     */
    public Set<String> getShortcuts() {
        return this.shortcuts;
    }

    /**
     * Returns the {@link JCommander} parameters associated to the command.
     * 
     * @return the {@link JCommander} parameters associated to the command.
     */
    public List<ParameterDescription> getParameters() {
        return this.parameters;
    }

}
