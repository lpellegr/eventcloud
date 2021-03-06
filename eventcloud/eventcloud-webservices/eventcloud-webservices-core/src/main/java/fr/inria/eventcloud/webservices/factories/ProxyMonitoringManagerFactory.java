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
package fr.inria.eventcloud.webservices.factories;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.webservices.monitoring.ProxyMonitoringManager;
import fr.inria.eventcloud.webservices.monitoring.ProxyMonitoringManagerImpl;

/**
 * This class is used to create and deploy a proxy monitoring manager as a non
 * functional component.
 * 
 * @author bsauvan
 */
public class ProxyMonitoringManagerFactory extends AbstractFactory {

    private static final Logger LOG =
            LoggerFactory.getLogger(ProxyMonitoringManagerFactory.class);

    private ProxyMonitoringManagerFactory() {
    }

    /**
     * Creates a new proxy monitoring manager non functional component deployed
     * on the local JVM.
     * 
     * @return the reference on the {@link ProxyMonitoringManager} interface of
     *         the new proxy monitoring manager non functional component
     *         created.
     */
    public static ProxyMonitoringManager newProxyMonitoringManager() {
        return ProxyMonitoringManagerFactory.createProxyMonitoringManager(new HashMap<String, Object>());
    }

    /**
     * Creates a new proxy monitoring manager non functional component deployed
     * on the specified {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link ProxyMonitoringManager} interface of
     *         the new proxy monitoring manager non functional component
     *         created.
     */
    public static ProxyMonitoringManager newProxyMonitoringManager(Node node) {
        return ProxyMonitoringManagerFactory.createProxyMonitoringManager(ComponentUtils.createContext(node));
    }

    /**
     * Creates a new proxy monitoring manager non functional component deployed
     * on the specified {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link ProxyMonitoringManager} interface of
     *         the new proxy monitoring manager non functional component
     *         created.
     */
    public static ProxyMonitoringManager newProxyMonitoringManager(GCMVirtualNode vn) {
        return ProxyMonitoringManagerFactory.createProxyMonitoringManager(ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new proxy monitoring manager non functional component deployed
     * on a node provided by the specified {@code node provider}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param vnName
     *            the name of the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link ProxyMonitoringManager} interface of
     *         the new proxy monitoring manager non functional component
     *         created.
     */
    public static ProxyMonitoringManager newProxyMonitoringManager(NodeProvider nodeProvider,
                                                                   String vnName) {
        return ProxyMonitoringManagerFactory.createProxyMonitoringManager(AbstractFactory.getContextFromNodeProvider(
                nodeProvider, vnName));
    }

    private static ProxyMonitoringManager createProxyMonitoringManager(Map<String, Object> context) {
        ProxyMonitoringManager proxyMonitoringManager =
                ComponentUtils.createNfComponentAndGetInterface(
                        ProxyMonitoringManagerImpl.PROXY_MONITORING_MANAGER_ADL,
                        context,
                        ProxyMonitoringManagerImpl.MONITORING_SERVICES_ITF,
                        ProxyMonitoringManager.class, true);

        LOG.info("ProxyMonitoringManager created");

        return proxyMonitoringManager;
    }

}
