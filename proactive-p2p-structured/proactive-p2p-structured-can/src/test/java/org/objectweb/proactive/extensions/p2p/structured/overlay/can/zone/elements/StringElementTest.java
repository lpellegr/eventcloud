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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link StringElement} class.
 * 
 * @author lpellegr
 */
public class StringElementTest {

    private static final Logger logger =
            LoggerFactory.getLogger(StringElementTest.class);

    @Test
    public void test() {
        LinkedList<Integer> al = this.alpha1();
        String min = StringElement.fromUnicodeToString(al);
        StringElement emin = new StringElement(min);

        LinkedList<Integer> al2 = alpha2();
        String max = StringElement.fromUnicodeToString(al2);

        logger.info("Second String " + max + " " + al2);
        logger.info("Test Sum " + StringElement.sumUnicodeCodePoints(al, al2));

        StringElement cmax = new StringElement(max);
        StringElement c = (StringElement) Element.middle(emin, cmax);

        logger.info("Middle is  : " + c);

        logger.info("Compare min middle " + emin.compareTo(c));
        logger.info("Compare middle max " + c.compareTo(new StringElement(max)));

        logger.info("Compare 'a' and 'b': " + "a".compareTo("b"));
    }

    public LinkedList<Integer> alpha1() {
        StringElement elt = new StringElement("a");
        return StringElement.fromStringtoUnicode(elt);
    }

    public LinkedList<Integer> alpha2() {
        StringElement elt = new StringElement("z");
        return StringElement.fromStringtoUnicode(elt);

    }

    public static ArrayList<Integer> alpha3() {
        ArrayList<Integer> al = new ArrayList<Integer>();
        al.add(104);
        al.add(7);
        al.add(63);
        al.add(435);
        al.add(55047);
        al.add(6202);
        al.add(692);
        al.add(232);
        al.add(29);
        al.add(97);
        return al;
    }

    public ArrayList<Integer> alpha4() {
        ArrayList<Integer> al2 = new ArrayList<Integer>();
        al2.add(104);
        al2.add(7);
        al2.add(63);
        al2.add(436);
        al2.add(670);
        al2.add(5894);
        al2.add(68);
        al2.add(44);
        al2.add(4);
        al2.add(30);
        return al2;
    }

    /**
     * Test many successive splits.
     * 
     * @param args
     */
    public static void main(String[] args) {
        // LexicographicCoordinate.test();

        StringElement coord1;
        StringElement middleCoord = new StringElement("ax");
        StringElement coord2 = new StringElement("bz");

        int nbOfSplit = 3;
        while (nbOfSplit > 0) {
            coord1 = new StringElement(middleCoord.getValue());
            middleCoord = (StringElement) Element.middle(middleCoord, coord2);
            System.out.println("[" + coord1.getValue() + ","
                    + middleCoord.getValue() + "[" + ",["
                    + middleCoord.getValue() + "," + coord2.getValue() + "[");
            nbOfSplit--;
        }
    }

}
