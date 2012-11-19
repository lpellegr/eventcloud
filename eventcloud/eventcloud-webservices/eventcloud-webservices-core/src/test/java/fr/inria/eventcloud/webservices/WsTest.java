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
package fr.inria.eventcloud.webservices;

/**
 * Abstract class common to all web service tests.
 * 
 * @author bsauvan
 */
public abstract class WsTest {

    protected static final int WEBSERVICES_PORT = getWebservicesPort();

    private static final int getWebservicesPort() {
        String port = System.getProperty("eventcloud.webservices.port");

        if (port != null) {
            return Integer.parseInt(port);
        }

        return 42999;
    }

}