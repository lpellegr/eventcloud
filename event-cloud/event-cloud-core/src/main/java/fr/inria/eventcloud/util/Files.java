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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.util;

import java.io.File;

/**
 * Utility class for operations on {@link File}s.
 * 
 * @author lpellegr
 */
public class Files {

    /**
     * Recursively delete a given directory.
     * 
     * @param path
     *            the path to the directory to remove.
     * @return <code>true</code> is the delete has succeeded, <code>false</code>
     *         otherwise.
     */
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    Files.deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            return path.delete();
        }
        return false;
    }

}
