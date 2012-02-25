/**
 * Copyright (c) 2011-2012 INRIA.
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
package fr.inria.eventcloud.webservices.putget;

import java.util.Collection;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;

/**
 * Component used to simulate a client of a put/get proxy by using web services.
 * 
 * @author bsauvan
 */
public class PutGetComponentImpl implements PutGetWsApi, BindingController {

    public static final String PUTGET_WEBSERVICES_NAME = "putget-webservices";

    private PutGetWsApi putgetWs;

    public PutGetComponentImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addQuadruple(Quadruple quad) {
        if (this.putgetWs != null) {
            return this.putgetWs.addQuadruple(quad);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addQuadrupleCollection(Collection<Quadruple> quads) {
        if (this.putgetWs != null) {
            return this.putgetWs.addQuadrupleCollection(quads);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsQuadruple(Quadruple quad) {
        if (this.putgetWs != null) {
            return this.putgetWs.containsQuadruple(quad);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteQuadruple(Quadruple quad) {
        if (this.putgetWs != null) {
            return this.putgetWs.deleteQuadruple(quad);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteQuadrupleCollection(Collection<Quadruple> quads) {
        if (this.putgetWs != null) {
            return this.putgetWs.deleteQuadrupleCollection(quads);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> deleteQuadruplePattern(QuadruplePattern quadPattern) {
        if (this.putgetWs != null) {
            return this.putgetWs.deleteQuadruplePattern(quadPattern);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> findQuadruplePattern(QuadruplePattern quadPattern) {
        if (this.putgetWs != null) {
            return this.putgetWs.findQuadruplePattern(quadPattern);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery) {
        if (this.putgetWs != null) {
            return this.putgetWs.executeSparqlQuery(sparqlQuery);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
        if (this.putgetWs != null) {
            return this.putgetWs.executeSparqlAsk(sparqlAskQuery);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
        if (this.putgetWs != null) {
            return this.putgetWs.executeSparqlConstruct(sparqlConstructQuery);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
        if (this.putgetWs != null) {
            return this.putgetWs.executeSparqlDescribe(sparqlDescribeQuery);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery) {
        if (this.putgetWs != null) {
            return this.putgetWs.executeSparqlSelect(sparqlSelectQuery);
        } else {
            return null;
        }
    }

    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (PUTGET_WEBSERVICES_NAME.equals(clientItfName)) {
            this.putgetWs = (PutGetWsApi) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public String[] listFc() {
        return new String[] {PUTGET_WEBSERVICES_NAME};
    }

    public Object lookupFc(String clientItfName)
            throws NoSuchInterfaceException {
        if (PUTGET_WEBSERVICES_NAME.equals(clientItfName)) {
            return this.putgetWs;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (PUTGET_WEBSERVICES_NAME.equals(clientItfName)) {
            this.putgetWs = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

}
