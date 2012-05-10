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
package fr.inria.eventcloud.api;

import java.io.Serializable;

/**
 * A subscription expresses what are the notifications you are interested in. It
 * is based on a subset of the SPARQL query language. The subscription takes
 * effect when it is created (i.e. all the events which are published after this
 * creation time and that are matching your constraints will be delivered).
 * Finally, each subscription can be submitted through a call to
 * {@link SubscribeApi#subscribe(Subscription, fr.inria.eventcloud.api.listeners.NotificationListener)}
 * on a subscribe proxy but once.
 * 
 * @author lpellegr
 */
public final class Subscription implements Serializable {

    private static final long serialVersionUID = 1L;

    private final SubscriptionId id;

    private final long creationTime;

    private final String sparqlQuery;

    public Subscription(String sparqlQuery) {
        // TODO: check SPARQL query syntax

        this.id = new SubscriptionId();
        this.creationTime = System.currentTimeMillis();
        this.sparqlQuery = sparqlQuery;
    }

    public SubscriptionId getId() {
        return this.id;
    }

    public String getSparqlQuery() {
        return this.sparqlQuery;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Subscription)
                && this.id.equals(((Subscription) obj).id);
    }

}
