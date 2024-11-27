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
package de.ibapl.openhab.fhz4j.handler;

import de.ibapl.fhz4j.protocol.evohome.DeviceId;
import static de.ibapl.fhz4j.protocol.evohome.DeviceType.MULTI_ZONE_CONTROLLER;
import static de.ibapl.fhz4j.protocol.evohome.DeviceType.RADIATOR_CONTROLLER;
import static de.ibapl.fhz4j.protocol.evohome.DeviceType.SINGLE_ZONE_THERMOSTAT;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeCommand;
import static de.ibapl.fhz4j.protocol.evohome.EvoHomeCommand.UNKNOWN_3120;
import static de.ibapl.fhz4j.protocol.evohome.EvoHomeCommand.ZONE_CONFIG;
import static de.ibapl.fhz4j.protocol.evohome.EvoHomeCommand.ZONE_SETPOINT;
import static de.ibapl.fhz4j.protocol.evohome.EvoHomeCommand.ZONE_TEMPERATURE;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeDeviceMessage;
import de.ibapl.fhz4j.protocol.evohome.ZoneTemperature;
import de.ibapl.fhz4j.protocol.evohome.messages.AbstractZoneSetpointPayloadMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.AbstractZoneTemperaturePayloadMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ActuatorSyncInformationMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.DeviceBatteryStatusInformationMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.DeviceInformationInformationMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.LocalizationRequestMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.LocalizationResponseMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.SystemSynchronizationPayloadMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.SystemSynchronizationRequestMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.SystemTimestampRequestMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.SystemTimestampResponseMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.Unknown_0x3120InformationMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.WindowSensorInformationMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ZoneConfigPayloadMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ZoneConfigRequestMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ZoneHeatDemandInformationMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ZoneNamePayloadMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ZoneNameRequestMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ZoneSetpointInformationMessage;
import de.ibapl.fhz4j.protocol.evohome.messages.ZoneSetpointRequestMessage;
import static de.ibapl.openhab.fhz4j.FHZ4JBindingConstants.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link RadiatorFht80bHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class EvoHomeHandler extends BaseThingHandler {

    class TestLogger {

        private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;
        private final PrintStream TEST_LOG_OS;
        private Instant lastTS;
        private EvoHomeDeviceMessage lastMsg;
        private final EvoHomeCommand cmd;

        public TestLogger(EvoHomeCommand cmd) {
            this.cmd = cmd;
            try {
                TEST_LOG_OS = new PrintStream(new FileOutputStream("EVO_HOME__" + cmd + "__" + DateTimeFormatter.ISO_INSTANT.format(Instant.now()) + ".log.txt"), false);
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }

        public void print(EvoHomeDeviceMessage msg) {
            final Instant now = Instant.now();
            if (lastTS == null) {
                lastTS = now;
            }
            if (Objects.equals(lastMsg, msg)) {
                TEST_LOG_OS.append('@' + dateTimeFormatter.format(now) + "\t" + Duration.between(lastTS, now).toString() + '\t').println("\tSame message repeated");
            } else {
                TEST_LOG_OS.append('@' + dateTimeFormatter.format(now) + "\t" + Duration.between(lastTS, now).toString() + '\t').append("\t").println(msg);
                //only change lastMsg if we find any differences ...
                lastMsg = msg;
            }
            lastTS = Instant.now();
        }

    }

    private final Map<EvoHomeCommand, TestLogger> loggers = new HashMap<>();

    private void logEvoHomeMsg(EvoHomeDeviceMessage msg) {
        TestLogger logger = loggers.get(msg.command);
        if (logger == null) {
            logger = new TestLogger(msg.command);
            loggers.put(msg.command, logger);
        }
        logger.print(msg);
    }

    //TODO END TEST unknown packages
    protected ThingStatusDetail evoHomeRadiatorHandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final static Logger LOGGER = Logger.getLogger("d.i.o.f.h.EvoHomeHandler");

    private int deviceId;

    public EvoHomeHandler(Thing thing) {
        super(thing);
    }

    private byte getZoneId(ChannelUID channelUID) {
        final String cuid = channelUID.getId();
        final int length = cuid.length();
        return Byte.parseByte(cuid.substring(length - 2, length));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (channelUID.getId().startsWith(CHANNEL_DESIRED_TEMPERATURE)) {
                final byte zoneId = getZoneId(channelUID);
                if (command instanceof DecimalType decimalType) {
                    ((SpswBridgeHandler) (getBridge().getHandler())).sendEvoHomeZoneSetpointPermanent(new DeviceId(deviceId),
                            new ZoneTemperature(zoneId, decimalType.toBigDecimal()));
                } else if (command instanceof RefreshType) {
                }
            } else {
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can''t parse deviceId");
            evoHomeRadiatorHandlerStatus = ThingStatusDetail.HANDLER_INITIALIZING_ERROR;
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            evoHomeRadiatorHandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
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
        switch (msg.command) {
            case ZONE_HEAT_DEMAND -> {
                final ZoneHeatDemandInformationMessage zhdim = (ZoneHeatDemandInformationMessage) msg;
                final float valvePos = zhdim.calcValvePosition();
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION),
                        new DecimalType(valvePos));
                final short heatDeamnd = zhdim.heatDemand;
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_RADIATOR_HEAT_DEMAND),
                        new DecimalType(heatDeamnd));
            }
            case ZONE_TEMPERATURE -> {
                //From SingleZoneThermostat and RadiatorController and MultZoneController
                final AbstractZoneTemperaturePayloadMessage<?> m = (AbstractZoneTemperaturePayloadMessage) msg;
                switch (m.deviceId1.type) {
                    case RADIATOR_CONTROLLER, SINGLE_ZONE_THERMOSTAT -> //TODO ZoneID ???
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_MEASURED),
                                new DecimalType(m.zoneTemperatures.get(0).temperature));
                    case MULTI_ZONE_CONTROLLER -> {
                        for (ZoneTemperature zoneTemperature : m.zoneTemperatures) {
                            updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_TEMPERATURE_MEASURED, zoneTemperature.zone)),
                                    new DecimalType(zoneTemperature.temperature));
                        }
                    }
                    default -> {
                        LOGGER.log(Level.SEVERE, "Can''t handle ZONE_TEMPERATURE (unknown deviceId1.type) message: {0}", msg);
                    }
                }
            }
            case ZONE_SETPOINT -> {
                if (msg instanceof final AbstractZoneSetpointPayloadMessage<?> azspm) {
                    //From RadiatorController and MultZoneController
                    switch (azspm.deviceId1.type) {
                        case RADIATOR_CONTROLLER, SINGLE_ZONE_THERMOSTAT -> //TODO ZoneID ???
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_DESIRED_TEMPERATURE),
                                    new DecimalType(azspm.zoneTemperatures.get(0).temperature));
                        case MULTI_ZONE_CONTROLLER -> {
                            for (ZoneTemperature zoneTemperature : azspm.zoneTemperatures) {
                                updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_DESIRED_TEMPERATURE, zoneTemperature.zone)),
                                        new DecimalType(zoneTemperature.temperature));
                            }
                        }
                        default -> {
                            LOGGER.log(Level.SEVERE, "Can''t handle ZONE_SETPOINT (unknown deviceId1.type) message: {0}", msg);
                        }
                    }
                } else if (msg instanceof ZoneSetpointRequestMessage m) {
                    LOGGER.log(Level.INFO, "ZoneSetpointRequestMessage received ZONE_SETPOINT payload: \"{0}\"", m);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle ZONE_SETPOINT class: {0} message: {1}", new Object[]{msg.getClass().getSimpleName(), msg});
                }
            }
            case ZONE_CONFIG -> {
                if (msg instanceof final ZoneConfigPayloadMessage<?> zpm) {
                    switch (zpm.deviceId1.type) {
                        case RADIATOR_CONTROLLER, SINGLE_ZONE_THERMOSTAT -> {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_MIN_TEMP),
                                    new DecimalType(zpm.zones.get(0).minTemperature));
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_TEMP),
                                    new DecimalType(zpm.zones.get(0).maxTemperature));
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_OPERATION_LOCK),
                                    zpm.zones.get(0).operationLock ? OnOffType.ON : OnOffType.OFF);
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_FUNCTION),
                                    zpm.zones.get(0).windowFunction ? OnOffType.ON : OnOffType.OFF);
                        }
                        case MULTI_ZONE_CONTROLLER -> {
                            for (ZoneConfigPayloadMessage.ZoneParams zoneParam : zpm.zones) {
                                updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_MIN_TEMP, zoneParam.zoneId)),
                                        new DecimalType(zoneParam.minTemperature));
                                updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_MAX_TEMP, zoneParam.zoneId)),
                                        new DecimalType(zoneParam.maxTemperature));
                                updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_OPERATION_LOCK, zoneParam.zoneId)),
                                        zoneParam.operationLock ? OnOffType.ON : OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_WINDOW_FUNCTION, zoneParam.zoneId)),
                                        zoneParam.windowFunction ? OnOffType.ON : OnOffType.OFF);
                            }
                        }
                        default -> {
                            LOGGER.log(Level.SEVERE, "Can''t handle ZONE_SETPOINT (unknown deviceId1.type) message: {0}", msg);
                        }
                    }
                } else if (msg instanceof ZoneConfigRequestMessage m) {
                    LOGGER.log(Level.INFO, "ZoneConfigRequestMessage received ZONE_CONFIG payload: \"{0}\"", m);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle ZONE_CONFIG class: {0} message: {1}", new Object[]{msg.getClass().getSimpleName(), msg});
                }
            }
            case UNKNOWN_3120 -> {
                if (msg instanceof Unknown_0x3120InformationMessage uim) {
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case SYSTEM_SYNCHRONIZATION -> {
                if (msg instanceof SystemSynchronizationRequestMessage) {
                    logEvoHomeMsg(msg);
                } else if (msg instanceof SystemSynchronizationPayloadMessage) {
                    //each 2m 20s the MULTI_ZONE_CONTROLLER will sent this constatnt message to itself
                    //{protocol : EVO_HOME, msgType : INFORMATION, command : SYSTEM_SYNCHRONIZATION, msgParam0 : _8, deviceId1 : {id : 0x067aec, type : "MULTI_ZONE_CONTROLLER"}, deviceId2 : {id : 0x067aec, type : "MULTI_ZONE_CONTROLLER"}, device_id : 0xff, countdown : 1405}
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case ACTUATOR_SYNC -> {
                if (msg instanceof ActuatorSyncInformationMessage) {
                    //each 10m + 2-7 s the MULTI_ZONE_CONTROLLER will sent this constatnt message to itself
                    //{protocol : EVO_HOME, msgType : INFORMATION, command : ACTUATOR_SYNC, msgParam0 : _8, deviceId1 : {id : 0x067aec, type : "MULTI_ZONE_CONTROLLER"}, deviceId2 : {id : 0x067aec, type : "MULTI_ZONE_CONTROLLER"}, domain_id : 0xfc, state : 0xc8}
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case SYSTEM_TIMESTAMP -> {
                if (msg instanceof SystemTimestampRequestMessage) {
                    logEvoHomeMsg(msg);
                } else if (msg instanceof SystemTimestampResponseMessage) {
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case ZONE_SETPOINT_OVERRIDE -> {
                if (msg instanceof ZoneSetpointInformationMessage) {
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case DEVICE_BATTERY_STATUS -> {
                if (msg instanceof DeviceBatteryStatusInformationMessage dbsim) {
                    switch (dbsim.deviceId1.type) {
                        case RADIATOR_CONTROLLER, SINGLE_ZONE_THERMOSTAT -> {
                            final OnOffType value;
                            switch (dbsim.unknown0) {
                                case 0 ->
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.OFF);
                                case (byte) 0x01 ->
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.ON);
                                default ->
                                    LOGGER.log(Level.SEVERE, "Can''t handle DEVICE_BATTERY_STATUS (unknown value for unknown0) message: {0} ", msg);
                            }
                        }
                        default -> {
                            LOGGER.log(Level.SEVERE, "Can''t handle DEVICE_BATTERY_STATUS (unknown deviceId1.type) message: {0}", msg);
                        }
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle DEVICE_BATTERY_STATUS (not an WindowSensorInformationMessage, but: {0}) message: {1}", new Object[]{msg.getClass().getSimpleName(), msg});
                }
            }
            case LOCALIZATION -> {
                if (msg instanceof LocalizationRequestMessage) {
                    logEvoHomeMsg(msg);
                } else if (msg instanceof LocalizationResponseMessage) {
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case DEVICE_INFORMATION -> {
                if (msg instanceof DeviceInformationInformationMessage diim) {
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case ZONE_NAME -> {
                if (msg instanceof ZoneNameRequestMessage) {
                    logEvoHomeMsg(msg);
                } else if (msg instanceof ZoneNamePayloadMessage) {
                    logEvoHomeMsg(msg);
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
                }
            }
            case WINDOW_SENSOR -> {
                if (msg instanceof WindowSensorInformationMessage wsim) {
                    switch (wsim.deviceId1.type) {
                        case RADIATOR_CONTROLLER -> {
                            final OpenClosedType value;
                            switch (wsim.unknown0) {
                                case 0 ->
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_OPEN), OpenClosedType.CLOSED);
                                case (short) 0xc800 ->
                                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDOW_OPEN), OpenClosedType.OPEN);
                                default ->
                                    LOGGER.log(Level.SEVERE, "Can''t handle WINDOW_SENSOR (unknown value for unknown0) message: {0} ", msg);
                            }
                        }
                        case MULTI_ZONE_CONTROLLER -> {
                            switch (wsim.unknown0) {
                                case 0 ->
                                    updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_WINDOW_OPEN, wsim.zoneId)),
                                            OpenClosedType.CLOSED);
                                case (short) 0xc800 ->
                                    updateState(new ChannelUID(getThing().getUID(), String.format(_XX_TEMPLATE, CHANNEL_WINDOW_OPEN, wsim.zoneId)),
                                            OpenClosedType.OPEN);
                                default ->
                                    LOGGER.log(Level.SEVERE, "Can''t handle WINDOW_SENSOR (unknown value for unknown0) message: {0} ", msg);
                            }
                        }
                        default -> {
                            LOGGER.log(Level.SEVERE, "Can''t handle WINDOW_SENSOR (unknown deviceId1.type) message: {0}", msg);
                        }
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Can''t handle WINDOW_SENSOR (not an WindowSensorInformationMessage, but: {0}) message: {1}", new Object[]{msg.getClass().getSimpleName(), msg});
                }
            }
            default -> {
                LOGGER.log(Level.SEVERE, "Can''t handle {0} class: {1} message: {2}", new Object[]{msg.command, msg.getClass().getSimpleName(), msg});
            }
        }
    }

}
