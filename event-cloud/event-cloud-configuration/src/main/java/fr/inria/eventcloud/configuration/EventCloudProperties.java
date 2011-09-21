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
import java.util.UUID;

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

    /**
     * Specifies where the repositories that store the RDF data are created.
     */
    public static final PropertyString REPOSITORIES_PATH = new PropertyString(
            "eventcloud.repositories.path", getDefaultRepositoriesPath());

    /**
     * This property is used to have the possibility to restore a repository
     * which has been used in a previous run. When this property is set to
     * {@code true} it is assumed that the network contain only <b>one</b> peer.
     * If this property is enabled in conjunction with
     * {@link EventCloudProperties#REPOSITORIES_AUTO_REMOVE}, it has no effect.
     */
    public static final PropertyBoolean REPOSITORIES_RESTORE =
            new PropertyBoolean("eventcloud.repositories.restore", false);

    public static final PropertyString REPOSITORIES_RESTORE_PATH =
            new PropertyString("eventcloud.repositories.restore.path");

    /**
     * Defines whether the repositories that are instantiated must be removed
     * automatically when they are closed. This value is set by default to
     * {@code false} but setting it to {@code true} is really useful for testing
     * purposes.
     */
    public static final PropertyBoolean REPOSITORIES_AUTO_REMOVE =
            new PropertyBoolean("eventcloud.repositories.autoremove", false);

    /**
     * Defines whether the Event-Cloud has to compress the data that are
     * transfered between the peers and the users when it is possible.
     */
    public static final PropertyBoolean COMPRESSION = new PropertyBoolean(
            "eventcloud.compression", false);

    /**
     * Defines the prefix that is used for transforming an event cloud
     * identifier to an URL.
     */
    public static final PropertyString EVENT_CLOUD_ID_PREFIX =
            new PropertyString(
                    "eventcloud.id.prefix", "http://streams.play-project.eu/");

    /**
     * Defines the namespace which is used to prefix some of the URI declared
     * into the project.
     */
    public static final PropertyString EVENT_CLOUD_NS = new PropertyString(
            "eventcloud.namespace", "http://www.play-project.eu/event-cloud/");

    public static final PropertyString SEMANTIC_PEER_ADL = new PropertyString(
            "semantic.peer.adl", "fr.inria.eventcloud.overlay.SemanticPeer");

    public static final PropertyString SEMANTIC_TRACKER_ADL =
            new PropertyString(
                    "semantic.tracker.adl",
                    "fr.inria.eventcloud.tracker.SemanticTracker");

    public static final PropertyString PUBLISH_PROXY_ADL = new PropertyString(
            "pub.proxy.adl", "fr.inria.eventcloud.proxies.PublishProxy");

    public static final PropertyString PUBLISH_PROXY_SERVICES_ITF =
            new PropertyString("publish.services.itf", "publish-services");

    public static final PropertyString PUBLISH_PROXY_WEBSERVICES_ITF =
            new PropertyString("publish.webservices.itf", "publish-webservices");

    public static final PropertyString SUBSCRIBE_PROXY_ADL =
            new PropertyString(
                    "sub.proxy.adl",
                    "fr.inria.eventcloud.proxies.SubscribeProxy");

    public static final PropertyString SUBSCRIBE_PROXY_SERVICES_ITF =
            new PropertyString("subscribe.services.itf", "subscribe-services");

    public static final PropertyString SUBSCRIBE_PROXY_WEBSERVICES_ITF =
            new PropertyString(
                    "subscribe.webservices.itf", "subscribe-webservices");

    public static final PropertyString PUTGET_PROXY_ADL = new PropertyString(
            "putget.proxy.adl", "fr.inria.eventcloud.proxies.PutGetProxy");

    public static final PropertyString PUTGET_PROXY_SERVICES_ITF =
            new PropertyString("putget.services.itf", "putget-services");

    public static final PropertyString PUTGET_PROXY_WEBSERVICES_ITF =
            new PropertyString("putget.webservices.itf", "putget-webservices");

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

    private EventCloudProperties() {

    }

    public static final File getRepositoryPath() {
        // a repository has to be restored
        if (!REPOSITORIES_AUTO_REMOVE.getValue()
                && EventCloudProperties.REPOSITORIES_RESTORE.getValue()) {
            if (EventCloudProperties.REPOSITORIES_RESTORE_PATH.getValue() != null
                    && !EventCloudProperties.REPOSITORIES_RESTORE_PATH.getValue()
                            .isEmpty()) {
                return new File(
                        EventCloudProperties.REPOSITORIES_RESTORE_PATH.getValue());
            } else {
                File repositoryPath =
                        new File(
                                REPOSITORIES_PATH.getValue(), UUID.randomUUID()
                                        .toString());
                ConfigurationParser.updatePreferencesWithRestorePath(repositoryPath.getPath());
                return repositoryPath;
            }
        } else {
            // a new repository has to be created
            return new File(REPOSITORIES_PATH.getValue(), UUID.randomUUID()
                    .toString());
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
