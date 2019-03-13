package de.ibapl.esh.onewire4j.handler;

/*-
 * #%L
 * OneWire4J Binding
 * %%
 * Copyright (C) 2017 - 2018 Arne Plöse
 * %%
 * Eclipse Smarthome Features (https://www.eclipse.org/smarthome/) and bundles see https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2017 - 2018, Arne Pl\u00f6se and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *  
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import static de.ibapl.esh.onewire4j.OneWire4JBindingConstants.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.ENotProperlyConvertedException;
import de.ibapl.onewire4j.container.OneWireDevice;
import de.ibapl.onewire4j.container.ReadScratchpadRequest;
import de.ibapl.onewire4j.container.TemperatureContainer;

/**
 * The {@link TemperatureHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class TemperatureHandler extends BaseThingHandler {

    public TemperatureContainer temperatureContainer;

    private final Logger logger = Logger.getLogger("esh.binding.onewire4j");

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
            case CHANNEL_TEMPERATURE:
                if (command instanceof RefreshType) {
                    // updateTemperature(++lastValue);
                }
                break;
            case CHANNEL_MIN_TEMPERATURE:
                if (command instanceof RefreshType) {
                    // updateMinTemperature(0);
                }
                break;
            case CHANNEL_MAX_TEMPERATURE:
                if (command instanceof RefreshType) {
                    // updateMaxTemperature(100);
                }
                break;
            default:

        }
    }

    @Override
    public void initialize() {
        logger.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        long deviceId;
        try {
            deviceId = Long.parseUnsignedLong((String) configuration.get("deviceId"), 16);
        } catch (Exception e) {
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
            return;
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
            ReadScratchpadRequest request = new ReadScratchpadRequest();
            temperatureContainer.readScratchpad(oneWireAdapter, request);
            final double temp = temperatureContainer.getTemperature(request);
            updateTemperature(temp);
            updateStatus(ThingStatus.ONLINE);
        } catch (ENotProperlyConvertedException e) {
            try {
                final double temp = temperatureContainer.convertAndReadTemperature(oneWireAdapter);
                updateTemperature(temp);
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            } catch (ENotProperlyConvertedException e1) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    }

}
