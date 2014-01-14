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

import java.util.Collection;

/**
 * Records statistical information about different runs and scenarios. Results
 * from different scenarios are classified according to categories.
 * 
 * @author lpellegr
 */
public interface StatsRecorder {

    void reportValue(String categoryName, long value);

    Category getCategory(String name);

    Collection<String> listCategoriesName();

}
