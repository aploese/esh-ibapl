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
package de.ibapl.openhab.fhz4j.internal.discovery;

import de.ibapl.openhab.fhz4j.FHZ4JBindingConstants;
import de.ibapl.openhab.fhz4j.handler.SpswBridgeHandler;
import de.ibapl.openhab.fhz4j.internal.FHZ4JHandlerFactory;
import de.ibapl.fhz4j.api.Protocol;
import de.ibapl.fhz4j.cul.CulMessage;
import de.ibapl.fhz4j.cul.CulMessageListener;
import de.ibapl.fhz4j.protocol.em.EmMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeDeviceMessage;
import de.ibapl.fhz4j.protocol.evohome.EvoHomeMessage;
import de.ibapl.fhz4j.protocol.fht.Fht80TfMessage;
import de.ibapl.fhz4j.protocol.fht.FhtMessage;
import de.ibapl.fhz4j.protocol.fs20.FS20Message;
import de.ibapl.fhz4j.protocol.hms.HmsMessage;
import de.ibapl.fhz4j.protocol.lacrosse.tx2.LaCrosseTx2Message;
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
public class FHZ4JDiscoveryService extends AbstractDiscoveryService implements CulMessageListener {

    private final static Logger logger = Logger.getLogger("d.i.e.f.h.FHZ4JDiscoveryService");

    private final static int SEARCH_TIME = 15 * 60; // 15 minutes FHT80 sends all 120 sec, HMS 100 TF all 10 min.

    private final SpswBridgeHandler spswBridgeHandler;

    public FHZ4JDiscoveryService(SpswBridgeHandler spswBridgeHandler) {
        super(FHZ4JHandlerFactory.SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.spswBridgeHandler = spswBridgeHandler;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return FHZ4JHandlerFactory.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void startScan() {
        spswBridgeHandler.setDiscoveryListener(this);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        spswBridgeHandler.setDiscoveryListener(null);
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void deactivate() {
        stopScan();
    }

    private void addHms100TfDevice(short housecode) {
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final String deviceIdStr = Short.toString(housecode);
        final ThingUID thingUID = getThingUID(deviceIdStr, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_HMS_100_TF);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_HMS_100_TF).withProperty("housecode", housecode)
                .withBridge(bridgeUID).withRepresentationProperty(Short.toString(housecode))
                .withLabel("HMS 100 TF " + housecode).build();

        thingDiscovered(discoveryResult);
    }

    private void addEm1000EmDevice(short address) {
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final String deviceIdStr = Short.toString(address);
        final ThingUID thingUID = getThingUID(deviceIdStr, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_EM_1000_EM);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_EM_1000_EM).withProperty("address", address)
                .withBridge(bridgeUID).withRepresentationProperty(Short.toString(address))
                .withLabel("EM 1000 EM " + address).build();

        thingDiscovered(discoveryResult);
    }

    private void addRadiatorEvoHomeDevice(int deviceId) {
        final String hexDeviceId = String.format("%06x", deviceId);
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final ThingUID thingUID = getThingUID(hexDeviceId, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_EVO_HOME);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_EVO_HOME)
                .withProperty("deviceId", deviceId)
                .withBridge(bridgeUID)
                .withRepresentationProperty(hexDeviceId)
                .withLabel("EvoHome Radiator 0x" + hexDeviceId)
                .build();

        thingDiscovered(discoveryResult);
    }

    private void addSingleZoneThermostatEvoHomeDevice(int deviceId) {
        final String hexDeviceId = String.format("%06x", deviceId);
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final ThingUID thingUID = getThingUID(hexDeviceId, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_SINGLE_ZONE_THERMOSTAT_EVO_HOME);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_SINGLE_ZONE_THERMOSTAT_EVO_HOME)
                .withProperty("deviceId", deviceId).withBridge(bridgeUID)
                .withRepresentationProperty(hexDeviceId).withLabel("EvoHome Single Zone Thermostat 0x" + hexDeviceId).build();

