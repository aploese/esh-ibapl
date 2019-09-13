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
package de.ibapl.esh.onewire4j;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OneWire4JBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@NonNullByDefault
public class OneWire4JBindingConstants {

    public static final String BINDING_ID = "onewire4j";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ONEWIRE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public static final ThingTypeUID THING_TYPE_ONEWIRE_UNKNOWN = new ThingTypeUID(BINDING_ID, "unknown");

    // List of all Bridge Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_ONEWIRE_RS232 = new ThingTypeUID(BINDING_ID, "rs232-bridge");

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_MIN_TEMPERATURE = "minTemperature";
    public static final String CHANNEL_MAX_TEMPERATURE = "maxTemperature";

}
