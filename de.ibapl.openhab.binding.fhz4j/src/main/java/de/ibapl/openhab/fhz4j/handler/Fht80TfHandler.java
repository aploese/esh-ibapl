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
package de.ibapl.openhab.fhz4j.handler;

import de.ibapl.fhz4j.protocol.fht.Fht80TfMessage;
import static de.ibapl.openhab.fhz4j.FHZ4JBindingConstants.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;

/**
 * The {@link RadiatorFht80bHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class Fht80TfHandler extends BaseThingHandler {

    protected ThingStatusDetail fht80TfHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final static Logger LOGGER = Logger.getLogger("d.i.e.f.h.Fht80TfHandler");

    private int address;

    public Fht80TfHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        LOGGER.log(Level.SEVERE, String.format("Unknown FHT80 TF 0x%06x channel: {%s} command: %s", address, channelUID.getId(), command));
    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        try {
            address = Integer.parseUnsignedInt((String) configuration.get("address"), 16);
        } catch (NumberFormatException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse address");
            fht80TfHandlerStatus = ThingStatusDetail.HANDLER_INITIALIZING_ERROR;
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            fht80TfHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                fht80TfHandlerStatus = ThingStatusDetail.NONE;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
    }

    public int getAddress() {
        return address;
    }

    public void updateFromFht80TfMsg(Fht80TfMessage fht80TfMsg) {
        if (fht80TfMsg.lowBattery) {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.ON);
        } else {
            updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.OFF);
        }
        switch (fht80TfMsg.value) {
            case WINDOW_INTERNAL_OPEN -> {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_INTERNAL), OpenClosedType.OPEN);
                LOGGER.log(Level.SEVERE, String.format("update FHT80 TF %s channel: {%s}", fht80TfMsg, getThing().getUID()));
            }
            case WINDOW_INTERNAL_CLOSED -> {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_INTERNAL), OpenClosedType.CLOSED);
                LOGGER.log(Level.SEVERE, String.format("update FHT80 TF %s channel: {%s}", fht80TfMsg, getThing().getUID()));
            }
            case WINDOW_EXTERNAL_OPEN -> {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_EXTERNAL), OpenClosedType.OPEN);
            }
            case WINDOW_EXTERNAL_CLOSED -> {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_EXTERNAL), OpenClosedType.CLOSED);
            }
            case SYNC -> {
//                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_TF_SYNC), OnOffType.ON);
            }
            case FINISH -> {
//                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_TF_SYNC), OnOffType.OFF);
            }
            default -> {
            }
        }
    }

}
