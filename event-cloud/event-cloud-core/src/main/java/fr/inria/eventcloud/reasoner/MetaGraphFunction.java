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

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

/**
 * This ARQ function serves as a marker for detecting that graph value must be
 * returned with meta-information.
 * 
 * @author lpellegr
 */
public class MetaGraphFunction extends FunctionBase1 {

    public MetaGraphFunction() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeValue exec(NodeValue v) {
        return v;
    }

}
