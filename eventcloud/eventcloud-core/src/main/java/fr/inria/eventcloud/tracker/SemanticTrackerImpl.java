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
package fr.inria.eventcloud.tracker;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerImpl;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * A specific implementation of a tracker for a semantic content addressable
 * network. It takes into account the frequency of quadruples in order to load
 * balance the join operation.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SemanticTrackerImpl extends TrackerImpl implements SemanticTracker {

    private static final long serialVersionUID = 160L;

    /**
     * ADL name of the semantic tracker component.
     */
    public static final String SEMANTIC_TRACKER_ADL =
            "fr.inria.eventcloud.tracker.SemanticTracker";

    /**
     * Empty constructor required by ProActive.
     */
    public SemanticTrackerImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.configurationProperty = "eventcloud.configuration";
        this.propertiesClass = EventCloudProperties.class;
        super.initComponentActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticPeer getRandomSemanticPeer() {
        return (SemanticPeer) super.getRandomPeer();
    }

}
