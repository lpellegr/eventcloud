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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements;

import java.util.LinkedList;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtil;

/**
 * Embodies a String coordinate element.
 * 
 * @author lpellegr
 */
public class StringElement extends Element<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Contains the value's representation as a list of unicode code points.
     */
    private transient LinkedList<Integer> unicodeCodePoints;

    private static final char[] legalCharacters = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
            'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
            'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',};

    /**
     * Constructs a new element with the specified <code>value</code>.
     * 
     * @param value
     *            the value as string.
     */
    public StringElement(String value) {
        super(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (P2PStructuredProperties.CAN_COORDINATE_DISPLAY.getValue().equals(
                "codepoints")) {
            return UnicodeUtil.asCodePoints(super.value);
        } else {
            return UnicodeUtil.makePrintable(super.value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element<String> middle(Element<String> elt) {
        LinkedList<Integer> sumCodePoints =
                sumUnicodeCodePoints(
                        this.getUnicodeCodePoints(),
                        ((StringElement) elt).getUnicodeCodePoints());

        // computes the middle code point value for each character element
        return new StringElement(
                reversedUnicodeToString(getMiddleUnicodes(sumCodePoints)));
    }

    /**
     * Returns the value associated to this coordinate element as a list of
     * unicode code points.
     * <p>
     * This method is thread-safe and has a lazy behavior: the unicode code
     * points representation is computed during the first call.
     * 
     * @return the value associated to this coordinate element as a list of
     *         unice code points.
     */
    public synchronized LinkedList<Integer> getUnicodeCodePoints() {
        if (this.unicodeCodePoints == null) {
            this.unicodeCodePoints = reversedStringToUnicode(this);
        }

        return this.unicodeCodePoints;
    }

    /**
     * Computes the position of the decimal separator. The position is given by
     * the coordinate element that has the shortest string length.
     * 
     * @param elt1
     *            the first coordinate.
     * @param elt2
     *            the second coordinate.
     * 
     * @return the position of the decimal separator.
     */
    public long getIndexOfDecimalSeparator(StringElement elt1,
                                           StringElement elt2) {
        return Math.min(elt1.getValue().length(), elt2.getValue().length());
    }

    /**
     * Returns the pairwise sum of the code points values. If the two code point
     * lists do not have the same size the remaining elements of the longest
     * list are append at the end.
     * 
     * @param codePtsStr1
     *            unicode code point list of characters in the first string.
     * @param codePtsStr2
     *            unicode code point list of characters in the second string.
     * 
     * @return the sum of unicode code point values.
     */
    public static LinkedList<Integer> sumUnicodeCodePoints(LinkedList<Integer> codePtsStr1,
                                                           LinkedList<Integer> codePtsStr2) {
        LinkedList<Integer> sumCodePoints = new LinkedList<Integer>();
        int minLen = Math.min(codePtsStr1.size(), codePtsStr2.size());
        int maxL = Math.max(codePtsStr1.size(), codePtsStr2.size());

        LinkedList<Integer> longest;
        LinkedList<Integer> shortest;

        if (minLen == codePtsStr1.size()) {
            longest = codePtsStr2;
            shortest = codePtsStr1;
        } else {
            longest = codePtsStr1;
            shortest = codePtsStr2;
        }

        LinkedList<Integer> tmp = new LinkedList<Integer>();
        // add 0 at the begining to ease addition
        for (int i = minLen; i < maxL; i++) {
            tmp.add(0);
        }
        tmp.addAll(shortest);
        shortest = tmp;

        // System.out.println("LexicographicCoordinate.sumUnicodeCodePoints() longest "
        // + longest);
        // System.out.println("LexicographicCoordinate.sumUnicodeCodePoints() shortest "
        // +
        // shortest);
        // );

        // for (int i = 0; i < minLen; i++) {
        // sumCodePoints.add(codePtsStr1.get(i) + codePtsStr2.get(i));
        // }
        //
        // LinkedList<Integer> currentCodePtsStr = codePtsStr2;
        // if (codePtsStr1.size() > codePtsStr2.size()) {
        // currentCodePtsStr = codePtsStr1;
        // }
        //
        // sumCodePoints.addAll(sumCodePoints.size(),
        // currentCodePtsStr.subList(minLen, maxL));
        //
        //

        int carry = 0;
        for (int i = 0; i < maxL; i++) {
            // System.out.println("LexicographicCoordinate.sumUnicodeCodePoints() adding "
            // +
            // codePtsStr1.get(i) + " "+ codePtsStr2.get(i) + " carry " +
            // carry);
            int r = shortest.get(i) + longest.get(i) + carry;
            carry = 0;
            if (r > Math.pow(2, 16.0)) {
                carry = 1;
                r -= Math.pow(2, 16.0);

            }
            sumCodePoints.add(r);
        }

        // reverse the result because of the add
        // int size = sumCodePoints.size();
        // for(int i =0;i<(size/2);i++) {
        // int tmp = sumCodePoints.get(i);
        // sumCodePoints.set(i, sumCodePoints.get(size-1-i));
        // sumCodePoints.set(size-1-i, tmp);
        // }
        //

        // System.out.println("LexicographicCoordinate.sumUnicodeCodePoints()x "
        // + sumCodePoints);
        for (int i = sumCodePoints.size() - 1; i >= 0; i++) {
            if (sumCodePoints.get(i) == 0) {
                sumCodePoints.remove(i);
            } else {
                break;
            }
        }

        return (sumCodePoints);
    }

    /**
     * Returns the unicode code points values for the specified coordinate
     * element. The values are reversed compared to the original coordinate
     * element.
     * 
     * @param elt
     *            the coordinate element.
     * 
     * @return the reversed list of unicode code points values of characters
     *         belonging to the coordinate element.
     */
    public static LinkedList<Integer> reversedStringToUnicode(StringElement elt) {
        LinkedList<Integer> codePtArray = new LinkedList<Integer>();
        for (int i = elt.getValue().length() - 1; i >= 0; i--) {
            int codePt = elt.getValue().codePointAt(i);
            codePtArray.add(codePt);
        }
        return (codePtArray);
    }

    /**
     * Returns the string value from the unicode code point values.
     * 
     * @param codePoints
     *            the middle code point arrays value.
     * 
     * @return the string value from the unicode code point values.
     */
    public static String reversedUnicodeToString(LinkedList<Integer> codePoints) {
        StringBuilder buf = new StringBuilder();
        for (int i = codePoints.size() - 1; i >= 0; i--) {
            buf.append(Character.toChars(codePoints.get(i)));
        }
        return buf.toString();
    }

    /**
     * Returns the middle of elements in the unicode code point list. If the
     * remainder is not <code>null</code>, it will be concatenated with the next
     * element if it exists otherwise it will be appended at the end. This list
     * will be used to retrieve the string value of the middle coordinate
     * element from its code point values.
     * 
     * @param codePoints
     *            the code points values.
     * 
     * @return the unicode code points list.
     */
    private static LinkedList<Integer> getMiddleUnicodes(LinkedList<Integer> codePoints) {
        LinkedList<Integer> middleChrCodePts = new LinkedList<Integer>();
        int cp;
        int remainder;
        int quotient;
        for (int i = codePoints.size() - 1; i >= 0; i--) {
            cp = codePoints.get(i);
            // System.out.println("LexicographicCoordinate.getMiddleUnicodes()  point  "
            // + cp);
            quotient = cp / 2;
            remainder = cp % 2;
            middleChrCodePts.add(quotient);
            // System.out.println("LexicographicCoordinate.getMiddleUnicodes() remainder "
            // +
            // remainder);
            if (remainder != 0) {
                // Shift the remainder to the next element
                if (i > 0) {
                    codePoints.set(i - 1, Integer.parseInt(remainder + ""
                            + codePoints.get(i - 1)));
                    // codePoints.set(i+1,codePoints.get(i+1)+remainder);
                } else {
                    middleChrCodePts.add(remainder);
                }
            }
        }
        // int size = middleChrCodePts.size();
        // for(int i =0;i<(size/2);i++) {
        // int tmp = middleChrCodePts.get(i);
        // middleChrCodePts.set(i, middleChrCodePts.get(size-1-i));
        // middleChrCodePts.set(size-1-i, tmp);
        // }

        // swap order
        int size = middleChrCodePts.size();
        for (int i = 0; i < size / 2; i++) {
            int tmp = middleChrCodePts.get(i);
            middleChrCodePts.set(i, middleChrCodePts.get(size - 1 - i));
            middleChrCodePts.set(size - 1 - i, tmp);
        }
        // System.out.println("LexicographicCoordinate.getMiddleUnicodes() " +
        // middleChrCodePts);

        // restricts interval to legal characters
        // if (!isLegalCharacter((char)middleChrCodePts.get(0).intValue())) {
        // middleChrCodePts.addFirst((int)nearestLegalCharacter((char)middleChrCodePts.get(0).intValue()));
        // }

        return middleChrCodePts;
    }

    private static char nearestLegalCharacter(char c) {
        char nearest = legalCharacters[0];
        int distance = Math.abs(c - legalCharacters[0]);

        int tmpDistance;
        for (int i = 1; i < legalCharacters.length - 1; i++) {
            if ((tmpDistance = (Math.abs(c - legalCharacters[i]))) < distance) {
                nearest = legalCharacters[i];
                distance = tmpDistance;
            } else {
                return nearest;
            }
        }

        return nearest;
    }

    @SuppressWarnings("unused")
    private static boolean isLegalCharacter(char c) {
        for (char legalChar : legalCharacters) {
            if (c == legalChar) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        int lowerBound = 'A';
        int upperBound = 'Z';

        for (int i = lowerBound; i <= lowerBound + (upperBound - lowerBound); i++) {
            System.out.print("'" + (char) i + "'");
            System.out.print(", ");
        }
        System.out.println();
        System.out.println("0 -> " + (int) '0');
        System.out.println("9 -> " + (int) '9');
        System.out.println("A -> " + (int) 'A');
        System.out.println("Z -> " + (int) 'Z');
        System.out.println("a -> " + (int) 'a');
        System.out.println("z -> " + (int) 'z');
        System.out.println((char) 58);

        System.out.println(nearestLegalCharacter((char) 62));
    }

}
