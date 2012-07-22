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
package fr.inria.eventcloud.reasoner;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

import fr.inria.eventcloud.api.Quadruple;

/**
 * This ARQ function is used for removing meta-information from a graph value.
 * 
 * @author lpellegr
 */
public class RemoveMetadataFunction extends FunctionBase1 {

    public RemoveMetadataFunction() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeValue exec(NodeValue v) {
        Node n = v.asNode();

        return NodeValue.makeNode(Quadruple.removeMetaInformation(n));
    }

}
