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
package org.objectweb.proactive.extensions.p2p.structured.providers;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.deployment.InjectionConstraints;

/**
 * A provider that knows how to create an {@link InjectionConstraints}.
 * 
 * @author lpellegr
 */
public abstract class InjectionConstraintsProvider implements Serializable {

    private static final long serialVersionUID = 160L;

    public abstract InjectionConstraints get(int nbPeers);

    /**
     * Returns an {@link InjectionConstraintsProvider} that knows how to create
     * a fractal {@link InjectionConstraints} (c.f.
     * {@link InjectionConstraints#newFractalInjectionConstraints(int)}).
     * 
     * @return an {@link InjectionConstraintsProvider} that knows how to create
     *         a fractal {@link InjectionConstraints} (c.f.
     *         {@link InjectionConstraints#newFractalInjectionConstraints(int)}
     *         ).
     */
    public static InjectionConstraintsProvider newFractalInjectionConstraintsProvider() {
        return new InjectionConstraintsProvider() {

            private static final long serialVersionUID = 160L;

            @Override
            public InjectionConstraints get(int nbPeers) {
                return InjectionConstraints.newFractalInjectionConstraints(nbPeers);
            }
        };
    }

    /**
     * Returns an {@link InjectionConstraintsProvider} that knows how to create
     * an uniform {@link InjectionConstraints} (c.f.
     * {@link InjectionConstraints#newUniformInjectionConstraints(int)}).
     * 
     * @return an {@link InjectionConstraintsProvider} that knows how to create
     *         an uniform {@link InjectionConstraints} (c.f.
     *         {@link InjectionConstraints#newUniformInjectionConstraints(int)}
     *         ).
     */
    public static InjectionConstraintsProvider newUniformInjectionConstraintsProvider() {
        return new InjectionConstraintsProvider() {

            private static final long serialVersionUID = 160L;

            @Override
            public InjectionConstraints get(int nbPeers) {
                return InjectionConstraints.newUniformInjectionConstraints(nbPeers);
            }
        };
    }

}
