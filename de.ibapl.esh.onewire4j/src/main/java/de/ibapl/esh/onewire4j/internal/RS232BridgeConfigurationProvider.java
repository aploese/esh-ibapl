/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2017-2019, Arne Plöse and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.ibapl.esh.onewire4j.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import de.ibapl.spsw.api.SerialPortSocketFactory;
import java.util.logging.Logger;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ConfigDescriptionProvider.class, immediate = true, configurationPid = "config.rs232-bridge")
public class RS232BridgeConfigurationProvider implements ConfigDescriptionProvider {

    private static final Logger LOGGER = Logger.getLogger("d.i.e.o.RS232BridgeConfigurationProvider");

    @Reference
    private SerialPortSocketFactory serialPortSocketFactory; // = new de.ibapl.spsw.jniprovider.SerialPortSocketFactoryImpl();

    private static final URI RS_232_URI;

    static {
        try {
            RS_232_URI = new URI("bridge-type:rs-232");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        return Collections.singleton(getConfigDescription(RS_232_URI, locale));
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        final List<ConfigDescriptionParameter> parameters = new LinkedList<>();
        if (RS_232_URI.equals(uri)) {
            ConfigDescriptionParameter refreshrate = new ConfigDescriptionParameter("refreshrate", Type.INTEGER) {

                @Override
                public String getLabel() {
                    return "Refresh Rate";
                }

                @Override
                public String getDescription() {
                    return "The refreshrate in s.";
                }

                @Override
                public String getDefault() {
                    return "60";
                }

                @Override
                public boolean isRequired() {
                    return true;
                }

            };
            parameters.add(refreshrate);
            ConfigDescriptionParameter port = new ConfigDescriptionParameter("port", Type.TEXT) {

                @Override
                public List<ParameterOption> getOptions() {
                    List<ParameterOption> result = new LinkedList<>();
                    // TODO Filter used ports or not ???
                    if (serialPortSocketFactory == null) {
                        LOGGER.severe("serialPortSocketFactory == null");
                        //TODO
                    } else {

                        for (String name : serialPortSocketFactory.getPortNames(false)) {
                            result.add(new ParameterOption(name, name));
                        }
                    }
                    return result;
                }

                @Override
                public boolean isRequired() {
                    return true;
                }

                @Override
                public String getLabel() {
                    return "serial port";
                }

                @Override
                public String getDescription() {
                    return "The serial port to use";
                }
            };
            parameters.add(port);

            ConfigDescriptionParameter logSerialPort = new ConfigDescriptionParameter("logSerialPort", Type.BOOLEAN) {

                @Override
                public String getDefault() {
                    return String.valueOf(false);
                }

                @Override
                public boolean isRequired() {
                    return true;
                }

                @Override
                public String getLabel() {
                    return "Log Serial Port Data";
                }

                @Override
                public String getDescription() {
                    return "Log IO and settings on the serial port";
                }

            };
            parameters.add(logSerialPort);

        }
        return new ConfigDescription(uri, parameters);
    }

}
