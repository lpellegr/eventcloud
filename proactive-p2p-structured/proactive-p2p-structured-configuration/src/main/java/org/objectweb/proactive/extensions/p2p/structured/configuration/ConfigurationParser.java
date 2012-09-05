package org.objectweb.proactive.extensions.p2p.structured.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to load properties (declared as public, static and being an instance of
 * {@link Property}) according to a java property (which gives the path to a
 * properties file) or a path to a preferences file (which is a properties
 * file).
 * 
 * @author lpellegr
 */
public class ConfigurationParser {

    private static final Logger log =
            LoggerFactory.getLogger(ConfigurationParser.class);

    /**
     * Loads the properties contained by the file referenced with the java
     * property {@code javaPropertyName} or {@code defaultPreferencesFile} (the
     * java property has an higher priority than the default preferences file).
     * Then, for each property {@code P} loaded from the file, the method tries
     * to find a property which is an instance of {@link Property}, declared as
     * public, static and that has the same name as P. In that case the value
     * associated to the property is updated by using the value contained by
     * {@code P}.
     * <p>
     * <strong>This method must be called from a static block after having
     * declared the properties.</strong>
     * 
     * @param classProperties
     *            the class from where the public static properties are
     *            searched.
     * 
     * @param javaPropertyName
     *            the java property name to get in order to know the path to
     *            file containing the values of the properties to update.
     * 
     * @param defaultPreferencesFile
     *            a file containing the values of the properties to update.
     * 
     * @return the path to the preferences file which has been loaded or
     *         {@code null}.
     */
    public static File load(Class<?> classProperties, String javaPropertyName,
                            String defaultPreferencesFile) {
        File configurationFile;

        if (System.getProperty(javaPropertyName) != null
                && (configurationFile =
                        new File(System.getProperty(javaPropertyName))).exists()) {
            load(configurationFile, classProperties);
        } else if ((configurationFile = new File(defaultPreferencesFile)).exists()) {
            load(configurationFile, classProperties);
        }

        return configurationFile;
    }

    @SuppressWarnings("unchecked")
    private static void load(File configurationFile, Class<?> classProperties) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configurationFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> properties = findProperties(classProperties);

        for (String propertyName : props.stringPropertyNames()) {
            if (properties.containsKey(propertyName)) {
                try {
                    ((Property<Object>) classProperties.getField(
                            properties.get(propertyName)).get(null)).setValueAsString(props.getProperty(propertyName));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                log.info(
                        "Property '{}' has been loaded with value '{}'",
                        propertyName, props.getProperty(propertyName));
            } else {
                log.warn("Skipped unknown property: " + propertyName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> findProperties(Class<?> classProperties) {
        Map<String, String> result = new HashMap<String, String>();

        for (Field field : classProperties.getFields()) {
            // any property is assumed to extend Property
            // and to be declared as public and static
            if (Modifier.isPublic(field.getModifiers())
                    && Modifier.isStatic(field.getModifiers())
                    && Property.class.isAssignableFrom(field.getType())) {
                Property<Object> fieldInstance;
                try {
                    fieldInstance = (Property<Object>) field.get(null);
                    result.put(fieldInstance.name, field.getName());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

}
