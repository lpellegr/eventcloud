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
package fr.inria.eventcloud.utils;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;

/**
 * Some utility methods to perform conversion from or to Jena objects.
 * 
 * @author lpellegr
 */
public class JenaConverter {

    /**
     * Converts an {@link Iterable} for {@link Quadruple} objects from the
     * EventCloud API to a {@link DatasetGraph}.
     * 
     * @param quadruples
     *            the object to convert.
     * 
     * @return a Jena dataset.
     */
    public static DatasetGraph toDatasetGraph(Iterable<Quadruple> quadruples) {
        DatasetGraph dsg = DatasetGraphFactory.createMem();

        for (Quadruple quadruple : quadruples) {
            if (!PublishSubscribeUtils.isMetaQuadruple(quadruple)) {
                dsg.add(
                        quadruple.getGraph(), quadruple.getSubject(),
                        quadruple.getPredicate(), quadruple.getObject());
            }
        }

        return dsg;
    }

    /**
     * Converts a quadruple from the Jena API to the EventCloud API.
     * 
     * @param quad
     *            the Jena quadruple to convert.
     * 
     * @return a quadruple instance from the EventCloud API.
     */
    public static Quadruple toQuadruple(Quad quad) {
        return toQuadruple(quad, true, true);
    }

    /**
     * Converts a quadruple from the Jena API to the EventCloud API.
     * 
     * @param quad
     *            the Jena quadruple to convert.
     * @param checkType
     *            indicates whether the type of the RDF terms has to be checked.
     * @param parseMetaInformation
     *            indicates whether the graph value should be parsed for
     *            potential meta information.
     * 
     * @return a quadruple instance from the EventCloud API.
     */
    public static Quadruple toQuadruple(Quad quad, boolean checkType,
                                        boolean parseMetaInformation) {
        return new Quadruple(
                quad.getGraph(), quad.getSubject(), quad.getPredicate(),
                quad.getObject(), checkType, parseMetaInformation);
    }

}
