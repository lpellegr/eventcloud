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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerImpl;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.priority.PriorityConstraint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;

import com.hp.hpl.jena.rdf.model.StmtIterator;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.messages.request.can.AddQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.ContainsQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.CountQuadruplePatternRequest;
import fr.inria.eventcloud.messages.request.can.DeleteQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.DeleteQuadruplesRequest;
import fr.inria.eventcloud.messages.request.can.IndexEphemeralSubscriptionRequest;
import fr.inria.eventcloud.messages.request.can.IndexSubscriptionRequest;
import fr.inria.eventcloud.messages.request.can.PublishCompoundEventRequest;
import fr.inria.eventcloud.messages.request.can.PublishQuadrupleRequest;
import fr.inria.eventcloud.messages.request.can.QuadruplePatternRequest;
import fr.inria.eventcloud.messages.request.can.ReconstructCompoundEventRequest;
import fr.inria.eventcloud.messages.response.can.BooleanForwardResponse;
import fr.inria.eventcloud.messages.response.can.CountQuadruplePatternResponse;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponse;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.utils.Callback;

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

    private static final long serialVersionUID = 140L;

    private static final Logger log =
            LoggerFactory.getLogger(SemanticPeerImpl.class);

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
    public void runComponentActivity(Body body) {
        this.multiActiveService = new MultiActiveService(body);

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

        this.multiActiveService.multiActiveServing(
                priorityConstraints,
                P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue(), false,
                false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public void publish(Quadruple quad) {
        if (quad.getPublicationTime() == -1) {
            quad.setPublicationTime();
        }

        // the quadruple is routed without taking into account the publication
        // datetime (neither the other meta information)
        super.sendv(new PublishQuadrupleRequest(quad));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
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
                super.sendv(new PublishCompoundEventRequest(compoundEvent, i));
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
    @MemberOf("parallel")
    public void subscribe(Subscription subscription) {
        subscription.setIndexationTime();

        super.sendv(new IndexSubscriptionRequest(subscription));
    }

    /*
     * PutGetApi implementation
     * 
     * The boolean value which is returned in the PutGetApi by some methods 
     * is used to ensure that ProActive calls are synchronous.
     */

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean add(Quadruple quad) {
        PAFuture.waitFor(super.send(new AddQuadrupleRequest(quad)));

        return true;
    }

    @MemberOf("parallel")
    private BooleanWrapper addAsync(Quadruple quad) {
        PAFuture.waitFor(super.send(new AddQuadrupleRequest(quad)));

        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean add(Collection<Quadruple> quads) {
        List<BooleanWrapper> results = new ArrayList<BooleanWrapper>();

        for (final Quadruple quad : quads) {
            results.add(this.addAsync(quad));
        }

        PAFuture.waitForAll(results);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean add(URL url, SerializationFormat format) {
        try {
            final List<BooleanWrapper> results =
                    new ArrayList<BooleanWrapper>();
            InputStream in = url.openConnection().getInputStream();

            RdfParser.parse(in, format, new Callback<Quadruple>() {
                @Override
                public void execute(Quadruple quad) {
                    results.add(SemanticPeerImpl.this.addAsync(quad));
                }
            });

            in.close();
            PAFuture.waitForAll(results);

            return true;
        } catch (IOException ioe) {
            log.error("An error occurred when reading from the given URL", ioe);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean contains(Quadruple quad) {
        return ((BooleanForwardResponse) PAFuture.getFutureValue(super.send(new ContainsQuadrupleRequest(
                quad)))).getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean delete(Quadruple quad) {
        PAFuture.waitFor(super.send(new DeleteQuadrupleRequest(quad)));

        return true;
    }

    @MemberOf("parallel")
    public BooleanWrapper deleteAsync(Quadruple quad) {
        PAFuture.waitFor(super.send(new DeleteQuadrupleRequest(quad)));

        return new BooleanWrapper(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public boolean delete(Collection<Quadruple> quads) {
        List<BooleanWrapper> results = new ArrayList<BooleanWrapper>();

        for (final Quadruple quad : quads) {
            results.add(this.deleteAsync(quad));
        }

        PAFuture.waitForAll(results);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public List<Quadruple> delete(QuadruplePattern quadPattern) {
        QuadruplePatternResponse response =
                (QuadruplePatternResponse) PAFuture.getFutureValue(super.send(new DeleteQuadruplesRequest(
                        quadPattern.getGraph(), quadPattern.getSubject(),
                        quadPattern.getPredicate(), quadPattern.getObject())));
        return response.getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public long count(QuadruplePattern quadPattern) {
        return ((CountQuadruplePatternResponse) PAFuture.getFutureValue((super.send(new CountQuadruplePatternRequest(
                quadPattern.getGraph(), quadPattern.getSubject(),
                quadPattern.getPredicate(), quadPattern.getObject()))))).getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public long count(String sparqlQuery) throws MalformedSparqlQueryException {
        SparqlResponse<?> response = this.executeSparqlQuery(sparqlQuery);

        if (response instanceof SparqlAskResponse) {
            return ((SparqlAskResponse) response).getResult()
                    ? 1 : 0;
        } else if (response instanceof SparqlConstructResponse) {
            StmtIterator it =
                    ((SparqlConstructResponse) response).getResult()
                            .listStatements();
            long result = 0;
            while (it.hasNext()) {
                it.next();
                result++;
            }
            return result;
        } else if (response instanceof SparqlSelectResponse) {
            ResultSetWrapper it = ((SparqlSelectResponse) response).getResult();
            long result = 0;
            while (it.hasNext()) {
                it.nextBinding();
                result++;
            }
            return result;
        }

        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public List<Quadruple> find(QuadruplePattern quadPattern) {
        return ((QuadruplePatternResponse) PAFuture.getFutureValue((super.send(new QuadruplePatternRequest(
                quadPattern.getGraph(), quadPattern.getSubject(),
                quadPattern.getPredicate(), quadPattern.getObject()))))).getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery)
            throws MalformedSparqlQueryException {
        sparqlQuery = sparqlQuery.trim();

        if (sparqlQuery.startsWith("ASK")) {
            return this.executeSparqlAsk(sparqlQuery);
        } else if (sparqlQuery.startsWith("CONSTRUCT")) {
            return this.executeSparqlConstruct(sparqlQuery);
        } else if (sparqlQuery.startsWith("DESCRIBE")) {
            return this.executeSparqlDescribe(sparqlQuery);
        } else if (sparqlQuery.startsWith("SELECT")) {
            return this.executeSparqlSelect(sparqlQuery);
        } else {
            throw new IllegalArgumentException("Unknow query form for query: "
                    + sparqlQuery);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery)
            throws MalformedSparqlQueryException {
        return ((SemanticRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlAsk(
                sparqlAskQuery, super.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstruct)
            throws MalformedSparqlQueryException {
        return ((SemanticRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlConstruct(
                sparqlConstruct, super.overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery)
            throws MalformedSparqlQueryException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MemberOf("parallel")
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelect)
            throws MalformedSparqlQueryException {
        return ((SemanticRequestResponseManager) super.overlay.getRequestResponseManager()).executeSparqlSelect(
                sparqlSelect, super.overlay);
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
