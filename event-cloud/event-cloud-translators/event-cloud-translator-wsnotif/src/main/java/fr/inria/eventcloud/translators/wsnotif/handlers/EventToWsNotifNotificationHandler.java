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

import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslator;

/**
 * Handler that is used to translate an Event to a WS-Notification notification
 * payload (which is a SOAP-XML payload).
 * 
 * @author lpellegr
 */
public class EventToWsNotifNotificationHandler {

    private static final String legalCharacters = "abcdefghijklmnopqrstuvwxyz";

    // contains the xml sub tree and the element created for this sub tree
    // /a/b/c -> c element
    private Map<String, Element> xmlElements;

    // contains the prefixes and their associated namespace value
    // prefix -> namespace
    private Map<String, String> prefixes;

    // contains the namespaces and their associated prefix value
    // namespace -> prefix
    private Map<String, String> namespaces;

    public EventToWsNotifNotificationHandler() {
        this.xmlElements = new HashMap<String, Element>();
        this.prefixes = new HashMap<String, String>();
        this.namespaces = new HashMap<String, String>();
    }

    public void translate(OutputStream out, Event event) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        DOMImplementation domImpl = builder.getDOMImplementation();
        Document xmlDoc = domImpl.createDocument(null, null, null);

        for (Quadruple quad : event.getQuadruples()) {
            if (quad.getPredicate()
                    .equals(
                            Node.createURI(EventCloudProperties.EVENT_CLOUD_NS.getValue()
                                    + "event/" + "nbquads"))) {
                continue;
            }

            String predicateValue = quad.getPredicate().getURI();

            String[] elements =
                    predicateValue.split(Pattern.quote(WsNotificationTranslator.URI_SEPARATOR));

            String lastElementRead = null;

            for (String element : elements) {
                // pln[0] -> namespace
                // pln[1] -> localName
                String[] pln = XmlUtils.splitUri(element);

                Element elt =
                        this.xmlElements.get(subTree(element, predicateValue));

                // the element does not exist in the XML tree
                if (elt == null) {
                    Element newElt = null;
                    // element with no namespace
                    if (pln[0].isEmpty()) {
                        newElt = xmlDoc.createElement(pln[1]);
                    } else if (this.namespaces.containsKey(pln[0])) {
                        // element with a namespace whose a prefix already exist
                        newElt =
                                xmlDoc.createElementNS(
                                        pln[0], XmlUtils.createQName(
                                                this.namespaces.get(pln[0]),
                                                pln[1]));
                    } else {
                        // element with a namespace with no existing prefix
                        String prefix =
                                this.generateUniquePrefix(this.prefixes.keySet());
                        this.namespaces.put(pln[0], prefix);
                        this.prefixes.put(prefix, pln[0]);
                        newElt =
                                xmlDoc.createElementNS(
                                        pln[0], XmlUtils.createQName(
                                                prefix, pln[1]));
                    }

                    this.xmlElements.put(
                            subTree(element, predicateValue), newElt);

                    if (lastElementRead == null) {
                        xmlDoc.appendChild(newElt);
                    } else {
                        this.xmlElements.get(lastElementRead).appendChild(
                                newElt);
                        if (predicateValue.endsWith(element)) {
                            newElt.appendChild(xmlDoc.createTextNode(quad.getObject()
                                    .getLiteralLexicalForm()));
                        }
                    }
                }

                lastElementRead = subTree(element, predicateValue);
            }
        }

        DOMSource domSource = new DOMSource(xmlDoc);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }

        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        try {
            transformer.transform(domSource, new StreamResult(out));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private String generateUniquePrefix(Set<String> keySet) {
        SecureRandom random = new SecureRandom();
        StringBuilder prefix = new StringBuilder();

        for (int i = 0; i < 1 + random.nextInt(3); i++) {
            prefix.append(legalCharacters.charAt(random.nextInt(legalCharacters.length())));
        }

        if (keySet.contains(prefix.toString())) {
            return this.generateUniquePrefix(keySet);
        } else {
            return prefix.toString();
        }
    }

    private static String subTree(String elt, String tree) {
        return tree.substring(0, tree.lastIndexOf(elt) + elt.length());
    }

}
