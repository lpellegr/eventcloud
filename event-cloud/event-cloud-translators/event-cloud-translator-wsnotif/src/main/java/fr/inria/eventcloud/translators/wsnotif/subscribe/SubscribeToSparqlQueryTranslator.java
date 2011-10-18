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

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import fr.inria.eventcloud.api.Event;

/**
 * Translator for {@link NotificationMessageHolderType notification messages} to
 * {@link Event events}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class SubscribeToSparqlQueryTranslator {

    private static Logger log =
            LoggerFactory.getLogger(SubscribeToSparqlQueryTranslator.class);

    /**
     * Translates the specified notification message to its corresponding event.
     * 
     * @param subscribeRequest
     *            the subscribe request to be translated.
     * 
     * @return the event corresponding to the specified notification message.
     */
    public String translate(Subscribe subscribeRequest) {
        logSubscribe(subscribeRequest);

        TopicExpressionType tet =
                (TopicExpressionType) ((List<Object>) subscribeRequest.getFilter()
                        .getAny()).get(0);

        return "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { "
                + tet.getContent().get(0) + " ?p ?o . } }";
    }

    private static void logSubscribe(Subscribe subscribeRequest) {
        logW3CEndpointReference(
                subscribeRequest.getConsumerReference(), "consumer");

        List<Object> any = subscribeRequest.getAny();
        if (any != null) {
            log.info("any values are:");
            for (Object obj : any) {
                log.info("  {} (class {})", obj, obj.getClass().getName());
            }
        } else {
            log.info("any is null");
        }

        FilterType filterType = subscribeRequest.getFilter();

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
        Object address = getFieldValue(ref, "address");
        if (address != null) {
            log.info("type={}, address={}", type, (String) getFieldValue(
                    address, "uri"));
            logAttributes(type + " Address Attributes", address, "attributes");
        } else {
            log.info("type={}, address is null", type);
        }

        // referenceParameters

        Object metadata = getFieldValue(ref, "metadata");
        if (metadata != null) {
            Object metadataElts = getFieldValue(metadata, "elements");

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

        Object elements = getFieldValue(ref, "elements");
        if (elements != null) {
            log.info("type={}, elements=", type);
            for (Element elt : (List<Element>) elements) {
                log.info("  {} ", asString(elt));
            }
        } else {
            log.info("type={}, elements is null", type);
        }
    }

    @SuppressWarnings("unchecked")
    private static void logAttributes(String type, Object obj, String fieldName) {
        Map<QName, String> attributes =
                (Map<QName, String>) getFieldValue(obj, fieldName);

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

}
