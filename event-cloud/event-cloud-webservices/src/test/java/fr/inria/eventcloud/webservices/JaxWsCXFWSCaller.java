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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.objectweb.proactive.core.component.webservices.PAWSCaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link PAWSCaller} interface using the <a
 * href="http://cxf.apache.org/">CXF</a> API configured for JAX-WS.
 * 
 * @author bsauvan
 */
// TODO Delete this class when ProActive 5.2 or 6.0 has been released
public class JaxWsCXFWSCaller implements PAWSCaller {

    private static final Logger log =
            LoggerFactory.getLogger(JaxWsCXFWSCaller.class);

    private Client client;

    private Map<String, String> operationNames;

    public JaxWsCXFWSCaller() {
    }

    public void setup(Class<?> serviceClass, String wsUrl) {
        JaxWsClientFactoryBean factory = new JaxWsClientFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(wsUrl);
        client = factory.create();

        operationNames = new HashMap<String, String>();
        Method[] methods = serviceClass.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(WebMethod.class)) {
                WebMethod webMethodAnnotation =
                        method.getAnnotation(WebMethod.class);
                if (!webMethodAnnotation.operationName().equals("")) {
                    operationNames.put(
                            method.getName(),
                            webMethodAnnotation.operationName());
                    continue;
                }
            }
            operationNames.put(method.getName(), method.getName());
        }
    }

    public Object callWS(String methodName, Object[] args, Class<?> returnType) {
        if (client != null) {
            try {
                Object[] results =
                        client.invoke(operationNames.get(methodName), args);
                if (returnType == null) {
                    return null;
                } else {
                    return results[0];
                }
            } catch (Exception e) {
                log.error(
                        "[JaxWsCXFWSCaller] Failed to invoke web service: "
                                + client.getEndpoint()
                                        .getEndpointInfo()
                                        .getAddress(), e);
            }
        } else {
            log.error("[JaxWsCXFWSCaller] Cannot invoke web service since the set up has not been done");
        }
        return null;
    }

}
