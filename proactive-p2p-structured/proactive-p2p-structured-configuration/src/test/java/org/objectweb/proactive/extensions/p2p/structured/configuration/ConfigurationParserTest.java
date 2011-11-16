package org.objectweb.proactive.extensions.p2p.structured.configuration;
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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.extensions.p2p.structured.configuration.ConfigurationParser;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyBoolean;
import org.objectweb.proactive.extensions.p2p.structured.configuration.PropertyString;

/**
 * Test cases associated to {@link ConfigurationParser}.
 * 
 * @author lpellegr
 */
public class ConfigurationParserTest {

    @Rule
    public static final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testLoadWithJavaProperty() throws IOException {
        File file = folder.newFile();

        PrintWriter pw = new PrintWriter(file);
        pw.write("property1");
        pw.write("=");
        pw.write("true");
        pw.println();
        pw.close();

        System.setProperty("test.configuration", file.toString());

        Assert.assertEquals(
                file.getAbsolutePath(),
                ConfigurationParserTest.Properties.configurationFileLoaded.getAbsolutePath());

        Assert.assertTrue(Properties.PROPERTY_1.getValue() != Properties.PROPERTY_1.getDefaultValue());
        Assert.assertTrue(Properties.PROPERTY_1.getValue());
        Assert.assertEquals("value", Properties.PROPERTY_2.getValue());
    }

    public static class Properties {

        public static final PropertyBoolean PROPERTY_1 = new PropertyBoolean(
                "property1", false);

        public static final PropertyString PROPERTY_2 = new PropertyString(
                "property2", "value");

        private static File configurationFileLoaded = null;

        static {
            try {
                configurationFileLoaded =
                        ConfigurationParser.load(
                                ConfigurationParserTest.Properties.class,
                                "test.configuration", folder.newFile()
                                        .toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}