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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Main class that is executed to list the launchers and readers that available
 * when the JAR associated to this module is executed.
 * 
 * @author lpellegr
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Available launchers are:");
        try {
            for (Class<?> clazz : getClasses("fr.inria.eventcloud.deployment.cli.launchers")) {
                System.out.println("  - " + clazz.getCanonicalName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println();

        System.out.println("Available readers are:");
        try {
            for (Class<?> clazz : getClasses("fr.inria.eventcloud.deployment.cli.readers")) {
                System.out.println("  - " + clazz.getCanonicalName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * The following methods have been retrieved from
     * http://internna.blogspot.com/2007/11/java-5-retrieving-all-classes-from.html
     */

    private static Set<Class<?>> getClasses(String packageName)
            throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return getClasses(loader, packageName);
    }

    private static Set<Class<?>> getClasses(ClassLoader loader,
                                            String packageName)
            throws FileNotFoundException, IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = loader.getResources(path);
        if (resources != null) {
            while (resources.hasMoreElements()) {
                String filePath = resources.nextElement().getFile();
                // WINDOWS HACK
                if (filePath.indexOf("%20") > 0) {
                    filePath = filePath.replaceAll("%20", " ");
                }
                if (filePath != null) {
                    if ((filePath.indexOf('!') > 0)
                            && (filePath.indexOf(".jar") > 0)) {
                        String jarPath =
                                filePath.substring(0, filePath.indexOf('!'))
                                        .substring(filePath.indexOf(':') + 1);
                        // WINDOWS HACK
                        if (jarPath.indexOf(':') >= 0) {
                            jarPath = jarPath.substring(1);
                        }
                        classes.addAll(getFromJARFile(jarPath, path));
                    } else {
                        classes.addAll(getFromDirectory(
                                new File(filePath), packageName));
                    }
                }
            }
        }
        return classes;
    }

    private static Set<Class<?>> getFromDirectory(File directory,
                                                  String packageName)
            throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        if (directory.exists()) {
            for (String file : directory.list()) {
                if (file.endsWith(".class")) {
                    String name =
                            packageName + '.' + stripFilenameExtension(file);
                    Class<?> clazz = Class.forName(name);
                    if (!Modifier.isAbstract(clazz.getModifiers())
                            && !Modifier.isInterface(clazz.getModifiers())
                            && clazz.getEnclosingClass() == null) {
                        classes.add(clazz);
                    }
                }
            }
        }
        return classes;
    }

    private static Set<Class<?>> getFromJARFile(String jar, String packageName)
            throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(jar));
            JarEntry jarEntry;

            do {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry != null) {
                    String className = jarEntry.getName();
                    if (className.endsWith(".class")) {
                        className = stripFilenameExtension(className);
                        if (className.startsWith(packageName)) {
                            Class<?> clazz =
                                    Class.forName(className.replace('/', '.'));
                            if (!Modifier.isAbstract(clazz.getModifiers())
                                    && !Modifier.isInterface(clazz.getModifiers())
                                    && clazz.getEnclosingClass() == null) {
                                classes.add(clazz);
                            }
                        }
                    }
                }
            } while (jarEntry != null);
        } catch (IOException e) {
            throw new IllegalStateException();
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return classes;
    }

    private static String stripFilenameExtension(String path) {
        if (path == null) {
            return null;
        }
        int sepIndex = path.lastIndexOf('.');
        return (sepIndex != -1
                ? path.substring(0, sepIndex) : path);
    }

}
