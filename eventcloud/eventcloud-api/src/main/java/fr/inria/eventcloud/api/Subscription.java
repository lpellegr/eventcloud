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
package fr.inria.eventcloud.api;

import java.io.Serializable;

/**
 * A subscription expresses what are the notifications you are interested in. It
 * is based on a subset of the SPARQL query language. The subscription takes
 * effect when it is created (i.e. all the events which are published after this
 * creation time and that are matching your constraints will be delivered).
 * 
 * @author lpellegr
 */
public final class Subscription implements Serializable {

    /**
     * SPARQL used for any subscription object build with
     * {@link Subscription#acceptAll()}. The value is set to {@value} .
     */
    public static final String ACCEPT_ALL =
            "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }";

    private static final long serialVersionUID = 160L;

    private final SubscriptionId id;

    private final long creationTime;

    private final String sparqlQuery;

    private final String subscriptionDestination;

    public Subscription(String sparqlQuery) {
        this(sparqlQuery, null);
    }

    public Subscription(String sparqlQuery, String subscriptionDestination) {
        // TODO: check SPARQL query syntax

        this.id = new SubscriptionId();
        this.creationTime = System.currentTimeMillis();
        this.sparqlQuery = sparqlQuery;
        this.subscriptionDestination = subscriptionDestination;
    }

    public SubscriptionId getId() {
        return this.id;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getSparqlQuery() {
        return this.sparqlQuery;
    }

    public String getSubscriptionDestination() {
        return this.subscriptionDestination;
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
        if (this == obj) {
            return true;
        }

        return (obj instanceof Subscription)
                && this.id.equals(((Subscription) obj).id);
    }

    /**
     * Returns a new subscription object which accepts any quadruple value. In
     * other words the associated SPARQL query is equals to {@code SELECT ?g ?s
     * ?p ?o WHERE \ GRAPH ?g \ ?s ?p ?o \} .
     * 
     * @return a new subscription object which accepts any quadruple value. In
     *         other words the associated SPARQL query is equals to
     *         {@code SELECT ?g ?s ?p ?o WHERE \ GRAPH ?g \ ?s ?p ?o \} .
     */
    public static Subscription acceptAll() {
        return new Subscription(ACCEPT_ALL);
    }

}
