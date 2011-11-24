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
package fr.inria.eventcloud.api.generators;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * Class which must be extended by any generator.
 * 
 * @author lpellegr
 */
public abstract class Generator {

    protected static final SecureRandom random = new SecureRandom();

    protected static final String LEGAL_CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Generates an URI with a random suffix and the specified {@code prefix}.
     * 
     * @return an URI with a random suffix and the specified {@code prefix}.
     */
    public static URI generateRandomUri(String prefix) {
        StringBuilder result = new StringBuilder(prefix);

        for (int i = 0; i < 20; i++) {
            result.append(random.nextInt(LEGAL_CHARS.length()));
        }

        try {
            return new URI(result.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates an URI with a random suffix and a prefix equals to
     * {@link EventCloudProperties#EVENT_CLOUD_ID_PREFIX}.
     * 
     * @return an URI with a random suffix and a prefix equals to
     *         {@link EventCloudProperties#EVENT_CLOUD_ID_PREFIX}.
     */
    public static URI generateRandomUri() {
        return generateRandomUri(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue());
    }

}
