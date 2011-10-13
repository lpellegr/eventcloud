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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.w3c.dom.Element;

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
 */
public class NotificationMessageToEvent {

    /**
     * Translates the specified notification message to its corresponding event.
     * 
     * @param notificationMessage
     *            the notification message to be translated.
     * @return the event corresponding to the specified notification message.
     */
    @SuppressWarnings("unchecked")
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
                (String) this.getFieldValue(this.getFieldValue(
                        notificationMessage.getSubscriptionReference(),
                        "address"), "uri");
        subscriptionAddress =
                Node.createLiteral(
                        subscriptionAddressText,
                        this.getDatatype(subscriptionAddressText));

        List<Object> topicContent = notificationMessage.getTopic().getContent();
        if (topicContent.size() > 0) {
            topic =
                    Node.createLiteral(
                            (String) topicContent.get(0),
                            this.getDatatype((String) topicContent.get(0)));
        }

        String producerAddressText =
                (String) this.getFieldValue(this.getFieldValue(
                        notificationMessage.getSubscriptionReference(),
                        "address"), "uri");
        producerAddress =
                Node.createLiteral(
                        producerAddressText,
                        this.getDatatype(subscriptionAddressText));

        List<Element> producerMetadataElements =
                (List<Element>) this.getFieldValue(
                        this.getFieldValue(
                                notificationMessage.getProducerReference(),
                                "metadata"), "elements");
        producerMetadatas = parseElements(producerMetadataElements);

        Message message = notificationMessage.getMessage();
        messages = parseElement((Element) message.getAny());

        quads.add(new Quadruple(
                eventId, subjectNode,
                NotificationMessageConstants.SUBSCRIPTION_ADDRESS_NODE,
                subscriptionAddress, false, true));

        quads.add(new Quadruple(
                eventId, subjectNode, NotificationMessageConstants.TOPIC_NODE,
                topic, false, true));

        quads.add(new Quadruple(
                eventId, subjectNode,
                NotificationMessageConstants.PRODUCER_ADDRESS_NODE,
                producerAddress, false, true));

        for (Node producerMetadataPredicate : producerMetadatas.keySet()
                .toArray(new Node[] {})) {
            quads.add(new Quadruple(
                    eventId, subjectNode, producerMetadataPredicate,
                    producerMetadatas.get(producerMetadataPredicate), false,
                    true));
        }

        for (Node messagePredicate : messages.keySet().toArray(new Node[] {})) {
            quads.add(new Quadruple(
                    eventId, subjectNode, messagePredicate,
                    producerMetadatas.get(messagePredicate), false, true));
        }

        return new Event(new Collection<Quadruple>(quads));
    }

    private Object getFieldValue(Object object, String fieldName) {
        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException e) {
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
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

        return null;
    }

    private XSDDatatype getDatatype(String textNode) {
        // anticipate datatype
        XSDDatatype expectedType = new XSDDatatype("anySimpleType");
        DatatypeFactory dataFactory;
        try {
            dataFactory = DatatypeFactory.newInstance();

            try {
                Integer.parseInt(textNode);
                expectedType = XSDDatatype.XSDint;
            } catch (NumberFormatException nfe) {
                try {
                    Float.parseFloat(textNode);
                    expectedType = XSDDatatype.XSDfloat;
                } catch (NumberFormatException nfe2) {
                    try {
                        dataFactory.newXMLGregorianCalendar(textNode)
                                .toGregorianCalendar()
                                .getTime();
                        expectedType = XSDDatatype.XSDdateTime;
                    } catch (IllegalArgumentException iae) {
                        expectedType = XSDDatatype.XSDstring;
                    }
                }
            }
        } catch (DatatypeConfigurationException dce) {
            dce.printStackTrace();
        }

        return expectedType;
    }

}
