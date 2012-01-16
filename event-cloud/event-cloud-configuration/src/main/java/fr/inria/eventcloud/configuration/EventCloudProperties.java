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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.configuration.ConfigurationParser;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyBoolean;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyInteger;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;

/**
 * Contains default values for Event-Cloud properties.
 * <p>
 * The first time a property is retrieved, before it, the class will try to load
 * properties from a preferences file. The location of this file is by default
 * set to {@code %HOMEPATH%\.eventcloud\preferences} on Windows and to
 * {@code $HOME/.eventcloud/preferences} on Unix systems. You can also override
 * the default location by using the java property
 * {@code -Deventcloud.configuration=/path/to/configurationFile.properties}.
 * 
 * @author lpellegr
 */
public class EventCloudProperties {

    /**
     * Defines whether the eventcloud has to compress the data which are
     * transfered between the peers and the users when it is possible. When this
     * property is enabled, all the entities communicating with the eventcloud
     * must also enable the compression.
     */
    public static final PropertyBoolean COMPRESSION = new PropertyBoolean(
            "compression", false);

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
            "eventcloud.namespace", "urn:eventcloud:");

    /**
     * Specifies where the repositories that store the RDF data are created.
     */
    public static final PropertyString REPOSITORIES_PATH = new PropertyString(
            "repositories.path", getDefaultRepositoriesPath());

    /**
     * Defines whether the colander (c.f. SparqlColander) uses an in-memory or a
     * persistent datastore for filtering results.
     */
    public static final PropertyBoolean COLANDER_IN_MEMORY =
            new PropertyBoolean("colander.inmemory", false);

    /**
     * Defines where the repositories associated to the colanders (c.f.
     * SparqlColander) are created. This property is used in conjunction to
     * {@link #COLANDER_IN_MEMORY} when it is set to {@code true}.
     */
    public static final PropertyString COLANDER_REPOSITORIES_PATH =
            new PropertyString(
                    "colander.repositories.path",
                    System.getProperty("java.io.tmpdir")
                            + "/eventcloud/colanders/");

    /**
     * This property is used to have the possibility to restore a repository
     * which has been used in a previous run. When this property is set to
     * {@code true} it is assumed that the network contain only <b>one</b> peer.
     * If this property is enabled in conjunction with
     * {@link EventCloudProperties#REPOSITORIES_AUTO_REMOVE}, it has no effect.
     */
    public static final PropertyBoolean REPOSITORIES_RESTORE =
            new PropertyBoolean("repositories.restore", false);

    /**
     * Defines the location of the repositories which are created by each peer.
     */
    public static final PropertyString REPOSITORIES_RESTORE_PATH =
            new PropertyString("repositories.restore.path", null);

    /**
     * Defines whether the repositories that are instantiated must be removed
     * automatically when they are closed. This value is set by default to
     * {@code false} but setting it to {@code true} is really useful for testing
     * purposes.
     */
    public static final PropertyBoolean REPOSITORIES_AUTO_REMOVE =
            new PropertyBoolean("eventcloud.repositories.autoremove", false);

    /**
     * Specifies the maximum number of peer stubs to put in cache. This property
     * does not guarantee that the specified number of entries will be put in
     * cache but only at most the given number (e.g. if there is not enough
     * memory, entries are evicted).
     */
    public static final PropertyInteger PEER_STUBS_CACHE_MAXIMUM_SIZE =
            new PropertyInteger("eventcloud.peer.stubs.cache.maximum.size", 100);

    /**
     * Defines the time to wait in milliseconds before to re-execute the query
     * for retrieving the quadruples that belongs to the specified event id when
     * the mode to reconstruct compound events is based on polling.
     */
    public static final PropertyInteger RECONSTRUCTION_RETRY_THRESHOLD =
            new PropertyInteger(
                    "eventcloud.reconstruction.retry.threshold", 300);

    /**
     * Specifies the maximum number of subscriptions to put in cache. This
     * property does not guarantee that the specified number of entries will be
     * put in cache but only at most the given number (e.g. if there is not
     * enough memory, entries are evicted).
     */
    public static final PropertyInteger SUBSCRIPTIONS_CACHE_MAXIMUM_SIZE =
            new PropertyInteger(
                    "eventcloud.subscriptions.cache.maximum.size", 1000);

    /**
     * Specifies the maximum number of subscribe proxies to put in cache. This
     * property does not guarantee that the specified number of entries will be
     * put in cache but only at most the given number (e.g. if there is not
     * enough memory, entries are evicted).
     */
    public static final PropertyInteger SUBSCRIBE_PROXIES_CACHE_MAXIMUM_SIZE =
            new PropertyInteger(
                    "eventcloud.subscribe.proxies.cache.maximum.size", 1000);

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

    public static final PropertyString SUBSCRIBE_PROXY_ADL =
            new PropertyString(
                    "sub.proxy.adl",
                    "fr.inria.eventcloud.proxies.SubscribeProxy");

    public static final PropertyString SUBSCRIBE_PROXY_SERVICES_ITF =
            new PropertyString("subscribe.services.itf", "subscribe-services");

    public static final PropertyString PUTGET_PROXY_ADL = new PropertyString(
            "putget.proxy.adl", "fr.inria.eventcloud.proxies.PutGetProxy");

    public static final PropertyString PUTGET_PROXY_SERVICES_ITF =
            new PropertyString("putget.services.itf", "putget-services");

    private static final File configurationFileLoaded;

    static {
        configurationFileLoaded =
                ConfigurationParser.load(
                        EventCloudProperties.class, "eventcloud.configuration",
                        System.getProperty("user.home")
                                + "/.eventcloud/eventcloud.properties");

        // forces the number of dimensions in a CAN network to 4
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
        // allows ISO 8859-1 characters
        P2PStructuredProperties.CAN_LOWER_BOUND.setValue("\u0000");
        P2PStructuredProperties.CAN_UPPER_BOUND.setValue("\u0256");
    }

    public static final File getRepositoryPath() {
        // a repository has to be restored
        if (!REPOSITORIES_AUTO_REMOVE.getValue()
                && EventCloudProperties.REPOSITORIES_RESTORE.getValue()) {
            File repositoryPath;

            if (EventCloudProperties.REPOSITORIES_RESTORE_PATH.getValue() != null
                    && !EventCloudProperties.REPOSITORIES_RESTORE_PATH.getValue()
                            .isEmpty()
                    && (repositoryPath =
                            new File(
                                    EventCloudProperties.REPOSITORIES_RESTORE_PATH.getValue())).exists()) {
                return repositoryPath;
            } else {
                repositoryPath =
                        new File(
                                REPOSITORIES_PATH.getValue(), UUID.randomUUID()
                                        .toString());

                Properties props = new Properties();
                FileInputStream fis = null;
                FileOutputStream fos = null;

                try {
                    fis = new FileInputStream(configurationFileLoaded);
                    fos = new FileOutputStream(configurationFileLoaded);

                    props.load(fis);
                    props.setProperty(
                            REPOSITORIES_RESTORE_PATH.getName(),
                            repositoryPath.toString());
                    props.store(fos, "");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

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
