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
package fr.inria.eventcloud.translators.wsnotif.handlers;

/**
 * Contains some convenient methods to manipulate XML objects.
 * 
 * @author lpellegr
 */
public class XmlUtils {

    public static String[] splitUri(String uri) {
        int slashIndex = uri.lastIndexOf('/');

        return new String[] {
                uri.substring(0, slashIndex), uri.substring(slashIndex + 1)};
    }

    public static String[] splitQName(String qName) {
        return qName.split(":");
    }

    public static String getLocalNameFromUri(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    public static String createQName(String prefix, String localName) {
        StringBuilder qName = new StringBuilder();
        qName.append(prefix);
        qName.append(":");
        qName.append(localName);
        return qName.toString();
    }

}
