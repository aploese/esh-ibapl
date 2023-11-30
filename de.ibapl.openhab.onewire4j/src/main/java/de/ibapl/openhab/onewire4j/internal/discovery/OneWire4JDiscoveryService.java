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
package de.ibapl.openhab.onewire4j.internal.discovery;

import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.OneWireDevice;
import de.ibapl.onewire4j.container.OneWireDevice26;
import de.ibapl.onewire4j.container.TemperatureContainer;
import de.ibapl.openhab.onewire4j.OneWire4JBindingConstants;
import de.ibapl.openhab.onewire4j.handler.SpswBridgeHandler;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class OneWire4JDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = Logger.getLogger("esh.binding.onewire4j");

    private final static int SEARCH_TIME = 60;

    private final SpswBridgeHandler spswBridgeHandler;

    public OneWire4JDiscoveryService(SpswBridgeHandler spswBridgeHandler) {
        super(SpswBridgeHandler.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.spswBridgeHandler = spswBridgeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SpswBridgeHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        try {
            spswBridgeHandler.discover(container -> {
                addDevice(container.getAddress());
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        stopScan();
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        stopScan();
    }

    private void addDevice(long deviceId) {
        final String deviceIdStr = OneWireContainer.addressToString(deviceId);
        final ThingUID thingUID = getThingUID(deviceIdStr);

        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        try {
            final DiscoveryResult discoveryResult;
            OneWireDevice owd = OneWireDevice.fromAdress(deviceId);
            if (owd instanceof TemperatureContainer) {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(getThingTypeUID("temperature"))
                        .withProperty("deviceId", deviceIdStr).withBridge(bridgeUID)
                        .withRepresentationProperty("deviceId")
                        .withLabel("Temp" + deviceIdStr)
                        .build();
                thingDiscovered(discoveryResult);
            } else if (owd instanceof OneWireDevice26) {
                //TODO humidity for now, but there are many other things battery moniotor ....
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(getThingTypeUID("humidity"))
                        .withProperty("deviceId", deviceIdStr)
                        .withRepresentationProperty("deviceId")
                        .withBridge(bridgeUID)
                        .withLabel("Humidity" + deviceIdStr)
                        .build();
                thingDiscovered(discoveryResult);
            } else {
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(getThingTypeUID("unknown"))
                        .withProperty("deviceId", deviceIdStr)
                        .withBridge(bridgeUID)
                        .withRepresentationProperty("deviceId")
                        .withLabel("UNKNOWN" + deviceIdStr)
                        .build();
                thingDiscovered(discoveryResult);
            }
        } catch (Exception e) {
            // Unknown device
            //TODO logging
            e.printStackTrace();
        }
    }

    private ThingUID getThingUID(String deviceId) {
        ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        ThingTypeUID thingTypeUID = getThingTypeUID("temperature");

        return new ThingUID(thingTypeUID, bridgeUID, deviceId);
    }

    private ThingTypeUID getThingTypeUID(String oneWireBinding) {
        return new ThingTypeUID(OneWire4JBindingConstants.BINDING_ID, oneWireBinding);
    }

}
