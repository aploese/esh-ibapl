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
package de.ibapl.esh.fhz4j.handler;

import static de.ibapl.esh.fhz4j.FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B;
import de.ibapl.fhz4j.api.FhzHandler;
import de.ibapl.fhz4j.api.Protocol;
import de.ibapl.fhz4j.cul.CulAdapter;
import de.ibapl.fhz4j.cul.CulMessage;
import de.ibapl.fhz4j.cul.CulMessageListener;
import de.ibapl.fhz4j.protocol.em.EmMessage;
import de.ibapl.fhz4j.protocol.evohome.DeviceId;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeDeviceMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeMessage;
import de.ibapl.fhz4j.protocol.evohome.ZoneTemperature;
import de.ibapl.fhz4j.protocol.fht.FhtMessage;
import de.ibapl.fhz4j.protocol.fht.FhtProperty;
import de.ibapl.fhz4j.protocol.fs20.FS20Message;
import de.ibapl.fhz4j.protocol.hms.HmsMessage;
import de.ibapl.fhz4j.protocol.lacrosse.tx2.LaCrosseTx2Message;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import de.ibapl.spsw.logging.LoggingSerialPortSocket;
import de.ibapl.spsw.logging.TimeStampLogging;
import de.ibapl.spsw.logging.LogExplainRead;
import de.ibapl.spsw.logging.LogExplainWrite;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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

/**
 *
 * @author aploese@gmx.de - Initial contribution
 */
//TODO rename to something like CulHandler
public class SpswBridgeHandler extends BaseBridgeHandler {

    private class Listener implements CulMessageListener {

        @Override
        public void emDataParsed(EmMessage emMsg) {
            final Em1000EmHandler emh = emThingHandler.get(emMsg.address);
            if (emh == null) {
                // Discovery
                if (discoveryListener != null) {
                    discoveryListener.emDataParsed(emMsg);
                }
                return;
            }
            if (logExplainRead != null) {
                logExplainRead.explainRead("EM Message: %s", emMsg);
            }
            emh.updateFromMsg(emMsg);
        }

        @Override
        public void failed(Throwable t) {
            if (logExplainRead != null) {
                logExplainRead.explainRead(t);
            }
        }

        @Override
        public void fhtDataParsed(FhtMessage fhtMsg) {
            final RadiatorFht80bHandler rfh = fhtThingHandler.get(fhtMsg.housecode);
            if (rfh == null) {
                // Discovery
                if (discoveryListener != null) {
                    discoveryListener.fhtDataParsed(fhtMsg);
                }
                return;
            }
            if (logExplainRead != null) {
                logExplainRead.explainRead("FHT Message: %s", fhtMsg);
            }
            rfh.updateFromMsg(fhtMsg);
        }

        @Override
        public void fs20DataParsed(FS20Message fs20Msg) {
            if (logExplainRead != null) {
                logExplainRead.explainRead("FS20 Message: %s", fs20Msg);
            }
        }

        @Override
        public void hmsDataParsed(HmsMessage hmsMsg) {
            final Hms100TfHandler hmsh = hmsThingHandler.get(hmsMsg.housecode);
            if (hmsh == null) {
                // Discovery
                if (discoveryListener != null) {
                    discoveryListener.hmsDataParsed(hmsMsg);
                }
                return;
            }
            if (logExplainRead != null) {
                logExplainRead.explainRead("HMS Message: %s", hmsMsg);
            }
            hmsh.updateFromMsg(hmsMsg);
        }

        @Override
        public void laCrosseTxParsed(LaCrosseTx2Message msg) {
            if (logExplainRead != null) {
                logExplainRead.explainRead("LaCrosseTx2Msg Message: %s", msg);
            }
        }

        @Override
        public void culMessageParsed(CulMessage msg) {
            if (logExplainRead != null) {
                logExplainRead.explainRead("CUL Message: %s", msg);
            }
        }

