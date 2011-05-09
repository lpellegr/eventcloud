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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.messages.request.can;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.datastore.SemanticDatastore;
import fr.inria.eventcloud.messages.response.can.StatementsCounterResponse;

/**
 * Count the number of statements contained by each datastore associated to a
 * {@link Peer}.
 * 
 * @author lpellegr
 */
public class StatementsCounterRequest extends AnycastRequest {

    private static final long serialVersionUID = 1L;

    private URI spaceURI;

    private final Map<StatementsCounterRequest.Entry, Long> entries;

    public StatementsCounterRequest(final URI space) {
        super(new DefaultAnycastConstraintsValidator(new StringCoordinate(
                null, null, null)));
        this.spaceURI = space;
        this.entries = new HashMap<StatementsCounterRequest.Entry, Long>();
    }

    public Long put(StatementsCounterRequest.Entry key, Long value) {
        return this.entries.put(key, value);
    }

    public AnycastRequestRouter<AnycastRequest> getRouter() {
        return new AnycastRequestRouter<AnycastRequest>() {
            @Override
            public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                       AnycastRequest msg) {
                ClosableIterator<QueryRow> it =
                        ((SemanticDatastore) overlay.getDatastore()).sparqlSelect(
                                spaceURI, "SELECT ?s WHERE { ?s ?p ?o }")
                                .iterator();
                long count = 0;
                while (it.hasNext()) {
                    it.next();
                    count++;
                }
                it.close();
                ((StatementsCounterRequest) msg).put(new Entry(
                        overlay.getId(), overlay.getZone()), count);
            }
        };
    }

    public Map<StatementsCounterRequest.Entry, Long> getNbStatementsByPeer() {
        return this.entries;
    }

    public StatementsCounterResponse createResponse() {
        return new StatementsCounterResponse(this);
    }

    public static class Entry implements Serializable {

        private static final long serialVersionUID = 1L;

        private final UUID id;

        private final Zone zone;

        public Entry(UUID id, Zone zone) {
            super();
            this.id = id;
            this.zone = zone;
        }

        public UUID getId() {
            return this.id;
        }

        public Zone getZone() {
            return this.zone;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Entry && this.id.equals(((Entry) obj).id);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return this.id.hashCode();
        }

    }

}
