/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud.api.listeners;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * This kind of notification listener will only receive a binding (i.e. the
 * variables and their associated values) that match the subscription.
 * 
 * @author lpellegr
 */
public interface BindingNotificationListener extends
        NotificationListener<Binding> {

}
