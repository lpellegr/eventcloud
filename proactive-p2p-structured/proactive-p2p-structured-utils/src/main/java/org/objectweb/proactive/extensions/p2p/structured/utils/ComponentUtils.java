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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

/**
 * Utility methods for GCM components.
 * 
 * @author bsauvan
 */
public class ComponentUtils {

    private static Factory factory;

    private static Factory nfFactory;

    static {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
            nfFactory = FactoryFactory.getNFFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a map containing the specified node and which can be used as a
     * context to instantiate components.
     * 
     * @param node
     *            Node to include to the map.
     * 
     * @return map containing the specified node and which can be used as a
     *         context to instantiate components.
     */
    public static Map<String, Object> createContext(Node node) {
        Map<String, Object> context = new HashMap<String, Object>();

        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }

        return context;
    }

    /**
     * Creates a map containing the specified {@code GCM virtual node} and which
     * can be used as a context to instantiate components.
     * 
     * @param vn
     *            {@code GCM virtual node} to include to the map.
     * 
     * @return map containing the specified {@code GCM virtual node} and which
     *         can be used as a context to instantiate components.
     */
    public static Map<String, Object> createContext(GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();

        if (vn != null) {
            context.put(vn.getName(), vn);
        }

        return context;
    }

    /**
     * Creates a component with the specified ADL and returns a reference on the
     * specified interface of the component created.
     * 
     * @param <T>
     *            type of the interface.
     * @param componentAdl
     *            ADL to use to create the component.
     * @param context
     *            optional additional information to create the component.
     * @param interfaceName
     *            the name of the interface to return.
     * @param interfaceClass
     *            class of the interface.
     * @param toStart
     *            {@code true} if the component has to be started, {@code false}
     *            otherwise.
     * 
     * @return a reference on the specified interface of the component created.
     */
    public static <T> T createComponentAndGetInterface(String componentAdl,
                                                       Map<String, Object> context,
                                                       String interfaceName,
                                                       Class<T> interfaceClass,
                                                       boolean toStart) {
        return createComponentAndGetInterface(
                factory, componentAdl, context, interfaceName, interfaceClass,
                toStart);
    }

    /**
     * Creates a non functional component with the specified ADL and returns a
     * reference on the specified interface of the non functional component
     * created.
     * 
     * @param <T>
     *            type of the interface.
     * @param componentAdl
     *            ADL to use to create the non functional component.
     * @param context
     *            optional additional information to create the non functional
     *            component.
     * @param interfaceName
     *            the name of the interface to return.
     * @param interfaceClass
     *            class of the interface.
     * @param toStart
     *            {@code true} if the non functional component has to be
     *            started, {@code false} otherwise.
     * 
     * @return a reference on the specified interface of the non functional
     *         component created.
     */
    public static <T> T createNfComponentAndGetInterface(String componentAdl,
                                                         Map<String, Object> context,
                                                         String interfaceName,
                                                         Class<T> interfaceClass,
                                                         boolean toStart) {
        return createComponentAndGetInterface(
                nfFactory, componentAdl, context, interfaceName,
                interfaceClass, toStart);
    }

    // TODO: remove synchronized once issue #98 is fixed
    private synchronized static <T> T createComponentAndGetInterface(Factory factory,
                                                                     String componentAdl,
                                                                     Map<String, Object> context,
                                                                     String interfaceName,
                                                                     Class<T> interfaceClass,
                                                                     boolean toStart) {
        try {
            Component component =
                    (Component) factory.newComponent(componentAdl, context);

            if (toStart) {
                GCM.getGCMLifeCycleController(component).startFc();
            }

            return interfaceClass.cast(component.getFcInterface(interfaceName));
        } catch (ADLException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchInterfaceException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a reference on the specified interface of the component
     * associated with the specified URI.
     * 
     * @param <T>
     *            type of the interface.
     * @param componentUri
     *            the registered location of the component.
     * @param interfaceName
     *            the name of the interface to lookup.
     * @param interfaceClass
     *            class of the interface.
     * 
     * @return a reference on the specified interface of the component
     *         associated with the specified URI.
     * 
     * @throws IOException
     *             if there is a communication problem with the registry or if
     *             the component could not be found at the specified URI.
     */
    public static <T> T lookupFcInterface(String componentUri,
                                          String interfaceName,
                                          Class<T> interfaceClass)
            throws IOException {
        try {
            return interfaceClass.cast(Fractive.lookup(componentUri)
                    .getFcInterface(interfaceName));
        } catch (NoSuchInterfaceException e) {
            throw new IllegalArgumentException(e);
        } catch (NamingException e) {
            throw new IOException(e);
        }
    }

    /**
     * Terminates a list of components.
     * 
     * @param stubs
     *            the stubs of the components to terminate.
     */
    public static <T> void terminateComponents(Iterable<T> stubs) {
        for (T stub : stubs) {
            terminateComponent(stub);
        }
    }

    public static <T> void terminateComponent(String url) {
        try {
            terminateComponent(Fractive.lookup(url));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (NamingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Terminates the component represented by the given stub.
     * 
     * @param stub
     *            the stub of the component to terminate.
     */
    public static <T> void terminateComponent(T stub) {
        try {
            Component component = ((Interface) stub).getFcItfOwner();

            PAGCMLifeCycleController controller =
                    Utils.getPAGCMLifeCycleController(component);

            controller.stopFc();
            controller.terminateGCMComponent(false);
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

}
