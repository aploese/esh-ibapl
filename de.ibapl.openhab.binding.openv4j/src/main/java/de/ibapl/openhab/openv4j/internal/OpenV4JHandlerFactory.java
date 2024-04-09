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
package de.ibapl.openhab.openv4j.internal;

import de.ibapl.openhab.openv4j.OpenV4JBindingConstants;
import de.ibapl.openhab.openv4j.handler.BoilerV200KW2Handler;
import de.ibapl.openhab.openv4j.handler.SpswBridgeHandler;
import de.ibapl.openhab.openv4j.handler.UnknownDeviceHandler;
import de.ibapl.openhab.openv4j.internal.discovery.OpenV4JDiscoveryService;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link OpenV4JHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.fhz4j")
public class OpenV4JHandlerFactory extends BaseThingHandlerFactory {

    private static final Logger logger = Logger.getLogger("d.i.o.o.h.OpenV4JHandlerFactory");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(OpenV4JBindingConstants.THING_TYPE_OPENV4J_BOILER_V200KW2);

    @Reference
    private List<SerialPortSocketFactory> serialPortSocketFactories;

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(OpenV4JBindingConstants.THING_TYPE_OPENV4J_BOILER_V200KW2)) {
            return new BoilerV200KW2Handler(thing);
        } else if (thingTypeUID.equals(OpenV4JBindingConstants.BRIDGE_TYPE_OPENV4J_RS232)) {
            if (serialPortSocketFactories == null) {
                logger.severe("serialPortSocketFactory == null");
                //TODO
                return null;
            } else {
                final SpswBridgeHandler spswBridgeHandler = new SpswBridgeHandler((Bridge) thing, serialPortSocketFactories);
                registerDiscoveryService(spswBridgeHandler);
                return spswBridgeHandler;
            }
        } else if (thingTypeUID.equals(OpenV4JBindingConstants.THING_TYPE_OPENV4J_UNKNOWN)) {
            return new UnknownDeviceHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerDiscoveryService(SpswBridgeHandler spswBridgeHandler) {
        OpenV4JDiscoveryService discoveryService = new OpenV4JDiscoveryService(spswBridgeHandler);
        this.discoveryServiceRegs.put(spswBridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SpswBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                OpenV4JDiscoveryService service = (OpenV4JDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

}
