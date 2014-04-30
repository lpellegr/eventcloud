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
package fr.inria.eventcloud.webservices.api;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.oasis_open.docs.wsn.bw_2.InvalidFilterFault;
import org.oasis_open.docs.wsn.bw_2.InvalidMessageContentExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidProducerPropertiesExpressionFault;
import org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault;
import org.oasis_open.docs.wsn.bw_2.MultipleTopicsSpecifiedFault;
import org.oasis_open.docs.wsn.bw_2.NoCurrentMessageOnTopicFault;
import org.oasis_open.docs.wsn.bw_2.NotifyMessageNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.SubscribeCreationFailedFault;
import org.oasis_open.docs.wsn.bw_2.TopicExpressionDialectUnknownFault;
import org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault;
import org.oasis_open.docs.wsn.bw_2.UnableToDestroySubscriptionFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableInitialTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnacceptableTerminationTimeFault;
import org.oasis_open.docs.wsn.bw_2.UnrecognizedPolicyRequestFault;
import org.oasis_open.docs.wsn.bw_2.UnsupportedPolicyRequestFault;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

/**
 * {@link EventCloudsManagementWsApi} extended to support subscriptions as
 * defined by the WS-Notification specification.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudsManagement", portName = "EventCloudsManagementPort", name = "EventCloudsManagementPortType", targetNamespace = "http://webservices.eventcloud.inria.fr/")
@XmlSeeAlso(value = {
        org.oasis_open.docs.wsn.br_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.rp_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class,
        org.oasis_open.docs.wsrf.r_2.ObjectFactory.class,
        org.oasis_open.docs.wsn.t_1.ObjectFactory.class,
        org.oasis_open.docs.wsn.b_2.ObjectFactory.class})
public interface EventCloudsManagementWsnApi extends EventCloudsManagementWsApi {

    @WebResult(name = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "GetCurrentMessageResponse")
    @WebMethod(operationName = "GetCurrentMessage")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    GetCurrentMessageResponse getCurrentMessage(@WebParam(partName = "GetCurrentMessageRequest", name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") GetCurrentMessage getCurrentMessageRequest)
            throws NoCurrentMessageOnTopicFault, TopicNotSupportedFault,
            ResourceUnknownFault, MultipleTopicsSpecifiedFault,
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault;

    @WebResult(name = "SubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "SubscribeResponse")
    @WebMethod(operationName = "Subscribe")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    SubscribeResponse subscribe(@WebParam(partName = "SubscribeRequest", name = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Subscribe subscribeRequest)
            throws UnrecognizedPolicyRequestFault,
            SubscribeCreationFailedFault,
            InvalidProducerPropertiesExpressionFault,
            UnsupportedPolicyRequestFault, TopicNotSupportedFault,
            NotifyMessageNotSupportedFault, ResourceUnknownFault,
            UnacceptableInitialTerminationTimeFault,
            InvalidMessageContentExpressionFault, InvalidFilterFault,
            TopicExpressionDialectUnknownFault, InvalidTopicExpressionFault;

    @WebResult(name = "RenewResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "RenewResponse")
    @WebMethod(operationName = "Renew")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    RenewResponse renew(@WebParam(partName = "RenewRequest", name = "Renew", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Renew renewRequest)
            throws UnacceptableTerminationTimeFault, ResourceUnknownFault;

    @WebResult(name = "UnsubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/b-2", partName = "UnsubscribeResponse")
    @WebMethod(operationName = "Unsubscribe")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    UnsubscribeResponse unsubscribe(@WebParam(partName = "UnsubscribeRequest", name = "Unsubscribe", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Unsubscribe unsubscribeRequest)
            throws UnableToDestroySubscriptionFault, ResourceUnknownFault;

}