        thingDiscovered(discoveryResult);
    }

    private void addMultiZoneControllerEvoHomeDevice(int deviceId) {
        final String hexDeviceId = String.format("%06x", deviceId);
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final ThingUID thingUID = getThingUID(hexDeviceId, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_MULTI_ZONE_CONTROLLER_EVO_HOME);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_MULTI_ZONE_CONTROLLER_EVO_HOME)
                .withProperty("deviceId", deviceId).withBridge(bridgeUID)
                .withRepresentationProperty(hexDeviceId).withLabel("EvoHome Multi Zone Controller 0x" + hexDeviceId).build();

        thingDiscovered(discoveryResult);
    }

    private void addFhtDevice(short housecode) {
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final String deviceIdStr = Short.toString(housecode);
        final ThingUID thingUID = getThingUID(deviceIdStr, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_RADIATOR_FHT80B)
                .withProperty("housecode", housecode)
                .withBridge(bridgeUID)
                .withRepresentationProperty("housecode")
                .withLabel("FHT80b " + housecode)
                .build();

        thingDiscovered(discoveryResult);
    }

    private void addFht80TfDevice(int address) {
        final String hexAdress = String.format("%06x", address);
        final ThingUID bridgeUID = spswBridgeHandler.getThing().getUID();
        final String deviceIdStr = hexAdress;
        final ThingUID thingUID = getThingUID(deviceIdStr, bridgeUID,
                FHZ4JBindingConstants.THING_TYPE_FHZ4J_FHT80_TF);

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(FHZ4JBindingConstants.THING_TYPE_FHZ4J_FHT80_TF)
                .withProperty("address", hexAdress)
                .withBridge(bridgeUID)
                .withRepresentationProperty("address")
                .withLabel("FHT80 TF 0x" + hexAdress)
                .build();

        thingDiscovered(discoveryResult);
    }

    private ThingUID getThingUID(String deviceId, ThingUID bridgeUID, ThingTypeUID thingTypeUID) {
        return new ThingUID(thingTypeUID, bridgeUID, deviceId);
    }

    @Override
    public void emDataParsed(EmMessage emMsg) {
        switch (emMsg.emDeviceType) {
            case EM_1000_EM:
                addEm1000EmDevice(emMsg.address);
                break;
            default:
                throw new RuntimeException("EM 1000 TYpe " + emMsg.emDeviceType + " not implemented yet");
        }
    }

    @Override
    public void failed(Throwable arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fhtDataParsed(FhtMessage fhtMsg) {
        addFhtDevice(fhtMsg.housecode);
    }

    @Override
    public void fht80TfDataParsed(Fht80TfMessage fht80TfMsg) {
        addFht80TfDevice(fht80TfMsg.address);
    }

    @Override
    public void fhtPartialDataParsed(FhtMessage arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void fs20DataParsed(FS20Message arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void hmsDataParsed(HmsMessage hmsMsg) {
        switch (hmsMsg.hmsDeviceType) {
            case HMS_100_TF:
                addHms100TfDevice(hmsMsg.housecode);
                break;
            default:
                throw new RuntimeException("HMS TYpe " + hmsMsg.hmsDeviceType + " not implemented yet");
        }
    }

    @Override
    public void laCrosseTxParsed(LaCrosseTx2Message arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void culMessageParsed(CulMessage arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void evoHomeParsed(EvoHomeMessage evoHomeMsg) {
        if (evoHomeMsg instanceof EvoHomeDeviceMessage) {
            final EvoHomeDeviceMessage devMsg = (EvoHomeDeviceMessage) evoHomeMsg;
            switch (devMsg.deviceId1.type) {
                case MULTI_ZONE_CONTROLLER:
                    addMultiZoneControllerEvoHomeDevice(devMsg.deviceId1.id);
                    break;
                case SINGLE_ZONE_THERMOSTAT:
                    addSingleZoneThermostatEvoHomeDevice(devMsg.deviceId1.id);
                    break;
                case RADIATOR_CONTROLLER:
                    addRadiatorEvoHomeDevice(devMsg.deviceId1.id);
                    break;
                default:
                    logger.severe("Cant handle EvoHomeDeviceMessage: " + devMsg);
            }
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
        //no-op
    }

}
