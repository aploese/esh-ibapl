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

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
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

import de.ibapl.fhz4j.cul.CulAdapter;
import de.ibapl.fhz4j.cul.CulMessage;
import de.ibapl.fhz4j.protocol.em.EmMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeMessage;
import de.ibapl.fhz4j.protocol.fht.FhtMessage;
import de.ibapl.fhz4j.protocol.fht.FhtProperty;
import de.ibapl.fhz4j.protocol.fs20.FS20Message;
import de.ibapl.fhz4j.protocol.hms.HmsMessage;
import de.ibapl.fhz4j.protocol.lacrosse.tx2.LaCrosseTx2Message;
import de.ibapl.spsw.api.SerialPortSocket;
import de.ibapl.spsw.api.SerialPortSocketFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import de.ibapl.fhz4j.api.FhzHandler;
import de.ibapl.fhz4j.api.Protocol;
import de.ibapl.fhz4j.cul.CulMessageListener;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeDeviceMessage;
import java.util.EnumSet;

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
            emh.updateFromMsg(emMsg);
        }

        @Override
        public void failed(Throwable arg0) {
            // TODO Auto-generated method stub

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
            rfh.updateFromMsg(fhtMsg);
        }

        @Override
        public void fs20DataParsed(FS20Message arg0) {
            // TODO Auto-generated method stub

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
            hmsh.updateFromMsg(hmsMsg);
        }

        @Override
        public void laCrosseTxParsed(LaCrosseTx2Message arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void culMessageParsed(CulMessage arg0) {
            //no-op

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
                reh.updateFromMsg(edm);
            } else {
                LOGGER.severe("EvoHome unhandled MSG:  " + evoHomeMsg);
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

    }

    private final SerialPortSocketFactory serialPortSocketFactory;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_FHZ4J_RADIATOR_FHT80B, THING_TYPE_FHZ4J_RADIATOR_FHT80B)
            .collect(Collectors.toSet());

    private static final String PORT_PARAM = "port";
    private static final String REFRESH_RATE_PARAM = "refreshrate";
    private static final String HOUSE_CODE_PARAM = "housecode";
    private static final String PROTOCOL_FHT_PARAM = "protocolFHT";
    private static final String PROTOCOL_EVO_HOME_PARAM = "protocolEvoHome";

    private static final Logger LOGGER = Logger.getLogger("esh.binding.fhz4j");

    private String port;
    private short housecode;
    private int refreshRate;
    private boolean protocolEvoHome;
    private boolean protocolFHT;

    private ScheduledFuture<?> refreshJob;
    private CulAdapter culAdapter;
    private final Map<Short, RadiatorFht80bHandler> fhtThingHandler = new HashMap<>();
    private final Map<Integer, EvoHomeHandler> evoHomeThingHandler = new HashMap<>();
    private final Map<Short, Hms100TfHandler> hmsThingHandler = new HashMap<>();
    private final Map<Short, Em1000EmHandler> emThingHandler = new HashMap<>();
    private CulMessageListener discoveryListener;

    public SpswBridgeHandler(Bridge bridge, SerialPortSocketFactory serialPortSocketFactory) {
        super(bridge);
        this.serialPortSocketFactory = serialPortSocketFactory;
        protocolFHT = true;
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
            LOGGER.info("Added FHT 80B " + rfh.getHousecode());
            try {
                if ((culAdapter != null) & protocolFHT) {
                    culAdapter.initFhtReporting(rfh.getHousecode());
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        } else if (childHandler instanceof EvoHomeHandler) {
            final EvoHomeHandler reh = (EvoHomeHandler) childHandler;
            evoHomeThingHandler.put(((EvoHomeHandler) childHandler).getDeviceId(), reh);
            LOGGER.info("Added Evo Home device " + reh.getDeviceId());
            try {
                if ((culAdapter != null) & protocolEvoHome) {
                    culAdapter.initEvoHome();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        } else if (childHandler instanceof Em1000EmHandler) {
            final Em1000EmHandler emh = (Em1000EmHandler) childHandler;
            emThingHandler.put(emh.getAddress(), emh);
            LOGGER.info("Added EM 1000 EM " + emh.getAddress());
            return;
        } else if (childHandler instanceof Hms100TfHandler) {
            final Hms100TfHandler hmsh = (Hms100TfHandler) childHandler;
            hmsThingHandler.put(hmsh.getHousecode(), hmsh);
            LOGGER.info("Added HMS 100 TF " + hmsh.getHousecode());
            return;
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
            return;
        } else if (childHandler instanceof EvoHomeHandler) {
            final EvoHomeHandler reh = (EvoHomeHandler) childHandler;
            evoHomeThingHandler.remove(reh.getDeviceId());
            return;
        } else if (childHandler instanceof Em1000EmHandler) {
            final Em1000EmHandler emh = (Em1000EmHandler) childHandler;
            emThingHandler.remove(emh.getAddress());
            return;
        } else if (childHandler instanceof Hms100TfHandler) {
            final Hms100TfHandler hmsh = (Hms100TfHandler) childHandler;
            hmsThingHandler.remove(hmsh.getHousecode());
            return;
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

        refreshRate = ((BigDecimal) config.get(REFRESH_RATE_PARAM)).intValue();

        Object protocol = config.get(PROTOCOL_FHT_PARAM);
        LOGGER.severe("Read protocolFHT from config: " + protocol);
        if (protocol instanceof Boolean) {
            this.protocolFHT = ((Boolean) protocol).booleanValue();
        } else {
            LOGGER.severe("Throw away stored protocolFHT configuration: " + protocol);
        }

        protocol = config.get(PROTOCOL_EVO_HOME_PARAM);
        LOGGER.severe("Read protocolEvoHome from config: " + protocol);
        if (protocol instanceof Boolean) {
            this.protocolEvoHome = ((Boolean) protocol).booleanValue();
            if (!((Boolean) protocol).booleanValue()) {
                this.protocolFHT = true;
            }
        } else {
            LOGGER.severe("Throw away stored protocolEvoHome configuration: " + protocol);
            this.protocolFHT = true;
        }

        SerialPortSocket serialPortSocket = serialPortSocketFactory.createSerialPortSocket(port);

        fhtThingHandler.clear();
        emThingHandler.clear();
        hmsThingHandler.clear();

        try {
            culAdapter = new CulAdapter(serialPortSocket, new Listener());
            culAdapter.open();
            if (protocolEvoHome) {
                culAdapter.initEvoHome();
            } else if (protocolFHT) {
                culAdapter.initFhz(housecode);
            } else {
                //TODO Fal back
                culAdapter.initFhz(housecode);
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            try {
                culAdapter.close();
            } catch (Exception e1) {
                LOGGER.log(Level.SEVERE, "Could not shutdown fhzAdapter", e);
            }
            culAdapter = null;
            return;
        }

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                culAdapter.initFhtReporting(fhtThingHandler.keySet());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Could not init fht reporting", e);
            }
        }, 0, refreshRate, TimeUnit.DAYS);

        updateStatus(ThingStatus.ONLINE);
        LOGGER.log(Level.INFO, "FhzAdapter initialized");
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }

        if (culAdapter != null) {
            FhzHandler cp = culAdapter;
            culAdapter = null;
            if (cp != null) {
                try {
                    cp.close();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not shutdown fhzAdapter", e);
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
        culAdapter.writeFhtModeAuto(housecode);
    }

    public void sendFhtModeManuMessage(short housecode) throws IOException {
        culAdapter.writeFhtModeManu(housecode);
    }

    public void sendFhtMessage(short housecode, FhtProperty fhtProperty, float value) throws IOException {
        culAdapter.writeFht(housecode, fhtProperty, value);
    }

    public void sendFhtMessage(short housecode, DayOfWeek dayOfWeek, LocalTime from1, LocalTime to1, LocalTime from2,
            LocalTime to2) throws IOException {
        culAdapter.writeFhtCycle(housecode, dayOfWeek, from1, to1, from2, to2);
    }

    public void sendFhtPartyMessage(short housecode, float temp, LocalDateTime to) throws IOException {
        culAdapter.writeFhtModeParty(housecode, temp, to);
    }

    public void sendFhtHolidayMessage(short housecode, float temp, LocalDate to) throws IOException {
        culAdapter.writeFhtModeHoliday(housecode, temp, to);
    }

}
