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
package fr.inria.eventcloud.webservices.pubsub;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.webservices.api.PublishWsApi;
import fr.inria.eventcloud.webservices.api.SubscribeInfos;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;
import fr.inria.eventcloud.webservices.api.SubscriberWsApi;

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
    public SubscriptionId subscribe(SubscribeInfos subscribeInfos) {
        if (this.subscribeWs != null) {
            return this.subscribeWs.subscribe(subscribeInfos);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
//    @Override
//    public void unsubscribe(SubscriptionId id) {
//        if (this.subscribeWs != null) {
//            this.subscribeWs.unsubscribe(id);
//        }
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<Event> events) {
        if (this.publishWs != null) {
            this.publishWs.publish(events);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Collection<Event> events) {
        this.hasReceivedEvent = true;
        for (Event event : events) {
            log.info("New event received: {}", event.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
