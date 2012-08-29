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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract factory used to check some Java properties before creating GCM
 * components.
 * 
 * @author bsauvan
 */
public class AbstractFactory {

    private static final Logger log =
            LoggerFactory.getLogger(AbstractFactory.class);

    static {
        if (CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getValue() == null) {
            log.warn("Java property \""
                    + CentralPAPropertyRepository.JAVA_SECURITY_POLICY.getName()
                    + "\" is not set");
        }

        if (!CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue()
                .equals("pnp")) {
            log.warn("Java property \""
                    + CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName()
                    + "\" is not set to pnp");
        }

        if (PNPConfig.PA_PNP_PORT.getValue() == 0) {
            log.warn("Java property \"" + PNPConfig.PA_PNP_PORT.getName()
                    + "\" is not set");
        } else if (log.isDebugEnabled()) {
            log.debug("Java property \"" + PNPConfig.PA_PNP_PORT.getName()
                    + "\" set to " + PNPConfig.PA_PNP_PORT.getValue());
        }

        if (CentralPAPropertyRepository.PA_HOSTNAME.getValue() == null) {
            log.warn("Java property \""
                    + CentralPAPropertyRepository.PA_HOSTNAME.getName()
                    + "\" is not set");
        } else if (log.isDebugEnabled()) {
            log.debug("Java property \""
                    + CentralPAPropertyRepository.PA_HOSTNAME.getName()
                    + "\" set to "
                    + CentralPAPropertyRepository.PA_HOSTNAME.getValue());
        }
    }

}