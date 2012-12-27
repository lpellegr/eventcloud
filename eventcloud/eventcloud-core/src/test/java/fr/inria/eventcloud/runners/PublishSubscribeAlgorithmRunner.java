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
package fr.inria.eventcloud.runners;

import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * Custom Junit {@link Runner} to run a test class with a specific version of
 * the publish subscribe algorithm.
 * 
 * @author lpellegr
 */
public class PublishSubscribeAlgorithmRunner extends BlockJUnit4ClassRunner {

    private PublishSubscribeAlgorithm publishSubscribeAlgorithm;

    public PublishSubscribeAlgorithmRunner(Class<?> klass,
            PublishSubscribeAlgorithm publishSubscribeAlgorithm)
            throws InitializationError {
        super(klass);

        this.publishSubscribeAlgorithm = publishSubscribeAlgorithm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                EventCloudProperties.PUBLISH_SUBSCRIBE_ALGORITHM.setValue(PublishSubscribeAlgorithmRunner.this.publishSubscribeAlgorithm.name());

                if (PublishSubscribeAlgorithmRunner.this.publishSubscribeAlgorithm == PublishSubscribeAlgorithm.SBCE2
                        || PublishSubscribeAlgorithmRunner.this.publishSubscribeAlgorithm == PublishSubscribeAlgorithm.SBCE3) {
                    EventCloudProperties.PREVENT_CHUNK_DUPLICATES.setValue(true);
                }

                PublishSubscribeAlgorithmRunner.super.methodBlock(method)
                        .evaluate();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getName() {
        // returns the name of the test class
        return this.publishSubscribeAlgorithm.toString();
    }

}
