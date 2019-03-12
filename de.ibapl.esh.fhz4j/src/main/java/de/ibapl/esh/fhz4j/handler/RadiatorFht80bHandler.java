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
import com.google.common.collect.HashBiMap;
import static de.ibapl.esh.fhz4j.FHZ4JBindingConstants.*;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import org.eclipse.smarthome.core.library.types.DateTimeType;

/**
 * The {@link RadiatorFht80bHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class RadiatorFht80bHandler extends BaseThingHandler {

    protected ThingStatusDetail fht80HandlerStatus = ThingStatusDetail.HANDLER_CONFIGURATION_PENDING;

    private final Logger logger = Logger.getLogger("esh.binding.fhz4j");
    
    private float desiredTemp;

    private short housecode;

    public RadiatorFht80bHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_DESIRED_TEMPERATURE:
                if (command instanceof DecimalType) {
                    try {
                        desiredTemp = ((DecimalType) command).floatValue();
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode,
                                FhtProperty.DESIRED_TEMP, desiredTemp);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (command instanceof RefreshType) {
                       desiredTemp = 17.0f;
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
                break;
            case CHANNEL_TEMPERATURE_DAY:
                if (command instanceof DecimalType) {
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode, FhtProperty.DAY_TEMP,
                                ((DecimalType) command).floatValue());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
                break;
            case CHANNEL_TEMPERATURE_NIGHT:
                if (command instanceof DecimalType) {
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode,
                                FhtProperty.NIGHT_TEMP, ((DecimalType) command).floatValue());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
                break;
            case CHANNEL_TEMPERATURE_WINDOW_OPEN:
                if (command instanceof DecimalType) {
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtMessage(housecode,
                                FhtProperty.WINDOW_OPEN_TEMP, ((DecimalType) command).floatValue());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
                break;
            case CHANNEL_MONDAY:
                if (command instanceof StringType) {
                    sendCycle(DayOfWeek.MONDAY, (StringType) command);
                }
                break;
            case CHANNEL_TUESDAY:
                if (command instanceof StringType) {
                    sendCycle(DayOfWeek.TUESDAY, (StringType) command);
                }
                break;
            case CHANNEL_WEDNESDAY:
                if (command instanceof StringType) {
                    sendCycle(DayOfWeek.WEDNESDAY, (StringType) command);
                }
                break;
            case CHANNEL_THURSDAY:
                if (command instanceof StringType) {
                    sendCycle(DayOfWeek.THURSDAY, (StringType) command);
                }
                break;
            case CHANNEL_FRIDAY:
                if (command instanceof StringType) {
                    sendCycle(DayOfWeek.FRIDAY, (StringType) command);
                }
                break;
            case CHANNEL_SATURDAY:
                if (command instanceof StringType) {
                    sendCycle(DayOfWeek.SATURDAY, (StringType) command);
                }
                break;
            case CHANNEL_SUNDAY:
                if (command instanceof StringType) {
                    sendCycle(DayOfWeek.SUNDAY, (StringType) command);
                }
                break;
            case CHANNEL_VALVE_POSITION:
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
                break;
            case CHANNEL_BATT_LOW:
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
                break;
            case CHANNEL_MODE:
                if (command instanceof StringType) {
                    try {
                        switch (((StringType) command).toString()) {
                            case "AUTO":
                                ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtModeAutoMessage(housecode);
                                break;
                            case "MANUAL":
                                ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtModeManuMessage(housecode);
                                break;
                            default:
                                throw new IllegalArgumentException("Cant set mode to " + ((StringType) command).toString());
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE), new DecimalType(00.00));
                }
                break;
            case CHANNEL_TEMPERATURE_MEASURED:
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
                break;
            case CHANNEL_VALVE_ALLOW_LOW_BATT_BEEP:
                if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
                break;
            case CHANNEL_PARTY_END_TIME:
                if (command instanceof StringType) {
                    String value = command.toString();

                    // TIME_FORMATTER.parse("12:00-13:00 15:00-18:00", new ParsePosition(0)).query(LocalTime::from);
                    String val = value.substring(0, 5);
                    final LocalTime toTime = TIME_NOT_SET.equals(val) ? null : TIME_FORMATTER.parse(val, LocalTime::from);
                    LocalDateTime toDateTime = LocalDateTime.now();
                    LocalTime nowTime = LocalTime.from(toDateTime);
                    if (toTime.isBefore(nowTime)) {
                        //it is tomorrow
                        toDateTime = toDateTime.plusDays(1);
                    }
                    toDateTime = LocalDateTime.of(toDateTime.getYear(), toDateTime.getMonth(), toDateTime.getDayOfMonth(), toTime.getHour(), toTime.getMinute());

                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtPartyMessage(housecode, desiredTemp, toDateTime);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
                break;
            case CHANNEL_HOLYDAY_END_DATE:
                if (command instanceof DateTimeType) {
                    final ZonedDateTime value = ((DateTimeType) command).getZonedDateTime();

                    final LocalDate toDate = LocalDate.of(value.getYear(), value.getMonth(), value.getDayOfMonth());
                    try {
                        ((SpswBridgeHandler) (getBridge().getHandler())).sendFhtHolidayMessage(housecode, desiredTemp, toDate);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (command instanceof RefreshType) {
                    // updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION), new DecimalType(22.22));
                }
                break;
            default:
                logger.log(Level.SEVERE, "Unknown Fht80 (" + housecode + ") channel: {0}", channelUID.getId());
        }
    }

    private void sendCycle(DayOfWeek dayOfWeek, @NonNull StringType command) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void initialize() {
        logger.log(Level.FINE, "thing {0} is initializing", this.thing.getUID());
        Configuration configuration = getConfig();
        try {
            housecode = ((Number) configuration.get("housecode")).shortValue();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR, "Can't parse housecode");
            fht80HandlerStatus = ThingStatusDetail.HANDLER_INITIALIZING_ERROR;
            return;
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
    }

    @Override
    public void dispose() {
    }

    public short getHousecode() {
        return housecode;
    }

    private void updateMode(FhtModeMessage modeMsg) {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE), new StringType(modeMsg.mode.name()));
        switch (modeMsg.mode) {
            case AUTO:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HOLYDAY_END_DATE), new StringType());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME), new StringType());
                break;
            case MANUAL:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HOLYDAY_END_DATE), new StringType());
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME), new StringType());
                break;
            case PARTY:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_HOLYDAY_END_DATE), new StringType());
                break;
            case HOLIDAY:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME), new StringType());
                break;
            default:;
        }
    }

    public void updateFromMsg(FhtMessage fhtMsg) {
        switch (fhtMsg.command) {
            case MODE:
                updateMode((FhtModeMessage) fhtMsg);
                break;
            case MONDAY_TIMES:
                update_FROM_TO(CHANNEL_MONDAY, (FhtTimesMessage) fhtMsg);
                break;
            case TUESDAY_TIMES:
                update_FROM_TO(CHANNEL_TUESDAY, (FhtTimesMessage) fhtMsg);
                break;
            case WEDNESDAY_TIMES:
                update_FROM_TO(CHANNEL_WEDNESDAY, (FhtTimesMessage) fhtMsg);
                break;
            case THURSDAY_TIMES:
                update_FROM_TO(CHANNEL_THURSDAY, (FhtTimesMessage) fhtMsg);
                break;
            case FRIDAY_TIMES:
                update_FROM_TO(CHANNEL_FRIDAY, (FhtTimesMessage) fhtMsg);
                break;
            case SATURDAYDAY_TIMES:
                update_FROM_TO(CHANNEL_SATURDAY, (FhtTimesMessage) fhtMsg);
                break;
            case SUNDAYDAY_TIMES:
                update_FROM_TO(CHANNEL_SUNDAY, (FhtTimesMessage) fhtMsg);
                break;
            case WARNINGS:
                final Set<Fht80bWarning> warnings = ((FhtWarningMessage) fhtMsg).warnings;
                if (warnings.contains(Fht80bWarning.BATT_LOW)) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.ON);
                } else {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATT_LOW), OnOffType.OFF);
                }
                break;
            case DAY_TEMP:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_DAY),
                        new DecimalType(((FhtTempMessage) fhtMsg).temp));
                break;
            case NIGHT_TEMP:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_NIGHT),
                        new DecimalType(((FhtTempMessage) fhtMsg).temp));
                break;
            case WINDOW_OPEN_TEMP:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_WINDOW_OPEN),
                        new DecimalType(((FhtTempMessage) fhtMsg).temp));
                break;
            case MANU_TEMP:
                break;
            case HOLIDAY_END_DATE:
                updateHolidays((FhtDateMessage) fhtMsg);
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE),
                        new StringType(Fht80bMode.HOLIDAY.name()));
                break;
            case PARTY_END_TIME:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PARTY_END_TIME),
                        new StringType((((FhtTimeMessage) fhtMsg).time.format(TIME_FORMATTER))));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE), new StringType(Fht80bMode.PARTY.name()));
                break;
            case VALVE:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_POSITION),
                        new DecimalType((((FhtValvePosMessage) fhtMsg).position)));
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE_ALLOW_LOW_BATT_BEEP),
                        ((FhtValvePosMessage) fhtMsg).allowLowBatteryBeep ? OnOffType.ON : OnOffType.OFF);
                break;
            case MEASURED_TEMP:
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE_MEASURED),
                        new DecimalType((((FhtTempMessage) fhtMsg).temp)));
                break;
            case DESIRED_TEMP:
                desiredTemp = ((FhtTempMessage) fhtMsg).temp;
                updateState(new ChannelUID(getThing().getUID(), CHANNEL_DESIRED_TEMPERATURE),
                        new DecimalType(desiredTemp));
                break;

            default:
                break;
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

    private void update_FROM_TO(@NonNull String channelFromTo, FhtTimesMessage timesMessage) {
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
