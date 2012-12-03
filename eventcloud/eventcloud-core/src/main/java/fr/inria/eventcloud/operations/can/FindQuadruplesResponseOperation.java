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
package fr.inria.eventcloud.operations.can;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;

import fr.inria.eventcloud.api.Quadruple;

/**
 * Response associated to {@link FindQuadruplesOperation}.
 * 
 * @author lpellegr
 */
public final class FindQuadruplesResponseOperation implements ResponseOperation {

    private static final long serialVersionUID = 140L;

    private final List<Quadruple> quadruples;

    public FindQuadruplesResponseOperation(List<Quadruple> quadruples) {
        this.quadruples = quadruples;
    }

    public List<Quadruple> getQuadruples() {
        return this.quadruples;
    }

}
