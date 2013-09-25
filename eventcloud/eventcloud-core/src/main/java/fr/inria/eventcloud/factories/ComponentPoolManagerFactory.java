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

import fr.inria.eventcloud.deployment.ComponentPoolManager;

/**
 * This class is used to create a {@link ComponentPoolManager} as an active
 * object.
 * 
 * @author bsauvan
 */
public class ComponentPoolManagerFactory extends AbstractFactory {

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
    public static ComponentPoolManager newComponentPoolManager(NodeProvider componentPoolNodeProvider) {
        return ComponentPoolManagerFactory.createComponentPoolManager(
                componentPoolNodeProvider, null);
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
    public static ComponentPoolManager newComponentPoolManager(NodeProvider componentPoolNodeProvider,
                                                               Node node) {
        return ComponentPoolManagerFactory.createComponentPoolManager(
                componentPoolNodeProvider, node);
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
    public static ComponentPoolManager newComponentPoolManager(NodeProvider componentPoolNodeProvider,
                                                               GCMVirtualNode vn) {
        return ComponentPoolManagerFactory.createComponentPoolManager(
                componentPoolNodeProvider, vn.getANode());
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
    public static ComponentPoolManager newComponentPoolManager(NodeProvider componentPoolNodeProvider,
                                                               NodeProvider nodeProvider) {
        return ComponentPoolManagerFactory.createComponentPoolManager(
                componentPoolNodeProvider, nodeProvider.getANode());
    }

    private static ComponentPoolManager createComponentPoolManager(NodeProvider componentPoolNodeProvider,
                                                                   Node node) {
        try {
            return PAActiveObject.newActive(
                    ComponentPoolManager.class,
                    new Object[] {componentPoolNodeProvider}, node);
        } catch (ActiveObjectCreationException e) {
            throw new IllegalStateException(e);
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }
    }

}
