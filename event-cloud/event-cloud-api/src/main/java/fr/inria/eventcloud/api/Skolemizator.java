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
package fr.inria.eventcloud.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.utils.MurmurHash;

/**
 * Blank nodes do not have identifiers in the RDF abstract syntax. The blank
 * node identifiers introduced by some concrete syntaxes have only local scope
 * and are purely an artifact of the serialization. In situations where stronger
 * identification is needed skolemization can used to replace Blank Nodes with
 * IRIs. That's the purpose of this class.
 * 
 * @author lpellegr
 */
public class Skolemizator {

    private static final String SKOLEM_URI_SUFFIX =
            "http://eventcloud.inria.fr/";

    private static final String SKOLEM_URI_PATH_COMPONENT =
            ".well-known/genid/";

    /**
     * Replaces Blank Nodes with IRIs within the specified {@link CompoundEvent}
     * . The transformation applied is the one described in the last <a href=
     * "https://dvcs.w3.org/hg/rdf/raw-file/default/rdf-concepts/index.html#section-skolemization"
     * >RDF 1.1 draft</a>.
     * 
     * @param ce
     *            the compound event to process.
     * 
     * @return a compound event where Blank Nodes have been replaced by IRIs.
     */
    public static CompoundEvent skolemize(CompoundEvent ce) {
        return new CompoundEvent(Skolemizator.skolemize(ce.getQuadruples()));

    }

    /**
     * Replaces Blank Nodes with IRIs within the specified collection of
     * {@link Quadruple}s . The transformation applied is the one described in
     * the last <a href=
     * "https://dvcs.w3.org/hg/rdf/raw-file/default/rdf-concepts/index.html#section-skolemization"
     * >RDF 1.1 draft</a>.
     * 
     * @param quads
     *            the quadruples to process.
     * 
     * @return a collection of quadruples where Blank Nodes have been replaced
     *         by IRIs.
     */
    public static List<Quadruple> skolemize(Collection<Quadruple> quads) {

        List<Quadruple> result = new ArrayList<Quadruple>();

        Map<Node, Node> assignedSkolems = new HashMap<Node, Node>();

        for (Quadruple q : quads) {
            Node subject = q.getSubject();
            Node object = q.getObject();

            if (subject.isBlank() || object.isBlank()) {
                if (subject.isBlank()) {
                    subject = getOrCreateSkolemUri(subject, assignedSkolems);
                }

                if (object.isBlank()) {
                    object = getOrCreateSkolemUri(object, assignedSkolems);
                }

                result.add(new Quadruple(
                        q.getGraph(), subject, q.getPredicate(), object));
            } else {
                result.add(q);
            }
        }

        return result;
    }

    private static Node getOrCreateSkolemUri(Node subjectOrObject,
                                             Map<Node, Node> assignedSkolems) {

        Node skolem = assignedSkolems.get(subjectOrObject);

        if (skolem == null) {
            skolem = createSkolemUri(subjectOrObject, assignedSkolems);
        }

        return skolem;
    }

    private static Node createSkolemUri(Node subjectOrObject,
                                        Map<Node, Node> assignedSkolems) {
        Node skolem =
                Node.createURI(SKOLEM_URI_SUFFIX
                        + SKOLEM_URI_PATH_COMPONENT
                        + MurmurHash.hash128(
                                subjectOrObject.toString(),
                                UUID.randomUUID().toString()).toString());

        assignedSkolems.put(subjectOrObject, skolem);

        return skolem;
    }

}
