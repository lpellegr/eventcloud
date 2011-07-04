package fr.inria.eventcloud.api;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

/**
 * 
 * @author lpellegr
 */
public class EventCloudsRegistryFactory {

    public static EventCloudsRegistry newEventCloudsRegistry() {
        try {
            return PAActiveObject.newActive(
                    EventCloudsRegistry.class, new Object[0]);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

}
