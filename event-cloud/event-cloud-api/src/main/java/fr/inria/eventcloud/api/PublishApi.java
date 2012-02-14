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

import java.io.InputStream;

import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

/**
 * Defines the publish operations that can be executed on an Event Cloud.
 * 
 * @author lpellegr
 */
public interface PublishApi {

    /**
     * Publishes the specified quadruple.
     * 
     * @param quad
     *            the quadruple to publish.
     */
    public void publish(Quadruple quad);

    /**
     * Publishes the specified compound event.
     * 
     * @param event
     *            the compound event to publish.
     */
    public void publish(CompoundEvent event);

    /**
     * Publishes the specified collection of {@link CompoundEvent}s.
     * 
     * @param events
     *            the compound events to publish.
     */
    public void publish(Collection<CompoundEvent> events);

    /**
     * Publishes the quadruples that are read from the specified input stream.
     * The input stream is assumed to comply with the <a
     * href="http://www4.wiwiss.fu-berlin.de/bizer/TriG/">TriG</a> or <a
     * href="http://sw.deri.org/2008/07/n-quads/">N-Quads</a> syntax.
     * 
     * @param in
     *            the input stream from where the quadruples are read.
     * 
     * @param format
     *            the format that is used to read the data from the input
     *            stream.
     */
    public void publish(InputStream in, SerializationFormat format);

}
