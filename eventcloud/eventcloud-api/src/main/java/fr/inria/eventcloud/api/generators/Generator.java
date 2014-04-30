/**
 * Copyright (c) 2011-2014 INRIA.
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

import java.util.Random;

/**
 * Class extended by any generator. It shares a number generator which is by
 * default an instance of {@link Random}.
 * 
 * @author lpellegr
 */
public abstract class Generator {

    protected static Random RANDOM = new Random();

    protected static final int DEFAULT_LENGTH = 10;

    public static void setNumberGenerator(Random random) {
        RANDOM = random;
    }

}
