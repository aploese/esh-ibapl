package de.ibapl.esh.fhz4j;

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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FHZ4JBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@NonNullByDefault
public class FHZ4JBindingConstants {

    public static final String BINDING_ID = "fhz4j";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FHZ4J_RADIATOR_FHT80B = new ThingTypeUID(BINDING_ID, "fht80b");
    public static final ThingTypeUID THING_TYPE_FHZ4J_UNKNOWN = new ThingTypeUID(BINDING_ID, "unknown");
    public static final ThingTypeUID THING_TYPE_FHZ4J_EM_1000_EM = new ThingTypeUID(BINDING_ID, "em-1000-em");
    public static final ThingTypeUID THING_TYPE_FHZ4J_HMS_100_TF = new ThingTypeUID(BINDING_ID, "hms-100-tf");

    // List of all Bridge Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_FHZ4J_RS232 = new ThingTypeUID(BINDING_ID, "rs232-bridge-cul");

    // List of all Channel ids
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_BATT_LOW = "low-battery";
    public static final String CHANNEL_TEMPERATURE_MEASURED = "temperatureMeasured";
    public static final String CHANNEL_HUMIDITY_MEASURED = "humidityMeasured";
    public static final String CHANNEL_DESIRED_TEMPERATURE = "desiredTemperature";
    public static final String CHANNEL_TEMPERATURE_DAY = "dayTemperature";
    public static final String CHANNEL_TEMPERATURE_NIGHT = "nightTemperature";
    public static final String CHANNEL_TEMPERATURE_WINDOW_OPEN = "windowOpenTemperature";
    public static final String CHANNEL_VALVE_POSITION = "valvePos";
    public static final String CHANNEL_VALVE_ALLOW_LOW_BATT_BEEP = "valveAllowLowBattBeep";

    public static final String CHANNEL_HOLYDAY_END_DATE = "holydayEndDate";
    public static final String CHANNEL_PARTY_END_TIME = "partyEndTime";

    public static final String CHANNEL_MONDAY = "mondaySwitchTimes";
    public static final String CHANNEL_TUESDAY = "tuesdaySwitchTimes";
    public static final String CHANNEL_WEDNESDAY = "wednesdaySwitchTimes";
    public static final String CHANNEL_THURSDAY = "thursdaySwitchTimes";
    public static final String CHANNEL_FRIDAY = "fridaySwitchTimes";
    public static final String CHANNEL_SATURDAY = "saturdaySwitchTimes";
    public static final String CHANNEL_SUNDAY = "sundaySwitchTimes";

    public static final String CHANNEL_ENERGY_TOTAL = "energyTotal";
    public static final String CHANNEL_POWER_5MINUTES = "power5Minutes";
    public static final String CHANNEL_MAX_POWER_5MINUTES = "maxPower5Minutes";
}
