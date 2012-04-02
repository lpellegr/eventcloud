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

import java.io.Serializable;
import java.util.LinkedList;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * The implementation is based on a <a href="http://paul-ebermann.tumblr.com/
 * post/6277562800/big-numbers-self-made-part-0-14-introduction" title="Big
 * numbers self-made">tutorial</a> made by Paulo Ebermann but adapted to support
 * decimals values.
 * 
 * @author lpellegr
 */
public class DecimalBigInt implements Comparable<DecimalBigInt>, Serializable {

    private static final long serialVersionUID = 1L;

    private final int radix;

    // use big-endian representation (read from left to right)
    private final LinkedList<Integer> digits;

    // indicates the position of the decimal separator (e.g. if decimal index is
    // set to 0 then the decimal separator is between index 0 and 1).
    private int decimalIndex;

    private DecimalBigInt(LinkedList<Integer> c, int decimalIndex, int radix) {
        this.digits = Lists.newLinkedList(c);
        this.decimalIndex = decimalIndex;
        this.radix = radix;

        this.decimalIndex -= this.removeLeadingZeros();
        this.removeTrailingZeros();
    }

    private static void checkRadix(DecimalBigInt thiss, DecimalBigInt that) {
        if (thiss.radix != that.radix) {
            throw new IllegalArgumentException(
                    "The numbers must use the same radix");
        }
    }

    /**
     * Calculates the sum {@code this + that}.
     * 
     * @return the sum {@code this + that}.
     */
    public DecimalBigInt plus(DecimalBigInt that) {
        checkRadix(this, that);
        normalize(this, that);

        LinkedList<Integer> result =
                createLinkedList(Math.max(this.length(), that.length()) + 1);

        addDigits(result, result.size() - 1, this.digits);
        addDigits(result, result.size() - 1, that.digits);

        int newDecimalIndex = this.decimalIndex;
        if (result.getFirst() == 0) {
            result.removeFirst();
        } else {
            newDecimalIndex++;
        }

        return new DecimalBigInt(result, newDecimalIndex, this.radix);
    }

    private void addDigits(LinkedList<Integer> result, int resultIndex,
                           LinkedList<Integer> addend) {
        for (int i = addend.size() - 1; i >= 0; i--) {
            addDigit(result, resultIndex, addend.get(i));
            resultIndex--;
        }
    }

    private void addDigit(LinkedList<Integer> result, int resultIndex,
                          int addendDigit) {
        int sum = result.get(resultIndex) + addendDigit;
        int carry = sum / this.radix;

        result.set(resultIndex, sum % this.radix);

        if (carry > 0) {
            addDigit(result, resultIndex - 1, carry);
        }
    }

    private static LinkedList<Integer> createLinkedList(int size) {
        LinkedList<Integer> result = new LinkedList<Integer>();
        for (int i = 0; i < size; i++) {
            result.add(0);
        }

        return result;
    }

    private static LinkedList<Integer> createLinkedList(int... digits) {
        LinkedList<Integer> result = new LinkedList<Integer>();
        for (int i = 0; i < digits.length; i++) {
            result.add(digits[i]);
        }
        return result;
    }

    /**
     * Returns the product {@code this × that}.
     * 
     * @return the product {@code this × that}.
     */
    public DecimalBigInt times(DecimalBigInt that) {
        checkRadix(this, that);
        normalize(this, that);

        LinkedList<Integer> result =
                createLinkedList(this.digits.size() + that.digits.size());

        multiplyDigits(result, result.size() - 1, this.digits, that.digits);

        int newDecimalIndex =
                result.size()
                        - (this.length() - this.decimalIndex + that.length()
                                - that.decimalIndex - 2) - 1;

        return new DecimalBigInt(result, newDecimalIndex, this.radix);
    }

    private void multiplyDigits(LinkedList<Integer> result, int resultIndex,
                                LinkedList<Integer> leftFactor,
                                LinkedList<Integer> rightFactor) {
        for (int i = 0; i < leftFactor.size(); i++) {
            for (int j = 0; j < rightFactor.size(); j++) {
                multiplyDigit(
                        result, resultIndex - (i + j),
                        leftFactor.get(leftFactor.size() - i - 1),
                        rightFactor.get(rightFactor.size() - j - 1));
            }
        }
    }

    private void multiplyDigit(LinkedList<Integer> result, int resultIndex,
                               Integer firstFactor, Integer secondFactor) {
        long prod = (long) firstFactor * (long) secondFactor;
        int prodDigit = (int) (prod % this.radix);
        int carry = (int) (prod / this.radix);

        addDigits(result, resultIndex, createLinkedList(carry, prodDigit));
    }

    /**
     * Divides this number by two.
     */
    public DecimalBigInt divideByTwo() {
        LinkedList<Integer> workingCopy = Lists.newLinkedList(this.digits);

        addTrailingZeros(workingCopy, 1);

        LinkedList<Integer> result = createLinkedList(workingCopy.size());

        divideDigits(result, 0, workingCopy, 0, 2);

        return new DecimalBigInt(result, this.decimalIndex, this.radix);
    }

