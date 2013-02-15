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
package fr.inria.eventcloud.api.generators;

import com.hp.hpl.jena.graph.Node;

/**
 * Builder for {@link QuadrupleGenerator}.
 * 
 * @author lpellegr
 */
public final class QuadrupleGeneratorBuilder {

    private Node graph = null;

    private String prefix = null;

    private ObjectType objectType = ObjectType.URI;

    private int graphSize = 0;

    private int subjectSize = 0;

    private int predicateSize = 0;

    private int objectSize = 0;

    public enum ObjectType {
        LITERAL, LITERAL_OR_URI, URI,
    }

    public QuadrupleGeneratorBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public QuadrupleGeneratorBuilder setGraph(Node graph) {
        this.graph = graph;
        return this;
    }

    public QuadrupleGeneratorBuilder setGraphSize(int length) {
        this.graphSize = length;
        return this;
    }

    public QuadrupleGeneratorBuilder setSubjectSize(int length) {
        this.subjectSize = length;
        return this;
    }

    public QuadrupleGeneratorBuilder setPredicateSize(int length) {
        this.predicateSize = length;
        return this;
    }

    public QuadrupleGeneratorBuilder setObjectSize(int length) {
        this.objectSize = length;
        return this;
    }

    public QuadrupleGeneratorBuilder setRdfTermSize(int length) {
        this.graphSize = length;
        this.subjectSize = length;
        this.predicateSize = length;
        this.objectSize = length;
        return this;
    }

    public QuadrupleGeneratorBuilder setObjectType(ObjectType type) {
        this.objectType = type;
        return this;
    }

    public QuadrupleGenerator build() {
        return new QuadrupleGenerator(
                this.graph, this.prefix, this.graphSize, this.subjectSize,
                this.predicateSize, this.objectSize, this.objectType);
    }

}
