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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorConstants;

/**
 * Translator for {@link CompoundEvent events} to {@link NotificationMessageHolderType
 * notification messages}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class EventToNotificationMessageTranslator {

    // used to have the possibility to create DOM elements
    private static Document document;

    static {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = dbfac.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        document = docBuilder.newDocument();
    }

    /**
     * Translates the specified event to its corresponding notification message.
     * 
     * @param event
     *            the event to be translated.
     * @return the notification message corresponding to the specified event.
     */
    public NotificationMessageHolderType translate(CompoundEvent event) {
        String subscriptionAddress = null;
        String producerAddress = null;
        List<Element> metadatas = new ArrayList<Element>();
        Element messagePayload = null;

        String topic = null;
        String eventId = null;

        for (Quadruple quad : event.getQuadruples()) {
            if (quad.getPredicate()
                    .equals(
                            Node.createURI(EventCloudProperties.EVENT_CLOUD_NS.getValue()
                                    + "event/" + "nbquads"))) {
                continue;
            }

            String predicateValue = quad.getPredicate().getURI();

            if (predicateValue.equals(WsNotificationTranslatorConstants.SUBSCRIPTION_ADDRESS_TEXT)) {
                subscriptionAddress = quad.getObject().getLiteralLexicalForm();
            } else if (predicateValue.equals(WsNotificationTranslatorConstants.TOPIC_TEXT)) {
                String value = quad.getObject().getLiteralLexicalForm();
                int start = value.lastIndexOf("/") + 1;
                topic = value.substring(start);
            } else if (predicateValue.equals(WsNotificationTranslatorConstants.PRODUCER_ADDRESS_TEXT)) {
                producerAddress = quad.getObject().getLiteralLexicalForm();
            } else if (predicateValue.startsWith(WsNotificationTranslatorConstants.PRODUCER_METADATA_TEXT)) {
                metadatas.add(this.getMetadataElement(quad));
            } else if (predicateValue.startsWith(WsNotificationTranslatorConstants.MESSAGE_TEXT)) {
                messagePayload = this.createElement(quad, messagePayload);
            } else if (predicateValue.contains(WsNotificationTranslatorConstants.PRODUCER_METADATA_EVENT_NAMESPACE)) {
                eventId = quad.getObject().getLiteralLexicalForm();
            }
        }

        if (eventId == null) {
            eventId = event.getGraph().getURI();
        }

        metadatas.add(this.createMetadataElement(
                new QName(
                        WsNotificationTranslatorConstants.PRODUCER_METADATA_EVENT_NAMESPACE,
                        "id"), eventId));

        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();

        if (subscriptionAddress != null) {
            W3CEndpointReferenceBuilder endPointReferenceBuilder =
                    new W3CEndpointReferenceBuilder();
            endPointReferenceBuilder.address(subscriptionAddress);
            notificationMessage.setSubscriptionReference(endPointReferenceBuilder.build());
        }

        if (topic != null) {
            TopicExpressionType topicExpression = new TopicExpressionType();
            topicExpression.getContent().add(topic);
            notificationMessage.setTopic(topicExpression);
        }

        if (producerAddress != null) {
            W3CEndpointReferenceBuilder endPointReferenceBuilder =
                    new W3CEndpointReferenceBuilder();
            endPointReferenceBuilder.address(producerAddress);
            for (Element metadata : metadatas) {
                endPointReferenceBuilder.metadata(metadata);
            }
            notificationMessage.setProducerReference(endPointReferenceBuilder.build());
        }

        if (messagePayload != null) {
            Message message = new Message();
            message.setAny(messagePayload);
            notificationMessage.setMessage(message);
        }

        return notificationMessage;
    }

    private Element createMetadataElement(QName qname, String value) {
        Element metadataElt =
                document.createElementNS(
                        WsNotificationTranslatorConstants.PRODUCER_METADATA_NAMESPACE,
                        "Metadata");
        Element childElt =
                document.createElementNS(
                        qname.getNamespaceURI(), qname.getLocalPart());
        Text textNode = document.createTextNode(value);

        metadataElt.appendChild(childElt);
        childElt.appendChild(textNode);

        return metadataElt;
    }

    private Element getMetadataElement(Quadruple quad) {
        return createElement(quad);
    }

    private static Element createElementFrom(String namespace, String localName) {
        if (namespace.isEmpty()) {
            return document.createElement(localName);
        } else {
            return document.createElementNS(namespace, localName);
        }
    }

    private static Element findByName(org.w3c.dom.Node node, String namespace,
                                      String localName) {
        if (node.hasChildNodes()) {
            if (namespace != null) {
                if (node.getNodeName().equals(localName)
                        && namespace.equals(node.getNamespaceURI())) {
                    return (Element) node;
                } else {
                    NodeList list = node.getChildNodes();
                    for (int i = 0; i < list.getLength(); i++) {
                        Element elt =
                                findByName(list.item(0), namespace, localName);
                        if (elt != null) {
                            return elt;
                        }
                    }
                }
            } else {
                if (node.getNodeName().equals(localName)) {
                    return (Element) node;
                } else {
                    NodeList list = node.getChildNodes();
                    for (int i = 0; i < list.getLength(); i++) {
                        Element elt =
                                findByName(list.item(0), namespace, localName);
                        if (elt != null) {
                            return elt;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Creates an XML tree (represented by its root {@link Element}) from the
     * specified {@code quadruple} and {@code prevRootElt}. The
     * {@code prevRootElt} element stands for the XML tree to which the new
     * translated quadruple must be appended.
     * 
     * @param quadruple
     *            the quadruple value to parse.
     * @param prevRootElt
     *            the XML tree to which the parsed quadruple must be appended.
     * 
     * @return an XML tree (represented by its root {@link Element}) from the
     *         specified {@code quadruple} and {@code prevRootElt}. The
     *         {@code prevRootElt} element stands for the XML tree to which the
     *         new translated quadruple must be appended.
     */
    private Element createElement(Quadruple quadruple, Element prevRootElt) {
        String[] elements = getXmlElements(quadruple);

        Element rootElt = prevRootElt;
        Element lastElt = null;

        // rootElt is not null -> it is not the first time we read a quadruple
        // that belongs to the wsnt:Message payload
        if (rootElt != null) {
            // iterates on the String elements extracted from the predicate
            // we start from 1 because if the previous root element is not
            // empty it means that the wsnt:Message has been already created
            for (int i = 0; i < elements.length; i++) {
                // splits the uri into namespace + localname
                String[] parts = splitUri(elements[i]);
                String namespace = parts[0];
                String localName = parts[1];

                // NodeList elts =
                // namespace.isEmpty()
                // ? rootElt.getElementsByTagName(localName)
                // : rootElt.getElementsByTagNameNS(
                // namespace, localName);
                Element eltFound = findByName(rootElt, namespace, localName);

                if (eltFound == null) {
                    // here we assume we have only one element that matches
                    Element elt =
                            namespace.isEmpty()
                                    ? document.createElement(localName)
                                    : document.createElementNS(
                                            namespace, localName);
                    lastElt.appendChild(elt);
                    lastElt = elt;

                    if (i == elements.length - 1) {
                        lastElt.appendChild(document.createTextNode(quadruple.getObject()
                                .getLiteralLexicalForm()));
                    }
                } else {
                    lastElt = eltFound;
                }
            }
        } else {
            rootElt = this.createElement(quadruple);
        }

        return rootElt;
    }

    /**
     * Creates an XML tree (represented by its root {@link Element}) from the
     * specified {@code quadruple} value. This creation consists retrieving the
     * predicate value which contains the concatenated values of the elements
     * corresponding to the XML tree to reconstruct. Then, when a leaf element
     * is created, a new text node is appended. This text node value will
     * contain the lexical literal value of the specified {@code quadruple}.
     * 
     * @param quadruple
     *            the quadruple value to parse.
     * 
     * @return an XML tree (represented by its root {@link Element}) from the
     *         specified {@code quadruple} value. This creation consists
     *         retrieving the predicate value which contains the concatenated
     *         values of the elements corresponding to the XML tree to
     *         reconstruct. Then, when a leaf element is created, a new text
     *         node is appended. This text node value will contain the lexical
     *         literal value of the specified {@code quadruple}.
     */
    private Element createElement(Quadruple quadruple) {
        String[] elements = getXmlElements(quadruple);

        String[] parts = splitUri(elements[0]);
        String namespace = parts[0];
        String localName = parts[1];
        Element rootElt = createElementFrom(namespace, localName);
        Element lastElt = rootElt;

        // iterates on the String elements extracted from the predicate
        for (int i = 1; i < elements.length; i++) {
            parts = splitUri(elements[i]);
            namespace = parts[0];
            localName = parts[1];

            Element elt =
                    namespace.isEmpty()
                            ? document.createElement(localName)
                            : document.createElementNS(namespace, localName);
            lastElt.appendChild(elt);
            lastElt = elt;

            if (i == elements.length - 1) {
                lastElt.appendChild(document.createTextNode(quadruple.getObject()
                        .getLiteralLexicalForm()));
            }
        }

        return rootElt;
    }

    private static String[] getXmlElements(Quadruple quadruple) {
        String[] elements =
                quadruple.getPredicate()
                        .getURI()
                        .split(
                                Pattern.quote(WsNotificationTranslatorConstants.URI_SEPARATOR));

        if (elements[0].startsWith(WsNotificationTranslatorConstants.MESSAGE_TEXT)) {
            elements = Arrays.copyOfRange(elements, 1, elements.length);
        }

        return elements;
    }

    private static String[] splitUri(String uri) {
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }

        int slashIndex = uri.lastIndexOf('/');

        if (slashIndex == -1) {
            return new String[] {"", uri};
        } else {
            return new String[] {
                    uri.substring(0, slashIndex), uri.substring(slashIndex + 1)};
        }
    }

}
