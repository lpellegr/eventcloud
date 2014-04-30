/**
 * Copyright (c) 2011-2014 INRIA.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation for {@link StatsRecorder}.
 * 
 * @author lpellegr
 */
public class StatsRecorderImpl implements StatsRecorder {

    private Map<String, Category> categories;

    private final int discardFirstRuns;

    private final int nbEntriesPerCategory;

    public StatsRecorderImpl(int nbEntriesPerCategory, int discardFirstRuns) {
        this(new String[0], nbEntriesPerCategory, discardFirstRuns);
    }

    public StatsRecorderImpl(String[] categoryNames, int nbEntriesPerCategory,
            int discardFirstRuns) {
        this.discardFirstRuns = discardFirstRuns;
        this.nbEntriesPerCategory = nbEntriesPerCategory;

        this.categories = new HashMap<String, Category>();

        for (int i = 0; i < categoryNames.length; i++) {
            this.categories.put(categoryNames[i], new CategoryImpl(
                    nbEntriesPerCategory, discardFirstRuns));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportValue(String categoryName, long value) {
        Category category;

        // creates category if it does not exist
        if (!this.categories.containsKey(categoryName)) {
            category =
                    new CategoryImpl(
                            this.nbEntriesPerCategory, this.discardFirstRuns);
            this.categories.put(categoryName, category);
        } else {
            category = this.categories.get(categoryName);
        }

        category.reportValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCategory(String name) {
        return this.categories.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> listCategoriesName() {
        return this.categories.keySet();
    }

}
