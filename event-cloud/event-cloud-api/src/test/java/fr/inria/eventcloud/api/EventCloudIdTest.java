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

import junit.framework.Assert;

import org.junit.Test;

import fr.inria.eventcloud.utils.LongLong;
import fr.inria.eventcloud.utils.MurmurHash;

/**
 * Test cases associated to the {@link EventCloudId} class.
 * 
 * @author lpellegr
 */
public class EventCloudIdTest {

    @Test
    public void testFromUrl() {
        long[] hashValue = MurmurHash.hash128("test");

        EventCloudId id = new EventCloudId(new LongLong(hashValue));

        Assert.assertEquals(id, EventCloudId.fromUrl(id.toUrl()));
    }

}