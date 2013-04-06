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
package fr.inria.eventcloud.benchmarks.pubsub;

/**
 * The purpose of this class is to offer methods that allows to a) check whether
 * a code point is part of an unreserved range of an IRI, b) find the best
 * substitute (in terms of code point distance with an unreserved range) if a
 * codepoint is not in an unreserved IRI range.
 * 
 * @author lpellegr
 */
public class IRIFixer {

    /*
     * "Unicode characters I hate you"™
     */

    // private static final int[] UCSCHARS_FIXED = {0x2D, 0x2E, 0x5F, 0x7E};

    // range bounds are inclusive
    private static final int[] UCSCHARS_RANGE_LB = {
            0x30, 0x41, 0x61, 0xA0, 0xF900, 0xFDF0, 0x10000, 0x20000, 0x30000,
            0x40000, 0x50000, 0x60000, 0x70000, 0x80000, 0x90000, 0xA0000,
            0xB0000, 0xC0000, 0xD0000, 0xE1000};

    private static final int[] UCSCHARS_RANGE_UB = {
            0x39, 0x5A, 0x7A, 0xD7FF, 0xFDCF, 0xFFEF, 0x1FFFD, 0x2FFFD,
            0x3FFFD, 0x4FFFD, 0x5FFFD, 0x6FFFD, 0x7FFFD, 0x8FFFD, 0x9FFFD,
            0xAFFFD, 0xBFFFD, 0xCFFFD, 0xDFFFD, 0xEFFFD

    };

    private static final int[][] UCSCHARS = {
            // UCSCHARS_FIXED,
            UCSCHARS_RANGE_LB, UCSCHARS_RANGE_UB};

    public static final boolean isUnreserved(int codepoint) {
        // for (int i = 0; i < UCSCHARS_FIXED.length; i++) {
        // if (codepoint == UCSCHARS_FIXED[i]) {
        // return true;
        // }
        // }

        for (int i = 0; i < UCSCHARS_RANGE_LB.length; i++) {
            if (codepoint >= UCSCHARS_RANGE_LB[i]
                    && codepoint <= UCSCHARS_RANGE_UB[i]) {
                return true;
            }
        }

        return false;
    }

    public static int findBestSubstitute(int codepoint) {
        int bestCandidate = 0;
        int diff = Integer.MAX_VALUE;

        out : {
            for (int i = 0; i < UCSCHARS.length; i++) {
                for (int j = 0; j < UCSCHARS[i].length; j++) {
                    int tmpDiff = Math.abs(codepoint - UCSCHARS[i][j]);

                    if (tmpDiff < diff) {
                        bestCandidate = UCSCHARS[i][j];
                        diff = tmpDiff;
                    }

                    if (tmpDiff == 1) {
                        break out;
                    }
                }
            }
        }

        return bestCandidate;
    }

    public static void main(String[] args) {
        System.out.println(11 - 0xB);
    }

}
