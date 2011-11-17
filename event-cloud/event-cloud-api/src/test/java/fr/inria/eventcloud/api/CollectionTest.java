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
package fr.inria.eventcloud.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

/**
 * Test cases associated to {@link Collection}.
 * 
 * @author lpellegr
 */
public class CollectionTest {

    @Test
    public void testRecursiveConstruction() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(7);

        Collection<Integer> c =
                new Collection<Integer>(new Collection<Integer>(list));

        Assert.assertEquals(1, c.size());
        Assert.assertEquals(7, (int) c.iterator().next());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        Collection<Integer> collection = new Collection<Integer>(list);

        Collection<Integer> deserializedCollection = null;
        try {
            deserializedCollection =
                    (Collection<Integer>) MakeDeepCopy.makeDeepCopy(collection);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Iterator<Integer> it = collection.iterator();
        Iterator<Integer> it2 = deserializedCollection.iterator();
        while (it.hasNext()) {
            Assert.assertEquals(it.next(), it2.next());
        }
    }

}
