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

/**
 * Default implementation for {@link StatsRecorder}.
 * 
 * @author lpellegr
 */
public class StatsRecorderImpl implements StatsRecorder {

    private Category[] categories;

    public StatsRecorderImpl(int nbCategories, int nbEntriesPerCategory,
            int discardFirstRuns) {
        this.categories = new Category[nbCategories];

        for (int i = 0; i < nbCategories; i++) {
            this.categories[i] =
                    new CategoryImpl(nbEntriesPerCategory, discardFirstRuns);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportTime(int categoryIndex, long time) {
        if (categoryIndex > this.categories.length - 1) {
            throw new IllegalArgumentException("Out of range category index: "
                    + categoryIndex);
        }

        this.categories[categoryIndex].reportTime(time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCategory(int categoryIndex) {
        return this.categories[categoryIndex];
    }

}
