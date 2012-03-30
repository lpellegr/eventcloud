/**
 * Copyright (c) 2011-2012 INRIA.
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

import java.util.Collection;

import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.webservices.api.PublishWsApi;
import fr.inria.eventcloud.webservices.api.SubscribeInfos;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;

/**
 * Component used to simulate a subscriber and a publisher.
 * 
 * @author bsauvan
 */
public class PubSubComponentImpl implements SubscribeWsApi, PublishWsApi,
        PubSubStatus, BindingController {

    private static final Logger log =
            LoggerFactory.getLogger(PubSubComponentImpl.class);

    public static final String SUBSCRIBE_WEBSERVICES_NAME =
            "subscribe-webservices";

    public static final String PUBLISH_WEBSERVICES_NAME = "publish-webservices";

    private boolean hasSentEvents;

    private boolean hasReceivedEvents;

    private PublishWsApi publishWs;

    private SubscribeWsApi subscribeWs;

    public PubSubComponentImpl() {
        this.hasSentEvents = false;
        this.hasReceivedEvents = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage currentMessage) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(SubscribeInfos subscribeInfos)
            throws UnrecognizedPolicyRequestFault,
            SubscribeCreationFailedFault,
            InvalidProducerPropertiesExpressionFault,
            UnsupportedPolicyRequestFault, TopicNotSupportedFault,
            NotifyMessageNotSupportedFault, ResourceUnknownFault,
            UnacceptableInitialTerminationTimeFault,
            InvalidMessageContentExpressionFault, InvalidFilterFault,
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault {
        if (this.subscribeWs != null) {
            return this.subscribeWs.subscribe(subscribeInfos);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<CompoundEvent> compoundEvents) {
        if (this.publishWs != null) {
            if (!this.hasSentEvents) {
                this.publishWs.publish(compoundEvents);
                this.hasSentEvents = true;
            } else {
                this.hasReceivedEvents = true;
                for (CompoundEvent compoundEvent : compoundEvents) {
                    log.info(
                            "New compound event received: {}",
                            compoundEvent.toString());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasReceivedEvents() {
        return this.hasReceivedEvents;
    }

    @Override
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

    @Override
    public String[] listFc() {
        return new String[] {
                SUBSCRIBE_WEBSERVICES_NAME, PUBLISH_WEBSERVICES_NAME};
    }

    @Override
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

    @Override
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
