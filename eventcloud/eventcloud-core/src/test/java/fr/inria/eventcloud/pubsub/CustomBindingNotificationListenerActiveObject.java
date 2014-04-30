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
package fr.inria.eventcloud.pubsub;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;

public class CustomBindingNotificationListenerActiveObject extends
        BindingNotificationListener {

    private static final long serialVersionUID = 160L;

    private static final Logger log =
            LoggerFactory.getLogger(CustomBindingNotificationListenerActiveObject.class);

    private List<Binding> bindings = new ArrayList<Binding>();

    public CustomBindingNotificationListenerActiveObject() {
    }

    public List<Binding> getBindings() {
        return this.bindings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, Binding solution) {
        synchronized (this.bindings) {
            this.bindings.add(solution);
        }
        log.info("New binding received:\n" + solution);
    }

}
