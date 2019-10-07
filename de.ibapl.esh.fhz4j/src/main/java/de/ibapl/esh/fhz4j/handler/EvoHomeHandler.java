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
package de.ibapl.esh.fhz4j.handler;

import static de.ibapl.esh.fhz4j.FHZ4JBindingConstants.*;
import de.ibapl.fhz4j.protocol.evohome.DeviceId;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeDeviceMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHome_0x18_0x2309_0xXX_ROOM_DESIRED_TEMP_Message;
import de.ibapl.fhz4j.protocol.evohome.EvoHome_0x18_0x30C9_0xXX_ROOM_MEASURED_TEMP_Message;
import de.ibapl.fhz4j.protocol.evohome.EvoHome_0x18_0x3150_0x02_HEAT_DEMAND_Message;
import de.ibapl.fhz4j.protocol.evohome.EvoHome_0x2C_0x2309_0xXX_ROOM_DESIRED_TEMP_Message;
import de.ibapl.fhz4j.protocol.evohome.ZoneTemperature;
import java.math.BigDecimal;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

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

/**
 * The {@link RadiatorFht80bHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class EvoHomeHandler extends BaseThingHandler {

    protected ThingStatusDetail evoHomeRadiatorHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final static Logger LOGGER = Logger.getLogger("d.i.e.f.h.EvoHomeHandler");

    private int deviceId;

    public EvoHomeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            switch (channelUID.getId()) {
                case CHANNEL_DESIRED_TEMPERATURE_00:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 0, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_01:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 1, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_02:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 2, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_03:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 3, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_04:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 4, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_05:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 5, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_06:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 6, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_07:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 7, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_08:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 8, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_09:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 9, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_10:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 10, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                case CHANNEL_DESIRED_TEMPERATURE_11:
                    if (command instanceof DecimalType) {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(
                                new DeviceId(deviceId),
                                new ZoneTemperature((byte) 11, ((DecimalType) command).toBigDecimal()));
                    } else if (command instanceof RefreshType) {
                    }
                    break;
                default:
                    LOGGER.log(Level.SEVERE, "Handle command {0} for unknown EvoHome settable channelUID: {1} ", new Object[]{command, channelUID});
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "IO EX: command {0} channelUID {1}", new Object[]{command, channelUID});
            //TODO set state ???
        }
    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        try {
            deviceId = ((Number) configuration.get("deviceId")).intValue();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse deviceId");
            evoHomeRadiatorHandlerStatus = ThingStatusDetail.HANDLER_INITIALIZING_ERROR;
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            evoHomeRadiatorHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                evoHomeRadiatorHandlerStatus = ThingStatusDetail.NONE;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
    }

    @Override
    public void dispose() {
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void updateFromMsg(EvoHomeDeviceMessage msg) {
        switch (msg.property) {
            case _18_3150_HEAT_DEMAND: {
                final float valvePos = ((EvoHome_0x18_0x3150_0x02_HEAT_DEMAND_Message) msg).calcValvePosition();
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION),
                        new DecimalType(valvePos));
                final short heatDeamnd = ((EvoHome_0x18_0x3150_0x02_HEAT_DEMAND_Message) msg).heatDemand;
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_RADIATOR_HEAT_DEMAND),
                        new DecimalType(heatDeamnd));
                LOGGER.severe("EvoHome handled MSG:  " + msg);
            }
            break;
            case _18_30C9_ROOM_MEASURED_TEMP: {
                //From SingleZoneThermostat and RadiatorController and MultZoneController
                final EvoHome_0x18_0x30C9_0xXX_ROOM_MEASURED_TEMP_Message m = (EvoHome_0x18_0x30C9_0xXX_ROOM_MEASURED_TEMP_Message) msg;
                switch (m.deviceId1.type) {
                    case RADIATOR_CONTROLLER:
                    case SINGLE_ZONE_THERMOSTAT:
                        //TODO ZoneID ???
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_MEASURED),
                                new DecimalType(m.zoneTemperatures.get(0).temperature));
                        LOGGER.severe("EvoHome handled MSG:  " + msg);
                        break;
                    case MULTI_ZONE_CONTROLLER:
                        for (ZoneTemperature zoneTemperature : m.zoneTemperatures) {
                            updateState(new ChannelUID(getThing().getUID(), String.format(CHANNEL_TEMPERATURE_MEASURED_XX__TEMPLATE, zoneTemperature.zone)),
                                    new DecimalType(zoneTemperature.temperature));
                        }
                        LOGGER.severe("EvoHome handled MSG:  " + msg);
                        break;
                    default:
                        LOGGER.severe("EvoHome unhandled MSG:  " + msg);
                }
            }
            break;
            case _18_2309_ROOM_DESIRED_TEMP: {
                //From RadiatorController and MultZoneController
                final EvoHome_0x18_0x2309_0xXX_ROOM_DESIRED_TEMP_Message m = (EvoHome_0x18_0x2309_0xXX_ROOM_DESIRED_TEMP_Message) msg;
                switch (m.deviceId1.type) {
                    case RADIATOR_CONTROLLER:
                        //TODO ZoneID ???
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_DESIRED_TEMPERATURE),
                                new DecimalType(m.zoneTemperatures.get(0).temperature));
                        LOGGER.severe("EvoHome handled MSG:  " + msg);
                        break;
                    case MULTI_ZONE_CONTROLLER:
                        for (ZoneTemperature zoneTemperature : m.zoneTemperatures) {
                            updateState(new ChannelUID(getThing().getUID(), String.format(CHANNEL_DESIRED_TEMPERATURE_XX__TEMPLATE, zoneTemperature.zone)),
                                    new DecimalType(zoneTemperature.temperature));
                        }
                        LOGGER.severe("EvoHome handled MSG:  " + msg);
                        break;
                    default:
                        LOGGER.severe("EvoHome unhandled MSG:  " + msg);
                }
            }
            break;
            case _2C_2309_ROOM_DESIRED_TEMP: {
                //From SingleZoneThermostat
                //TODO ZoneID ???
                final BigDecimal desiredTemp = ((EvoHome_0x2C_0x2309_0xXX_ROOM_DESIRED_TEMP_Message) msg).zoneTemperatures.get(0).temperature;
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_DESIRED_TEMPERATURE),
                        new DecimalType(desiredTemp));
                LOGGER.severe("EvoHome handled MSG:  " + msg);
            }
            break;
            default:
                LOGGER.severe("EvoHome unhandled DeviceMessage:  " + msg);
                break;
        }
    }

}
