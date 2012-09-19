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
package fr.inria.eventcloud.datastore;

import java.util.Collection;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;

/**
 * Defines the operations that may be performed on transaction created by
 * calling {@link TransactionalTdbDatastore#begin(AccessMode)}.
 * 
 * @author lpellegr
 */
public interface TransactionalDatasetGraph {

    void add(Node g, Node s, Node p, Node o);

    void add(Quadruple quadruple);

    void add(Collection<Quadruple> quadruples);

    boolean contains(Quadruple quadruple);

    void delete(Quadruple quadruple);

    void delete(Collection<Quadruple> quadruples);

    void delete(QuadruplePattern quadruplePattern);

    void delete(Node g, Node s, Node p, Node o);

    QuadrupleIterator find(QuadruplePattern quadruplePattern);

    QuadrupleIterator find(Node g, Node s, Node p, Node o);

    void abort();

    void commit();

    void end();

    Dataset getUnderlyingDataset();

}
