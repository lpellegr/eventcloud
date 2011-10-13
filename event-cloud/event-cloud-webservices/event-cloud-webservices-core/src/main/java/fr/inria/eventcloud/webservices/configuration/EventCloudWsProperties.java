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
package fr.inria.eventcloud.webservices.configuration;

import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;

import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * Contains default values for proxy web services.
 * 
 * @author bsauvan
 */
public class EventCloudWsProperties extends EventCloudProperties {

    public static final PropertyString PUBLISH_PROXY_ADL = new PropertyString(
            "pub.proxy.adl",
            "fr.inria.eventcloud.webservices.proxies.PublishWsProxy");

    public static final PropertyString PUBLISH_PROXY_WEBSERVICES_ITF =
            new PropertyString("publish.webservices.itf", "publish-webservices");

    public static final PropertyString SUBSCRIBE_PROXY_ADL =
            new PropertyString(
                    "sub.proxy.adl",
                    "fr.inria.eventcloud.webservices.proxies.SubscribeWsProxy");

    public static final PropertyString SUBSCRIBE_PROXY_WEBSERVICES_ITF =
            new PropertyString(
                    "subscribe.webservices.itf", "subscribe-webservices");

    public static final PropertyString PUTGET_PROXY_ADL = new PropertyString(
            "putget.proxy.adl",
            "fr.inria.eventcloud.webservices.proxies.PutGetWsProxy");

    public static final PropertyString PUTGET_PROXY_WEBSERVICES_ITF =
            new PropertyString("putget.webservices.itf", "putget-webservices");

}
