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
package de.ibapl.openhab.openv4j.handler;

import de.ibapl.openv4j.spi.protocolhandlers.OpenV4JAdapter;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.api.Speed;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.SupressReadTimeoutExceptionLogWriter;
import de.ibapl.spsw.logging.TimeStampLogging;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
//TODO rename to something like CulHandler
public class SpswBridgeHandler extends BaseBridgeHandler {

    private final List<SerialPortSocketFactory> serialPortSocketFactories;

    private static final String PORT_PARAM = "port";
    private static final String REFRESH_RATE_PARAM = "refreshrate";
    private static final String LOG_SERIAL_PORT_PARAM = "logSerialPort";

    private static final Logger LOGGER = Logger.getLogger("d.i.o.ov.h.SpswBridgeHandler");

    private String port;
    private BigDecimal refreshRate;
    private boolean logSerialPort;
    private ScheduledFuture<?> refreshJob;
    private OpenV4JAdapter openV4JAdapter;

    public SpswBridgeHandler(Bridge bridge, List<SerialPortSocketFactory> serialPortSocketFactories) {
        super(bridge);
        this.serialPortSocketFactories = serialPortSocketFactories;
    }

    private SerialPortSocket createSerialPortSocket() throws IOException {
        String opendString = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        final SerialPortSocket sps = serialPortSocketFactories.open(port);
        if (logSerialPort) {
            LoggingSerialPortSocket result = LoggingSerialPortSocket.wrapWithCustomOutputStream(sps,
                    new SupressReadTimeoutExceptionLogWriter(new FileOutputStream("CUL_SpswBridgeHandler_" + opendString + ".log.txt"),
                            true,
                            TimeStampLogging.UTC,
                            true));
            return result;
        } else {
            return sps;
        }
    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "Initializing SpswBridgeHandler.");

        Configuration config = getThing().getConfiguration();

        port = (String) config.get(PORT_PARAM);
        refreshRate = (BigDecimal) config.get(REFRESH_RATE_PARAM);
        if (refreshRate == null) {
            refreshRate = BigDecimal.valueOf(60);
            config.put(REFRESH_RATE_PARAM, refreshRate);
        }

        if (!config.containsKey(LOG_SERIAL_PORT_PARAM)) {
            logSerialPort = false;
        } else {
            logSerialPort = ((Boolean) config.get(LOG_SERIAL_PORT_PARAM));
        }

        try {
            openV4JAdapter = new OpenV4JAdapter(createSerialPortSocket());
            initAdapter();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            try {
                openV4JAdapter.close();
            } catch (Exception e1) {
                LOGGER.log(Level.SEVERE, "Could not shutdown OpenV4JAdapter", e);
            }
            openV4JAdapter = null;
            return;
        }

        refreshJob = scheduler.scheduleWithFixedDelay(this::scheduledAquireData, 0, refreshRate.intValue(), TimeUnit.SECONDS);
        updateStatus(ThingStatus.ONLINE);
        LOGGER.log(Level.INFO, "OpenV4JAdapter initialized");
    }

    @Override
    public void dispose() {
        if (openV4JAdapter != null) {
            OpenV4JAdapter cp = openV4JAdapter;
            openV4JAdapter = null;
            if (cp != null) {
                try {
                    cp.close();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not shutdown OpenV4JAdapter", e);
//Set cp to null, so the gc can collect it.
//TODO How to suppress warning??                    @SuppressWarnings("Unused Assignment")
                    cp = null;
                    //Trigger gc to get rid of current cp ...
                    System.gc();
                }
            }
        }

        LOGGER.log(Level.INFO, "OpenV4JAdapter disposed");
    }

    private void initAdapter() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void scheduledAquireData() {
        final boolean parasitePowerNeeded = true;
        try {
            TemperatureContainer.sendDoConvertRequestToAll(oneWireAdapter, parasitePowerNeeded);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not request parasite power needed", e);
        }

        for (Thing thing : getThing().getThings()) {
            if (ThingStatusDetail.DISABLED.equals(thing.getStatusInfo().getStatusDetail())) {
                continue; //just skip this thing
            }
            final ThingHandler thingHandler = thing.getHandler();
            //In case of an error try 3 times to readout the device
            for (int i = 0; i < 3; i++) {
                try {
                    if (thingHandler instanceof TemperatureHandler temperatureHandler) {
                        temperatureHandler.readDevice(oneWireAdapter);
                        break;
                    } else if (thingHandler instanceof HumidityHandler humidityHandler) {
                        humidityHandler.readDevice(oneWireAdapter);
                        break;
                    }
                } catch (IOException ioe) {
                    handleIOException(ioe);
                } catch (Throwable t) {
                    if (i < 3) {
                        LOGGER.log(Level.WARNING, "Could not read Device in round(max 3): " + i, t);
                    } else {
                        LOGGER.log(Level.WARNING, "Could not read Device, max(3) tries reached!", t);
                    }
                }
            }
        }
    }

}
