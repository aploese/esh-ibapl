package de.ibapl.esh.onewire4j.handler;

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

import static de.ibapl.esh.onewire4j.OneWire4JBindingConstants.THING_TYPE_ONEWIRE_TEMPERATURE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;

import de.ibapl.onewire4j.AdapterFactory;
import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.TemperatureContainer;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
public class SpswBridgeHandler extends BaseBridgeHandler implements Runnable {

    private final SerialPortSocketFactory serialPortSocketFactory;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_ONEWIRE_TEMPERATURE)
            .collect(Collectors.toSet());

    private static final String PORT_PARAM = "port";
    private static final String REFRESH_RATE_PARAM = "refreshrate";

    private final Logger logger = Logger.getLogger("esh.binding.onewire4j");

    private String port;
    private BigDecimal refreshRate;
    private ScheduledFuture<?> refreshJob;
    private SerialPortSocket serialPortSocket;
    private OneWireAdapter oneWireAdapter;

    public SpswBridgeHandler(Bridge bridge, SerialPortSocketFactory serialPortSocketFactory) {
        super(bridge);
        this.serialPortSocketFactory = serialPortSocketFactory;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        logger.fine("Initializing SpswBridgeHandler.");

        Configuration config = getThing().getConfiguration();

        port = (String) config.get(PORT_PARAM);
        refreshRate = (BigDecimal) config.get(REFRESH_RATE_PARAM);
        if (refreshRate == null) {
            refreshRate = BigDecimal.valueOf(60);
            config.put(REFRESH_RATE_PARAM, refreshRate);
        }

        serialPortSocket = serialPortSocketFactory.createSerialPortSocket(port);
        try {
            oneWireAdapter = new AdapterFactory().open(serialPortSocket);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            serialPortSocket = null;
            return;
        }
        boolean ppN;
        try {
            ppN = TemperatureContainer.isParasitePower(oneWireAdapter);
        } catch (Exception e) {
            // TODO: handle exception
            ppN = true;
        }

        final boolean parasitePowerNeeded = ppN;

        refreshJob = scheduler.scheduleWithFixedDelay(this, 0, refreshRate.intValue(), TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public void run() {
        final boolean parasitePowerNeeded = true;
        try {
            try {
                TemperatureContainer.sendDoConvertRequestToAll(oneWireAdapter, parasitePowerNeeded);
            } catch (Exception e) {
                // TODO: handle exception
            }

            for (Thing thing : getThing().getThings()) {
                final ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof TemperatureHandler) {
                    final TemperatureHandler tempHandler = (TemperatureHandler) thingHandler;
                    tempHandler.readDevice(oneWireAdapter);
                    // tempH.updateMinTemperature(0);
                    // tempH.updateMaxTemperature(100);
                }
            }

        } catch (Exception e) {
            logger.logp(Level.SEVERE, this.getClass().getName(), "run()", "Exception occurred during execution", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        if (oneWireAdapter != null) {
            try {
                oneWireAdapter.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            oneWireAdapter = null;
        }
        if (serialPortSocket != null) {
            try {
                serialPortSocket.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
            serialPortSocket = null;
        }
    }

    public void discover(Consumer<OneWireContainer> consumer) throws IOException {
        oneWireAdapter.searchDevices(consumer);
    }

}
