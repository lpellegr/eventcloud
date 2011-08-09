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
package fr.inria.eventcloud.deployment.cli.launchers;

import fr.inria.eventcloud.deployment.cli.commands.SubscribeCommand;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * This launcher is used to have the possibility to execute subscribe operations
 * from the interactive command-line by delegating the commands to a
 * {@link SubscribeProxy}.
 * 
 * @author lpellegr
 */
public class SubscribeProxyLauncher extends ProxyLauncher<SubscribeProxy> {

    @SuppressWarnings("unchecked")
    public SubscribeProxyLauncher(String[] args) {
        super(args, new SubscribeCommand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeProxy createProxy(ProxyFactory factory) {
        return factory.createSubscribeProxy();
    }

    public static void main(String[] args) {
        new SubscribeProxyLauncher(args).run();
    }

}
