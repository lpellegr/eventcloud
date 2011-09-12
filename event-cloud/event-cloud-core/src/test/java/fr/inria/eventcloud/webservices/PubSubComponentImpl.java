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
package fr.inria.eventcloud.webservices;

import java.net.URI;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.webservices.PublishWsApi;
import fr.inria.eventcloud.api.webservices.SubscribeWsApi;
import fr.inria.eventcloud.api.webservices.SubscriberWsApi;

/**
 * Component used to simulate a subscriber and a publisher.
 * 
 * @author bsauvan
 */
public class PubSubComponentImpl implements SubscribeWsApi, PublishWsApi,
        SubscriberWsApi, PubSubStatus, BindingController {

    private static final Logger log =
            LoggerFactory.getLogger(PubSubComponentImpl.class);

    public static final String SUBSCRIBE_WEBSERVICES_NAME =
            "subscribe-webservices";

    public static final String PUBLISH_WEBSERVICES_NAME = "publish-webservices";

    private boolean hasReceivedEvent;

    private PublishWsApi publishWs;

    private SubscribeWsApi subscribeWs;

    public PubSubComponentImpl() {
        this.hasReceivedEvent = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String wsNotifSubscriptionPayload,
                                    String topicNameSpacePayload,
                                    String[] topicsDefinitionPayloads,
                                    String subscriberWsUrl) {
        if (this.subscribeWs != null) {
            return this.subscribeWs.subscribe(
                    wsNotifSubscriptionPayload, topicNameSpacePayload,
                    topicsDefinitionPayloads, subscriberWsUrl);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
        if (this.subscribeWs != null) {
            this.subscribeWs.unsubscribe(id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishEvent(String xmlPayload, URI eventId) {
        if (this.publishWs != null) {
            this.publishWs.publishEvent(xmlPayload, eventId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishEventWithXsd(String xmlPayload, String xsdPayload,
                                    URI eventId) {
        if (this.publishWs != null) {
            this.publishWs.publishEventWithXsd(xmlPayload, xsdPayload, eventId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyEvent(SubscriptionId id, String xmlPayload) {
        this.hasReceivedEvent = true;
        log.info("New event received: {}", xmlPayload);
    }

    public boolean hasReceivedEvent() {
        return this.hasReceivedEvent;
    }

    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (SUBSCRIBE_WEBSERVICES_NAME.equals(clientItfName)) {
            this.subscribeWs = (SubscribeWsApi) serverItf;
        } else if (PUBLISH_WEBSERVICES_NAME.equals(clientItfName)) {
            this.publishWs = (PublishWsApi) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] {
                SUBSCRIBE_WEBSERVICES_NAME, PUBLISH_WEBSERVICES_NAME};
    }

    public Object lookupFc(String clientItfName)
            throws NoSuchInterfaceException {
        if (SUBSCRIBE_WEBSERVICES_NAME.equals(clientItfName)) {
            return this.subscribeWs;
        } else if (PUBLISH_WEBSERVICES_NAME.equals(clientItfName)) {
            return this.publishWs;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (SUBSCRIBE_WEBSERVICES_NAME.equals(clientItfName)) {
            this.subscribeWs = null;
        } else if (PUBLISH_WEBSERVICES_NAME.equals(clientItfName)) {
            this.publishWs = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

}
