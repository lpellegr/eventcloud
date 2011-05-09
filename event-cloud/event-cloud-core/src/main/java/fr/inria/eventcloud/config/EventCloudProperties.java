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
package fr.inria.eventcloud.config;

import java.io.File;

import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyInteger;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;
import org.objectweb.proactive.extensions.p2p.structured.util.SystemUtil;
import org.ontoware.rdf2go.model.node.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.util.RDF2GoBuilder;

/**
 * Contains default values for Event-Cloud properties.
 * <p>
 * The first time a property is retrieved, before it, the class will try to load
 * properties from a preferences file. The location of this file is by default
 * set to {@code %HOMEPATH%\eventcloud\preferences} on Windows and to
 * {@code $HOME/.eventcloud/preferences} on Unix systems. You can also override
 * the default location by using the java property
 * {@code eventcloud.configuration}.
 * 
 * @author lpellegr
 */
public class EventCloudProperties {

    private static final Logger logger =
            LoggerFactory.getLogger(EventCloudProperties.class);

    public static final PropertyInteger CONSISTENCY_TIMEOUT =
            new PropertyInteger("repository.consistency.timeout", 500);

    // TODO: remove this property as soon as possible (however
    // we need to define how to manage the context part before)
    public static final URI DEFAULT_CONTEXT =
            RDF2GoBuilder.createURI("http://www.inria.fr");

    public static final String PATH_SEPARATOR =
            System.getProperty("file.separator");

    public static final PropertyString REPOSITORIES_PATH = new PropertyString(
            "repositories.path", getDefaultRepositoriesPath());

    static {
        File preferencesFile = new File(getPreferencesFilePath());

        String eventCloudConfigurationProperty =
                System.getProperty("eventcloud.configuration");
        if (eventCloudConfigurationProperty != null) {
            preferencesFile = new File(eventCloudConfigurationProperty);
        }

        if (preferencesFile.exists()) {
            ConfigurationParser.parse(preferencesFile.toString());
        } else {
            logger.info(
                    "No Event-Cloud properties loaded because file {} does not exist",
                    preferencesFile);
        }
    }

    public static final String getPreferencesFilePath() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getPreferencesPath());
        buffer.append(System.getProperty("file.separator"));
        buffer.append("preferences");

        return buffer.toString();
    }

    /**
     * Returns the default path to the preferences.
     * 
     * @return the default path to the preferences.
     */
    public static final String getPreferencesPath() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.home"));
        buffer.append(System.getProperty("file.separator"));
        if (!SystemUtil.isWindows()) {
            buffer.append(".");
        }
        buffer.append("eventcloud");

        return buffer.toString();
    }

    /**
     * Returns the default path to the repositories.
     * 
     * @return the default path to the repositories.
     */
    public static final String getDefaultRepositoriesPath() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getPreferencesPath());
        buffer.append(System.getProperty("file.separator"));
        buffer.append("repositories");

        return buffer.toString();
    }

}
