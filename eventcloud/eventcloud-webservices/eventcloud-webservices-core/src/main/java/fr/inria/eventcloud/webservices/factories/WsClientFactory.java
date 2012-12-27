/**
 * Copyright (c) 2011-2013 INRIA.
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
package fr.inria.eventcloud.webservices.factories;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

/**
 * Simple class to easily create CXF clients.
 * 
 * @author lpellegr
 */
public class WsClientFactory {

    /**
     * Creates a web service client for the web service exposed at the specified
     * URL and exposing the specified interface.
     * 
     * @param serviceInterface
     *            interface exposed by the web service.
     * @param serviceAddress
     *            URL of the web service.
     * 
     * @return web service client for the web service exposed at the specified
     *         URL and exposing the specified interface.
     */
    public static <T> T createWsClient(Class<T> serviceInterface,
                                       String serviceAddress) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        factory.setServiceClass(serviceInterface);
        factory.setAddress(serviceAddress);

        return serviceInterface.cast(factory.create());
    }

}