        @Override
        public void evoHomeParsed(EvoHomeMessage evoHomeMsg) {
            if (evoHomeMsg instanceof EvoHomeDeviceMessage) {
                final EvoHomeDeviceMessage edm = (EvoHomeDeviceMessage) evoHomeMsg;
                final EvoHomeHandler reh = evoHomeThingHandler.get(edm.deviceId1.id);
                if (reh == null) {
                    // Discovery
                    if (discoveryListener != null) {
                        discoveryListener.evoHomeParsed(edm);
                    }
                    return;
                }
                if (logExplainRead != null) {
                    logExplainRead.explainRead("EvoHome Message: %s", evoHomeMsg);
                }

                reh.updateFromMsg(edm);
            }
        }

        @Override
        public void signalStrength(float signalStrength) {
            //no-op
        }

        @Override
        public void receiveEnabled(Protocol protocol) {
            //no-op
        }

        @Override
        public void helpParsed(String helpMessages) {
            //no-op
        }

        @Override
        public void onIOException(IOException ioe) {
            LOGGER.log(Level.WARNING, "Got IOE in CUL Adapter", ioe);
            try {
                culAdapter.close();
            } catch (Exception e) {
            }
            try {
                Thread.sleep(5000);//try to recover => 5 s rest
            } catch (InterruptedException ex) {
                //nothing to do
            }
            try {
                culAdapter = new CulAdapter(getSerialPortSocket(), this);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ioe.getMessage());
                LOGGER.log(Level.SEVERE, "Can't reopen Serial port", e);
            }
        }

    }

    private final SerialPortSocketFactory serialPortSocketFactory;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_FHZ4J_RADIATOR_FHT80B, THING_TYPE_FHZ4J_RADIATOR_FHT80B)
            .collect(Collectors.toSet());

    private static final String PORT_PARAM = "port";
    private static final String HOUSE_CODE_PARAM = "housecode";
    private static final String PROTOCOL_FHT_PARAM = "protocolFHT";
    private static final String PROTOCOL_EVO_HOME_PARAM = "protocolEvoHome";
    private static final String LOG_SERIAL_PORT = "logSerialPort";

    private static final Logger LOGGER = Logger.getLogger("d.i.e.f.h.SpswBridgeHandler");

    private String port;
    private short housecode;
    private boolean protocolEvoHome;
    private boolean protocolFHT;
    private boolean logSerialPort;

    private CulAdapter culAdapter;
    private final Object writeLock = new Object();
    private final Map<Short, RadiatorFht80bHandler> fhtThingHandler = new HashMap<>();
    private final Map<Integer, EvoHomeHandler> evoHomeThingHandler = new HashMap<>();
    private final Map<Short, Hms100TfHandler> hmsThingHandler = new HashMap<>();
    private final Map<Short, Em1000EmHandler> emThingHandler = new HashMap<>();
    private CulMessageListener discoveryListener;
    private LogExplainRead logExplainRead;
    private LogExplainWrite logExplainWrite;

    public SpswBridgeHandler(Bridge bridge, SerialPortSocketFactory serialPortSocketFactory) {
        super(bridge);
        this.serialPortSocketFactory = serialPortSocketFactory;
        protocolFHT = true;
    }

    private SerialPortSocket getSerialPortSocket() throws IOException {
        String opendString = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        final SerialPortSocket sps = serialPortSocketFactory.open(port);
        if (logSerialPort) {
            LoggingSerialPortSocket result = LoggingSerialPortSocket.wrapWithAsciiOutputStream(sps,
                    new FileOutputStream("CUL_SpswBridgeHandler_" + opendString + ".log.txt"),
                    false,
                    TimeStampLogging.UTC.UTC);
            logExplainRead = result;
            logExplainWrite = result;
            return result;
        } else {
            return sps;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        super.childHandlerInitialized(childHandler, childThing);
        if (childHandler instanceof RadiatorFht80bHandler) {
            final RadiatorFht80bHandler rfh = (RadiatorFht80bHandler) childHandler;
            fhtThingHandler.put(rfh.getHousecode(), rfh);
            LOGGER.log(Level.INFO, "Added FHT 80B {0}", rfh.getHousecode());
            try {
                if ((culAdapter != null) & protocolFHT) {
                    synchronized (writeLock) {
                        culAdapter.initFhtReporting(rfh.getHousecode());
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to inti FHT reporting", e);
            }
        } else if (childHandler instanceof EvoHomeHandler) {
            final EvoHomeHandler reh = (EvoHomeHandler) childHandler;
            evoHomeThingHandler.put(((EvoHomeHandler) childHandler).getDeviceId(), reh);
            LOGGER.log(Level.INFO, "Added Evo Home device {0}", reh.getDeviceId());
        } else if (childHandler instanceof Em1000EmHandler) {
            final Em1000EmHandler emh = (Em1000EmHandler) childHandler;
            emThingHandler.put(emh.getAddress(), emh);
            LOGGER.log(Level.INFO, "Added EM 1000 EM {0}", emh.getAddress());
        } else if (childHandler instanceof Hms100TfHandler) {
            final Hms100TfHandler hmsh = (Hms100TfHandler) childHandler;
            hmsThingHandler.put(hmsh.getHousecode(), hmsh);
            LOGGER.log(Level.INFO, "Added HMS 100 TF {0}", hmsh.getHousecode());
        } else {
            // TODO
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        super.childHandlerDisposed(childHandler, childThing);
        if (childHandler instanceof RadiatorFht80bHandler) {
            final RadiatorFht80bHandler rfh = (RadiatorFht80bHandler) childHandler;
            fhtThingHandler.remove(rfh.getHousecode());
        } else if (childHandler instanceof EvoHomeHandler) {
            final EvoHomeHandler reh = (EvoHomeHandler) childHandler;
            evoHomeThingHandler.remove(reh.getDeviceId());
        } else if (childHandler instanceof Em1000EmHandler) {
            final Em1000EmHandler emh = (Em1000EmHandler) childHandler;
            emThingHandler.remove(emh.getAddress());
        } else if (childHandler instanceof Hms100TfHandler) {
            final Hms100TfHandler hmsh = (Hms100TfHandler) childHandler;
            hmsThingHandler.remove(hmsh.getHousecode());
        } else {
            // TODO
        }
    }

    @Override
    public void initialize() {
        LOGGER.log(Level.FINE, "Initializing SpswBridgeHandler.");

        Configuration config = getThing().getConfiguration();

        port = (String) config.get(PORT_PARAM);

        housecode = ((BigDecimal) config.get(HOUSE_CODE_PARAM)).shortValue();

        if (!config.containsKey(LOG_SERIAL_PORT)) {
            logSerialPort = false;
        } else {
            logSerialPort = ((Boolean) config.get(LOG_SERIAL_PORT));
        }

        Object protocol = config.get(PROTOCOL_FHT_PARAM);
        LOGGER.log(Level.SEVERE, "Read protocolFHT from config: {0}", protocol);
        if (protocol instanceof Boolean) {
            this.protocolFHT = ((Boolean) protocol);
        } else {
            LOGGER.log(Level.SEVERE, "Throw away stored protocolFHT configuration: {0}", protocol);
        }

        protocol = config.get(PROTOCOL_EVO_HOME_PARAM);
        LOGGER.log(Level.SEVERE, "Read protocolEvoHome from config: {0}", protocol);
        if (protocol instanceof Boolean) {
            this.protocolEvoHome = ((Boolean) protocol);
            if (!((Boolean) protocol)) {
                this.protocolFHT = true;
            }
        } else {
            LOGGER.log(Level.SEVERE, "Throw away stored protocolEvoHome configuration: {0}", protocol);
            this.protocolFHT = true;
        }

        fhtThingHandler.clear();
        emThingHandler.clear();
        hmsThingHandler.clear();

        try {
            culAdapter = new CulAdapter(getSerialPortSocket(), new Listener());
            if (protocolEvoHome) {
                synchronized (writeLock) {
                    culAdapter.initEvoHome();
                }
            } else if (protocolFHT) {
                synchronized (writeLock) {
                    culAdapter.initFhz(housecode);
                }
            } else {
                //TODO fall back
                synchronized (writeLock) {
                    culAdapter.initFhz(housecode);
                }
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            try {
                culAdapter.close();
            } catch (Exception e1) {
                LOGGER.log(Level.SEVERE, "Could not shutdown fhzAdapter", e);
            }
            culAdapter = null;
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        LOGGER.log(Level.INFO, "FhzAdapter initialized");
    }

    @Override
    public void dispose() {
        if (culAdapter != null) {
            FhzHandler cp = culAdapter;
            culAdapter = null;
            if (cp != null) {
                try {
                    cp.close();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not shutdown fhzAdapter", e);
//TODO How to suppress warning??                    @SuppressWarnings("Unused Assignment")
//Set cp to null, so the gc can collect it.
                    cp = null;
                    //Trigger gc to get rid of current cp ...
                    System.gc();
                    Runtime.getRuntime().runFinalization();
                    System.gc();
                }
            }
        }
        fhtThingHandler.clear();
        emThingHandler.clear();
        hmsThingHandler.clear();
        LOGGER.log(Level.INFO, "FhzAdapter disposed");
    }

    public CulMessageListener getDiscoveryListener() {
        return discoveryListener;
    }

    public void setDiscoveryListener(CulMessageListener discoveryListener) {
        this.discoveryListener = discoveryListener;
    }

    public void sendFhtModeAutoMessage(short housecode) throws IOException {
        synchronized (writeLock) {
            if (logExplainWrite != null) {
                logExplainWrite.explainWrite("Set mode to auto of: %d", housecode);
            }
            culAdapter.writeFhtModeAuto(housecode);
        }
    }

    public void sendFhtModeManuMessage(short housecode) throws IOException {
        synchronized (writeLock) {
            culAdapter.writeFhtModeManu(housecode);
        }
    }

    public void sendFhtMessage(short housecode, FhtProperty fhtProperty, float value) throws IOException {
        synchronized (writeLock) {
            culAdapter.writeFht(housecode, fhtProperty, value);
        }
    }

    public void sendFhtMessage(short housecode, DayOfWeek dayOfWeek, LocalTime from1, LocalTime to1, LocalTime from2,
            LocalTime to2) throws IOException {
        synchronized (writeLock) {
            culAdapter.writeFhtCycle(housecode, dayOfWeek, from1, to1, from2, to2);
        }
    }

    public void sendFhtPartyMessage(short housecode, float temp, LocalDateTime to) throws IOException {
        synchronized (writeLock) {
            culAdapter.writeFhtModeParty(housecode, temp, to);
        }
    }

    public void sendFhtHolidayMessage(short housecode, float temp, LocalDate to) throws IOException {
        synchronized (writeLock) {
            culAdapter.writeFhtModeHoliday(housecode, temp, to);
        }
    }

    public void sendEvoHomeZoneSetpointPermanent(DeviceId deviceId, ZoneTemperature temperature) throws IOException {
        synchronized (writeLock) {
            culAdapter.writeEvoHomeZoneSetpointPermanent(deviceId, temperature);
        }
    }

    public void sendEvoHomeZoneSetpointUntil(DeviceId deviceId, ZoneTemperature temperature, LocalDateTime localDateTime) throws IOException {
        synchronized (writeLock) {
            culAdapter.writeEvoHomeZoneSetpointUntil(deviceId, temperature, localDateTime);
        }
    }

	public void initFhtReporting(short housecode) throws IOException {
		synchronized (writeLock) {
			culAdapter.initFhtReporting(housecode);
		}
	}

    void setClock(short housecode, LocalDateTime localDateTime) throws IOException {
		synchronized (writeLock) {
			culAdapter.writeFhtTimeAndDate(housecode, localDateTime);
		}
    }

}
