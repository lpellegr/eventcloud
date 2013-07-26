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
package fr.inria.eventcloud.overlay;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.multiactivies.PeerServingPolicy;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.multiactivity.component.ComponentMultiActiveService;
import org.objectweb.proactive.multiactivity.priority.PriorityConstraint;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.messages.request.IndexEphemeralSubscriptionRequest;
import fr.inria.eventcloud.messages.request.IndexSubscriptionRequest;
import fr.inria.eventcloud.messages.request.PublishCompoundEventRequest;
import fr.inria.eventcloud.messages.request.PublishQuadrupleRequest;
import fr.inria.eventcloud.messages.request.ReconstructCompoundEventRequest;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * SemanticPeerImpl is a concrete implementation of {@link SemanticPeer}. It is
 * a peer constructed by using a {@link CanOverlay} and a
 * {@link TransactionalTdbDatastore}. It exposes the methods provided by the
 * {@link PutGetApi} interface in order to provide semantic operations like
 * {@code add}, {@code delete}, {@code find}, etc. but also to execute a SPARQL
 * query.
 * <p>
 * Warning, you have to use the {@link SemanticFactory} in order to create a new
 * SemanticPeer component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class SemanticPeerImpl extends PeerImpl implements SemanticPeer,
        BindingController {

    private static final long serialVersionUID = 160L;

    /**
     * ADL name of the semantic peer component.
     */
    public static final String SEMANTIC_PEER_ADL =
            "fr.inria.eventcloud.overlay.SemanticPeer";

    /**
     * Functional client interface name to bind the social filter.
     */
    public static final String SOCIAL_FILTER_SERVICES_ITF =
            "social-filter-services";

    /**
     * Empty constructor required by ProActive.
     */
    public SemanticPeerImpl() {
        // keep it empty because it is called each time we have to reify
        // a SemanticPeer object (e.g. each time a call to getRandomPeer()
        // from a tracker is performed)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponentActivity(Body body) {
        this.configurationProperty = "eventcloud.configuration";
        this.propertiesClass = EventCloudProperties.class;
        super.initComponentActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endComponentActivity(Body body) {
        if (EventCloudProperties.isDynamicLoadBalancingEnabled()) {
            ((SemanticCanOverlay) super.overlay).getLoadBalancingManager()
                    .stop();
        }

        super.endComponentActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runComponentActivity(Body body) {
        this.multiActiveService = new ComponentMultiActiveService(body);

        List<PriorityConstraint> priorityConstraints =
                new ArrayList<PriorityConstraint>();

        priorityConstraints.add(new PriorityConstraint(-1, 1, "publish"));

        if (EventCloudProperties.isSbce1PubSubAlgorithmUsed()) {
            priorityConstraints.add(new PriorityConstraint(
                    3, 8, "send", ReconstructCompoundEventRequest.class));
            priorityConstraints.add(new PriorityConstraint(0, 1));
        } else if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()) {
            priorityConstraints.add(new PriorityConstraint(
                    1, 8, "sendv", IndexEphemeralSubscriptionRequest.class));
        }

        this.multiActiveService.policyServing(
                new PeerServingPolicy(), priorityConstraints,
                P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue(), false,
                false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("routing")
    public void publish(Quadruple quad) {
        if (quad.getPublicationTime() == -1) {
            quad.setPublicationTime();
        }

        // the quadruple is routed without taking into account the publication
        // datetime (neither the other meta information)
        super.route(new PublishQuadrupleRequest(quad));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("routing")
    public void publish(CompoundEvent compoundEvent) {
        long publicationTime = System.currentTimeMillis();

        // SBCE3
        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            // the timestamp must be set to all the quadruples before to send
            // the full CE to each peer managing one of the quadruples contained
            // by the compound event
            for (Quadruple q : compoundEvent) {
                q.setPublicationTime(publicationTime);
            }

            for (int i = 0; i < compoundEvent.size(); i++) {
                // sends the whole compound event
                super.route(new PublishCompoundEventRequest(compoundEvent, i));
            }

            // the meta quadruple is necessary when we use the fallback scheme
            // (SBCE2)
            Quadruple metaQuadruple =
                    CompoundEvent.createMetaQuadruple(compoundEvent);
            metaQuadruple.setPublicationTime(publicationTime);
            this.publish(metaQuadruple);
        } else {
            // SBCE1 or SBCE2
            Quadruple metaQuadruple =
                    CompoundEvent.createMetaQuadruple(compoundEvent);
            metaQuadruple.setPublicationTime(publicationTime);
            this.publish(metaQuadruple);

            for (Quadruple quad : compoundEvent) {
                quad.setPublicationTime(publicationTime);
                this.publish(quad);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("routing")
    public void subscribe(Subscription subscription) {
        subscription.setIndexationTime();

        super.route(new IndexSubscriptionRequest(subscription));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException {
        if (clientItfName.equals(SOCIAL_FILTER_SERVICES_ITF)) {
            ((SemanticCanOverlay) this.overlay).setSocialFilter((RelationshipStrengthEngineManager) serverItf);
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] listFc() {
        return new String[] {SOCIAL_FILTER_SERVICES_ITF};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookupFc(String clientItfName)
            throws NoSuchInterfaceException {
        if (clientItfName.equals(SOCIAL_FILTER_SERVICES_ITF)) {
            return ((SemanticCanOverlay) this.overlay).getSocialFilter();
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals(SOCIAL_FILTER_SERVICES_ITF)) {
            ((SemanticCanOverlay) this.overlay).setSocialFilter(null);
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

}
