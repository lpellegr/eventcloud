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
package fr.inria.eventcloud.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

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
     * Replaces blank nodes with IRIs within the specified collection of
     * {@link Quadruple}s. The transformation applied is the one described in
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
    public static List<Quadruple> skolemize(Iterable<Quadruple> quads) {

        Builder<Quadruple> result = new ImmutableList.Builder<Quadruple>();

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
                        q.createMetaGraphNode(), subject, q.getPredicate(),
                        object, false, true));
            } else {
                result.add(q);
            }
        }

        return result.build();
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
        UUID randomId = UUID.randomUUID();

        Hasher hasher = Hashing.murmur3_128().newHasher();
        hasher.putString(subjectOrObject.toString(), Charsets.UTF_8);
        hasher.putLong(randomId.getMostSignificantBits());
        hasher.putLong(randomId.getLeastSignificantBits());

        Node skolem =
                NodeFactory.createURI(SKOLEM_URI_SUFFIX
                        + SKOLEM_URI_PATH_COMPONENT + hasher.hash().toString());

        assignedSkolems.put(subjectOrObject, skolem);

        return skolem;
    }

}
