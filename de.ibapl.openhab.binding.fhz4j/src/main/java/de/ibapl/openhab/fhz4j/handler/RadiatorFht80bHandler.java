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

import de.ibapl.fhz4j.protocol.fht.Fht80bMode;
import de.ibapl.fhz4j.protocol.fht.Fht80bWarning;
import de.ibapl.fhz4j.protocol.fht.FhtDateMessage;
import de.ibapl.fhz4j.protocol.fht.FhtMessage;
import de.ibapl.fhz4j.protocol.fht.FhtModeMessage;
import de.ibapl.fhz4j.protocol.fht.FhtProperty;
import de.ibapl.fhz4j.protocol.fht.FhtTempMessage;
import de.ibapl.fhz4j.protocol.fht.FhtTimeMessage;
import de.ibapl.fhz4j.protocol.fht.FhtTimesMessage;
import de.ibapl.fhz4j.protocol.fht.FhtValvePosMessage;
import de.ibapl.fhz4j.protocol.fht.FhtWarningMessage;
import static de.ibapl.openhab.fhz4j.FHZ4JBindingConstants.*;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link RadiatorFht80bHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class RadiatorFht80bHandler extends BaseThingHandler {

    protected ThingStatusDetail fht80HandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final static Logger LOGGER = Logger.getLogger("d.i.e.f.h.RadiatorFht80bHandler");
    private static final String CRON_PATTERN_DEVICE_PING = "cronPatternDevicePing";

    private float desiredTemp;

    private short housecode;

    private String cronPatternDevicePing = "0 0 0 ? * SUN *";

    private final CronScheduler cronScheduler;

    private ScheduledCompletableFuture refreshJob;

    public RadiatorFht80bHandler(Thing thing, CronScheduler cronScheduler) {
        super(thing);
        this.cronScheduler = cronScheduler;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        //Handle this here....
        if (configurationParameters.containsKey(CRON_PATTERN_DEVICE_PING)) {
            LOGGER.log(Level.SEVERE, "handleConfigurationUpdate called: cronPatternDevicePing = {0}", configurationParameters.get(CRON_PATTERN_DEVICE_PING));
        } else {
            LOGGER.severe("handleConfigurationUpdate called: cronPatternDevicePing = ");
        }
        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_DESIRED_TEMPERATURE -> {
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
            case CHANNEL_TEMPERATURE_DAY -> {
                if (command instanceof DecimalType decimalType) {
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode, FhtProperty.DAY_TEMP,
                                decimalType.floatValue());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "handleCommand CHANNEL_TEMPERATURE_DAY", e);
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
            }
            case CHANNEL_TEMPERATURE_NIGHT -> {
                if (command instanceof DecimalType decimalType) {
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode,
                                FhtProperty.NIGHT_TEMP, decimalType.floatValue());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "handleCommand CHANNEL_TEMPERATURE_NIGHT", e);
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
            }
            case CHANNEL_TEMPERATURE_WINDOW_OPEN -> {
                if (command instanceof DecimalType decimalType) {
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode,
                                FhtProperty.WINDOW_OPEN_TEMP, decimalType.floatValue());
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "handleCommand CHANNEL_TEMPERATURE_WINDOW_OPEN", e);
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
            }
            case CHANNEL_MONDAY -> {
                if (command instanceof StringType stringType) {
                    sendCycle(DayOfWeek.MONDAY, stringType);
                }
            }
            case CHANNEL_TUESDAY -> {
                if (command instanceof StringType stringType) {
                    sendCycle(DayOfWeek.TUESDAY, stringType);
                }
            }
            case CHANNEL_WEDNESDAY -> {
                if (command instanceof StringType stringType) {
                    sendCycle(DayOfWeek.WEDNESDAY, stringType);
                }
            }
            case CHANNEL_THURSDAY -> {
                if (command instanceof StringType stringType) {
                    sendCycle(DayOfWeek.THURSDAY, stringType);
                }
            }
            case CHANNEL_FRIDAY -> {
                if (command instanceof StringType stringType) {
                    sendCycle(DayOfWeek.FRIDAY, stringType);
                }
            }
            case CHANNEL_SATURDAY -> {
                if (command instanceof StringType stringType) {
                    sendCycle(DayOfWeek.SATURDAY, stringType);
                }
            }
            case CHANNEL_SUNDAY -> {
                if (command instanceof StringType stringType) {
                    sendCycle(DayOfWeek.SUNDAY, stringType);
                }
            }
            case CHANNEL_VALVE_POSITION -> {
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
            }
            case CHANNEL_BATT_LOW -> {
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
            }
            case CHANNEL_MODE -> {
                if (command instanceof StringType stringType) {
                    try {
                        switch (stringType.toString()) {
                            case "AUTO" ->
                                ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtModeAutoMessage(housecode);
                            case "MANUAL" ->
                                ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtModeManuMessage(housecode);
                            default ->
                                throw new IllegalArgumentException("Cant set mode to " + stringType.toString());
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "handleCommand CHANNEL_MODE", e);
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
            }
            case CHANNEL_TEMPERATURE_MEASURED -> {
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
            }
            case CHANNEL_VALVE_ALLOW_LOW_BATT_BEEP -> {
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
            }
            case CHANNEL_PARTY_END_TIME -> {
                if (command instanceof StringType) {
                    String value = command.toString();

                    // TIME_FORMATTER.parse("12:00-13:00 15:00-18:00", new ParsePosition(0)).query(LocalTime::from);
                    String val = value.substring(0, 5);
                    final LocalTime toTime = TIME_NOT_SET.equals(val) ? null : TIME_FORMATTER.parse(val, LocalTime::from);
                    LocalDateTime toDateTime = LocalDateTime.now();
                    final LocalTime nowTime = LocalTime.from(toDateTime);
                    if (toTime.isBefore(nowTime)) {
                        //it is tomorrow
                        toDateTime = toDateTime.plusDays(1);
                    }
                    toDateTime = LocalDateTime.of(toDateTime.getYear(), toDateTime.getMonth(), toDateTime.getDayOfMonth(), toTime.getHour(), toTime.getMinute());

                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtPartyMessage(housecode, desiredTemp, toDateTime);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "handleCommand CHANNEL_PARTY_END_TIME", e);
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
            }
            case CHANNEL_HOLYDAY_END_DATE -> {
                if (command instanceof DateTimeType dateTimeType) {
                    final ZonedDateTime value = dateTimeType.getZonedDateTime();

                    final LocalDate toDate = LocalDate.of(value.getYear(), value.getMonth(), value.getDayOfMonth());
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtHolidayMessage(housecode, desiredTemp, toDate);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "handleCommand CHANNEL_HOLYDAY_END_DATE", e);
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
            }
            default ->
                LOGGER.log(Level.SEVERE, "Unknown Fht80 (" + housecode + ") channel: {0}", channelUID.getId());
        }
    }

    private void sendCycle(DayOfWeek dayOfWeek, StringType command) {
        String value = command.toString();

        // TIME_FORMATTER.parse("12:00-13:00 15:00-18:00", new ParsePosition(0)).query(LocalTime::from);
        String val = value.substring(0, 5);
        final LocalTime from1 = TIME_NOT_SET.equals(val) ? null : TIME_FORMATTER.parse(val, LocalTime::from);

        val = value.substring(6, 11);
        LocalTime to1 = TIME_NOT_SET.equals(val) ? null : TIME_FORMATTER.parse(val, LocalTime::from);
        val = value.substring(12, 17);
        LocalTime from2 = TIME_NOT_SET.equals(val) ? null : TIME_FORMATTER.parse(val, LocalTime::from);
        val = value.substring(18, 23);
        LocalTime to2 = TIME_NOT_SET.equals(val) ? null : TIME_FORMATTER.parse(val, LocalTime::from);

        try {
            ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode, dayOfWeek, from1, to1, from2,
                    to2);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "sendCycle", e);
        }

    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        try {
            housecode = ((Number) configuration.get("housecode")).shortValue();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse housecode");
            fht80HandlerStatus = ThingStatusDetail.HANDLER_INITIALIZING_ERROR;
            return;
        }
        if (configuration.containsKey(CRON_PATTERN_DEVICE_PING)) {
            cronPatternDevicePing = configuration.get(CRON_PATTERN_DEVICE_PING).toString();
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no bridge assigned");
            fht80HandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
            return;
        } else {
            if (bridge.getStatus().equals(ThingStatus.ONLINE)) {
                updateStatus(ThingStatus.ONLINE);
                fht80HandlerStatus = ThingStatusDetail.NONE;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        }
        try {
            refreshJob = cronScheduler.schedule(() -> {
                LOGGER.log(Level.FINE, "Try run trigger reporting for {0}", housecode);
                try {
                    final BridgeHandler myBridge = getBridge().getHandler();
                    if (myBridge instanceof SpswBridgeHandler spswBridgeHandler) {
                        spswBridgeHandler.setClock(housecode, LocalDateTime.now());
                        spswBridgeHandler.initFhtReporting(housecode);
                        LOGGER.log(Level.INFO, "Did run update clock and trigger reporting of {0} succesfully", housecode);
                    } else {
                        LOGGER.log(Level.SEVERE, "Reporting for {0} not triggerd, can't get bridge.", housecode);
                    }
                } catch (IOException ioe) {
                    LOGGER.log(Level.SEVERE, "Could not init fht reporting for " + housecode + "due to an IO error", ioe);
                } catch (NullPointerException npe) {
                    LOGGER.log(Level.SEVERE, "Could not init fht reporting for " + housecode + "due cul adapter may be null", npe);
                }
            }, cronPatternDevicePing);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Could not schedule fht reporting for:" + housecode, ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Can't schedule refresh job. " + ex);
            fht80HandlerStatus = ThingStatusDetail.CONFIGURATION_ERROR;
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(false);
        }
    }

    public short getHousecode() {
        return housecode;
    }

    private void updateMode(FhtModeMessage modeMsg) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE), new StringType(modeMsg.mode.name()));
        switch (modeMsg.mode) {
            case AUTO -> {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HOLYDAY_END_DATE), new StringType());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME), new StringType());
            }
            case MANUAL -> {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HOLYDAY_END_DATE), new StringType());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME), new StringType());
            }
            case PARTY ->
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HOLYDAY_END_DATE), new StringType());
            case HOLIDAY ->
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME), new StringType());
            default -> {
            }
        }
    }

    public void updateFromFhtMsg(FhtMessage fhtMsg) {
        switch (fhtMsg.command) {
            case MODE ->
                updateMode((FhtModeMessage) fhtMsg);
            case MONDAY_TIMES ->
                update_FROM_TO(CHANNEL_MONDAY, (FhtTimesMessage) fhtMsg);
            case TUESDAY_TIMES ->
                update_FROM_TO(CHANNEL_TUESDAY, (FhtTimesMessage) fhtMsg);
            case WEDNESDAY_TIMES ->
                update_FROM_TO(CHANNEL_WEDNESDAY, (FhtTimesMessage) fhtMsg);
            case THURSDAY_TIMES ->
                update_FROM_TO(CHANNEL_THURSDAY, (FhtTimesMessage) fhtMsg);
            case FRIDAY_TIMES ->
                update_FROM_TO(CHANNEL_FRIDAY, (FhtTimesMessage) fhtMsg);
            case SATURDAYDAY_TIMES ->
                update_FROM_TO(CHANNEL_SATURDAY, (FhtTimesMessage) fhtMsg);
            case SUNDAYDAY_TIMES ->
                update_FROM_TO(CHANNEL_SUNDAY, (FhtTimesMessage) fhtMsg);
            case WARNINGS -> {
                final Set<Fht80bWarning> warnings = ((FhtWarningMessage) fhtMsg).warnings;
                if (warnings.contains(Fht80bWarning.BATT_LOW)) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.ON);
                } else {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.OFF);
                }
            }
            case DAY_TEMP ->
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_DAY),
                        new DecimalType(((FhtTempMessage) fhtMsg).temp));
            case NIGHT_TEMP ->
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_NIGHT),
                        new DecimalType(((FhtTempMessage) fhtMsg).temp));
            case WINDOW_OPEN_TEMP ->
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_WINDOW_OPEN),
                        new DecimalType(((FhtTempMessage) fhtMsg).temp));
            case MANU_TEMP -> {
            }
            case HOLIDAY_END_DATE -> {
                updateHolidays((FhtDateMessage) fhtMsg);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE),
                        new StringType(Fht80bMode.HOLIDAY.name()));
            }
            case PARTY_END_TIME -> {
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME),
                        new StringType((((FhtTimeMessage) fhtMsg).time.format(TIME_FORMATTER))));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE), new StringType(Fht80bMode.PARTY.name()));
            }
            case VALVE -> {
                //Do ignore FhtValveSynhcMessage
                if (fhtMsg instanceof FhtValvePosMessage fhtValvePosMessage) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION),
                            new DecimalType((fhtValvePosMessage.position)));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_ALLOW_LOW_BATT_BEEP),
                            fhtValvePosMessage.allowLowBatteryBeep ? OnOffType.ON : OnOffType.OFF);
                }
            }
            case MEASURED_TEMP ->
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_MEASURED),
                        new DecimalType((((FhtTempMessage) fhtMsg).temp)));
            case DESIRED_TEMP -> {
                desiredTemp = ((FhtTempMessage) fhtMsg).temp;
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_DESIRED_TEMPERATURE),
                        new DecimalType(desiredTemp));
            }

            default -> {
            }
        }
    }

    private void updateHolidays(FhtDateMessage fhtMsg) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime result = ZonedDateTime.of(now.getYear(), fhtMsg.month, fhtMsg.day, 0, 0, 0, 0, now.getZone());
        if (result.isBefore(now)) {
            result = result.plusYears(1);
        }
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_HOLYDAY_END_DATE), new DateTimeType(result));
    }

    private final static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final static String TIME_NOT_SET = "XX:XX";

    private void update_FROM_TO(String channelFromTo, FhtTimesMessage timesMessage) {
        StringBuilder sb = new StringBuilder();
        if (timesMessage.timeFrom1 != null) {
            TIME_FORMATTER.formatTo(timesMessage.timeFrom1, sb);
        } else {
            sb.append(TIME_NOT_SET);
        }
        sb.append('-');
        if (timesMessage.timeTo1 != null) {
            TIME_FORMATTER.formatTo(timesMessage.timeTo1, sb);
        } else {
            sb.append(TIME_NOT_SET);
        }
        sb.append(' ');
        if (timesMessage.timeFrom2 != null) {
            TIME_FORMATTER.formatTo(timesMessage.timeFrom2, sb);
        } else {
            sb.append(TIME_NOT_SET);
        }
        sb.append('-');
        if (timesMessage.timeTo2 != null) {
            TIME_FORMATTER.formatTo(timesMessage.timeTo2, sb);
        } else {
            sb.append(TIME_NOT_SET);
        }
        updateState(new ChannelUID(getThing().getUID(), channelFromTo), new StringType(sb.toString()));
    }

}
