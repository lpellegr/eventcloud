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
