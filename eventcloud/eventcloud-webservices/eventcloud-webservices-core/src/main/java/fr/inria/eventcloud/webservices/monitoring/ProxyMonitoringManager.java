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
package fr.inria.eventcloud.webservices.monitoring;

import fr.inria.eventcloud.monitoring.ProxyMonitoringActions;

/**
 * Defines methods for managing monitoring on proxies (e.g. enabling, disabling,
 * sending reports or retrieving properties).
 * 
 * @author lpellegr
 */
public interface ProxyMonitoringManager extends ProxyMonitoringActions,
        ProxyMonitoringService {

}
