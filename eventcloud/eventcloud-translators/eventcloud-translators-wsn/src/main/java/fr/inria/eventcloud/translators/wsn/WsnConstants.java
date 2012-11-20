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
package fr.inria.eventcloud.translators.wsn;

import javax.xml.namespace.QName;

import com.hp.hpl.jena.graph.Node;

import eu.play_project.play_commons.constants.Namespace;

/**
 * Defines constants which are related to WS-Notification messages and
 * translations.
 * 
 * @author bsauvan
 */
public class WsnConstants {

    /**
     * Defines the value of the separator that is used to concatenate several
     * URIs into a new one.
     */
    public static final String URI_SEPARATOR = "$0$";

    public static final String SHARP_ESCAPE = "\\$1\\$";

    public static final String TOPIC_TEXT = Namespace.TYPES.getUri() + "stream";

    public static final Node TOPIC_NODE = Node.createURI(TOPIC_TEXT);

    public static final String PRODUCER_ADDRESS_TEXT =
            "http://docs.oasis-open.org/wsn/b-2/ProducerReference/Address";

    public static final Node PRODUCER_ADDRESS_NODE =
            Node.createURI(PRODUCER_ADDRESS_TEXT);

    public static final String PRODUCER_METADATA_NAMESPACE =
            "http://docs.oasis-open.org/wsn/b-2/ProducerReference";

    public static final String PRODUCER_METADATA_TEXT =
            "http://docs.oasis-open.org/wsn/b-2/ProducerReference/Metadata";

    public static final String MESSAGE_TEXT =
            "http://docs.oasis-open.org/wsn/b-2/Message";

    public static final String PRODUCER_METADATA_EVENT_NAMESPACE =
            "http://eventcloud.inria.fr";

    public static final String XML_TRANSLATION_MARKER = "-wsn-xml";

    public static final String SIMPLE_TOPIC_EXPRESSION_MARKER = "-ste";

    public final static QName TOPIC_EXPRESSION_QNAME = new QName(
            "http://docs.oasis-open.org/wsn/b-2", "TopicExpression");

    public static final String TOPIC_EXPRESSION_DIALECT =
            "http://docs.oasis-open.org/wsn/t-1/TopicExpression/Concrete";

    public final static QName SIMPLE_TOPIC_EXPRESSION_QNAME = new QName(
            "http://www.ebmwebsourcing.com/wsn/t-1-extension",
            "simpleTopicExpression");

    public static final String SIMPLE_TOPIC_EXPRESSION_DIALECT =
            "http://www.w3.org/TR/1999/REC-xpath-19991116";

    public final static String SUBSCRIPTION_ID_NAMESPACE =
            "http://www.ebmwebsourcing.com/wsstar/wsnb/ws-resource";

    public final static String SUBSCRIPTION_ID_QUALIFIED_NAME =
            "rpimpl:SubscriptionId";

}
