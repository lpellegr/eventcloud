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
package fr.inria.eventcloud.proxies;

import org.objectweb.proactive.extensions.p2p.structured.AbstractComponent;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

import fr.inria.eventcloud.monitoring.ProxyMonitoringManager;
import fr.inria.eventcloud.monitoring.ProxyMonitoringManagerImpl;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Common interface to user side proxies.
 * 
 * @author lpellegr
 */
public abstract class Proxy extends AbstractComponent {

    /**
     * GCM Virtual Node name of the proxy component.
     */
    public static final String PROXY_VN = "ProxyVN";

    protected EventCloudCache eventCloudCache;

    protected org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy proxy;

    protected ProxyMonitoringManager monitoringManager;

    protected Proxy() {
        super();
        this.monitoringManager = new ProxyMonitoringManagerImpl();
    }

    public void sendv(Request<?> request) throws DispatchException {
        this.proxy.sendv(request);
    }

    public void sendv(Request<?> request, Peer peer) throws DispatchException {
        this.proxy.sendv(request, peer);
    }

    public Response<?> send(Request<?> request) throws DispatchException {
        return this.proxy.send(request);
    }

    public Response<?> send(Request<?> request, Peer peer)
            throws DispatchException {
        return this.proxy.send(request, peer);
    }

    public SemanticPeer selectPeer() {
        return (SemanticPeer) this.proxy.selectPeer();
    }

    public EventCloudCache getEventCloudCache() {
        return this.eventCloudCache;
    }

}
