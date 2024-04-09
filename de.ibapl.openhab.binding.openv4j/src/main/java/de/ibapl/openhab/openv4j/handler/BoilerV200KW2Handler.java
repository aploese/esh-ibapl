/*
 * ESH-IBAPL  - OpenHAB bindings for various IB APL drivers, https://github.com/aploese/esh-ibapl/
 * Copyright (C) 2024, Arne Plöse and individual contributors as indicated
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
package de.ibapl.openhab.openv4j.handler;

import static de.ibapl.openhab.openv4j.OpenV4JBindingConstants.*;
import java.io.IOException;
import java.util.Map;
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
 * The {@link BoilerV200KW2Handler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class BoilerV200KW2Handler extends BaseThingHandler {

    protected ThingStatusDetail BoilerV200KW2HandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final static Logger LOGGER = Logger.getLogger("d.i.o.o.h.BoilerV200KW2Handler");

    public BoilerV200KW2Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_BOILER_WATER_TEMP -> {
                if (command instanceof DecimalType decimalType) {
                    try {
                        desiredTemp = decimalType.floatValue();
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode,
                                FhtProperty.DESIRED_TEMP, desiredTemp);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "handleCommand CHANNEL_DESIRED_TEMPERATURE", e);
                    }
                } else if (command instanceof RefreshType) {
                    //TODO
                    desiredTemp = 17.0f;
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
            }
            default ->
                LOGGER.log(Level.SEVERE, "Unknown Fht80 (" + housecode + ") channel: {0}", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            BoilerV200KW2HandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                BoilerV200KW2HandlerStatus = ThingStatusDetail.NONE;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
        //init here
    }

    @Override
    public void dispose() {
    }

}
