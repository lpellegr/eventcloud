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
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.multiactivity.MultiActiveService;

import com.google.common.collect.ImmutableSet;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * EventCloudsRegistryImpl is a concrete implementation of
 * {@link EventCloudsRegistry}. This class has to be instantiated as a
 * ProActive/GCM component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@DefineGroups({
        @Group(name = "parallel", selfCompatible = true),
        @Group(name = "deployers", selfCompatible = false)})
@DefineRules({@Compatible(value = {"deployers", "parallel"})})
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
     * Empty constructor required by ProActive.
     */
    public EventCloudsRegistryImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.configurationProperty = "eventcloud.configuration";
        this.propertiesClass = EventCloudProperties.class;
        super.initComponentActivity(body);

        this.eventCloudDeployers =
                new HashMap<EventCloudId, EventCloudDeployer>();
        this.registryUrl = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        new MultiActiveService(body).multiActiveServing(
                EventCloudProperties.MAO_SOFT_LIMIT_EVENTCLOUDS_REGISTRY.getValue(),
                false, false);
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
    @MemberOf("deployers")
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
    @MemberOf("deployers")
    public Set<EventCloudId> listEventClouds() {
        // returns an immutable copy because this.eventCloudDeployers.keySet()
        // sends back a non-serializable set
        return ImmutableSet.copyOf(this.eventCloudDeployers.keySet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public boolean contains(EventCloudId id) {
        return this.eventCloudDeployers.containsKey(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public EventCloudDeployer find(EventCloudId id) {
        return this.eventCloudDeployers.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public List<Tracker> findTrackers(EventCloudId id) {
        if (this.contains(id)) {
            return this.eventCloudDeployers.get(id).getTrackers();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public void registerProxy(EventCloudId id, PublishProxy proxy) {
        this.checkEventCloudIdAndRetrieveNetworkDeployer(id).registerProxy(
                proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public void registerProxy(EventCloudId id, PutGetProxy proxy) {
        this.checkEventCloudIdAndRetrieveNetworkDeployer(id).registerProxy(
                proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public void registerProxy(EventCloudId id, SubscribeProxy proxy) {
        this.checkEventCloudIdAndRetrieveNetworkDeployer(id).registerProxy(
                proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public List<PublishProxy> getPublishProxies(EventCloudId id) {
        this.checkEventCloudId(id);

        return this.eventCloudDeployers.get(id).getPublishProxies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public List<PutGetProxy> getPutGetProxies(EventCloudId id) {
        this.checkEventCloudId(id);

        return this.eventCloudDeployers.get(id).getPutGetProxies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public List<SubscribeProxy> getSubscribeProxies(EventCloudId id) {
        this.checkEventCloudId(id);

        return this.eventCloudDeployers.get(id).getSubscribeProxies();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public boolean unregisterProxy(EventCloudId id, PublishProxy proxy) {
        return this.checkEventCloudIdAndRetrieveNetworkDeployer(id)
                .unregisterProxy(proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public boolean unregisterProxy(EventCloudId id, PutGetProxy proxy) {
        return this.checkEventCloudIdAndRetrieveNetworkDeployer(id)
                .unregisterProxy(proxy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public boolean unregisterProxy(EventCloudId id, SubscribeProxy proxy) {
        return this.checkEventCloudIdAndRetrieveNetworkDeployer(id)
                .unregisterProxy(proxy);
    }

    @MemberOf("deployers")
    private void checkEventCloudId(EventCloudId id) {
        if (!this.contains(id)) {
            throw new IllegalArgumentException(
                    "EventCloud identifier not managed: " + id);
        }
    }

    @MemberOf("deployers")
    private EventCloudDeployer checkEventCloudIdAndRetrieveNetworkDeployer(EventCloudId id) {
        this.checkEventCloudId(id);

        NetworkDeployer networkDeployer = this.eventCloudDeployers.get(id);

        if (!(networkDeployer instanceof EventCloudDeployer)) {
            throw new IllegalArgumentException(
                    "Network deployer associated to EventCloud identifier '"
                            + id + "' is not an instance of EventCloudDeployer");
        }

        return (EventCloudDeployer) networkDeployer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("deployers")
    public boolean undeploy(EventCloudId id) {
        EventCloudDeployer deployer = this.eventCloudDeployers.remove(id);

        if (deployer != null) {
            deployer.undeploy();
            return true;
        }

        return false;
    }

    /**
     * Lookups an EventClouds registry component on the specified
     * {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the EventClouds registry component.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         EventClouds registry component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     * 
     * @deprecated This method will be removed for the next release. Please use
     *             {@link EventCloudsRegistryFactory#lookupEventCloudsRegistry(String)}
     *             instead.
     */
    @Deprecated
    @MemberOf("parallel")
    public static EventCloudsRegistry lookup(String componentUri)
            throws IOException {
        return EventCloudsRegistryFactory.lookupEventCloudsRegistry(componentUri);
    }

}
