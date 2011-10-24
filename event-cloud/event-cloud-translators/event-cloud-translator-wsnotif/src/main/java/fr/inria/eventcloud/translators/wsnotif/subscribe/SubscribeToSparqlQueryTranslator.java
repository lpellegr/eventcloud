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
package fr.inria.eventcloud.translators.wsnotif.subscribe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
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
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fr.inria.eventcloud.translators.wsnotif.WsNotificationTranslatorConstants;
import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * Translator for {@link Subscribe subscribe notifications} to SPARQL queries.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class SubscribeToSparqlQueryTranslator {

    private static Logger log =
            LoggerFactory.getLogger(SubscribeToSparqlQueryTranslator.class);

    /**
     * Translates the specified subscribe notification to its corresponding
     * SPARQL query.
     * 
     * @param subscribe
     *            the subscribe notification to be translated.
     * 
     * @return the SPARQL query corresponding to the specified subscribe
     *         notification.
     */
    @SuppressWarnings("unchecked")
    public String translate(Subscribe subscribe) {
        logSubscribe(subscribe);

        FilterType filterType = subscribe.getFilter();
        if (filterType != null) {
            List<Object> any = filterType.getAny();
            if (any.size() > 0) {
                try {
                    TopicExpressionType topicExpressionType = null;
                    if (any.get(0) instanceof JAXBElement) {
                        topicExpressionType =
                                ((JAXBElement<TopicExpressionType>) any.get(0)).getValue();
                    } else {
                        JAXBContext jc;
                        try {
                            jc =
                                    JAXBContext.newInstance(TopicExpressionType.class.getName());
                            Unmarshaller u = jc.createUnmarshaller();

                            DocumentBuilderFactory dbf =
                                    DocumentBuilderFactory.newInstance();
                            dbf.setNamespaceAware(true);
                            DocumentBuilder db = dbf.newDocumentBuilder();

                            ByteArrayOutputStream baos =
                                    new ByteArrayOutputStream();
                            ObjectOutputStream oos =
                                    new ObjectOutputStream(baos);
                            oos.writeObject(any.get(0));

                            Document doc =
                                    db.parse(new ByteArrayInputStream(
                                            baos.toByteArray()));
                            Object o = u.unmarshal(doc);

                            topicExpressionType = (TopicExpressionType) o;
                        } catch (JAXBException e) {
                            e.printStackTrace();
                        } catch (ParserConfigurationException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
                    }

                    List<Object> content = topicExpressionType.getContent();
                    if (content.size() > 0) {
                        return "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { <"
                                + WsNotificationTranslatorConstants.DEFAULT_TOPIC_NAMESPACE
                                + "/"
                                + ((String) content.get(0)).trim().replaceAll(
                                        "\n", "") + "> ?p ?o . } }";
                    } else {
                        log.error("No topic content set in the subscribe notification");
                    }
                } catch (ClassCastException cce) {
                    cce.printStackTrace();
                }
            } else {
                log.error("No any object set in the subscribe notification");
            }
        } else {
            log.error("No filter set in the subscribe notification");
        }

        return null;
    }

    private static void logSubscribe(Subscribe subscribe) {
        logW3CEndpointReference(subscribe.getConsumerReference(), "consumer");

        List<Object> any = subscribe.getAny();
        if (any != null) {
            log.info("any values are:");
            for (Object obj : any) {
                log.info("  {} (class {})", obj, obj.getClass().getName());
            }
        } else {
            log.info("any is null");
        }

        FilterType filterType = subscribe.getFilter();

        if (filterType != null) {
            any = filterType.getAny();
            if (any != null) {
                log.info("filter type any values are:");
                for (Object obj : any) {
                    if (obj != null) {
                        if (obj instanceof TopicExpressionType) {
                            TopicExpressionType topicType =
                                    (TopicExpressionType) obj;
                            List<Object> topicContent = topicType.getContent();
                            if (topicContent != null) {
                                log.info(
                                        "filter type topicContent(dialect={}) :",
                                        topicType.getDialect());
                                for (Object obj2 : topicContent) {
                                    log.info(
                                            "  {} (class {})", obj2,
                                            obj2.getClass().getName());
                                }

                                logAttributes(
                                        "filter type topicAttribute",
                                        topicType, "otherAttributes");
                            } else {
                                log.info("filter type topicContent is null");
                            }
                        } else {
                            log.info("  {} (class {})", obj, obj.getClass()
                                    .getName());
                        }
                    } else {
                        log.info("filter type topicType is null");
                    }
                }
            } else {
                log.info("filter type any is null");
            }
        } else {
            log.info("filter type is null");
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

    private static String asString(Element elt) {
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

}
