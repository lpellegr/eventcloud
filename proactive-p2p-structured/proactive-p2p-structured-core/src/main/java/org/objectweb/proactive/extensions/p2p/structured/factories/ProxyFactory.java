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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.proxies.ProxyAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.proxies.ProxyImpl;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * ProxyFactory is used to create a new instance of a {@link Proxy}.
 * 
 * @author lpellegr
 */
public class ProxyFactory extends AbstractFactory {

    private static Map<String, ProxyCache> PROXY_CACHES =
            new HashMap<String, ProxyCache>();

    private ProxyFactory() {

    }

    public static Proxy newProxy(Tracker... trackers) {
        return newProxy(ImmutableList.copyOf(trackers));
    }

    /**
     * Creates a new proxy component deployed on the local JVM.
     * 
     * @param trackers
     *            the trackers to use for getting entry points in the P2P
     *            network.
     * 
     * @return the reference on the {@link Proxy} interface of the new proxy
     *         component created.
     */
    public static Proxy newProxy(List<Tracker> trackers) {
        return createProxy(new HashMap<String, Object>(), trackers);
    }

    /**
     * Creates a new proxy component deployed on the specified {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param trackers
     *            the trackers to use for getting entry points in the P2P
     *            network.
     * 
     * @return the reference on the {@link Proxy} interface of the new proxy
     *         component created.
     */
    public static Proxy newProxy(Node node, List<Tracker> trackers) {
        return createProxy(ComponentUtils.createContext(node), trackers);
    }

    /**
     * Creates a new proxy component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param trackers
     *            the trackers to use for getting entry points in the P2P
     *            network.
     * 
     * @return the reference on the {@link Proxy} interface of the new proxy
     *         component created.
     */
    public static Proxy newProxy(GCMVirtualNode vn, List<Tracker> trackers) {
        return createProxy(ComponentUtils.createContext(vn), trackers);
    }

    /**
     * Creates a new proxy component deployed on a node provided by the
     * specified {@code node provider}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param trackers
     *            the trackers to use for getting entry points in the P2P
     *            network.
     * 
     * @return the reference on the {@link Proxy} interface of the new proxy
     *         component created.
     */
    public static Proxy newProxy(NodeProvider nodeProvider,
                                 List<Tracker> trackers) {
        return createProxy(AbstractFactory.getContextFromNodeProvider(
                nodeProvider, ProxyImpl.PROXY_VN), trackers);
    }

    protected static Proxy createProxy(Map<String, Object> context,
                                       List<Tracker> trackers) {
        Preconditions.checkArgument(trackers.size() > 0, "No tracker specified");

        try {
            Proxy proxy =
                    ComponentUtils.createComponentAndGetInterface(
                            ProxyImpl.PROXY_ADL, context,
                            ProxyImpl.PROXY_SERVICES_ITF, Proxy.class, true);

            ProxyCache proxyCache =
                    ProxyFactory.getOrCreateProxyCache(trackers);

            ((ProxyAttributeController) GCM.getAttributeController(((Interface) proxy).getFcItfOwner())).initAttributes(proxyCache);

            return proxy;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ProxyCache getOrCreateProxyCache(List<Tracker> trackers) {
        String networkName = trackers.get(0).getNetworkName();

        ProxyCache proxyCache = null;

        synchronized (PROXY_CACHES) {
            proxyCache = PROXY_CACHES.get(networkName);

            if (proxyCache == null) {
                proxyCache = new ProxyCache(trackers);
                PROXY_CACHES.put(networkName, proxyCache);
            }
        }

        return proxyCache;
    }

    public static void clear() {
        synchronized (PROXY_CACHES) {
            PROXY_CACHES.clear();
        }
    }

}
