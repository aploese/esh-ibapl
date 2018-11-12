package de.ibapl.esh.fhz4j.internal;

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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
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

import com.google.common.collect.ImmutableSet;

import de.ibapl.esh.fhz4j.FHZ4JBindingConstants;
import de.ibapl.esh.fhz4j.handler.Em1000EmHandler;
import de.ibapl.esh.fhz4j.handler.Hms100TfHandler;
import de.ibapl.esh.fhz4j.handler.RadiatorFht80bHandler;
import de.ibapl.esh.fhz4j.handler.SpswBridgeHandler;
import de.ibapl.esh.fhz4j.handler.UnknownDeviceHandler;
import de.ibapl.esh.fhz4j.internal.discovery.FHZ4JDiscoveryService;
import de.ibapl.spsw.api.SerialPortSocketFactory;

/**
 * The {@link FHZ4JHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author aploese@gmx.de - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.fhz4j")
public class FHZ4JHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = Logger.getLogger("esh.binding.fhz4j");

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
            FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B, FHZ4JBindingConstants.BRIDGE_TYPE_FHZ4J_RS232,
            FHZ4JBindingConstants.THING_TYPE_FHZ4J_EM_1000_EM, FHZ4JBindingConstants.THING_TYPE_FHZ4J_HMS_100_TF);

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

    private synchronized void registerDiscoveryService(@NonNull SpswBridgeHandler spswBridgeHandler) {
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
