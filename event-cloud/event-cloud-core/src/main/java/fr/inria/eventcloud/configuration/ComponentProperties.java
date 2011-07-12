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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.configuration;

import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;

/**
 * Contains default values for component properties.
 * 
 * @author bsauvan
 */
public class ComponentProperties {

    public static final PropertyString SEMANTIC_PEER_ADL = new PropertyString(
            "semantic.peer.adl",
            "fr.inria.eventcloud.overlay.SemanticPeer");

}
