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
package fr.inria.eventcloud.benchmarks.load_balancing.proxies;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.Body;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.proxies.PublishProxyImpl;

/**
 * Custom publish proxy for benchmark purposes.
 * 
 * @author lpellegr
 */
public class CustomPublishProxyImpl extends PublishProxyImpl implements
        CustomPublishProxy {

    public static final String PUBLISH_PROXY_ADL =
            "fr.inria.eventcloud.benchmarks.load_balancing.proxies.CustomPublishProxy";

    private List<Event> events;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean assignEvents(Event[] events) {
        boolean result = true;

        for (Event event : events) {
            result &= this.events.add(event);
        }

        return result;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void publish() {
        if (!this.events.isEmpty()) {
            if (this.events.get(0) instanceof CompoundEvent) {
                for (int i = 0; i < this.events.size(); i++) {
                    CompoundEvent ce = (CompoundEvent) this.events.get(i);

                    super.publish(ce);
                }
            } else {
                for (int i = 0; i < this.events.size(); i++) {
                    Quadruple q = (Quadruple) this.events.get(i);

                    super.publish(q);
                }
            }
        }
    }

    @Override
    public boolean clear() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initComponentActivity(body);

        this.events = new ArrayList<Event>();
    }

}
