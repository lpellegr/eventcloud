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
package fr.inria.eventcloud.translators.wsnotif.notify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
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
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.Generator;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorConstants;
import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * Translator for {@link NotificationMessageHolderType notification messages} to
 * {@link Event events}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class NotificationMessageToEventTranslator {

    private static Logger log =
            LoggerFactory.getLogger(NotificationMessageToEventTranslator.class);

    /**
     * This method removes all white spaces between > < elements for a node
     * 
     * @param incomingNode
     * @return
     */
    public static org.w3c.dom.Node removeWhiteSpacesFromNode(org.w3c.dom.Node incomingNode) {
        try {
            byte[] nodeBytes = xmlNodeToByteArray(incomingNode);
            String nodeString = new String(nodeBytes, "UTF-8");
            nodeString = nodeString.replaceAll(">\\s*<", "><");
            incomingNode = byteArrayToXmlNode(nodeString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
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
    public Event translate(NotificationMessageHolderType notificationMessage) {
        List<Quadruple> quads = new ArrayList<Quadruple>();
        String eventId = null;
        Node eventIdNode = null;
        Node subjectNode = null;
        Node subscriptionAddressNode = null;
        Node topicNode = null;
        Node producerAddressNode = null;
        Map<Node, Node> producerMetadataNodes = new HashMap<Node, Node>();
        Map<Node, Node> messageNodes = new HashMap<Node, Node>();

        logNotificationMessage(notificationMessage);

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
                producerMetadataNodes = parseElements(producerMetadataElements);

                // creates the event identifier by trying to retrieve it from
                // the
                // metadata part. If it is not available, a random identifier is
                // created

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
                    Node.createURI(Generator.generateRandomUri().toString());
        }

        Message message = notificationMessage.getMessage();
        messageNodes = parseElement((Element) message.getAny(), false);

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

            return new Event(new Collection<Quadruple>(quads));
        } else {
            log.error("Cannot construct Event because no subject can be extracted from the notification message");
            return null;
        }
    }

    private static void logNotificationMessage(NotificationMessageHolderType msg) {
        logW3CEndpointReference(msg.getSubscriptionReference(), "subscriber");

        TopicExpressionType topicType = msg.getTopic();
        if (topicType != null) {
            List<Object> topicContent = topicType.getContent();
            if (topicContent != null) {
                log.info("topicContent(dialect={}) :", topicType.getDialect());
                for (Object obj : topicContent) {
                    log.info("  {} (class {})", obj, obj.getClass().getName());
                }

                logAttributes("topicAttribute", topicType, "otherAttributes");
            } else {
                log.info("topicContent is null");
            }
        } else {
            log.info("topicType is null");
        }

        logW3CEndpointReference(msg.getProducerReference(), "producer");

        Message message = msg.getMessage();

        if (message != null) {
            if (message.getAny() instanceof Element) {
                log.info(
                        "message any is:\n{} ",
                        asString((Element) message.getAny()));
            } else {
                if (message.getAny() == null) {
                    log.info("message any is null");
                } else {
                    log.info("message any class type is {} ", message.getAny()
                            .getClass()
                            .getName());
                }
            }
        } else {
            log.info("message is null");
        }
    }

    @SuppressWarnings("unchecked")
    private static void logW3CEndpointReference(W3CEndpointReference ref,
                                                String type) {
        if (ref != null) {
            Object address = ReflectionUtils.getFieldValue(ref, "address");
            if (address != null) {
                log.info(
                        "type={}, address={}", type,
                        ReflectionUtils.getFieldValue(address, "uri"));
                logAttributes(
                        type + " Address Attributes", address, "attributes");
            } else {
                log.info("type={}, address is null", type);
            }

            // referenceParameters

            Object metadata = ReflectionUtils.getFieldValue(ref, "metadata");
            if (metadata != null) {
                Object metadataElts =
                        ReflectionUtils.getFieldValue(metadata, "elements");

                if (metadataElts != null) {
                    log.info("type={}, metadata=", type);
                    for (Element elt : (List<Element>) metadataElts) {
                        log.info("  {} ", asString(elt));
                    }
                } else {
                    log.info("type={}, metadata elements is null", type);
                }

                logAttributes(
                        type + " metadata Elements Attributes", metadata,
                        "attributes");
            } else {
                log.info("type={}, metadata is null", type);
            }

            logAttributes(type + " Attributes", ref, "attributes");

            Object elements = ReflectionUtils.getFieldValue(ref, "elements");
            if (elements != null) {
                log.info("type={}, elements=", type);
                for (Element elt : (List<Element>) elements) {
                    log.info("  {} ", asString(elt));
                }
            } else {
                log.info("type={}, elements is null", type);
            }
        } else {
            log.info("type={} is null", type);
        }
    }

    @SuppressWarnings("unchecked")
    private static void logAttributes(String type, Object obj, String fieldName) {
        Map<QName, String> attributes =
                (Map<QName, String>) ReflectionUtils.getFieldValue(
                        obj, fieldName);

        if (attributes != null) {
            for (Entry<QName, String> entry : attributes.entrySet()) {
                log.info("type={}, attributes=<{}, {}>", new Object[] {
                        type, entry.getKey(), entry.getValue()});
            }
        } else {
            log.info("type={}, attributes is null", type);
        }
    }

    public static String asString(Element elt) {
        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = null;
        try {
            trans = transfac.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");

        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(elt);
        try {
            trans.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return sw.toString();
    }

    private Map<Node, Node> parseElements(List<Element> elements) {
        Map<Node, Node> elementNodes = new HashMap<Node, Node>();

        if (elements != null) {
            for (Element element : elements) {
                elementNodes.putAll(parseElement(element, true));
            }
        }

        return elementNodes;
    }

    private Map<Node, Node> parseElement(Element element, boolean isMetadata) {
        Map<Node, Node> result = new HashMap<Node, Node>();

        if (element != null) {
            this.parseElement(
                    removeWhiteSpacesFromNode(element), new StringBuilder(),
                    result, isMetadata);
        }

        return result;
    }

    private void parseElement(org.w3c.dom.Node node, StringBuilder predicate,
                              Map<Node, Node> result, boolean metadata) {
        if (!node.hasChildNodes()) {

            String literalValue = ((Text) node).getTextContent();

            if (metadata) {
                result.put(
                        Node.createURI(predicate.toString()),
                        Node.createLiteral(
                                literalValue, findDatatype(literalValue)));
            } else {
                result.put(
                        Node.createURI(WsNotificationTranslatorConstants.MESSAGE_TEXT
                                + WsNotificationTranslatorConstants.URI_SEPARATOR
                                + predicate.toString()), Node.createLiteral(
                                literalValue, findDatatype(literalValue)));
            }

            // result.put(
            // Node.createURI(predicate.toString()), Node.createLiteral(
            // literalValue, findDatatype(literalValue)));
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
