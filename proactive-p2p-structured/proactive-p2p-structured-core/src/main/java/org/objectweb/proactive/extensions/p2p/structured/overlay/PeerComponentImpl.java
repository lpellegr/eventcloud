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
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.component.body.ComponentEndActive;
import org.objectweb.proactive.core.component.body.ComponentInitActive;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.slf4j.LoggerFactory;

/**
 * PeerComponentImpl is a component extension of a {@link PeerImpl}. It is
 * composed of a {@link StructuredOverlay} which allows to have several
 * implementations of common operations for each peer-to-peer protocol to
 * implement.
 * <p>
 * Warning, this class must not be instantiate directly. In order to create a
 * new component peer you must use the {@link PeerFactory}.
 * 
 * @author bsauvan
 */
public class PeerComponentImpl extends PeerImpl implements Peer,
        ComponentInitActive, ComponentEndActive, Serializable {

    private static final long serialVersionUID = 1L;

    static {
        logger = LoggerFactory.getLogger(PeerComponentImpl.class);
    }

    /**
     * The no-argument constructor as commanded by ProActive.
     */
    public PeerComponentImpl() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        super.initActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        super.endActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OverlayType getType() {
        return this.overlay.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Peer
                && this.getId().equals(((PeerComponentImpl) obj).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

}
