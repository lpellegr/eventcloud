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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.util;

import java.util.Random;

import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

/**
 * 
 * @author lpellegr
 */
public class StringUtil {

    public static String generateRandomString(int length, char inferiorBound,
                                              char superiorBound) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < length; i++) {
            buf.append((char) (inferiorBound + ProActiveRandom.nextInt(superiorBound
                    - inferiorBound)));
        }

        return buf.toString();
    }

    public static String generateRandomString(int length, char[][] bounds) {
        StringBuffer buf = new StringBuffer();
        int lineIndex = 0;

        for (int i = 0; i < length; i++) {
            lineIndex = ProActiveRandom.nextInt(bounds.length);

            buf.append((char) (bounds[lineIndex][0] + ProActiveRandom.nextInt(bounds[lineIndex][1]
                    - bounds[lineIndex][0])));
        }

        return buf.toString();
    }

    public static String generateRandomString(String prefix, int length,
                                              char[][] bounds) {
        return prefix + StringUtil.generateRandomString(length, bounds);
    }

    public static String generateRandomString(String prefix, int length,
                                              char inferiorBound,
                                              char superiorBound) {
        return prefix
                + StringUtil.generateRandomString(
                        length, inferiorBound, superiorBound);
    }

    public static void main(String[] args) {
        Random rand = new Random();
        int randomNumber;

        for (int i = 0; i < 10; i++) {
            randomNumber = 1 + rand.nextInt(20);
            System.out.println("generateRandomString("
                    + randomNumber
                    + ", '"
                    + P2PStructuredProperties.CAN_LOWER_BOUND.getValueAsString()
                    + "', '"
                    + P2PStructuredProperties.CAN_UPPER_BOUND.getValueAsString()
                    + "')="
                    + StringUtil.generateRandomString(
                            randomNumber,
                            P2PStructuredProperties.CAN_LOWER_BOUND.getValueAsString()
                                    .charAt(0),
                            P2PStructuredProperties.CAN_UPPER_BOUND.getValueAsString()
                                    .charAt(0)));
        }

        System.out.println();

        System.out.println("generateRandomString(\"http://\", 15, '"
                + P2PStructuredProperties.CAN_LOWER_BOUND.getValueAsString()
                + "', '"
                + P2PStructuredProperties.CAN_UPPER_BOUND.getValueAsString()
                + "')="
                + StringUtil.generateRandomString(
                        "http://",
                        15,
                        P2PStructuredProperties.CAN_LOWER_BOUND.getValueAsString()
                                .charAt(0),
                        P2PStructuredProperties.CAN_UPPER_BOUND.getValueAsString()
                                .charAt(0)));

        System.out.println();

        System.out.println(StringUtil.generateRandomString(
                "http://", 5,
                new char[][] { {'0', '9'}, {'A', 'Z'}, {'a', 'z'}}));
    }
}
