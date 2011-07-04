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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api;

import java.io.InputStream;
import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;

import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * The EventCloudRegistry is in charge of storing all the information related to
 * the Event-Clouds which are runnings for an organization or a group.
 * <p>
 * As a first prototype the registry is centralized and stores the information
 * as {@link Quadruple}s.
 * 
 * @author lpellegr
 */
public class EventCloudsRegistry implements PutGetApi, Serializable {

    private static final long serialVersionUID = 1L;

    // TODO: temporary fields that have to be replaced by an RDF repository.
    private EventCloudId id;

    private Collection<SemanticTracker> trackers;

    public EventCloudId getId() {
        return this.id;
    }

    public Collection<SemanticTracker> getTrackers() {
        return this.trackers;
    }

    public EventCloudsRegistry() {
    }

    public void register(EventCloud eventCloud) {
        this.id = eventCloud.getId();
        this.trackers = eventCloud.getTrackers();
    }

    public String getUrl() {
        return PAActiveObject.getUrl(PAActiveObject.getStubOnThis());
    }

    /*
     * Implementation of the PutGetApi
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Quadruple quad) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Collection<Quadruple> quads) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(InputStream in, SerializationFormat format) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Quadruple quad) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Quadruple quad) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Collection<Quadruple> quads) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> delete(QuadruplePattern quadPattern) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> find(QuadruplePattern quadPattern) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery) {
        // TODO Auto-generated method stub
        return null;
    }

}
