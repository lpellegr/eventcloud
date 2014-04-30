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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.util.Random;

/**
 * Utility methods for arrays.
 * 
 * @author lpellegr
 */
public class Arrays {

    /**
     * Shuffles the specified array by using the Fisher–Yates method.
     * 
     * @param array
     */
    public static <T> void shuffle(T[] array) {
        Random random = new Random();

        for (int i = array.length - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);

            T a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    /**
     * Shuffles the specified array with the given random instance by using the
     * Fisher–Yates method.
     * 
     * @param array
     * @param random
     */
    public static <T> void shuffle(T[] array, Random random) {
        for (int i = array.length - 1; i >= 0; i--) {
            int index = random.nextInt(i + 1);

            T a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

}
