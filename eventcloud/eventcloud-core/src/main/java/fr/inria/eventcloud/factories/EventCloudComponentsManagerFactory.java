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
package fr.inria.eventcloud.factories;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import fr.inria.eventcloud.deployment.EventCloudComponentsManager;

/**
 * This class is used to create a {@link EventCloudComponentsManager} as an
 * active object.
 * 
 * @author bsauvan
 */
public class EventCloudComponentsManagerFactory extends AbstractFactory {

    private EventCloudComponentsManagerFactory() {
    }

    /**
     * Creates a new component pool manager active object deployed on the local
     * JVM.
     * 
     * @param componentPoolNodeProvider
     *            the node provider to be used for the component pool.
     * 
     * @return the reference on the new component pool manager active object
     *         created.
     */
    public static EventCloudComponentsManager newComponentsManager(NodeProvider componentPoolNodeProvider,
                                                                   int nbTrackers,
                                                                   int nbPeers,
                                                                   int nbPublishProxies,
                                                                   int nbSubscribeProxies,
                                                                   int nbPutGetProxies) {
        return EventCloudComponentsManagerFactory.createComponentsManager(
                componentPoolNodeProvider, null, nbTrackers, nbPeers,
                nbPublishProxies, nbSubscribeProxies, nbPutGetProxies);
    }

    /**
     * Creates a new component pool manager active object deployed on the
     * specified {@code node}.
     * 
     * @param componentPoolNodeProvider
     *            the node provider to be used for the component pool.
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the new component pool manager active object
     *         created.
     */
    public static EventCloudComponentsManager newComponentsManager(NodeProvider componentPoolNodeProvider,
                                                                   Node node,
                                                                   int nbTrackers,
                                                                   int nbPeers,
                                                                   int nbPublishProxies,
                                                                   int nbSubscribeProxies,
                                                                   int nbPutGetProxies) {
        return EventCloudComponentsManagerFactory.createComponentsManager(
                componentPoolNodeProvider, node, nbTrackers, nbPeers,
                nbPublishProxies, nbSubscribeProxies, nbPutGetProxies);
    }

    /**
     * Creates a new component pool manager active object deployed on the
     * specified {@code GCM virtual node}.
     * 
     * @param componentPoolNodeProvider
     *            the node provider to be used for the component pool.
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on new the component pool manager active object
     *         created.
     */
    public static EventCloudComponentsManager newComponentsManager(NodeProvider componentPoolNodeProvider,
                                                                   GCMVirtualNode vn,
                                                                   int nbTrackers,
                                                                   int nbPeers,
                                                                   int nbPublishProxies,
                                                                   int nbSubscribeProxies,
                                                                   int nbPutGetProxies) {
        return EventCloudComponentsManagerFactory.createComponentsManager(
                componentPoolNodeProvider, vn.getANode(), nbTrackers, nbPeers,
                nbPublishProxies, nbSubscribeProxies, nbPutGetProxies);
    }

    /**
     * Creates a new component pool manager active object deployed on a node
     * provided by the specified {@code node provider}.
     * 
     * @param componentPoolNodeProvider
     *            the node provider to be used for the component pool.
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on new the component pool manager active object
     *         created.
     */
    public static EventCloudComponentsManager newComponentsManager(NodeProvider componentPoolNodeProvider,
                                                                   NodeProvider nodeProvider,
                                                                   int nbTrackers,
                                                                   int nbPeers,
                                                                   int nbPublishProxies,
                                                                   int nbSubscribeProxies,
                                                                   int nbPutGetProxies) {
        return EventCloudComponentsManagerFactory.createComponentsManager(
                componentPoolNodeProvider, nodeProvider.getANode(), nbTrackers,
                nbPeers, nbPublishProxies, nbSubscribeProxies, nbPutGetProxies);
    }

    private static EventCloudComponentsManager createComponentsManager(NodeProvider componentPoolNodeProvider,
                                                                       Node node,
                                                                       int nbTrackers,
                                                                       int nbPeers,
                                                                       int nbPublishProxies,
                                                                       int nbSubscribeProxies,
                                                                       int nbPutGetProxies) {
        try {
            return PAActiveObject.newActive(
                    EventCloudComponentsManager.class, new Object[] {
                            componentPoolNodeProvider, nbTrackers, nbPeers,
                            nbPublishProxies, nbSubscribeProxies,
                            nbPutGetProxies}, node);
        } catch (ActiveObjectCreationException e) {
            throw new IllegalStateException(e);
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }
    }

}
