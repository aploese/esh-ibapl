package de.ibapl.esh.fhz4j.handler;

/*-
 * #%L
 * FHZ4J Binding
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

import static de.ibapl.esh.fhz4j.FHZ4JBindingConstants.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

import de.ibapl.fhz4j.protocol.hms.Hms100TfMessage;
import de.ibapl.fhz4j.protocol.hms.HmsDeviceStatus;
import de.ibapl.fhz4j.protocol.hms.HmsMessage;

/**
 * The {@link Hms100TfHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class Hms100TfHandler extends BaseThingHandler {
    protected ThingStatusDetail owHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final Logger logger = Logger.getLogger("esh.binding.fhz4j");

    private short housecode;

    public Hms100TfHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no-op
    }

    @Override
    public void initialize() {
        logger.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        try {
            housecode = ((Number) configuration.get("housecode")).shortValue();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse housecode");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            owHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                owHandlerStatus = ThingStatusDetail.NONE;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
    }

    public short getHousecode() {
        return housecode;
    }

    public void updateFromMsg(HmsMessage hmsMessage) {
        switch (hmsMessage.hmsDeviceType) {
            case HMS_100_TF:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_MEASURED),
                        new DecimalType(((Hms100TfMessage) hmsMessage).temp));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY_MEASURED),
                        new DecimalType(((Hms100TfMessage) hmsMessage).humidy));
                if (((Hms100TfMessage) hmsMessage).deviceStatus.contains(HmsDeviceStatus.BATT_LOW)) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.ON);
                } else {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.OFF);
                }
                break;
            default:
                throw new RuntimeException("Cant handle HMS device: " + hmsMessage.hmsDeviceType);
        }
    }

}
