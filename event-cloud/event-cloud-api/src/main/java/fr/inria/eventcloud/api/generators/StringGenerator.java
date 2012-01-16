/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud.api.generators;

/**
 * Utility class that defines some convenient methods to create Strings.
 * 
 * @author lpellegr
 */
public final class StringGenerator extends Generator {

    private StringGenerator() {

    }

    public static String create(int minLength, int maxLength,
                                char inferiorBound, char superiorBound) {
        StringBuffer buf = new StringBuffer();

        int length = minLength + random.nextInt(maxLength - minLength);

        for (int i = 0; i < length; i++) {
            buf.append((char) (inferiorBound + random.nextInt(superiorBound
                    - inferiorBound)));
        }

        return buf.toString();
    }

    public static String create(int minLength, int maxLength, char[][] bounds) {
        StringBuffer buf = new StringBuffer();
        int lineIndex = 0;

        int delta = maxLength - minLength;

        int length = minLength + (delta > 0
                ? random.nextInt(delta) : 0);

        for (int i = 0; i < length; i++) {
            lineIndex = random.nextInt(bounds.length);

            buf.append((char) (bounds[lineIndex][0] + random.nextInt(bounds[lineIndex][1]
                    - bounds[lineIndex][0])));
        }

        return buf.toString();
    }

    public static String create(int length, char[][] bounds) {
        return create(length, length, bounds);
    }

    public static String create(String prefix, int length, char[][] bounds) {
        return prefix.concat(create(length, length, bounds));
    }

    public static String create(String prefix, int minLength, int maxLength,
                                char[][] bounds) {
        return prefix.concat(create(minLength, maxLength, bounds));
    }

    public static String create(String prefix, int length, char inferiorBound,
                                char superiorBound) {
        return prefix.concat(create(
                length, length, inferiorBound, superiorBound));
    }

    public static String create(String prefix, int minLength, int maxLength,
                                char inferiorBound, char superiorBound) {
        return prefix.concat(create(
                minLength, maxLength, inferiorBound, superiorBound));
    }

}
