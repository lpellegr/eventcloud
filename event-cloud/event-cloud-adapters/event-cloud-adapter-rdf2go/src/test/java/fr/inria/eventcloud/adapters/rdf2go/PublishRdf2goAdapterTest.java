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
package fr.inria.eventcloud.adapters.rdf2go;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Provides test cases for the {@link PublishRdf2goAdapter}. This class provides
 * tests in order to test whether the translation between RDF2Go objects and
 * event-cloud-api/Jena objects works.
 * 
 * @author lpellegr
 */
public class PublishRdf2goAdapterTest extends
        Rdf2goAdapterTest<PublishRdf2goAdapter> {

    @Before
    public void setUp() {
        this.adapter = new PublishRdf2goAdapter(new MockPublishProxy());
    }

    @Test
    public void testPublishURIResourceURINode() {
        this.adapter.publish(uri, uri, uri, uri);
    }

    @Test
    public void testPublishStatement() {
        this.adapter.publish(model.createStatement(uri, uri, uri));
    }

    @Ignore
    public void testPublishInputStreamSerializationFormat() {
        // we don't have to test it because there is no RDF2Go object that is
        // used as input or output
    }

    @Ignore
    public void testSubscribeStringBindingsNotificationListener() {
        // we don't have to test it because there is no RDF2Go object that is
        // used as input or output
    }

    @Ignore
    public void testSubscribeStringEventsNotificationListener() {
        // we don't have to test it because there is no RDF2Go object that is
        // used as input or output
    }

    @Ignore
    public void testUnsubscribe() {
        // we don't have to test it because there is no RDF2Go object that is
        // used as input or output
    }

}
