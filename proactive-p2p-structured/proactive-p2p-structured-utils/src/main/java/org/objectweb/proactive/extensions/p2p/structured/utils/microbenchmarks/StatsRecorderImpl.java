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
package org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Default implementation for {@link StatsRecorder}.
 * 
 * @author lpellegr
 */
public class StatsRecorderImpl implements StatsRecorder {

    private Map<String, Category> categories;

    public StatsRecorderImpl(String[] categoryNames, int nbEntriesPerCategory,
            int discardFirstRuns) {
        Builder<String, Category> builder =
                ImmutableMap.<String, Category> builder();

        for (int i = 0; i < categoryNames.length; i++) {
            builder.put(categoryNames[i], new CategoryImpl(
                    nbEntriesPerCategory, discardFirstRuns));
        }

        this.categories = builder.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportTime(String categoryName, long time) {
        if (!this.categories.containsKey(categoryName)) {
            throw new IllegalArgumentException("Unknow category name "
                    + categoryName);
        }

        this.categories.get(categoryName).reportTime(time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCategory(String name) {
        return this.categories.get(name);
    }

}
