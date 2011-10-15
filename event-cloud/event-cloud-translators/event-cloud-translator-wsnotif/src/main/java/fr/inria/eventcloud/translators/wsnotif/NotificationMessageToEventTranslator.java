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
package fr.inria.eventcloud.translators.wsnotif;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.Generator;

/**
 * Translator for {@link NotificationMessageHolderType notification messages} to
 * {@link Event events}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class NotificationMessageToEventTranslator {

    /**
     * Translates the specified notification message to its corresponding event.
     * 
     * @param notificationMessage
     *            the notification message to be translated.
     * 
     * @return the event corresponding to the specified notification message.
     */
    public Event translate(NotificationMessageHolderType notificationMessage) {
        List<Quadruple> quads = new ArrayList<Quadruple>();
        Node eventId = Node.createURI(Generator.generateRandomUri().toString());
        Node subjectNode = null;
        Node subscriptionAddress = null;
        Node topic = null;
        Node producerAddress = null;
        Map<Node, Node> producerMetadatas = new HashMap<Node, Node>();
        Map<Node, Node> messages = new HashMap<Node, Node>();

        String subscriptionAddressText =
                (String) getFieldValue(getFieldValue(
                        notificationMessage.getSubscriptionReference(),
                        "address"), "uri");
        subscriptionAddress = Node.createLiteral(subscriptionAddressText);

        List<Object> topicContent = notificationMessage.getTopic().getContent();
        if (topicContent.size() > 0) {
            topic = Node.createLiteral((String) topicContent.get(0));
        }

        String producerAddressText =
                (String) getFieldValue(
                        getFieldValue(
                                notificationMessage.getProducerReference(),
                                "address"), "uri");
        producerAddress = Node.createLiteral(producerAddressText);

        @SuppressWarnings("unchecked")
        List<Element> producerMetadataElements =
                (List<Element>) getFieldValue(
                        getFieldValue(
                                notificationMessage.getProducerReference(),
                                "metadata"), "elements");
        producerMetadatas = parseElements(producerMetadataElements);

        Message message = notificationMessage.getMessage();
        messages = parseElement((Element) message.getAny());

        // the subject value is equals to the full qualified name of the first
        // element contained in the message payload
        org.w3c.dom.Node firstElt =
                ((Element) message.getAny()).getChildNodes().item(0);
        subjectNode =
                Node.createURI(firstElt.getNamespaceURI() + "/"
                        + firstElt.getLocalName());

        quads.add(new Quadruple(
                eventId, subjectNode,
                WsNotificationTranslatorConstants.SUBSCRIPTION_ADDRESS_NODE,
                subscriptionAddress, false, true));

        quads.add(new Quadruple(
                eventId, subjectNode,
                WsNotificationTranslatorConstants.TOPIC_NODE, topic, false,
                true));

        quads.add(new Quadruple(
                eventId, subjectNode,
                WsNotificationTranslatorConstants.PRODUCER_ADDRESS_NODE,
                producerAddress, false, true));

        for (Entry<Node, Node> entry : producerMetadatas.entrySet()) {
            quads.add(new Quadruple(
                    eventId, subjectNode, entry.getKey(), entry.getValue(),
                    false, true));
        }

        for (Entry<Node, Node> entry : messages.entrySet()) {
            quads.add(new Quadruple(
                    eventId, subjectNode, entry.getKey(), entry.getValue(),
                    false, true));
        }

        return new Event(new Collection<Quadruple>(quads));
    }

    private static Object getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<Node, Node> parseElements(List<Element> elements) {
        Map<Node, Node> elementNodes = new HashMap<Node, Node>();

        for (Element element : elements) {
            elementNodes.putAll(parseElement(element));
        }

        return elementNodes;
    }

    private Map<Node, Node> parseElement(Element element) {
        Map<Node, Node> result = new HashMap<Node, Node>();

        this.parseElement(element, new StringBuilder(), result);

        return result;
    }

    private void parseElement(org.w3c.dom.Node node, StringBuilder predicate,
                              Map<Node, Node> result) {
        if (node instanceof Text) {
            String literalValue = ((Text) node).getTextContent();

            result.put(
                    Node.createURI(predicate.toString()), Node.createLiteral(
                            literalValue, findDatatype(literalValue)));
        } else {
            if (predicate.length() > 0) {
                predicate.append(WsNotificationTranslatorConstants.URI_SEPARATOR);
            }

            if (node.getNamespaceURI() != null) {
                predicate.append(node.getNamespaceURI());
                predicate.append("/");
            }
            predicate.append(node.getNodeName());

            // gets the children and call the method recursively
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                this.parseElement(
                        nodes.item(i), new StringBuilder(predicate), result);
            }
        }
    }

    /**
     * Finds the {@link XSDDatatype} associated to the specified {@code literal}
     * value for simple types such that int, float, datetime and strings.
     * 
     * @param literal
     *            the literal value to parse.
     * 
     * @return the {@link XSDDatatype} associated to the specified
     *         {@code literal} value for simple types such that int, float,
     *         datetime and strings.
     */
    private static XSDDatatype findDatatype(String literal) {
        try {
            Integer.parseInt(literal);
            return XSDDatatype.XSDint;
        } catch (NumberFormatException nfe) {
            try {
                Float.parseFloat(literal);
                return XSDDatatype.XSDfloat;
            } catch (NumberFormatException nfe2) {
                try {
                    DatatypeFactory.newInstance().newXMLGregorianCalendar(
                            literal).toGregorianCalendar().getTime();
                    return XSDDatatype.XSDdateTime;
                } catch (DatatypeConfigurationException e) {
                    return XSDDatatype.XSDstring;
                } catch (IllegalArgumentException iae) {
                    return XSDDatatype.XSDstring;
                }
            }
        }
    }

}
