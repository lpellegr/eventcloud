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

import java.util.Random;
import java.util.UUID;

/**
 * Utility class that defines a convenient method to create arbitrary
 * {@link UUID} that does not start by a digit.
 * 
 * @author bsauvan
 */
public class UuidGenerator {

    private static Random random = new Random();

    /**
     * Returns the string representation of a randomly generated {@link UUID}
     * that does not start by a digit.
     * 
     * @return the string representation of a randomly generated {@link UUID}
     *         that does not start by a digit.
     */
    public static String randomUuid() {
        String randomUuid = UUID.randomUUID().toString();

        if (Character.isDigit(randomUuid.charAt(0))) {
            randomUuid =
                    ((char) (random.nextInt(26) + 'a'))
                            + randomUuid.substring(1);
        }

        return randomUuid;
    }
}
