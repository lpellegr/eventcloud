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

import com.hp.hpl.jena.graph.Node;

/**
 * Defines constants that are regularly used by the publish/subscribe
 * algorithms.
 * 
 * @author lpellegr
 */
public final class PublishSubscribeConstants {

    /*
     * Namespaces
     */

    public static final String EVENTCLOUD_NS = "urn:ec:";

    public static final String EVENT_NS = EVENTCLOUD_NS + "event:";

    public static final String SUBSCRIPTION_NS = EVENTCLOUD_NS + "s:";

    public static final String SUBSUBSCRIPTION_NS = EVENTCLOUD_NS + "ss:";

    public static final String PUBLICATION_NS = EVENTCLOUD_NS + "pub:";

    public static final String QUADRUPLE_NS = EVENTCLOUD_NS + "quad:";

    /*
     * Nodes associated to namespaces
     */

    public static final Node EVENT_NS_NODE = Node.createURI(EVENT_NS);

    public static final Node SUBSCRIPTION_NS_NODE =
            Node.createURI(SUBSCRIPTION_NS);

    public static final Node SUBSUBSCRIPTION_NS_NODE =
            Node.createURI(SUBSUBSCRIPTION_NS);

    public static final Node PUBLICATION_NS_NODE =
            Node.createURI(PUBLICATION_NS);

    public static final Node QUADRUPLE_NS_NODE = Node.createURI(QUADRUPLE_NS);

    /*
     * Properties
     */

    public static final String EVENT_NB_QUADRUPLES = EVENT_NS + "nbquads";

    public static final String SUBSCRIPTION_ID_PROPERTY = SUBSCRIPTION_NS
            + "id";

    public static final String SUBSCRIPTION_PARENT_ID_PROPERTY =
            SUBSCRIPTION_NS + "pid";

    public static final String SUBSCRIPTION_ORIGINAL_ID_PROPERTY =
            SUBSCRIPTION_NS + "oid";

    public static final String SUBSCRIPTION_SPARQL_QUERY_PROPERTY =
            SUBSCRIPTION_NS + "query";

    public static final String SUBSCRIPTION_CREATION_DATETIME_PROPERTY =
            SUBSCRIPTION_NS + "ctime";

    public static final String SUBSCRIPTION_INDEXATION_DATETIME_PROPERTY =
            SUBSCRIPTION_NS + "itime";

    public static final String SUBSCRIPTION_TYPE_PROPERTY = SUBSCRIPTION_NS
            + "type";

    public static final String SUBSCRIPTION_SUBSCRIBER_PROPERTY =
            SUBSCRIPTION_NS + "subscriber";

    public static final String SUBSCRIPTION_DESTINATION_PROPERTY = EVENT_NS
            + "sdestination";

    public static final String SUBSCRIPTION_INDEXED_WITH_PROPERTY =
            SUBSCRIPTION_NS + "iref";

    public static final String SUBSCRIPTION_STUB_PROPERTY = SUBSCRIPTION_NS
            + "stub";

    public static final String SUBSCRIPTION_HAS_SUBSUBSCRIPTION_PROPERTY =
            SUBSCRIPTION_NS + "hss";

    public static final String SUBSUBSCRIPTION_ID_PROPERTY = SUBSUBSCRIPTION_NS
            + "id";

    public static final String SUBSUBSCRIPTION_INDEX_PROPERTY =
            SUBSUBSCRIPTION_NS + "index";

    public static final String SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY =
            SUBSUBSCRIPTION_NS + "g";

    public static final String SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY =
            SUBSUBSCRIPTION_NS + "s";

    public static final String SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY =
            SUBSUBSCRIPTION_NS + "p";

    public static final String SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY =
            SUBSUBSCRIPTION_NS + "o";

    /*
     *  Nodes associated to the properties
     */

    public static final Node EVENT_NB_QUADRUPLES_NODE =
            Node.createURI(EVENT_NB_QUADRUPLES);

    public static final Node SUBSCRIPTION_ID_NODE =
            Node.createURI(SUBSCRIPTION_ID_PROPERTY);

    public static final Node SUBSCRIPTION_PARENT_ID_NODE =
            Node.createURI(SUBSCRIPTION_PARENT_ID_PROPERTY);

    public static final Node SUBSCRIPTION_ORIGINAL_ID_NODE =
            Node.createURI(SUBSCRIPTION_ORIGINAL_ID_PROPERTY);

    public static final Node SUBSCRIPTION_SERIALIZED_VALUE_NODE =
            Node.createURI(SUBSCRIPTION_SPARQL_QUERY_PROPERTY);

    public static final Node SUBSCRIPTION_CREATION_DATETIME_NODE =
            Node.createURI(SUBSCRIPTION_CREATION_DATETIME_PROPERTY);

    public static final Node SUBSCRIPTION_INDEXATION_DATETIME_NODE =
            Node.createURI(SUBSCRIPTION_INDEXATION_DATETIME_PROPERTY);

    public static final Node SUBSCRIPTION_TYPE_NODE =
            Node.createURI(SUBSCRIPTION_TYPE_PROPERTY);

    public static final Node SUBSCRIPTION_SUBSCRIBER_NODE =
            Node.createURI(SUBSCRIPTION_SUBSCRIBER_PROPERTY);

    public static final Node SUBSCRIPTION_DESTINATION_NODE =
            Node.createURI(SUBSCRIPTION_DESTINATION_PROPERTY);

    public static final Node SUBSCRIPTION_INDEXED_WITH_NODE =
            Node.createURI(SUBSCRIPTION_INDEXED_WITH_PROPERTY);

    public static final Node SUBSCRIPTION_STUB_NODE =
            Node.createURI(SUBSCRIPTION_STUB_PROPERTY);

    public static final Node SUBSCRIPTION_HAS_SUBSUBSCRIPTION_NODE =
            Node.createURI(SUBSCRIPTION_HAS_SUBSUBSCRIPTION_PROPERTY);

    public static final Node SUBSUBSCRIPTION_ID_NODE =
            Node.createURI(SUBSUBSCRIPTION_ID_PROPERTY);

    public static final Node SUBSUBSCRIPTION_INDEX_NODE =
            Node.createURI(SUBSUBSCRIPTION_INDEX_PROPERTY);

    public static final Node SUBSUBSCRIPTION_GRAPH_VALUE_NODE =
            Node.createURI(SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY);

    public static final Node SUBSUBSCRIPTION_SUBJECT_VALUE_NODE =
            Node.createURI(SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY);

    public static final Node SUBSUBSCRIPTION_PREDICATE_VALUE_NODE =
            Node.createURI(SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY);

    public static final Node SUBSUBSCRIPTION_OBJECT_VALUE_NODE =
            Node.createURI(SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY);

    /*
     * Values
     */

    public static final String SUBSCRIPTION_VARIABLE_VALUE = EVENTCLOUD_NS
            + "var";

    public static final String QUADRUPLE_MATCHES_SUBSCRIPTION_VALUE =
            SUBSCRIPTION_NS + "matches";

    /*
     * Nodes associated to values
     */

    public static final Node SUBSCRIPTION_VARIABLE_NODE =
            Node.createURI(SUBSCRIPTION_VARIABLE_VALUE);

    public static final Node QUADRUPLE_MATCHES_SUBSCRIPTION_NODE =
            Node.createURI(QUADRUPLE_MATCHES_SUBSCRIPTION_VALUE);

    private PublishSubscribeConstants() {

    }

}
