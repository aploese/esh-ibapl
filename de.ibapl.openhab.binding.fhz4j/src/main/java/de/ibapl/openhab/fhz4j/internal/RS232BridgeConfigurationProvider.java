/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2017-2023, Arne Plöse and individual contributors as indicated
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
package de.ibapl.openhab.fhz4j.internal;

import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.api.Speed;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionProvider;
import org.openhab.core.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ConfigDescriptionProvider.class, immediate = true, configurationPid = "config.rs232-bridge-cul")
public class RS232BridgeConfigurationProvider implements ConfigDescriptionProvider {

    @Reference
    private SerialPortSocketFactory serialPortSocketFactory; // = new de.ibapl.spsw.jniprovider.SerialPortSocketFactoryImpl();

    private final Logger LOGGER = Logger.getLogger("d.i.e.f.RS232BridgeConfigurationProvider");

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
        ConfigDescriptionBuilder configBuilder = ConfigDescriptionBuilder.create(uri);

        if (RS_232_URI.equals(uri)) {

            ConfigDescriptionParameterBuilder paramBuilder = ConfigDescriptionParameterBuilder.create("housecode", Type.INTEGER).
                    withDefault("1234").
                    withRequired(true).
                    withLabel("housecode").
                    withDescription("The Housecode of this devcice");
            configBuilder.withParameter(paramBuilder.build());

            // TODO Filter used ports or not ???
            if (serialPortSocketFactory == null) {
                LOGGER.severe("serialPortSocketFactory == null");
                //TODO
            } else {

                List<ParameterOption> optionList = new LinkedList<>();
                for (String name : serialPortSocketFactory.getPortNames(false)) {
                    optionList.add(new ParameterOption(name, name));
                }
                paramBuilder = ConfigDescriptionParameterBuilder.create("port", Type.TEXT).
                        withOptions(optionList).
                        withRequired(true).
                        withLabel("serial port").
                        withDescription("The serial port to use");
                configBuilder.withParameter(paramBuilder.build());

                optionList = new LinkedList<>();
                for (Speed s : Speed.values()) {
                    optionList.add(new ParameterOption(String.valueOf(s.value), s.toString()));
                }
                paramBuilder = ConfigDescriptionParameterBuilder.create("speed", Type.INTEGER).
                        withOptions(optionList).
                        withRequired(true).
                        withDefault(String.valueOf(Speed._9600_BPS.value)).
                        withLabel("speed ").
                        withDescription("The speed (baudrate) to use. Default is 9600 bps.");
                configBuilder.withParameter(paramBuilder.build());

                paramBuilder = ConfigDescriptionParameterBuilder.create("protocolEvoHome", Type.BOOLEAN).
                        withDefault(String.valueOf(false)).
                        withRequired(true).
                        withLabel("protocol Evo Home").
                        withDescription("The Protocol Evo Home");
                configBuilder.withParameter(paramBuilder.build());

                paramBuilder = ConfigDescriptionParameterBuilder.create("protocolFHT", Type.BOOLEAN).withDefault(String.valueOf(false)).
                        withRequired(true).withLabel("protocol FHT").
                        withDescription("The Protocol for Heating sytem FHT 80");
                configBuilder.withParameter(paramBuilder.build());

                paramBuilder = ConfigDescriptionParameterBuilder.create("logSerialPort", Type.BOOLEAN).
                        withDefault(String.valueOf(false)).
                        withRequired(true).
                        withLabel("Log Serial Port Data").
                        withDescription("Log IO and settings on the serial port");
                configBuilder.withParameter(paramBuilder.build());
            }
        }
        return configBuilder.build();
    }
}
