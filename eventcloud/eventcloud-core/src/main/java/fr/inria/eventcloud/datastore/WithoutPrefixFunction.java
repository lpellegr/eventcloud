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

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * This ARQ function is used to remove prefixes from a RDF term as for data that
 * are indexed on an eventcloud.
 * 
 * @author lpellegr
 */
public class WithoutPrefixFunction extends FunctionBase1 {

    public static final String NAME = "withoutPrefix";

    public static final String URI =
            EventCloudProperties.FILTER_FUNCTIONS_NS.getValue() + NAME;

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeValue exec(NodeValue v) {
        return NodeValue.makeString(SemanticElement.removePrefix(NodeFunctions.str(v.asNode())));
    }

}
