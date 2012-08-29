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
package fr.inria.eventcloud.overlay;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

import fr.inria.eventcloud.api.PutGetApi;

/**
 * The SemanticPeer interface is used to merge the {@link Peer} interface and
 * the {@link PutGetApi} interface into a single one.
 * 
 * @author bsauvan
 */
public interface SemanticPeer extends Peer, PutGetApi {

}