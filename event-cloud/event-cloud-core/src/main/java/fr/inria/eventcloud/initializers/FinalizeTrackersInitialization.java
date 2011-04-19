package fr.inria.eventcloud.initializers;

import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * Interface used to have the possibility to run a specific task just after the
 * trackers have been initialized and not the network yet.
 * 
 * @author lpelleg
 */
public interface FinalizeTrackersInitialization {

    public void run(Tracker[] trackers);

}
