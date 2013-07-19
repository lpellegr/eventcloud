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
package fr.inria.eventcloud.overlay.can;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.TestingDeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.JunitByClassEventCloudDeployer;
import fr.inria.eventcloud.messages.request.StatefulQuadruplePatternRequest;
import fr.inria.eventcloud.messages.response.StatefulQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Dynamic load balancing test.
 * 
 * @author lpellegr
 */
public class DynamicLoadBalancingTest extends JunitByClassEventCloudDeployer {

    private static final Logger log =
            LoggerFactory.getLogger(SemanticPeerTest.class);

    public DynamicLoadBalancingTest() {
        super(
                (EventCloudDeploymentDescriptor) new EventCloudDeploymentDescriptor().setDeploymentConfiguration(createDeploymentConfiguration()),
                1, 2);
    }

    public static DeploymentConfiguration createDeploymentConfiguration() {
        return new TestingDeploymentConfiguration() {

            private static final long serialVersionUID = 150L;

            @Override
            public void configure() {
                super.configure();

                EventCloudProperties.DYNAMIC_LOAD_BALANCING.setValue(true);
                // EventCloudProperties.LOAD_BALANCING_CRITERION_NB_QUADS_STORED_EMERGENCY_THRESHOLD.setValue(10d);
            }
        };
    }

    // @Test
    public void test() {
        while (true) {
            try {
                Thread.sleep(RandomUtils.nextIntClosed(50, 100));

                super.getPutGetProxy().add(QuadrupleGenerator.random());

                // GetLoadInformationResponse loadInformation =
                // (GetLoadInformationResponse)
                // PAFuture.getFutureValue(super.getRandomSemanticPeer()
                // .send(new GetLoadInformationRequest()));
                //
                // Map<OverlayId, LoadInformation> map =
                // loadInformation.getResult();
                //
                // StringBuilder buf = new StringBuilder();
                //
                // Iterator<Entry<OverlayId, LoadInformation>> iter =
                // map.entrySet().iterator();
                //
                // while (iter.hasNext()) {
                // Entry<OverlayId, LoadInformation> entry = iter.next();
                //
                // buf.append(entry.getKey());
                // buf.append(" => ");
                // buf.append(entry.getValue());
                //
                // if (iter.hasNext()) {
                // buf.append(',');
                // }
                //
                // buf.append("\n");
                // }
                //
                // buf.append("Number of peers is ");
                // buf.append(map.size());
                //
                // log.info(buf.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class GetLoadInformationRequest extends
            StatefulQuadruplePatternRequest<Map<OverlayId, LoadInformation>> {

        private static final long serialVersionUID = 150L;

        public GetLoadInformationRequest() {
            super(
                    QuadruplePattern.ANY,
                    new ResponseProvider<GetLoadInformationResponse, Coordinate<SemanticElement>>() {
                        private static final long serialVersionUID = 150L;

                        @Override
                        public GetLoadInformationResponse get() {
                            return new GetLoadInformationResponse();
                        }
                    });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<OverlayId, LoadInformation> onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                                              AnycastRequest<SemanticElement> request,
                                                                              QuadruplePattern quadruplePattern) {
            SemanticCanOverlay customOverlay = (SemanticCanOverlay) overlay;

            Map<OverlayId, LoadInformation> result =
                    new HashMap<OverlayId, LoadInformation>();
            result.put(overlay.getId(), new LoadInformation(
                    customOverlay.getLoadBalancingManager().getLocalLoad(),
                    customOverlay.getLoadBalancingManager()
                            .getAverageOverlayLoad()));

            return result;
        }

    }

    private static class GetLoadInformationResponse extends
            StatefulQuadruplePatternResponse<Map<OverlayId, LoadInformation>> {

        private static final long serialVersionUID = 150L;

        public GetLoadInformationResponse() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized Map<OverlayId, LoadInformation> merge(List<SerializedValue<Map<OverlayId, LoadInformation>>> intermediateResults) {
            Map<OverlayId, LoadInformation> result = null;

            for (SerializedValue<Map<OverlayId, LoadInformation>> subResult : intermediateResults) {
                Map<OverlayId, LoadInformation> i = subResult.getValue();

                if (result == null) {
                    result = subResult.getValue();
                } else {
                    result.putAll(i);
                }
            }

            return result;
        }

    }

    private static class LoadInformation implements Serializable {

        private static final long serialVersionUID = 150L;

        public double localLoad;

        public double systemLoad;

        public LoadInformation(double localLoad, double systemLoad) {
            this.localLoad = localLoad;
            this.systemLoad = systemLoad;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "localLoad=" + this.localLoad + ", systemLoad="
                    + this.systemLoad;
        }

    }

}
