/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.load_balancing;

import java.io.Serializable;

/**
 * Provides load information. Mainly used to disseminate load to other peers
 * when a relative strategy is applied.
 * 
 * @author lpellegr
 */
public class LoadReport implements Serializable {

    private static final long serialVersionUID = 160L;

    private final long creationTime;

    private final String peerURL;

    private final double[] values;

    public LoadReport(String peerURL, double[] values) {
        this.creationTime = System.currentTimeMillis();
        this.peerURL = peerURL;
        this.values = values;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getPeerURL() {
        return this.peerURL;
    }

    public double[] getValues() {
        return this.values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.creationTime);
        result.append(" -> ");

        for (int i = 0; i < this.values.length; i++) {
            result.append(this.values[i]);

            if (i < this.values.length - 1) {
                result.append(' ');
            }
        }

        return result.toString();
    }

}
