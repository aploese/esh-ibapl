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
package de.ibapl.esh.fhz4j.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import de.ibapl.esh.fhz4j.FHZ4JBindingConstants;
import de.ibapl.esh.fhz4j.handler.Em1000EmHandler;
import de.ibapl.esh.fhz4j.handler.EvoHomeHandler;
import de.ibapl.esh.fhz4j.handler.Hms100TfHandler;
import de.ibapl.esh.fhz4j.handler.RadiatorFht80bHandler;
import de.ibapl.esh.fhz4j.handler.SpswBridgeHandler;
import de.ibapl.esh.fhz4j.handler.UnknownDeviceHandler;
import de.ibapl.esh.fhz4j.internal.discovery.FHZ4JDiscoveryService;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import java.util.HashSet;

/**
 * The {@link FHZ4JHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.fhz4j")
public class FHZ4JHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = Logger.getLogger("d.i.e.f.h.FHZ4JHandlerFactory");

    //TODO ImmutableSet.of(Elements .. e) does not exist???
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;

    static {
        SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
        SUPPORTED_THING_TYPES_UIDS.add(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B);
        SUPPORTED_THING_TYPES_UIDS.add(FHZ4JBindingConstants.BRIDGE_TYPE_FHZ4J_RS232);
        SUPPORTED_THING_TYPES_UIDS.add(FHZ4JBindingConstants.THING_TYPE_FHZ4J_EM_1000_EM);
        SUPPORTED_THING_TYPES_UIDS.add(FHZ4JBindingConstants.THING_TYPE_FHZ4J_HMS_100_TF);
        SUPPORTED_THING_TYPES_UIDS.add(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_EVO_HOME);
        SUPPORTED_THING_TYPES_UIDS.add(FHZ4JBindingConstants.THING_TYPE_FHZ4J_SINGLE_ZONE_THERMOSTAT_EVO_HOME);
        SUPPORTED_THING_TYPES_UIDS.add(FHZ4JBindingConstants.THING_TYPE_FHZ4J_MULTI_ZONE_CONTROLLER_EVO_HOME);
    }

    @Reference
    private SerialPortSocketFactory serialPortSocketFactory; // = new de.ibapl.spsw.jniprovider.SerialPortSocketFactoryImpl();

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B)) {
            return new RadiatorFht80bHandler(thing);
        } else if (thingTypeUID.equals(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_EVO_HOME)) {
            return new EvoHomeHandler(thing);
        } else if (thingTypeUID.equals(FHZ4JBindingConstants.THING_TYPE_FHZ4J_SINGLE_ZONE_THERMOSTAT_EVO_HOME)) {
            return new EvoHomeHandler(thing);
        } else if (thingTypeUID.equals(FHZ4JBindingConstants.THING_TYPE_FHZ4J_MULTI_ZONE_CONTROLLER_EVO_HOME)) {
            return new EvoHomeHandler(thing);
        } else if (thingTypeUID.equals(FHZ4JBindingConstants.THING_TYPE_FHZ4J_EM_1000_EM)) {
            final Em1000EmHandler em1000EmHandler = new Em1000EmHandler(thing);
            return em1000EmHandler;
        } else if (thingTypeUID.equals(FHZ4JBindingConstants.THING_TYPE_FHZ4J_HMS_100_TF)) {
            final Hms100TfHandler hms100TkHandler = new Hms100TfHandler(thing);
            return hms100TkHandler;
        } else if (thingTypeUID.equals(FHZ4JBindingConstants.BRIDGE_TYPE_FHZ4J_RS232)) {
            if (serialPortSocketFactory == null) {
                logger.severe("serialPortSocketFactory == null");
                //TODO 
                return null;
            } else {
                final SpswBridgeHandler spswBridgeHandler = new SpswBridgeHandler((Bridge) thing, serialPortSocketFactory);
                registerDiscoveryService(spswBridgeHandler);
                return spswBridgeHandler;
            }
        } else if (thingTypeUID.equals(FHZ4JBindingConstants.THING_TYPE_FHZ4J_UNKNOWN)) {
            return new UnknownDeviceHandler(thing);
        } else {
            return null;
        }
    }

    private synchronized void registerDiscoveryService(SpswBridgeHandler spswBridgeHandler) {
        FHZ4JDiscoveryService discoveryService = new FHZ4JDiscoveryService(spswBridgeHandler);
        this.discoveryServiceRegs.put(spswBridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SpswBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                FHZ4JDiscoveryService service = (FHZ4JDiscoveryService) bundleContext
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