    private int divideDigits(LinkedList<Integer> result, int resultIndex,
                             LinkedList<Integer> divident, int dividentIndex,
                             int divisor) {
        int remainder = 0;

        for (; dividentIndex < divident.size(); dividentIndex++, resultIndex++) {
            remainder =
                    divideDigit(
                            result, resultIndex, divident.get(dividentIndex),
                            remainder, divisor);
        }

        return remainder;
    }

    private int divideDigit(LinkedList<Integer> result, int resultIndex,
                            int divident, int lastRemainder, int divisor) {
        // assert divisor < this.radix;
        // assert lastRemainder < divisor;

        long ent = divident + (long) this.radix * lastRemainder;
        long quot = ent / divisor;
        long remainder = ent % divisor;

        // assert quot < this.radix;
        // assert remainder < divisor;

        result.set(resultIndex, (int) quot);
        return (int) remainder;
    }

    public static void normalize(DecimalBigInt thiss, DecimalBigInt that) {
        int delta = thiss.decimalIndex - that.decimalIndex;

        if (delta > 0) {
            that.shift(delta);
        } else if (delta < 0) {
            thiss.shift(Math.abs(delta));
        }

        if (thiss.digits.size() > that.digits.size()) {
            that.addTrailingZeros(thiss.digits.size() - that.digits.size());
        } else {
            thiss.addTrailingZeros(that.digits.size() - thiss.digits.size());
        }
    }

    private void shift(int n) {
        for (int i = 0; i < n; i++) {
            this.digits.addFirst(0);
            this.decimalIndex++;
        }
    }

    private int removeLeadingZeros() {
        return removeLeadingZeros(this.digits, this.decimalIndex);
    }

    private static int removeLeadingZeros(LinkedList<Integer> digits,
                                          int decimalIndex) {
        int nbRemoveOp = 0;
        int counter = decimalIndex;

        while (digits.getFirst() == 0 && counter > 0) {
            digits.removeFirst();
            counter--;
            nbRemoveOp++;
        }

        return nbRemoveOp;
    }

    private int removeTrailingZeros() {
        return removeTrailingZeros(this.digits, this.decimalIndex);
    }

    private static int removeTrailingZeros(LinkedList<Integer> digits,
                                           int decimalIndex) {
        int nbRemoveOp = 0;

        while (digits.getLast() == 0 && digits.size() > decimalIndex + 1) {
            digits.removeLast();
            nbRemoveOp++;
        }

        return nbRemoveOp;
    }

    private void addTrailingZeros(int n) {
        addTrailingZeros(this.digits, n);
    }

    private static void addTrailingZeros(LinkedList<Integer> digits, int n) {
        for (int i = 0; i < n; i++) {
            digits.addLast(0);
        }
    }

    public int length() {
        return this.digits.size();
    }

    public int getDecimalSeparatorIndex() {
        return this.decimalIndex;
    }

    public LinkedList<Integer> getDigits() {
        return this.digits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(DecimalBigInt that) {
        if (this.radix != that.radix) {
            throw new IllegalArgumentException(
                    "Decimal numbers to compare must use the same radix");
        }

        normalize(this, that);

        for (int i = 0; i < this.length(); i++) {
            int thisDigit = this.digits.get(i);
            int thatDigit = that.digits.get(i);

            if (thisDigit != thatDigit) {
                return thisDigit - thatDigit;
            }
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof DecimalBigInt
                && this.radix == ((DecimalBigInt) that).radix
                && this.decimalIndex == ((DecimalBigInt) that).decimalIndex) {

            this.removeLeadingZeros();
            ((DecimalBigInt) that).removeLeadingZeros();
            this.removeTrailingZeros();
            ((DecimalBigInt) that).removeTrailingZeros();

            return this.digits.equals(((DecimalBigInt) that).digits);

        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(this.radix, this.decimalIndex, this.digits);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.digits.size(); i++) {
            result.append(Integer.toString(this.digits.get(i)));

            if (i == this.decimalIndex
                    && this.decimalIndex != this.digits.size() - 1) {
                result.append(',');
            }
        }

        return result.toString();
    }

    public static DecimalBigInt create(int... digits) {
        return new DecimalBigInt(
                createLinkedList(digits), digits.length - 1, 10);
    }

    public static DecimalBigInt create(int[] digits, int decimalIndex) {
        return new DecimalBigInt(createLinkedList(digits), decimalIndex, 10);
    }

    public static DecimalBigInt create(int[] digits, int decimalIndex, int radix) {
        return new DecimalBigInt(createLinkedList(digits), decimalIndex, radix);
    }

    public static DecimalBigInt create(LinkedList<Integer> digits,
                                       int decimalIndex, int radix) {
        return new DecimalBigInt(digits, decimalIndex, radix);
    }

}
