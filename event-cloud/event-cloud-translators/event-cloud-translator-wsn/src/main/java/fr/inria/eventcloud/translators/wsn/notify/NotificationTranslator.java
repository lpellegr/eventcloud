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
package fr.inria.eventcloud.translators.wsn.notify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.UriGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;
import fr.inria.eventcloud.translators.wsn.WsNotificationTranslatorConstants;
import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * Translator for {@link NotificationMessageHolderType} notification messages to
 * {@link CompoundEvent events}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class NotificationTranslator extends
        Translator<NotificationMessageHolderType, CompoundEvent> {

    private static Logger log =
            LoggerFactory.getLogger(NotificationTranslator.class);

    /**
     * This method removes all white spaces between > < elements for a node
     * 
     * @param incomingNode
     * 
     * @return xml tree without white spaces between nodes.
     */
    public static org.w3c.dom.Node removeWhiteSpacesAndSharpFromNode(org.w3c.dom.Node incomingNode) {
        try {
            byte[] nodeBytes = xmlNodeToByteArray(incomingNode);
            String nodeString = new String(nodeBytes, "UTF-8");
            nodeString = nodeString.replaceAll(">\\s*<", "><");
            nodeString =
                    nodeString.replaceAll(
                            "#", WsNotificationTranslatorConstants.SHARP_ESCAPE);
            incomingNode = byteArrayToXmlNode(nodeString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return incomingNode;
    }

    public static byte[] xmlNodeToByteArray(org.w3c.dom.Node node) {
        try {
            Source source = new DOMSource(node);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Result result = new StreamResult(out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return out.toByteArray();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static org.w3c.dom.Node byteArrayToXmlNode(byte[] xml)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml))
                .getDocumentElement();
    }

    /**
     * Translates the specified notification message to its corresponding event.
     * 
     * @param notificationMessage
     *            the notification message to be translated.
     * 
     * @return the event corresponding to the specified notification message.
     */
    @Override
    public CompoundEvent translate(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        List<Quadruple> quads = new ArrayList<Quadruple>();
        String eventId = null;
        Node eventIdNode = null;
        Node subjectNode = null;
        Node subscriptionAddressNode = null;
        Node topicNode = null;
        Node producerAddressNode = null;
        Map<Node, Node> producerMetadataNodes = new HashMap<Node, Node>();
        Map<Node, Node> messageNodes = new HashMap<Node, Node>();

        W3CEndpointReference subscriptionReference =
                notificationMessage.getSubscriptionReference();
        if (subscriptionReference != null) {
            Object subscriptionAddress =
                    ReflectionUtils.getFieldValue(
                            subscriptionReference, "address");
            if (subscriptionAddress != null) {
                String subscriptionAddressUri =
                        (String) ReflectionUtils.getFieldValue(
                                subscriptionAddress, "uri");
                if (subscriptionAddressUri != null) {
                    subscriptionAddressNode =
                            Node.createLiteral(subscriptionAddressUri);
                }
            }
        }

        TopicExpressionType topicExpressionType =
                notificationMessage.getTopic();
        if (topicExpressionType != null) {
            List<Object> topicContent = topicExpressionType.getContent();
            if (topicContent.size() > 0) {
                String topic = (String) topicContent.get(0);
                subjectNode =
                        Node.createURI(WsNotificationTranslatorConstants.DEFAULT_TOPIC_NAMESPACE
                                + "/" + topic);
                topicNode = Node.createLiteral(topic);
            }
        }

        W3CEndpointReference producerReference =
                notificationMessage.getProducerReference();
        if (producerReference != null) {
            Object producerAddress =
                    ReflectionUtils.getFieldValue(producerReference, "address");
            if (producerAddress != null) {
                String producerAddressUri =
                        (String) ReflectionUtils.getFieldValue(
                                producerAddress, "uri");
                if (producerAddressUri != null) {
                    producerAddressNode =
                            Node.createLiteral(producerAddressUri);
                }
            }

            Object metadata =
                    ReflectionUtils.getFieldValue(
                            notificationMessage.getProducerReference(),
                            "metadata");
            if (metadata != null) {
                @SuppressWarnings("unchecked")
                List<Element> producerMetadataElements =
                        (List<Element>) ReflectionUtils.getFieldValue(
                                metadata, "elements");
                producerMetadataNodes =
                        this.parseElements(producerMetadataElements);

                // creates the event identifier by trying to retrieve it from
                // the metadata part. If it is not available, a random
                // identifier is created

                // Map<predicate, object
                for (Entry<Node, Node> entry : producerMetadataNodes.entrySet()) {
                    if (entry.getKey()
                            .getURI()
                            .contains(
                                    WsNotificationTranslatorConstants.PRODUCER_METADATA_EVENT_NAMESPACE)) {
                        eventId = entry.getValue().getLiteralLexicalForm();
                        break;
                    }
                }
            }
        }

        if (eventId != null) {
            eventIdNode = Node.createURI(eventId);
        } else {
            eventIdNode =
                    Node.createURI(UriGenerator.randomPrefixed(
                            10,
                            EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue())
                            .toString());
        }

        Message message = notificationMessage.getMessage();
        if (message != null) {
            messageNodes = this.parseElement((Element) message.getAny(), false);
            if (messageNodes == null) {
                throw new TranslationException("messageNodes is null");
            }
        }

        if (subjectNode != null) {
            if (subscriptionAddressNode != null) {
                quads.add(new Quadruple(
                        eventIdNode,
                        subjectNode,
                        WsNotificationTranslatorConstants.SUBSCRIPTION_ADDRESS_NODE,
                        subscriptionAddressNode, false, true));
            } else {
                log.warn("No subscription reference address set in the notification message");
            }

            if (topicNode != null) {
                quads.add(new Quadruple(
                        eventIdNode, subjectNode,
                        WsNotificationTranslatorConstants.TOPIC_NODE,
                        topicNode, false, true));
            } else {
                log.warn("No topic set in the notification message");
            }

            if (producerAddressNode != null) {
                quads.add(new Quadruple(
                        eventIdNode,
                        subjectNode,
                        WsNotificationTranslatorConstants.PRODUCER_ADDRESS_NODE,
                        producerAddressNode, false, true));
            } else {
                log.warn("No producer reference address set in the notification message");
            }

            for (Entry<Node, Node> entry : producerMetadataNodes.entrySet()) {
                quads.add(new Quadruple(
                        eventIdNode, subjectNode, entry.getKey(),
                        entry.getValue(), false, true));
            }

            for (Entry<Node, Node> entry : messageNodes.entrySet()) {
                quads.add(new Quadruple(
                        eventIdNode, subjectNode, entry.getKey(),
                        entry.getValue(), false, true));
            }

            return new CompoundEvent(quads);
        } else {
            throw new TranslationException(
                    "Cannot construct compound event because no subject can be extracted from the notification message");
        }
    }

    private Map<Node, Node> parseElements(List<Element> elements) {
        Map<Node, Node> elementNodes = new HashMap<Node, Node>();

        if (elements != null) {
            for (Element element : elements) {
                Map<Node, Node> result = this.parseElement(element, true);
                if (result != null) {
                    elementNodes.putAll(result);
                } else {
                    return null;
                }
            }
        }

        return elementNodes;
    }

    private Map<Node, Node> parseElement(Element element, boolean isMetadata) {
        Map<Node, Node> result = new HashMap<Node, Node>();

        if (element != null) {
            org.w3c.dom.Node nn = removeWhiteSpacesAndSharpFromNode(element);
            if (nn != null) {
                this.parseElement(nn, new StringBuilder(), result, isMetadata);
            } else {
                return null;
            }
        }

        return result;
    }

    private void parseElement(org.w3c.dom.Node node, StringBuilder predicate,
                              Map<Node, Node> result, boolean metadata) {
        if (!node.hasChildNodes()
                && node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
            String literalValue = node.getNodeValue();

            Node predicateNode = null;
            if (!metadata) {
                predicateNode =
                        Node.createURI(WsNotificationTranslatorConstants.MESSAGE_TEXT
                                + WsNotificationTranslatorConstants.URI_SEPARATOR
                                + predicate.toString());
            } else {
                predicateNode = Node.createURI(predicate.toString());
            }

            result.put(predicateNode, Node.createLiteral(
                    literalValue, findDatatype(literalValue)));
        } else {
            if (predicate.length() > 0) {
                predicate.append(WsNotificationTranslatorConstants.URI_SEPARATOR);
            }

            if (node.getNamespaceURI() != null) {
                predicate.append(node.getNamespaceURI());
                predicate.append('/');
            }
            predicate.append(node.getNodeName());

            // gets the children and call the method recursively
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                this.parseElement(
                        nodes.item(i), new StringBuilder(predicate), result,
                        metadata);
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
