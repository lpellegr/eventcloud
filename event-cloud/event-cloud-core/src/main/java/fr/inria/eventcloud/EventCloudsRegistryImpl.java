/**
 * Copyright (c) 2011-2012 INRIA.
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
package fr.inria.eventcloud;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import com.google.common.collect.ImmutableSet;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.proxies.Proxy;

/**
 * EventCloudsRegistryImpl is a concrete implementation of
 * {@link EventCloudsRegistry}. This class has to be instantiated as a
 * ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class EventCloudsRegistryImpl extends AbstractComponent implements
        EventCloudsRegistry {

    /**
     * ADL name of the EventClouds registry component.
     */
    public static final String EVENTCLOUDS_REGISTRY_ADL =
            "fr.inria.eventcloud.EventCloudsRegistry";

    /**
     * Functional interface name of the EventClouds registry component.
     */
    public static final String EVENTCLOUDS_REGISTRY_SERVICES_ITF =
            "eventclouds-registry-services";

    /**
     * GCM Virtual Node name of the EventClouds registry component.
     */
    public static final String REGISTRY_VN = "RegistryVN";

    private Map<EventCloudId, EventCloudDeployer> eventCloudDeployers;

    private String registryUrl;

    /**
     * No-arg constructor for ProActive.
     */
    public EventCloudsRegistryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.p2pConfigurationProperty = "eventcloud.configuration";
        super.initComponentActivity(body);

        this.eventCloudDeployers =
                new HashMap<EventCloudId, EventCloudDeployer>();
        this.registryUrl = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String register(String bindingName) throws ProActiveException {
        this.registryUrl =
                Fractive.registerByName(
                        Fractive.getComponentRepresentativeOnThis(),
                        bindingName);

        return this.registryUrl;
    }

    @Override
    public void unregister() throws IOException {
        if (this.registryUrl != null) {
            Fractive.unregister(this.registryUrl);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean register(EventCloudDeployer deployer) {
        if (this.eventCloudDeployers.containsKey(deployer.getEventCloudDescription()
                .getId())) {
            return false;
        } else {
            return this.eventCloudDeployers.put(
                    deployer.getEventCloudDescription().getId(), deployer) == null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<EventCloudId> listEventClouds() {
        // returns an immutable copy because this.eventCloudDeployers.keySet()
        // sends back a non-serializable set
        return ImmutableSet.copyOf(this.eventCloudDeployers.keySet());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(EventCloudId id) {
        return this.eventCloudDeployers.containsKey(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventCloudDeployer find(EventCloudId id) {
        return this.eventCloudDeployers.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Tracker> findTrackers(EventCloudId id) {
        if (this.contains(id)) {
            return this.eventCloudDeployers.get(id).getTrackers();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerProxy(EventCloudId id, Proxy proxy) {
        if (this.contains(id)) {
            NetworkDeployer networkDeployer = this.eventCloudDeployers.get(id);

            if (networkDeployer instanceof EventCloudDeployer) {
                ((EventCloudDeployer) networkDeployer).registerProxy(proxy);
            } else {
                throw new IllegalArgumentException(
                        "Network deployer associated to this Event Cloud ID is not an instance of EventCloudDeployer");
            }
        } else {
            throw new IllegalArgumentException("Unknown Event Cloud ID");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unregisterProxy(EventCloudId id, Proxy proxy) {
        if (this.contains(id)) {
            NetworkDeployer networkDeployer = this.eventCloudDeployers.get(id);

            if (networkDeployer instanceof EventCloudDeployer) {
                return ((EventCloudDeployer) networkDeployer).unregisterProxy(proxy);
            } else {
                throw new IllegalArgumentException(
                        "Network deployer associated to this Event Cloud ID is not an instance of EventCloudDeployer");
            }
        } else {
            throw new IllegalArgumentException("Unknown Event Cloud ID");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean undeploy(EventCloudId id) {
        EventCloudDeployer deployer = this.eventCloudDeployers.remove(id);

        if (deployer != null) {
            deployer.undeploy();
            return true;
        }

        return false;
    }

    /**
     * Lookups a ProActive stub representation for the specified
     * {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the component.
     * 
     * @return a ProActive stub representation of an EventCloudsRegistry .
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     */
    public static EventCloudsRegistry lookup(String componentUri)
            throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri, EVENTCLOUDS_REGISTRY_SERVICES_ITF,
                EventCloudsRegistry.class);
    }

}
