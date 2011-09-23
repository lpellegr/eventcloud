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
