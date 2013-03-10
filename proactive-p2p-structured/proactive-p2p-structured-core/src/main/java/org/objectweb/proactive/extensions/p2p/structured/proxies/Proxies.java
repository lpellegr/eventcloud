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
package org.objectweb.proactive.extensions.p2p.structured.proxies;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

import com.google.common.collect.Lists;

/**
 * Static utility methods pertaining to {@link Proxy} instances.
 * 
 * @author lpellegr
 */
public class Proxies {

    @SafeVarargs
    public static <T extends Tracker> Proxy newProxy(T... trackers) {
        return newProxy(Lists.newArrayList(trackers));
    }

    public static <T extends Tracker> Proxy newProxy(List<T> trackers) {
        return new ProxyImpl(trackers);
    }

}
