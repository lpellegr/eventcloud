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
package fr.inria.eventcloud.webservices.services;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType.Message;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.translators.wsn.TranslationException;
import fr.inria.eventcloud.utils.ReflectionUtils;

/**
 * Defines a publish web service as defined by the WS-Notification
 * specification. All the calls to the notify request will be translated and
 * redirected to a {@link PublishProxy} in order to be published into an
 * eventcloud.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudPublish", portName = "EventCloudPublishPort", targetNamespace = "http://docs.oasis-open.org/wsn/bw-2", name = "EventCloudPublishPortType")
public class PublishServiceImpl extends
        EventCloudTranslatableProxyService<PublishProxy> implements
        NotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PublishServiceImpl.class);

    public PublishServiceImpl(String registryUrl, String eventCloudIdUrl) {
        super(registryUrl, eventCloudIdUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Notify notify) {
        if (super.proxy == null) {
            return;
        }

        if (notify.getNotificationMessage().size() > 0) {
            for (NotificationMessageHolderType notificationMessage : notify.getNotificationMessage()) {
                try {
                    logNotificationMessageHolderType(notificationMessage);

                    CompoundEvent compoundEvent =
                            super.translator.translate(notificationMessage);

                    log.info("Translation output:\n{}", compoundEvent);

                    super.proxy.publish(compoundEvent);
                } catch (TranslationException e) {
                    log.error("Translation error:");
                    logAndThrowIllegalArgumentException(e.getMessage());
                }
            }

            log.info("New notification message handled");
        } else {
            logAndThrowIllegalArgumentException("Notify message received does not contain any notification message");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublishProxy createProxy() {
        return ProxyFactory.getInstance(
                super.registryUrl, new EventCloudId(super.streamUrl))
                .createPublishProxy();
    }

    private static void logNotificationMessageHolderType(NotificationMessageHolderType msg) {
        if (log.isInfoEnabled()) {
            log.info("-- NotificationMessageHolderType message to process (start) ------");

            logW3CEndpointReference(
                    msg.getSubscriptionReference(), "subscriber");

            TopicExpressionType topicType = msg.getTopic();
            if (topicType != null) {
                List<Object> topicContent = topicType.getContent();
                if (topicContent != null) {
                    log.info(
                            "topicContent(dialect={}) :",
                            topicType.getDialect());
                    for (Object obj : topicContent) {
                        log.info("  {} (class {})", obj, obj.getClass()
                                .getName());
                    }

                    logAttributes(
                            "topicAttribute", topicType, "otherAttributes");
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

}
