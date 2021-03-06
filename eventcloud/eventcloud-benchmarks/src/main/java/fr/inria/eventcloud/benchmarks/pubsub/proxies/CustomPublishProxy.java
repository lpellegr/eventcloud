/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.benchmarks.pubsub.proxies;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.proxies.PublishProxy;

/**
 * Custom publish proxy interface for benchmark purposes.
 * 
 * @author lpellegr
 */
public interface CustomPublishProxy extends PublishProxy {

    boolean assignEvents(Event[] events);

    void publish();

    boolean init(String collectorURL, int waitPeriodBetweenPublications);

    boolean clear();

}
