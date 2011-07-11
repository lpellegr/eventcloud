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

import org.objectweb.proactive.extensions.p2p.structured.overlay.datastore.PersistentDatastore;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Defines all the methods that a Semantic Datastore has to expose.
 * 
 * @author lpellegr
 */
public abstract class SemanticDatastore extends PersistentDatastore {

    protected SemanticDatastore(File parentPath) {
        super(parentPath);
    }

    /**
     * Adds the specified {@code quad} into the datastore.
     * 
     * @param quad
     *            the quadruple to add into the datastore.
     */
    public abstract void add(Quadruple quad);

    /**
     * Returns a boolean indicating if the specified {@code quad} is contained
     * by the datastore.
     * 
     * @param quad
     *            the quadruple to look for.
     * @return a boolean indicating if the specified {@code quad} is contained
     *         by the datastore.
     */
    public abstract boolean contains(Quadruple quad);

    /**
     * Removes the specified {@code quad} from the datastore.
     * 
     * @param quad
     *            the quadruple to remove.
     */
    public abstract void delete(Quadruple quad);

    /**
     * Deletes any quads matching the specified pattern. To specify a wildcard
     * you can use a {@code null} or {@link Node#ANY} value.
     * 
     * @param g
     *            the graph value.
     * @param s
     *            the subject value.
     * @param p
     *            the predicate value.
     * @param o
     *            the object value.
     */
    public abstract void deleteAny(Node g, Node s, Node p, Node o);

    /**
     * Returns an iterator to iterate on the quadruples that match the specified
     * pattern. To specify a wildcard you can use a {@code null} value or
     * {@link Node#ANY}.
     * 
     * @param g
     *            the graph value.
     * @param s
     *            the subject value.
     * @param p
     *            the predicate value.
     * @param o
     *            the object value.
     * 
     * @return an iterator to iterate on the quadruples that match the specified
     *         pattern and which have been returned.
     */
    public abstract Collection<Quadruple> find(Node g, Node s, Node p, Node o);

    /**
     * Returns a boolean indicating if the repository is empty or not.
     * 
     * @return {@code true} if the repository is empty, {@code false} otherwise.
     */
    public abstract boolean isEmpty();

    /**
     * Returns the result associated to the specified {@code sparqlAskQuery}
     * after having executed it.
     * 
     * @param sparqlAskQuery
     *            the SPARQL Ask query to execute.
     * 
     * @return the result associated to the specified {@code sparqlAskQuery}
     *         after having executed it.
     */
    public abstract boolean executeSparqAsk(String sparqlAskQuery);

    /**
     * Returns the result associated to the specified
     * {@code sparqlConstructQuery} after having executed it.
     * 
     * @param sparqlConstructQuery
     *            the SPARQL Construct query to execute.
     * 
     * @return the result associated to the specified
     *         {@code sparqlConstructQuery} after having executed it.
     */
    public abstract Model executeSparqlConstruct(String sparqlConstructQuery);

    /**
     * Returns the result associated to the specified {@code sparqlSelectQuery}
     * after having executed it.
     * 
     * @param sparqlSelectQuery
     *            the SPARQL Select query to execute.
     * 
     * @return the result associated to the specified {@code sparqlSelectQuery}
     *         after having executed it.
     */
    public abstract ResultSet executeSparqlSelect(String sparqlSelectQuery);

}
