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
package de.ibapl.esh.fhz4j;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FHZ4JBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class FHZ4JBindingConstants {

    public static final String BINDING_ID = "fhz4j";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FHZ4J_RADIATOR_FHT80B = new ThingTypeUID(BINDING_ID, "fht80b");
    public static final ThingTypeUID THING_TYPE_FHZ4J_UNKNOWN = new ThingTypeUID(BINDING_ID, "unknown");
    public static final ThingTypeUID THING_TYPE_FHZ4J_EM_1000_EM = new ThingTypeUID(BINDING_ID, "em-1000-em");
    public static final ThingTypeUID THING_TYPE_FHZ4J_HMS_100_TF = new ThingTypeUID(BINDING_ID, "hms-100-tf");
    public static final ThingTypeUID THING_TYPE_FHZ4J_RADIATOR_EVO_HOME = new ThingTypeUID(BINDING_ID, "evo-home-radiator");
    public static final ThingTypeUID THING_TYPE_FHZ4J_SINGLE_ZONE_THERMOSTAT_EVO_HOME = new ThingTypeUID(BINDING_ID, "evo-home-single-zone-thermostat");
    public static final ThingTypeUID THING_TYPE_FHZ4J_MULTI_ZONE_CONTROLLER_EVO_HOME = new ThingTypeUID(BINDING_ID, "evo-home-multi-zone-controller");

    // List of all Bridge Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_FHZ4J_RS232 = new ThingTypeUID(BINDING_ID, "rs232-bridge-cul");

    // List of all Channel ids
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_BATT_LOW = "low-battery";
    public static final String CHANNEL_TEMPERATURE_MEASURED = "temperatureMeasured";
    /**
     * For channels evo home multi zone controller
     */
    public static final String CHANNEL_TEMPERATURE_MEASURED_XX__TEMPLATE = "temperatureMeasured_%02d"; 
    public static final String CHANNEL_HUMIDITY_MEASURED = "humidityMeasured";
    public static final String CHANNEL_DESIRED_TEMPERATURE = "desiredTemperature";
    /**
     * For channels evo home multi zone controller
     */
    public static final String CHANNEL_DESIRED_TEMPERATURE_XX__TEMPLATE = "desiredTemperature_%02d"; 
    public static final String CHANNEL_DESIRED_TEMPERATURE_00 = "desiredTemperature_00";
    public static final String CHANNEL_DESIRED_TEMPERATURE_01 = "desiredTemperature_01";
    public static final String CHANNEL_DESIRED_TEMPERATURE_02 = "desiredTemperature_02";
    public static final String CHANNEL_DESIRED_TEMPERATURE_03 = "desiredTemperature_03";
    public static final String CHANNEL_DESIRED_TEMPERATURE_04 = "desiredTemperature_04";
    public static final String CHANNEL_DESIRED_TEMPERATURE_05 = "desiredTemperature_05";
    public static final String CHANNEL_DESIRED_TEMPERATURE_06 = "desiredTemperature_06";
    public static final String CHANNEL_DESIRED_TEMPERATURE_07 = "desiredTemperature_07";
    public static final String CHANNEL_DESIRED_TEMPERATURE_08 = "desiredTemperature_08";
    public static final String CHANNEL_DESIRED_TEMPERATURE_09 = "desiredTemperature_09";
    public static final String CHANNEL_DESIRED_TEMPERATURE_10 = "desiredTemperature_10";
    public static final String CHANNEL_DESIRED_TEMPERATURE_11 = "desiredTemperature_11";
    public static final String CHANNEL_TEMPERATURE_DAY = "dayTemperature";
    public static final String CHANNEL_TEMPERATURE_NIGHT = "nightTemperature";
    public static final String CHANNEL_TEMPERATURE_WINDOW_OPEN = "windowOpenTemperature";
    public static final String CHANNEL_VALVE_POSITION = "valvePos";
    public static final String CHANNEL_RADIATOR_HEAT_DEMAND = "heatDemand";
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
