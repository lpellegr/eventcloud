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

import java.io.Serializable;
import java.util.Collection;

import fr.inria.eventcloud.api.Quadruple;

/**
 * Data exchanged during a
 * {@link SemanticPeer#join(org.objectweb.proactive.extensions.p2p.structured.overlay.Peer)}
 * or {@link SemanticPeer#leave()} operation.
 * 
 * @author lpellegr
 */
public class SemanticData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Collection<Quadruple> data;

    private final Collection<Quadruple> subscriptions;

    public SemanticData(Collection<Quadruple> data,
            Collection<Quadruple> subscriptions) {
        this.data = data;
        this.subscriptions = subscriptions;
    }

    public Collection<Quadruple> getMiscData() {
        return this.data;
    }

    public Collection<Quadruple> getSubscriptions() {
        return this.subscriptions;
    }

}
