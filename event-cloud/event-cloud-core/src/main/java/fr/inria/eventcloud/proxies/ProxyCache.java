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

import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Any user side proxy have to implement this abstract proxy class that stores
 * an {@link EventCloudCache} which serves as a cache.
 * 
 * @author lpellegr
 */
public abstract class ProxyCache {

    protected EventCloudCache proxy;

    protected ProxyCache() {
    }

    protected ProxyCache(EventCloudCache proxy) {
        this.proxy = proxy;
    }

    protected SemanticPeer selectPeer() {
        return this.proxy.selectTracker().getRandomSemanticPeer();
    }

}
