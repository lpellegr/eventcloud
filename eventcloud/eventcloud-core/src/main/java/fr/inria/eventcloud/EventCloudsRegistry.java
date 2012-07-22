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
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * An eventclouds registry is in charge of maintaining the list of eventclouds
 * which are running for an organization or a group. In addition, for each
 * eventcloud which is managed, the registry also have to store the entry points
 * associated to an eventcloud.
 * <p>
 * <strong>This first prototype is centralized and stores the information in
 * memory.</strong>
 * 
 * @author lpellegr
 */
public interface EventCloudsRegistry {

    /**
     * Registers the registry with the specified {@code bindingName}.
     * 
     * @param bindingName
     *            the binding name to use.
     * 
     * @return the URL where the object is bound.
     * 
     * @throws ProActiveException
     *             if a problem occurs while the registry is registered.
     */
    public String register(String bindingName) throws ProActiveException;

    /**
     * Unregisters the registry.
     * 
     * @throws IOException
     *             if a problem occurs while the registry is unregistered.
     */
    public void unregister() throws IOException;

    /**
     * Registers the given {@link EventCloudDeployer} into the registry.
     * 
     * @param eventCloudDeployer
     *            the eventcloud to register into the registry.
     * 
     * @return {@code true} if the registration has succeed or {@code false} if
     *         the eventcloud is already registered into the registry.
     */
    public boolean register(EventCloudDeployer eventCloudDeployer);

    /**
     * Returns a list that contains the identifier of the eventclouds which are
     * managed by the registry.
     * 
     * @return a list that contains the identifier of the eventclouds which are
     *         managed by the registry.
     */
    public Set<EventCloudId> listEventClouds();

    /**
     * Returns a boolean which indicates if the eventcloud identified by the
     * specified {@code eventcloudId} is already managed by the registry.
     * 
     * @param id
     *            the eventcloud identifier to check for.
     * 
     * @return {@code true} if the eventcloud identifier is already managed,
     *         {@code false} otherwise.
     */
    public boolean contains(EventCloudId id);

    /**
     * Returns the {@link EventCloudDescription} object associated to the
     * specified {@code id} if it is managed by the registry.
     * 
     * @param id
     *            the eventcloud identifier to look for.
     * 
     * @return the {@link EventCloudDescription} object associated to the
     *         specified {@code id} if it is managed by the registry or
     *         {@code null}.
     */
    public EventCloudDeployer find(EventCloudId id);

    /**
     * Returns the trackers associated to the specified {@link EventCloudId} if
     * it is registered in the registry or {@code null}.
     * 
     * @param id
     *            the Event Cloud identifier to look for.
     * 
     * @return the trackers associated to the eventcloud identified by the
     *         specified {@link EventCloudId} or {@code null}.
     */
    public List<Tracker> findTrackers(EventCloudId id);

    /**
     * Registers a publish proxy to the list of proxies associated to the Event
     * Cloud represented by the specified {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier on which the proxy is associated
     *            to.
     * @param proxy
     *            the publish proxy to register.
     */
    public void registerProxy(EventCloudId id, PublishProxy proxy);

    /**
     * Registers a putget proxy to the list of proxies associated to the Event
     * Cloud represented by the specified {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier on which the proxy is associated
     *            to.
     * @param proxy
     *            the putget proxy to register.
     */
    public void registerProxy(EventCloudId id, PutGetProxy proxy);

    /**
     * Registers a subscribe proxy to the list of proxies associated to the
     * Event Cloud represented by the specified {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier on which the proxy is associated
     *            to.
     * @param proxy
     *            the subscribe proxy to register.
     */
    public void registerProxy(EventCloudId id, SubscribeProxy proxy);

    /**
     * Returns the publish proxies which are running for the specified
     * {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier
     * 
     * @return the publish proxies which are running for the specified
     *         {@link EventCloudId}.
     */
    public List<PublishProxy> getPublishProxies(EventCloudId id);

    /**
     * Returns the putget proxies which are running for the specified
     * {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier
     * 
     * @return the putget proxies which are running for the specified
     *         {@link EventCloudId}.
     */
    public List<PutGetProxy> getPutGetProxies(EventCloudId id);

    /**
     * Returns the subscribe proxies which are running for the specified
     * {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier
     * 
     * @return the subscribe proxies which are running for the specified
     *         {@link EventCloudId}.
     */
    public List<SubscribeProxy> getSubscribeProxies(EventCloudId id);

    /**
     * Unregisters a publish proxy from the list of publish proxies associated
     * to the Event Cloud represented by the specified {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier on which the publish proxy is
     *            associated to.
     * @param proxy
     *            the publish proxy to unregister.
     * 
     * @return {@code true} if the publish proxy has been successfully
     *         unregistered, {@code false} otherwise.
     */
    public boolean unregisterProxy(EventCloudId id, PublishProxy proxy);

    /**
     * Unregisters a putget proxy from the list of putget proxies associated to
     * the Event Cloud represented by the specified {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier on which the putget proxy is
     *            associated to.
     * @param proxy
     *            the putget proxy to unregister.
     * 
     * @return {@code true} if the putget proxy has been successfully
     *         unregistered, {@code false} otherwise.
     */
    public boolean unregisterProxy(EventCloudId id, PutGetProxy proxy);

    /**
     * Unregisters a subscribe proxy from the list of subscribe proxies
     * associated to the Event Cloud represented by the specified
     * {@link EventCloudId}.
     * 
     * @param id
     *            the Event Cloud identifier on which the subscribe proxy is
     *            associated to.
     * @param proxy
     *            the subscribe proxy to unregister.
     * 
     * @return {@code true} if the subscribe proxy has been successfully
     *         unregistered, {@code false} otherwise.
     */
    public boolean unregisterProxy(EventCloudId id, SubscribeProxy proxy);

    /**
     * Undeploys the eventcloud identified with the specified {@code id}.
     * 
     * @param id
     *            the Event Cloud identifier to look for.
     * 
     * @return {@code true} if the eventcloud has been undeployed, {@code false}
     *         if the specified {@code id} does not correspond to any eventcloud
     *         running.
     */
    boolean undeploy(EventCloudId id);

}
