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
package org.objectweb.proactive.extensions.p2p.structured.operations;

/**
 * Defines a basic boolean (true/false) response operation.
 * 
 * @author lpellegr
 */
public class BooleanResponseOperation extends GenericResponseOperation<Boolean> {

    private static final long serialVersionUID = 160L;

    private static BooleanResponseOperation NEGATIVE_INSTANCE =
            new BooleanResponseOperation(false);

    private static BooleanResponseOperation POSITIVE_INSTANCE =
            new BooleanResponseOperation(true);

    /**
     * Constructor.
     * 
     * @param value
     *            Indicates whether the operation has been handled properly.
     */
    private BooleanResponseOperation(boolean value) {
        super(value);
    }

    public static BooleanResponseOperation getInstance(boolean value) {
        if (value) {
            return POSITIVE_INSTANCE;
        }

        return NEGATIVE_INSTANCE;
    }

    public static BooleanResponseOperation getNegativeInstance() {
        return NEGATIVE_INSTANCE;
    }

    public static BooleanResponseOperation getPositiveInstance() {
        return POSITIVE_INSTANCE;
    }

}
