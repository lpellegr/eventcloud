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
package fr.inria.eventcloud.configuration;

import java.io.File;

import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyBoolean;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains default values for Event-Cloud properties.
 * <p>
 * The first time a property is retrieved, before it, the class will try to load
 * properties from a preferences file. The location of this file is by default
 * set to {@code %HOMEPATH%\.eventcloud\preferences} on Windows and to
 * {@code $HOME/.eventcloud/preferences} on Unix systems. You can also override
 * the default location by using the java property
 * {@code eventcloud.configuration}.
 * 
 * @author lpellegr
 */
public class EventCloudProperties {

    private static final Logger log =
            LoggerFactory.getLogger(EventCloudProperties.class);

    public static final PropertyString SEMANTIC_PEER_ADL = new PropertyString(
            "semantic.peer.adl", "fr.inria.eventcloud.overlay.SemanticPeer");

    public static final PropertyString SEMANTIC_TRACKER_ADL =
            new PropertyString(
                    "semantic.tracker.adl",
                    "fr.inria.eventcloud.tracker.SemanticTracker");

    public static final PropertyString PUBSUB_PROXY_ADL = new PropertyString(
            "pubsub.proxy.adl",
            "fr.inria.eventcloud.proxies.PublishSubscribeProxy");

    public static final PropertyString PUBSUB_PROXY_SERVICES_ITF =
            new PropertyString(
                    "pubsub-proxy.services.itf", "pubsub-proxy-services");

    /**
     * Specifies where the repositories that store the RDF data are created.
     */
    public static final PropertyString REPOSITORIES_PATH = new PropertyString(
            "eventcloud.repositories.path", getDefaultRepositoriesPath());

    /**
     * Defines whether the Event-Cloud has to compress the data that are
     * transfered between the peers and the users when it is possible.
     */
    public static final PropertyBoolean COMPRESSION = new PropertyBoolean(
            "eventcloud.compression", false);

    /**
     * Defines the prefix that is used for any EventCloudId when an EventCloudId
     * is returned as an URL.
     */
    public static final PropertyString EVENT_CLOUD_ID_PREFIX =
            new PropertyString(
                    "eventcloud.id.prefix", "http://streams.play-project.eu/");

    static {
        File preferencesFile = new File(getPreferencesFilePath());
        String eventCloudConfigurationProperty =
                System.getProperty("eventcloud.configuration");
        if (eventCloudConfigurationProperty != null) {
            preferencesFile = new File(eventCloudConfigurationProperty);
        }

        if (preferencesFile.exists()) {
            log.info("Loading properties from {}", preferencesFile);
            ConfigurationParser.parse(preferencesFile.toString());
        } else {
            log.info(
                    "No Event-Cloud properties loaded because file {} does not exist",
                    preferencesFile);
        }
    }

    public static final String getPreferencesFilePath() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getPreferencesPath());
        buffer.append(File.separator);
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
        buffer.append(File.separator);
        buffer.append(".eventcloud");

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
        buffer.append(File.separator);
        buffer.append("repositories");

        return buffer.toString();
    }

}
