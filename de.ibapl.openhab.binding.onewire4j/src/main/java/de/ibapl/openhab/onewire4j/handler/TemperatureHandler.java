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
package de.ibapl.openhab.onewire4j.handler;

import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.OneWireDevice;
import de.ibapl.onewire4j.container.TemperatureContainer;
import static de.ibapl.openhab.onewire4j.OneWire4JBindingConstants.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link TemperatureHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class TemperatureHandler extends BaseThingHandler {

    public TemperatureContainer temperatureContainer;

    private static final Logger LOGGER = Logger.getLogger("d.i.o.ow.h.TemperatureHandler");

    public TemperatureHandler(Thing thing) {
        super(thing);
    }

    public void updateTemperature(double temperature) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(temperature));
    }

    public void updateMinTemperature(double temperature) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MIN_TEMPERATURE), new DecimalType(temperature));
    }

    public void updateMaxTemperature(double temperature) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_TEMPERATURE), new DecimalType(temperature));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_TEMPERATURE -> {
                if (command instanceof RefreshType) {
                    // updateTemperature(++lastValue);
                }
            }
            case CHANNEL_MIN_TEMPERATURE -> {
                if (command instanceof RefreshType) {
                    // updateMinTemperature(0);
                }
            }
            case CHANNEL_MAX_TEMPERATURE -> {
                if (command instanceof RefreshType) {
                    // updateMaxTemperature(100);
                }
            }
            default -> {
            }

        }
    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        long deviceId;
        try {
            deviceId = Long.parseUnsignedLong((String) configuration.get("deviceId"), 16);
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse DeviceId");
            return;
        }
        temperatureContainer = (TemperatureContainer) OneWireDevice.fromAdress(deviceId);
        if (temperatureContainer == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "No container Found");
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
    }

    public void readDevice(OneWireAdapter oneWireAdapter) throws IOException {
        try {
            TemperatureContainer.ReadScratchpadRequest request = new TemperatureContainer.ReadScratchpadRequest();
            temperatureContainer.readScratchpad(oneWireAdapter, request);
            final double temp = temperatureContainer.getTemperature(request);
            updateTemperature(temp);
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            try {
                TemperatureContainer.ReadScratchpadRequest request = new TemperatureContainer.ReadScratchpadRequest();
                temperatureContainer.readScratchpad(oneWireAdapter, request);
                final double temp = temperatureContainer.getTemperature(request);
                updateTemperature(temp);
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            } catch (IOException e1) {
                LOGGER.logp(Level.SEVERE, this.getClass().getName(), "run()", "Exception occurred during execution", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                throw e1;
            }
        }
    }

}
