/**
 * Copyright (c) 2011-2013 INRIA.
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
import javax.xml.namespace.QName;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import eu.play_project.play_commons.constants.Stream;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.UriGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;
import fr.inria.eventcloud.translators.wsn.WsnConstants;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * Translator for {@link NotificationMessageHolderType WS-Notification messages}
 * to {@link CompoundEvent compound events}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class XmlNotificationTranslator extends
        Translator<NotificationMessageHolderType, CompoundEvent> {

    private static Logger log =
            LoggerFactory.getLogger(XmlNotificationTranslator.class);

    public XmlNotificationTranslator() {

    }

    /**
     * Translates the specified {@link NotificationMessageHolderType
     * WS-Notification message} to its corresponding {@link CompoundEvent
     * compound event}.
     * 
     * @param notificationMessage
     *            the WS-Notification message to be translated.
     * 
     * @return the compound event corresponding to the specified WS-Notification
     *         message.
     */
    @Override
    public CompoundEvent translate(NotificationMessageHolderType notificationMessage)
            throws TranslationException {
        List<Quadruple> quads = new ArrayList<Quadruple>();
        String eventId = null;
        Node eventIdNode = null;
        Node subjectNode = null;
        Node topicNode = null;
        Node producerAddressNode = null;
        Map<Node, Node> producerMetadataNodes = new HashMap<Node, Node>();
        Map<Node, Node> messageNodes = new HashMap<Node, Node>();

        if (notificationMessage.getTopic() != null) {
            QName topic = WsnHelper.getTopic(notificationMessage);
            String topicNamespace = topic.getNamespaceURI();
            if ((topicNamespace == null) || (topicNamespace.equals(""))) {
                // FIXME: a TranslationException should be thrown but
                // first the issue #43 has to be fixed
                log.warn("No namespace declared for prefix '"
                        + topic.getPrefix()
                        + "' associated to topic "
                        + topic
                        + " the default topic namespace will be used 'http://streams.event-processing.org/ids/'");

                topicNamespace = "http://streams.event-processing.org/ids/";
            }
            topicNode =
                    Node.createURI(topicNamespace + topic.getLocalPart()
                            + Stream.STREAM_ID_SUFFIX);
        } else {
            throw new TranslationException(
                    "No topic defined in the notify message");
        }

        W3CEndpointReference producerReference =
                notificationMessage.getProducerReference();
        String producerAddressUri = null;

        if (producerReference != null) {
            Object producerAddress =
                    ReflectionUtils.getFieldValue(producerReference, "address");
            if (producerAddress != null) {
                producerAddressUri =
                        (String) ReflectionUtils.getFieldValue(
                                producerAddress, "uri");
                if (producerAddressUri != null) {
                    producerAddressNode =
                            Node.createLiteral(producerAddressUri);
                } else {
                    throw new TranslationException("No producer uri specified");
                }
            } else {
                throw new TranslationException("No producer address specified");
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
                    if (entry.getKey().getURI().contains(
                            WsnConstants.PRODUCER_METADATA_EVENT_NAMESPACE)) {
                        eventId = entry.getValue().getLiteralLexicalForm();
                        break;
                    }
                }
            }
        } else {
            throw new TranslationException(
                    "No producer reference defined in the notify message");
        }

        if (eventId == null) {
            eventId =
                    UriGenerator.randomPrefixed(
                            10,
                            EventCloudProperties.EVENTCLOUD_ID_PREFIX.getValue())
                            .toString();
        }
        if (!eventId.contains(WsnConstants.XML_TRANSLATION_MARKER)) {
            eventId += WsnConstants.XML_TRANSLATION_MARKER;
        }
        if (WsnHelper.hasSimpleTopicExpression(notificationMessage)
                && !eventId.endsWith(WsnConstants.SIMPLE_TOPIC_EXPRESSION_MARKER)) {
            eventId += WsnConstants.SIMPLE_TOPIC_EXPRESSION_MARKER;
        }
        eventIdNode = Node.createURI(eventId);
        subjectNode = Node.createURI(eventId + "#event");

        Message message = notificationMessage.getMessage();
        if (message != null) {
            messageNodes = this.parseElement((Element) message.getAny(), false);
            if (messageNodes == null) {
                throw new TranslationException("messageNodes is null");
            }
        } else {
            throw new TranslationException(
                    "No any content specified in the notify message");
        }

        quads.add(new Quadruple(
                eventIdNode, subjectNode, WsnConstants.TOPIC_NODE, topicNode,
                false, true));

        quads.add(new Quadruple(
                eventIdNode, subjectNode, WsnConstants.PRODUCER_ADDRESS_NODE,
                producerAddressNode, false, true));

        for (Entry<Node, Node> entry : producerMetadataNodes.entrySet()) {
            quads.add(new Quadruple(
                    eventIdNode, subjectNode, entry.getKey(), entry.getValue(),
                    false, true));
        }

        for (Entry<Node, Node> entry : messageNodes.entrySet()) {
            quads.add(new Quadruple(
                    eventIdNode, subjectNode, entry.getKey(), entry.getValue(),
                    false, true));
        }

        if (producerAddressUri != null) {
            for (Quadruple q : quads) {
                q.setPublicationSource(producerAddressUri);
            }
        }

        return new CompoundEvent(quads);
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

    private static org.w3c.dom.Node removeWhiteSpacesAndSharpFromNode(org.w3c.dom.Node incomingNode) {
        try {
            byte[] nodeBytes = xmlNodeToByteArray(incomingNode);
            String nodeString = new String(nodeBytes, "UTF-8");
            nodeString = nodeString.replaceAll(">\\s*<", "><");
            nodeString = nodeString.replaceAll("#", WsnConstants.SHARP_ESCAPE);
            incomingNode = byteArrayToXmlNode(nodeString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return incomingNode;
    }

    private static byte[] xmlNodeToByteArray(org.w3c.dom.Node node) {
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

    private static org.w3c.dom.Node byteArrayToXmlNode(byte[] xml)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml))
                .getDocumentElement();
    }

    private void parseElement(org.w3c.dom.Node node, StringBuilder predicate,
                              Map<Node, Node> result, boolean metadata) {
        if (!node.hasChildNodes()
                && node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
            String literalValue = node.getNodeValue();

            Node predicateNode = null;
            if (!metadata) {
                predicateNode =
                        Node.createURI(WsnConstants.MESSAGE_TEXT
                                + WsnConstants.URI_SEPARATOR
                                + predicate.toString());
            } else {
                predicateNode = Node.createURI(predicate.toString());
            }

            result.put(predicateNode, Node.createLiteral(
                    literalValue, findDatatype(literalValue)));
        } else {
            if (predicate.length() > 0) {
                predicate.append(WsnConstants.URI_SEPARATOR);
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
