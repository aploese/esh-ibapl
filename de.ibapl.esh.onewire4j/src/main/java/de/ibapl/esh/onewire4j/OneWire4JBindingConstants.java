package de.ibapl.esh.onewire4j;

/*-
 * #%L
 * OneWire4J Binding
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
