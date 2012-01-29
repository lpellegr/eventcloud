/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud.deployment.cli.launchers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.beust.jcommander.JCommander;

/**
 * This class offers the possibility to any launcher to redirect the output of
 * the {@link #run()} method either to the standard output or to a file which is
 * named instance file when a Java property with name
 * {@link Launcher#INSTANCE_FILE_JAVA_PROPERTY_NAME} is defined.
 * 
 * @author lpellegr
 */
public abstract class Launcher {

    private static final String INSTANCE_FILE_JAVA_PROPERTY_NAME =
            "eventcloud.instance.file";

    private File instanceFile;

    protected Launcher() {
        if (System.getProperty(INSTANCE_FILE_JAVA_PROPERTY_NAME) != null) {
            this.instanceFile =
                    new File(
                            System.getProperty(INSTANCE_FILE_JAVA_PROPERTY_NAME));
        }
    }

    protected void parseArguments(Launcher instance, String[] args) {
        new JCommander(instance).parse(args);
    }

    public void launch() {
        String bindingName = this.run();

        if (this.instanceFile != null) {
            this.createInstanceFile(bindingName);
        } else {
            System.out.println(bindingName);
        }
    }

    protected abstract String run();

    private void createInstanceFile(String msg) {
        if (this.instanceFile.exists()) {
            throw new IllegalStateException("Instance file already exists: "
                    + this.instanceFile.toString());
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(this.instanceFile);
            fos.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
