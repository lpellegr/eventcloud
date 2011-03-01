package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.ProActiveRandom;

/**
 * {@link TrackersProperties} is used to store and to get trackers bindingNames
 * from a specified properties file.
 * 
 * @author lpellegr
 */
public class TrackersProperties {

    private static String DEFAULT_PROPERTIES_COMMENT = "Trackers list";

    private File propertiesFilePath;

    private Properties properties;

    public TrackersProperties(String propertiesFilePath) {
        this.propertiesFilePath = new File(propertiesFilePath);
        if (!this.propertiesFilePath.exists()) {
            try {
                this.propertiesFilePath.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.properties = new Properties();
        try {
            this.properties.load(new FileInputStream(this.propertiesFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void store(String associatedNetworkName, String bindingName, String type) {
        List<String> bindingNames;
        if ((bindingNames = this.getBindingNames(associatedNetworkName)) == null) {
            bindingNames = new ArrayList<String>();
            this.properties.put(associatedNetworkName + ".type", type);
        }

        if (!bindingNames.contains(bindingName)) {
            bindingNames.add(bindingName);
        }

        this.properties.put(associatedNetworkName, TrackersProperties.asString(bindingNames));
        try {
            this.properties.store(new FileOutputStream(this.propertiesFilePath),
                    TrackersProperties.DEFAULT_PROPERTIES_COMMENT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void store(String associatedNetworkName, List<String> bindingNames) {
        this.properties.put(associatedNetworkName, TrackersProperties.asString(bindingNames));

        try {
            this.properties.store(new FileOutputStream(this.propertiesFilePath),
                    TrackersProperties.DEFAULT_PROPERTIES_COMMENT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String asString(List<String> bindingNames) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < bindingNames.size() - 1; i++) {
            buf.append(bindingNames.get(i));
            buf.append(";");
        }
        buf.append(bindingNames.get(bindingNames.size() - 1));
        return buf.toString();
    }

    public List<String> getBindingNames(String associatedNetworkName) {
        String value = (String) this.properties.get(associatedNetworkName);

        if (value == null) {
            return null;
        }
        return new ArrayList<String>(Arrays.asList(((String) value).split(";")));
    }

    public File getPropertiesFilePath() {
        return this.propertiesFilePath;
    }

    public String getRandomBindingName(String associatedNetworkName) {
        List<String> bindingNames = this.getBindingNames(associatedNetworkName);
        return bindingNames.get(ProActiveRandom.nextInt(bindingNames.size()));
    }

    public void remove(String associatedNetworkName, String bindingName) {
        List<String> bindingNames;
        if ((bindingNames = this.getBindingNames(associatedNetworkName)) != null) {
            if (bindingNames.contains(bindingName)) {

                bindingNames.remove(bindingName);
                if (bindingNames.size() == 0) {
                    this.properties.remove(associatedNetworkName);
                    this.properties.remove(associatedNetworkName + ".type");
                } else {
                    this.store(associatedNetworkName, bindingNames);
                }
            } else {
                throw new IllegalStateException(
                        "The specified network name does not contain the specified bindingName");
            }
        } else {
            throw new IllegalStateException("The specified network name does not exists");
        }
    }

    public Tracker getRandomActiveAndValidTracker(String associatedNetworkName) {
        Tracker tracker = null;
        String bindingName = this.getRandomBindingName(associatedNetworkName);

        if (bindingName == null) {
            throw new NullPointerException("No valid trackers available in "
                    + this.propertiesFilePath);
        } else {
            try {
                return PAActiveObject.lookupActive(Tracker.class, bindingName);
            } catch (Exception e) {
                this.remove(associatedNetworkName, bindingName);
                this.getRandomActiveAndValidTracker(associatedNetworkName);
            }

            return tracker;
        }
    }

}
