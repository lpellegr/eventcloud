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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import fr.inria.eventcloud.EventCloud;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;

/**
 * This launcher is used to create a new {@link EventCloud} and to register it
 * to the specified {@link EventCloudsRegistry} URL. If the deployment succeed,
 * a three columns value is returned like for example {@code 1 1 -932949592}.
 * The first number indicates the number of trackers deployed, the second the
 * number of peers deployed and the last the identifier associated to the event
 * cloud.
 * 
 * @author lpellegr
 */
public final class EventCloudLauncher {

    @Parameter(names = {"-registryUrl"}, description = "The EventCloudsRegistry URL", required = true)
    private String registryUrl;

    @Parameter(names = {"-nb-peers"}, description = "Number of Peers")
    private int nbPeers = 1;

    @Parameter(names = {"-nb-trackers"}, description = "Number of Trackers")
    private int nbTrackers = 1;

    private EventCloudLauncher() {

    }

    public static void main(String[] args) {
        EventCloudLauncher launcher = new EventCloudLauncher();

        try {
            JCommander jc = new JCommander(launcher);
            jc.parse(args);
        } catch (ParameterException e) {
            e.printStackTrace();
            System.exit(1);
        }

        launcher.run();
    }

    private void run() {
        EventCloud eventCloud =
                EventCloud.create(
                        this.registryUrl, null,
                        new Collection<UnalterableElaProperty>(),
                        this.nbTrackers, this.nbPeers);

        System.out.println(this.nbTrackers + " " + this.nbPeers + " "
                + eventCloud.getId());
    }

}
