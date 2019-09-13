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
package de.ibapl.esh.fhz4j.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import de.ibapl.spsw.api.SerialPortSocketFactory;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ConfigDescriptionProvider.class, immediate = true, configurationPid = "config.rs232-bridge-cul")
public class RS232BridgeConfigurationProvider implements ConfigDescriptionProvider {

    @Reference
    private SerialPortSocketFactory serialPortSocketFactory; // = new de.ibapl.spsw.jniprovider.SerialPortSocketFactoryImpl();

    private final Logger logger = Logger.getLogger("esh.binding.fhz4j");

    private static final URI RS_232_URI;
    static {
        try {
            RS_232_URI = new URI("bridge-type:rs-232-cul");
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
                public String getDefault() {
                    return "1";
                }

                @Override
                public boolean isRequired() {
                    return true;
                }

                @Override
                public String getLabel() {
                    return "refreshrate";
                }

                @Override
                public String getDescription() {
                    return "The refreshrate in days";
                }
            };
            parameters.add(refreshrate);

            ConfigDescriptionParameter housecode = new ConfigDescriptionParameter("housecode", Type.INTEGER) {

                @Override
                public String getDefault() {
                    return "1234";
                }

                @Override
                public boolean isRequired() {
                    return true;
                }

                @Override
                public String getLabel() {
                    return "housecode";
                }

                @Override
                public String getDescription() {
                    return "The Housecode of this devcice";
                }

            };
            parameters.add(housecode);

            ConfigDescriptionParameter port = new ConfigDescriptionParameter("port", Type.TEXT) {

                @Override
                public List<ParameterOption> getOptions() {
                    List<ParameterOption> result = new LinkedList<>();
                    // TODO Filter used ports or not ???
                	if (serialPortSocketFactory == null) {
                		logger.severe("serialPortSocketFactory == null");
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
            ConfigDescriptionParameter protocolEvoHome = new ConfigDescriptionParameter("protocolEvoHome", Type.BOOLEAN) {

                @Override
                public boolean isRequired() {
                    return true;
                }

                @Override
                public String getLabel() {
                    return "protocol Evo Home";
                }

                @Override
                public String getDescription() {
                    return "The Protocol Evo Home";
                }

            };
            parameters.add(protocolEvoHome);

            ConfigDescriptionParameter protocolFHT = new ConfigDescriptionParameter("protocolFHT", Type.BOOLEAN) {

                @Override
                public boolean isRequired() {
                    return true;
                }

                @Override
                public String getLabel() {
                    return "protocol FHT";
                }

                @Override
                public String getDescription() {
                    return "The Protocol for Heating sytem FHT 80";
                }

            };
            parameters.add(protocolFHT);

        }
        return new ConfigDescription(uri, parameters);
    }

}
