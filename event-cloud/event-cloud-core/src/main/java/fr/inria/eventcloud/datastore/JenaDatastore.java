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
package fr.inria.eventcloud.datastore;

import java.io.File;
import java.util.Iterator;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * This class defines a concrete implementation of a persistent datastore which
 * implements the {@link SemanticDatastore} interface by using Jena TDB as
 * internal datastore.
 * 
 * @author lpellegr
 */
public class JenaDatastore extends SemanticDatastore {

    private DatasetGraphTDB datastore;

    /**
     * Creates a new datastore that stores data into a folder from the OS
     * temporary directory.
     */
    public JenaDatastore() {
        this(new File(System.getProperty("java.io.tmpdir")), true);
    }

    /**
     * Creates a new datastore that stores data into the specified
     * {@code repositoryPath}.
     * 
     * @param repositoryPath
     *            the path where to store the data.
     * 
     * @param autoRemove
     *            indicates whether the repository has to be removed or not when
     *            it is closed.
     */
    public JenaDatastore(File repositoryPath, boolean autoRemove) {
        super(repositoryPath, autoRemove);
        this.registerPlugins();
    }

    private void registerPlugins() {
        TypeMapper.getInstance().registerDatatype(
                VariableDatatype.getInstance());
    }

    /*
     * Implementation of PersistentDatastore abstract methods
     */

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalOpen() {
        if (!this.path.exists()) {
            this.path.mkdirs();
        }
        this.datastore = TDBFactory.createDatasetGraph(super.path.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalClose() {
        this.datastore.close();
    }

    /*
     * Implementation of SemanticDatastore interface
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Quadruple quad) {
        this.datastore.getLock().enterCriticalSection(Lock.WRITE);

        try {
            this.datastore.add(
                    quad.getGraph(), quad.getSubject(), quad.getPredicate(),
                    quad.getObject());
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Collection<Quadruple> quads) {
        this.datastore.getLock().enterCriticalSection(Lock.WRITE);

        try {
            for (Quadruple quad : quads) {
                this.datastore.add(
                        quad.getGraph(), quad.getSubject(),
                        quad.getPredicate(), quad.getObject());
            }
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Quadruple quad) {
        this.datastore.getLock().enterCriticalSection(Lock.WRITE);

        try {
            this.datastore.delete(
                    quad.getGraph(), quad.getSubject(), quad.getPredicate(),
                    quad.getObject());
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Quadruple quad) {
        boolean result;

        this.datastore.getLock().enterCriticalSection(Lock.READ);

        try {
            result =
                    this.datastore.contains(
                            quad.getGraph(), quad.getSubject(),
                            quad.getPredicate(), quad.getObject());
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        this.datastore.getLock().enterCriticalSection(Lock.WRITE);

        try {
            this.datastore.deleteAny(g, s, p, o);
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }
    }

    public Collection<Quadruple> find(QuadruplePattern quadruplePattern) {
        return this.find(
                quadruplePattern.getGraph(), quadruplePattern.getSubject(),
                quadruplePattern.getPredicate(), quadruplePattern.getObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> find(Node g, Node s, Node p, Node o) {
        Collection<Quadruple> result = new Collection<Quadruple>();

        this.datastore.getLock().enterCriticalSection(Lock.READ);

        try {
            Iterator<Quad> quads = this.datastore.find(g, s, p, o);

            Quad quad;
            while (quads.hasNext()) {
                quad = quads.next();
                result.add(new Quadruple(
                        quad.getGraph(), quad.getSubject(),
                        quad.getPredicate(), quad.getObject()));
            }
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        boolean result;

        this.datastore.getLock().enterCriticalSection(Lock.READ);

        try {
            result = this.datastore.isEmpty();
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean executeSparqAsk(String sparqlAskQuery) {
        Query query = QueryFactory.create(sparqlAskQuery);

        this.datastore.getLock().enterCriticalSection(Lock.READ);
        try {
            QueryExecution qe =
                    QueryExecutionFactory.create(
                            query, this.datastore.toDataset());
            return qe.execAsk();
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model executeSparqlConstruct(String sparqlConstructQuery) {
        Query query = QueryFactory.create(sparqlConstructQuery);

        this.datastore.getLock().enterCriticalSection(Lock.READ);

        try {
            QueryExecution qe =
                    QueryExecutionFactory.create(
                            query, this.datastore.toDataset());
            return qe.execConstruct();
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResultSet executeSparqlSelect(String sparqlSelectQuery) {
        Query query = QueryFactory.create(sparqlSelectQuery);

        this.datastore.getLock().enterCriticalSection(Lock.READ);

        try {
            QueryExecution qe =
                    QueryExecutionFactory.create(
                            query, this.datastore.toDataset());
            return qe.execSelect();
        } finally {
            this.datastore.getLock().leaveCriticalSection();
        }
    }

    /*
     * Implementation of PeerDataHandler interface
     */

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void affectDataReceived(Object dataReceived) {
        this.add((Collection<Quadruple>) dataReceived);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> retrieveAllData() {
        return this.find(QuadruplePattern.ANY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> retrieveDataIn(Object interval) {
        return this.retrieveDataIn(interval, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> removeDataIn(Object interval) {
        return this.retrieveDataIn(interval, new QuadrupleAction() {
            @Override
            public void performAction(Quadruple quad) {
                delete(quad);
            }
        });
    }

    private Collection<Quadruple> retrieveDataIn(Object interval,
                                                 QuadrupleAction action) {
        Zone zone = (Zone) interval;
        String graph, subject, predicate, object;

        Collection<Quadruple> quads = new Collection<Quadruple>();

        for (Quadruple quad : this.find(QuadruplePattern.ANY)) {
            graph = SemanticElement.parseElement(quad.getGraph().toString());

            subject =
                    SemanticElement.parseElement(quad.getSubject().toString());
            predicate =
                    SemanticElement.parseElement(quad.getPredicate().toString());
            object = SemanticElement.parseElement(quad.getObject().toString());

            if (graph.compareTo(zone.getLowerBound((byte) 0).toString()) >= 0
                    && graph.compareTo(zone.getUpperBound((byte) 0).toString()) < 0
                    && subject.compareTo(zone.getLowerBound((byte) 1)
                            .toString()) >= 0
                    && subject.compareTo(zone.getUpperBound((byte) 1)
                            .toString()) < 0
                    && predicate.compareTo(zone.getLowerBound((byte) 2)
                            .toString()) >= 0
                    && predicate.compareTo(zone.getUpperBound((byte) 2)
                            .toString()) < 0
                    && object.compareTo(zone.getLowerBound((byte) 3).toString()) >= 0
                    && object.compareTo(zone.getUpperBound((byte) 3).toString()) < 0) {
                quads.add(quad);
                if (action != null) {
                    action.performAction(quad);
                }
            }
        }

        return quads;
    }

    public static interface QuadrupleAction {

        public void performAction(Quadruple quad);

    }

}
