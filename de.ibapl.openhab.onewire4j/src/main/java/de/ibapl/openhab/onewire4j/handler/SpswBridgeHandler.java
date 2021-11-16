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
package de.ibapl.openhab.onewire4j.handler;

import static de.ibapl.openhab.onewire4j.OneWire4JBindingConstants.THING_TYPE_ONEWIRE_TEMPERATURE;
import de.ibapl.onewire4j.AdapterFactory;
import de.ibapl.onewire4j.OneWireAdapter;
import de.ibapl.onewire4j.container.OneWireContainer;
import de.ibapl.onewire4j.container.TemperatureContainer;
import de.ibapl.onewire4j.request.data.SearchCommand;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;

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
    private static final String LOG_SERIAL_PORT = "logSerialPort";

    private static final Logger LOGGER = Logger.getLogger("d.i.e.o.SpswBridgeHandler");

    private String port;
    private BigDecimal refreshRate;
    private boolean logSerialPort;

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
        LOGGER.info("Initializing SpswBridgeHandler.");

        Configuration config = getThing().getConfiguration();

        port = (String) config.get(PORT_PARAM);
        refreshRate = (BigDecimal) config.get(REFRESH_RATE_PARAM);
        if (refreshRate == null) {
            refreshRate = BigDecimal.valueOf(60);
            config.put(REFRESH_RATE_PARAM, refreshRate);
        }
        
        if (!config.containsKey(LOG_SERIAL_PORT)) {
            logSerialPort = false;
        } else {
            logSerialPort = ((Boolean)config.get(LOG_SERIAL_PORT));
        }

        try {
            serialPortSocket = serialPortSocketFactory.open(port);
            if (logSerialPort) {
//Wrap socket with logger 
                String opendString = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
                serialPortSocket = LoggingSerialPortSocket.wrapWithHexOutputStream(serialPortSocket,
                        new FileOutputStream("OneWire_SpswBridgeHandler_" + opendString + ".log.txt"),
                        false,
                        TimeStampLogging.UTC);
            }

            oneWireAdapter = new AdapterFactory().open(serialPortSocket, 3);
            LOGGER.info("Onewire adapter opend");
        } catch (IOException | IllegalStateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            LOGGER.log(Level.SEVERE, "Could not open Onewire adapter", e);
            serialPortSocket = null;
            if (oneWireAdapter != null) {
                try {
                    oneWireAdapter.close();
                    oneWireAdapter = null;
                } catch (Exception ex) {
                    oneWireAdapter = null;
                    LOGGER.log(Level.SEVERE, "Could not shutdown Onewire adapter", ex);
                }
            }
            return;
        }

        //TODO
        boolean ppN;
        try {
            ppN = TemperatureContainer.isAnyTempDeviceUsingParasitePower(oneWireAdapter);
        } catch (Exception e) {
            // TODO: handle exception
            ppN = true;
        }

        final boolean parasitePowerNeeded = ppN;

        refreshJob = scheduler.scheduleWithFixedDelay(this, 0, refreshRate.intValue(), TimeUnit.SECONDS);

        updateStatus(ThingStatus.ONLINE);
        LOGGER.info("Onewire adapter ONLINE");
    }

    @Override
    public void run() {
        final boolean parasitePowerNeeded = true;
        try {
            TemperatureContainer.sendDoConvertRequestToAll(oneWireAdapter, parasitePowerNeeded);
        } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not request parasite power needed", e);
        }

        for (Thing thing : getThing().getThings()) {
            try {
                if (ThingStatusDetail.DISABLED.equals(thing.getStatusInfo().getStatusDetail())) {
                	continue; //just skip this thing
                } 
                final ThingHandler thingHandler = thing.getHandler();
                if (thingHandler instanceof TemperatureHandler) {
                        final TemperatureHandler tempHandler = (TemperatureHandler) thingHandler;
                        tempHandler.readDevice(oneWireAdapter);
                }
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "uncaughth exception(!) in handler for thing: " + thing, t);
            }
        }
    }

    @Override
    public void dispose() {
        LOGGER.info("Close Onewire adapter");

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        if (oneWireAdapter != null) {
            try {
                oneWireAdapter.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not shutdown Onewire adapter", e);
            }
            oneWireAdapter = null;
        }
        if (serialPortSocket != null) {
            try {
                serialPortSocket.close();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not shutdown serial port", e);
            }
            serialPortSocket = null;
        }
        LOGGER.info("Onewire adapter closed");
    }

    public void discover(Consumer<OneWireContainer> consumer) throws IOException {
        oneWireAdapter.searchDevices(SearchCommand.SEARCH_ROM, consumer);
    }

}
