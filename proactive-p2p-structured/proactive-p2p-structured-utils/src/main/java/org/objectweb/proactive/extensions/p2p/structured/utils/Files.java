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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for operations on {@link File}s.
 * 
 * @author lpellegr
 */
public final class Files {

    private Files() {
        
    }
    
    /**
     * Recursively delete a given directory.
     * 
     * @param path
     *            the path to the directory to remove.
     * 
     * @throws IOException
     */
    public static void deleteDirectory(File path) throws IOException {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    Files.deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete " + file);
                    }
                }
            }

            path.delete();
        }
    }

}
