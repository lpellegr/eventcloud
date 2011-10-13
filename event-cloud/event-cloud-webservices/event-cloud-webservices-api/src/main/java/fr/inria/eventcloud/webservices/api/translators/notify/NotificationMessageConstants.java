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
package fr.inria.eventcloud.webservices.api.translators.notify;

import com.hp.hpl.jena.graph.Node;

/**
 * 
 * 
 * @author bsauvan
 */
public class NotificationMessageConstants {

    public static final String SUBSCRIPTION_ADDRESS_TEXT =
            "http://docs.oasis-open.org/wsn/b-2/SubscriptionReference/Address";

    public static final Node SUBSCRIPTION_ADDRESS_NODE =
            Node.createURI(SUBSCRIPTION_ADDRESS_TEXT);

    public static final String TOPIC_TEXT =
            "http://docs.oasis-open.org/wsn/b-2/Topic";

    public static final Node TOPIC_NODE = Node.createURI(TOPIC_TEXT);

    public static final String PRODUCER_ADDRESS_TEXT =
            "http://docs.oasis-open.org/wsn/b-2/ProducerReference/Address";

    public static final Node PRODUCER_ADDRESS_NODE =
            Node.createURI(PRODUCER_ADDRESS_TEXT);

    public static final String PRODUCER_METADATA_TEXT =
            "http://docs.oasis-open.org/wsn/b-2/ProducerReference/Metadata";

}
