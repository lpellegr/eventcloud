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
package fr.inria.eventcloud.runners;

import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Custom suite runner used to run a test class with the different versions of
 * the publish subscribe algorithm.
 * 
 * @author lpellegr
 */
public class EachPublishSubscribeAlgorithm extends Suite {

    public EachPublishSubscribeAlgorithm(Class<?> klass)
            throws InitializationError {
        super(klass, extractAndCreateRunners(klass));
    }

    private static List<Runner> extractAndCreateRunners(Class<?> klass)
            throws InitializationError {
        Builder<Runner> runners = new ImmutableList.Builder<Runner>();

        for (PublishSubscribeAlgorithm algo : PublishSubscribeAlgorithm.values()) {
            runners.add(new PublishSubscribeAlgorithmRunner(klass, algo));
        }

        return runners.build();
    }

}
