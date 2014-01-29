/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.configuration;

import java.io.File;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.configuration.ConfigurationParser;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyBoolean;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyClass;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyDouble;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyInteger;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;
import org.objectweb.proactive.extensions.p2p.structured.configuration.Validator;

/**
 * Contains default values for EventCloud properties.
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
     * Defines approximatively the average number of quadruples contained for
     * each compound event handled by the system. This value is used to
     * approximate the expected number of entries in the various map that are
     * initialized.
     */
    public static final PropertyInteger AVERAGE_NB_QUADRUPLES_PER_COMPOUND_EVENT =
            new PropertyInteger("eventcloud.compound.events.average.size", 30);

    /**
     * Defines whether the EventCloud has to compress the data which are
     * transfered between the peers and the users when it is possible. When this
     * property is enabled, all the entities communicating with the EventCloud
     * must also enable the compression.
     */
    public static final PropertyBoolean COMPRESSION = new PropertyBoolean(
            "eventcloud.compression", false);

    /**
     * Defines the prefix that is used for transforming an EventCloud identifier
     * to an URL.
     */
    public static final PropertyString EVENTCLOUD_ID_PREFIX =
            new PropertyString(
                    "eventcloud.id.prefix",
                    "http://events.event-processing.org/ids/");

    /**
     * Specifies where the repositories that store the RDF data are created.
     */
    public static final PropertyString REPOSITORIES_PATH = new PropertyString(
            "eventcloud.repositories.path", getDefaultRepositoriesPath());

    /**
     * Defines whether the colander (c.f. SparqlColander) uses an in-memory or a
     * persistent datastore for filtering results or not.
     */
    public static final PropertyBoolean COLANDER_IN_MEMORY =
            new PropertyBoolean("eventcloud.colander.inmemory", false);

    /**
     * Defines where the repositories associated to the colanders (c.f.
     * SparqlColander) are created. This property has to be used in conjunction
     * with {@link #COLANDER_IN_MEMORY} when it is set to {@code true}.
     */
    public static final PropertyString COLANDER_REPOSITORIES_PATH =
            new PropertyString(
                    "eventcloud.colander.repositories.path",
                    getDefaultTemporaryPath() + "colanders"
                            + File.separatorChar);

    /**
     * Defines the namespace associated to extra function imported and used
     * during the evaluation of SPARQL requests.
     */
    public static final PropertyString FILTER_FUNCTIONS_NS =
            new PropertyString(
                    "eventcloud.filter.functions.ns",
                    "http://eventcloud.inria.fr/function#");

    // /**
    // * Defines the number of quadruples to put in cache before to commit them
    // to
    // * disk and trigger the associated action.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_QUADRUPLE_DELAYER_COMMIT_SIZE =
    // new PropertyInteger(
    // "eventcloud.pubsub.quadruple.delayer.commit.size", 120);
    //
    // /**
    // * Defines the number of subscriptions to put in cache before to commit
    // them
    // * to disk and trigger the associated action.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_SUBSCRIPTION_DELAYER_COMMIT_SIZE =
    // new PropertyInteger(
    // "eventcloud.pubsub.subscription.delayer.commit.size", 120);
    //
    // /**
    // * Defines the number of compound events to put in cache before to commit
    // * them to disk and trigger the associated action.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_COMPOUND_EVENT_DELAYER_COMMIT_SIZE =
    // new PropertyInteger(
    // "eventcloud.pubsub.compound.event.delayer.commit.size", 120);

    /**
     * Defines the number of elements (quadruples, subscriptions or compound
     * events) to put in cache before to commit them to disk and trigger the
     * associated action.
     */
    public static final PropertyInteger PUBLISH_SUBSCRIBE_DELAYER_COMMIT_SIZE =
            new PropertyInteger("eventcloud.pubsub.delayer.commit.size", 120);

    // /**
    // * Defines the maximum time to wait (in milliseconds) before to commit
    // * buffered quadruples when a commit thread is running with the
    // * publish/subscribe operations delayer.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_QUADRUPLE_DELAYER_COMMIT_INTERVAL =
    // new PropertyInteger(
    // "eventcloud.pubsub.quadruple.delayer.commit.interval", 500);
    //
    // /**
    // * Defines the maximum time to wait (in milliseconds) before to commit
    // * buffered subscriptions when a commit thread is running with the
    // * publish/subscribe operations delayer.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_SUBSCRIPTION_DELAYER_COMMIT_INTERVAL =
    // new PropertyInteger(
    // "eventcloud.pubsub.subscription.delayer.commit.interval",
    // 500);
    //
    // /**
    // * Defines the maximum time to wait (in milliseconds) before to commit
    // * buffered compound events when a commit thread is running with the
    // * publish/subscribe operations delayer.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_COMPOUND_EVENT_DELAYER_COMMIT_INTERVAL =
    // new PropertyInteger(
    // "eventcloud.pubsub.compound.event.delayer.commit.interval",
    // 500);

    /**
     * Defines the maximum time to wait (in milliseconds) before to commit
     * buffered elements when a commit thread is running with the
     * publish/subscribe operations delayer.
     */
    public static final PropertyInteger PUBLISH_SUBSCRIBE_DELAYER_COMMIT_INTERVAL =
            new PropertyInteger(
                    "eventcloud.pubsub.delayer.commit.interval", 500);

    // /**
    // * Defines the size of the thread pool used by the delayer action once
    // * quadruples have been flushed to the disk.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_QUADRUPLE_DELAYER_THREAD_POOL_SIZE =
    // new PropertyInteger(
    // "eventcloud.pubsub.quadruple.delayer.thread.pool.size", 0);
    //
    // /**
    // * Defines the size of the thread pool used by the delayer action once
    // * subscriptions have been flushed to the disk.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_SUBSCRIPTION_DELAYER_THREAD_POOL_SIZE =
    // new PropertyInteger(
    // "eventcloud.pubsub.subscription.delayer.thread.pool.size",
    // 0);
    //
    // /**
    // * Defines the size of the thread pool used by the delayer action once
    // * compound events have been flushed to the disk.
    // */
    // public static final PropertyInteger
    // PUBLISH_SUBSCRIBE_COMPOUND_EVENT_DELAYER_THREAD_POOL_SIZE =
    // new PropertyInteger(
    // "eventcloud.pubsub.compound.events.delayer.thread.pool.size",
    // Runtime.getRuntime().availableProcessors());

    /**
     * Defines the size of the thread pool used by the delayer action once
     * compound events have been flushed to the disk. Using 0 as value forces
     * the action to be dispatched in the current thread (i.e. no additional
     * thread is created).
     */
    public static final PropertyInteger PUBLISH_SUBSCRIBE_DELAYER_THREAD_POOL_SIZE =
            new PropertyInteger("eventcloud.pubsub.delayer.thread.pool.size", 0);

    /**
     * Constant used to identify the SBCE publish/subscribe algorithm in version
     * 1.
     */
    public static final String PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_1 = "SBCE1";

    /**
     * Constant used to identify the SBCE publish/subscribe algorithm in version
     * 2.
     */
    public static final String PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_2 = "SBCE2";

    /**
     * Constant used to identify the SBCE publish/subscribe algorithm in version
     * 3.
     */
    public static final String PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_3 = "SBCE3";

    /**
     * Defines the publish/subscribe algorithm and version used by the system.
     * By default SBCE3 is enabled.
     */
    public static final PropertyString PUBLISH_SUBSCRIBE_ALGORITHM =
            new PropertyString(
                    "eventcloud.publish.subscribe.algorithm",
                    PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_3,
                    new PubSubAlgorithmPropertyValidator());

    /**
     * Defines at which frequence the garbage collection timeout for ephemeral
     * subscriptions must be triggered (in ms). The default value is set to 15
     * minutes.
     */
    public static final PropertyInteger EPHEMERAL_SUBSCRIPTIONS_GC_TIMEOUT =
            new PropertyInteger(
                    "eventcloud.ephemeral.subscriptions.gc.timeout", 900000);

    /**
     * Defines the expiration time of an ephemeral subscription (in ms). The
     * default value is set to 1 hour.
     */
    public static final PropertyInteger EPHEMERAL_SUBSCRIPTION_EXPIRATION_TIME =
            new PropertyInteger(
                    "eventcloud.ephemeral.subscription.expiration.time",
                    3600000);

    /**
     * Defines whether dynamic load balancing is enabled or not. Setting dynamic
     * load balancing to {@code true} enables
     * {@link EventCloudProperties#STATIC_LOAD_BALANCING}.
     */
    public static final PropertyBoolean DYNAMIC_LOAD_BALANCING =
            new PropertyBoolean("eventcloud.load.balancing.dynamic", false);

    /**
     * Defines whether static load balancing is enabled or not. Settings static
     * load balancing to {@code true} enables
     * {@link EventCloudProperties#RECORD_STATS_MISC_DATASTORE}. To know whether
     * static load balancing is enabled or you should use
     * {@link #isStaticLoadBalancingEnabled()} and no the value returned for
     * this property since static load balancing may be enabled due to dynamic
     * load balancing.
     */
    public static final PropertyBoolean STATIC_LOAD_BALANCING =
            new PropertyBoolean("eventcloud.load.balancing.static", false);

    /**
     * Defines the load balancing function to apply on coordinate elements of
     * resources in order to improve distribution on peers. It may consists in
     * removing prefixes ({@code prefix-removal}), reversing values (
     * {@code reverse}) or returning the input value ({@code identity}). Default
     * one is {@code identity}.
     */
    public static final PropertyString LOAD_BALANCING_DOPING_FUNCTION =
            new PropertyString(
                    "eventcloud.load.balancing.doping.function",
                    "prefix-removal");

    /**
     * Defines which gossip strategy is applied for reporting load. Any class
     * implementing GossipStrategy may be used.
     */
    public static final PropertyClass LOAD_BALANCING_GOSSIP_STRATEGY =
            new PropertyClass(
                    "eventcloud.load.balancing.gossip.strategy",
                    "fr.inria.eventcloud.load_balancing.gossip.ImmediateNeighborsStrategy");

    /**
     * Defines load balancing parameter value for k1.
     */
    public static final PropertyDouble LOAD_BALANCING_PARAMETER_K1 =
            new PropertyDouble("eventcloud.load.balancing.parameter.k1", 1.0);

    /**
     * Defines load balancing parameter value for k2.
     */
    public static final PropertyDouble LOAD_BALANCING_PARAMETER_K2 =
            new PropertyDouble("eventcloud.load.balancing.parameter.k2", 1.0);

    /**
     * Defines the period in milliseconds to wait between each load balancing
     * iteration.
     */
    public static final PropertyInteger LOAD_BALANCING_PERIOD =
            new PropertyInteger("eventcloud.load.balancing.period", 5000);

    /**
     * Defines the period in milliseconds to wait between each gossip iteration
     * when relative load balancing is used.
     */
    public static final PropertyInteger LOAD_BALANCING_GOSSIP_PERIOD =
            new PropertyInteger("eventcloud.load.balancing.gossip.period", 500);

    /**
     * Defines emergency threshold value for quadruple criterion.
     */
    public static final PropertyDouble LOAD_BALANCING_EMERGENCY_THRESHOLD_QUADRUPLE_CRITERION =
            new PropertyDouble(
                    "eventcloud.load.balancing.emergency.threshold.quadruple.criterion",
                    10000.0);

    /**
     * This property is used to have the possibility to restore a repository
     * which has been used in a previous run. When this property is set to
     * {@code true} it is assumed that the network contain only <b>one</b> peer.
     * If this property is enabled in conjunction with
     * {@link EventCloudProperties#REPOSITORIES_AUTO_REMOVE}, it has no effect.
     */
    public static final PropertyBoolean REPOSITORIES_RESTORE =
            new PropertyBoolean("eventcloud.repositories.restore", false);

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
     * Defines the time to wait in milliseconds before to execute again the
     * query used to reconstruct a compound event based on polling. This
     * property is used only when {@link #PUBLISH_SUBSCRIBE_ALGORITHM} is set to
     * SBCE1.
     */
    public static final PropertyInteger RECONSTRUCTION_RETRY_THRESHOLD =
            new PropertyInteger(
                    "eventcloud.reconstruction.retry.threshold", 330);

    /**
     * Specifies the maximum number of subscriptions to put in cache. This
     * property does not guarantee that the specified number of entries will be
     * put in cache but only at most the given number (e.g. if there is not
     * enough memory, entries are evicted).
     */
    public static final PropertyInteger SUBSCRIPTIONS_CACHE_MAXIMUM_SIZE =
            new PropertyInteger(
                    "eventcloud.subscriptions.cache.maximum.size", 10000);

    /**
     * Specifies the maximum number of subscribe proxies to put in cache. This
     * property does not guarantee that the specified number of entries will be
     * put in cache but only at most the given number (e.g. if there is not
     * enough memory, entries are evicted).
     */
    public static final PropertyInteger SUBSCRIBE_PROXIES_CACHE_MAXIMUM_SIZE =
            new PropertyInteger(
                    "eventcloud.subscribe.proxies.cache.maximum.size", 1000);

    /**
     * Specifies the URL of a socialfilter to filter notifications before
     * delivery. A {@code null} value indicates that no socialfilter has to be
     * used.
     */
    public static final PropertyString SOCIAL_FILTER_URL = new PropertyString(
            "eventcloud.socialfilter.url", null);

    /**
     * Specifies the threshold for the relationship strengths provided by the
     * social filter.
     */
    public static final PropertyDouble SOCIAL_FILTER_THRESHOLD =
            new PropertyDouble("eventcloud.socialfilter.threshold", 0.5);

    public static final PropertyInteger STATS_RECORDER_NB_BACKGROUND_THREADS =
            new PropertyInteger(
                    "eventcloud.stats.recorder.nb.background.threads", 2);

    /**
     * Defines whether statistics recording must be enabled or not for the misc
     * datastore. When it is enabled, some statistics like the number of
     * quadruples added, etc. are recorded during each data insertion. To know
     * whether stats recording for the misc datastore is enabled, you should use
     * {@link #isRecordStatsMiscDatastoreEnabled()} and no the value returned
     * for this property since stats recording may be enabled due to dynamic or
     * static load balancing.
     */
    public static final PropertyBoolean RECORD_STATS_MISC_DATASTORE =
            new PropertyBoolean("eventcloud.record.stats.misc.datastore", false);

    /**
     * Defines whether statistics recording must be enabled or not for the peer
     * stubs put in cache.
     */
    public static final PropertyBoolean RECORD_STATS_PEER_STUBS_CACHE =
            new PropertyBoolean(
                    "eventcloud.record.stats.peer.stubs.cache", false);

    /**
     * Defines whether statistics recording must be enabled or not for the
     * subscriptions put in cache.
     */
    public static final PropertyBoolean RECORD_STATS_SUBSCRIPTIONS_CACHE =
            new PropertyBoolean(
                    "eventcloud.record.stats.subscriptions.cache", false);

    /**
     * Defines whether statistics recording must be enabled or not for the
     * subscribe proxy stubs put in cache.
     */
    public static final PropertyBoolean RECORD_STATS_SUBSCRIBE_PROXIES_CACHE =
            new PropertyBoolean(
                    "eventcloud.record.stats.subscribe.proxies.cache", false);

    /**
     * Defines which type of statistics recorder to use when
     * {@link #RECORD_STATS_MISC_DATASTORE} is set to {@code true}.
     */
    public static final PropertyClass STATS_RECORDER_CLASS = new PropertyClass(
            "eventcloud.stats.recorder.class",
            "fr.inria.eventcloud.datastore.stats.CentroidStatsRecorder");

    /**
     * Defines the maximum number of entries that are kept in-memory on the
     * subscriber side before to be moved on disk. Default value is set to
     * 200000 which requires about 100MB of heap.
     */
    public static final PropertyInteger SUBSCRIBER_CACHE_MAX_ENTRIES =
            new PropertyInteger(
                    "eventcloud.subscriber.cache.max.entries", 200000);

    /**
     * Defines the cache engine used by subscribers to prevent duplicates with
     * some publish/subscribe algorithms. The values allowed are {@code ehcache}
     * and {@code infinispan}. The last is the default one.
     */
    public static final PropertyString SUBSCRIBER_CACHE_ENGINE =
            new PropertyString(
                    "eventcloud.subscriber.cache.engine", "infinispan");

    /**
     * Defines whether JMX statistics must be exposed or not.
     */
    public static final PropertyBoolean EXPOSE_JMX_STATISTICS =
            new PropertyBoolean("eventcloud.expose.jmx.statistics", false);

    /**
     * Defines the limit used by each EventClouds registry that runs with
     * multi-active objects.
     */
    public static final PropertyInteger MAO_LIMIT_EVENTCLOUDS_REGISTRY =
            new PropertyInteger(
                    "eventcloud.mao.limit.eventclouds.registry",
                    Runtime.getRuntime().availableProcessors());

    /**
     * Defines the limit used by each publish proxy that runs with multi-active
     * objects.
     */
    public static final PropertyInteger MAO_LIMIT_PUBLISH_PROXIES =
            new PropertyInteger(
                    "eventcloud.mao.limit.publish.proxies",
                    Runtime.getRuntime().availableProcessors());

    /**
     * Defines the limit used by each putget proxy that runs with multi-active
     * objects.
     */
    public static final PropertyInteger MAO_LIMIT_PUTGET_PROXIES =
            new PropertyInteger(
                    "eventcloud.mao.limit.putget.proxies", Runtime.getRuntime()
                            .availableProcessors());

    /**
     * Defines the limit used by each subscribe proxy that runs with
     * multi-active objects.
     */
    public static final PropertyInteger MAO_LIMIT_SUBSCRIBE_PROXIES =
            new PropertyInteger(
                    "eventcloud.mao.limit.subscribe.proxies",
                    Runtime.getRuntime().availableProcessors());

    /**
     * Specifies the number maximum of attempts to lookup a proxy that is not
     * reachable. For instance, this is useful to remove references to subscribe
     * proxies which have left without unsubscribing.
     */
    public static final PropertyInteger PROXY_MAX_LOOKUP_ATTEMPTS =
            new PropertyInteger("eventcloud.proxy.max.lookup.attempts", 3);

    private static File configurationFileLoaded;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        if (configurationFileLoaded == null) {
            configurationFileLoaded =
                    ConfigurationParser.load(
                            EventCloudProperties.class,
                            "eventcloud.configuration",
                            System.getProperty("user.home")
                                    + File.pathSeparator + ".eventcloud"
                                    + File.pathSeparator
                                    + "eventcloud.properties");

            // forces the number of dimensions in a CAN network to 4
            P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
        }
    }

    public static final File getRepositoryPath(String streamUrl) {
        // adds file separator at the end of the repositories path
        if (!REPOSITORIES_PATH.getValue().endsWith("/")) {
            REPOSITORIES_PATH.setValue(REPOSITORIES_PATH.getValue()
                    + File.separatorChar);
        }

        File repositoryPath =
                new File(REPOSITORIES_PATH.getValue() + normalize(streamUrl));

        String repositorySuffix = null;

        // a repository has to be restored
        if (REPOSITORIES_RESTORE.getValue()
                && !REPOSITORIES_AUTO_REMOVE.getValue()
                && repositoryPath.exists()) {
            File[] files = repositoryPath.listFiles();

            for (File f : files) {
                // use first repository available for restoration
                if (f.isDirectory()) {
                    repositorySuffix = f.getName();
                    break;
                }
            }

            if (repositorySuffix == null) {
                repositorySuffix = UUID.randomUUID().toString();
            }
        } else {
            // a new repository has to be created
            repositorySuffix = UUID.randomUUID().toString();
        }

        return new File(repositoryPath, repositorySuffix);
    }

    private static String normalize(String directoryName) {
        if (directoryName.startsWith("http://")) {
            directoryName = directoryName.substring(7);
        } else if (directoryName.startsWith("https://")) {
            directoryName = directoryName.substring(8);
        }

        return directoryName.replaceAll("/", "_");
    }

    public static final String getPreferencesFilePath() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getPreferencesPath());
        buffer.append(File.separatorChar);
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
        buffer.append(File.separatorChar);
        buffer.append(".eventcloud");
        buffer.append(File.separatorChar);

        return buffer.toString();
    }

    /**
     * Returns the default path to the repositories.
     * 
     * @return the default path to the repositories.
     */
    public static final String getDefaultRepositoriesPath() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(getPreferencesPath());
        buffer.append("repositories");
        buffer.append(File.separatorChar);

        return buffer.toString();
    }

    /**
     * Returns the path to the default temporary directory for files.
     * 
     * @return the path to the default temporary directory for files.
     */
    public static final String getDefaultTemporaryPath() {
        return System.getProperty("java.io.tmpdir") + File.separatorChar
                + "eventcloud-" + System.getProperty("user.name")
                + File.separatorChar;
    }

    public static final boolean isDynamicLoadBalancingEnabled() {
        return DYNAMIC_LOAD_BALANCING.getValue();
    }

    public static final boolean isStaticLoadBalancingEnabled() {
        return STATIC_LOAD_BALANCING.getValue()
                || isDynamicLoadBalancingEnabled();
    }

    public static final boolean isRecordStatsMiscDatastoreEnabled() {
        return RECORD_STATS_MISC_DATASTORE.getValue()
                || isStaticLoadBalancingEnabled();
    }

    public static final boolean isSbce1PubSubAlgorithmUsed() {
        return isPubSubAlgorithmUsedEqualsTo(PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_1);
    }

    public static final boolean isSbce2PubSubAlgorithmUsed() {
        return isPubSubAlgorithmUsedEqualsTo(PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_2);
    }

    public static final boolean isSbce3PubSubAlgorithmUsed() {
        return isPubSubAlgorithmUsedEqualsTo(PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_3);
    }

    private static final boolean isPubSubAlgorithmUsedEqualsTo(String name) {
        return PUBLISH_SUBSCRIBE_ALGORITHM.getValue().equals(name);
    }

    private static final class PubSubAlgorithmPropertyValidator extends
            Validator<String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isLegalValue(String propertyValue) {
            return propertyValue.equalsIgnoreCase(PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_1)
                    || propertyValue.equalsIgnoreCase(PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_2)
                    || propertyValue.equalsIgnoreCase(PUBLISH_SUBSCRIBE_ALGORITHM_SBCE_3);
        }
    }

}
