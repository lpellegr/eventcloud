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
package fr.inria.eventcloud.api;

import static com.google.common.base.Preconditions.checkNotNull;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * SemanticFactory must be used to create new instances of Semantic objects like
 * for example {@link SemanticPeer}s or {@link SemanticTracker}.
 * 
 * @author lpellegr
 */
public class SemanticFactory {

    /**
     * Creates a new active semantic tracker on the local JVM and associates it
     * to the network named "default".
     * 
     * @return the new active semantic tracker created.
     */
    public static SemanticTracker newActiveSemanticTracker() {
        return SemanticFactory.newActiveSemanticTracker("default", null);
    }

    /**
     * Creates a new active semantic tracker on the local JVM and associates it
     * to the specified {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the new active semantic tracker created.
     */
    public static SemanticTracker newActiveSemanticTracker(String networkName) {
        return SemanticFactory.newActiveSemanticTracker(networkName, null);
    }

    /**
     * Creates a new semantic tracker active object on the specified
     * {@code node} and associates it to the given {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param node
     *            the node to use for the deployment.
     * 
     * @return the new semantic tracker active object created.
     */
    public static SemanticTracker newActiveSemanticTracker(String networkName,
                                                           Node node) {
        try {
            return PAActiveObject.newActive(
                    SemanticTracker.class, new Object[] {networkName}, node);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new {@link SemanticPeer} active object on the local machine.
     * 
     * @return the new active SemanticPeer object created.
     */
    public static SemanticPeer newActiveSemanticPeer() {
        return SemanticFactory.newActiveSemanticPeer(null);
    }

    /**
     * Creates a new semantic peer active object deployed on the specified
     * {@code node}.
     * 
     * @param node
     *            the node used to deploy the peer.
     * 
     * @return the new semantic peer active object created.
     */
    public static SemanticPeer newActiveSemanticPeer(Node node) {
        try {
            return PAActiveObject.newActive(
                    SemanticPeer.class, new Object[0], node);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates the specified {@code number} of SemanticPeer active object in
     * parallel.
     * 
     * @param number
     *            the number of {@link SemanticPeer} to create.
     * @return the {@link SemanticPeer} active objects created.
     */
    public static SemanticPeer[] newActiveSemanticPeersInParallel(int number) {
        return newActiveSemanticPeersInParallel(new Node[number]);
    }

    /**
     * Creates a number of SemanticPeer active objects which equals to the
     * number {@code nodes} specified. Each new SemanticPeer being deployed on a
     * node from the array node specified in parameter.
     * 
     * @param nodes
     *            the nodes to use for the deployment of active objects.
     * @return the {@link SemanticPeer} active objects created.
     */
    public static SemanticPeer[] newActiveSemanticPeersInParallel(Node[] nodes) {
        checkNotNull(nodes);

        try {
            return (SemanticPeer[]) PAActiveObject.newActiveInParallel(
                    SemanticPeer.class.getCanonicalName(),
                    new Object[nodes.length][0], nodes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
