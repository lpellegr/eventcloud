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
package fr.inria.eventcloud.proxies;

import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;

import fr.inria.eventcloud.api.PublishApi;

/**
 * A PublishProxy is a proxy that implements the {@link PublishApi}. It has to
 * be used by a user who wants to execute publish operations on an EventCloud.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface PublishProxy extends Proxy, PublishApi {

}
