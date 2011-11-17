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

/**
 * Any launchers extends this abstract class to have the possibility to create
 * easily an instance file which indicates that the launcher has finished to
 * deploy the application.
 * 
 * @author lpellegr
 */
public abstract class Launcher {

    protected static final String INSTANCE_FILE_JAVA_PROPERTY_NAME =
            "eventcloud.instance.file";

    private final File instanceFile;

    protected Launcher(String javaPropertyName) {
        if (System.getProperty(javaPropertyName) == null) {
            throw new IllegalArgumentException("Java property '"
                    + javaPropertyName + "' undefined");
        }

        this.instanceFile = new File(System.getProperty(javaPropertyName));
    }

    public void launch() {
        this.createInstanceFile(this.run());
    }

    protected abstract String run();

    protected void createInstanceFile(String message) {
        if (this.instanceFile.exists()) {
            throw new IllegalArgumentException("Instance file already exists: "
                    + this.instanceFile.toString());
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(this.instanceFile);
            fos.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
