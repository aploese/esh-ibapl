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
package de.ibapl.openhab.fhz4j.handler;

import de.ibapl.fhz4j.protocol.fht.FhtTfMessage;
import static de.ibapl.openhab.fhz4j.FHZ4JBindingConstants.*;
import de.ibapl.openhab.fhz4j.handler.RadiatorFht80bHandler;
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
public class FhtTfHandler extends BaseThingHandler {

    protected ThingStatusDetail fhtTfHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final static Logger LOGGER = Logger.getLogger("d.i.e.f.h.FhtTfHandler");

    private int address;

    public FhtTfHandler(Thing thing) {
        super(thing);
    }

    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        LOGGER.log(Level.SEVERE, "Unknown FHT TF (" + address + ") channel: {0}", channelUID.getId());
    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        try {
            address = ((Number) configuration.get("address")).intValue();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse address");
            fhtTfHandlerStatus = ThingStatusDetail.HANDLER_INITIALIZING_ERROR;
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            fhtTfHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                fhtTfHandlerStatus = ThingStatusDetail.NONE;
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

    public void updateFromFhtTfMsg(FhtTfMessage fhtTfMsg) {
                if (fhtTfMsg.lowBattery) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.ON);
                } else {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.OFF);
                }
        switch (fhtTfMsg.value) {
            case WINDOW_INTERNAL_OPEN:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_INTERNAL), OpenClosedType.OPEN);
                break;
            case WINDOW_INTERNAL_CLOSED:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_INTERNAL), OpenClosedType.CLOSED);
                break;
            case WINDOW_EXTERNAL_OPEN:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_EXTERNAL), OpenClosedType.OPEN);
                break;
            case WINDOW_EXTERNAL_CLOSED:
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_EXTERNAL), OpenClosedType.CLOSED);
                break;
            case SYNC:
//                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_TF_SYNC), OnOffType.ON);
                break;
            case FINISH:
//                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_TF_SYNC), OnOffType.OFF);
                break;

            default:
                break;
        }
    }

}
