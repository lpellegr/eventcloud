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
package fr.inria.eventcloud.translators.wsnotif.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.inria.eventcloud.api.Event;

/**
 * Translates a WS-Notification subscription payload to an {@link Event}. For
 * this translation, three information are needed: the WS-Notification
 * subscription, the TopicNamespace definition, the definition of the topics.
 * 
 * @author lpellegr
 */
public final class WsNotifSubscriptionToSparqlHandler {

    private static final String ADDRESSING_NAMESPACE =
            "http://www.w3.org/2005/08/addressing";

    private static final String WS_NOTIFICATION_NAMESPACE =
            "http://docs.oasis-open.org/wsn/b-2";

    private static final String WS_TOPIC_NAMESPACE =
            "http://docs.oasis-open.org/wsn/t-1";

    private static final String WSDL_NAMESPACE =
            "http://schemas.xmlsoap.org/wsdl/";

    private WsNotifSubscriptionToSparqlHandler() {

    }

    public static String createSparqlSubscription(String topicMessageElement) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ?g ?p ?o WHERE {\n    GRAPH ?g {\n        <");
        query.append(topicMessageElement);
        query.append("> ?p ?o\n    }\n}");
        return query.toString();
    }

    private static class DefaultHandlerPrefixesAware extends DefaultHandler {

        // contains the prefixes and their associated value
        // prefix -> namespace
        private Map<String, String> prefixes;

        public DefaultHandlerPrefixesAware() {
            this.prefixes = new HashMap<String, String>();
        }

        @Override
        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            this.prefixes.put(prefix, uri);
        }

    }

    public static class Subscription extends DefaultHandlerPrefixesAware {

        private String topicExpression;

        private String consumerReference;

        private String lastTextNodeRead;

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            // at this time we look only for a basic subscription by topic
            if (uri.startsWith(WS_NOTIFICATION_NAMESPACE)
                    && localName.equals("TopicExpression")) {
                this.topicExpression = this.lastTextNodeRead;
            } else if (uri.startsWith(ADDRESSING_NAMESPACE)
                    && localName.equals("Address")) {
                this.consumerReference = this.lastTextNodeRead;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            this.lastTextNodeRead = new String(ch, start, length);
        }

        @Override
        public void endDocument() throws SAXException {
            String topicExpressionPrefix =
                    XmlUtils.splitQName(this.topicExpression)[0];
            this.topicExpression =
                    this.topicExpression.replace(
                            topicExpressionPrefix + ":",
                            super.prefixes.get(topicExpressionPrefix));
        }

        public String getConsumerReference() {
            return this.consumerReference;
        }

        public String getTopicExpression() {
            return this.topicExpression;
        }

    }

    public static class TopicNamespace extends DefaultHandlerPrefixesAware {

        // contains the topic names and their associated messageType
        // topicName -> messageType
        // TODO: currently we assume that the a topicName has only one
        // messageType whereas it is possible to have several messageTypes for a
        // given topicName
        private Map<String, String> topicNames = new HashMap<String, String>();

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            if (uri.startsWith(WS_TOPIC_NAMESPACE) && localName.equals("Topic")) {
                String topicName = null;
                String messageType = null;

                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.getLocalName(i).equals("name")) {
                        topicName = attributes.getValue(i);
                    } else if (attributes.getLocalName(i)
                            .equals("messageTypes")) {
                        messageType = attributes.getValue(i);
                    }
                }

                this.topicNames.put(topicName, messageType);
            }
        }

        @Override
        public void endDocument() throws SAXException {
            // replace the topicNamespace qName by the
            // fullQualifiedTopicNamepace values
            Map<String, String> fullQualifiedTopicNames =
                    new HashMap<String, String>();

            for (Entry<String, String> entry : this.topicNames.entrySet()) {
                String messageType = entry.getValue();
                String messageTypePrefix = XmlUtils.splitQName(messageType)[0];
                String prefixNamespace = super.prefixes.get(messageTypePrefix);

                if (prefixNamespace == null) {
                    throw new IllegalStateException(
                            "TopicNamespace document contains a topicName with a messageType value whose the qName prefix is not declared: "
                                    + messageTypePrefix);
                } else {
                    fullQualifiedTopicNames.put(
                            entry.getKey(), messageType.replace(
                                    messageTypePrefix + ":", prefixNamespace));
                }
            }

            this.topicNames = fullQualifiedTopicNames;
        }

        public Map<String, String> getTopicNames() {
            return this.topicNames;
        }

    }

    public static class TopicDefinition extends DefaultHandlerPrefixesAware {

        private String messageType;

        private boolean messageTypeRead;

        private String topicMessageElement;

        public TopicDefinition(String messageTypeUri) {
            this.messageType = XmlUtils.getLocalNameFromUri(messageTypeUri);
            this.messageTypeRead = false;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (uri.equals(WSDL_NAMESPACE) && localName.equals("message")) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.getLocalName(i).equals("name")) {
                        if (attributes.getValue(i).equals(this.messageType)) {
                            this.messageTypeRead = true;
                        }
                    }
                }
            } else if (this.messageTypeRead && uri.equals(WSDL_NAMESPACE)
                    && localName.equals("part")) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (attributes.getLocalName(i).equals("element")) {
                        this.topicMessageElement = attributes.getValue(i);
                    }
                }
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (uri.equals(WSDL_NAMESPACE) && localName.equals("message")
                    && this.messageTypeRead) {
                this.messageTypeRead = false;
            }
        }

        @Override
        public void endDocument() throws SAXException {
            if (this.topicMessageElement != null) {
                String[] topicMessageElementQName =
                        this.topicMessageElement.split(":");
                String namespace =
                        super.prefixes.get(topicMessageElementQName[0]);

                this.topicMessageElement =
                        (namespace.endsWith("/")
                                ? super.prefixes.get(topicMessageElementQName[0])
                                : super.prefixes.get(topicMessageElementQName[0])
                                        + "/")
                                + topicMessageElementQName[1];
            }
        }

        public void clear() {
            this.messageTypeRead = false;
            this.topicMessageElement = null;
        }

        public String getMessageType() {
            return this.messageType;
        }

        public String getTopicMessageElement() {
            return this.topicMessageElement;
        }

    }

}
