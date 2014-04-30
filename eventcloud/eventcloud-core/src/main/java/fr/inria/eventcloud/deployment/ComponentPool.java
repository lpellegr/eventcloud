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
package fr.inria.eventcloud.deployment;

import java.io.Serializable;
import java.util.Iterator;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.extensions.p2p.structured.CommonAttributeController;

import com.google.common.base.Supplier;

import fr.inria.eventcloud.utils.Pool;

/**
 * Simple pool implementation for ProActive components.
 * 
 * @author lpellegr
 */
public class ComponentPool<T> implements Iterable<T>, Serializable {

    private static final long serialVersionUID = 160L;

    // use encapsulation since we do not want to expose the method borrow
    // without parameters
    protected final Pool<T> pool;

    public ComponentPool(Supplier<? extends T> supplier) {
        this.pool = new Pool<T>(supplier);
    }

    public void allocate(int nb) {
        this.pool.allocate(nb);
    }

    public void clear() {
        this.pool.clear();
    }

    public boolean isEmpty() {
        return this.pool.isEmpty();
    }

    public void release(T resource) {
        this.reset(resource);
        this.pool.release(resource);
    }

    protected void reset(T resource) {
        Component component = ((Interface) resource).getFcItfOwner();

        try {
            GCM.getLifeCycleController(component).stopFc();
            ((CommonAttributeController) GCM.getAttributeController(component)).resetAttributes();
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return this.pool.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.pool.toString();
    }

}
