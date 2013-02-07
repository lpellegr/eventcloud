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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

/**
 * Manager of a set of {@link NodeProvider NodeProviders}.
 * 
 * @author bsauvan
 */
public class NodeProvidersManager {

    public static final String DEFAULT_NODE_PROVIDER_ID = "Default";

    private final Map<String, NodeProvider> nodeProviders;

    /**
     * Constructs a {@link NodeProvidersManager} which will manage a set of
     * {@code node providers} (a {@link LocalNodeProvider} associated to the
     * identifier {@link DEFAULT_NODE_PROVIDER_ID} and the {@code node providers} loaded from the
     * configuration file located at the path given by the property
     * {@link P2PStructuredProperties#NODE_PROVIDER_CONFIG_FILE}).
     */
    @SuppressWarnings("unchecked")
    public NodeProvidersManager() {
        this.nodeProviders = new HashMap<String, NodeProvider>();
        this.nodeProviders.put(
                DEFAULT_NODE_PROVIDER_ID, new LocalNodeProvider());

        if (P2PStructuredProperties.NODE_PROVIDER_CONFIG_FILE.getValue() != null) {
            try {
                YamlReader reader =
                        new YamlReader(
                                new FileReader(
                                        P2PStructuredProperties.NODE_PROVIDER_CONFIG_FILE.getValue()));
                this.nodeProviders.putAll(reader.read(HashMap.class));
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(e);
            } catch (YamlException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Starts all the managed {@code node providers}.
     */
    public void startAllNodeProviders() {
        for (NodeProvider nodeProvider : this.nodeProviders.values()) {
            nodeProvider.start();
        }
    }

    /**
     * Returns the identifiers of the managed {@code node providers}.
     * 
     * @return the identifiers of the managed {@code node providers}.
     */
    public List<String> getNodeProviderIds() {
        return new ArrayList<String>(this.nodeProviders.keySet());
    }

    /**
     * Returns the {@code node provider} represented by the specified
     * identifier.
     * 
     * @param nodeProviderId
     *            the identifier of the node provider.
     * @return the {@code node provider} represented by the specified
     *         identifier.
     */
    public NodeProvider getNodeProvider(String nodeProviderId) {
        if (this.nodeProviders.containsKey(nodeProviderId)) {
            return this.nodeProviders.get(nodeProviderId);
        } else {
            throw new IllegalArgumentException(
                    "No such node provider with the specified identifier");
        }
    }

    /**
     * Terminates all the managed {@code node providers}.
     */
    public void terminateAllNodeProviders() {
        for (NodeProvider nodeProvider : this.nodeProviders.values()) {
            nodeProvider.terminate();
        }
    }

}
