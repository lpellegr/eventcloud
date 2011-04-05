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
