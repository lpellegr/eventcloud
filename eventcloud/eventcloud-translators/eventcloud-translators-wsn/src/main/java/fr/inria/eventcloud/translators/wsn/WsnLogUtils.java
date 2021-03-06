/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.translators.wsn;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
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
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * Some helpers to ease the logging of WS-Notification messages.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class WsnLogUtils {

    private static Logger log = LoggerFactory.getLogger(WsnLogUtils.class);

    /**
     * Logs a {@link Subscribe WS-Notification Subscribe message}.
     * 
     * @param subscribe
     *            the WS-Notification Subscribe message message to log.
     */
    @SuppressWarnings("unchecked")
    public static void logSubscribe(Subscribe subscribe) {
        if (log.isInfoEnabled()) {
            log.info("-- Subscribe message to process (start) ------");

            logW3CEndpointReference(
                    subscribe.getConsumerReference(), "consumer");

            List<Object> any = subscribe.getAny();
            if ((any != null) && (any.size() != 0)) {
                log.info("any values are:");
                for (Object obj : any) {
                    log.info("  * {} (class {})", obj, obj.getClass().getName());
                }
            } else {
                log.info("any is null");
            }

            FilterType filterType = subscribe.getFilter();

            if (filterType != null) {
                any = filterType.getAny();
                if ((any != null) && (any.size() != 0)) {
                    log.info("filter type any values are:");
                    for (Object obj : any) {
                        try {
                            logTopic(
                                    ((JAXBElement<TopicExpressionType>) obj).getValue(),
                                    "  ");
                        } catch (ClassCastException e) {
                            log.info("  an object of class "
                                    + obj.getClass().getName());
                        }
                    }
                } else {
                    log.info("filter type any is null");
                }
            } else {
                log.info("filter type is null");
            }

            log.info("-- Subscribe message to process (end) ------");
        }
    }

    /**
     * Logs a {@link NotificationMessageHolderType WS-Notification message}.
     * 
     * @param msg
     *            the {@link NotificationMessageHolderType WS-Notification
     *            message} to log.
     */
    public static void logNotificationMessageHolderType(NotificationMessageHolderType msg) {
        if (log.isInfoEnabled()) {
            log.info("-- NotificationMessageHolderType message to process (start) ------");

            logW3CEndpointReference(
                    msg.getSubscriptionReference(), "subscriber");

            logTopic(msg.getTopic(), "");

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
                        log.info(
                                "message any class type is {} ",
                                message.getAny().getClass().getName());
                    }
                }
            } else {
                log.info("message is null");
            }

            log.info("-- NotificationMessageHolderType message to process (end) ------");
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
                        log.info("  * {} ", asString(elt));
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
                    log.info("  * {} ", asString(elt));
                }
            } else {
                log.info("type={}, elements is null", type);
            }
        } else {
            log.info("type={} is null", type);
        }
    }

    private static void logTopic(TopicExpressionType topicExpressionType,
                                 String indentation) {
        List<Object> topicContent = topicExpressionType.getContent();
        if (topicContent != null) {
            log.info(
                    indentation + "topicContent(dialect={}):",
                    topicExpressionType.getDialect());
            for (Object obj2 : topicContent) {
                if (obj2 instanceof String) {
                    log.info(indentation + "  * " + obj2 + " (class "
                            + obj2.getClass().getName() + ")");
                } else {
                    Element topicElement = (Element) obj2;
                    String topic =
                            topicElement.getTextContent().trim().replaceAll(
                                    "\n", "");
                    log.info(indentation
                            + "  * "
                            + topic
                            + " (namespace "
                            + topicElement.lookupNamespaceURI(org.apache.xml.utils.QName.getPrefixPart(topic))
                            + ") (class " + obj2.getClass().getName() + ")");
                }
            }

            logAttributes(
                    "  topicAttribute", topicExpressionType, "otherAttributes");
        } else {
            log.info("  topicContent is null");
        }
    }

    @SuppressWarnings("unchecked")
    private static void logAttributes(String type, Object obj, String fieldName) {
        Map<QName, String> attributes =
                (Map<QName, String>) ReflectionUtils.getFieldValue(
                        obj, fieldName);

        if (attributes != null) {
            for (Entry<QName, String> entry : attributes.entrySet()) {
                log.info("* type={}, attributes=<{}, {}>", new Object[] {
                        type, entry.getKey(), entry.getValue()});
            }
        } else {
            log.info("type={}, attributes is null", type);
        }
    }

    private static String asString(Element elt) {
        TransformerFactory transfac = TransformerFactory.newInstance();

        try {
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");

            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(elt);

            try {
                trans.transform(source, result);
                return sw.toString();
            } catch (TransformerException e) {
                throw new IllegalStateException(e);
            }

        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

}
