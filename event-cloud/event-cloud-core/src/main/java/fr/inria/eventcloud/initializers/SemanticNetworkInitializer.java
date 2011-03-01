package fr.inria.eventcloud.initializers;

import org.objectweb.proactive.core.util.ProActiveRandom;

/**
 * Defines and store many informations which are commons to all network
 * initializers.
 * 
 * @author lpellegr
 */
public abstract class SemanticNetworkInitializer<T> {

    protected boolean initialized = false;

    protected T[] trackers;

    public SemanticNetworkInitializer() {

    }
    
    public abstract void tearDownNetwork();

    public T getRandomTracker() {
        return this.trackers[ProActiveRandom.nextInt(this.trackers.length)];
    }

    public T[] getTrackers() {
        return this.trackers;
    }

}
