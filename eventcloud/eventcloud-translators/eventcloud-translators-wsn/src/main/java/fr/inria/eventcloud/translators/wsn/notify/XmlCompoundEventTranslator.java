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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.hp.hpl.jena.graph.NodeFactory;

import eu.play_project.play_commons.constants.Stream;
import eu.play_project.play_commons.eventformat.xml.DocumentBuilder;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.translators.wsn.Translator;
import fr.inria.eventcloud.translators.wsn.WsnConstants;

/**
 * Translator for {@link CompoundEvent compound events} to
 * {@link NotificationMessageHolderType WS-Notification messages}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class XmlCompoundEventTranslator extends
        Translator<CompoundEvent, NotificationMessageHolderType> {

    /**
     * Creates a {@link XmlCompoundEventTranslator}.
     */
    public XmlCompoundEventTranslator() {

    }

    /**
     * Translates the specified {@link CompoundEvent compound event} to its
     * corresponding {@link NotificationMessageHolderType WS-Notification
     * message}.
     * 
     * @param event
     *            the compound event to be translated.
     * 
     * @return the WS-Notification message corresponding to the specified
     *         compound event.
     */
    @Override
    public NotificationMessageHolderType translate(CompoundEvent event)
            throws TranslationException {
        Document document = DocumentBuilder.createDocument();
        String topic = null;
        String producerAddress = null;
        List<Element> metadatas = new ArrayList<Element>();
        Element messagePayload = null;
        String eventId = null;
        boolean hasSimpleExpressionType = false;

        for (Quadruple quad : event) {
            if (quad.getPredicate()
                    .equals(
                            NodeFactory.createURI(PublishSubscribeConstants.EVENTCLOUD_NS
                                    + "event/" + "nbquads"))) {
                continue;
            }

            String predicateValue = quad.getPredicate().getURI();

            if (predicateValue.equals(WsnConstants.TOPIC_TEXT)) {
                String value = quad.getObject().getURI();
                int start = value.lastIndexOf("/") + 1;
                int end = value.lastIndexOf(Stream.STREAM_ID_SUFFIX);
                topic = value.substring(start, end);
            } else if (predicateValue.equals(WsnConstants.PRODUCER_ADDRESS_TEXT)) {
                producerAddress = quad.getObject().getLiteralLexicalForm();
            } else if (predicateValue.startsWith(WsnConstants.PRODUCER_METADATA_TEXT)) {
                metadatas.add(this.getMetadataElement(document, quad));
            } else if (predicateValue.startsWith(WsnConstants.MESSAGE_TEXT)) {
                messagePayload =
                        this.createElement(document, quad, messagePayload);
            } else if (predicateValue.contains(WsnConstants.PRODUCER_METADATA_EVENT_NAMESPACE)) {
                eventId = quad.getObject().getLiteralLexicalForm();
            }
        }

        if (eventId == null) {
            eventId = event.getGraph().getURI();
        }

        if (eventId.endsWith(WsnConstants.SIMPLE_TOPIC_EXPRESSION_MARKER)) {
            hasSimpleExpressionType = true;
            eventId =
                    eventId.substring(
                            0,
                            eventId.lastIndexOf(WsnConstants.SIMPLE_TOPIC_EXPRESSION_MARKER));
        }

        if (eventId.endsWith(WsnConstants.XML_TRANSLATION_MARKER)) {
            eventId =
                    eventId.substring(
                            0,
                            eventId.lastIndexOf(WsnConstants.XML_TRANSLATION_MARKER));
        }

        metadatas.add(this.createMetadataElement(document, new QName(
                WsnConstants.PRODUCER_METADATA_EVENT_NAMESPACE, "id"), eventId));

        NotificationMessageHolderType notificationMessage =
                new NotificationMessageHolderType();

        if (topic != null) {
            TopicExpressionType topicExpression = new TopicExpressionType();
            if (hasSimpleExpressionType) {
                JAXBElement<QName> simpleTopicExpression =
                        new JAXBElement<QName>(
                                WsnConstants.SIMPLE_TOPIC_EXPRESSION_QNAME,
                                QName.class, null, new QName(topic));
                topicExpression.getContent().add(simpleTopicExpression);
            } else {
                topicExpression.getContent().add(topic);
            }
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

    private Element createMetadataElement(Document document, QName qname,
                                          String value) {
        Element metadataElt =
                document.createElementNS(
                        WsnConstants.PRODUCER_METADATA_NAMESPACE, "Metadata");
        Element childElt =
                document.createElementNS(
                        qname.getNamespaceURI(), qname.getLocalPart());
        Text textNode = document.createTextNode(value);

        metadataElt.appendChild(childElt);
        childElt.appendChild(textNode);

        return metadataElt;
    }

    private Element getMetadataElement(Document document, Quadruple quad) {
        return this.createElement(document, quad);
    }

    /*
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
    private Element createElement(Document document, Quadruple quadruple,
                                  Element prevRootElt) {
        String[] elements = this.getXmlElements(quadruple);

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
                String[] parts = this.splitUri(elements[i]);
                String namespace = parts[0];
                String localName = parts[1];

                // NodeList elts =
                // namespace.isEmpty()
                // ? rootElt.getElementsByTagName(localName)
                // : rootElt.getElementsByTagNameNS(
                // namespace, localName);
                Element eltFound =
                        this.findByName(rootElt, namespace, localName);

                if (eltFound == null) {
                    // here we assume we have only one element that matches
                    Element elt =
                            this.createElementFrom(
                                    document, namespace, localName);
                    lastElt.appendChild(elt);
                    lastElt = elt;

                    if (i == elements.length - 1) {
                        lastElt.appendChild(document.createTextNode(quadruple.getObject()
                                .getLiteralLexicalForm()
                                .replaceAll(WsnConstants.SHARP_ESCAPE, "#")));
                    }
                } else {
                    lastElt = eltFound;
                }
            }
        } else {
            rootElt = this.createElement(document, quadruple);
        }

        return rootElt;
    }

    private Element findByName(org.w3c.dom.Node node, String namespace,
                               String localName) {
        if (node.hasChildNodes()) {
            if (namespace != null) {
                if (namespace.equals(node.getNamespaceURI())
                        && localName.equals(node.getNodeName())) {
                    return (Element) node;
                } else {
                    NodeList list = node.getChildNodes();
                    for (int i = 0; i < list.getLength(); i++) {
                        Element elt =
                                this.findByName(
                                        list.item(i), namespace, localName);
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
                                this.findByName(
                                        list.item(i), namespace, localName);
                        if (elt != null) {
                            return elt;
                        }
                    }
                }
            }
        }

        return null;
    }

    /*
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
    private Element createElement(Document document, Quadruple quadruple) {
        String[] elements = this.getXmlElements(quadruple);

        String[] parts = this.splitUri(elements[0]);
        String namespace = parts[0];
        String localName = parts[1];
        Element rootElt =
                this.createElementFrom(document, namespace, localName);
        Element lastElt = rootElt;

        // iterates on the String elements extracted from the predicate
        for (int i = 1; i < elements.length; i++) {
            parts = this.splitUri(elements[i]);
            namespace = parts[0];
            localName = parts[1];

            Element elt =
                    this.createElementFrom(document, namespace, localName);
            lastElt.appendChild(elt);
            lastElt = elt;

            if (i == elements.length - 1) {
                lastElt.appendChild(document.createTextNode(quadruple.getObject()
                        .getLiteralLexicalForm()
                        .replaceAll(WsnConstants.SHARP_ESCAPE, "#")));
            }
        }

        return rootElt;
    }

    private String[] getXmlElements(Quadruple quadruple) {
        String[] elements =
                quadruple.getPredicate().getURI().replaceAll(
                        WsnConstants.SHARP_ESCAPE, "#").split(
                        Pattern.quote(WsnConstants.URI_SEPARATOR));

        if (elements[0].startsWith(WsnConstants.MESSAGE_TEXT)) {
            elements = Arrays.copyOfRange(elements, 1, elements.length);
        }

        return elements;
    }

    private Element createElementFrom(Document document, String namespace,
                                      String localName) {
        if (namespace.isEmpty()) {
            return document.createElement(localName);
        } else {
            return document.createElementNS(namespace, localName);
        }
    }

    private String[] splitUri(String uri) {
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
